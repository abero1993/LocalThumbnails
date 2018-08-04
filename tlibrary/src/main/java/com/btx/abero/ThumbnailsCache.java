package com.btx.abero;

import android.graphics.Bitmap;

/**
 * Created by abero on 2018/7/16.
 */

public interface ThumbnailsCache {

    public void putBitmap(String key, Bitmap bitmap);

    public Bitmap getBitmap(String key);

}
