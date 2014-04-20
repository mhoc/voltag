package edu.purdue.voltag.lobby;

import android.graphics.Bitmap;

/**
 * Created by david on 4/20/14.
 */
public interface BitmapCacheHost {

    public void addBitmapToMemoryCache(String id, Bitmap bitmap);
    public Bitmap getBitmapFromMemCache(String key);
    public void initMemoryCache();
}
