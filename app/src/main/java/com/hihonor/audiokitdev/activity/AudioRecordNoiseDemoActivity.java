/*
 * Copyright (c) Honor Device Co., Ltd. 2023-2023. All rights reserved.
 */

package com.hihonor.audiokitdev.activity;

import static android.media.AudioFormat.CHANNEL_IN_STEREO;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.hihonor.android.magicx.media.audio.config.ResultCode;
import com.hihonor.android.magicx.media.audio.interfaces.HnAdvancedRecordClient;
import com.hihonor.android.magicx.media.audio.interfaces.HnAudioClient;
import com.hihonor.android.magicx.media.audio.interfaces.IAudioServiceCallback;
import com.hihonor.audiokitdev.AudioRecordPlayTask;
import com.hihonor.audiokitdev.AudioRecordTask;
import com.hihonor.audiokitdev.OnTaskFinishedListener;
import com.hihonor.audiokitdev.Parameters;
import com.hihonor.audiokitdev.R;

import java.util.List;

/**
 * 录音降噪
 *
 * @author libo lw00098691
 * @since 2023-02-15
 */
public class AudioRecordNoiseDemoActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "AudioRecordNoiseDemoActivity";
    private static final String[] PERMISSIONS = {Manifest.permission.RECORD_AUDIO,
        Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
    private static final int INTENT_FLAG = 123;
    private TextView mInfoTextView;
    private AudioManager mAudioManager = null;
    private AudioRecordTask mAudioRecordTask = null;
    private AudioRecordPlayTask mAudioRecordPlayTask = null;

    private HnAudioClient mHnAudioClient;

    private HnAdvancedRecordClient mHnAdvancedRecordClient;

    private boolean mIsAdvancedRecordBindSuccess = false;

    private Switch mReduceNoiseSwitch;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recordnoise_view);
        setTitle("录音降噪DEMO");
        checkPermissions();
        initView();
        createAudioManager();
        initHnAudioClient();
    }

    private void initHnAudioClient() {
        if (!HnAudioClient.isDeviceSupported(this)) {
            Log.e(TAG, "audio engine is not supported");
            return;
        }
        mHnAudioClient = new HnAudioClient(this, new IAudioServiceCallback() {
            @Override
            public void onResult(int result) {
                switch (result) {
                    case ResultCode.AUDIO_SERVICE_SUCCESS:
                        List<Integer> list = mHnAudioClient.getSupportedServices();
                        if ((list != null) && (list.size() > 0)) {
                            for (int service : list) {
                                if (service == HnAudioClient.ServiceType.HNAUDIO_SERVICE_HIGHSAMPLERATEPLAY
                                    .getServiceType()) {
                                    initHnAudioPlayClient();
                                    break;
                                }
                            }
                        } else {
                            mInfoTextView.setText("当前设备不支持录音降噪");
                            Log.i(TAG, "current device not support record denoise.");
                        }
                        break;
                    case ResultCode.ADVANCED_RECORD_SUCCESS:
                        mIsAdvancedRecordBindSuccess = true;
                        break;
                    default:
                        break;
                }
            }
        });
        mHnAudioClient.initialize();
    }

    private void initHnAudioPlayClient() {
        mHnAdvancedRecordClient = mHnAudioClient.createService(HnAudioClient.ServiceType.HNAUDIO_SERVICE_RECORDDENOISE);
    }
    /**
     * 初始化View
     */
    private void initView() {
        Button mStartDefaultRecordButton = findViewById(R.id.startDefaultRecordButton);
        mStartDefaultRecordButton.setOnClickListener(this);
        Button mstopDefaultRecordButton = findViewById(R.id.stopDefaultRecordButton);
        mstopDefaultRecordButton.setOnClickListener(this);
        Button mPlayDefaultRecordButton = findViewById(R.id.playDefaultRecordButton);
        mPlayDefaultRecordButton.setOnClickListener(this);
        Button mStopPlayRecordButton = findViewById(R.id.stopPlayRecordButton);
        mStopPlayRecordButton.setOnClickListener(this);
        mReduceNoiseSwitch = findViewById(R.id.switch_reduce_noise);
        mInfoTextView = findViewById(R.id.infoTextView);
        findViewById(R.id.setRecordMode).setOnClickListener(this);
        mReduceNoiseSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (mHnAdvancedRecordClient != null && mIsAdvancedRecordBindSuccess) {
                    int result;
                    result = mHnAdvancedRecordClient.enableRecordDenoise(isChecked,
                            HnAdvancedRecordClient.DenoiseMode.DENOISE_NN_MODE,
                            HnAdvancedRecordClient.DenoiseScene.DENOISE_SPEAK_SCENE,
                            HnAdvancedRecordClient.DenoiseLevel.DENOISE_DEFAULT_LEVEL);
                    if (result != 0) {
                        mInfoTextView.setText("功能开启或者关闭失败，错位码为: " + result);
                    } else {
                        mInfoTextView.setText("功能开启或者关闭成功");
                    }
                } else {
                    mInfoTextView.setText("增强录音服务绑定失败");
                }
            }
        });
    }

    /**
     * 创建AudioManager对象
     */
    private void createAudioManager() {
        if (mAudioManager == null) {
            Object systemService = this.getSystemService(Context.AUDIO_SERVICE);
            if (systemService instanceof AudioManager) {
                mAudioManager = (AudioManager) systemService;
            }
        }
    }

    /**
     * Called when view has been clicked
     */
    @Override
    public void onClick(View view) {
        if (view == null) {
            Log.e(TAG, "view is null");
            return;
        }
        int id = view.getId();
        if (id == R.id.startDefaultRecordButton) {
            if (mAudioRecordTask != null && mAudioRecordTask.isRecording()) {
                mInfoTextView.setText("正在录音，请先停止");
                return;
            }
            startMicRecord();
        } else if (id == R.id.stopDefaultRecordButton) {
            if (mAudioRecordTask != null && mAudioRecordTask.isRecording()) {
                stopMicRecord();
                mInfoTextView.setText("录音已停止");
            }
        } else if (id == R.id.playDefaultRecordButton) {
            if (mAudioRecordPlayTask != null && mAudioRecordPlayTask.isPlaying()) {
                mInfoTextView.setText("录音正在播放，请先停止");
                return;
            }
            playReduceNoiseRecord();
        } else if (id == R.id.stopPlayRecordButton) {
            if (mAudioRecordPlayTask != null && mAudioRecordPlayTask.isPlaying()) {
                mAudioRecordPlayTask.setIsPlaying(false);
                mInfoTextView.setText("已停止录音1");
            }
        } else if (id == R.id.setRecordMode) {
            mAudioManager.setParameters("RECORD_SCENE=video");
        }
    }

    private void playReduceNoiseRecord() {
        mAudioRecordPlayTask = new AudioRecordPlayTask(this);
        mAudioRecordPlayTask.setListener(new OnTaskFinishedListener() {
            @Override
            public void onTaskFinished() {
                mInfoTextView.setText("录音播放结束");
            }
        });
        Parameters parameters = new Parameters(Parameters.MIC, Parameters.DEFAULT_SAMPLE_RATE, CHANNEL_IN_STEREO,
            AudioManager.STREAM_MUSIC);
        parameters.setSavedFileName("recordDenoiseDemo.pcm");
        mAudioRecordPlayTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, parameters);
        mAudioRecordPlayTask.setIsPlaying(true);
        mInfoTextView.setText("正在播放录音");
    }

    private void stopMicRecord() {
        if (mAudioRecordTask != null) {
            mAudioRecordTask.setIsRecording(false);
            if (mAudioRecordTask.isRecording()) {
                mAudioRecordTask.cancel(true);
            }
            mReduceNoiseSwitch.setChecked(false);
        }
    }

    private void startMicRecord() {
        mAudioManager.setParameters("RECORD_SCENE=video");
        Parameters parameters = new Parameters(Parameters.MIC, Parameters.DEFAULT_SAMPLE_RATE, CHANNEL_IN_STEREO,
            AudioManager.STREAM_MUSIC);
        parameters.setSavedFileName("recordDenoiseDemo.pcm");
        mAudioRecordTask = new AudioRecordTask(this, parameters);
        mAudioRecordTask.setIsRecording(true);
        mAudioRecordTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        mInfoTextView.setText("正在录音");
    }

    /**
     * Permission check
     */
    private void checkPermissions() {
        if (!hasPermission()) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, INTENT_FLAG);
        }
    }

    /**
     * 是否有权限
     *
     * @return true：有 false:没有
     */
    private boolean hasPermission() {
        for (String permission : PERMISSIONS) {
            int result = ContextCompat.checkSelfPermission(this, permission);
            if (result != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
        @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == INTENT_FLAG) {
            if (PackageManager.PERMISSION_GRANTED == grantResults[0] && grantResults.length >= 1) {
                mInfoTextView.setText("权限申请成功");
            } else {
                mInfoTextView.setText("权限申请失败");
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopMicRecord();
        if (mAudioRecordPlayTask != null) {
            mAudioRecordPlayTask.setIsPlaying(false);
            mInfoTextView.setText("已停止录音播放");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopMicRecord();
        if (mAudioRecordPlayTask != null) {
            mAudioRecordPlayTask.setIsPlaying(false);
            mInfoTextView.setText("已停止录音播放");
        }
        if (mIsAdvancedRecordBindSuccess) {
            mHnAdvancedRecordClient.destroy();
            mIsAdvancedRecordBindSuccess = false;
            mHnAudioClient.destroy();
        }
    }
}
