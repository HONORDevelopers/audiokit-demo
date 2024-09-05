
/*
 * Copyright (c) Honor Device Co., Ltd. 2022-2022. All rights reserved.
 */

package com.hihonor.audiokitdev.activity;

import static android.media.AudioFormat.CHANNEL_IN_STEREO;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.media.projection.MediaProjectionManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.hihonor.android.magicx.media.audio.config.ResultCode;
import com.hihonor.android.magicx.media.audio.interfaces.HnAdvancedRecordClient;
import com.hihonor.android.magicx.media.audio.interfaces.HnAudioClient;
import com.hihonor.android.magicx.media.audio.interfaces.HnEarReturnClient;
import com.hihonor.android.magicx.media.audio.interfaces.IAudioServiceCallback;
import com.hihonor.android.magicx.media.audio.utils.Constant;
import com.hihonor.audiokitdev.AudioRecordPlayTask;
import com.hihonor.audiokitdev.AudioTrackThread;
import com.hihonor.audiokitdev.CaptureService;
import com.hihonor.audiokitdev.OnTaskFinishedListener;
import com.hihonor.audiokitdev.Parameters;
import com.hihonor.audiokitdev.R;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * 演示界面
 *
 * @since 2022-04-08
 */
public class EarReturnAndRecordDemoActivity extends Activity
    implements View.OnClickListener, IAudioServiceCallback, OnTaskFinishedListener {
    private static final String TAG = "EarReturnAndRecordDemoActivity";

    private static final String FEATURE_TYPE = " HNAUDIO_SERVICE_EARRETURN";

    private static final String FEATURERECORDER_TYPE = " HNAUDIO_SERVICE_ADVANCEDRECORD";

    private static final String[] PERMISSIONS =
        {Manifest.permission.RECORD_AUDIO, Manifest.permission.WAKE_LOCK, Manifest.permission.POST_NOTIFICATIONS};

    private static final int INTENT_FLAG = 123;

    private static final int INIT_VOLUME = 50;

    private static final int RESULT_CODE = 1;

    /**
     * 初始化.
     */
    private Button mInitButton;

    /**
     * 获取支持的功能类型.
     */
    private Button mGetFeaturesButton;

    /**
     * 是否支持查询的功能类型.
     */
    private Button misEarReturnSupportedBtn;

    /**
     * 是否支持增强录音查询的功能类型.
     */
    private Button mIsAdvancedRecordSupportButton;

    /**
     * 初始化耳返
     */
    private Button mInitKaraokeButton;

    /**
     * 初始化增强录音.
     */
    private Button mInitAudioGameLive;

    private Button mIsSupportAdvancedRecord;

    /**
     * 是否支持耳返功能.
     */
    private Button mIsKaraokeSupportButton;

    /**
     * 打开耳返功能.
     */
    private Button mEnableKaraokeButton;

    /**
     * 关闭耳返功能.
     */
    private Button mDisableKaraokeButton;

    private Button mDefaultReverberationMode;

    private Button mKtvReverberationMode;

    private Button mTheaterReverberationMode;

    private Button mConcertReverberationMode;

    private EditText mEditTextSetVolume;

    private Button mSetVolume;

    private Button mDefaultEqualizerMode;

    private Button mFullEqualizerMode;

    private Button mBrightEqualizerMode;

    private Button audioGameLiveVoiceRecording;

    private Button audioGameLiveRequestVoice;

    /**
     * 信息显示TextView.
     */
    private TextView mInfoTextView;

    // 增强录音功能按钮
    private Button mStartAdvancedRecordBtn;

    private Button mStopAdvancedRecordBtn;

    private Button mStartPlayMicBtn;

    private Button mStartPlaySubmixBtn;

    private Button mStopPlayBtn;

    private Button mMicRecordOnBtn;

    private Button mMicRecordOffBtn;

    private Button mDoubleRecordPlayBtn;

    private AudioRecordPlayTask mAudioRecordPlayTask = null;

    private HnAudioClient mHnAudioClient;

    private HnEarReturnClient mHnEarReturnClient;

    private HnAdvancedRecordClient mHnAdvancedRecordClient;

    private String mResultType = "";

    private boolean mIsAudiokitBindSuccess = false;

    private boolean mIsEarReturnBindSuccess = false;

    private boolean mIsAdvancedRecordBindSuccess = false;

    private boolean mIsAdvancedRecordEnable = false;

    private MediaProjectionManager mMediaProjectionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_main);
        initView();
        initData();
        setClickListener();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();

        // we suggest stop ear return if not using record
        if (mIsEarReturnBindSuccess) {
            enableKaraoke(false);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy");
        if (mAudioRecordPlayTask != null) {
            mAudioRecordPlayTask.setIsPlaying(false);
            mAudioRecordPlayTask.cancel(true);
        }
        if (mHnAudioClient != null) {
            mHnAudioClient.destroy();
        }
        if (mHnEarReturnClient != null) {
            mHnEarReturnClient.destroy();
        }
    }

    /**
     * Initialize View
     */
    private void initView() {
        mInitButton = findViewById(R.id.init);
        audioGameLiveVoiceRecording = findViewById(R.id.disableAdvancedRecording);
        audioGameLiveRequestVoice = findViewById(R.id.enableAdvancedRecord);
        mGetFeaturesButton = findViewById(R.id.getFeaturesButton);
        misEarReturnSupportedBtn = findViewById(R.id.isEarReturnSupportButton);
        mIsAdvancedRecordSupportButton = findViewById(R.id.isAdvancedRecordSupportBtn);
        mInitKaraokeButton = findViewById(R.id.initKaraoke);
        mInitAudioGameLive = findViewById(R.id.initAudioGameLive);
        mIsKaraokeSupportButton = findViewById(R.id.isKaraokeSupportButton);
        mEnableKaraokeButton = findViewById(R.id.enableKaraokeButton);
        mDisableKaraokeButton = findViewById(R.id.disableKaraokeButton);
        mDefaultReverberationMode = findViewById(R.id.setDefaultReverberationMode);
        mKtvReverberationMode = findViewById(R.id.setKtvReverberationMode);
        mTheaterReverberationMode = findViewById(R.id.setTheaterReverberationMode);
        mConcertReverberationMode = findViewById(R.id.setConcertReverberationMode);
        mDefaultEqualizerMode = findViewById(R.id.setDefaultEqualizer);
        mFullEqualizerMode = findViewById(R.id.setFullEqualizer);
        mBrightEqualizerMode = findViewById(R.id.setBrightEqualizer);
        mEditTextSetVolume = findViewById(R.id.setKaraokeAudioVolume);
        mSetVolume = findViewById(R.id.settingVolume);
        mInfoTextView = findViewById(R.id.infoTextView);
        initAdvancedRecordButton();
    }

    private void initAdvancedRecordButton() {
        mIsSupportAdvancedRecord = findViewById(R.id.isSupportAdvancedRecord);
        mStartAdvancedRecordBtn = findViewById(R.id.startAdvancedRecordBtn);
        mStopAdvancedRecordBtn = findViewById(R.id.stopAdvancedRecordBtn);
        mStartPlayMicBtn = findViewById(R.id.startPlayMicBtn);
        mStartPlaySubmixBtn = findViewById(R.id.startPlaySubmixBtn);
        mStopPlayBtn = findViewById(R.id.stopPlayBtn);
        mMicRecordOnBtn = findViewById(R.id.micRecordOnBtn);
        mMicRecordOffBtn = findViewById(R.id.micRecordOffBtn);
        mDoubleRecordPlayBtn = findViewById(R.id.doubleRecordPlayBtn);
    }

    /**
     * Initialize data
     */
    private void initData() {
        checkPermissions();
    }

    /**
     * Setting Button Click Events
     */
    private void setClickListener() {
        mInitButton.setOnClickListener(this);
        audioGameLiveRequestVoice.setOnClickListener(this);
        audioGameLiveVoiceRecording.setOnClickListener(this);
        mGetFeaturesButton.setOnClickListener(this);
        misEarReturnSupportedBtn.setOnClickListener(this);
        mIsAdvancedRecordSupportButton.setOnClickListener(this);
        mInitKaraokeButton.setOnClickListener(this);
        mInitAudioGameLive.setOnClickListener(this);
        mIsKaraokeSupportButton.setOnClickListener(this);
        mEnableKaraokeButton.setOnClickListener(this);
        mDisableKaraokeButton.setOnClickListener(this);
        mDefaultReverberationMode.setOnClickListener(this);
        mKtvReverberationMode.setOnClickListener(this);
        mTheaterReverberationMode.setOnClickListener(this);
        mConcertReverberationMode.setOnClickListener(this);
        mDefaultEqualizerMode.setOnClickListener(this);
        mFullEqualizerMode.setOnClickListener(this);
        mBrightEqualizerMode.setOnClickListener(this);
        mSetVolume.setOnClickListener(this);
        mIsSupportAdvancedRecord.setOnClickListener(this);
        mStartAdvancedRecordBtn.setOnClickListener(this);
        mStopAdvancedRecordBtn.setOnClickListener(this);
        mStartPlayMicBtn.setOnClickListener(this);
        mStartPlaySubmixBtn.setOnClickListener(this);
        mStopPlayBtn.setOnClickListener(this);
        mMicRecordOnBtn.setOnClickListener(this);
        mMicRecordOffBtn.setOnClickListener(this);
        mDoubleRecordPlayBtn.setOnClickListener(this);
    }

    /**
     * Permission check
     */
    private void checkPermissions() {
        if (!hasPermission()) {
            startRequestPermission();
        }
    }

    private boolean hasPermission() {
        for (String permission : PERMISSIONS) {
            int result = ContextCompat.checkSelfPermission(this, permission);
            if (result != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private void startRequestPermission() {
        ActivityCompat.requestPermissions(this, PERMISSIONS, INTENT_FLAG);
    }

    @Override
    public void onClick(View view) {
        if (view == null) {
            return;
        }
        mResultType = "";
        onClickParameter(view);
        onClickReocrder(view);
        onClickAdvancedRecord(view);
        int id = view.getId();
        if (id == R.id.init) {
            initAudioKit();
        } else if (id == R.id.getFeaturesButton) {
            getFeatures();
        } else if (id == R.id.isEarReturnSupportButton) {
            testFeatureSupport(HnAudioClient.ServiceType.HNAUDIO_SERVICE_EARRETURN);
        } else if (id == R.id.initKaraoke) {
            initKaraokeFeature();
        } else if (id == R.id.isKaraokeSupportButton) {
            testKaraokeSupport();
        } else if (id == R.id.disableAdvancedRecording) {
            releaseVoiceRecording();
        } else if (id == R.id.enableKaraokeButton) {
            enableKaraoke(true);
        } else if (id == R.id.disableKaraokeButton) {
            enableKaraoke(false);
        }
    }

    private void onClickAdvancedRecord(View view) {
        int id = view.getId();
        if (id == R.id.isSupportAdvancedRecord) {
            isSupportAdvancedRecord();
        } else if (id == R.id.startAdvancedRecordBtn) { // 开启增强录音
            startAdvancedRecord();
        } else if (id == R.id.stopAdvancedRecordBtn) {
            mStartAdvancedRecordBtn.setEnabled(true);
            Intent intentService = new Intent(this, CaptureService.class);
            stopService(intentService);
            Log.i(TAG, "stop service");
        } else if (id == R.id.startPlayMicBtn) {
            startPlayAdvancedRecord(Parameters.MIC, "recordOut.pcm");
            mInfoTextView.setText("正在播放MIC录音");
        } else if (id == R.id.startPlaySubmixBtn) {
            startPlayAdvancedRecord(Parameters.REMOTE_SUBMIX, "recordSubmixOut.pcm");
            mInfoTextView.setText("正在播放系统音录音");
        } else if (id == R.id.stopPlayBtn) {
            if (mAudioRecordPlayTask == null) {
                mInfoTextView.setText("还没有开始录音");
            } else {
                mAudioRecordPlayTask.setIsPlaying(false);
                mInfoTextView.setText("已停止录音播放");
            }
        } else if (id == R.id.micRecordOnBtn) {
            if (ActivityCompat.checkSelfPermission(EarReturnAndRecordDemoActivity.this,
                Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                mMicRecordOnBtn.setEnabled(false);
                startMicRecord();
                mInfoTextView.setText("正在进行MIC录音");
            } else {
                mInfoTextView.setText("请点击开始录音获取录音权限");
            }
        } else if (id == R.id.micRecordOffBtn) {
            mMicRecordOnBtn.setEnabled(true);
            Intent service = new Intent(this, CaptureService.class);
            stopService(service);
            Log.i(TAG, "stop mic record service");
            mInfoTextView.setText("停止Mic音录制，点击“播放MIC”播放MIC录音");
        } else if (id == R.id.doubleRecordPlayBtn) {
            startPlayAdvancedRecord(Parameters.UNKNOWN, "doubleRecord.pcm");
            mInfoTextView.setText("正在播放录音双开场景的录音");
        }
    }

    private void isSupportAdvancedRecord() {
        if ((mHnAdvancedRecordClient != null) && mIsAdvancedRecordBindSuccess) {
            boolean isSupport = mHnAdvancedRecordClient.isServiceSupported();
            if (isSupport) {
                mInfoTextView.setText("支持增强录音");
            } else {
                mInfoTextView.setText("不支持增强录音");
            }
        }
    }

    private void startMicRecord() {
        Intent intent = new Intent(this, CaptureService.class);
        startService(intent);

    }

    private void startPlayAdvancedRecord(int source, String filename) {
        mAudioRecordPlayTask = new AudioRecordPlayTask(this);
        mAudioRecordPlayTask.setListener(this);
        Parameters parameters =
            new Parameters(source, Parameters.DEFAULT_SAMPLE_RATE, CHANNEL_IN_STEREO, AudioManager.STREAM_MUSIC,
                filename);
        mAudioRecordPlayTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, parameters);
        mAudioRecordPlayTask.setIsPlaying(true);
    }

    private void startAdvancedRecord() {
        if (mIsAdvancedRecordBindSuccess && mIsAdvancedRecordEnable) {
            mInfoTextView.setText("正在录制系统音和MIC音");
            mMediaProjectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
            mStartAdvancedRecordBtn.setEnabled(false);
            if (mMediaProjectionManager == null) {
                return;
            }
            Intent intent = mMediaProjectionManager.createScreenCaptureIntent();
            startActivityForResult(intent, RESULT_CODE);
        } else {
            mInfoTextView.setText("录音失败，请确认是否初始化或者是否打开增强录音");
        }
    }

    private void onClickReocrder(View view) {
        int id = view.getId();
        if (id == R.id.initAudioGameLive) {
            initAudioRecorderLive();
        } else if (id == R.id.enableAdvancedRecord) {
            requestVoiceRecording();
        } else if (id == R.id.isAdvancedRecordSupportBtn) {
            testFeatureSupport(HnAudioClient.ServiceType.HNAUDIO_SERVICE_ADVANCEDRECORD);
        }
    }

    /**
     * Delivering parameters
     *
     * @param view View
     */
    private void onClickParameter(View view) {
        int id = view.getId();
        if (id == R.id.setDefaultReverberationMode) {
            setReverberation(Constant.REVERB_EFFECT_MODE_ORIGINAL);
        } else if (id == R.id.setKtvReverberationMode) {
            setReverberation(Constant.REVERB_EFFECT_MODE_KTV);
        } else if (id == R.id.setTheaterReverberationMode) {
            setReverberation(Constant.REVERB_EFFECT_MODE_THEATRE);
        } else if (id == R.id.setConcertReverberationMode) {
            setReverberation(Constant.REVERB_EFFECT_MODE_CONCERT);
        } else if (id == R.id.settingVolume) {
            String volume = mEditTextSetVolume.getText().toString().trim();
            if (!volume.isEmpty()) {
                try {
                    setVolume(Integer.valueOf(volume));
                } catch (NumberFormatException ex) {
                    Log.e(TAG, "NumberFormatException, ex = NumberFormatException");
                }
            } else {
                setVolume(INIT_VOLUME);
            }
        } else if (id == R.id.setDefaultEqualizer) {
            setEqualizer(Constant.EQUALIZER_MODE_DEFAULT);
        } else if (id == R.id.setFullEqualizer) {
            setEqualizer(Constant.EQUALIZER_MODE_FULL);
        } else if (id == R.id.setBrightEqualizer) {
            setEqualizer(Constant.EQUALIZER_MODE_BRIGHT);
        }
    }

    /**
     * 初始化基础服务
     */
    private void initAudioKit() {
        Log.i(TAG, "initAudioKit");
        if (HnAudioClient.isDeviceSupported(this)) {
            mHnAudioClient = new HnAudioClient(this, this);
            mHnAudioClient.initialize();
        }
    }

    /**
     * 初始化耳返服务
     */
    private void initKaraokeFeature() {
        Log.i(TAG, "initKaraokeFeature");
        if (mHnAudioClient != null) {
            mHnEarReturnClient = mHnAudioClient.createService(HnAudioClient.ServiceType.HNAUDIO_SERVICE_EARRETURN);
        } else {
            mInfoTextView.setText(R.string.isInit);
        }
    }

    /**
     * 初始化增强录音
     */
    private void initAudioRecorderLive() {
        Log.i(TAG, "initAudioRecorderLive");
        if (mHnAudioClient != null) {
            mHnAdvancedRecordClient =
                mHnAudioClient.createService(HnAudioClient.ServiceType.HNAUDIO_SERVICE_ADVANCEDRECORD);
        } else {
            mInfoTextView.setText(R.string.isInit);
        }
    }

    @Override
    public void onResult(int resultType) {
        Log.i(TAG, "resultType = " + resultType);
        mResultType = "";
        setResultType(resultType);
        switch (resultType) {
            case ResultCode.VENDOR_NOT_SUPPORTED:
                mResultType = getResources().getString(R.string.notInstallAudioKitService);
                break;
            case ResultCode.AUDIO_SERVICE_DISCONNECTED:
                mResultType = getResources().getString(R.string.audioKitServiceDisconnect);
                break;
            case ResultCode.AUDIO_SERVICE_DIED:
                mResultType = getResources().getString(R.string.audioKitServiceDied);
                break;
            case ResultCode.EARRETURN_EFFECT_SERVICE_DISCONNECTED:
                mResultType = getResources().getString(R.string.karaokeServiceDisconnect);
                break;
            case ResultCode.EARRETURN_EFFECT_SERVICE_DIE:
                mResultType = getResources().getString(R.string.karaokeServiceDied);
                break;
            default:
                break;
        }
        mResultType = mResultType + resultType;
        mInfoTextView.setText(mResultType);
    }

    private void setResultType(int resultType) {
        switch (resultType) {
            case ResultCode.ADVANCED_RECORD_SUCCESS:
                mResultType = getResources().getString(R.string.initAudioGameLiveSuccess);
                mIsAdvancedRecordBindSuccess = true;
                break;
            case ResultCode.ADVANCED_RECORD_DISCONNECTED:
                mResultType = getResources().getString(R.string.initAudioGameLiveDisconnect);
                break;
            case ResultCode.AUDIO_SERVICE_SUCCESS:
                mIsAudiokitBindSuccess = true;
                mResultType = getResources().getString(R.string.kitServiceSucess);
                break;
            case ResultCode.EARRETURN_EFFECT_SUCCESS:
                mIsEarReturnBindSuccess = true;
                mResultType = getResources().getString(R.string.karaokeServiceSucess);
                break;
            default:
                break;
        }
    }

    private void getFeatures() {
        if (mHnAudioClient != null) {
            setFeaturesText();
        } else {
            mInfoTextView.setText(R.string.isInit);
        }
    }

    private void setFeaturesText() {
        List<Integer> arrayList = mHnAudioClient.getSupportedServices();
        String str = getResources().getString(R.string.features);
        StringBuffer strBuffer = new StringBuffer(str);
        if ((arrayList != null) && (arrayList.size() > 0)) {
            for (Integer array : arrayList) {
                strBuffer.append(getFeatureName(array).orElse("null"));
            }
            mInfoTextView.setText(strBuffer.toString());
        } else if (mIsAudiokitBindSuccess) {
            mInfoTextView.setText(R.string.noFeatures);
        } else {
            Log.i(TAG, "setFeaturesText");
        }
    }

    private Optional<String> getFeatureName(int type) {
        if (type == Constant.HNAUDIO_SERVICE_EARRETURN) {
            return Optional.of(FEATURE_TYPE);
        }
        if (type == Constant.HNAUDIO_SERVICE_ADVANCEDRECORD) {
            return Optional.of(FEATURERECORDER_TYPE);
        }
        return Optional.empty();
    }

    private void testFeatureSupport(HnAudioClient.ServiceType type) {
        boolean isSupport = false;
        if (mHnAudioClient != null) {
            isSupport = mHnAudioClient.isServiceSupported(type);
        } else {
            mInfoTextView.setText(R.string.isInit);
            return;
        }
        if (isSupport) {
            String str = getResources().getString(R.string.support);
            StringBuffer strBuffer = new StringBuffer(str);
            strBuffer.append(getFeatureName(type.getServiceType()).orElse("null"));
            mInfoTextView.setText(strBuffer.toString());
        } else {
            mInfoTextView.setText(R.string.notSupportFeature);
        }
    }

    private void testKaraokeSupport() {
        boolean isSupport = false;
        if ((mHnEarReturnClient != null) && mIsEarReturnBindSuccess) {
            isSupport = mHnEarReturnClient.isServiceSupported();
        } else {
            mInfoTextView.setText(R.string.isInit);
        }
        if (isSupport) {
            String str = getResources().getString(R.string.supportKaraoke);
            mInfoTextView.setText(str);
        }
    }

    private void requestVoiceRecording() {
        boolean isSupport = false;
        if ((mHnAdvancedRecordClient != null) && mIsAdvancedRecordBindSuccess) {
            isSupport = mHnAdvancedRecordClient.enableAdvancedRecord(this);
            if (isSupport) {
                mIsAdvancedRecordEnable = true;
                String str = getResources().getString(R.string.requestVoiceRecording);
                mInfoTextView.setText(str);
            } else {
                mInfoTextView.setText("当前设备不支持该功能");
            }
        } else {
            mInfoTextView.setText(R.string.noRequestVoiceRecording);
        }
    }

    private void releaseVoiceRecording() {
        boolean isSupport = false;
        if ((mHnAdvancedRecordClient != null) && mIsAdvancedRecordBindSuccess) {
            isSupport = mHnAdvancedRecordClient.disableAdvancedRecord(this);
            if (isSupport) {
                mIsAdvancedRecordEnable = false;
                String str = getResources().getString(R.string.releaseVoiceRecording);
                mInfoTextView.setText(str);
            } else {
                mInfoTextView.setText("当前设备不支持该功能");
            }
        } else {
            mInfoTextView.setText(R.string.noReleaseVoiceRecording);
        }
    }

    private void enableKaraoke(boolean isEnable) {
        int enableSuccess;
        if ((mHnEarReturnClient != null) && mIsEarReturnBindSuccess) {
            enableSuccess = mHnEarReturnClient.enableEarReturn(isEnable);
        } else {
            mInfoTextView.setText(R.string.isInit);
            return;
        }
        if (enableSuccess == ResultCode.EARRETURN_DEVICE_NOT_AVAILBLE) {
            mInfoTextView.setText(R.string.notHeadset);
        }
        if (enableSuccess == ResultCode.PLATEFORM_NOT_SUPPORT) {
            mInfoTextView.setText(R.string.notSupportFeature);
        }
        if (enableSuccess == ResultCode.BLUETOOTH_HD_FB_NOT_SUPPORT) {
            mInfoTextView.setText(R.string.notSupportBluetoothFeature);
        }
        if (isEnable && (enableSuccess == 0)) {
            mInfoTextView.setText(R.string.openSuccess);
        }
        setEnableKaraokeView(isEnable, enableSuccess == 0);
    }

    private void setEnableKaraokeView(boolean isEnable, boolean isEnableSuccess) {
        if (!isEnable && isEnableSuccess) {
            mInfoTextView.setText(R.string.closeSuccess);
        }
    }

    private void setReverberation(int value) {
        if ((mHnEarReturnClient != null) && mIsEarReturnBindSuccess) {
            int success =
                mHnEarReturnClient.setParameter(HnEarReturnClient.ParameName.CMD_SET_AUDIO_EFFECT_MODE_BASE, value);
            if (success == 0) {
                String str = getResources().getString(R.string.success);
                mInfoTextView.setText(
                    str + ": " + HnEarReturnClient.ParameName.CMD_SET_AUDIO_EFFECT_MODE_BASE.getParameName() + value);
            }
            if (success == ResultCode.PLATEFORM_NOT_SUPPORT) {
                mInfoTextView.setText(R.string.notSupportFeature);
            }
            if (success == ResultCode.PARAME_VALUE_ERROR) {
                mInfoTextView.setText(R.string.paramValueError);
            }
        } else {
            mInfoTextView.setText(R.string.isInit);
        }
    }

    private void setVolume(int value) {
        if ((mHnEarReturnClient != null) && mIsEarReturnBindSuccess) {
            int success =
                mHnEarReturnClient.setParameter(HnEarReturnClient.ParameName.CMD_SET_VOCAL_VOLUME_BASE, value);
            if (success == 0) {
                String str = getResources().getString(R.string.success);
                mInfoTextView.setText(
                    str + ": " + HnEarReturnClient.ParameName.CMD_SET_VOCAL_VOLUME_BASE.getParameName() + value);
            }
            if (success == ResultCode.PLATEFORM_NOT_SUPPORT) {
                mInfoTextView.setText(R.string.notSupportFeature);
            }
            if (success == ResultCode.PARAME_VALUE_ERROR) {
                mInfoTextView.setText(R.string.paramValueError);
            }
        } else {
            mInfoTextView.setText(R.string.isInit);
        }
    }

    private void setEqualizer(int value) {
        if ((mHnEarReturnClient != null) && mIsEarReturnBindSuccess) {
            int success =
                mHnEarReturnClient.setParameter(HnEarReturnClient.ParameName.CMD_SET_VOCAL_EQUALIZER_MODE, value);
            if (success == 0) {
                String str = getResources().getString(R.string.success);
                mInfoTextView.setText(
                    str + " : " + HnEarReturnClient.ParameName.CMD_SET_VOCAL_EQUALIZER_MODE.getParameName() + value);
            }
            if (success == ResultCode.PLATEFORM_NOT_SUPPORT) {
                mInfoTextView.setText(R.string.notSupportFeature);
            }
            if (success == ResultCode.PARAME_VALUE_ERROR) {
                mInfoTextView.setText(R.string.paramValueError);
            }
        } else {
            mInfoTextView.setText(R.string.isInit);
        }
    }

    @Override
    public void onTaskFinished() {
        mInfoTextView.setText("录音已播完");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i(TAG, "activity result, resultCode = " + resultCode);
        if (resultCode == RESULT_OK) {
            Intent intent = new Intent(this, CaptureService.class);
            intent.putExtra("resultCode", resultCode);
            intent.putExtra("data", data);
            startService(intent);
        } else {
            Log.i(TAG, "resulecode is " + requestCode + ", record stop");
            mInfoTextView.setText("启动录音失败");
            mStartAdvancedRecordBtn.setEnabled(true);
        }
    }
}