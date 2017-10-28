package jp.co.cyberagent.android.gpuimage.util;

/**
 * Created by guanche on 18/08/2017.
 * 对Texture进行旋转翻转信息（先旋转，后翻转）
 */

public class TextureTransform {
    private static final float TEXTURE_NO_ROTATION[] = {
            0.0f, 1.0f,
            1.0f, 1.0f,
            0.0f, 0.0f,
            1.0f, 0.0f,
    };
    private static final float TEXTURE_ROTATED_90[] = {
            1.0f, 1.0f,
            1.0f, 0.0f,
            0.0f, 1.0f,
            0.0f, 0.0f,
    };
    private static final float TEXTURE_ROTATED_180[] = {
            1.0f, 0.0f,
            0.0f, 0.0f,
            1.0f, 1.0f,
            0.0f, 1.0f,
    };
    private static final float TEXTURE_ROTATED_270[] = {
            0.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 0.0f,
            1.0f, 1.0f,
    };

    public static final int FLIP_NONE = 0;
    public static final int FLIP_HORIZONTAL = 1;
    public static final int FLIP_VERTICAL = 2;
    // 是否水平翻转
    private boolean flipHorizontal = false;
    // 旋转角度
    private int rotation = 0;

    public static final TextureTransform NONE = new TextureTransform();

    private TextureTransform() {
    }

    /**
     * 创建一个TextureTransform
     *
     * @param flipOrRotation 可以为翻转0/1/2，或者旋转0/90/180/270
     */
    public TextureTransform(int flipOrRotation) {
        add(flipOrRotation);
    }

    public TextureTransform(TextureTransform transform) {
        rotation = transform.rotation;
        flipHorizontal = transform.flipHorizontal;
    }

    public boolean isSwapWidthAndHeight() {
        int temp = getRotation();
        return temp == 90 || temp == 270;
    }

    public TextureTransform add(int flipOrRotation) {
        switch (flipOrRotation) {
            case FLIP_NONE:
                break;
            case FLIP_HORIZONTAL:
                if (isSwapWidthAndHeight()) {
                    rotation += 180;
                }
                flipHorizontal = !flipHorizontal;
                break;
            case FLIP_VERTICAL:
                if (!isSwapWidthAndHeight()) {
                    rotation += 180;
                }
                flipHorizontal = !flipHorizontal;
                break;
            default:
                rotation += flipOrRotation;
                break;
        }
        return this;
    }

    public TextureTransform add(TextureTransform transform) {
        if (transform.flipHorizontal) {
            add(FLIP_HORIZONTAL);
        }
        return add(transform.rotation);
    }

    public TextureTransform opposite() {
        if (isSwapWidthAndHeight() && flipHorizontal) {
            rotation = -rotation + 180;
        } else {
            rotation = -rotation;
        }
        return this;
    }

    public int getRotation() {
        int result = rotation;
        while (result < 0) {
            result += 360;
        }

        while (result >= 360) {
            result -= 360;
        }
        return result;
    }

    public boolean isFlipHorizontal() {
        return flipHorizontal;
    }

    public float[] getTextureCords() {
        float[] rotatedTex;
        switch (getRotation()) {
            case 90:
                rotatedTex = TEXTURE_ROTATED_90;
                break;
            case 180:
                rotatedTex = TEXTURE_ROTATED_180;
                break;
            case 270:
                rotatedTex = TEXTURE_ROTATED_270;
                break;
            case 0:
            default:
                rotatedTex = TEXTURE_NO_ROTATION;
                break;
        }
        if (flipHorizontal) {
            rotatedTex = new float[]{
                    flip(rotatedTex[0]), rotatedTex[1],
                    flip(rotatedTex[2]), rotatedTex[3],
                    flip(rotatedTex[4]), rotatedTex[5],
                    flip(rotatedTex[6]), rotatedTex[7],
            };
        }

        return rotatedTex;
    }

    private float flip(final float i) {
        if (i == 0.0f) {
            return 1.0f;
        }
        return 0.0f;
    }
}
