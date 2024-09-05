
/*
 * Copyright (c) Honor Device Co., Ltd. 2022-2022. All rights reserved.
 */

package com.hihonor.audiokitdev;

import static android.media.AudioFormat.CHANNEL_IN_STEREO;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.AudioPlaybackCaptureConfiguration;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

/**
 * Capture service
 *
 * @author d00013419
 * @since 2022-04-25
 */
public class CaptureService extends Service {
    private static final String NOTIFICATION_CHANNEL_ID = "com.hihonor.audiokitdev.MediaService";

    private static final String NOTIFICATION_CHANNEL_NAME = "com.hihonor.audiokitdev.channel_name";

    private static final String NOTIFICATION_CHANNEL_DESC = "com.hihonor.audiokitdev.channel_desc";

    private static final String TAG = "CaptureService";

    private AudioRecordTask mRecordSubmixTask = null;

    private AudioRecordTask mRecordTask = null;

    private NotificationManager notificationManager = null;

    @Override
    public void onCreate() {
        super.onCreate();
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    }

    private void startNotification(boolean isEnableRemote) {
        // Call Start foreground with notification
        Intent notificationIntent = new Intent(this, CaptureService.class);
        PendingIntent pendingIntent =
            PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);
        String context;
        if (isEnableRemote) {
            context = "当前正在录制MIC音和系统音";
        } else {
            context = "当前正在录制MIC音";
        }
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher_foreground))
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("开启录音服务")
            .setContentText(context)
            .setContentIntent(pendingIntent);
        Notification notification = notificationBuilder.build();
        NotificationChannel notificationChannel = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, NOTIFICATION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.setDescription(NOTIFICATION_CHANNEL_DESC);
            notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            notificationManager.createNotificationChannel(notificationChannel);
            if (isEnableRemote) {
                // 在增强录音打开的情况下，用这个
                startForeground(1, notification); // 必须使用此方法显示通知，不能使用notificationManager.notify，否则还是会报上面的错误
            } else {

                // 普通录音的系统服务必须用这个方法打开,
                notificationManager.notify(1, notification);
            }

        }

    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            return START_NOT_STICKY;
        }
        int resultCode = intent.getIntExtra("resultCode", 0);
        if (resultCode == 0) {
            startNotification(false);
            Parameters parameters = new Parameters(Parameters.MIC, Parameters.DEFAULT_SAMPLE_RATE, CHANNEL_IN_STEREO,
                AudioManager.STREAM_MUSIC);
            mRecordTask = new AudioRecordTask(this, parameters);
            mRecordTask.setIsRecording(true);
            mRecordTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            startNotification(true);
            Intent data = intent.getParcelableExtra("data");
            MediaProjectionManager mMediaProjectionManager =
                (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
            MediaProjection mediaProjection = mMediaProjectionManager.getMediaProjection(resultCode, data);
            AudioPlaybackCaptureConfiguration.Builder builder =
                new AudioPlaybackCaptureConfiguration.Builder(mediaProjection);
            builder.addMatchingUsage(AudioAttributes.USAGE_MEDIA);
            builder.addMatchingUsage(AudioAttributes.USAGE_GAME);
            builder.addMatchingUsage(AudioAttributes.USAGE_UNKNOWN);
            Parameters parameters = new Parameters(Parameters.MIC, Parameters.DEFAULT_SAMPLE_RATE, CHANNEL_IN_STEREO,
                AudioManager.STREAM_MUSIC);
            parameters.setSavedFileName("recordOut.pcm");
            mRecordTask = new AudioRecordTask(this, parameters);
            Parameters parametersSubmix = new Parameters(Parameters.REMOTE_SUBMIX, Parameters.DEFAULT_SAMPLE_RATE,
                CHANNEL_IN_STEREO, AudioManager.STREAM_MUSIC);
            parametersSubmix.setSavedFileName("recordSubmixOut.pcm");
            mRecordSubmixTask = new AudioRecordTask(this, parametersSubmix);
            AudioPlaybackCaptureConfiguration configuration = builder.build();
            mRecordSubmixTask.setConfiguration(configuration);
            mRecordTask.setIsRecording(true);
            mRecordTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            mRecordSubmixTask.setIsRecording(true);
            mRecordSubmixTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "service on destroy");
        if (notificationManager != null) {
            notificationManager.cancel(1);
        }
        if (mRecordTask != null) {
            mRecordTask.setIsRecording(false);
            if (mRecordTask.isRecording()) {
                mRecordTask.cancel(true);
            }
        }
        if (mRecordSubmixTask != null) {
            mRecordSubmixTask.setIsRecording(false);
            if (mRecordSubmixTask.isRecording()) {
                mRecordSubmixTask.cancel(true);
            }
        }
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new Binder();
    }
}
