
/*
 * Copyright (c) Honor Device Co., Ltd. 2022-2022. All rights reserved.
 */

package com.hihonor.audiokitdev;

import android.media.MediaRecorder;

/**
 * Parameters
 *
 * @since 2022-04-08
 */
public class Parameters {
    /**
     * Audio source REMOTE_SUBMIX_EXTEND
     */
    public static final int REMOTE_SUBMIX = MediaRecorder.AudioSource.REMOTE_SUBMIX;

    /**
     * Audio source MIC
     */
    public static final int MIC = MediaRecorder.AudioSource.MIC;

    /**
     * Audio source for play
     */
    public static final int UNKNOWN = 0;

    /**
     * Constant double
     */
    public static final int DOUBLE = 2;

    /**
     * Constant double
     */
    public static final int TEN = 10;

    /**
     * Constant double
     */
    public static final int MILLISECOND = 1000;

    /**
     * DEFAULT_SAMPLE_RATE
     */
    public static final int DEFAULT_SAMPLE_RATE = 16000;

    private int mSource;

    private int mSampleRate;

    private int mChannels;

    private int mStreamType;

    private String mFileName;

    /**
     * Parameters
     *
     * @param source
     * @param sampleRate
     * @param channels
     * @param streamType
     */
    public Parameters(int source, int sampleRate, int channels, int streamType) {
        mSource = source;
        mSampleRate = sampleRate;
        mChannels = channels;
        mStreamType = streamType;
    }

    /**
     * Parameters
     *
     * @param source
     * @param sampleRate
     * @param channels
     * @param streamType
     * @param fileName
     */
    public Parameters(int source, int sampleRate, int channels, int streamType, String fileName) {
        mSource = source;
        mSampleRate = sampleRate;
        mChannels = channels;
        mStreamType = streamType;
        mFileName = fileName;
    }

    /**
     * get source
     *
     * @return audio source
     */
    public int getSource() {
        return mSource;
    }

    /**
     * get sample rate
     *
     * @return sample rate
     */
    public int getSampleRate() {
        return mSampleRate;
    }

    /**
     * get channel
     *
     * @return channel
     */
    public int getChannels() {
        return mChannels;
    }

    /**
     * get stream type
     *
     * @return stream type
     */
    public int getStreamType() {
        return mStreamType;
    }

    /**
     * Get saved file name
     *
     * @return saved file name
     */
    public String getFileName() {
        return mFileName;
    }

    /**
     * Set saved file name
     *
     * @param fileName saved file name
     */
    public void setSavedFileName(String fileName) {
        mFileName = fileName;
    }
}
