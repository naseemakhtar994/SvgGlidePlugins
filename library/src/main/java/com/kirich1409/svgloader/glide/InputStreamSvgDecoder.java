package com.kirich1409.svgloader.glide;

import android.support.annotation.NonNull;
import android.support.annotation.RestrictTo;

import com.bumptech.glide.load.Options;
import com.caverock.androidsvg.SVG;
import com.caverock.androidsvg.SVGParseException;

import java.io.IOException;
import java.io.InputStream;

@RestrictTo(RestrictTo.Scope.LIBRARY)
final class InputStreamSvgDecoder extends SvgDecoder<InputStream> {

    @Override
    SVG loadSvg(@NonNull InputStream source, int width, int height, @NonNull Options options)
            throws SVGParseException {
        return SVG.getFromInputStream(source);
    }

    @Override
    protected int getSize(@NonNull InputStream source) throws IOException {
        return SizeUtils.getSize(source);
    }
}
