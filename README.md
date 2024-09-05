# Honor Audio Kit Sample Code (Android)
[![Apache-2.0](https://img.shields.io/badge/license-Apache-blue)](http://www.apache.org/licenses/LICENSE-2.0)
[![Open Source Love](https://img.shields.io/static/v1?label=Open%20Source&message=%E2%9D%A4%EF%B8%8F&color=green)](https://developer.hihonor.com/demos/)
[![Java Language](https://img.shields.io/badge/language-java-green.svg)](https://www.java.com/en/)

English | [中文](README_ZH.md)

## Contents

* [Introduction](#Introduction)
* [Preparations](#Preparations)
* [Environment Requirements](#Environment-Requirements)
* [Hardware Requirements](#Hardware-Requirements)
* [Installation](#Installation)
* [Technical Support](#Technical-Support)
* [License](#License)

## Introduction

In this sample code, you will use the created demo project to call APIs of AudioKit-demo. Through the demo project, you will:
1.	Low-latency ear return: You can turn on and off the low-latency ear return capability, set the ear return volume and sound effects, and get a low-latency, low-noise ear return experience.
2.	Multi-channel recording: It can provide the ability to record system sound and environmental sound at the same time, which is suitable for scenarios that have more requirements for recording functions.
3.	AI noise reduction: It uses beam noise reduction and AI noise reduction technology to provide high-quality audio collection functions, suitable for VOIP video or voice calls and audio recording scenarios where human voices need to be highlighted.
4.  High-definition audio playback: It supports lossless playback of audio with a sampling rate of 96~192KHZ. The rich sound information and powerful expressiveness bring an auditory feast to the audience, and is suitable for high-definition audio playback scenarios.
5.  Spatial audio: It provides the capability of virtual sound image externalization, which can perfectly restore the three-dimensional sense of distance and direction, create a 360-degree spatial audio immersive listening experience, and bring users an immersive sense of being there.

For more, see [Business introduction](https://developer.honor.com/cn/docs/audiokit/guides/introduction)


## Environment Requirements

Android Studio 3.X or later and JDK 1.8 or later are recommended.

## Hardware Requirements

A Honor MagicOS 7.0 and above phones with USB data cable, which is used for debugging.

## Preparations

1.  Register as a Honor developer.
2.  Create an app and start APIs.
3.  Import your demo project to Android Studio (Chipmunk | 2022.2.1) or later. Download the mcs-services.json file of the app from Honor Developer Site, and add the file to the root directory of your project. Generate a signing certificate fingerprint, add the certificate file to your project, and add the configuration to the build.gradle file. For details, please refer to the integration preparations.

## Installation
Method 1: Compile and build the APK in Android Studio. Then, install the APK on your phone and debug it.

Method 2: Generate the APK in Android Studio. Use the Android Debug Bridge (ADB) tool to run the **adb install {*YourPath/YourApp.apk*}** command to install the APK on your phone and debug it.

## Technical Support

If you have any questions about the sample code, try the following:
- Visit [Stack Overflow](https://stackoverflow.com/questions/tagged/honor-developer-services?tab=Votes), submit your questions, and tag them with `honor-developer-services`. Honor experts will answer your questions.

If you encounter any issues when using the sample code, submit your [issues](https://github.com/HONORDevelopers/audiokit-demo/issues) or submit a [pull request](https://github.com/HONORDevelopers/audiokit-demo/pulls).

## License
The sample code is licensed under [Apache License 2.0](http://www.apache.org/licenses/LICENSE-2.0).