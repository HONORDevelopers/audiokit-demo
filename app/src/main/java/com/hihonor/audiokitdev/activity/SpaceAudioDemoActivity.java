/*
 * Copyright (c) Honor Device Co., Ltd. 2024-2024. All rights reserved.
 */

package com.hihonor.audiokitdev.activity;

import android.content.Context;
import android.media.AudioManager;
import android.media.Spatializer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.hihonor.audiokitdev.R;

import java.util.concurrent.Executor;
/**
 * 空间音频DEMO
 *
 * @author libo lw0098691
 * @since 2024-07-12
 */
public class SpaceAudioDemoActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "AudioRecordNoiseDemoActivity";
    private TextView mInfoEnableView;
    private TextView mInfoAvailableView;
    private AudioManager mAudioManager = null;
    private Spatializer mSpatializer;

    private final Spatializer.OnSpatializerStateChangedListener listener =
        new Spatializer.OnSpatializerStateChangedListener() {
            @Override
            public void onSpatializerEnabledChanged(@NonNull Spatializer spat, boolean isEnabled) {
                if (isEnabled) {
                    // TODO 空间音频开启
                    mInfoEnableView.setText("监听到当前空间音频功能已开启");
                } else {
                    // TODO 空间音频关闭
                    mInfoEnableView.setText("监听到当前空间音频功能已关闭");
                }
            }

            @Override
            public void onSpatializerAvailableChanged(@NonNull Spatializer spat, boolean isAvailable) {
                if (isAvailable) {
                    // TODO 空间音频可用
                    mInfoAvailableView.setText("监听到当前空间音频功能可用");
                } else {
                    // TODO 空间音频不可用
                    mInfoAvailableView.setText("监听到当前空间音频功能不可用");
                }
            }
        };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apaceaudio_view);
        setTitle("空间音频DEMO");
        initView();
        createAudioManager();
    }

    /**
     * 初始化View
     */
    private void initView() {
        Button spaceAudioIsAvailable = findViewById(R.id.space_Audio_Is_Available_Button);
        spaceAudioIsAvailable.setOnClickListener(this);
        Button spaceAudioIsEnable = findViewById(R.id.space_Audio_Is_Enable_Button);
        spaceAudioIsEnable.setOnClickListener(this);
        mInfoEnableView = findViewById(R.id.infoTextView);
        mInfoAvailableView = findViewById(R.id.infoTextView2);
    }

    /**
     * 创建AudioManager对象
     */
    private void createAudioManager() {
        if (mAudioManager == null) {
            Object systemService = this.getSystemService(Context.AUDIO_SERVICE);
            if (systemService instanceof AudioManager) {
                mAudioManager = (AudioManager) systemService;
                mSpatializer = mAudioManager.getSpatializer();
                mSpatializer.addOnSpatializerStateChangedListener(new Executor() {
                    @Override
                    public void execute(Runnable command) {
                        command.run();
                    }
                }, listener);
            }
        }
    }

    /**
     * Called when view has been clicked
     *
     * @param view The view that was clicked.
     */
    @Override
    public void onClick(View view) {
        if (view == null) {
            Log.e(TAG, "view is null");
            return;
        }

        int id = view.getId();
        if (id == R.id.space_Audio_Is_Available_Button) {
            boolean isAvailable = mSpatializer.isAvailable();
            if (isAvailable) {
                // TODO 空间音频音效在当前设备上可用，可以做相关业务处理
                mInfoAvailableView.setText("当前设备空间音频可用");
            } else {
                mInfoAvailableView.setText("当前设备空间音频不可用");
            }
        } else if (id == R.id.space_Audio_Is_Enable_Button) {
            boolean isEnabled = mSpatializer.isEnabled();
            if (isEnabled) {
                // TODO 空间音频音效在当前设备上已经开启，可以做相关业务处理
                mInfoEnableView.setText("当前设备空间音频功能已开启");
            } else {
                mInfoEnableView.setText("当前设备空间音频功能尚未开启");
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mSpatializer.removeOnSpatializerStateChangedListener(listener);
    }
}
