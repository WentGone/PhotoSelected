package cn.mdruby.pickphotovideoview.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import cn.mdruby.pickphotovideoview.App;

/**
 * Created by Went_Gone on 2018/3/28.
 */

public class FileUtil {
    public static File saveImage(Context context,Bitmap bmp) {
        File appDir = new File(Environment.getExternalStorageDirectory(), "broewers");

        if (!appDir.exists()) {
//            appDir.mkdir();
        }
        String fileName = System.currentTimeMillis() + ".jpg";
        appDir = context.getFilesDir();
        File file = new File(appDir, fileName);
        try {
            FileOutputStream fos = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file;
    }

    /**
     * 删除单个文件
     *
     * @param fileName
     *            要删除的文件的文件名
     * @return 单个文件删除成功返回true，否则返回false
     */
    public static boolean deleteFile(String fileName) {
        File file = new File(fileName);
        // 如果文件路径所对应的文件存在，并且是一个文件，则直接删除
        if (file.exists() && file.isFile()) {
            if (file.delete()) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }
}
