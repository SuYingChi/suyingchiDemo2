package jp.co.cyberagent.android.gpuimage;

import android.content.Context;

/**
 * Supply render source for GPUImageRenderer
 * Created by guanche on 21/09/2017.
 */

public interface GPUImageRendererSource {
    int getTextureId();

    boolean isTextureExternalOES();

    int getTextureWidth();

    int getTextureHeight();

    int getFaceOrientation();

    void initTexture();

    void updateTexture();

    void destroyTexture();
}
