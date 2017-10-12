package jp.co.cyberagent.android.gpuimage.util;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import jp.co.cyberagent.android.gpuimage.OpenGlUtils;

/**
 * Created by cyril on 20/02/2017.
 */

public final class CustomGlUtils {
    public static String readShaderFromRawResource(final Context context,
                                                   final int resourceId) {
        final InputStream inputStream = context.getResources().openRawResource(
                resourceId);
        final InputStreamReader inputStreamReader = new InputStreamReader(
                inputStream);
        final BufferedReader bufferedReader = new BufferedReader(
                inputStreamReader);

        String nextLine;
        final StringBuilder body = new StringBuilder();

        try {
            while ((nextLine = bufferedReader.readLine()) != null) {
                body.append(nextLine);
                body.append('\n');
            }
        } catch (IOException e) {
            return null;
        }
        return body.toString();
    }

    public static Bitmap loadBitmapFromAssets(Context context, String fileName) {
        Bitmap image = null;
        AssetManager am = context.getResources().getAssets();
        try {
            InputStream is = am.open(fileName);
            image = BitmapFactory.decodeStream(is);
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return image;
    }

    public static int loadTextureFromAssets(final Context context, final String imageName) {
        final Bitmap bitmap = loadBitmapFromAssets(context, imageName);
        return OpenGlUtils.loadTexture(bitmap, OpenGlUtils.NO_TEXTURE, true);
    }
}
