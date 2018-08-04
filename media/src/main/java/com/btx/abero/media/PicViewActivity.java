package com.btx.abero.media;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SoundEffectConstants;
import android.view.View;
import android.widget.ImageView;


import com.btx.abero.media.image.PinchImageView;

import java.io.File;


public class PicViewActivity extends Activity {

    private static final String TAG = "PicViewActivity";
    private static final String EXTRA_PATH = "extra_path";
    private static final String EXTRA_RECT = "extra_rect";

    private static final long ANIM_TIME = 200;

    private RectF mThumbMaskRect;
    private Matrix mThumbImageMatrix;

    private ObjectAnimator mBackgroundAnimator;

    private View mBackground;
    private PinchImageView mImageView;

    public static void startPicViewActivity(AppCompatActivity activity, String path, Rect rect) {
        Intent intent = new Intent(activity, PicViewActivity.class);
        intent.putExtra(EXTRA_PATH, path);
        intent.putExtra(EXTRA_RECT, rect);
        activity.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //获取参数
        String path = getIntent().getStringExtra(EXTRA_PATH);
        final Rect rect = getIntent().getParcelableExtra(EXTRA_RECT);

        setContentView(R.layout.activity_pic_view);
        mImageView = (PinchImageView) findViewById(R.id.pic);
        mBackground = findViewById(R.id.background);


        mImageView.setImageURI(Uri.fromFile(new File(path)));

        mImageView.post(new Runnable() {
            @Override
            public void run() {
                mImageView.setAlpha(1f);

                //背景动画
                mBackgroundAnimator = ObjectAnimator.ofFloat(mBackground, "alpha", 0f, 1f);
                mBackgroundAnimator.setDuration(ANIM_TIME);
                mBackgroundAnimator.start();

                //status bar高度修正
                Rect tempRect = new Rect();
                mImageView.getGlobalVisibleRect(tempRect);

                Log.i(TAG, "run: top=" + tempRect.top + " bot=" + tempRect.bottom + " left=" + tempRect.left + " right=" + tempRect.right);
                Log.i(TAG, "run: top=" + rect.top + " bot=" + rect.bottom + " left=" + rect.left + " right=" + rect.right);

                rect.top = rect.top - tempRect.top;
                rect.bottom = rect.bottom - tempRect.top;

                Log.i(TAG, "run: top=" + rect.top + " bot=" + rect.bottom + " left=" + rect.left + " right=" + rect.right);

                //mask动画
                mThumbMaskRect = new RectF(rect);
                Log.i(TAG, "run: top=" + mThumbMaskRect.top + " bot=" + mThumbMaskRect.bottom + " left=" + mThumbMaskRect.left + " right=" + mThumbMaskRect.right);
                RectF bigMaskRect = new RectF(0, 0, mImageView.getWidth(), mImageView.getHeight());
                mImageView.zoomMaskTo(mThumbMaskRect, 0);
                mImageView.zoomMaskTo(bigMaskRect, ANIM_TIME);


                int width = 100;
                int height = 100;
                //图片放大动画
                RectF thumbImageMatrixRect = new RectF();
                PinchImageView.MathUtils.calculateScaledRectInContainer(new RectF(rect), width, height, ImageView.ScaleType.MATRIX, thumbImageMatrixRect);
                RectF bigImageMatrixRect = new RectF();
                PinchImageView.MathUtils.calculateScaledRectInContainer(new RectF(0, 0, mImageView.getWidth(), mImageView.getHeight()), width, height, ImageView.ScaleType.FIT_CENTER, bigImageMatrixRect);
                mThumbImageMatrix = new Matrix();
                PinchImageView.MathUtils.calculateRectTranslateMatrix(bigImageMatrixRect, thumbImageMatrixRect, mThumbImageMatrix);
                mImageView.outerMatrixTo(mThumbImageMatrix, 0);
                mImageView.outerMatrixTo(new Matrix(), ANIM_TIME);
            }
        });
        mImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mImageView.playSoundEffect(SoundEffectConstants.CLICK);
                finish();
            }
        });
    }

    @Override
    public void finish() {
        if ((mBackgroundAnimator != null && mBackgroundAnimator.isRunning())) {
            return;
        }

        //背景动画
        mBackgroundAnimator = ObjectAnimator.ofFloat(mBackground, "alpha", mBackground.getAlpha(), 0f);
        mBackgroundAnimator.setDuration(ANIM_TIME);
        mBackgroundAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                PicViewActivity.super.finish();
                overridePendingTransition(0, 0);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        mBackgroundAnimator.start();

        //mask动画
        mImageView.zoomMaskTo(mThumbMaskRect, ANIM_TIME);

        //图片缩小动画
        mImageView.outerMatrixTo(mThumbImageMatrix, ANIM_TIME);
    }
}