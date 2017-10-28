package jp.co.cyberagent.android.gpuimage;

import jp.co.cyberagent.android.gpuimage.util.TextureTransform;

public interface GPUImageBaseFilter {
    TextureInfo draw(final TextureInfo textureInfo, final TextureTransform textureTransform, int outputWidth, int outputHeight, GPUImage.ScaleType scaleType, boolean drawToTexture);

    void destroy();

    boolean requireFeature(FilterFeature feature);

    enum FilterFeature {
        FACE_DETECT("face_detect"),
        OPEN_MOUTH_DETECT("open_mouth_detect"),
        RAISE_EYEBROW_DETECT("raise_eyebrow_detect"),
        BLINK_DETECT("blink_detect");

        private String name;

        FilterFeature(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }
}
