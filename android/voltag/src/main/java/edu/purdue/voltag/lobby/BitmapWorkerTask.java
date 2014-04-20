package edu.purdue.voltag.lobby;

import java.lang.ref.WeakReference;

import edu.purdue.voltag.data.Player;
import edu.purdue.voltag.helper.ImageHelper;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.widget.ImageView;

public class BitmapWorkerTask extends AsyncTask<Player, Void, Bitmap>
{
	private final WeakReference<ImageView> imageViewReference; 
	public Player player = null;
    private BitmapCacheHost cacheHost;
	
	public BitmapWorkerTask(BitmapCacheHost cacheHost, ImageView imageView)
	{
        this.cacheHost = cacheHost;
		imageViewReference = new WeakReference<ImageView>(imageView);
	}
	
	@Override
	protected Bitmap doInBackground(Player... players)
	{
		player = players[0];
		Bitmap bitmap = player.getGravitar(180);
		cacheHost.addBitmapToMemoryCache(player.getEmail(), bitmap);
		return bitmap;
	}
	
	@Override
    protected void onPostExecute(Bitmap bitmap) {
		if (isCancelled()) {
            bitmap = null;
        }

        if (imageViewReference != null) {
        	ImageView imageView = imageViewReference.get();
        	BitmapWorkerTask bitmapDownloaderTask = ImageHelper.getBitmapWorkerTask(imageView);
            // Change bitmap only if this process is still associated with it
            if (this == bitmapDownloaderTask) {
                imageView.setImageBitmap(bitmap);
            }
        }
		
		
    }
}
