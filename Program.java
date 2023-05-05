package cs576;
import java.awt.image.BufferedImage;
import javax.sound.sampled.*;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.transform.DftNormalization;
import org.apache.commons.math3.transform.FastFourierTransformer;
import org.apache.commons.math3.transform.TransformType;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;
import be.tarsos.dsp.io.jvm.AudioDispatcherFactory;
import be.tarsos.dsp.mfcc.MFCC;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
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
	public static void main(String[] args) throws UnsupportedAudioFileException, IOException, LineUnavailableException {
		
		   
		   // This code perform FFT
		   String filePath ="/Users/bassel/Downloads/The_Great_Gatsby_rgb/InputAudio.wav";
	       try {
	           performFFTWithPadding(filePath);
	       } catch (UnsupportedAudioFileException | IOException e) {
	           e.printStackTrace();
	       }
		   
		   
		   
		   //This part is Pitch-Detection
		   
		   try {
	           String audioFilePath = "/Users/bassel/Downloads/The_Great_Gatsby_rgb/InputAudio.wav";
	           int sampleRate = 44100;
	           int bufferSize = 2048;
	           int bufferOverlap = 1024;

	           AudioDispatcher audioDispatcher = AudioDispatcherFactory.fromFile(new File(audioFilePath), bufferSize, bufferOverlap);
	           MFCC mfccProcessor = new MFCC(bufferSize, sampleRate, 13, 20, 50, sampleRate / 2);

	           audioDispatcher.addAudioProcessor(mfccProcessor);
	           audioDispatcher.addAudioProcessor(new AudioProcessor() {
	               public boolean process(AudioEvent audioEvent) {
	                   float[] mfcc = mfccProcessor.getMFCC();
	                   for (int i = 0; i < mfcc.length; i++) {
	                       System.out.printf("MFCC[%d]: %f\n", i, mfcc[i]);
	                   }
	                   return true;
	               }

	               @Override
	               public void processingFinished() {
	               }
	           });

	           audioDispatcher.run();
	       } catch (UnsupportedAudioFileException | IOException e) {
	           e.printStackTrace();
	       }
		   
		   
	       String inputFile0 = "/Users/bassel/Downloads/The_Great_Gatsby_rgb/InputAudio.wav";; // Replace with your input file path
	       File audioFile0 = new File(inputFile0);
	       AudioInputStream audioInputStream0 = AudioSystem.getAudioInputStream(audioFile0);
	       AudioFormat baseFormat0 = audioInputStream0.getFormat();

	       int frameSize0 = baseFormat0.getFrameSize();
	       byte[] buffer0 = new byte[frameSize0 * 1024];
	       int bytesRead0;

	       while ((bytesRead0 = audioInputStream0.read(buffer0, 0, buffer0.length)) != -1) {
	           short[] audioData = byteArrayToShortArray(buffer0, bytesRead0, frameSize0);
	           double pitch = detectPitch(audioData, baseFormat0.getSampleRate());
	           System.out.println("Detected pitch: " + pitch);
	       }

	       audioInputStream0.close();
		   
		   
		   
		   //This Part is Audio Segmantion
		   
		   String audioFilePath = "/Users/bassel/Downloads/The_Great_Gatsby_rgb/InputAudio.wav";
	       float threshold = 0.5f; // adjust this value as needed


	       try (AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new File(audioFilePath))) {
	           AudioFormat format = audioInputStream.getFormat();
	           int frameSize = format.getFrameSize();
	           int bufferLength = (int) (format.getFrameRate() * frameSize * 0.1); // 100 ms buffer
	           byte[] audioBuffer = new byte[bufferLength];
	           int bytesRead;
	           int totalBytesRead = 0;
	           float maxAmplitude = 0;
	           boolean inSegment = false;
	           double segmentStartTime = 0;


	           while ((bytesRead = audioInputStream.read(audioBuffer)) != -1) {
	               totalBytesRead += bytesRead;
	               for (int i = 0; i < bytesRead; i += frameSize) {
	                   float amplitude = getAmplitude(audioBuffer, i, frameSize);
	                   if (amplitude > maxAmplitude) {
	                       maxAmplitude = amplitude;
	                   }
	               }
	               if (maxAmplitude > threshold) {
	                   if (!inSegment) {
	                       inSegment = true;
	                       segmentStartTime = (double) totalBytesRead / format.getFrameRate();
	                   }
	               } else {
	                   if (inSegment) {
	                       inSegment = false;
	                       double segmentEndTime = (double) totalBytesRead / format.getFrameRate();
	                       System.out.printf("Segment from %.2f s to %.2f s%n", segmentStartTime, segmentEndTime);
	                   }
	               }
	               maxAmplitude = 0;
	           }
	           if (inSegment) {
	               double segmentEndTime = (double) totalBytesRead / format.getFrameRate();
	               System.out.printf("Segment from %.2f s to %.2f s%n", segmentStartTime, segmentEndTime);
	           }
	       } catch (UnsupportedAudioFileException | IOException ex) {
	           ex.printStackTrace();
	       }
	       
	       
	       
	       
	       
	       
	       //This part gives Sampling Rate and Quantization:
	       String audioFilePathSQ = "/Users/bassel/Downloads/The_Great_Gatsby_rgb/InputAudio.wav";

	       try {
	           File audioFile = new File(audioFilePathSQ);
	           AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(audioFile);
	           AudioFormat audioFormat = audioInputStream.getFormat();

	           int sampleRate = (int) audioFormat.getSampleRate();
	           int bitsPerSample = audioFormat.getSampleSizeInBits();

	           System.out.println("Sampling Rate: " + sampleRate + " Hz");
	           System.out.println("Quantization (Bits per Sample): " + bitsPerSample);

	           long frameLength = audioInputStream.getFrameLength();
	           int frameSize = audioFormat.getFrameSize();
	           byte[] audioData = new byte[(int) (frameLength * frameSize)];

	           int bytesRead = audioInputStream.read(audioData);

	           while (bytesRead != -1) {
	               bytesRead = audioInputStream.read(audioData);
	           }

	           audioInputStream.close();


	       } catch (UnsupportedAudioFileException | IOException e) {
	           System.err.println("Error processing audio file: " + e.getMessage());
	       }
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
	
	private static float getAmplitude(byte[] audioBuffer, int offset, int frameSize) {
	       float amplitude = 0;
	       for (int j = 0; j < frameSize; j++) {
	           amplitude += Math.abs(audioBuffer[offset + j]);
	       }
	       return amplitude / frameSize / 127.0f;
	   }
	   
	   
	   
	   private static short[] byteArrayToShortArray(byte[] byteArray, int length, int frameSize) {
	       ByteBuffer byteBuffer = ByteBuffer.wrap(byteArray, 0, length).order(ByteOrder.LITTLE_ENDIAN);
	       short[] shortArray = new short[length / frameSize];

	       for (int i = 0; i < shortArray.length; i++) {
	           shortArray[i] = byteBuffer.getShort();
	       }

	       return shortArray;
	   }

	   private static double detectPitch(short[] audioData, float sampleRate) {
	       int maxOffset = audioData.length / 2;
	       int[] autocorr = new int[maxOffset];

	       for (int offset = 0; offset < maxOffset; offset++) {
	           int sum = 0;
	           for (int i = 0; i < maxOffset; i++) {
	               sum += audioData[i] * audioData[i + offset];
	           }
	           autocorr[offset] = sum;
	       }

	       int maxPeakIndex = findMaxPeakIndex(autocorr);
	       return sampleRate / maxPeakIndex;
	   }

	   private static int findMaxPeakIndex(int[] autocorr) {
	       int maxIndex = 0;
	       int maxValue = 0;

	       for (int i = 1; i < autocorr.length - 1; i++) {
	           if (autocorr[i] > maxValue && autocorr[i] > autocorr[i - 1] && autocorr[i] > autocorr[i + 1]) {
	               maxValue = autocorr[i];
	               maxIndex = i;
	           }
	       }

	       return maxIndex;
	   }
	   
	   
	   public static void performFFTWithPadding(String filePath) throws UnsupportedAudioFileException, IOException {
	       // Read audio data from file
	       File audioFile = new File(filePath);
	       AudioInputStream ais = AudioSystem.getAudioInputStream(audioFile);
	       AudioFormat format = ais.getFormat();
	       byte[] audioBytes = ais.readAllBytes();
	       ais.close();

	       // Convert audio bytes to PCM samples
	       int bytesPerSample = format.getSampleSizeInBits() / 8;
	       int numSamples = audioBytes.length / bytesPerSample;
	       double[] samples = new double[numSamples];
	       ByteBuffer bb = ByteBuffer.wrap(audioBytes);

	       if (format.isBigEndian()) {
	           bb.order(ByteOrder.BIG_ENDIAN);
	       } else {
	           bb.order(ByteOrder.LITTLE_ENDIAN);
	       }

	       for (int i = 0; i < numSamples; i++) {
	           if (bytesPerSample == 2) {
	               samples[i] = bb.getShort();
	           } else if (bytesPerSample == 4) {
	               samples[i] = bb.getInt();
	           }
	       }

	       // Find the next power of 2 for zero-padding
	       int paddedLength = nextPowerOf2(numSamples);

	       // Zero-padding
	       double[] paddedSamples = new double[paddedLength];
	       System.arraycopy(samples, 0, paddedSamples, 0, numSamples);

	       // Perform FFT
	       FastFourierTransformer fft = new FastFourierTransformer(DftNormalization.STANDARD);
	       Complex[] result = fft.transform(paddedSamples, TransformType.FORWARD);

	       // Print results
	       for (int i = 0; i < result.length; i++) {
	           System.out.println("Frequency " + i + ": " + result[i]);
	       }
	   }

	   public static int nextPowerOf2(int n) {
	       int power = 1;
	       while (power < n) {
	           power *= 2;
	       }
	       return power;
	   }
	   
}
