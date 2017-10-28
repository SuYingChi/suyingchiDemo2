/*
 * Copyright (C) 2012 CyberAgent
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jp.co.cyberagent.android.gpuimage;

import android.annotation.TargetApi;
import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView.Renderer;

import java.util.LinkedList;
import java.util.Queue;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import jp.co.cyberagent.android.gpuimage.util.TextureTransform;

@TargetApi(11)
public class GPUImageRenderer implements Renderer {
    private GPUImageBaseFilter mFilter;
    private GPUImageExternalOESTextureFilter mExternalOESTextureFilter;

    public final Object mSurfaceChangedWaiter = new Object();

    private GPUImageRendererSource rendererSource;

    private int mOutputWidth;
    private int mOutputHeight;

    private final Queue<Runnable> mRunOnDraw;
    private final Queue<Runnable> mRunOnDrawEnd;
    private TextureTransform mTextureTransform = TextureTransform.NONE;
    private GPUImage.ScaleType mScaleType = GPUImage.ScaleType.CENTER_CROP;

    private float mBackgroundRed = 0;
    private float mBackgroundGreen = 0;
    private float mBackgroundBlue = 0;

    private Context context;

    public GPUImageRenderer(final GPUImageBaseFilter filter, Context context) {
        mFilter = filter;
        this.context = context;
        mRunOnDraw = new LinkedList<>();
        mRunOnDrawEnd = new LinkedList<>();
    }

    @Override
    public void onSurfaceCreated(final GL10 unused, final EGLConfig config) {
        GLES20.glClearColor(mBackgroundRed, mBackgroundGreen, mBackgroundBlue, 1);
        GLES20.glDisable(GLES20.GL_DEPTH_TEST);
    }

    @Override
    public void onSurfaceChanged(final GL10 gl, final int width, final int height) {
        mOutputWidth = width;
        mOutputHeight = height;
        synchronized (mSurfaceChangedWaiter) {
            mSurfaceChangedWaiter.notifyAll();
        }
    }

    @Override
    public void onDrawFrame(final GL10 gl) {
        GLES20.glClearColor(mBackgroundRed, mBackgroundGreen, mBackgroundBlue, 1);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        runAll(mRunOnDraw);

        if (rendererSource != null) {
            rendererSource.updateTexture();

            if (mFilter != null) {
                TextureInfo textureInfo = new TextureInfo(rendererSource.getTextureId(), rendererSource.getTextureWidth(), rendererSource.getTextureHeight(),
                        rendererSource.getFaceOrientation());
                if (rendererSource.isTextureExternalOES()) {
                    if (mExternalOESTextureFilter == null) {
                        mExternalOESTextureFilter = new GPUImageExternalOESTextureFilter();
                    }
                    textureInfo = mExternalOESTextureFilter.draw(textureInfo, TextureTransform.NONE, rendererSource.getTextureWidth(), rendererSource.getTextureHeight(), mScaleType, true);
                }
                mFilter.draw(textureInfo, mTextureTransform, mOutputWidth, mOutputHeight, mScaleType, false);
            }
        }

        runAll(mRunOnDrawEnd);
    }

    /**
     * Sets the background color
     *
     * @param red   red color value
     * @param green green color value
     * @param blue  red color value
     */
    public void setBackgroundColor(float red, float green, float blue) {
        mBackgroundRed = red;
        mBackgroundGreen = green;
        mBackgroundBlue = blue;
    }

    private void runAll(Queue<Runnable> queue) {
        synchronized (queue) {
            while (!queue.isEmpty()) {
                queue.poll().run();
            }
        }
    }

    public void setRendererSource(final GPUImageRendererSource rendererSource) {
        runOnDraw(new Runnable() {
            @Override
            public void run() {
                GPUImageRenderer.this.rendererSource = rendererSource;
                rendererSource.initTexture();
            }
        });
    }

    public void setFilter(final GPUImageBaseFilter filter) {
        runOnDraw(new Runnable() {
            @Override
            public void run() {
                if (mFilter != null) {
                    mFilter.destroy();
                }
                mFilter = filter;
            }
        });
    }

    public void clearRendererSource() {
        runOnDraw(new Runnable() {
            @Override
            public void run() {
                if (rendererSource != null) {
                    rendererSource.destroyTexture();
                    rendererSource = null;
                }
                if (mFilter != null) {
                    mFilter.destroy();
                }
                if (mExternalOESTextureFilter != null) {
                    mExternalOESTextureFilter.destroy();
                }
            }
        });
    }

    public void setScaleType(GPUImage.ScaleType scaleType) {
        mScaleType = scaleType;
    }

    protected int getFrameWidth() {
        return mOutputWidth;
    }

    protected int getFrameHeight() {
        return mOutputHeight;
    }

    public void setTextureTransform(TextureTransform textureTransform) {
        mTextureTransform = textureTransform;
    }

    protected void runOnDraw(final Runnable runnable) {
        synchronized (mRunOnDraw) {
            mRunOnDraw.add(runnable);
        }
    }

    protected void runOnDrawEnd(final Runnable runnable) {
        synchronized (mRunOnDrawEnd) {
            mRunOnDrawEnd.add(runnable);
        }
    }
}
