package com.adms.australianmobileadtoolkit.platform.tiktok;

import static com.adms.australianmobileadtoolkit.IsolatedTest.outputCropped;
import static com.adms.australianmobileadtoolkit.IsolatedTest.testScreenRecordingsDirectory;
import static com.adms.australianmobileadtoolkit.IsolatedTest.testScreenshotsDirectory;
import static com.adms.australianmobileadtoolkit.interpreter.platform.Facebook.facebookComprehensiveReading;
import static com.adms.australianmobileadtoolkit.interpreter.platform.Platform.filenameUnextended;
import static com.adms.australianmobileadtoolkit.interpreter.platform.Platform.logger;
import static com.adms.australianmobileadtoolkit.interpreter.platform.Platform.readJSONFromFile;
import static com.adms.australianmobileadtoolkit.interpreter.platform.Platform.saveBitmap;
import static com.adms.australianmobileadtoolkit.interpreter.platform.Platform.writeToJSON;
import static com.adms.australianmobileadtoolkit.interpreter.platform.Tiktok.convertToBlackAndWhite;
import static com.adms.australianmobileadtoolkit.interpreter.platform.Tiktok.cropToBoundingBox;
import static com.adms.australianmobileadtoolkit.interpreter.platform.Tiktok.findTightestBoundingBox;
import static com.adms.australianmobileadtoolkit.interpreter.platform.Tiktok.tiktokComprehensiveReading;
import static com.adms.australianmobileadtoolkit.interpreter.platform.Tiktok.tiktokGenerateQuickReading;
import static com.adms.australianmobileadtoolkit.machine.frameGrabMachine;
import static com.adms.australianmobileadtoolkit.machine.getVideoMetadataMachine;
import static com.adms.australianmobileadtoolkit.machine.testContext;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;

import com.adms.australianmobileadtoolkit.IsolatedTest;
import com.adms.australianmobileadtoolkit.JSONXObject;
import com.adms.australianmobileadtoolkit.interpreter.platform.Tiktok;
import com.adms.australianmobileadtoolkit.interpreter.visual.Stencil;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.function.Function;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = {33})

public class readings {


    @Test
    public void testTikTokQuickReading() {
        IsolatedTest thisTest = new IsolatedTest("tiktokQuickReading");
        thisTest.setCasesDirectory(testScreenRecordingsDirectory);
        HashMap<String, Object> tiktokStencilReference = Tiktok.retrieveTiktokReferenceStencilsPictograms(testContext);

        Function<File, JSONObject> localRoutine = x -> {
            File debugDirectory = new File(thisTest.outputDirectory.getAbsolutePath(), filenameUnextended(x));

            return tiktokGenerateQuickReading(testContext, true, debugDirectory, x, getVideoMetadataMachine, frameGrabMachine, tiktokStencilReference);
        };

        thisTest.applyToCases(localRoutine); // Comment out this line to speed up reporting

        for (File thisCaseDirectory : thisTest.outputDirectory.listFiles()) {
            if (thisCaseDirectory.isDirectory()) {
                JSONXObject thisMetadata = new JSONXObject(readJSONFromFile(new File(thisCaseDirectory, "metadata.json")));


                Boolean wasMatched = (Boolean) (new JSONXObject((JSONObject) thisMetadata.get("nameValuePairs"))).get("of");
                Boolean shouldBeMatched = (thisCaseDirectory.getName().contains("tiktok"));

                // Apply the outcome of the case to the confusion matrix
                thisTest.confusionMatrix.add(
                        (((shouldBeMatched == wasMatched) ? "T" : "F")  + ((wasMatched) ? "P" : "N")),
                        thisCaseDirectory.getName());
            }
        }

        // Print the confusion matrix results
        thisTest.confusionMatrix.print();
    }

    @Test
    public void testTikTokComprehensiveReading() {
        IsolatedTest thisTest = new IsolatedTest("facebookComprehensiveReading");
        File tempDirectory = new File(thisTest.outputDirectory, "temp");
        File thisScreenRecordingFile = new File(testScreenRecordingsDirectory, "facebook_dark_hq_large_slow_inapp_nonav_1.mp4"); // TODO
        writeToJSON(new File(thisTest.outputDirectory, "output.json"), tiktokComprehensiveReading(testContext, tempDirectory,
                thisScreenRecordingFile, getVideoMetadataMachine, frameGrabMachine));
    }

