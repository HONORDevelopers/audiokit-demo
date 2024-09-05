/*
 * Copyright (c) Honor Device Co., Ltd. 2023-2023. All rights reserved.
 */

package com.hihonor.audiokitdev.activity;

import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.hihonor.android.magicx.media.audio.config.ResultCode;
import com.hihonor.android.magicx.media.audio.interfaces.HnAudioClient;
import com.hihonor.android.magicx.media.audio.interfaces.HnAudioPlayClient;
import com.hihonor.android.magicx.media.audio.interfaces.IAudioServiceCallback;
import com.hihonor.audiokitdev.R;

import java.io.IOException;
import java.util.List;

/**
 * High sample rate play demo activity
 *
 * @since 2023-06-15
 */
public class HighSampleRatePlayDemoActivity extends AppCompatActivity
    implements View.OnClickListener, IAudioServiceCallback {
    private static final String TAG = "HighSampleRatePlayDemoActivity";
    private Button mEnableHighSampleRatePlay;
    private Button mDisableHighSampleRatePlay;
    private HnAudioClient mHnAudioClient;

    private HnAudioPlayClient mHnAudioPlayClient;

    private String mResultStr;

    private boolean mIsAudioPlayBindSuccess;

    private TextView mTextView;

    private String mUnsupportvideodevice = "";
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_high_sample_rate_play_demo);
        setTitle("高采样率音频播放Demo");
        mUnsupportvideodevice =
                HighSampleRatePlayDemoActivity.this.getResources().getString(R.string.unsupportvideodevice);
        mEnableHighSampleRatePlay = findViewById(R.id.enableHighSampleRatePlay);
        mDisableHighSampleRatePlay = findViewById(R.id.disableHighSampleRatePlay);
        mEnableHighSampleRatePlay.setOnClickListener(this);
        mDisableHighSampleRatePlay.setOnClickListener(this);
        mTextView = findViewById(R.id.resultShow);
    }

    @Override
    public void onClick(View view) {
        if (view == null) {
            Log.e(TAG, "view is null");
            return;
        }
        int id = view.getId();
        if (id == R.id.enableHighSampleRatePlay) {
            initAudioKit();
        } else if (id == R.id.disableHighSampleRatePlay) {
            if (mIsAudioPlayBindSuccess && mHnAudioPlayClient != null) {
                mHnAudioPlayClient.enableHighSampleRatePlay(false);
                mTextView.setText("高清音频播放功能关闭");
            }
            if (mHnAudioClient != null) {
                mHnAudioClient.destroy();
            }
            if (mHnAudioPlayClient != null) {
                mHnAudioPlayClient.destroy();
                mIsAudioPlayBindSuccess = false;
            }
        }
    }

    private void initAudioKit() {
        Log.i(TAG, "initAudioKit");
        if (HnAudioClient.isDeviceSupported(this)) {
            mHnAudioClient = new HnAudioClient(this, this);
            mHnAudioClient.initialize();

        }
    }

    @Override
    public void onResult(int result) {
        Log.i(TAG, "resultType = " + result);
        mResultStr = "";
        setResultType(result);
        switch (result) {
            case ResultCode.VENDOR_NOT_SUPPORTED:
                mResultStr = getResources().getString(R.string.notInstallAudioKitService);
                break;
            case ResultCode.AUDIO_SERVICE_DISCONNECTED:
                mResultStr = getResources().getString(R.string.audioKitServiceDisconnect);
                break;
            case ResultCode.AUDIO_SERVICE_DIED:
                mResultStr = getResources().getString(R.string.audioKitServiceDied);
                break;
            default:
                break;
        }
    }

    private void setResultType(int resultType) {
        switch (resultType) {
            case ResultCode.AUDIO_SERVICE_SUCCESS -> {
                List<Integer> servicesList = mHnAudioClient.getSupportedServices();
                if ((servicesList != null) && (servicesList.size() > 0)) {
                    for (Integer service : servicesList) {
                        if (service == HnAudioClient.ServiceType.HNAUDIO_SERVICE_HIGHSAMPLERATEPLAY.getServiceType()) {
                            initHnAudioPlayClient();
                            return;
                        }
                    }
                    mTextView.setText(mUnsupportvideodevice);
                } else {
                    Log.e(TAG, "no service supported on this service");
                    mTextView.setText(mUnsupportvideodevice);
                }
            }
            case ResultCode.AUDIO_PLAY_SERVICE_SUCCESS -> {
                mIsAudioPlayBindSuccess = true;
                mHnAudioPlayClient.enableHighSampleRatePlay(true);
                mTextView.setText("高清音频播放功能开启");
            }
            case ResultCode.AUDIO_PLAY_SERVICE_DISCONNECTED -> mIsAudioPlayBindSuccess = false;
            case ResultCode.HIGHSAMPLERATE_PLAY_SERVICE_UNSUPPORTED -> Log.i(TAG, "version check fail");
            default -> {
            }
        }
    }

    private void initHnAudioPlayClient() {
        Log.i(TAG, " initHnAudioPlayClient");
        if (mHnAudioClient != null) {
            mHnAudioPlayClient =
                mHnAudioClient.createService(HnAudioClient.ServiceType.HNAUDIO_SERVICE_HIGHSAMPLERATEPLAY);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}