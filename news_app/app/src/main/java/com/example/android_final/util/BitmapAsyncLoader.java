package com.example.android_final.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.security.PublicKey;

public class BitmapAsyncLoader extends AsyncTask<URL,Void, Bitmap> {

    private final String TAG = "BitmapAsyncLoader";
    private WeakReference<LRUBitmapCache> lruBitmapCache;
    private WeakReference<ImageView> imageViewWeakReference;

    public BitmapAsyncLoader(LRUBitmapCache lruBitmapCache, ImageView imageView){
        this.lruBitmapCache = new WeakReference<>(lruBitmapCache);
        this.imageViewWeakReference = new WeakReference<>(imageView);
    }

    @Override
    protected Bitmap doInBackground(URL... urls) {

        URL imageurl = urls[0];
        Bitmap downloadedBitmap = null;

        try{
            InputStream inputStream = imageurl.openStream();
            downloadedBitmap = BitmapFactory.decodeStream(inputStream);

            if(lruBitmapCache != null){
                LRUBitmapCache cache = lruBitmapCache.get();
                cache.addBitmapToMemoryCache(imageurl.toString(),downloadedBitmap);
            }
        }catch (IOException e){
            Log.e(TAG,e.getMessage());
            e.printStackTrace();
        }
        return downloadedBitmap;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        super.onPostExecute(bitmap);

        if(imageViewWeakReference != null && bitmap != null)
        {
            ImageView imageView = imageViewWeakReference.get();
            imageView.setImageBitmap(bitmap);
        }
    }
}
