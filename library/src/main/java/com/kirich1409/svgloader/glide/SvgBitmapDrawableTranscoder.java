package com.kirich1409.svgloader.glide;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RestrictTo;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.engine.Resource;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapResource;
import com.bumptech.glide.load.resource.bitmap.DownsampleStrategy;
import com.bumptech.glide.load.resource.bitmap.Downsampler;
import com.bumptech.glide.load.resource.bitmap.LazyBitmapDrawableResource;
import com.bumptech.glide.load.resource.gif.GifOptions;
import com.bumptech.glide.load.resource.transcode.ResourceTranscoder;
import com.caverock.androidsvg.SVG;

@RestrictTo(RestrictTo.Scope.LIBRARY)
final class SvgBitmapDrawableTranscoder implements ResourceTranscoder<SVG, BitmapDrawable> {

    private final BitmapPool mBitmapPool;
    private final Resources mResources;
    private final SvgUtils.BitmapProvider mBitmapProvider;

    SvgBitmapDrawableTranscoder(@NonNull Context context, @NonNull Glide glide) {
        mResources = context.getResources();
        mBitmapPool = glide.getBitmapPool();
        mBitmapProvider = new PoolBitmapProvider(mBitmapPool);
    }

    @Override
    public Resource<BitmapDrawable> transcode(
            @NonNull Resource<SVG> toTranscode, @Nullable Options options) {
        prepareSvg(toTranscode, options);
        Bitmap bitmap = SvgUtils.toBitmap(toTranscode.get(), mBitmapProvider, getDecodeFormat(options));
        return LazyBitmapDrawableResource.obtain(mResources, new BitmapResource(bitmap, mBitmapPool));
    }

    @NonNull
    private Bitmap.Config getDecodeFormat(@Nullable Options options) {
        DecodeFormat decodeFormat = options == null ? null : options.get(GifOptions.DECODE_FORMAT);
        if (decodeFormat == null) {
            return Bitmap.Config.ARGB_8888;
        }

        switch (decodeFormat) {
            case PREFER_RGB_565:
                return Bitmap.Config.RGB_565;

            //noinspection deprecation
            case PREFER_ARGB_8888_DISALLOW_HARDWARE:
            case PREFER_ARGB_8888:
            default:
                return Bitmap.Config.ARGB_8888;
        }
    }

    private void prepareSvg(@NonNull Resource<SVG> toTranscode, @Nullable Options options) {
        if (!(toTranscode instanceof BaseSvgResource)) {
            return;
        }

        DownsampleStrategy strategy =
                options == null ? null : options.get(Downsampler.DOWNSAMPLE_STRATEGY);
        if (strategy != null) {
            float scaleFactor = strategy.getScaleFactor(
                    Math.round(toTranscode.get().getDocumentWidth()),
                    Math.round(toTranscode.get().getDocumentHeight()),
                    ((BaseSvgResource) toTranscode).getWidth(),
                    ((BaseSvgResource) toTranscode).getHeight()
            );
            SvgUtils.scaleDocumentSize(toTranscode.get(), scaleFactor);
        }
    }

    private static final class PoolBitmapProvider implements SvgUtils.BitmapProvider {

        private final BitmapPool mBitmapPool;

        PoolBitmapProvider(@NonNull BitmapPool bitmapPool) {
            mBitmapPool = bitmapPool;
        }

        @NonNull
        @Override
        public Bitmap get(int width, int height, @NonNull Bitmap.Config config) {
            return mBitmapPool.get(width, height, config);
        }
    }
}