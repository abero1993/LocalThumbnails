package com.btx.abero;

import android.graphics.Bitmap;
import android.util.Log;
import android.util.LruCache;

/**
 * Created by abero on 2018/7/16.
 */

public class MemoryThumbnailsCache implements ThumbnailsCache {

    private static final String TAG = "MemoryThumbnailsCache";

    private LruCache mMemoryCache;

    public MemoryThumbnailsCache(int maxSize) {
        mMemoryCache = new LruCache<String, Bitmap>(maxSize) {
            @Override
            protected int sizeOf(String key, Bitmap value) {
                return value.getRowBytes() * value.getHeight() / 1024;
            }
        };
    }


    @Override
    public void putBitmap(String key, Bitmap bitmap) {
        if (null == getBitmap(key)) {
            Log.i(TAG, "putBitmap: " + key);
            mMemoryCache.put(key, bitmap);
        }
    }

    @Override
    public Bitmap getBitmap(String key) {
        Log.i(TAG, "getBitmap: " + key);
        return (Bitmap) mMemoryCache.get(key);
    }
}
