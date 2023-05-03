package cs576;
import java.awt.image.BufferedImage;

import java.io.BufferedWriter;
import java.io.FileWriter;
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
		String outFilename = "output.txt";
		int width = 480;
		int height = 270;
		int numFrames = 8682;
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		
		// Open the video file
		VideoCapture capture = new VideoCapture("longDark.mp4");
		
		if (!capture.isOpened()) {
			System.out.println("Error: cannot open video file");
			System.exit(1);
		}
		
		System.out.println("fps: " + capture.get(Videoio.CAP_PROP_FPS));
		System.out.println("Start Processing...");
		
		List<Integer> shots = new ArrayList<Integer>();
		List<Integer> scenes = new ArrayList<Integer>();
		List<Integer> subshots = new ArrayList<Integer>();
		List<Double> motionsInAShot = new ArrayList<Double>();
		int frameCount = 0;
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
		scenes.add(1);
		
		// parameters... need tweaking
		int windowLen = 15;
		Size windowSize = new Size(windowLen, windowLen);
		int maxLevel = 5;
		TermCriteria criteria = new TermCriteria(TermCriteria.EPS + TermCriteria.COUNT, 100, 0.03);
		double motionSqThreshold = 6000.0; // maximum motion magnitude square allowed
		double subMoChanSqThreshold = 1000;
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
		Mat prevLastHist = new Mat(); // Color Histograms of the last frame in the previous shot
		List<Mat> prevHSVList = Arrays.asList(prevHSV);
		List<Mat> currHSVList = new ArrayList<>();
		double histTest = 0.0;
		double histTestThreshold = 80;
		
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
			
//			if(frameId > 1352 && frameId < 2332)
//			{
//				System.out.println("	frame " + frameId + " motion: " + avgMotionSq);
//			}
			if (avgMotionSq > motionSqThreshold && (frameId - prevShotStart < minShotInterval || frameId - prevShotStart > 20))
			{
				currShotStart = frameId;
				if(currShotStart - prevShotStart > minShotInterval)
				{
					shots.add(currShotStart); // get current frame number and add to array 
					++shotCount;
					System.out.println("New shot at frame " + (int)capture.get(Videoio.CAP_PROP_POS_FRAMES));
					System.out.println("	motion " + avgMotionSq + ", hist " + histTest);					
					// scene test with color histograms comparison
					// check if the previous shot is a transition shot, which makes this shot in a new scene
					double transHistTest = Imgproc.compareHist(lastShotHist, currHist, Imgproc.HISTCMP_CHISQR);
					if(histTest <= histTestThreshold && transHistTest > 150)
					{
						System.out.println(" !!!Transition histTest at " + frameId + ": " + transHistTest);
						scenes.add(frameId);
					}else if(histTest > histTestThreshold && transHistTest > 30)
					{
						System.out.println("!!!histTest at " + frameId + ": " + histTest);
						scenes.add(frameId);
					}
					// check for subshots based on motion slow or fast
					boolean slow = motionsInAShot.get(0) > 3 ? false : true;
					int ctnCount = 1, noiseCount = 0, maxNoiseCount = 20, minSubCount = 100;
					int allCount = 1;
					boolean isNewSubShot = true;
					for(int i = 1; i < motionsInAShot.size(); ++i)
					{
						++allCount;
						boolean currSlow = motionsInAShot.get(i) > 30 ? false: true;
						if(slow == currSlow)
						{
							++ctnCount;
							noiseCount = 0;
							if(ctnCount >= minSubCount && isNewSubShot)
							{
								subshots.add(prevShotStart + i - allCount);
								isNewSubShot = false;
								System.out.println("-------Subshot at frame " + (prevShotStart + i - allCount));
							}
						}else {
							++noiseCount;
							if(noiseCount > maxNoiseCount)
							{
								slow = currSlow;
								noiseCount = 0;
								ctnCount = maxNoiseCount;
								isNewSubShot = true;
								allCount = maxNoiseCount;
							}
						}
					}
					
					prevShotStart = currShotStart;
					prevLastHist = prevHist.clone();
					motionsInAShot.clear();
				}else if (currShotStart - scenes.get(scenes.size() - 1) > minShotInterval){ 
					// prevent adding multiple shots where it takes several frames to transition
					// will not add new shot, but needs to check if there is a new scene missed 
					double additionalHistTest = Imgproc.compareHist(prevLastHist, currHist, Imgproc.HISTCMP_CHISQR);
					double transHistTest = Imgproc.compareHist(lastShotHist, currHist, Imgproc.HISTCMP_CHISQR);
					if(histTest > histTestThreshold || additionalHistTest > histTestThreshold)
					{
						System.out.println("!!!histTest at " + prevShotStart + ": " + histTest);
						scenes.add(prevShotStart);
					}else if(transHistTest > 150)
					{
						System.out.println("!!!TransitionHistTest at " + prevShotStart + ": " + transHistTest);
						scenes.add(prevShotStart);
					}
				}
				lastShotHist = currHist.clone();
			}
			else {
				// if is continuous motion, meaning in the same shots, check if there is subshots
				motionsInAShot.add(avgMotionSq);
			}
			
			
			
			// update prevFrame to be used in the next iteration
			//prevFrame = currFrame;
			//prevGray = currGray;
			prevGray = currGray.clone();
			prevHist = currHist.clone();
		}

		System.out.println("Finished processing... total number of frames: " + (int)capture.get(Videoio.CAP_PROP_POS_FRAMES));
		capture.release();
		
		// Output to file
		String outStr = "";
		int subCount = 0;
		try {
            FileWriter fw = new FileWriter(outFilename);
            BufferedWriter writer = new BufferedWriter(fw);
            System.out.print(scenes.size() + " " + shots.size());
            //writer.write(scenes.size() + " " + shots.size() + " ");
            int j = 0, k = 0;
            for (int i = 0; i < shotCount; i++) {
                if(j >= scenes.size() || shots.get(i) < scenes.get(j))
                {
                	System.out.print(shots.get(i) + " ");
                	//writer.write(shots.get(i) + " ");
                	outStr += shots.get(i) + " ";
                }else {
                	System.out.println();
                	//writer.newLine();
                	//writer.write("n ");
                	outStr += "n ";
                	System.out.print(scenes.get(j) + " ");
                	//writer.write(scenes.get(j) + " ");
                	outStr += scenes.get(j) + " ";
                	++j;
                }
               while(k < subshots.size() && subshots.get(k) - shots.get(i) < 60) {
            	   ++k;
               }
            	   
               if(k < subshots.size() && i < shotCount - 1 && shots.get(i + 1) - subshots.get(k) > 60)
               {
            	   System.out.print("(-" + subshots.get(k) + ") " );
            	   //writer.write("-" + subshots.get(k) + " ");
            	   outStr += "-" + subshots.get(k) + " ";
            	   ++subCount;
               }
            }
            
            String counters = scenes.size() + " " + shots.size() + " " + subCount + " ";
            writer.write(counters + outStr);
            writer.close();
            System.out.println("Finished writing results to " + outFilename);
        } catch (IOException e) {
            System.out.println("Error writing to file " + outFilename);
            e.printStackTrace();
        }
		
		System.exit(0);
	}
}
