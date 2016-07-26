package com.xjh1994.draw;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.io.File;

import cn.bmob.v3.Bmob;
import cn.bmob.v3.update.BmobUpdateAgent;
import co.mobiwise.materialintro.animation.MaterialIntroListener;

public class MainActivity extends AppCompatActivity {

    private DrawOutlineView drawOutlineView;
    private Bitmap sobelBm;
    private ImageView imageView;
    private RelativeLayout contentPanel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Bmob.initialize(this, "6691de2a1e3a6908f84cb9bc79732915");

        //将Bitmap压缩处理，防止OOM
        Bitmap bm = CommenUtils.getRatioBitmap(this, R.drawable.test, 100, 100);
        sobelBm = SobelUtils.Sobel(bm);

        drawOutlineView = (DrawOutlineView) findViewById(R.id.outline);

        Bitmap paintBm = CommenUtils.getRatioBitmap(this, R.drawable.paint, 10, 20);
        drawOutlineView.setPaintBm(paintBm);

        imageView = (ImageView) findViewById(R.id.dd);
        contentPanel = (RelativeLayout) findViewById(R.id.contentPanel);

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!drawOutlineView.isDrawing()) {
                    pickPhoto(null);
                }
            }
        });

        initGuide();
        BmobUpdateAgent.forceUpdate(this);
    }

    private void initGuide() {
        GuideUtils.show(this, imageView, "点击图片选择相册", "imageView", new MaterialIntroListener() {
            @Override
            public void onUserClicked(String s) {
                GuideUtils.show(MainActivity.this, drawOutlineView, "点击空白开始手绘", "drawOutlineView", new MaterialIntroListener() {
                    @Override
                    public void onUserClicked(String s) {
                        if (!drawOutlineView.isDrawing()) {
                            draw(null);
                        }
                    }
                });
            }
        });
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!drawOutlineView.isDrawing()) {
            draw(null);
        }

        return super.onTouchEvent(event);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                if (!drawOutlineView.isDrawing()) {
                    if (drawOutlineView.hasDraw()) {
                        Bitmap bitmap = drawOutlineView.getBitmap();
                        if (bitmap != null) {
                            save(bitmap);
                        } else {
                            Toast.makeText(MainActivity.this, "保存失败~", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(MainActivity.this, "还没画完呢~", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(MainActivity.this, "还没画完呢~", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.action_about:
                startActivity(new Intent(MainActivity.this, AboutActivity.class));
                break;
            default:
                break;
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    public void pickPhoto(View view) {
        if (!drawOutlineView.isDrawing()) {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(intent, 1);

            drawOutlineView.setHasDraw(false);
        }
    }

    public void pickPen(View view) {
        if (!drawOutlineView.isDrawing()) {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(intent, 2);
        }
    }

    public void draw(View view) {
        if (first) {
            first = false;
            drawOutlineView.beginDraw(getArray(sobelBm));
        } else
            drawOutlineView.reDraw(getArray(sobelBm));
    }

    private static final String APP_DIR = "draw";

    public void save(Bitmap bitmap) {
        BitmapUtils.saveImage(this, bitmap, APP_DIR + String.valueOf(System.currentTimeMillis()) + ".jpg");
        File appDir = new File(Environment.getExternalStorageDirectory(), APP_DIR);
        String msg = String.format("图片已保存至 %s 文件夹", appDir.getAbsolutePath());
        Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
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

    /*@Override
    public boolean onTouchEvent(MotionEvent event) {
        if (first) {
            first = false;
            drawOutlineView.beginDraw(getArray(sobelBm));
        } else
            drawOutlineView.reDraw(getArray(sobelBm));
        return true;
    }*/
}