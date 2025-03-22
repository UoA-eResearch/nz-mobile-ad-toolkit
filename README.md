# The NZ Aotearoa Ads Observatory Mobile Ad Toolkit

## An Overview

The Australian Mobile Ad Toolkit is a 'mobile ad detection' software application developed by the ADM+S for Android devices that assists researchers to identify ads on social media. This document outlines the specification for building a critical part of the mobile advertisement detection infrastructure that will help identify ads on a range of social media platforms, and furthermore white-labelling the software to be repackaged as the 'NZ Aotearoa Ads Observatory' Mobile Ad Toolkit.

## The Challenge

Understandably, there are a range of tasks to think about when developing a software application of this kind. Here, we enumerate the tasks to clarify what to achieve, as well as provide descriptions and tips to guide you through the challenge:

1. **White-labelling The Australian Mobile Ad Toolkit To The NZ Aotearoa Ads Observatory**
2. **Extending The Toolkit**
   1. **TikTok**
   2. **Snapchat**
   3. **X (formerly Twitter)**
   4. **LinkedIn**
3. **Expanding/Refining The Image Analysis Techniques**

**Directing You To The CodeBase:** All of these tasks will typically revolve around the continuous updates of a GitHub hosted software code repository - see https://github.com/ADMSCentre/nz-aotearoa-mobile-ad-toolkit-internal - we encourage you to fork this repository, join it as collaborators, or add new branches to the Git project that will permit you greater control over the editing process, which can be later merged back into the original distribution. Throughout this specification, explanations are given about the software - for your convenience, links are then provided to connect you directly with the concerning source code, so that you may more quickly adapt to the codebase.

