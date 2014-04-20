package edu.purdue.voltag.lobby;

import java.lang.ref.WeakReference;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;

public class AsyncDrawable extends ColorDrawable
{
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
