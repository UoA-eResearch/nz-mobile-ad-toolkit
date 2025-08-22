package com.adms.australianmobileadtoolkit.interpreter.platform;

import static com.adms.australianmobileadtoolkit.Arguments.A;
import static com.adms.australianmobileadtoolkit.Arguments.Args;
import static com.adms.australianmobileadtoolkit.Common.optionalGetDouble;
import static com.adms.australianmobileadtoolkit.appSettings.prescribedMinVideoWidth;
import static com.adms.australianmobileadtoolkit.interpreter.platform.Platform.createDirectory;
import static com.adms.australianmobileadtoolkit.interpreter.platform.Platform.getStandardDeviationDouble;
import static com.adms.australianmobileadtoolkit.interpreter.platform.Platform.logger;
import static com.adms.australianmobileadtoolkit.interpreter.platform.Platform.rangesOverlap;
import static com.adms.australianmobileadtoolkit.interpreter.platform.Platform.saveBitmap;
import static com.adms.australianmobileadtoolkit.interpreter.platform.Platform.getStandardDeviation;
import static com.adms.australianmobileadtoolkit.interpreter.visual.Visual.imageToStencil;
import static com.adms.australianmobileadtoolkit.interpreter.visual.Visual.pixelDifferencePercentage;
import static com.adms.australianmobileadtoolkit.interpreter.visual.Visual.stencilSimilarity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import androidx.palette.graphics.Palette;

import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;
import android.util.Pair;


import com.adms.australianmobileadtoolkit.JSONXObject;
import com.adms.australianmobileadtoolkit.R;
import com.adms.australianmobileadtoolkit.interpreter.visual.Stencil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.IntStream;


public class Tiktok {
    private static String TAG = "Tiktok";
    private static String fileFormat = "jpg";

    public static Integer prescribedMinVideoWidth = 500;

    public static HashMap<String, Integer> tiktokSampleImage(Bitmap thisBitmap) {
        int height = thisBitmap.getHeight();
        int width = thisBitmap.getWidth();
        // Define Region of interest (RIO) for the middle bottom button
        int buttonHeight = (int) (height * 0.04);  // Bottom 10% of the screen
        int buttonWidth = (int) (width * 0.11);    // Middle 20% of the width
        int buttonStartX = (width - buttonWidth) / 2;
        int buttonStartY = height - (int) (height * 0.08);



        // Crop the button region
        Bitmap croppedButton = Bitmap.createBitmap(thisBitmap, buttonStartX, buttonStartY, buttonWidth, buttonHeight);


        //Extract colour using the andriod Palette API
        Palette palette = Palette.from(croppedButton).generate();
        HashMap<String, Integer> colourPalette = new HashMap<>();
        for (Palette.Swatch swatch : palette.getSwatches()){
            String colorHex = String.format("#%06X", (0xFFFFFF & swatch.getRgb()));
            colourPalette.put(colorHex, swatch.getPopulation());
        }
        return colourPalette;

    }
    public static Bitmap processSearchBox(Bitmap thisBitmap) {
        int height = thisBitmap.getHeight();
        int width = thisBitmap.getWidth();
        //Define RIO for the top search box
        int searchWidth = (int)(width *  0.3);// right 40% of the screen
        int searchHeight = (int)(height * 0.08); // top 20% of the screen
        int searchStartX = width - searchWidth;
        int searchStartY = (int)(height * 0.04);

        // Crop the search box region
        Bitmap croppedSearchBox = Bitmap.createBitmap(thisBitmap, searchStartX, searchStartY, searchWidth, searchHeight);

        Bitmap processedSearchBox = convertToBlackAndWhite(croppedSearchBox);
        return processedSearchBox;
    }

