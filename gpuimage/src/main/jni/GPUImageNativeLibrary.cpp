//
// Created by guanche on 14/09/2017.
//

#include "GPUImageNativeLibrary.h"

JNIEXPORT void JNICALL Java_jp_co_cyberagent_android_gpuimage_GPUImageNativeLibrary_captureGLData
        (JNIEnv *env, jclass obj, jintArray in, jintArray out, jint w, jint h) {
    int offset1;
    int offset2;
    int i;
    int j;
    int texturePixel;
    int blue;
    int red;
    int pixel;

    jint *inArray = (jint *) (env->GetPrimitiveArrayCritical(in, 0));
    jint *outArray = (jint *) (env->GetPrimitiveArrayCritical(out, 0));

    for (i = 0; i < h; i++) {
        offset1 = i * w;
        offset2 = (h - i - 1) * w;
        for (j = 0; j < w; j++) {
            texturePixel = inArray[offset1 + j];
            blue = (texturePixel >> 16) & 0xff;
            red = (texturePixel << 16) & 0x00ff0000;
            pixel = (texturePixel & 0xff00ff00) | red | blue;
            outArray[offset2 + j] = pixel;
        }
    }

    env->ReleasePrimitiveArrayCritical(in, inArray, 0);
    env->ReleasePrimitiveArrayCritical(out, outArray, 0);
}