    @Test
    public void testTiktokVideoDetection() {
        // Create an isolated test environment
        IsolatedTest thisTest = new IsolatedTest("tiktokVideoDetectionWithFrames");
        thisTest.setCasesDirectory(testScreenRecordingsDirectory);
        HashMap<String, Object> tiktokStencilReference = Tiktok.retrieveTiktokReferenceStencilsPictograms(testContext);

        // Define the routine for processing each video file
        Function<File, JSONObject> localRoutine = x -> {
            File debugDirectory = new File(thisTest.outputDirectory.getAbsolutePath(), filenameUnextended(x));
            return Tiktok.tiktokGenerateQuickReading(
                    testContext, // Test context from your environment
                    true,        // Enable debugging
                    debugDirectory,
                    x,           // Current video file being tested
                    getVideoMetadataMachine, // Metadata function
                    frameGrabMachine,//Frame-grab function
                    tiktokStencilReference
            );
        };

        // Apply the routine to all test cases
        thisTest.applyToCases(localRoutine);

        // Evaluate results for each test case and save frames with boolean results
        for (File thisCaseDirectory : thisTest.outputDirectory.listFiles()) {
            if (thisCaseDirectory.isDirectory()) {
                // Retrieve the metadata for the current test case
                JSONXObject thisMetadata = new JSONXObject(readJSONFromFile(new File(thisCaseDirectory, "metadata.json")));
                Boolean wasMatched = null;
                // Extract the result of the detection
                try {
                    wasMatched = (Boolean) (new JSONXObject((JSONObject) thisMetadata.get("nameValuePairs"))).get("ofTikTok");
                }catch (Exception e){
                    logger.error("fail to parse:" + thisCaseDirectory.getName(),e);
                }
                if (wasMatched == null) {
                    logger.warn("Skipping test case due to null 'wasMatched': " + thisCaseDirectory.getName());
                    continue;
                }
                Boolean shouldBeMatched = thisCaseDirectory.getName().contains("tiktok");

                // Update the confusion matrix based on the test result
                thisTest.confusionMatrix.add(
                        (((shouldBeMatched == wasMatched) ? "T" : "F") + ((wasMatched) ? "P" : "N")),
                        thisCaseDirectory.getName()
                );

                // Save each frame with its boolean result
                File framesDirectory = new File(thisCaseDirectory, "frames");
                if (!framesDirectory.exists()) {
                    framesDirectory.mkdir();
                }


            }
        }

        // Print the confusion matrix results
        thisTest.confusionMatrix.print();
    }

