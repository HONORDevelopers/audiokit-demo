/*
 * Copyright (c) Honor Device Co., Ltd. 2026-2026. All rights reserved.
 */

package com.hihonor.audiokitdev.activity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.hihonor.android.magicx.media.audio.config.ResultCode;
import com.hihonor.android.magicx.media.audio.interfaces.AudioChannelFormatListener;
import com.hihonor.android.magicx.media.audio.interfaces.AudioFormatExtend;
import com.hihonor.android.magicx.media.audio.interfaces.HnAudioClient;
import com.hihonor.android.magicx.media.audio.interfaces.HnAudioPlayClient;
import com.hihonor.android.magicx.media.audio.interfaces.IAudioServiceCallback;
import com.hihonor.audiokitdev.R;

import java.util.List;

/**
 * 三维环绕声格式查询Demo
 * 演示通过HnAudioPlayClient查询车机外接设备支持的三维环绕声格式，
 * 并注册监听格式变化回调。
 *
 * @since 2026-07-15
 */
public class MultiChannelAudioDemoActivity extends AppCompatActivity
    implements View.OnClickListener, IAudioServiceCallback {
    private static final String TAG = "MultiChannelAudioDemo";

    private Button mInitSdkButton;

    private Button mIsMultiChannelSupportedButton;

    private Button mQueryFormatButton;

    private Button mRegisterListenerButton;

    private Button mUnregisterListenerButton;

    private Button mDestroyButton;

    private TextView mStatusTextView;

    private TextView mResultTextView;

    private TextView mListenerTextView;

    private HnAudioClient mHnAudioClient;

    private HnAudioPlayClient mHnAudioPlayClient;

    private boolean mIsAudioPlayBindSuccess = false;

    private final Handler mMainHandler = new Handler(Looper.getMainLooper());

    private final AudioChannelFormatListener mAudioChannelFormatListener =
        new AudioChannelFormatListener() {
            @Override
            public void onSupportedAudioChannelFormatChanged(List<AudioFormatExtend> audioFormats) {
                Log.i(TAG, "onSupportedAudioChannelFormatChanged: " + audioFormats);
                String displayText = formatAudioFormats(audioFormats);
                mMainHandler.post(() -> mListenerTextView.setText(displayText));
            }
        };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multi_channel_audio_demo);
        setTitle("三维环绕声格式查询Demo");
        initView();
    }

    private void initView() {
        mInitSdkButton = findViewById(R.id.initSdkButton);
        mIsMultiChannelSupportedButton = findViewById(R.id.isMultiChannelSupportedButton);
        mQueryFormatButton = findViewById(R.id.queryFormatButton);
        mRegisterListenerButton = findViewById(R.id.registerListenerButton);
        mUnregisterListenerButton = findViewById(R.id.unregisterListenerButton);
        mDestroyButton = findViewById(R.id.destroyButton);
        mStatusTextView = findViewById(R.id.statusTextView);
        mResultTextView = findViewById(R.id.resultTextView);
        mListenerTextView = findViewById(R.id.listenerTextView);

        mInitSdkButton.setOnClickListener(this);
        mIsMultiChannelSupportedButton.setOnClickListener(this);
        mQueryFormatButton.setOnClickListener(this);
        mRegisterListenerButton.setOnClickListener(this);
        mUnregisterListenerButton.setOnClickListener(this);
        mDestroyButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if (view == null) {
            Log.e(TAG, "view is null");
            return;
        }
        int id = view.getId();
        if (id == R.id.initSdkButton) {
            initAudioKit();
        } else if (id == R.id.isMultiChannelSupportedButton) {
            testMultiChannelSupport();
        } else if (id == R.id.queryFormatButton) {
            querySupportedAudioChannelFormat();
        } else if (id == R.id.registerListenerButton) {
            registerFormatListener();
        } else if (id == R.id.unregisterListenerButton) {
            unregisterFormatListener();
        } else if (id == R.id.destroyButton) {
            destroyResources();
        }
    }

    /**
     * 初始化SDK，绑定基础服务
     */
    private void initAudioKit() {
        Log.i(TAG, "initAudioKit");
        if (HnAudioClient.isDeviceSupported(this)) {
            mHnAudioClient = new HnAudioClient(this, this);
            mHnAudioClient.initialize();
        } else {
            mStatusTextView.setText("当前设备不支持AudioKit服务");
        }
    }

    @Override
    public void onResult(int result) {
        Log.i(TAG, "onResult, result = " + result);
        String statusText;
        switch (result) {
            case ResultCode.VENDOR_NOT_SUPPORTED:
                statusText = "未安装AudioKit服务";
                break;
            case ResultCode.AUDIO_SERVICE_DISCONNECTED:
                statusText = "基础服务已断开";
                break;
            case ResultCode.AUDIO_SERVICE_DIED:
                statusText = "基础服务已死亡";
                break;
            case ResultCode.AUDIO_SERVICE_SUCCESS:
                statusText = "基础服务连接成功";
                checkAndCreatePlayClient();
                break;
            case ResultCode.AUDIO_PLAY_SERVICE_SUCCESS:
                mIsAudioPlayBindSuccess = true;
                statusText = "音频播放服务连接成功，可进行查询操作";
                break;
            case ResultCode.AUDIO_PLAY_SERVICE_DISCONNECTED:
                mIsAudioPlayBindSuccess = false;
                statusText = "音频播放服务已断开";
                break;
            case ResultCode.AUDIO_PLAY_SERVICE_DIED:
                mIsAudioPlayBindSuccess = false;
                statusText = "音频播放服务已死亡";
                break;
            default:
                statusText = "结果码: " + result;
                break;
        }
        mStatusTextView.setText(statusText);
    }

    /**
     * 检查是否支持三维环绕声查询功能，若支持则创建HnAudioPlayClient
     */
    private void checkAndCreatePlayClient() {
        if (mHnAudioClient == null) {
            mStatusTextView.setText("请先初始化SDK");
            return;
        }
        List<Integer> servicesList = mHnAudioClient.getSupportedServices();
        if (servicesList == null || servicesList.isEmpty()) {
            mStatusTextView.setText("当前设备无任何支持的功能");
            return;
        }
        for (Integer service : servicesList) {
            if (service == HnAudioClient.ServiceType.HNAUDIO_SERVICE_MULTICHANNELPLAY.getServiceType()) {
                createPlayClient();
                return;
            }
        }
        mStatusTextView.setText("当前设备不支持三维环绕声格式查询功能");
    }

    private void createPlayClient() {
        Log.i(TAG, "createPlayClient");
        if (mHnAudioClient != null) {
            mHnAudioPlayClient =
                mHnAudioClient.createService(HnAudioClient.ServiceType.HNAUDIO_SERVICE_MULTICHANNELPLAY);
            if (mHnAudioPlayClient != null) {
                mStatusTextView.setText("已创建播放客户端，等待服务连接...");
            }
        }
    }

    /**
     * 查询当前设备是否支持三维环绕声格式查询
     */
    private void testMultiChannelSupport() {
        if (mHnAudioClient == null) {
            mStatusTextView.setText("请先初始化SDK");
            return;
        }
        boolean isSupport =
            mHnAudioClient.isServiceSupported(HnAudioClient.ServiceType.HNAUDIO_SERVICE_MULTICHANNELPLAY);
        if (isSupport) {
            mStatusTextView.setText("当前设备支持三维环绕声格式查询");
        } else {
            mStatusTextView.setText("当前设备不支持三维环绕声格式查询");
        }
    }

    /**
     * 查询支持的三维环绕声格式列表
     */
    private void querySupportedAudioChannelFormat() {
        if (!mIsAudioPlayBindSuccess || mHnAudioPlayClient == null) {
            mResultTextView.setText("请先初始化SDK并等待服务连接");
            return;
        }
        Log.i(TAG, "querySupportedAudioChannelFormat");
        List<AudioFormatExtend> formatList = mHnAudioPlayClient.getSupportedAudioChannelFormat();
        String displayText = formatAudioFormats(formatList);
        mResultTextView.setText(displayText);
    }

    /**
     * 注册三维环绕声格式变化监听
     */
    private void registerFormatListener() {
        if (!mIsAudioPlayBindSuccess || mHnAudioPlayClient == null) {
            mListenerTextView.setText("请先初始化SDK并等待服务连接");
            return;
        }
        Log.i(TAG, "registerFormatListener");
        mHnAudioPlayClient.registerAudioChannelFormatListener(mAudioChannelFormatListener);
        mListenerTextView.setText("已注册格式变化监听，等待回调...");
    }

    /**
     * 取消注册三维环绕声格式变化监听
     */
    private void unregisterFormatListener() {
        if (mHnAudioPlayClient == null) {
            mListenerTextView.setText("播放客户端为空");
            return;
        }
        Log.i(TAG, "unregisterFormatListener");
        mHnAudioPlayClient.unregisterAudioChannelFormatListener(mAudioChannelFormatListener);
        mListenerTextView.setText("已取消注册格式变化监听");
    }

    /**
     * 销毁释放资源
     */
    private void destroyResources() {
        Log.i(TAG, "destroyResources");
        if (mHnAudioPlayClient != null) {
            mHnAudioPlayClient.unregisterAudioChannelFormatListener(mAudioChannelFormatListener);
            mHnAudioPlayClient.destroy();
            mHnAudioPlayClient = null;
            mIsAudioPlayBindSuccess = false;
        }
        if (mHnAudioClient != null) {
            mHnAudioClient.destroy();
            mHnAudioClient = null;
        }
        mStatusTextView.setText("资源已释放");
        mResultTextView.setText("");
        mListenerTextView.setText("");
    }

    /**
     * 将AudioFormatExtend列表格式化为可读字符串
     */
    private String formatAudioFormats(List<AudioFormatExtend> formatList) {
        if (formatList == null) {
            return "查询结果为空(null)";
        }
        if (formatList.isEmpty()) {
            return "当前无支持的三维环绕声格式";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("支持的三维环绕声格式共").append(formatList.size()).append("种:\n");
        for (int i = 0; i < formatList.size(); i++) {
            AudioFormatExtend format = formatList.get(i);
            sb.append("[").append(i + 1).append("] ");
            sb.append("声道掩码: ").append(format.getChannelMask());
            List<AudioFormatExtend.AudioFormatEncoding> encodings = format.getEncodings();
            if (encodings != null && !encodings.isEmpty()) {
                for (AudioFormatExtend.AudioFormatEncoding encoding : encodings) {
                    sb.append("\n    编码格式: ").append(encoding.getEncoding());
                    List<Integer> sampleRates = encoding.getSampleRates();
                    if (sampleRates != null && !sampleRates.isEmpty()) {
                        sb.append(", 采样率: ").append(sampleRates);
                    }
                }
            }
            if (i < formatList.size() - 1) {
                sb.append("\n");
            }
        }
        return sb.toString();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy");
        if (mHnAudioPlayClient != null) {
            mHnAudioPlayClient.unregisterAudioChannelFormatListener(mAudioChannelFormatListener);
            mHnAudioPlayClient.destroy();
        }
        if (mHnAudioClient != null) {
            mHnAudioClient.destroy();
        }
    }
}