__Setting Up Android Studio:__ The software IDE that you will need to develop and test your project will be Android Studio (available here: https://developer.android.com/studio). Consider when opening the software package within the IDE for the first time that you perform a Gradle sync, clean the project, and then rebuild it to ensure that everything is in working order.

__Note: It's important to remember that these tasks will not only require knowledge about the advertising practises of the platform in question, but will also need methods that consider the ethical implications when gathering the data.__

## White-labelling The Toolkit

### Repackaging and Rebranding

White-labelling is the process of de-coupling a software from its brand, most often for the purpose of adapting it to a new project. The Australian Mobile Ad Toolkit has been designed with white-labelling in mind, where most of the language, colour, image resources, themes, and other content for the app are stored beyond the `res/values` asset resource directories of the software package.

In terms of adapting the UX of the mobile application, the `res/layout` asset directory (which goes hand-in-hand with the `com.adms.australianmobileadtoolkit.ui` sub-package) provides the necessary foundation to adjust various app screens, as well as the positionings of content. It is expected that the UX of the application will not be changed too fundamentally, however this is a point to be discussed as part of the development process.

### The Chrome Dev Console

Another aspect of producing the 'NZ Aotearoa Ads Observatory' Mobile Ad Toolkit will be submitting the software package to the Google Play Store. This process is facilitated by the Google Play Console (see https://play.google.com/console/), which has strict conditions that will need to be addressed in order for the app to go _LIVE_.

It is to be expected that logo design, branding, app descriptions, privacy policy documentation, and other assets will need to be drafted in order to successfully submit the app for review (and potential acceptance) through the Google Play Console. These assets will need to comply with ethical standards of academic and research-based projects.

#### Closed Testing And Verification

Beyond submitting the app for review, Google has a comprehensive and tedious "closed to open" testing framework that will need to be followed in order to get from simply testing the app amongst a *closed* group of individuals, to *openly* distributing the app nation-wide. You will need to actively develop responses to questionnaires that the vendor may request of you, and find participants to trial the app, guiding them through the installation and usage process.

__Note: We stress that this particular sub-task not be underestimated.__

## Extending The Toolkit

The app has a lifecycle that ensures that ads are not only gathered from mobile devices, but that they are gathered without inconveniencing the user; its use of mobile screen-recording technologies means that it requires image analysis techniques in order to identify ads. Understandably when processing large amounts of video content, this can impose upon the memory, processing capabilities, and by extension the device's battery life itself.

As a way around this, the app's functionality is split into two types of logic when evaluating a screen-recording captured by the device:

1. **Quick Analysis:** This is the first (and sometimes only) point of analysis for screen-recordings captured by the app. The logic in this step is responsible for quickly determining whether the screen-recording is of one of the targeted platforms (e.g. TikTok), or whether its of unwanted personal phone usage. Every screen-recording from the hours of video content collected must go through this step, and so its important that the logic here is computationally light. In fact, it would be expected that this step only analyses 5-7 frames of images from a screen-recording video in order to make a judgement on whether it is relevant or not. Understandably the less computationally intensive it is, the less accurate it will be - although fortunately it does not need to be accurate every time - just enough to disregard a reasonable amount (whatsay 75%) or more screen-recordings of personal phone usage, while retaining at least a fair amount (perhaps 95%) of screen-recordings of the given platform.

   In the provided codebase, the quick reading method for the TikTok platform is stubbed at `com.adms.australianmobileadtoolkit.interpreter.platform.Tiktok#tiktokGenerateQuickReading`

2. **Comprehensive Analysis:** At the end of the 'Quick Analysis', we benefit from having a reduced list of screen-recordings that can then be pushed to the 'Comprehensive Analysis' step. It's in this step that the final 'OK' is given to determine that the screen-recording is indeed of a mobile ad. It does this by  interpreting the content more scrupulously to get an accurate indication (ideally with 95% accuracy) that the recording is an ad or not. While this step can be more computationally intensive than the 'Quick Analysis', its important to remember that we're still processing content on a mobile device and so we should be very careful to keep memory and processor usage to a minimum.

   In the provided codebase, the comprehensive reading method for the TikTok platform is stubbed at `com.adms.australianmobileadtoolkit.interpreter.platform.Tiktok#tiktokComprehensiveReading` and fulfils the first part of the 'Comprehensive Analysis' by constructing a set of still image frames that correspond to the TikTok platform observation in question.

   __Note: This method is expected to generate a directory of images that is temporarily kept on the device, and not held all at once in memory, but rather read from, an image at a time (to avoid excessive memory consumption).__

   This is expected to be executed within (and benefit) a separate method that is also stubbed at `com.adms.australianmobileadtoolkit.interpreter.platform.Tiktok#tiktokInterpretation` that will handle the more broader task of determining whether the TikTok observation constitutes an ad or not.

   The output of the comprehensive analysis should be a set of data corresponding to the recordings given in its input, where each block of data should then contain the isolated ads, cropped as static image parts from said recordings, and accompanying metadata about the time of observation, etc. 

   Refer to the `com.adms.australianmobileadtoolkit.interpreter.platform.Facebook#prepareAdContentForUpload` method for an example of how the data could be prepared functionally.

   __Note: We also provide an example of the output from a single execution at https://drive.google.com/file/d/1k_YB9TQGP_VqdSL07DVeJT5sQTlI52-K/view?usp=sharing.__

The functionality for the app is already complete for the Meta platform Facebook, for which (almost) all relative execution is contained at `com.adms.australianmobileadtoolkit.interpreter.platform.Facebook`. Furthermore, the code corresponding to the first of the social media platforms that we will hope to target is stubbed at `com/adms/australianmobileadtoolkit/interpreter/platform/Tiktok.java`.

__Note: We say 'almost' in the paragraph above to signal that there is some logic pertaining to identifying Facebook content outside of the designated class. Consider `com/adms/australianmobileadtoolkit/interpreter/platform/Platform.java:673` , where resources that are common to all executions of the Facebook analysis are loaded once for a single session, instead of at each video within the process - this is undertaken to avoid stressing computational resources - as food for thought, consider what other code may be added here for other social media platforms.__

### The Testing Environment

When testing a mobile app, physically loading the software onto the device (and consequently undertaking a unit test) can be incredibly time-consuming, and so is not encouraged. For this reason, we have developed a local testing environment that can be executed directly from your local machine - here we have put together a quick guide for how tests can be undertaken.

__Download The externalAssets:__ In order to undertake local tests on _real_ screen-recordings, you will firstly need an _externalAssets_ folder, which will contain the files to be examined with the testing environment - we provide a downloadable compressed version of this directory to get you started (see here: https://drive.google.com/file/d/1Uqi2vAt8rCFPJGoUqsLmGbX-3kRYJEjX/view?usp=sharing), however we encourage you to develop your own tests, and to expand on the examples already provided. As the externalAssets folder is not included as part of the GitHub repository, you will need to insert it into the parent folder that contains the local version of the code e.g.:

* `<Parent Folder>`
  * `australian-mobile-ad-toolkit-internal` (software code repository)
  * `externalAssets`
  * `...`

__Note: Consider that the app is intended to work on various devices, of differing dimensions, brightnesses, UIs, and such. The greater the variety of test data, the more successful your solution will be.__

As new social media platforms are added, it is expected that tests on their various code logics be sectioned into organised sub-packages - this is exemplified for the `com.adms.australianmobileadtoolkit.platform.facebook ` (_test_) sub-package, and already stubbed for the `com.adms.australianmobileadtoolkit.platform.tiktok ` (_test_) sub-package.

__Note: As part of the testing will require the construction of confusion matrices, you will be expected to gather false positive videos to convey screen-recordings that could be mistaken for the targeted social media platforms__

**Note: While undertaking these processes, it is stressed that no information that could be used to identify the participant, or any private profiles (on the given platform) they have observability into, can be retained in the data produced from the 'Comprehensive Analysis' - nor can this information be sent off-device (although provided that your tests are undertaken on a local machine, this will not be of concern).**

### The Online Platforms

There are various platforms that we hope to develop observability into, for the purpose of identifying mobile ads. These platforms (in order of relevance) are TikTok, Snapchat, X (formerly Twitter) and LinkedIn. To help you get started, we provide a TikTok stub as part of the specification and software repository, along with screen recordings for your own testing.

### The TikTok Stub

__Some Tips On Analysis:__

1. **Quick Analysis:** TikTok has a number of visual cues that help distinguish it from other apps. Consider the buttons that sit along the edges of reels, and whether they can be easily detected. Particularly in the Quick Analysis, it might help to abstract the button positionings to certain areas of the image that are consistently coloured as buttons would be, irrespective of the frame. In the simplest approach, one could sample and cross-check the colours of pixels in these areas against other areas of the image to then verify if the recording is of TikTok or not.

2. **Comprehensive Analysis:** For a more comprehensive analysis, consider how the visual cues are then made more specific as we go from determining that we are in the TikTok app to determining that we are observing a TikTok ad - there exist instances such 'Paid Promotions', 'Partnerships', and the typical 'Sponsored' tag that can help you identify that we are indeed observing an ad.

__A Note On The API That Connects To The App: As part of the formal "distribution-ready" version of the app, collected ads are periodically submitted to an online database. Through the course of this project, we do not require that you engage with this aspect of the ad collection process, but rather focus on the app itself. __

## Expanding/Refining The Image Analysis Techniques

An agile (yet mediocre) aspect of the app's design has been the ground-up development of 'semantic' techniques for image analysis of Android devices. More specifically, all image analysis techniques that take place on-device have been written up entirely from the Android Java software language, yielding incredibly basic (and for the most part hard-coded) image processing operations.

While this has been done deliberately to serve battery and processing optimisation, it is speculated that machine-learning approaches may succeed this form of processing, although this line of enquiry hasn't been fully studied, especially in attempts to find a processing technique that is backwards compatible (down to devices as old as the Samsung Galaxy S7).

### Image Analysis Functions

As part of this sub-task, there exist various image analysis functions throughout the `com.adms.australianmobileadtoolkit.interpreter.visual.Visual` and `com.adms.australianmobileadtoolkit.interpreter.platform.Platform` sub-packages - consider the objectives that these functions seek to address when determining what alternative solutions might be appropriate. 

_____________

__Questions__

If you have any questions regarding this specification document, please reach out to Dr Abdul Karim Obeid (obei@qut.edu.au).
