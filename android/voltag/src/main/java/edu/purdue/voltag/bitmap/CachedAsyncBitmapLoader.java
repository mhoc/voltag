package edu.purdue.voltag.bitmap;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.widget.ImageView;

import java.lang.ref.WeakReference;

/**
 * Implemented by David Tschida to allow long running loading if bitmaps in a ListView.
 */
public final class CachedAsyncBitmapLoader {

    /**
     * Asynchronously loads bitmaps into imageviews.
     * <p/>
     * When given an instance of an ImageRenderer, and imageview to place it in and
     * a cachehost, this method will spawn a task to asynchronously load an image into the view.
     * <p/>
     * This is optimized for calling in a listView's getView() method.
     *
     * @param imageOwner The owner that the image is created from.
     * @param imageView  The imageview to place the bitmap in
     * @param host       The host of the cache that this will use.
     * @param size       The size of the image that should be rendered (renders as a square)
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


    private static class BitmapWorkerTask extends AsyncTask<ImageRenderer, Void, Bitmap> {
        private final WeakReference<ImageView> imageViewReference;
        public ImageRenderer imageOwner = null;
        private BitmapCacheHost cacheHost;
        private int sizeofImage;

        public BitmapWorkerTask(BitmapCacheHost cacheHost, ImageView imageView, int size) {
            this.cacheHost = cacheHost;
            imageViewReference = new WeakReference<ImageView>(imageView);
            this.sizeofImage = size;
        }

        @Override
        protected Bitmap doInBackground(ImageRenderer... imageOwners) {
            imageOwner = imageOwners[0];
            Bitmap bitmap = imageOwner.renderBitmap(sizeofImage);
            cacheHost.addBitmapToMemoryCache(imageOwner.getUniqueImageId(), bitmap);
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (isCancelled()) {
                bitmap = null;
            }

            if (imageViewReference != null) {
                ImageView imageView = imageViewReference.get();
                BitmapWorkerTask bitmapDownloaderTask = getBitmapWorkerTask(imageView);
                // Change bitmap only if this process is still associated with it
                if (this == bitmapDownloaderTask) {
                    imageView.setImageBitmap(bitmap);
                }
            }
        }
    }

    private static class AsyncDrawable extends ColorDrawable {
        private final WeakReference<BitmapWorkerTask> bitmapWorkerTaskReference;

        public AsyncDrawable(BitmapWorkerTask bitmapWorkerTask) {
            super(Color.TRANSPARENT);
            bitmapWorkerTaskReference =
                    new WeakReference<BitmapWorkerTask>(bitmapWorkerTask);
        }

        public BitmapWorkerTask getBitmapWorkerTask() {
            return bitmapWorkerTaskReference.get();
        }
    }
}
