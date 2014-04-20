package edu.purdue.voltag.helper;

/**
 * Created by tylorgarrett on 4/19/14.
 */
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Bitmap.Config;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import edu.purdue.voltag.data.Player;
import edu.purdue.voltag.lobby.AsyncDrawable;
import edu.purdue.voltag.lobby.BitmapCacheHost;
import edu.purdue.voltag.lobby.BitmapWorkerTask;

public class ImageHelper {

    public static Bitmap getRoundedCornerBitmap(Bitmap bitmap, int pixels) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap
                .getHeight(), Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);
        final float roundPx = pixels;

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

        paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        return output;
    }

    public static void loadBitmapAsAsyncTask(Player player,
                                             ImageView imageView, BitmapCacheHost host)
    {
        final Bitmap bitmap = host.getBitmapFromMemCache(player.getEmail());

        if(bitmap != null)
        {
            imageView.setImageBitmap(bitmap);
        }
        else if (cancelPotentialWork(player, imageView))
        {
            BitmapWorkerTask task = new BitmapWorkerTask(host, imageView);
            AsyncDrawable downloadedDrawable = new AsyncDrawable(task);
            imageView.setImageDrawable(downloadedDrawable);
            task.execute(player);
        }

    }

    private static boolean cancelPotentialWork(Player player,
                                               ImageView imageView)
    {
        BitmapWorkerTask bitmapLoaderTask = getBitmapWorkerTask(imageView);

        if (bitmapLoaderTask != null)
        {
            String bitmapPath = bitmapLoaderTask.player.getEmail();
            if ((bitmapPath == null) || (!bitmapPath.equals(player.getEmail())))
            {
                bitmapLoaderTask.cancel(true);
            } else
            {
                // The same URL is already being downloaded.
                return false;
            }
        }
        return true;
    }

    public static BitmapWorkerTask getBitmapWorkerTask(ImageView imageView)
    {
        if (imageView != null)
        {
            Drawable drawable = imageView.getDrawable();
            if (drawable instanceof AsyncDrawable)
            {
                AsyncDrawable downloadedDrawable = (AsyncDrawable) drawable;
                return downloadedDrawable.getBitmapWorkerTask();
            }
        }
        return null;
    }

}