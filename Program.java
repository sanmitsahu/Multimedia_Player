package cs576;
import java.awt.image.BufferedImage;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.ImageIcon;
import org.opencv.core.*;
import org.opencv.video.*;
import org.opencv.imgproc.*;
import org.opencv.videoio.*;

public class Program {	
	public static void main(String[] args) {
		int width = 480;
		int height = 270;
		int numFrames = 8682;
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		
		// Open the video file
		VideoCapture capture = new VideoCapture("InputVideo.mp4");
		if (!capture.isOpened()) {
			System.out.println("Error: cannot open video file");
			System.exit(1);
		}
		
		System.out.println("Start Processing...");
		
		ArrayList<Integer> shots = new ArrayList<Integer>();
		int shotCount = 0;
		
		// Read the first frame
		Mat currFrame = new Mat();
		Mat prevFrame = new Mat();
		Mat prevGray = new Mat();
		Mat currGray = new Mat();
		capture.read(prevFrame);
		Imgproc.cvtColor(prevFrame, prevGray, Imgproc.COLOR_BGR2GRAY);
		shots.add(1);
		++shotCount;
		
		// parameters... need tweaking
		int windowLen = 15;
		Size windowSize = new Size(windowLen, windowLen);
		int maxLevel = 5;
		TermCriteria criteria = new TermCriteria(TermCriteria.EPS + TermCriteria.COUNT, 100, 0.03);
		double motionSqThreshold = 5000.0; // maximum motion magnitude square allowed
		double noMatchRatioThreshold = 1; // not sure whether this is needed...might delete later
		
		// variables to hold motion vector results
		MatOfPoint2f prevPoints = new MatOfPoint2f();
		MatOfPoint2f currPoints = new MatOfPoint2f();
		MatOfByte status = new MatOfByte();
		MatOfFloat err = new MatOfFloat();
		
		// initialize points (centers of the tracking blocks)
		for(int x = windowLen / 2 ; x < width - windowLen / 2; x += windowLen)
		{
			for(int y = windowLen; y < height - windowLen / 2; y += windowLen)
			{
				prevPoints.push_back(new MatOfPoint2f(new Point(x, y)));
			}
		}
		// might want to try only tracking good features like edge or corner...?
		//Imgproc.goodFeaturesToTrack(prevGray, prevPoints, 500, 0.01, 10, Mat(), 10, 3, 0, 0.04);
		
		
		// Web site for reference for motion vector calculation:
		// https://docs.opencv.org/3.4/dc/d6b/group__video__track.html#ga473e4b886d0bcc6b65831eb88ed93323
		
		// color histograms variable for scene cutting
		Mat prevHSV = new Mat(), currHSV = new Mat();
		Imgproc.cvtColor(prevFrame, prevHSV, Imgproc.COLOR_BGR2HSV);
		int hBins = 50, sBins = 60;
		int[] histSize = {hBins, sBins};
		// hue varies from 0 to 179, saturation from 0 to 255
        float[] ranges = { 0, 180, 0, 256 };
        int[] channels = {0, 1}; // using H and S channel
		boolean accumulate = false;
		Mat prevHist = new Mat(), currHist = new Mat();
		Mat lastShotHist = new Mat(); // Color Histograms of the first frame in the previous shot
		List<Mat> prevHSVList = Arrays.asList(prevHSV);
		List<Mat> currHSVList = new ArrayList<>();
		double histTest = 0.0;
		double histTestThreshold = 100;
		
		// compute color histogram for the first frame
		Imgproc.calcHist(prevHSVList, new MatOfInt(channels), new Mat(), prevHist, new MatOfInt(histSize), new MatOfFloat(ranges), accumulate);
		Core.normalize(prevHist, prevHist, 0, 1, Core.NORM_MINMAX);
		lastShotHist = prevHist.clone();
        
		int prevShotStart = 1;
		int currShotStart = 1;
		int minShotInterval = 3;
		// for each frame, compare it with the previous one using motion vector, if diff > threshold, we say it's the start of a new shot
		while(capture.read(currFrame))
		{
			int frameId = (int)capture.get(Videoio.CAP_PROP_POS_FRAMES);
			Imgproc.cvtColor(currFrame, currGray, Imgproc.COLOR_BGR2GRAY);
			Imgproc.cvtColor(currFrame, currHSV, Imgproc.COLOR_BGR2HSV);
			
			// compute color histograms for current frame
			currHSVList = Arrays.asList(currHSV);
			Imgproc.calcHist(currHSVList, new MatOfInt(channels), new Mat(), currHist, new MatOfInt(histSize), new MatOfFloat(ranges), accumulate);
			Core.normalize(currHist, currHist, 0, 1, Core.NORM_MINMAX);
			
			// compute motion vectors 
			Video.calcOpticalFlowPyrLK(prevGray, currGray, prevPoints, currPoints, status, err, windowSize, maxLevel, criteria);
			
			// analyze the motion vectors
			double totalMotionSq = 0.0;
			int noMatchNumThreshold = (int)(prevPoints.rows() * noMatchRatioThreshold);
			int noMatchNum = 0;
			for(int i = 0; i < prevPoints.rows(); ++i) {
				if(status.get(i, 0)[0] == 1)
				{ // if there exists a match
					double[] displacement = {currPoints.get(i, 0)[0] - prevPoints.get(i, 0)[0], currPoints.get(i, 0)[1] - prevPoints.get(i, 0)[1]};
					double motionMagnitudeSq = Math.pow(displacement[0], 2) + Math.pow(displacement[1], 2);
					totalMotionSq += motionMagnitudeSq;
				}else
				{ // no match is found for this window
					++noMatchNum;
				}
			}
			
			// decide if this frame is a new scene
			histTest = Imgproc.compareHist(prevHist, currHist, Imgproc.HISTCMP_CHISQR);
			if(histTest > histTestThreshold)
			{
				//System.out.println("histTest at " + frameId + ": " + histTest);
			}
			
			// decide if this frame is in a new shot
			// if too many no match window or if the motion is too large, then we say it's a new shot
			double avgMotionSq = 0.0;
			if(noMatchNum < prevPoints.rows()) {
				avgMotionSq= totalMotionSq / (prevPoints.rows() - noMatchNum);
			}
			if (avgMotionSq > motionSqThreshold)
			{
				currShotStart = frameId;
				if(currShotStart - prevShotStart > minShotInterval)
				{
					shots.add(currShotStart); // get current frame number and add to array 
					++shotCount;
					System.out.println("New shot at frame " + (int)capture.get(Videoio.CAP_PROP_POS_FRAMES));
					prevShotStart = currShotStart;
					
					// check if the previous shot is a transition shot, which makes this shot in a new scene
					double transHistTest = Imgproc.compareHist(lastShotHist, currHist, Imgproc.HISTCMP_CHISQR);
					if(histTest <= histTestThreshold && transHistTest > 200)
					{
						System.out.println("!!!Transition histTest at " + frameId + ": " + transHistTest);
					}else if(histTest > histTestThreshold && transHistTest > 100)
					{
						System.out.println("!!!histTest at " + frameId + ": " + histTest);
					}
					
					lastShotHist = currHist.clone();
				}
			}
			
			// update prevFrame to be used in the next iteration
			//prevFrame = currFrame;
			//prevGray = currGray;
			prevGray = currGray.clone();
			prevHist = currHist.clone();
		}

		System.out.println("Finished processing... total number of frames: " + (int)capture.get(Videoio.CAP_PROP_POS_FRAMES));
		capture.release();
		System.exit(0);
	}
}
