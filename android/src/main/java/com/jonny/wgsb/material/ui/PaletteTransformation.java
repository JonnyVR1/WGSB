package com.jonny.wgsb.material.ui;

import android.graphics.Bitmap;
import android.support.v4.util.Pools;
import android.support.v7.graphics.Palette;

import com.squareup.picasso.Transformation;

import java.util.Map;
import java.util.WeakHashMap;

public final class PaletteTransformation implements Transformation {
    private static final PaletteTransformation INSTANCE = new PaletteTransformation();
    private static final Map<Bitmap, Palette> CACHE = new WeakHashMap<>();
    private static final Pools.Pool<PaletteTransformation> POOL = new Pools.SynchronizedPool<>(5);

    private PaletteTransformation() {
    }

    public static PaletteTransformation getInstance() {
        PaletteTransformation instance = POOL.acquire();
        return instance != null ? instance : new PaletteTransformation();
    }

    public static PaletteTransformation instance() {
        return INSTANCE;
    }

    public static Palette getPalette(Bitmap bitmap) {
        return CACHE.get(bitmap);
    }

    @Override
    public Bitmap transform(Bitmap source) {
        Palette palette = Palette.generate(source);
        CACHE.put(source, palette);
        return source;
    }

    @Override
    public String key() {
        return "";
    }
}