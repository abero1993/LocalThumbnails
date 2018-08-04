package com.btx.abero;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;

/**
 * Created by Frank on 2016/10/13.
 */
public class ThumbProc {

    private static final String TAG = "ThumbProc";

    private static final int video_type = 1;
    private static final int audio_type = 2;
    private static final int image_type = 3;
    private final int W = 100;
    private final int H = 70;

    Bitmap mDefaultAudioBitmap = null;
    Bitmap mDefaultVideoBitmap = null;
    Bitmap mDefaultImageBitmap = null;

    private Context mContext;

    public ThumbProc(Context context) {
        mContext = context;
    }

    public Bitmap getDefaultAudioThumbnail(int width, int height) {
        Resources res = mContext.getResources();
        if (mDefaultAudioBitmap == null) {
            mDefaultAudioBitmap = BitmapFactory.decodeResource(res, R.drawable.audio_thumb);
            mDefaultAudioBitmap = ThumbnailUtils.extractThumbnail(mDefaultAudioBitmap, width, height,
                    ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
        }
        return mDefaultAudioBitmap;
    }

    public Bitmap getDefaultVideoThumbnail(int width, int height) {
        Log.i(TAG, "getDefaultVideoThumbnail: ");
        Resources res = mContext.getResources();
        if (null==mDefaultVideoBitmap ) {
            mDefaultVideoBitmap = BitmapFactory.decodeResource(res, R.drawable.video_thumb);
            mDefaultVideoBitmap = ThumbnailUtils.extractThumbnail(mDefaultVideoBitmap, width, height,
                    ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
        }
        return mDefaultVideoBitmap;
    }

    public Bitmap getDefaultImageThumbnail(int width, int height) {
        Resources res = mContext.getResources();
        if (mDefaultImageBitmap == null) {
            mDefaultImageBitmap = BitmapFactory.decodeResource(res, R.drawable.image_thumb);
            mDefaultImageBitmap = ThumbnailUtils.extractThumbnail(mDefaultImageBitmap, width, height,
                    ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
        }
        return mDefaultImageBitmap;
    }


    public Bitmap getImageThumbnail(String filename) {
        if (!TextUtils.isEmpty(filename)) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true; // 设置了此属性一定要记得将值设置为false
            Bitmap bitmap = BitmapFactory.decodeFile(filename, options);
            options.inJustDecodeBounds = false;
            int be = options.outHeight / 4;
            if (be <= 0) {
                be = 10;
            }
            options.inSampleSize = 4;
            bitmap = BitmapFactory.decodeFile(filename, options);
            return bitmap;
        }
        return null;
    }

    //image thumb
    public Bitmap getImageThumbnail(String imagePath, int width, int height) {
        if (TextUtils.isEmpty(imagePath))
            return null;
        Bitmap bitmap = null;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        // 获取这个图片的宽和高，注意此处的bitmap为null
        bitmap = BitmapFactory.decodeFile(imagePath, options);
        options.inJustDecodeBounds = false; // 设为 false
        // 计算缩放比
        int h = options.outHeight;
        int w = options.outWidth;
        int beWidth = w / width;
        int beHeight = h / height;
        int be = 1;
        if (beWidth < beHeight) {
            be = beWidth;
        } else {
            be = beHeight;
        }
        if (be <= 0) {
            be = 1;
        }
        options.inSampleSize = be;
        // 重新读入图片，读取缩放后的bitmap，注意这次要把options.inJustDecodeBounds 设为 false
        bitmap = BitmapFactory.decodeFile(imagePath, options);
        // 利用ThumbnailUtils来创建缩略图，这里要指定要缩放哪个Bitmap对象
        bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height,
                ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
        return bitmap;
    }

    ////////////////////////////////////////////////
    //video thumb
    public Bitmap getVideoThumbnail(String videoPath, int width, int height,
                                    int kind) {
        if (TextUtils.isEmpty(videoPath))
            return null;
        Bitmap bitmap = null;
        // 获取视频的缩略图
        bitmap = ThumbnailUtils.createVideoThumbnail(videoPath, kind);
        bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height,
                ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
        Log.i(TAG, "getVideoThumbnail: done");
        return bitmap;
    }

    public Bitmap getFileThumbnail(String filePath) {

        Bitmap bitmap = null;
        if (TextUtils.isEmpty(filePath))
            throw new NullPointerException("file name can not be null for thumbnail!");

        int type;
        if (filePath.contains("mp4"))
            type = video_type;
        else if (filePath.contains("wav"))
            type = audio_type;
        else if (filePath.contains("jpg"))
            type = image_type;
        else
            throw new IllegalArgumentException("can not file file type!!!");

        if (video_type == type) {
            bitmap = getVideoThumbnail(filePath, W, H, MediaStore.Video.Thumbnails.MINI_KIND);
            if (null == bitmap)
                bitmap = getDefaultVideoThumbnail(W, H);
        } else if (image_type == type) {
            bitmap = getImageThumbnail(filePath, W, H);
            if (null == bitmap)
                bitmap = getDefaultImageThumbnail(W, H);
        } else {
            bitmap = getDefaultAudioThumbnail(W, H);
        }


        return bitmap;

    }
}
