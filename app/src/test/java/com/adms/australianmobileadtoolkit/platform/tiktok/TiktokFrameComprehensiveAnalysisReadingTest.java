package com.adms.australianmobileadtoolkit.platform.tiktok;

import static com.adms.australianmobileadtoolkit.IsolatedTest.testScreenRecordingsDirectory;
import static com.adms.australianmobileadtoolkit.interpreter.platform.Tiktok.tiktokComprehensiveReading;
import static com.adms.australianmobileadtoolkit.interpreter.platform.Platform.writeToJSON;
import static com.adms.australianmobileadtoolkit.machine.frameGrabMachine;
import static com.adms.australianmobileadtoolkit.machine.getVideoMetadataMachine;
import static com.adms.australianmobileadtoolkit.machine.testContext;

import com.adms.australianmobileadtoolkit.IsolatedTest;

import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.io.File;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = {33})

public class TiktokFrameComprehensiveAnalysisReadingTest {

    @Test
    public void testTiktokComprehensiveReading() {
        // Initialize the test environment
        IsolatedTest thisTest = new IsolatedTest("tiktokComprehensiveReading");
        File tempDirectory = new File(thisTest.outputDirectory, "temp");

        // Print temporary directory paths (debug information)
        System.out.println("Temp Directory Path: " + tempDirectory.getAbsolutePath());

        // Define a test case file (using a TikTok sample video)
        File thisScreenRecordingFile = new File(testScreenRecordingsDirectory, "tiktok_ad_sponsored_googlepixel_1.mp4");

        // Call TikTok Comprehensive Reading method and write to JSON file
        JSONObject output = tiktokComprehensiveReading(
                testContext,
                tempDirectory,
                thisScreenRecordingFile,
                getVideoMetadataMachine,
                frameGrabMachine
        );

        // Write the result to the output file (JSON format)
        writeToJSON(new File(thisTest.outputDirectory, "output.json"), output);

        // Optional: Print out the results for debugging
        System.out.println("Output JSON: " + output.toString());
    }
}
