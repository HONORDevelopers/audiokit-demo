
/*
 * Copyright (c) Honor Device Co., Ltd. 2024-2024. All rights reserved.
 *
 */

package com.hihonor.audiokitdev;

import android.content.Context;
import android.media.AudioDeviceInfo;
import android.media.AudioFormat;
import android.media.AudioTrack;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import androidx.annotation.Nullable;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

/**
 * Advanced record play task
 *
 * @since 2024-04-08
 */
public class AudioRecordPlayTask extends AsyncTask<Parameters, Integer, Long> {
    private static final String TAG = "AdvancedRecordPlayTask";

    private volatile boolean isPlaying;

    private final Context mContext;

    private OnTaskFinishedListener mListener;

    private AudioDeviceInfo mPreferredDevice = null;

    /**
     * 构造方法
     *
     * @param context
     */
    public AudioRecordPlayTask(Context context) {
        mContext = context;
    }

    public void setIsPlaying(boolean playing) {
        isPlaying = playing;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        Log.i(TAG, " onPreExecute()");
    }

    /**
     * set listener
     *
     * @param listener task finish listener
     */
    public void setListener(OnTaskFinishedListener listener) {
        this.mListener = listener;
    }

    /**
     * Whether the audio is playing
     *
     * @return playing status
     */
    public boolean isPlaying() {
        return isPlaying;
    }

    /**
     * Set preferred device
     *
     * @param audioDeviceInfo preferred device
     */
    public void setPreferredDevice(AudioDeviceInfo audioDeviceInfo) {
        mPreferredDevice = audioDeviceInfo;
    }

    @Override
    protected void onPostExecute(Long result) {
        super.onPostExecute(result);
        isPlaying = false;
        if (mListener != null) {
            mListener.onTaskFinished();
        }
        Log.i(TAG, " onPostExecute()");
    }

    @Override
    protected Long doInBackground(Parameters... params) {
        Log.i(TAG, " doInBackground()");
        Parameters parameters = params[0];
        int bufferSize = AudioTrack.getMinBufferSize(parameters.getSampleRate(), parameters.getChannels(),
            AudioFormat.ENCODING_PCM_16BIT);
        AudioTrack audioTrack = null;
        try {
            audioTrack = new AudioTrack(parameters.getStreamType(), parameters.getSampleRate(),
                parameters.getChannels(), AudioFormat.ENCODING_PCM_16BIT, bufferSize, AudioTrack.MODE_STREAM);
            if (mPreferredDevice != null) {
                audioTrack.setPreferredDevice(mPreferredDevice);
            }

            // 开始播放
            audioTrack.play();
            Log.i(TAG, "AudioRecordPlay session ID: " + audioTrack.getAudioSessionId());
        } catch (IllegalArgumentException e) {
            Log.i(TAG, "" + e.getMessage());
        }

        int bytesPer10ms = Parameters.DOUBLE * parameters.getChannels()
            * (parameters.getSampleRate() * Parameters.TEN / Parameters.MILLISECOND);
        byte[] tempBuf = new byte[bytesPer10ms];

        AudioTrack finalAudioTrack = audioTrack;
        Optional.ofNullable(getInputStream(parameters)).ifPresent(inputStream -> {
            try {
                int bytesRead;
                if (inputStream.available() > 0) {
                    while (isPlaying) {
                        bytesRead = inputStream.read(tempBuf, 0, bytesPer10ms);

                        // 然后将数据写入到AudioTrack中
                        if (bytesRead > 0) {
                            finalAudioTrack.write(tempBuf, 0, bytesPer10ms);
                        } else {
                            isPlaying = false;
                            break;
                        }
                    }
                }
            } catch (IOException e) {
                Log.i(TAG, "" + e.getMessage());
            } finally {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    Log.i(TAG, "" + e.getMessage());
                }
            }
        });

        // 由于AudioTrack播放的是流，所以，我们需要一边播放一边读取
        audioTrack.stop();
        audioTrack.release();
        return 0L;
    }

    @Nullable
    private InputStream getInputStream(Parameters parameters) {
        InputStream inputStream = null;
        try {
            String path = mContext.getExternalFilesDir(Environment.DIRECTORY_MUSIC).getCanonicalPath() + "/"
                + parameters.getFileName();
            Log.i(TAG, "path is " + path);
            inputStream = new FileInputStream(path);
        } catch (FileNotFoundException e) {
            Log.e(TAG, "FileNotFoundException");
        } catch (IOException e) {
            Log.e(TAG, "IOException");
        }
        return inputStream;
    }

}
