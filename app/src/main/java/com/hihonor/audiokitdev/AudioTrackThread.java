
/*
 * Copyright (c) Honor Device Co., Ltd. 2022-2022. All rights reserved.
 */

package com.hihonor.audiokitdev;

import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioTrack;
import android.util.Log;

/**
 * 功能描述
 *
 * @since 2022-02-21
 */
public class AudioTrackThread extends Thread {
    private static final String TAG = "EarReturn.AudioTrackThread";

    private static final int DEFAULT_INT_TYPE = 0;

    private static final int SAMPLE_RATE = 44100;

    private boolean mIsRunning = true;

    private AudioTrack mAudioTrack = null;

    @Override
    public void run() {
        setPriority(Thread.MAX_PRIORITY);
        int buffsize =
            AudioTrack.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_IN_STEREO, AudioFormat.ENCODING_PCM_16BIT);
        try {
            AudioAttributes audioAttributes =
                new AudioAttributes.Builder().setFlags(AudioAttributes.FLAG_LOW_LATENCY).build();
            mAudioTrack = new AudioTrack.Builder().setAudioAttributes(audioAttributes)
                .setBufferSizeInBytes(buffsize)
                .setAudioFormat(new AudioFormat.Builder().setChannelMask(AudioFormat.CHANNEL_IN_STEREO)
                    .setSampleRate(SAMPLE_RATE)
                    .build())
                .build();
        } catch (IllegalThreadStateException e) {
            Log.e(TAG, "new Track IllegalThreadStateException");
        }
        if (mAudioTrack == null) {
            Log.e(TAG, "mAudioTrack is null");
            return;
        }
        audioTrackWrite(buffsize);
        mAudioTrack.release();
    }

    private void audioTrackWrite(int buffsize) {
        short[] samples = new short[buffsize];
        if (mAudioTrack.getState() != AudioTrack.STATE_INITIALIZED) {
            Log.e(TAG, "mAudioTrack uninitialized");
        } else {
            try {
                mAudioTrack.play();
                while (mIsRunning) {
                    mAudioTrack.write(samples, DEFAULT_INT_TYPE, buffsize);
                }
            } catch (IllegalThreadStateException | IllegalStateException e) {
                Log.e(TAG, "running IllegalThreadStateException");
            }
            try {
                mAudioTrack.stop();
            } catch (IllegalStateException e) {
                Log.e(TAG, "stop IllegalStateException");
            }
        }
    }

    /**
     * thread destroy
     *
     * @since 2022-03-01
     */
    public void destroy() {
        if (mAudioTrack != null) {
            try {
                mAudioTrack.stop();
            } catch (IllegalStateException e) {
                Log.e(TAG, "stop IllegalThreadStateException");
            }
        }
        mIsRunning = false;
    }
}