    public static Bitmap convertToBlackAndWhite (Bitmap greyscaleBitmap){
        int width = greyscaleBitmap.getWidth();
        int height = greyscaleBitmap.getHeight();

        Bitmap blackAndWhiteBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        // Set threshold to 200
        int threshold = 200;

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int pixel = greyscaleBitmap.getPixel(x, y);
                int brightness = (Color.red(pixel) + Color.green(pixel) + Color.blue(pixel)) / 3; // Average brightness

                if (brightness > threshold) {
                    blackAndWhiteBitmap.setPixel(x, y, Color.WHITE); // Bright pixels become white
                } else {
                    blackAndWhiteBitmap.setPixel(x, y, Color.BLACK); // Dark pixels become black
                }
            }
        }

        return blackAndWhiteBitmap;
    }
    public static Rect findTightestBoundingBox(Bitmap topRightCorner) {
        int width = topRightCorner.getWidth();
        int height = topRightCorner.getHeight();

        int minX = width, minY = height, maxX = 0, maxY = 0;
        boolean foundWhitePixel = false;

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int pixel = topRightCorner.getPixel(x, y);
                if (Color.red(pixel) == 255 && Color.green(pixel) == 255 && Color.blue(pixel) == 255) {
                    foundWhitePixel = true;
                    if (x < minX) minX = x;
                    if (x > maxX) maxX = x;
                    if (y < minY) minY = y;
                    if (y > maxY) maxY = y;
                }
            }
        }

        // Return null if no white pixels are found (failsafe)
        if (!foundWhitePixel) return null;

        // Return the bounding box
        return new Rect(minX, minY, maxX, maxY);
    }

    public static Bitmap cropToBoundingBox(Bitmap image, Rect boundingBox) {
        if (boundingBox == null) return image; // Return original if no bounding box found
        return Bitmap.createBitmap(image, boundingBox.left, boundingBox.top,
                boundingBox.width(), boundingBox.height());
    }

    public static Bitmap resizeProportionally(Bitmap original) {
        // Define target size inside the function
        int targetWidth = 124;
        int targetHeight = 28;

        int originalWidth = original.getWidth();
        int originalHeight = original.getHeight();

        // Calculate aspect ratio
        float aspectRatio = originalWidth / (float) originalHeight;
        int newWidth, newHeight;

        if ((float) targetWidth / targetHeight > aspectRatio) {
            // Fit height, adjust width
            newHeight = targetHeight;
            newWidth = Math.round(newHeight * aspectRatio);
        } else {
            // Fit width, adjust height
            newWidth = targetWidth;
            newHeight = Math.round(newWidth / aspectRatio);
        }

        // Resize the image proportionally
        return Bitmap.createScaledBitmap(original, newWidth, newHeight, true);
    }

    public static JSONObject tiktokGenerateQuickReadingEvaluate(HashMap<Integer, Boolean> frameResults){
        int tiktokFrames = 0;

        for (Boolean result : frameResults.values()) {
            if (result) {
                tiktokFrames++;
            }
        }

        double tiktokPercentage = (tiktokFrames / (double) frameResults.size()) * 100;
        JSONObject output = new JSONObject();

        try {
            output.put("of", tiktokFrames >= frameResults.size() / 2); // More than 50%
            output.put("tiktokPercentage", tiktokPercentage);
            output.put("frameResults", frameResults);
        } catch (Exception e) {
            logger("ERROR: " + e.toString());
        }

        return output;

    }
    /*
     *
     * This function instantiates the HashMap that contains the references for all pictograms and
     * stencils used in the Tiktok Quick Analysis logic
     *
     * */
    public static HashMap<String, Integer> DEFAULT_RETRIEVE_REFERENCE_STENCILS_PICTOGRAMS_INDEX =
            new HashMap<String, Integer>() {{
                // Stencils
                put("tiktokSearch", R.drawable.tiktok_search);
                // Pictograms

            }};

    public static int DEFAULT_RETRIEVE_REFERENCE_STENCILS_PICTOGRAMS_SIZE_RADIUS = 64;
    public static HashMap <String,Integer>
            DEFAULT_RETRIEVE_REFERENCE_STENCILS_PICTOGRAMS_SIZE =
            new HashMap<String, Integer>() {{
                put("s", DEFAULT_RETRIEVE_REFERENCE_STENCILS_PICTOGRAMS_SIZE_RADIUS);
            }};
    public static int DEFAULT_RETRIEVE_REFERENCE_STENCILS_PICTOGRAMS_SIZE_WIDTH = 128;
    public static int DEFAULT_RETRIEVE_REFERENCE_STENCILS_PICTOGRAMS_SIZE_HEIGHT = 64;
    public static HashMap <String,Integer>
            DEFAULT_RETRIEVE_REFERENCE_STENCILS_TIKTOK_SIZE_SEARCH =
            new HashMap<String, Integer>() {{
                put("w", DEFAULT_RETRIEVE_REFERENCE_STENCILS_PICTOGRAMS_SIZE_WIDTH);
                put("h", DEFAULT_RETRIEVE_REFERENCE_STENCILS_PICTOGRAMS_SIZE_HEIGHT);
            }};

    // image manipulation


    @SuppressLint("UseCompatLoadingForDrawables")
    public static HashMap<String, Object> retrieveTiktokReferenceStencilsPictograms(Context context) {
        if (context == null) {
            throw new RuntimeException("Context is null. Ensure it is properly initialised before calling this function.");
        }
        HashMap<String, Bitmap> pictogramReferenceHashMap = new HashMap<>();
        // Apply the drawable references to a hashmap that loads in the corresponding resources
        for (HashMap.Entry<String, Integer> e : DEFAULT_RETRIEVE_REFERENCE_STENCILS_PICTOGRAMS_INDEX.entrySet()) {
            try {
                Bitmap bitmap = ((BitmapDrawable) context.getResources().getDrawable(e.getValue())).getBitmap();
                pictogramReferenceHashMap.put(e.getKey(), bitmap);
                System.out.println("Loaded drawable for key: " + e.getKey());
            } catch (Exception ex) {
                System.err.println("Failed to load drawable for key: " + e.getKey());
                ex.printStackTrace();
            }
        }
        /*
        for (HashMap.Entry<String, Integer> e
                : DEFAULT_RETRIEVE_REFERENCE_STENCILS_PICTOGRAMS_INDEX.entrySet()) {
            pictogramReferenceHashMap.put(e.getKey(),
                    ((BitmapDrawable)context.getResources().getDrawable(e.getValue())).getBitmap());
        }
        */

        // Assemble the references
        HashMap<String, Object> reference = new HashMap<>();
        for (String key : pictogramReferenceHashMap.keySet()) {
            Bitmap thisPictogram = pictogramReferenceHashMap.get(key);
            if (thisPictogram == null) {
                System.err.println("Bitmap is null for key: " + key);
                continue;
            }

            HashMap <String,Integer> size = DEFAULT_RETRIEVE_REFERENCE_STENCILS_PICTOGRAMS_SIZE;
            // Sizing specific to tiktok 'search' text case...
            if (key.contains("tiktok") && key.contains("Search")) {

                size = DEFAULT_RETRIEVE_REFERENCE_STENCILS_TIKTOK_SIZE_SEARCH;
            }
            reference.put(key,imageToStencil(Args(
                        A("bitmap", thisPictogram),
                        A("size", size))));

        }
        return reference;
    }


    public static boolean tiktokGenerateQuickReadingOnFrame(Bitmap thisBitmap, HashMap<String, Object> tiktokStencilReference ) {
        //detect the existence of the bottom button
        HashMap<String, Integer> colourPalette = tiktokSampleImage(thisBitmap);
        // Define the target colours
        String targetBlue = "#30E0E0";
        String targetRed = "#E01060";

        int pixelThreshold = 5;

        //threshold for manhattan distance percentage
        double maxDistanceThreshold = 0.10; //allow 5% difference

        int bluePixelCount = 0;
        int redPixelCount = 0;


        for (Map.Entry<String,Integer> entry : colourPalette.entrySet()) {
            String detectedColour =entry.getKey();
            int population = entry.getValue();

            double blueDifference = pixelDifferencePercentage(Color.parseColor(targetBlue), Color.parseColor(detectedColour));
            double redDifference = pixelDifferencePercentage(Color.parseColor(targetRed), Color.parseColor(detectedColour));

            if (blueDifference <= maxDistanceThreshold) {
                bluePixelCount += colourPalette.get(detectedColour);
            }

            if (redDifference <= maxDistanceThreshold) {
                redPixelCount += colourPalette.get(detectedColour);
            }
        }

        // If either condition fails, return false
        boolean bottomButtonDetected = bluePixelCount >= pixelThreshold && redPixelCount >= pixelThreshold;

        Bitmap croppedSearchBox = processSearchBox(thisBitmap);
        Rect bounding = findTightestBoundingBox(croppedSearchBox);
        Bitmap croppedTightBox  = cropToBoundingBox(croppedSearchBox,bounding);
        Bitmap finalCroppedSearchBox = resizeProportionally(croppedTightBox);

        Stencil searchBoxStencil = imageToStencil(Args(
                A("bitmap", finalCroppedSearchBox),
                A("size", DEFAULT_RETRIEVE_REFERENCE_STENCILS_TIKTOK_SIZE_SEARCH)
        ));
        //DEBUGGING

        // Retrieve the reference stencil
        Stencil referenceSearchStencil = (Stencil) tiktokStencilReference.get("tiktokSearch");
        if (referenceSearchStencil == null) {
            throw new RuntimeException("Reference stencil 'tiktokSearch' is null. Check stencil initialization.");
        }


        // Debugging: Log reference stencil dimensions
        int[][] stencilArray = referenceSearchStencil.getStencil();
        if (stencilArray != null) {
            System.out.println("Reference stencil dimensions: " +
                    stencilArray[0].length + "x" + stencilArray.length);
        } else {
            System.err.println("Reference stencil array is null.");
        }

        // Debugging: Log generated stencil dimensions
        int[][] generatedStencilArray = searchBoxStencil.getStencil();
        if (generatedStencilArray != null) {
            System.out.println("Generated stencil dimensions: " +
                    generatedStencilArray[0].length + "x" + generatedStencilArray.length);
        } else {
            System.err.println("Generated stencil array is null.");
        }

        // Compare the candidate stencil with the reference stencil
        double searchBoxSimilarity = stencilSimilarity(Args(
                A("a", tiktokStencilReference.get("tiktok" + "Search" )),
                A("b", searchBoxStencil),
                A("method", "multiplied"),
                A("deepSampling", true)
        ));
        // Debugging: Log similarity score
        System.out.println("Search Box Similarity Score: " + searchBoxSimilarity);
        // Debug: Log reference stencil dimensions

        if (referenceSearchStencil == null) {
            throw new RuntimeException("Reference stencil 'tiktokSearch' is null. Check stencil initialization.");
        }

        // Debug: Log reference stencil dimensions





        boolean searchBoxDetected = searchBoxSimilarity >= 0.8;
        return bottomButtonDetected || searchBoxDetected;
    }
    public static JSONObject tiktokGenerateQuickReading(Context context, Boolean DEBUG, File debugDirectory, File screenRecordingFile,
                                                        Function<JSONXObject, JSONXObject> functionGetVideoMetadata, Function<JSONXObject, Bitmap> frameGrabFunction, HashMap<String, Object> tiktokStencilReference) {


        //create a file
        File readingsFolder = new File(debugDirectory, "sample");
        if (DEBUG) {
            createDirectory(readingsFolder, true); //if DEBUG is enabled, create readingFolder
        }

        // Initialises a HashMap to store results of frame analysis. The key is the frame number and value is the boolean to indicate if it's a tiktok frame
        HashMap<Integer, Boolean> frameResults = new HashMap<>();

        // Retrieves metadata about the video (e.g., total frame count, video duration)
        JSONXObject thisVideoMetadata = functionGetVideoMetadata.apply(
                new JSONXObject().set("context", context).set("screenRecordingFile", screenRecordingFile)
        );

        // Sets the number of frames to sample to 7.
        int nScreenshotsToGrab = 7;
        Integer nFrames = null;
        Integer videoDuration = null;

        try {

            nFrames = (Integer) thisVideoMetadata.get("METADATA_KEY_VIDEO_FRAME_COUNT");
            videoDuration = (Integer) thisVideoMetadata.get("METADATA_KEY_DURATION");
        } catch (Exception e) {
            logger("ERROR: " + e.toString());
        }
        //Extracts the total frame count and video duration
        try {
            nFrames = (Integer) thisVideoMetadata.get("METADATA_KEY_VIDEO_FRAME_COUNT");
            videoDuration = (Integer) thisVideoMetadata.get("METADATA_KEY_DURATION");
        } catch (Exception e) {
            logger("ERROR: " + e.toString());
        };

        int interval = (int) Math.floor((double) nFrames / nScreenshotsToGrab);

        //Iterates through the video frames, stepping by the calculated interval.
        for (int f = 0; f < nFrames; f += interval) {

                JSONXObject bitmapStat = new JSONXObject()
                        .set("context", context)
                        .set("thisScreenRecordingFile", screenRecordingFile)
                        .set("f", f)
                        .set("videoFrames", nFrames)
                        .set("videoDuration", videoDuration)
                        .set("minWidth", prescribedMinVideoWidth);

                Bitmap thisBitmap = frameGrabFunction.apply(bitmapStat);
                if (DEBUG) {
                    saveBitmap(thisBitmap, new File(readingsFolder, "frame-" + f + ".jpg").getAbsolutePath());
                }
                HashMap<String, Integer> colourPalette = tiktokSampleImage(thisBitmap);

                // Debug: Log colour palette for the current frame
                System.out.println("Frame " + f + " Colour Palette: " + colourPalette);

                boolean interResult = tiktokGenerateQuickReadingOnFrame(thisBitmap,tiktokStencilReference );
                System.out.println("Frame " + f + " InterResult: " + interResult);

                boolean result = tiktokGenerateQuickReadingOnFrame(thisBitmap, tiktokStencilReference);
                System.out.println("Frame " + f + " result: " + result);
                frameResults.put(f, result);
            }

        System.out.println("Final Frame Results: " + frameResults);
        return tiktokGenerateQuickReadingEvaluate(frameResults);
    }

