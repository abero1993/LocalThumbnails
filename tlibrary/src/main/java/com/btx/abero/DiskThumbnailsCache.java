package com.btx.abero;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;

import com.jakewharton.disklrucache.DiskLruCache;
import com.jakewharton.disklrucache.Util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static android.os.Environment.isExternalStorageRemovable;

/**
 * Created by abero on 2018/7/16.
 */

public class DiskThumbnailsCache implements ThumbnailsCache {

    private static final String TAG = "DiskThumbnailsCache";

    private final String FILE_NAME = "ThumbnailCache";
    private DiskLruCache mDiskLruCache;
    private Context mContext;

    public DiskThumbnailsCache(Context context, int maxSize) {

        mContext = context;

        try {
            mDiskLruCache = DiskLruCache.open(getDiskCacheDir(context, FILE_NAME), 1, 1, maxSize);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private File getDiskCacheDir(Context context, String uniqueName) {
        final String cachePath = Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) || !isExternalStorageRemovable()
                ? context.getExternalCacheDir().getPath()
                : context.getCacheDir().getPath();
        return new File(cachePath + File.separator + uniqueName);
    }


    @Override
    public void putBitmap(String key, Bitmap bitmap) {
        if (bitmap != null) {
            try {
                DiskLruCache.Editor editor = mDiskLruCache.edit(key);
                if (editor != null) {
                    OutputStream out = editor.newOutputStream(0);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                    out.flush();
                    out.close();
                    editor.commit();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    public Bitmap getBitmap(String key) {

        try {
            DiskLruCache.Snapshot snapshot = mDiskLruCache.get(key);
            if (snapshot != null) {
                InputStream in = snapshot.getInputStream(0);
                return BitmapFactory.decodeStream(in);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;

    }


}
