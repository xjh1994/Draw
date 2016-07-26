package com.xjh1994.draw;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.view.View;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by Administrator on 2016/7/25.
 */
public class BitmapUtils {

    /**
     * 保存到SD卡
     *
     * @param context
     * @param bm
     * @param name
     */
    public static void saveImage(Context context, Bitmap bm, String name) {
        File appDir = new File(Environment.getExternalStorageDirectory(), "/draw");
        if (!appDir.exists()) {
            appDir.mkdir();
        }
        File file = new File(appDir, name);
        Log.d("BitmapUtils", file.getName());
        try {
            FileOutputStream out = new FileOutputStream(file);
            bm.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Uri uri = Uri.fromFile(file);
        // 通知图库更新
        Intent scannerIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri);
        context.sendBroadcast(scannerIntent);
    }

    /**
     * 将View转换为Bitmap
     *
     * @param view
     * @return
     */
    public static Bitmap convertViewToBitmap(View view) {
        view.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
        view.buildDrawingCache();
        Bitmap bitmap = view.getDrawingCache();

        return bitmap;
    }

    public static final Bitmap screenShot(Activity activity) {
        if (null == activity) {
            throw new IllegalArgumentException("parameter can't be null.");
        }
        View view = activity.getWindow().getDecorView();
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache();

        Bitmap b1 = view.getDrawingCache();
        Rect frame = new Rect();
        view.getWindowVisibleDisplayFrame(frame);

        int statusBarHeight = frame.top;

        Point point = new Point();
        activity.getWindowManager().getDefaultDisplay().getSize(point);

        int width = point.x;
        int height = point.y;

        Bitmap b2 = Bitmap.createBitmap(b1, 0, statusBarHeight, width, height - statusBarHeight);
        view.destroyDrawingCache();
        return b2;
    }
}
