package com.adms.australianmobileadtoolkit.platform.tiktok;

import static com.adms.australianmobileadtoolkit.IsolatedTest.testScreenRecordingsDirectory;
import static com.adms.australianmobileadtoolkit.interpreter.platform.Platform.filenameUnextended;
import static com.adms.australianmobileadtoolkit.interpreter.platform.Platform.logger;
import static com.adms.australianmobileadtoolkit.interpreter.platform.Platform.saveBitmap;
import static com.adms.australianmobileadtoolkit.interpreter.platform.Platform.writeToJSON;

import android.graphics.Bitmap;

import com.adms.australianmobileadtoolkit.IsolatedTest;
import com.adms.australianmobileadtoolkit.interpreter.platform.Tiktok;
import com.adms.australianmobileadtoolkit.machine;

import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = {33})
public class TiktokFrameSignatureTest {

    @Test
    public void testTiktokFrameSignature() {
        IsolatedTest thisTest = new IsolatedTest("TiktokFrameSignature");
        thisTest.setCasesDirectory(testScreenRecordingsDirectory);

        Function<File, JSONObject> localRoutine = x -> {
            try {
                // get 7 frame average from the video
                List<Bitmap> frames = extractAverageFrames(x, 7);
                if (frames.isEmpty()) {
                    logger.error("Failed to extract frames from video: " + x.getName());
                    return new JSONObject();
                }

                File debugDirectory = new File(thisTest.outputDirectory, filenameUnextended(x));
                if (!debugDirectory.exists()) {
                    debugDirectory.mkdirs();
                }

                JSONObject frameSignatures = new JSONObject();
                for (int i = 0; i < frames.size(); i++) {
                    Bitmap frame = frames.get(i);

                    // Integer referenceColor = 0x000000; // black reference color 这里要修改，因为现在的framesignature 删除掉了参考颜色
                    // JSONObject frameSignature = Tiktok.tiktokFrameSignature(frame, referenceColor); // 这里同理，删除掉了参考颜色
                    JSONObject frameSignature = Tiktok.tiktokFrameSignature(frame);

                    File outputBitmapFile = new File(debugDirectory, "frame_" + i + ".png");
                    saveBitmap(frame, outputBitmapFile.getAbsolutePath());

                    frameSignatures.put("frame_" + i, frameSignature);
                }

                JSONObject comparisonResults = new JSONObject();
                for (int i = 0; i < frames.size() - 1; i++) {
                JSONObject frameASignature = frameSignatures.getJSONObject("frame_" + i);
                Bitmap frameB = frames.get(i + 1);

                JSONObject comparisonResult = Tiktok.tiktokframeSignaturesCompare(frameASignature, frameB);
                // JSONObject comparisonResult = Tiktok.tiktokframeSignaturesCompare(frameASignature, frameB, 0x000000);
                comparisonResults.put("frame_" + i + "_vs_frame_" + (i + 1), comparisonResult);

                File outputComparisonFile = new File(debugDirectory, "frameComparisons.json");
                writeToJSON(outputComparisonFile, comparisonResults);

        }

                //File outputSignatureFile = new File(debugDirectory, "frameSignatures.json");
                //writeToJSON(outputSignatureFile, frameSignatures);

                return frameSignatures;
            } catch (Exception e) {
                logger.error("Error processing video file: " + x.getName(), e);
                return new JSONObject();
            }
        };

        // apply this test logic to all test cases
        thisTest.applyToCases(localRoutine);

        // print test result
        logger.info("Test completed for TiktokFrameSignature.");
    }


    /**
     *
     *
     * @param videoFile
     * @param numberOfFrames
     * @return the frame list
     */
    private List<Bitmap> extractAverageFrames(File videoFile, int numberOfFrames) {
        List<Bitmap> frames = new ArrayList<>();
        try {
            FFmpegFrameGrabber frameGrabber = new FFmpegFrameGrabber(videoFile);
            frameGrabber.setFormat("mp4");
            frameGrabber.start();

            int totalFrames = frameGrabber.getLengthInVideoFrames();
            if (totalFrames <= 0) {
                logger.error("Video has no valid frames.");
                return frames;
            }

            for (int i = 1; i <= numberOfFrames; i++) {
                int frameIndex = (int) Math.ceil((double) i * totalFrames / (numberOfFrames + 1));
                logger.info("Grabbing frame at index: " + frameIndex);

                frameGrabber.setVideoFrameNumber(frameIndex);
                Frame frame = frameGrabber.grab();

                if (frame != null) {
                    Bitmap bitmap = machine.getMP4At(videoFile, null, frameIndex);
                    //figure out what‘s that function
                    if (bitmap != null) {
                        frames.add(bitmap);
                    } else {
                        logger.warn("Failed to convert frame at index: " + frameIndex);
                    }
                } else {
                    logger.warn("Failed to grab frame at index: " + frameIndex);
                }
            }

            frameGrabber.stop();
        } catch (Exception e) {
            logger.error("Error extracting frames: " + e.getMessage(), e);
        }
        return frames;
    }
}


