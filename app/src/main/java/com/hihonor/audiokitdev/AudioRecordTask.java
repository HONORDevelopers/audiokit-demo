
/*
 * Copyright (c) Honor Device Co., Ltd. 2022-2022. All rights reserved.
 */

package com.hihonor.audiokitdev;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioPlaybackCaptureConfiguration;
import android.media.AudioRecord;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Optional;

/**
 * Advanced record task
 *
 * @since 2022-04-08
 */
public class AudioRecordTask extends AsyncTask<Parameters, Integer, Long> {
    private static final String TAG = "AudioRecordTask";

    private final Context mContext;

    private final Parameters mParameters;

    private volatile boolean mIsRecording = false;
    private OnTaskFinishedListener mListener;
    private AudioPlaybackCaptureConfiguration mConfiguration;

    /**
     * AudioRecordTask
     *
     * @param context
     * @param parameters
     *
     */
    public AudioRecordTask(Context context, Parameters parameters) {
        this.mContext = context;
        mParameters = parameters;
    }

    /**
     * get current recording status
     *
     * @return recording status
     */
    public boolean isRecording() {
        return mIsRecording;
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
     * set current recording status
     *
     * @param isRecording recording status
     */
    public void setIsRecording(boolean isRecording) {
        Log.i(TAG, "isRecording = " + isRecording);
        mIsRecording = isRecording;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        Log.i(TAG, "AdvancedRecordTask.onPreExecute()");
    }

    @Override
    protected void onPostExecute(Long result) {
        super.onPostExecute(result);
        Log.i(TAG, "AdvancedRecordTask.onPostExecute()");
        if (mListener != null) {
            mListener.onTaskFinished();
        }
    }

    @Override
    protected Long doInBackground(Parameters... params) {
        Long result = null;
        int bufferSize = AudioRecord.getMinBufferSize(mParameters.getSampleRate(), mParameters.getChannels(),
            AudioFormat.ENCODING_PCM_16BIT) * Parameters.DOUBLE;
        Log.i(TAG, "AdvancedRecordTask.AudioRecord bufferSize = " + bufferSize);

        AudioRecord audioRecord = getAudioRecord(bufferSize);
        if (audioRecord == null) {
            return result;
        }

        // 开始录制
        try {
            audioRecord.startRecording();
            Log.i(TAG, "AudioRecord session ID: " + audioRecord.getAudioSessionId());
        } catch (IllegalStateException e) {
            Log.e(TAG, "start recording fail for IllegalStateException." + e);
            return result;
        }

        Optional.ofNullable(getFileOutputStream()).ifPresent(outputStream -> {
            try {
                int bytesPer10ms = Parameters.DOUBLE * mParameters.getChannels()
                    * (mParameters.getSampleRate() * Parameters.TEN / Parameters.MILLISECOND);
                int bytesRead;
                byte[] tempBuf = new byte[bytesPer10ms];

                // 定义循环，根据isRecording的值来判断是否继续录制
                Log.i(TAG, "isRecording = " + mIsRecording);
                while (mIsRecording) {
                    bytesRead = audioRecord.read(tempBuf, 0, bytesPer10ms);
                    if (bytesRead > 0) {
                        outputStream.write(tempBuf);
                    }
                }
            } catch (IOException e) {
                Log.i(TAG, e.getMessage());
            } finally {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    Log.i(TAG, e.getMessage());
                }
            }
        });

        // 录制结束
        audioRecord.stop();
        audioRecord.release();
        Log.i(TAG, "stop record  ");
        return result;
    }

    @Nullable
    private AudioRecord getAudioRecord(int bufferSize) {
        AudioRecord audioRecord = null;
        try {
            if (ActivityCompat.checkSelfPermission(mContext,
                Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                if (mParameters.getSource() == Parameters.REMOTE_SUBMIX) {
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                        AudioRecord.Builder builder = new AudioRecord.Builder()
                            .setAudioFormat(new AudioFormat.Builder().setChannelMask(mParameters.getChannels())
                                .setSampleRate(mParameters.getSampleRate())
                                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                                .build())
                            .setBufferSizeInBytes(bufferSize)
                            .setAudioPlaybackCaptureConfig(mConfiguration);
                        audioRecord = builder.build();
                    }
                } else {
                    audioRecord = new AudioRecord(mParameters.getSource(), mParameters.getSampleRate(),
                        mParameters.getChannels(), AudioFormat.ENCODING_PCM_16BIT, bufferSize);
                }
            } else {
                Log.e(TAG, "has no permission RECORD_AUDIO");
            }
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "init audioRecord fail for IllegalArgumentException");
        }
        return audioRecord;
    }

    @Nullable
    private FileOutputStream getFileOutputStream() {
        FileOutputStream fileOutputStream = null;
        try {
            String path = mContext.getExternalFilesDir(Environment.DIRECTORY_MUSIC).getCanonicalPath()
                + "/" + mParameters.getFileName();
            Log.i(TAG, "path is " + path);
            fileOutputStream = new FileOutputStream(path);
        } catch (FileNotFoundException e) {
            Log.e(TAG, "FileNotFoundException");
        } catch (IOException e) {
            Log.e(TAG, "IOException");
        }
        return fileOutputStream;
    }

    /**
     * set configuration
     *
     * @param configuration audio playback capture configuration
     */
    public void setConfiguration(AudioPlaybackCaptureConfiguration configuration) {
        mConfiguration = configuration;
    }
}
