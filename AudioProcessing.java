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

public class AudioProcessing {


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
