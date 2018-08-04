package com.btx.abero;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.ImageView;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by abero on 2018/7/16.
 */

public class LocalThumbnails {

    private static final String TAG = "LocalThumbnails";

    private Context mContext;
    private boolean enableMemoryCache;
    private boolean enableDiskCache;
    private int mMaxMemoryCacheSize;
    private int mMaxDiskCacheSize;

    private MemoryThumbnailsCache mMemoryCache;
    private DiskThumbnailsCache mDiskCache;

    //
    private ThreadPoolExecutor mExecutor;
    private Map<String, ImageView> mViewMap;
    private Map<String, BitmapRunable> mRunableMap;

    //
    private ThumbProc mThumbProc;


    private LocalThumbnails(Context context, boolean enableMemoryCache, boolean enableDiskCache, int memoryCacheSize, int diskCacheSize) {

        this.mContext = context;
        this.enableMemoryCache = enableMemoryCache;
        this.enableDiskCache = enableDiskCache;
        this.mMaxMemoryCacheSize = memoryCacheSize;
        this.mMaxDiskCacheSize = diskCacheSize;

        if (mMaxMemoryCacheSize <= 0) {
            int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
            //设置LruCache的缓存大小
            mMaxMemoryCacheSize = maxMemory / 8;
            Log.i(TAG, "init memory size=" + mMaxMemoryCacheSize);
        }

        if (mMaxDiskCacheSize <= 0) {
            //200m
            mMaxDiskCacheSize = 200 * 1024 * 1024;
        }

        Log.i(TAG, "memory cache size=" + mMaxMemoryCacheSize + "KB");
        mMemoryCache = new MemoryThumbnailsCache(mMaxMemoryCacheSize);

        mDiskCache = new DiskThumbnailsCache(context, mMaxDiskCacheSize);


        BlockingQueue workQueue = new LinkedBlockingDeque<>();
        ThreadFactory threadFactory = Executors.defaultThreadFactory();
        RejectedExecutionHandler handler = new ThreadPoolExecutor.DiscardPolicy();

        mExecutor = new ThreadPoolExecutor(5, 20, 1, TimeUnit.SECONDS, workQueue, threadFactory, handler);
        mViewMap = new HashMap<>(20);
        mRunableMap = new HashMap<>(20);

        mThumbProc = new ThumbProc(context);

    }


    public static class Builder {

        private boolean enableMemoryCache = true;
        private boolean enableDiskCache = true;
        private int memoryCacheSize = 0;
        private int diskCacheSize = 0;
        private Context context;

        public Builder enableMemoryCache(boolean cache) {
            enableMemoryCache = cache;
            return this;
        }

        public Builder setMemoryCacheSize(int size) {

            memoryCacheSize = size;
            return this;
        }

        public Builder anableDiskCache(boolean cache) {
            enableDiskCache = cache;
            return this;
        }

        public Builder setDiskCacheSize(int size) {
            diskCacheSize = size;
            return this;
        }

        public LocalThumbnails create(Context context) {
            LocalThumbnails localThumbnails = new LocalThumbnails(context, enableMemoryCache, enableDiskCache, memoryCacheSize, diskCacheSize);
            return localThumbnails;
        }

    }

    public void load(String filePath, ImageView imageView) {

        if (!mViewMap.containsKey(filePath)) {

            mViewMap.put(filePath, imageView);

            BitmapRunable bitmapRunable = new BitmapRunable(filePath);
            mRunableMap.put(filePath, bitmapRunable);

            mExecutor.execute(bitmapRunable);
        }

        Log.i(TAG, "load map size="+mViewMap.size());

    }


    private class BitmapRunable implements Runnable {

        private String mPath;

        public BitmapRunable(String filePath) {
            mPath = filePath;
        }

        @Override
        public void run() {

            String key = Util.hashKeyForDisk(mPath);
            Log.i(TAG, "start get thumbnails =" + mPath);
            Log.i(TAG, "md5=" + key);
            Bitmap bitmap = null;

            if (enableMemoryCache && enableDiskCache) {
                bitmap = mMemoryCache.getBitmap(key);
                if (null == bitmap) {
                    bitmap = mDiskCache.getBitmap(key);
                    if (null == bitmap) {
                        bitmap = mThumbProc.getFileThumbnail(mPath);
                        if (null == bitmap) {
                            Log.e(TAG, "can not get thumbnails");
                        } else {
                            Log.i(TAG, "got discache");
                            mMemoryCache.putBitmap(key, bitmap);
                            mDiskCache.putBitmap(key, bitmap);
                        }
                    } else {
                        Log.i(TAG, "got memorycache");
                        mMemoryCache.putBitmap(key, bitmap);
                    }
                }
            } else if (!enableMemoryCache && enableDiskCache) {
                bitmap = mDiskCache.getBitmap(key);
                if (null == bitmap) {
                    bitmap = mThumbProc.getFileThumbnail(mPath);
                    if (null == bitmap) {
                        Log.e(TAG, "can not get thumbnails");
                    } else {
                        Log.i(TAG, "got discache");
                        mDiskCache.putBitmap(key, bitmap);
                    }
                }
            } else if (enableMemoryCache && !enableDiskCache) {
                bitmap = mMemoryCache.getBitmap(key);
                if (null == bitmap) {
                    bitmap = mThumbProc.getFileThumbnail(mPath);
                    if (null == bitmap) {
                        Log.e(TAG, "can not get thumbnails");
                    } else {
                        Log.i(TAG, "got discache");
                        mMemoryCache.putBitmap(key, bitmap);
                    }
                }
            } else {
                bitmap = mThumbProc.getFileThumbnail(mPath);
                if (null == bitmap) {
                    Log.e(TAG, "can not get thumbnails");
                } else {
                    Log.i(TAG, "got discache");
                }
            }

            sendBitmap(mPath, bitmap);

        }
    }

    private void sendBitmap(String key, Bitmap bitmap) {
        Message msg = mHandler.obtainMessage();
        msg.what = 1;
        msg.obj = bitmap;
        Bundle bundle = new Bundle();
        bundle.putString("key", key);
        msg.setData(bundle);
        mHandler.sendMessage(msg);
    }

    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            Bitmap bitmap = (Bitmap) msg.obj;
            String key = msg.getData().getString("key");
            ImageView view = mViewMap.remove(key);
            view.setImageBitmap(bitmap);
            mRunableMap.remove(key);
            //bitmap.recycle();
        }
    };


}