//Here we Capture and Compare the frame, after the processes, the duplicated frame should be ready to be removed


    public static void tiktokInterpretation(Context context, File appStorageRecordingsDirectory, HashMap<String, String> thisInterpretation,
                                            File rootDirectory, Function<JSONXObject, JSONXObject> getVideoMetadataFunction,
                                            Function<JSONXObject, Bitmap> frameGrabFunction, Boolean implementedOnAndroid,
                                            File adsFromDispatchDirectory) {

        // Retrieve the screen recording file and timestamp
        File screenRecordingFile = new File(appStorageRecordingsDirectory, thisInterpretation.get("filename"));
        Integer thisScreenRecordingTimestamp = Integer.parseInt(thisInterpretation.get("timestamp"));

        File tempTiktokComprehensiveSampleDirectory = new File(rootDirectory, "tempTiktokComprehensiveSample");
        // Ensure the directory is clean before processing
        createDirectory(tempTiktokComprehensiveSampleDirectory, true);
        // Run the comprehensive sampling process to deduplicate frames
        JSONObject frameSampleMetadata = tiktokComprehensiveReading(
                context, tempTiktokComprehensiveSampleDirectory, screenRecordingFile, getVideoMetadataFunction, frameGrabFunction
        );
        // Extract the statistics object from frameSampleMetadata
        JSONObject statistics = null;
        try {
            statistics = frameSampleMetadata.optJSONObject("statistics");
        } catch (Exception e) {
            logger("ERROR: Error retrieving statistics from frameSampleMetadata: " + e.toString());
        }
        List<Integer> runningListOfFrames = null;
        if (statistics != null) {
            runningListOfFrames = (List<Integer>) statistics.opt("runningListOfFrames");
        }

        if (runningListOfFrames != null) {

                // Save the non-duplicate frames directly to the adsToDispatch folder
                for (Integer frameIndex : runningListOfFrames) {
                    File sourceFile = new File(tempTiktokComprehensiveSampleDirectory, frameIndex + ".jpg");
                    File destinationFile = new File(adsFromDispatchDirectory, thisScreenRecordingTimestamp + "_" + frameIndex + ".jpg");

                    if (sourceFile.exists()) {
                        try {
                            Files.move(sourceFile.toPath(), destinationFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }

                    }
                }

        }
        if (implementedOnAndroid) {
            screenRecordingFile.delete();
            //facebookAnalysisCleanup(tempTiktokComprehensiveSampleDirectory, null);
        }
    }

    public static JSONObject tiktokFrameSignature(Bitmap thisBitmap) {
        // Integer referenceColour = wsColor;

        JSONObject statistics = new JSONObject();
        Integer w = thisBitmap.getWidth();
        Integer h = thisBitmap.getHeight();
        // Integer sampleOnX = 5;
        // Integer sampleOnY = 30;
        Integer sampleOnX = 10;
        Integer sampleOnY = 50;
        Integer strideX = (int) Math.round(w / (double) (sampleOnX + 2));
        Integer strideY = (int) Math.round(h / (double) (sampleOnY + 2));

        HashMap<Integer, HashMap<Integer, Integer>> thisFrameSignature = new HashMap<>();
        HashMap<Integer, Boolean> thisFrameSignatureWS = new HashMap<>();

        for (int yy = strideY; yy < h; yy += strideY) {
            HashMap<Integer, Integer> frameSignaturePart = new HashMap<>();
            List<Integer> colours = new ArrayList<>();
            for (int xx = strideX; xx < w; xx += strideX) {
                Integer thisPixel = thisBitmap.getPixel(xx, yy);
                frameSignaturePart.put(xx, thisPixel);
                colours.add(thisPixel);
            }
            thisFrameSignature.put(yy, frameSignaturePart);
            // thisFrameSignatureWS.put(yy, pixelDifferencePercentage(referenceColour, averageColours(Args(A("colors", colours)))) < 0.25);
            thisFrameSignatureWS.put(yy, true); // Assume that all rows are valid without reference to color

        }

        try {
            statistics.put("frameSignature", thisFrameSignature);
            statistics.put("frameSignatureWS", thisFrameSignatureWS);
            statistics.put("strideX", strideX);
            statistics.put("strideY", strideY);
            statistics.put("h", h);
        } catch (Exception e) {
            logger("ERROR: " + e.toString());
        }
        return statistics;
    }

    public static List<Integer> tiktokFrameSignaturesCompareDetermineLocalMaxima(HashMap<Integer, Double> similaritiesAtOffsets, Integer interval) {
        List<Integer> localMaxima = new ArrayList<>();
        List<Integer> orderedKeySet = similaritiesAtOffsets.keySet().stream().sorted().collect(Collectors.toList());
        Double minClimb = 0.005;
        for (Integer thisOffset : orderedKeySet) {

            List<Double> leftSideVals = similaritiesAtOffsets.keySet().stream()
                    .filter(z -> z < thisOffset && z > (thisOffset - interval))
                    .map(a -> similaritiesAtOffsets.get(a)).collect(Collectors.toList());
            Double leftSideMax = optionalGetDouble(leftSideVals.stream().mapToDouble(b -> b).max());
            Double leftSideMin = optionalGetDouble(leftSideVals.stream().mapToDouble(b -> b).min());

            List<Double> rightSideVals = similaritiesAtOffsets.keySet().stream()
                    .filter(z -> z > thisOffset && z < (thisOffset + interval))
                    .map(a -> similaritiesAtOffsets.get(a)).collect(Collectors.toList());
            Double rightSideMax = optionalGetDouble(rightSideVals.stream().mapToDouble(b -> b).max());
            Double rightSideMin = optionalGetDouble(rightSideVals.stream().mapToDouble(b -> b).min());

            if ((similaritiesAtOffsets.get(thisOffset) > leftSideMax)
                    && (similaritiesAtOffsets.get(thisOffset) > rightSideMax)
                    && (Math.abs(similaritiesAtOffsets.get(thisOffset) - leftSideMin) > minClimb)
                    && (Math.abs(similaritiesAtOffsets.get(thisOffset) - rightSideMin) > minClimb)) {
                localMaxima.add(thisOffset);
            }
        }
        return localMaxima;
    }

    public static JSONObject tiktokframeSignaturesCompare(JSONObject FSAJ, Bitmap frameB) {
        JSONObject statistics = new JSONObject();
        HashMap<Integer, HashMap<Integer, Integer>> FSANF = null;
        HashMap<Integer, HashMap<Integer, Integer>> FSBNF = null;
        HashMap<Integer, Boolean> frameSignatureWS = null;
        Integer strideX = 0;
        Integer strideY = 0;
        Integer h = 0;
        Boolean breakOnTooSimilar = false;
        try {
            FSANF = (HashMap<Integer, HashMap<Integer, Integer>>) FSAJ.get("frameSignature");
            frameSignatureWS = (HashMap<Integer, Boolean>) FSAJ.get("frameSignatureWS");
            strideX = (Integer) FSAJ.get("strideX");
            strideY = (Integer) FSAJ.get("strideY");
            h = (Integer) FSAJ.get("h");
        } catch (Exception e) {
            logger("ERROR: " + e.toString());
        }
        final HashMap<Integer, HashMap<Integer, Integer>> FSA = FSANF;

        // Create an inhibited section (to accomodate parts that dont move (like the navbar))
        // Double inhibitedRangeSimilarityThreshold = 0.99;
        Double inhibitedRangeSimilarityThreshold = 0.98;
        List<Integer> inhibitedRange = FSA.keySet().stream().filter(y -> {
            Double averageDifference = optionalGetDouble(FSA.get(y).keySet().stream().map(x ->
                    pixelDifferencePercentage(FSA.get(y).get(x), frameB.getPixel(x, y)) ).mapToDouble(z -> z).average());
            Double averageSimilarity = (1.0 - averageDifference);
            return (averageSimilarity > inhibitedRangeSimilarityThreshold);
        }).collect(Collectors.toList());

        // if half the image is identical, they're techniaclly teh same
        if (inhibitedRange.size() > (FSA.keySet().size() * 0.5)) {
            breakOnTooSimilar = true;
        }

        Integer halfHeight = (int) Math.floor(h * 0.4);

        // This figure determines the precision of the offset (if it can be found)
        Integer minimumComparisonStrideY = (int) Math.floor(h*0.005);
        Integer comparisonStrideY = (int) Math.floor(h*0.05);
        // Integer sampleModulusInteger = 8;
        Integer sampleModulusInteger = 16;  // increase sampleModulusInteger，Reduce calculation times

        Integer nComparisons = 0;

        Boolean foundReliableOffset = false;
        // Double desiredSimilarity = 0.90;
        Double desiredSimilarity = 0.85; // Modify the similarity threshold to allow for small pixel differences to reduce false positives (TOO_DIFFERENT or TOO_SIMILAR)
        Boolean breakOnImmediateDifference = false;
        Boolean breakOnRangeAbsence = false;
        Boolean breakOnNoMatch = false;
        Double determinedOffsetAccuracy = null;
        Double determinedOffsetSTDev = null;
        Integer determinedOffset = null;
        Integer absoluteBoundaryMin = (0 - halfHeight);
        Integer absoluteBoundaryMax = (0 + halfHeight);
        List<Pair<Integer, Integer>> rangesToScan = new ArrayList<>();
        rangesToScan.add(new Pair(absoluteBoundaryMin, absoluteBoundaryMax));
        while ((comparisonStrideY >= minimumComparisonStrideY) && (!foundReliableOffset)
                && (!breakOnRangeAbsence) && (!breakOnNoMatch) && (!breakOnImmediateDifference) && (!breakOnTooSimilar)) {
            HashMap<Integer, Double> similaritiesAtOffsets = new HashMap<>();
            HashMap<Integer, Double> stdevsAtOffsets = new HashMap<>();
            // For all of the current ranges
            for (Pair<Integer, Integer> thisRange : rangesToScan) {
                // Go over the range at intervals of the comparisonStrideY
                for (int i = thisRange.first; i < thisRange.second; i += comparisonStrideY) {
                    List<Double> pixelSimilarities = new ArrayList<>();
                    // determine the similarity at this offset
                    Integer m = 0;
                    for (Integer yy : FSA.keySet()) {
                        for (Integer xx : FSA.get(yy).keySet()) {
                            Integer yyAtOffset = (yy + i);
                            if ((yyAtOffset > 0) && (yyAtOffset < h)) {
                                m++;
                                if (m % sampleModulusInteger == 0) {
                                    nComparisons++;
                                    pixelSimilarities.add(1.0 - pixelDifferencePercentage(FSA.get(yy).get(xx), frameB.getPixel(xx, yyAtOffset)));
                                }
                            }
                        }
                    }
                    Double thisAverageSimilarityAtOffset = optionalGetDouble(pixelSimilarities.stream().mapToDouble(x->x).average());
                    //System.out.println("offset: " + i);
                    //System.out.println("similarity: "+ thisAverageSimilarityAtOffset + "\n");
                    similaritiesAtOffsets.put(i, thisAverageSimilarityAtOffset);
                    stdevsAtOffsets.put(i, getStandardDeviationDouble(pixelSimilarities));
                }
            }

            if (optionalGetDouble(similaritiesAtOffsets.values().stream().mapToDouble(z -> z).max()) < 0.5) {
                breakOnImmediateDifference = true;
                break;
            }

            // Determine the local maxima
            // This is necessary to determine which ranges we should go over again
            List<Integer> lmIntervalOffsets = similaritiesAtOffsets.keySet().stream().sorted().collect(Collectors.toList());
            Integer lmInterval = (Math.abs(lmIntervalOffsets.get(0) - lmIntervalOffsets.get(1))*4);
            List<Integer> localMaxima = tiktokFrameSignaturesCompareDetermineLocalMaxima(similaritiesAtOffsets, lmInterval);

            // Convert the local maxima into ranges
            rangesToScan = new ArrayList<>();
            for (Integer thisLocalMaxima : localMaxima) {
                List<Integer> tentativeRange = Arrays.asList(thisLocalMaxima - comparisonStrideY, thisLocalMaxima + comparisonStrideY);
                // The range needs to be adapted to the boundaries of the image
                tentativeRange.set(0, ((tentativeRange.get(0) < absoluteBoundaryMin) ? absoluteBoundaryMin : tentativeRange.get(0)));
                tentativeRange.set(1, ((tentativeRange.get(1) > absoluteBoundaryMax) ? absoluteBoundaryMax : tentativeRange.get(1)));
                // The range might be null after this transformation - check
                if (!Objects.equals(tentativeRange.get(0), tentativeRange.get(1))) {
                    // proceed if it isn't null
                    // lastly, determine that the tentative range does not overlap any existing
                    // range within the ranges to scan
                    List<Pair<Integer, Integer>> adaptedRangesToScan = new ArrayList<>();
                    Boolean overlapFound = false;
                    for (Pair<Integer, Integer> thisRange : rangesToScan) {
                        // If they do overlap, combine them
                        if (rangesOverlap(tentativeRange.get(0), tentativeRange.get(1), thisRange.first, thisRange.second) != 0) {
                            Pair<Integer, Integer> thisNewRange = new Pair(Math.min(tentativeRange.get(0), thisRange.first), Math.max(tentativeRange.get(1), thisRange.second));
                            adaptedRangesToScan.add(thisNewRange);
                            overlapFound = true;
                        } else {
                            adaptedRangesToScan.add(thisRange);
                        }
                    }
                    if (!overlapFound) {
                        Pair<Integer, Integer> thisNewRange = new Pair(tentativeRange.get(0), tentativeRange.get(1));
                        adaptedRangesToScan.add(thisNewRange);
                    }
                    rangesToScan = adaptedRangesToScan;
                } else {
                    // if teh ranges are equal, we dont need to apply them
                }
            }



            if (rangesToScan.isEmpty()) {
                // TODO - have to carry fowrard best match if it exists
                breakOnRangeAbsence = true;
                break;
            } else {





                comparisonStrideY = (int) Math.floor(comparisonStrideY * 0.5);
                sampleModulusInteger = Math.max((int) Math.floor(sampleModulusInteger * 0.5), 1);

                if (comparisonStrideY <= minimumComparisonStrideY) {
                    List<Integer> flaggedOffsets = similaritiesAtOffsets.keySet().stream()
                            .filter(z ->  (similaritiesAtOffsets.get(z) > desiredSimilarity)).collect(Collectors.toList());



                    if (flaggedOffsets.isEmpty()) {
                        breakOnNoMatch = true;
                        break;
                    } else {
                        foundReliableOffset = true;
                        determinedOffsetAccuracy = optionalGetDouble(similaritiesAtOffsets.values().stream().mapToDouble(x -> x).max()); // TODO
                        determinedOffset = Collections.max(similaritiesAtOffsets.entrySet(), Map.Entry.comparingByValue()).getKey(); // TODO
                        determinedOffsetSTDev = stdevsAtOffsets.get(determinedOffset);
                    }
                }
            }
        }

        try {
            String outcome = "COMPARABLE";
            if (breakOnImmediateDifference || breakOnNoMatch || breakOnRangeAbsence) {
                outcome = "TOO_DIFFERENT";
            }
            if (breakOnTooSimilar) {
                outcome = "TOO_SIMILAR";
            }
            if (foundReliableOffset) {
                outcome = "COMPARABLE";
            }
            statistics.put("outcome", outcome);
            statistics.put("maxSimilarity", determinedOffsetAccuracy);
            statistics.put("standardDeviation", determinedOffsetSTDev);
            statistics.put("nComparisons", nComparisons);
            statistics.put("offsetWithMaxSimilarity", determinedOffset);
        } catch (Exception e) {

            logger("ERROR: " + e.toString());
        }

        return statistics;
    }

    //Comprehensive Reading
    public static JSONObject tiktokComprehensiveReading(Context context,
                                                          File tempDirectory, File thisScreenRecordingFile, Function<JSONXObject, JSONXObject> videoMetadataFunction,
                                                          Function<JSONXObject, Bitmap> frameGrabFunction) {
        long elapsedTimeFacebookComprehensiveFramesSample = System.currentTimeMillis();
        JSONObject output = new JSONObject();
        JSONObject statistics = new JSONObject();
        Double intervalAsPercentageOfFrameRate = 0.5;
        Integer intervalMinimumComparable = 2;
        Double adjustingIntervalDivisionFactor = 2.0;
        List<HashMap<String, Integer>> scanLog = new ArrayList<>();
        List<List<Pair<Integer, Integer>>> rangesToScanLog = new ArrayList<>();
        Integer totalFramesSampled = 0;
        Integer totalComparisons = 0;
        Integer durationInMilliseconds = 0;
        Double frameRate = null;
        Integer w = null;
        Integer h = null;




        //TODO MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        //TODO retriever.setDataSource(context, Uri.fromFile(thisScreenRecordingFile));
        JSONXObject videoMetadata = videoMetadataFunction.apply(new JSONXObject().set("context", context).set("screenRecordingFile", thisScreenRecordingFile));


        System.out.println(videoMetadata);

        // JSONObject outputOfQuickReading = tiktokGenerateQuickReading(context, false, null, thisScreenRecordingFile, videoMetadataFunction, frameGrabFunction);
        // String denotedMode = null;
        // Integer wsColor = null;
        // try {
        //    denotedMode = (String) outputOfQuickReading.get("denotedMode");
        //    wsColor = (Integer) outputOfQuickReading.get("denotedColour");
        //} catch (Exception e) {

        //    logger("ERROR: " + e.toString());
        //}

        try {
            // Initialize the pseudo frame grabber
            frameRate = (Double) videoMetadata.get("METADATA_DERIVED_FRAMERATE");
            Log.i(TAG, "frameRate: "+frameRate);
            // Determine the length of the recording in frames
            Integer thisScreenRecordingVideoFrames = ((int) videoMetadata.get("METADATA_KEY_VIDEO_FRAME_COUNT")) - 1;
            durationInMilliseconds = ((int) videoMetadata.get("METADATA_KEY_DURATION")) - 1;

            Log.i(TAG, "thisScreenRecordingVideoFrames: "+thisScreenRecordingVideoFrames);
            // Derive a 'base-maximum' interval to jump between frames (as a percentage of the frame rate)
            Integer baseMaximumInterval = (int) Math.floor(frameRate * intervalAsPercentageOfFrameRate);

            /*
             *
             * In developing this approach, we anticipate that the code may run on devices with a strictly
             * small amount of memory. Thus it is wise for us to save the sampled frames as bitmaps, in a temporary
             * sample folder
             *
             * */
            // Before running a sample, the temp folder is cleared and recreated
            createDirectory(tempDirectory, true);

            Integer adjustingInterval = baseMaximumInterval;
            Boolean earlyExitFlag = false;




            // do the frame checking in waves - first wave does bare minimum

            // second wave goes in, checks where there are bad cuts and fills in their half reaches

            // continue to do this until a precision is reached

            // then go in and cull the parts that are redundant

            Boolean scanFlag = true;
            List<JSONObject> frameComparisonStructure = new ArrayList<>();
            List<JSONObject> frameComparisonStructureDisqualified = new ArrayList<>();
            HashMap<Integer, JSONObject> signaturesMap = new HashMap<>();
            while (scanFlag) {



                List<Pair<Integer, Integer>> rangesToScan = new ArrayList<>();

                HashMap<String, Integer> thisScanLog = new HashMap<>();

                // If the frameComparisonStructure is empty, do a full loop through
                if (frameComparisonStructure.isEmpty()) {
                    rangesToScan.add(new Pair(0, thisScreenRecordingVideoFrames));
                } else {

                    // Go through the list of frameComparisons
                    //
                    // For those that are too similar, remove the second frame and attach the first to
                    // the consecutive comparison (logical approach, considering they are similar)
                    // Note: We are making an assumption that if two frames are too similar, that their
                    // adjacent comparisons are transitively attributed the same comparison results
                    //
                    // Note: This step assumes items are added to array-lists in order (verified)
                    List<JSONObject> updatedFrameComparisonStructure = new ArrayList<>();
                    Integer transitedSimilarFrame = null;
                    Integer n_TOO_SIMILAR = 0;
                    for (JSONObject thisFrameComparison : frameComparisonStructure) {
                        // If this comparison is preceded by one that was 'too similar', augment this
                        // comparison with the transitedSimilarFrame
                        if (transitedSimilarFrame != null) {
                            thisFrameComparison.put("lastFrame", transitedSimilarFrame);
                            transitedSimilarFrame = null;
                        }
                        String thisComparisonOutcome = (String) ((JSONObject) thisFrameComparison.get("comparisonResult")).get("outcome");
                        // If this frame comparison is too similar, do not add it, and transit the
                        // second frame of its comparison to the next comparison
                        // Note: If this is the last framecomparison, we might naively allow it to be retained - although there is no reason
                        if (Objects.equals(thisComparisonOutcome, "TOO_SIMILAR")) {
                            //    && (!thisFrameComparison.equals(frameComparisonStructure.get(frameComparisonStructure.size() - 1)))) {
                            transitedSimilarFrame = (Integer) thisFrameComparison.get("lastFrame");
                            frameComparisonStructureDisqualified.add(thisFrameComparison);
                            n_TOO_SIMILAR ++;
                        } else {
                            updatedFrameComparisonStructure.add(thisFrameComparison);
                        }
                    }
                    thisScanLog.put("n_TOO_SIMILAR", n_TOO_SIMILAR);
                    // Update the frameComparisonStructure for the next step
                    frameComparisonStructure = new ArrayList<>(updatedFrameComparisonStructure);
                    // Flush the updatedFrameComparisonStructure
                    updatedFrameComparisonStructure = new ArrayList<>();

                    // For those that are too different, if their interval is large enough, add
                    // their frame range - otherwise preserve their frame comparison
                    //
                    // NOTE: This step has to occur separate to the previous, as the frameComparisonStructure
                    // is already edited at this point
                    Integer n_TOO_DIFFERENT = 0;
                    Integer n_COMPARABLE = 0;
                    for (JSONObject thisFrameComparison : frameComparisonStructure) {
                        String thisComparisonOutcome = (String) ((JSONObject) thisFrameComparison.get("comparisonResult")).get("outcome");
                        if (Objects.equals(thisComparisonOutcome, "TOO_DIFFERENT")) {
                            // Is the interval large enough?
                            Integer lastFrame = (Integer) thisFrameComparison.get("lastFrame");
                            Integer currentFrame = (Integer) thisFrameComparison.get("currentFrame");

                            if (Math.abs(lastFrame - currentFrame) >= intervalMinimumComparable) {
                                // If the interval between the frames is large enough to warrant
                                // the creation of a range for scanning, do not add the comparison to
                                // the updatedFrameComparisonStructure, but add a new range to the
                                // rangesToScan
                                rangesToScan.add(new Pair(lastFrame, currentFrame));
                                frameComparisonStructureDisqualified.add(thisFrameComparison);
                            } else {
                                updatedFrameComparisonStructure.add(thisFrameComparison);
                            }
                            n_TOO_DIFFERENT ++;
                        } else {
                            updatedFrameComparisonStructure.add(thisFrameComparison);
                            n_COMPARABLE ++;
                        }
                    }
                    thisScanLog.put("n_COMPARABLE", n_COMPARABLE);
                    thisScanLog.put("n_TOO_DIFFERENT", n_TOO_DIFFERENT);
                    // Update the frameComparisonStructure for the next step
                    frameComparisonStructure = new ArrayList<>(updatedFrameComparisonStructure);

                    //writeToJSON((new File(tempDirectory, "frameComparisonStructurePost.json")), frameComparisonStructure);

                    // Collapse adjacent ranges if they exist
                    if (rangesToScan.size() > 1) {
                        Integer retainedRangeStart = null;
                        Integer retainedRangeEnd = null;
                        List<Pair<Integer, Integer>> adjustedRangesToScan = new ArrayList<>();
                        for (Pair<Integer, Integer> thisRange : rangesToScan) {
                            if (retainedRangeStart == null) {
                                retainedRangeStart = thisRange.first;
                                retainedRangeEnd = thisRange.second;
                            } else
                            if (retainedRangeEnd == thisRange.first) {
                                retainedRangeEnd = thisRange.second;
                            } else {
                                adjustedRangesToScan.add(new Pair(retainedRangeStart, retainedRangeEnd));
                                retainedRangeStart = thisRange.first;
                                retainedRangeEnd = thisRange.second;
                            }
                        }
                        if (retainedRangeStart != null) {
                            adjustedRangesToScan.add(new Pair(retainedRangeStart, retainedRangeEnd));
                        }
                        rangesToScan = new ArrayList<>(adjustedRangesToScan);
                    }
                    scanLog.add(thisScanLog);
                }

                rangesToScanLog.add(rangesToScan);

                for (Pair<Integer, Integer> thisRange : rangesToScan) {
                    earlyExitFlag = false;
                    Integer lastFrame = thisRange.first;
                    Integer currentFrame = thisRange.first;
                    Integer destinationFrame = thisRange.second;
                    Boolean compareFrames = true;
                    while (compareFrames) {
                        if (currentFrame >= destinationFrame) {
                            compareFrames = false;
                        }
                        // Retrieve the bitmap for the current frame
                        Bitmap currentFrameBitmap = null;
                        while (currentFrameBitmap == null) {
                            // Set the frame number
                            // Grab the frame
                            JSONXObject bitmapGrabStat = new JSONXObject();

                            bitmapGrabStat
                                    .set("context", context)
                                    .set("thisScreenRecordingFile", thisScreenRecordingFile)
                                    .set("f", currentFrame)
                                    .set("videoFrames", thisScreenRecordingVideoFrames)
                                    .set("videoDuration", durationInMilliseconds)
                                    .set("minWidth", prescribedMinVideoWidth);

                            currentFrameBitmap = frameGrabFunction.apply(bitmapGrabStat);
                            if (h == null) {
                                w = currentFrameBitmap.getWidth();
                                h = currentFrameBitmap.getHeight();
                            }
                            totalFramesSampled ++;
                            System.out.println(currentFrame);

                            // The bitmap may still be null at this step - step backwards one frame in said case
                            if (currentFrameBitmap == null) {
                                System.out.println("frame is null");
                                if (currentFrame == destinationFrame) {
                                    currentFrame --;
                                } else {
                                    earlyExitFlag = true;
                                    //scanFlag = false;
                                    //System.out.println("SCAN FLAG TRIGGERED FROM HERE");
                                    break;
                                }
                            }
                        }

                        // The indication of the early exit flag will be that the currentFrameBitmap is not null,
                        // in which case we can proceed with a comparison at this frame.
                        if (!earlyExitFlag) {

                            if (!signaturesMap.containsKey(currentFrame)) {
                                signaturesMap.put(currentFrame,  tiktokFrameSignature(currentFrameBitmap));//frameSignature(currentFrameBitmap));
                                saveBitmap(currentFrameBitmap, (new File(tempDirectory, currentFrame + "." + fileFormat).getAbsolutePath()));
                            }

                            // Provided the lastFrame is instantiated
                            if (lastFrame != null) {
                                JSONObject signatureOfLastFrame = signaturesMap.get(lastFrame);
                                JSONObject signatureOfCurrentFrame = signaturesMap.get(currentFrame);
                                JSONObject comparisonResult = tiktokframeSignaturesCompare(signatureOfLastFrame, currentFrameBitmap);
                                totalComparisons ++;
                                JSONObject thisComparison = new JSONObject();
                                try {
                                    thisComparison.put("lastFrame", lastFrame);
                                    thisComparison.put("currentFrame", currentFrame);
                                    thisComparison.put("comparisonResult", comparisonResult);
                                } catch (Exception e) {

                                    logger("ERROR: " + e.toString());
                                }
                                frameComparisonStructure.add(thisComparison);
                            }

                            // At the end of the comparison, the currentFrameBitmap is applied to the lastFrameBitmap
                            // lastFrameBitmap = currentFrameBitmap;
                        }

                        // Update the position within the sample
                        lastFrame = currentFrame;
                        currentFrame += adjustingInterval;
                        System.out.println("adjustingInterval: " + adjustingInterval);
                        System.out.println("destinationFrame: " + destinationFrame);
                        if (currentFrame > destinationFrame) {
                            currentFrame = destinationFrame;
                        }
                    }
                }

                // Sort the results
                Collections.sort(frameComparisonStructure, new SortByLastFrame());

                adjustingInterval = (int) Math.floor(adjustingInterval / adjustingIntervalDivisionFactor);
                if (adjustingInterval < intervalMinimumComparable) {
                    scanFlag = false;
                }


            }

            frameComparisonStructure = frameComparisonStructure.stream().filter(x -> {
                try {
                    return (((JSONObject) x.get("comparisonResult")).get("outcome") != "TOO_SIMILAR");
                } catch (Exception e) {

                    logger("ERROR: " + e.toString());
                    return false;
                } } ).collect(Collectors.toList());

            // Delete all frames that aren't part of the frameComparisonStructure
            List<Integer> runningListOfFrames = new ArrayList<>();
            for (JSONObject thisComparison : frameComparisonStructure) {
                for (String alt : Arrays.asList("last", "current")) {
                    runningListOfFrames.add((Integer) thisComparison.get(alt + "Frame"));
                }
            }
            runningListOfFrames = runningListOfFrames.stream().distinct().collect(Collectors.toList());

            for (File child : Objects.requireNonNull(tempDirectory.listFiles())) {
                if (runningListOfFrames.indexOf(Integer.parseInt(child.getName().replaceAll("\\." + fileFormat, ""))) == -1) {
                    child.delete();
                }
            }
            // Insert the log and print the absolute path to tempDirectory
            // Log.i("TempDirectory", "Remaining frames are stored in: " + tempDirectory.getAbsolutePath());


            //try { retriever.release(); } catch (Exception e) { }

            // TODO - may not be necessary
            // Create a condensed version of the data-structure for future processing
            List<JSONObject> offsetChain = new ArrayList<>();
            if (runningListOfFrames.size() >= 2) {
                for (int i = 0; i < (runningListOfFrames.size() - 1); i ++) {
                    Integer frameThis = runningListOfFrames.get(i);
                    Integer frameNext = runningListOfFrames.get(i + 1);
                    List<JSONObject> candidateComparisons = Stream.concat(
                            frameComparisonStructure.stream().filter(x -> {
                                try {
                                    return ((Objects.equals(x.get("lastFrame"), frameThis))
                                            && (Objects.equals(x.get("currentFrame"), frameNext)));
                                } catch (Exception e) {

                                    logger("ERROR: " + e.toString());
                                    return false;
                                }
                            }),
                            frameComparisonStructureDisqualified.stream().filter(x -> {
                                try {
                                    return ((Objects.equals(x.get("lastFrame"), frameThis))
                                            && (Objects.equals(x.get("currentFrame"), frameNext)));
                                } catch (Exception e) {

                                    logger("ERROR: " + e.toString());
                                    return false;
                                }
                            })).collect(Collectors.toList());
                    if (!candidateComparisons.isEmpty()) {
                        offsetChain.add(candidateComparisons.get(0));
                    } else {
                        // TODO
                    }
                }
            } else {
                // TODO
            }

            // Capture the necessary statistics
            statistics.put("signaturesMap", signaturesMap);
            statistics.put("offsetChain", offsetChain);
            statistics.put("earlyExitFlag", earlyExitFlag);
            statistics.put("runningListOfFrames", runningListOfFrames);
            statistics.put("frameComparisonStructure", frameComparisonStructure);
            statistics.put("frameComparisonStructureDisqualified", frameComparisonStructureDisqualified);
            statistics.put("minWidth", prescribedMinVideoWidth);
            statistics.put("thisScreenRecordingFile", thisScreenRecordingFile.getAbsolutePath());
            statistics.put("intervalAsPercentageOfFrameRate", intervalAsPercentageOfFrameRate);
            statistics.put("intervalMinimumComparable", intervalMinimumComparable);
            statistics.put("adjustingIntervalDivisionFactor", adjustingIntervalDivisionFactor);
            statistics.put("scanLog", scanLog);
            statistics.put("rangesToScanLog", rangesToScanLog);
            statistics.put("totalComparisons", totalComparisons);
            statistics.put("frameRate", frameRate);
            statistics.put("w", w);
            statistics.put("h", h);

            JSONObject statisticsSummary = new JSONObject();
            statisticsSummary.put("elapsedTimeFacebookComprehensiveFramesSample", Math.abs(elapsedTimeFacebookComprehensiveFramesSample - System.currentTimeMillis()));
            statisticsSummary.put("nScreenRecordingVideoFrames", thisScreenRecordingVideoFrames);
            statisticsSummary.put("nScreenRecordingVideoFramesRetained", runningListOfFrames.size());
            statisticsSummary.put("nScreenRecordingVideoFramesSampled", totalFramesSampled);
            statisticsSummary.put("nComparisons", totalComparisons);
            statisticsSummary.put("nScreenRecordingVideoFrameRate", frameRate);
            statisticsSummary.put("nScreenRecordingVideoSeconds", (thisScreenRecordingVideoFrames / frameRate));

            output.put("statistics", statistics);
            output.put("statisticsSummary", statisticsSummary);
            // output.put("denotedMode", denotedMode);
            // output.put("wsColor", wsColor);
            // frameComparisonStructure // TODO - preserve matched offsets

        } catch (Exception e) {
            logger("ERROR: " + e.toString());
            try {
                output.put("error", e.getMessage());
            } catch (JSONException e2) {

                logger("ERROR: " + e2.toString());
            }
        }
        return output;
    }

}



