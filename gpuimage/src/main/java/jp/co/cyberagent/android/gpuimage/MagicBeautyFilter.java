package jp.co.cyberagent.android.gpuimage;

import android.content.Context;
import android.opengl.GLES20;
import android.os.Build;
import android.view.WindowManager;

import jp.co.cyberagent.android.gpuimage.util.CustomGlUtils;


public class MagicBeautyFilter extends GPUImageFilter {
    private int mSingleStepOffsetLocation;
    private int mParamsLocation;
    private int currentLevel;

    public MagicBeautyFilter(Context context) {
        super(NO_FILTER_VERTEX_SHADER,
                (shouldUseLowPerformanceShader(context) ?
                        CustomGlUtils.readShaderFromRawResource(context, R.raw.beautify_fragment_low) :
                        CustomGlUtils.readShaderFromRawResource(context, R.raw.beautify_fragment)));
    }

    private static boolean shouldUseLowPerformanceShader(Context context) {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        int resolution = windowManager.getDefaultDisplay().getWidth() * windowManager.getDefaultDisplay().getHeight();
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.M && resolution > 1000000;
    }

    public void onInit() {
        super.onInit();
        mSingleStepOffsetLocation = GLES20.glGetUniformLocation(getProgram(), "singleStepOffset");
        mParamsLocation = GLES20.glGetUniformLocation(getProgram(), "params");
        setBeautyLevel(currentLevel);
    }

    public void onDestroy() {
        super.onDestroy();
    }

    private void setTexelSize(final float w, final float h) {
        setFloatVec2(mSingleStepOffsetLocation, new float[]{2.0f / w, 2.0f / h});
    }

    @Override
    public void onOutputSizeChanged(final int width, final int height) {
        super.onOutputSizeChanged(width, height);
        setTexelSize(width, height);
    }

    public void setBeautyLevel(int level) {
        currentLevel = level;
        switch (level) {
            case 1:
                setFloatVec4(mParamsLocation, new float[]{1.5f, 1.0f, 0.15f, 0.15f});
                break;
            case 2:
                setFloatVec4(mParamsLocation, new float[]{0.8f, 0.9f, 0.2f, 0.2f});
                break;
            case 3:
                setFloatVec4(mParamsLocation, new float[]{0.6f, 0.8f, 0.25f, 0.25f});
                break;
            case 4:
                setFloatVec4(mParamsLocation, new float[]{0.4f, 0.7f, 0.38f, 0.3f});
                break;
            case 5:
                setFloatVec4(mParamsLocation, new float[]{0.33f, 0.63f, 0.4f, 0.35f});
                break;
            default:
                break;
        }
    }
}
