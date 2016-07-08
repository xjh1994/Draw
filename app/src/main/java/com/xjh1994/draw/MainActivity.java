package com.xjh1994.draw;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

public class MainActivity extends AppCompatActivity {
    private DrawOutlineView drawOutlineView;
    private Bitmap sobelBm;
    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        //将Bitmap压缩处理，防止OOM
        Bitmap bm = CommenUtils.getRatioBitmap(this, R.drawable.test, 100, 100);
        sobelBm = SobelUtils.Sobel(bm);

        drawOutlineView = (DrawOutlineView) findViewById(R.id.outline);

        Bitmap paintBm = CommenUtils.getRatioBitmap(this, R.drawable.paint, 10, 20);
        drawOutlineView.setPaintBm(paintBm);

        imageView = (ImageView) findViewById(R.id.dd);
    }

    public void pickPhoto(View view) {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, 1);
    }

    public void pickPen(View view) {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, 2);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) {
            return;
        }

        Uri mImageCaptureUri = data.getData();
        if (mImageCaptureUri != null) {
            if (requestCode == 1) {
                Bitmap bitmap;
                try {
                    //这个方法是根据Uri获取Bitmap图片的静态方法
                    bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), mImageCaptureUri);
                    if (bitmap != null) {
                        Bitmap b = bitmap.copy(Bitmap.Config.ARGB_8888, false);
                        imageView.setImageBitmap(b);
                        //返回的是处理过的Bitmap
                        sobelBm = SobelUtils.Sobel(bitmap);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (requestCode == 2) {
                Bitmap bitmap = null;
                try {
                    //这个方法是根据Uri获取Bitmap图片的静态方法
                    bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), mImageCaptureUri);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (bitmap != null) {
                    drawOutlineView.setPaintBm(bitmap);
                }
            }
        }

    }

    //根据Bitmap信息，获取每个位置的像素点是否需要绘制
    //使用boolean数组而不是int[][]主要是考虑到内存的消耗
    private boolean[][] getArray(Bitmap bitmap) {
        boolean[][] b = new boolean[bitmap.getWidth()][bitmap.getHeight()];

        for (int i = 0; i < bitmap.getWidth(); i++) {
            for (int j = 0; j < bitmap.getHeight(); j++) {
                if (bitmap.getPixel(i, j) != Color.WHITE)
                    b[i][j] = true;
                else
                    b[i][j] = false;
            }
        }
        return b;
    }

    boolean first = true;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (first) {
            first = false;
            drawOutlineView.beginDraw(getArray(sobelBm));
        } else
            drawOutlineView.reDraw(getArray(sobelBm));
        return true;
    }
}