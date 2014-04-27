package edu.purdue.voltag.bitmap;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.widget.ImageView;

import java.lang.ref.WeakReference;

public class BitmapWorkerTask extends AsyncTask<ImageRenderer, Void, Bitmap> {
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
            BitmapWorkerTask bitmapDownloaderTask = CachedAsyncBitmapLoader.getBitmapWorkerTask(imageView);
            // Change bitmap only if this process is still associated with it
            if (this == bitmapDownloaderTask) {
                imageView.setImageBitmap(bitmap);
            }
        }
    }
}
