package com.btx.abero.media;

/**
 * Created by abero on 2018/4/28.
 */

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.MediaController;
import android.widget.VideoView;

public class AudioPlayActivity extends AppCompatActivity {

    VideoView mVideoView;

    public static void startAudioPlayActivity(AppCompatActivity activity,String path)
    {
        Intent intent=new Intent(activity,AudioPlayActivity.class);
        intent.putExtra("path",path);
        activity.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // disable sleep
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.audio_play);
        mVideoView = (VideoView) findViewById(R.id.video_view);
        String path = getIntent().getExtras().getString("path");
        View audioIcon = findViewById(R.id.audio_icon);
        audioIcon.setVisibility(path.endsWith(".wav") ? View.VISIBLE : View.GONE);
        startPlay(path);
    }


    private void startPlay(String path) {
        AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        if (am.isWiredHeadsetOn() || am.isBluetoothA2dpOn() || am.isBluetoothScoOn()) {
            Log.i("video playback", "wired or bluetooth headset on");
        } else {
            Log.i("video playback", "turn speaker on");
            am.setSpeakerphoneOn(true);
        }

        mVideoView.setVideoPath(path);
        mVideoView.setMediaController(new MediaController(this));
        mVideoView.requestFocus();
        mVideoView.setKeepScreenOn(true);
        mVideoView.start();
    }

    protected void onDestroy() {
        AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        am.setSpeakerphoneOn(false);
        mVideoView.stopPlayback();
        super.onDestroy();
    }
}
