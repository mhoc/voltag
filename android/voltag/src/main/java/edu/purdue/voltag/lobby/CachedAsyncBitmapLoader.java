package edu.purdue.voltag.lobby;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

/**
 * Created by david on 4/27/14.
 */
public final class CachedAsyncBitmapLoader {

    /**
     * Asynchronously loads bitmaps into imageviews.
     *
     * When given an instance of an ImageRenderer, and imageview to place it in and
     * a cachehost, this method will spawn a task to asynchronously load an image into the view.
     *
     * This is optimized for calling in a listView's getView() method.
     *
     * @param imageOwner The owner that the image is created from.
     * @param imageView The imageview to place the bitmap in
     * @param host The host of the cache that this will use.
     * @param size The size of the image that should be rendered (renders as a square)
     */
    public static void loadBitmapAsCachedAsyncTask(ImageRenderer imageOwner,
                                                   ImageView imageView, BitmapCacheHost host, int size) {
        final Bitmap bitmap = host.getBitmapFromMemCache(imageOwner.getUniqueImageId());

        if (bitmap != null) {
            imageView.setImageBitmap(bitmap);
        } else if (cancelPotentialWork(imageOwner, imageView)) {
            BitmapWorkerTask task = new BitmapWorkerTask(host, imageView, size);
            AsyncDrawable downloadedDrawable = new AsyncDrawable(task);
            imageView.setImageDrawable(downloadedDrawable);
            task.execute(imageOwner);
        }
    }

    private static boolean cancelPotentialWork(ImageRenderer imageOwner,
                                               ImageView imageView) {
        BitmapWorkerTask bitmapLoaderTask = getBitmapWorkerTask(imageView);

        if (bitmapLoaderTask != null) {
            String bitmapPath = bitmapLoaderTask.imageOwner.getUniqueImageId();
            if ((bitmapPath == null) || (!bitmapPath.equals(imageOwner.getUniqueImageId()))) {
                bitmapLoaderTask.cancel(true);
            } else {
                // The same URL is already being downloaded.
                return false;
            }
        }
        return true;
    }

    public static BitmapWorkerTask getBitmapWorkerTask(ImageView imageView) {
        if (imageView != null) {
            Drawable drawable = imageView.getDrawable();
            if (drawable instanceof AsyncDrawable) {
                AsyncDrawable downloadedDrawable = (AsyncDrawable) drawable;
                return downloadedDrawable.getBitmapWorkerTask();
            }
        }
        return null;
    }


}
