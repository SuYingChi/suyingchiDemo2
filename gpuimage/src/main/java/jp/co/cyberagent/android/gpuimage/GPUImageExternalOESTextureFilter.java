package jp.co.cyberagent.android.gpuimage;

import android.opengl.GLES11Ext;
import android.opengl.GLES20;

/**
 * Created by guanche on 08/08/2017.
 */

public class GPUImageExternalOESTextureFilter extends GPUImageFilter {
    public static final String FILTER_FRAGMENT_SHADER = "" +
            "#extension GL_OES_EGL_image_external : require\n" +
            "precision mediump float;\n" +
            "varying highp vec2 textureCoordinate;\n" +
            " \n" +
            "uniform samplerExternalOES inputImageTexture;\n" +
            " \n" +
            "void main()\n" +
            "{\n" +
            "     gl_FragColor = texture2D(inputImageTexture, textureCoordinate);\n" +
            "}";

    public GPUImageExternalOESTextureFilter() {
        super(GPUImageFilter.NO_FILTER_VERTEX_SHADER, FILTER_FRAGMENT_SHADER);
    }

    @Override
    protected void bindTexture(int textureId) {
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId);
    }
}