    @Test
    public void testTikTokSampleImageCroppedButton() {
        IsolatedTest thisTest = new IsolatedTest("tiktokSampleImageTestCroppedButton");
        thisTest.setCasesDirectory(testScreenshotsDirectory);

        File[] screenshotFiles = testScreenshotsDirectory.listFiles((dir, name) -> name.endsWith(".jpg") || name.endsWith(".png"));
        for (File screenshotFile : screenshotFiles){
            try{
                Bitmap thisBitmap = BitmapFactory.decodeFile(screenshotFile.getAbsolutePath());
                File croppedImageFile = new File(outputCropped, "cropped" + screenshotFile.getName());
                int cropWidth = (int) (thisBitmap.getWidth() * 0.11);
                int cropHeight = (int) (thisBitmap.getHeight() * 0.04);

                int startX = (thisBitmap.getWidth() - (int) (thisBitmap.getWidth() * 0.11)) / 2;
                int startY = thisBitmap.getHeight() - (int) (thisBitmap.getHeight() * 0.08);

                Bitmap croppedBitmap = Bitmap.createBitmap(thisBitmap, startX, startY, cropWidth, cropHeight);

                saveBitmap(croppedBitmap,croppedImageFile.getAbsolutePath());

            } catch (RuntimeException e) {
                throw new RuntimeException(e);
            }
        }
    }
    @Test
    public void testTikTokSampleImageCroppedSearchBox() {
        IsolatedTest thisTest = new IsolatedTest("tiktokSampleImageTestCroppedSearchBox");
        thisTest.setCasesDirectory(testScreenshotsDirectory);

        File[] screenshotFiles = testScreenshotsDirectory.listFiles((dir, name) -> name.endsWith(".jpg") || name.endsWith(".png"));
        for (File screenshotFile : screenshotFiles){
            try{
                Bitmap thisBitmap = BitmapFactory.decodeFile(screenshotFile.getAbsolutePath());
                File croppedImageFile = new File(outputCropped, "cropped" + screenshotFile.getName());
                int width = thisBitmap.getWidth();
                int height = thisBitmap.getHeight();
                int cropWidth = (int) (thisBitmap.getWidth() * 0.3);
                int cropHeight = (int) (thisBitmap.getHeight() * 0.08);

                int startX = width- cropWidth;
                int startY = (int)(height * 0.04);

                Bitmap croppedSearchBox = Bitmap.createBitmap(thisBitmap, startX, startY, cropWidth, cropHeight);

                saveBitmap(croppedSearchBox,croppedImageFile.getAbsolutePath());

            } catch (RuntimeException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Test
    public void testConvertToBlackAndWhite() {
        IsolatedTest thisTest = new IsolatedTest("testConvertToBlackAndWhite");
        thisTest.setCasesDirectory(testScreenshotsDirectory);

        File[] screenshotFiles = testScreenshotsDirectory.listFiles((dir, name) -> name.endsWith(".jpg") || name.endsWith(".png"));
        for (File screenshotFile : screenshotFiles) {
            try {
                // Load the image
                Bitmap thisBitmap = BitmapFactory.decodeFile(screenshotFile.getAbsolutePath());
                File processedImageFile = new File(outputCropped, "bw_" + screenshotFile.getName());

                // Convert the image to black and white
                Bitmap blackAndWhiteImage = convertToBlackAndWhite(thisBitmap);

                // Save the processed image for inspection
                saveBitmap(blackAndWhiteImage, processedImageFile.getAbsolutePath());

            } catch (RuntimeException e) {
                throw new RuntimeException("Error processing screenshot: " + screenshotFile.getName(), e);
            }
        }
    }
    @Test
    public void testSearchBoundingBox() {
        IsolatedTest thisTest = new IsolatedTest("testSearchBoundingBox");
        thisTest.setCasesDirectory(testScreenshotsDirectory);

        File[] screenshotFiles = testScreenshotsDirectory.listFiles((dir, name) -> name.endsWith(".jpg") || name.endsWith(".png"));
        for (File screenshotFile : screenshotFiles) {
            try {
                // Load the image
                Bitmap thisBitmap = BitmapFactory.decodeFile(screenshotFile.getAbsolutePath());
                File processedImageFile = new File(outputCropped, "bounding_" + screenshotFile.getName());
                Rect boundingBox = findTightestBoundingBox(thisBitmap);
                if (boundingBox == null) {
                    System.out.println("No search text found in: " + screenshotFile.getName());
                    continue; // Skip to next image
                }

                Bitmap croppedImage = cropToBoundingBox(thisBitmap, boundingBox);

                // Save the cropped image
                saveBitmap(croppedImage, processedImageFile.getAbsolutePath());

                System.out.println("Processed: " + processedImageFile.getAbsolutePath());

            } catch (RuntimeException e) {
                throw new RuntimeException("Error processing screenshot: " + screenshotFile.getName(), e);
            }
        }
    }

    @Test
    public void testProcessSearchBox() {
        IsolatedTest thisTest = new IsolatedTest("processSearchBoxTest");
        thisTest.setCasesDirectory(testScreenshotsDirectory);

        // List all screenshots in the directory
        File[] screenshotFiles = testScreenshotsDirectory.listFiles((dir, name) -> name.endsWith(".jpg") || name.endsWith(".png"));

        if (screenshotFiles == null || screenshotFiles.length == 0) {
            System.err.println("No screenshots found in the directory!");
            return;
        }

        for (File screenshotFile : screenshotFiles) {
            // Load the bitmap from the screenshot
            Bitmap thisBitmap = BitmapFactory.decodeFile(screenshotFile.getAbsolutePath());

            // Process the search box
            Bitmap processedSearchBox = Tiktok.processSearchBox(thisBitmap);

            // Save the processed search box for visual inspection
            File outputDirectory = new File(thisTest.outputDirectory, "processedSearchBox");
            if (!outputDirectory.exists()) {
                outputDirectory.mkdirs();
            }
            File outputFile = new File(outputDirectory, "processed_" + screenshotFile.getName());
            saveBitmap(processedSearchBox, outputFile.getAbsolutePath());


            // Debugging output
            System.out.println("Processed search box saved for " + screenshotFile.getName());
        }
    }

    @Test
    public void testResizeProportionally() {
        IsolatedTest thisTest = new IsolatedTest("testResizeProportionally");
        thisTest.setCasesDirectory(testScreenshotsDirectory);

        // List all screenshots in the directory
        File[] screenshotFiles = testScreenshotsDirectory.listFiles((dir, name) -> name.endsWith(".jpg") || name.endsWith(".png"));

        if (screenshotFiles == null || screenshotFiles.length == 0) {
            System.err.println("No screenshots found in the directory!");
            return;
        }

        for (File screenshotFile : screenshotFiles) {
            // Load the bitmap from the screenshot
            Bitmap thisBitmap = BitmapFactory.decodeFile(screenshotFile.getAbsolutePath());

            // Process the search box
            Bitmap resizedImage = Tiktok.resizeProportionally(thisBitmap);

            // Save the processed search box for visual inspection
            File outputDirectory = new File(thisTest.outputDirectory, "processedSearchBox");
            if (!outputDirectory.exists()) {
                outputDirectory.mkdirs();
            }
            File outputFile = new File(outputDirectory, "processed_" + screenshotFile.getName());
            saveBitmap(resizedImage, outputFile.getAbsolutePath());


            // Debugging output
            System.out.println("Processed search box saved for " + screenshotFile.getName());
        }
    }


    @Test
    public void testTikTokSampleImagePalette() {
        IsolatedTest thisTest = new IsolatedTest("tiktokSampleImagePaletteTest");
        thisTest.setCasesDirectory(testScreenshotsDirectory);

        // List all screenshots in the directory
        File[] screenshotFiles = testScreenshotsDirectory.listFiles((dir, name) -> name.endsWith(".jpg") || name.endsWith(".png"));

        if (screenshotFiles == null || screenshotFiles.length == 0) {
            System.err.println("No screenshots found in the directory!");
            return;
        }
        for (File screenshotFile : screenshotFiles) {
            try {
                // Load the bitmap from the screenshot
                Bitmap thisBitmap = BitmapFactory.decodeFile(screenshotFile.getAbsolutePath());

                // Call the method to extract the colour palette
                HashMap<String, Integer> colourPalette = Tiktok.tiktokSampleImage(thisBitmap);

                // Save the colour palette as a JSON file in the output directory
                File paletteFile = new File(outputCropped, "palette_" + screenshotFile.getName().replace(".jpg", ".json").replace(".png", ".json"));
                try (PrintWriter writer = new PrintWriter(paletteFile)) {
                    JSONObject jsonPalette = new JSONObject(colourPalette);
                    writer.println(jsonPalette.toString(4)); // Pretty print JSON
                }catch (FileNotFoundException e) {
                    throw new RuntimeException(e);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
                ;

            } catch (RuntimeException e) {
            }
        }
    }

    @Test
    public void testTiktokGenerateQuickReadingOnFrame() {
        IsolatedTest thisTest = new IsolatedTest("tiktokSampleImagePaletteTest");
        thisTest.setCasesDirectory(testScreenshotsDirectory);



        // Retrieve the TikTok stencil references
        HashMap<String, Object> tiktokStencilReference = Tiktok.retrieveTiktokReferenceStencilsPictograms(testContext);

        // Ensure the "tiktokSearch" stencil is loaded
        if (!tiktokStencilReference.containsKey("tiktokSearch")) {
            throw new RuntimeException("Stencil 'tiktokSearch' is missing from the reference map.");
        }



        // List all screenshots in the directory
        File[] screenshotFiles = testScreenshotsDirectory.listFiles((dir, name) -> name.endsWith(".jpg") || name.endsWith(".png"));

        if (screenshotFiles == null || screenshotFiles.length == 0) {
            System.err.println("No screenshots found in the directory!");
            return;
        }


        for (File screenshotFile : screenshotFiles) {
            Bitmap thisBitmap = BitmapFactory.decodeFile(screenshotFile.getAbsolutePath());

            // Run TikTok quick analysis on the frame
            boolean result = Tiktok.tiktokGenerateQuickReadingOnFrame(thisBitmap, tiktokStencilReference);
            System.out.println("Frame result for " + screenshotFile.getName() + ": " + result);

            // Save debug images
            Bitmap croppedSearchBox = Tiktok.processSearchBox(thisBitmap);
            saveBitmap(croppedSearchBox, "output/cropped_" + screenshotFile.getName());


        }


    }
}
