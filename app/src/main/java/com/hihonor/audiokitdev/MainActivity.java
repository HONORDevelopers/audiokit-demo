/*
 * Copyright (c) Honor Device Co., Ltd. 2024-2024. All rights reserved.
 */

package com.hihonor.audiokitdev;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.hihonor.audiokitdev.activity.SpaceAudioDemoActivity;
import com.hihonor.audiokitdev.activity.AudioRecordNoiseDemoActivity;
import com.hihonor.audiokitdev.activity.EarReturnAndRecordDemoActivity;
import com.hihonor.audiokitdev.activity.HighSampleRatePlayDemoActivity;

/**
 * Ear return and advanced record demo activity
 *
 * @author d00013419
 * @since 2024-01-14
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "MainActivity";
    private Button mEarReturnAndRecordBtn;
    private Button mHighSampleRatePlayBtn;

    private Button recordNoiseReduction;

    private Button spaceAudioButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_advanced_record);
        mEarReturnAndRecordBtn = findViewById(R.id.earReturnAndRecordButton);
        mHighSampleRatePlayBtn = findViewById(R.id.highSampleRatePlay);
        recordNoiseReduction = findViewById(R.id.recordNoiseReduction);
        spaceAudioButton = findViewById(R.id.spaceAudioButton);
        mEarReturnAndRecordBtn.setOnClickListener(this);
        recordNoiseReduction.setOnClickListener(this);
        spaceAudioButton.setOnClickListener(this);
        mHighSampleRatePlayBtn.setOnClickListener(this);
    }

    /**
     * Called when view has been clicked.
     *
     * @param view The view that was clicked.
     */
    @Override
    public void onClick(View view) {
        if (view == null) {
            Log.e(TAG, "view si null");
            return;
        }
        Intent intent;
        int id = view.getId();
        if (id == R.id.earReturnAndRecordButton) {
            intent = new Intent(this, EarReturnAndRecordDemoActivity.class);
            startActivity(intent);
        } else if (id == R.id.highSampleRatePlay) {
            intent = new Intent(this, HighSampleRatePlayDemoActivity.class);
            startActivity(intent);
        } else if (id == R.id.recordNoiseReduction) {
            intent = new Intent(this, AudioRecordNoiseDemoActivity.class);
            startActivity(intent);
        } else if (id == R.id.spaceAudioButton) {
            intent = new Intent(this, SpaceAudioDemoActivity.class);
            startActivity(intent);
        }
    }
}
