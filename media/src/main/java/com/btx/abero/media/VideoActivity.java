/*
 * Copyright (C) 2015 Bilibili
 * Copyright (C) 2015 Zhang Rui <bbcallen@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.btx.abero.media;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;


import com.btx.abero.media.ijkplayer.IjkVideoView;

import java.lang.ref.WeakReference;
import java.util.Locale;

import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

public class VideoActivity extends AppCompatActivity implements IMediaPlayer.OnPreparedListener {
    private static final String TAG = "VideoActivity";
    private static final int SHOW = 1;
    private static final int HIDE = 2;
    private static final int UPDATE = 3;

    private String mVideoPath;
    private Uri mVideoUri;

    private IjkVideoView mVideoView;
    private View mControlRoot;
    private ImageView mPlayPauseImage;
    private ImageView mForwardImage;
    private ImageView mBackwardImage;
    private TextView mSpeedText;
    private TextView mCurrentTimeText;
    private TextView mDurationText;
    private SeekBar mSeekBar;

    private boolean isShow = false;
    private VideoHandler mHandler;

    private String[] mSpeedStrs;
    private int mSpeedIndex = 1;

    public static Intent newIntent(Context context, String videoPath, String videoTitle) {
        Intent intent = new Intent(context, VideoActivity.class);
        intent.putExtra("videoPath", videoPath);
        intent.putExtra("videoTitle", videoTitle);
        return intent;
    }

    public static void intentTo(Context context, String videoPath, String videoTitle) {
        context.startActivity(newIntent(context, videoPath, videoTitle));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.video_play);

        // handle arguments
        mVideoPath = getIntent().getStringExtra("videoPath");

        Intent intent = getIntent();
        String intentAction = intent.getAction();
        if (!TextUtils.isEmpty(intentAction)) {
            if (intentAction.equals(Intent.ACTION_VIEW)) {
                mVideoPath = intent.getDataString();
            } else if (intentAction.equals(Intent.ACTION_SEND)) {
                mVideoUri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                    String scheme = mVideoUri.getScheme();
                    if (TextUtils.isEmpty(scheme)) {
                        Log.e(TAG, "Null unknown scheme\n");
                        finish();
                        return;
                    }
                    if (scheme.equals(ContentResolver.SCHEME_ANDROID_RESOURCE)) {
                        mVideoPath = mVideoUri.getPath();
                    } else if (scheme.equals(ContentResolver.SCHEME_CONTENT)) {
                        Log.e(TAG, "Can not resolve content below Android-ICS\n");
                        finish();
                        return;
                    } else {
                        Log.e(TAG, "Unknown scheme " + scheme + "\n");
                        finish();
                        return;
                    }
                }
            }
        }


        // init player
        IjkMediaPlayer.loadLibrariesOnce(null);
        IjkMediaPlayer.native_profileBegin("libijkplayer.so");

        mVideoView = (IjkVideoView) findViewById(R.id.video_view);
        mVideoView.setOnPreparedListener(this);

        mHandler = new VideoHandler(this);

        initControlView();

        // prefer mVideoPath
        if (mVideoPath != null)
            mVideoView.setVideoPath(mVideoPath);
        else if (mVideoUri != null)
            mVideoView.setVideoURI(mVideoUri);
        else {
            Log.e(TAG, "Null Data Source\n");
            finish();
            return;
        }
        mVideoView.start();
        mVideoView.setSpeed(2);

    }

    private void initControlView() {
        mControlRoot = findViewById(R.id.control_root);
        mPlayPauseImage = findViewById(R.id.control_play);
        mForwardImage = findViewById(R.id.control_front);
        mBackwardImage = findViewById(R.id.control_later);
        mSpeedText = findViewById(R.id.control_speed);
        mCurrentTimeText = findViewById(R.id.control_current_time);
        mDurationText = findViewById(R.id.control_length);
        mSeekBar = findViewById(R.id.control_seekbar);

        mPlayPauseImage.setOnClickListener(mOnClickListener);
        mForwardImage.setOnClickListener(mOnClickListener);
        mBackwardImage.setOnClickListener(mOnClickListener);
        mSpeedText.setOnClickListener(mOnClickListener);

        mSpeedStrs = getResources().getStringArray(R.array.video_speed_values);

    }


    private static class VideoHandler extends Handler {
        private WeakReference<VideoActivity> mWeakActivity;

        public VideoHandler(VideoActivity videoActivity) {
            mWeakActivity = new WeakReference<VideoActivity>(videoActivity);
        }

        @Override
        public void handleMessage(Message msg) {

            VideoActivity activity = mWeakActivity.get();
            if (activity != null && !activity.isFinishing()) {
                switch (msg.what) {
                    case SHOW:

                        break;
                    case HIDE:
                        activity.hideControlView();
                        break;
                    case UPDATE:
                        activity.updateCurrentTime();
                        if (activity.isShow)
                            sendEmptyMessageDelayed(UPDATE, 1000);
                        break;
                    default:
                        break;
                }
            } else {
                super.handleMessage(msg);
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (MotionEvent.ACTION_DOWN == event.getAction()) {
            if (isShow)
                hideControlView();
            else
                showControlView();

        }
        return super.onTouchEvent(event);
    }

    private void showSpeedDialog() {
        new AlertDialog.Builder(this)
                .setSingleChoiceItems(mSpeedStrs, mSpeedIndex, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        mSpeedIndex=i;


                        if(i-1>0)
                        mVideoView.setSpeed((float) Math.pow(2,i-1));
                        else if(i-1<0)
                            mVideoView.setSpeed(0.5f/**/);

                    }
                }).create()
                .show();

    }

    private void showControlView() {
        if (!isShow) {
            mControlRoot.setVisibility(View.VISIBLE);
            isShow = true;

            updateCurrentTime();

            Message msg = mHandler.obtainMessage();
            msg.what = HIDE;
            mHandler.sendMessageDelayed(msg, 5000);

            sendUpdateMsg();
        }
    }

    private void hideControlView() {
        isShow = false;
        mControlRoot.setVisibility(View.INVISIBLE);
        mHandler.removeMessages(HIDE);
    }

    private void updateCurrentTime() {
        if (mCurrentTimeText != null)
            mCurrentTimeText.setText(buildTimeMilli(mVideoView.getCurrentPosition()));
    }


    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            int id = view.getId();
            if (R.id.control_play == id) {

            } else if (R.id.control_front == id) {

            } else if (R.id.control_later == id) {

            } else if (R.id.control_speed == id) {
                showSpeedDialog();
            }


        }
    };

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onStop() {
        super.onStop();

        mVideoView.stopPlayback();
        mVideoView.release(true);

        IjkMediaPlayer.native_profileEnd();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public void onPrepared(IMediaPlayer iMediaPlayer) {

        sendUpdateMsg();
        mDurationText.setText(buildTimeMilli(iMediaPlayer.getDuration()));
        showControlView();
    }

    private void sendUpdateMsg() {
        Message message = mHandler.obtainMessage();
        message.what = UPDATE;
        mHandler.sendMessage(message);
    }

    private String buildTimeMilli(long duration) {
        long total_seconds = duration / 1000;
        long hours = total_seconds / 3600;
        long minutes = (total_seconds % 3600) / 60;
        long seconds = total_seconds % 60;
        if (duration <= 0) {
            return "00:00";
        }
        if (hours >= 100) {
            return String.format(Locale.US, "%d:%02d:%02d", hours, minutes, seconds);
        } else if (hours > 0) {
            return String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds);
        } else {
            return String.format(Locale.US, "%02d:%02d", minutes, seconds);
        }
    }
}
