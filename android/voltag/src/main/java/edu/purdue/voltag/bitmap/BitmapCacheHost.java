package edu.purdue.voltag.bitmap;

import android.graphics.Bitmap;

/**
 * Interface for contexts that implement a cache for storing bitmaps for use in CachedAsyncBitmapLoader
 *
 * The init and clear should be called upon the creation and destruction of the context that is being shown.
 */
public interface BitmapCacheHost {

    public void addBitmapToMemoryCache(String id, Bitmap bitmap);

    public Bitmap getBitmapFromMemCache(String key);

    public void initMemoryCache();

    public void clearCache();
}
