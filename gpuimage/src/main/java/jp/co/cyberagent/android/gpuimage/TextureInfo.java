package jp.co.cyberagent.android.gpuimage;


import jp.co.cyberagent.android.gpuimage.util.TextureTransform;

public class TextureInfo {
    private int textureId;
    private int originalTextureWidth;
    private int originalTextureHeight;
    private int originalTextureOrientation;
    private TextureTransform transform = TextureTransform.NONE;

    public TextureInfo(int textureId, int textureWidth, int textureHeight, int textureOrientation) {
        this.textureId = textureId;
        this.originalTextureWidth = textureWidth;
        this.originalTextureHeight = textureHeight;
        this.originalTextureOrientation = textureOrientation;
    }

    public int getTextureId() {
        return textureId;
    }

    public int getTextureWidth() {
        return transform.isSwapWidthAndHeight() ? originalTextureHeight : originalTextureWidth;
    }

    public int getTextureHeight() {
        return transform.isSwapWidthAndHeight() ? originalTextureWidth : originalTextureHeight;
    }

    public int getTextureOrientation() {
        boolean isFlipHorizontal = transform.isFlipHorizontal();
        TextureTransform resultTransform = new TextureTransform(originalTextureOrientation).add(isFlipHorizontal ? TextureTransform.FLIP_HORIZONTAL : TextureTransform.FLIP_NONE);
        TextureTransform newTransform = new TextureTransform(transform).opposite().add(resultTransform);
        return newTransform.getRotation();
    }

    public TextureInfo newTransformedTexture(int textureId, TextureTransform transform) {
        TextureInfo result = new TextureInfo(textureId, originalTextureWidth, originalTextureHeight, originalTextureOrientation);
        result.transform = new TextureTransform(this.transform).add(transform);
        return result;
    }
}
