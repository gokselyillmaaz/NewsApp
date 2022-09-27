package com.example.android_final.util;

import android.graphics.Bitmap;
import android.media.Image;
import android.util.LruCache;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import com.example.android_final.R;

import java.net.URL;

public class LRUBitmapCache extends LruCache<String, Bitmap> {

    public static int getCacheSize(){
        int maxMemory = (int) (Runtime.getRuntime().maxMemory()/1024);

        int cacheSize = maxMemory / 8;
        return cacheSize;
    }
    public LRUBitmapCache(int maxSize) {
        super(maxSize);
    }

    public LRUBitmapCache(){
        this(getCacheSize());
    }

    @Override
    protected int sizeOf(String key, Bitmap value) {
        return value.getByteCount() /1024;
    }

    public synchronized void addBitmapToMemoryCache(String key,Bitmap value){
        if(getBitmapFromMemoryCache(key)==null){
            this.put(key,value);
        }
    }

    public Bitmap getBitmapFromMemoryCache(String key){
        return this.get(key);
    }

    public void loadBitmap(@NonNull URL url, @NonNull ImageView imageView){
        final Bitmap bitmap = getBitmapFromMemoryCache(url.toString());
        if(bitmap != null){
            imageView.setImageBitmap(bitmap);
        }else {

            BitmapAsyncLoader task = new BitmapAsyncLoader(this,imageView);
            task.execute(url);
        }
    }
}
