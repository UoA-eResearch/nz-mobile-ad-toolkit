package com.adms.australianmobileadtoolkit.interpreter.platform;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.Log;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Platform utility class providing common functionality for the mobile ad toolkit.
 * This is a comprehensive implementation for Android.
 */
public class Platform {
    private static final String TAG = "Platform";
    private static Random random = new Random();
    
    // Logger functionality
    public static void logger(String message) {
        Log.d(TAG, message);
    }
    
    public static void logger(String tag, String message) {
        Log.d(tag, message);
    }
    
    // Directory operations
    public static boolean createDirectory(String path) {
        try {
            File dir = new File(path);
            return dir.mkdirs();
        } catch (Exception e) {
            logger("Error creating directory: " + e.getMessage());
            return false;
        }
    }
    
    public static boolean createDirectory(File file, boolean mkdirs) {
        try {
            if (mkdirs) {
                return file.mkdirs();
            } else {
                return file.mkdir();
            }
        } catch (Exception e) {
            logger("Error creating directory: " + e.getMessage());
            return false;
        }
    }
    
    public static boolean directoryExists(String path) {
        File dir = new File(path);
        return dir.exists() && dir.isDirectory();
    }
    
    public static boolean deleteRecursive(String path) {
        File file = new File(path);
        if (!file.exists()) return true;
        
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (File child : files) {
                    deleteRecursive(child.getAbsolutePath());
                }
            }
        }
        return file.delete();
    }
    
    // Platform interpretation routine - placeholder implementation
    public static void platformInterpretationRoutine() {
        logger("Platform interpretation routine called");
        // Placeholder implementation
    }
    
    public static void platformInterpretationRoutine(android.content.Context context, File rootDirectory, 
            java.util.function.Function<com.adms.australianmobileadtoolkit.JSONXObject, com.adms.australianmobileadtoolkit.JSONXObject> videoMetadataFunction,
            java.util.function.Function<com.adms.australianmobileadtoolkit.JSONXObject, Bitmap> frameGrabFunction,
            boolean flag) {
        logger("Platform interpretation routine called with parameters");
        // TODO: Implement proper interpretation routine
    }
    
    // File operations
    public static boolean fileExists(String path) {
        return new File(path).exists();
    }
    
    public static boolean deleteFile(String path) {
        return new File(path).delete();
    }
    
    public static List<String> listFiles(String directoryPath) {
        List<String> files = new ArrayList<>();
        File directory = new File(directoryPath);
        if (directory.exists() && directory.isDirectory()) {
            File[] fileArray = directory.listFiles();
            if (fileArray != null) {
                for (File file : fileArray) {
                    files.add(file.getName());
                }
            }
        }
        return files;
    }
    
    // Bitmap operations
    public static boolean saveBitmap(Bitmap bitmap, String path) {
        try {
            FileOutputStream out = new FileOutputStream(path);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.close();
            return true;
        } catch (Exception e) {
            logger("Error saving bitmap: " + e.getMessage());
            return false;
        }
    }
    
    public static Bitmap overlayBitmaps(Bitmap base, Bitmap overlay) {
        if (base == null || overlay == null) return base;
        
        Bitmap result = Bitmap.createBitmap(base.getWidth(), base.getHeight(), base.getConfig());
        Canvas canvas = new Canvas(result);
        canvas.drawBitmap(base, 0, 0, null);
        canvas.drawBitmap(overlay, 0, 0, null);
        return result;
    }
    
    public static int averageColours(List<Integer> colors) {
        if (colors == null || colors.isEmpty()) return Color.BLACK;
        
        long r = 0, g = 0, b = 0;
        for (int color : colors) {
            r += Color.red(color);
            g += Color.green(color);
            b += Color.blue(color);
        }
        
        int size = colors.size();
        return Color.rgb((int)(r / size), (int)(g / size), (int)(b / size));
    }
    
    public static boolean whitespacePixelFromImage(Bitmap bitmap, int x, int y) {
        if (bitmap == null || x < 0 || y < 0 || x >= bitmap.getWidth() || y >= bitmap.getHeight()) {
            return false;
        }
        
        int pixel = bitmap.getPixel(x, y);
        int threshold = 240; // Consider pixels with RGB values above this as whitespace
        return Color.red(pixel) > threshold && Color.green(pixel) > threshold && Color.blue(pixel) > threshold;
    }
    
    // Mathematical operations
    public static double getStandardDeviation(List<Double> values) {
        if (values == null || values.size() < 2) return 0.0;
        
        double mean = values.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        double variance = values.stream().mapToDouble(val -> Math.pow(val - mean, 2)).average().orElse(0.0);
        return Math.sqrt(variance);
    }
    
    // Overload for List<Integer>
    public static double getStandardDeviationInt(List<Integer> values) {
        if (values == null || values.size() < 2) return 0.0;
        
        double mean = values.stream().mapToDouble(Integer::doubleValue).average().orElse(0.0);
        double variance = values.stream().mapToDouble(val -> Math.pow(val - mean, 2)).average().orElse(0.0);
        return Math.sqrt(variance);
    }
    
    public static double getStandardDeviationD(List<Double> values) {
        return getStandardDeviation(values);
    }
    
    public static double getStandardDeviationDouble(List<Double> values) {
        return getStandardDeviation(values);
    }
    
    public static double forceToRange(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
    
    public static int forceToRange(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
    
    public static double randomInRange(double min, double max) {
        return min + (max - min) * random.nextDouble();
    }
    
    // Overloads for different parameter types
    public static int randomInRange(java.util.Random r, android.util.Pair<Integer, Integer> bounds) {
        if (bounds == null) return 0;
        return bounds.first + r.nextInt(Math.max(1, bounds.second - bounds.first + 1));
    }
    
    // Range operations
    public static class Range {
        public double start, end;
        public Range(double start, double end) {
            this.start = start;
            this.end = end;
        }
    }
    
    public static boolean rangesOverlap(Range r1, Range r2) {
        if (r1 == null || r2 == null) return false;
        return r1.start <= r2.end && r2.start <= r1.end;
    }
    
    // Overload for integer parameters
    public static int rangesOverlap(int start1, int end1, int start2, int end2) {
        if (start1 <= end2 && start2 <= end1) {
            return 1; // overlap
        }
        return 0; // no overlap
    }
    
    public static Range orderRange(double a, double b) {
        return new Range(Math.min(a, b), Math.max(a, b));
    }
    
    // Overloads for different parameter types
    public static android.util.Pair<Integer, Integer> orderRange(List<Integer> range) {
        if (range == null || range.size() < 2) {
            return new android.util.Pair<>(0, 0);
        }
        int min = Math.min(range.get(0), range.get(1));
        int max = Math.max(range.get(0), range.get(1));
        return new android.util.Pair<>(min, max);
    }
    
    public static List<Range> discreteIntervalsToRanges(List<Integer> intervals) {
        List<Range> ranges = new ArrayList<>();
        if (intervals == null || intervals.isEmpty()) return ranges;
        
        for (int i = 0; i < intervals.size() - 1; i += 2) {
            if (i + 1 < intervals.size()) {
                ranges.add(new Range(intervals.get(i), intervals.get(i + 1)));
            }
        }
        return ranges;
    }
    
    // Overload for two-parameter version
    public static List<Range> discreteIntervalsToRanges(List<Integer> intervals, List<Integer> consistent) {
        List<Range> ranges = new ArrayList<>();
        if (intervals == null || intervals.isEmpty() || consistent == null) return ranges;
        
        // Simple implementation - convert consistent list to ranges based on intervals
        for (int i = 0; i < Math.min(intervals.size(), consistent.size() - 1); i += 2) {
            if (i + 1 < consistent.size()) {
                ranges.add(new Range(consistent.get(i), consistent.get(i + 1)));
            }
        }
        return ranges;
    }
    
    public static List<Range> subtractRanges(List<Range> original, List<Range> toSubtract) {
        List<Range> result = new ArrayList<>();
        if (original == null) return result;
        if (toSubtract == null) return new ArrayList<>(original);
        
        for (Range orig : original) {
            List<Range> current = Arrays.asList(orig);
            for (Range sub : toSubtract) {
                List<Range> newCurrent = new ArrayList<>();
                for (Range c : current) {
                    if (!rangesOverlap(c, sub)) {
                        newCurrent.add(c);
                    } else {
                        if (c.start < sub.start) {
                            newCurrent.add(new Range(c.start, Math.min(c.end, sub.start)));
                        }
                        if (c.end > sub.end) {
                            newCurrent.add(new Range(Math.max(c.start, sub.end), c.end));
                        }
                    }
                }
                current = newCurrent;
            }
            result.addAll(current);
        }
        return result;
    }
    
    // Overload for List<Integer> parameters
    public static List<List<Integer>> subtractIntegerRanges(List<Integer> original, List<Integer> toSubtract) {
        List<List<Integer>> result = new ArrayList<>();
        if (original == null || original.size() < 2) return result;
        if (toSubtract == null || toSubtract.size() < 2) {
            result.add(new ArrayList<>(original));
            return result;
        }
        
        int origStart = original.get(0);
        int origEnd = original.get(1);
        int subStart = toSubtract.get(0);
        int subEnd = toSubtract.get(1);
        
        // If no overlap, return original range
        if (rangesOverlap(origStart, origEnd, subStart, subEnd) == 0) {
            result.add(new ArrayList<>(original));
            return result;
        }
        
        // Add parts that don't overlap
        if (origStart < subStart) {
            result.add(Arrays.asList(origStart, Math.min(origEnd, subStart)));
        }
        if (origEnd > subEnd) {
            result.add(Arrays.asList(Math.max(origStart, subEnd), origEnd));
        }
        
        return result;
    }
    
    // Utility operations
    public static List<Integer> generateSamplePositions(int total, int samples) {
        List<Integer> positions = new ArrayList<>();
        if (samples <= 0 || total <= 0) return positions;
        
        if (samples >= total) {
            for (int i = 0; i < total; i++) {
                positions.add(i);
            }
        } else {
            double step = (double) total / samples;
            for (int i = 0; i < samples; i++) {
                positions.add((int) Math.round(i * step));
            }
        }
        return positions;
    }
    
    // Overload for Facebook compatibility
    public static android.util.Pair<Integer, List<Integer>> generateSamplePositions(int nSamples, int lowerBound, int upperBound) {
        List<Integer> positions = new ArrayList<>();
        int range = upperBound - lowerBound;
        if (nSamples <= 0 || range <= 0) {
            return new android.util.Pair<>(0, positions);
        }
        
        double step = (double) range / nSamples;
        for (int i = 0; i < nSamples; i++) {
            positions.add((int) Math.round(lowerBound + i * step));
        }
        return new android.util.Pair<>(positions.size(), positions);
    }
    
    public static List<Integer> boundsOnStride(int start, int end, int stride) {
        List<Integer> bounds = new ArrayList<>();
        for (int i = start; i <= end; i += stride) {
            bounds.add(i);
        }
        if (bounds.isEmpty() || bounds.get(bounds.size() - 1) != end) {
            bounds.add(end);
        }
        return bounds;
    }
    
    // Overload for Facebook compatibility
    public static android.util.Pair<Integer, Integer> boundsOnStride(int strideSize, int stridePosition, int lowerBound, int upperBound) {
        int start = Math.max(lowerBound, stridePosition * strideSize);
        int end = Math.min(upperBound, start + strideSize - 1);
        return new android.util.Pair<>(start, end);
    }
    
    // JSON operations
    public static boolean writeToJSON(JSONObject json, String filePath) {
        try {
            FileWriter writer = new FileWriter(filePath);
            writer.write(json.toString(2));
            writer.close();
            return true;
        } catch (Exception e) {
            logger("Error writing JSON: " + e.getMessage());
            return false;
        }
    }
    
    public static void printJSON(JSONObject json) {
        try {
            logger("JSON", json.toString(2));
        } catch (JSONException e) {
            logger("Error printing JSON: " + e.getMessage());
        }
    }
    
    // Overloads for different types
    public static void printJSON(Object obj) {
        if (obj instanceof JSONObject) {
            printJSON((JSONObject) obj);
        } else {
            logger("OBJECT", obj.toString());
        }
    }
    
    // Utility methods
    public static String getCurrentTimestamp() {
        return String.valueOf(System.currentTimeMillis());
    }
    
    public static void sleep(long milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger("Sleep interrupted: " + e.getMessage());
        }
    }
    
    // Convert List<Range> to List<List<Integer>>
    public static List<List<Integer>> rangesToIntegerLists(List<Range> ranges) {
        List<List<Integer>> result = new ArrayList<>();
        if (ranges != null) {
            for (Range range : ranges) {
                result.add(Arrays.asList((int)range.start, (int)range.end));
            }
        }
        return result;
    }
    
    // Comparator class for sorting frame comparison structures
    public static class SortByLastFrame implements java.util.Comparator<JSONObject> {
        @Override
        public int compare(JSONObject a, JSONObject b) {
            try {
                int lastFrameA = a.optInt("lastFrame", 0);
                int lastFrameB = b.optInt("lastFrame", 0);
                return Integer.compare(lastFrameA, lastFrameB);
            } catch (Exception e) {
                logger("Error comparing JSONObjects in SortByLastFrame: " + e.getMessage());
                return 0;
            }
        }
    }
    
    public static String filenameUnextended(File file) {
        String name = file.getName();
        int lastDot = name.lastIndexOf('.');
        return lastDot > 0 ? name.substring(0, lastDot) : name;
    }
    
    public static boolean writeToJSON(File file, JSONObject json) {
        return writeToJSON(json, file.getAbsolutePath());
    }
}