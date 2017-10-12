package jp.co.cyberagent.android.gpuimage.util;

import android.content.Context;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by guanche on 09/08/2017.
 * File utils
 */

public class FileUtils {
    public static byte[] getFileContent(Context context, int id) {
        InputStream inputStream;
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int count;
        try {
            inputStream = context.getResources().openRawResource(id);
            while ((count = inputStream.read(buffer)) != -1) {
                byteArrayOutputStream.write(buffer, 0, count);
            }
            byteArrayOutputStream.close();
            inputStream.close();
        } catch (IOException e) {
            return null;
        }
        return byteArrayOutputStream.toByteArray();
    }
}
