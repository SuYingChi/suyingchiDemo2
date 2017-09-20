package com.ihs.inputmethod.uimodules.ui.facemoji.ui;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ihs.app.framework.HSApplication;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.ihs.commons.utils.HSLog;
import com.ihs.inputmethod.api.managers.HSPictureManager;
import com.ihs.inputmethod.api.utils.HSFileUtils;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.ui.facemoji.FacemojiManager;
import com.ihs.inputmethod.uimodules.ui.settings.activities.HSAppCompatActivity;
import com.ihs.inputmethod.uimodules.utils.BitmapUtils;
import com.ihs.inputmethod.uimodules.utils.DisplayUtils;
import com.ihs.inputmethod.uimodules.utils.UriUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import pl.droidsonroids.gif.GifImageView;


public class CameraActivity extends HSAppCompatActivity {
    public static final String FACEMOJI_SAVED = "FACEMOJI_SAVED";
    public static final String FACE_CHANGED = "FACE_CHANGED";
    private static final int SAMPLE_SIZE = 8;

    private INotificationObserver mImeActionObserver = new INotificationObserver() {
        @Override
        public void onReceive(String eventName, HSBundle notificaiton) {
            if (eventName == null) {
                return;
            }

            if (eventName.equals(FACEMOJI_SAVED)) {
                finish();
            }
        }
    };

    private final class MyPictureCallback implements Camera.PictureCallback {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            final ImageView maskView = (ImageView) findViewById(R.id.mask_view);
            String index = maskView.getTag().toString();

            View faceTitleBar = findViewById(R.id.face_title_bar);
            RelativeLayout.LayoutParams faceTitlePara = (RelativeLayout.LayoutParams) faceTitleBar.getLayoutParams();

            final Camera.Parameters parameters = camera.getParameters();

            int cropHeight1 = parameters.getPictureSize().width * faceTitlePara.height / parameters.getPreviewSize().width;
            int cropHeight2 = faceTitlePara.height;
            int cropHeight3 = parameters.getPictureSize().width * faceTitlePara.height / surfaceView.getHeight();
            int cropHeight4 = parameters.getPictureSize().width * faceTitlePara.height / DisplayUtils.getScreenHeightForContent();

            int cropHeight = (int) (4.0f / (1.0f / cropHeight1 + 1.0f / cropHeight2 + 1.0f / cropHeight3 + 1.0f / cropHeight4));

            new PictureProcessor().execute(data,
                    index.getBytes(),
                    String.valueOf(cropHeight).getBytes());


        }
    }


    private final class SurfaceCallback implements SurfaceHolder.Callback {

        // 拍照状态变化时调用该方法
        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            camera.startPreview();
            final Camera.Parameters parameters = camera.getParameters();
            final FrameLayout.LayoutParams surfaceViewLayoutParams = (FrameLayout.LayoutParams) surfaceView.getLayoutParams();
            Camera.Size size = getOptimalCameraSize(parameters.getSupportedPictureSizes(), surfaceViewLayoutParams.width, surfaceViewLayoutParams.height);
            parameters.setPictureSize(size.width, size.height);
            camera.setParameters(parameters);
        }

        // 开始拍照时调用该方法
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            releaseCamera();
            startCameraAndPreview(holder);
        }

        // 停止拍照时调用该方法
        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            releaseCamera();
        }
    }


    /**
     * 播放 GIF
     */
    private void playAnimation() {
        mCreatingView.setImageURI(Uri.parse("res://" + HSApplication.getContext().getPackageName() + "/" + R.raw.creating));
    }

    /**
     * 显示处理等待对话框
     */
    private void showProcessingDialog() {
        //show dialog
        if (mCreatingDialog == null) {

            mCreatingDialog = new Dialog(CameraActivity.this, R.style.CustomProgressDialog);
            mCreatingDialog.setContentView(R.layout.dialog_creating);
            mCreatingDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            mCreatingDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
            if (mCreatingView == null) {
                mCreatingView = (GifImageView) mCreatingDialog.findViewById(R.id.creating_view);
                ImageView creatingBar = (ImageView) mCreatingDialog.findViewById(R.id.creating_bar_bg);
                FrameLayout.LayoutParams param = (FrameLayout.LayoutParams) creatingBar.getLayoutParams();
                Drawable bg = getResources().getDrawable(R.drawable.facemoji_creating);
                param.width = (int) (bg.getIntrinsicWidth() * 0.8);
                param.height = (int) (bg.getIntrinsicHeight() * 0.8);
                creatingBar.setLayoutParams(param);
                WindowManager manager = getWindowManager();
                DisplayMetrics outMetrics = new DisplayMetrics();
                manager.getDefaultDisplay().getMetrics(outMetrics);
                ViewGroup.LayoutParams lp = mCreatingView.getLayoutParams();
                lp.height = lp.width = outMetrics.widthPixels / 2;
                mCreatingView.setLayoutParams(lp);

                // Creating thumbnail view
                View creatingThumbnailView = mCreatingDialog.findViewById(R.id.iv_creating_thumbnail);
                lp = creatingThumbnailView.getLayoutParams();
                lp.height = lp.width = outMetrics.widthPixels / 2;
                creatingThumbnailView.setLayoutParams(lp);
            }
            playAnimation();
            mCreatingDialog.show();
        } else {
            if (!mCreatingDialog.isShowing()) {
                mCreatingDialog.show();
                playAnimation();
            }
        }
    }

    /**
     * 异步任务处理图片
     */
    private class PictureProcessor extends AsyncTask<byte[], Void, Boolean> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            showProcessingDialog();
            findViewById(R.id.camera_click_button).setClickable(true);

        }

        @Override
        protected Boolean doInBackground(byte[][] params) {
            try {
                byte[] data = params[0];

                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = SAMPLE_SIZE;

                Bitmap src = BitmapFactory.decodeByteArray(data, 0, data.length, options);
                Bitmap des;
                Matrix matrix = new Matrix();

                //adjust picture
                if (currentCameraId == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                    //front camera   scale and rotate picture
                    matrix.postTranslate(src.getWidth(), 0);
                    matrix.setScale(-1, 1);
                    Bitmap modifiedBitmap = Bitmap.createBitmap(src, 0, 0, src.getWidth(), src.getHeight(), matrix, true);
                    src.recycle();

                    matrix.reset();
                    matrix.setRotate(getPreviewDegree(), modifiedBitmap.getWidth() / 2, modifiedBitmap.getHeight() / 2);
                    des = Bitmap.createBitmap(modifiedBitmap, 0, 0, modifiedBitmap.getWidth(), modifiedBitmap.getHeight(), matrix, true);
                    modifiedBitmap.recycle();
                } else {
                    //back camera  rotate picture
                    matrix.setRotate(getPreviewDegree(), src.getWidth() / 2, src.getHeight() / 2);
                    des = Bitmap.createBitmap(src, 0, 0, src.getWidth(), src.getHeight(), matrix, true);
                    src.recycle();
                }

                int cropHeight = Integer.valueOf(new String(params[2])) / SAMPLE_SIZE;
                boolean isTablet = getResources().getBoolean(R.bool.isTablet);
                if (isTablet) {
                    cropHeight = (int) (cropHeight * BitmapUtils.TABLET_WIDTH_SCALE_FACTOR);
                }

                matrix.reset();

                if (cropHeight + des.getWidth() > des.getHeight()) {
                    des = Bitmap.createBitmap(des, (des.getWidth() + cropHeight - des.getHeight()) / 2, cropHeight, des.getHeight() - cropHeight, des.getHeight() - cropHeight, matrix, true);
                } else {
                    int cropWidth = (int) (des.getWidth() * (isTablet ? BitmapUtils.TABLET_WIDTH_SCALE_FACTOR : 1));
                    des = Bitmap.createBitmap(des, (des.getWidth() - cropWidth) / 2, cropHeight, cropWidth, cropWidth, matrix, true);
                }

                String index = new String(params[1]);
                Bitmap mask = BitmapFactory.decodeResource(getResources(),
                        getResources().getIdentifier("drawable/facemask" + index + "_black", null, getPackageName()));
                matrix.setScale(mask.getWidth() * 1.0f / des.getWidth(), mask.getHeight() * 1.0f / des.getHeight());
                Bitmap finalPic = Bitmap.createBitmap(des, 0, 0, des.getWidth(), des.getHeight(), matrix, true);
                des.recycle();

                //get face
                boolean face = dealBitmapWithMask(finalPic, mask);

                //reduce memory
                if (!mask.isRecycled()) {
                    mask.recycle();
                }
                if (!finalPic.isRecycled()) {
                    finalPic.recycle();
                }

                return face;

            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {

            if (result) {
                Intent i = new Intent(CameraActivity.this, MyFacemojiActivity.class);
                startActivity(i);
            } else {
                camera.startPreview();
            }
        }
    }

    /**
     * 关闭等待对话框
     */
    private void closeProcessingDialog() {
        //close dialog
        if (mCreatingDialog != null) {
            mCreatingDialog.dismiss();
        }
    }

    private static final int SELECT_PIC = 5566;
    private static final int FACE_LIST_COUNT = 5;
    private static final String TEMP_FACE_FILE_NAME = "face.png";
    private Camera camera;
    private SurfaceView surfaceView;
    private ImageView photoView;
    private int currentCameraId = Camera.CameraInfo.CAMERA_FACING_FRONT;
    private Dialog mCreatingDialog;
    private GifImageView mCreatingView;
    private boolean isSynthesisingImage;
    private Button choose_pic_click_button;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT < 16) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
        setContentView(R.layout.camera_activity);
        hideStatusBar();

        // 照相预览界面
        surfaceView = (SurfaceView) findViewById(R.id.preview);
        surfaceView.getHolder().setKeepScreenOn(true);// 屏幕常亮
        surfaceView.getHolder().addCallback(new SurfaceCallback());//为SurfaceView的句柄添加一个回调函数

        photoView = (ImageView) findViewById(R.id.photo_view);
        photoView.setVisibility(View.INVISIBLE);

        choose_pic_click_button = (Button) findViewById(R.id.choose_pic_click_button);
        choose_pic_click_button.setOnClickListener(new View.OnClickListener() {
                                                       @Override
                                                       public void onClick(View v) {
                                                           showProcessingDialog();
                                                           handler.sendEmptyMessageDelayed(1, 1000);
                                                       }
                                                   }
        );


        //relayout
        View faceTitleBar = findViewById(R.id.face_title_bar);
        faceTitleBar.setBackgroundColor(Color.BLACK);
        faceTitleBar.setAlpha(0.85f);
        RelativeLayout.LayoutParams faceTitlePara = (RelativeLayout.LayoutParams) faceTitleBar.getLayoutParams();
        faceTitlePara.height = (DisplayUtils.getScreenHeightForContent() - DisplayUtils.getStatusBarHeight(getWindow())) / 10;
        faceTitleBar.setLayoutParams(faceTitlePara);

        ImageView switcher = (ImageView) findViewById(R.id.camera_switcher);
        LinearLayout.LayoutParams switcherPara = (LinearLayout.LayoutParams) switcher.getLayoutParams();
        switcherPara.height = (int) (getResources().getDrawable(R.drawable.change_camera).getIntrinsicHeight() * 0.8f);
        switcherPara.width = (int) (getResources().getDrawable(R.drawable.change_camera).getIntrinsicWidth() * 0.8f);
        switcher.setLayoutParams(switcherPara);

        View switcherHolder = findViewById(R.id.switcher_holder);
        switcherHolder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                releaseCamera();

                currentCameraId++;
                currentCameraId %= 2;
                startCameraAndPreview(surfaceView.getHolder());
                String derection = currentCameraId == 1 ? "front" : "rear";
            }
        });

        ImageView back = (ImageView) findViewById(R.id.face_back);
        LinearLayout.LayoutParams backPara = (LinearLayout.LayoutParams) back.getLayoutParams();
        backPara.height = (int) (getResources().getDrawable(R.drawable.back_button).getIntrinsicHeight() * 0.8f);
        backPara.width = (int) (getResources().getDrawable(R.drawable.back_button).getIntrinsicWidth() * 0.8f);
        back.setLayoutParams(backPara);

        View backHolder = findViewById(R.id.back_holder);
        backHolder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent pre = getIntent();
                if (pre.getBooleanExtra("FaceGridAdapter", false)) {
                    Intent i = new Intent(CameraActivity.this, FaceListActivity.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(i);
                }

                // Release camera and finish
                onBackPressed();
            }
        });

        ImageView maskView = (ImageView) findViewById(R.id.mask_view);

        boolean isTablet = getResources().getBoolean(R.bool.isTablet);

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        BitmapFactory.decodeResource(getResources(), R.drawable.facemask1, options);
        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) maskView.getLayoutParams();
        lp.width = DisplayUtils.getScreenWidthForContent();
        lp.height = (int) (lp.width * options.outHeight * (isTablet ? BitmapUtils.TABLET_WIDTH_SCALE_FACTOR : 1) / options.outWidth);
        maskView.setLayoutParams(lp);

        int statusBarHeight = DisplayUtils.getStatusBarHeight(getWindow());
        int screenHeight = DisplayUtils.getScreenHeightForContent();

        HorizontalScrollView scrollView = (HorizontalScrollView) findViewById(R.id.face_list);
        RelativeLayout.LayoutParams scrollViewPara = (RelativeLayout.LayoutParams) scrollView.getLayoutParams();

        TextView textView = (TextView) findViewById(R.id.fill_text);
        RelativeLayout.LayoutParams textPara = (RelativeLayout.LayoutParams) textView.getLayoutParams();


        Button camera = (Button) findViewById(R.id.camera_click_button);
        LinearLayout.LayoutParams cameraPara = (LinearLayout.LayoutParams) camera.getLayoutParams();
        cameraPara.height = (int) (0.65f * (screenHeight - statusBarHeight - faceTitlePara.height - lp.height - scrollViewPara.height - textPara.height));
        cameraPara.width = cameraPara.height;
        camera.setLayoutParams(cameraPara);
        camera.setClickable(true);

        ImageView gallery = (ImageView) findViewById(R.id.gallery);
        LinearLayout.LayoutParams galleryPara = (LinearLayout.LayoutParams) gallery.getLayoutParams();
        galleryPara.height = (int) (0.25f * (screenHeight - statusBarHeight - faceTitlePara.height - lp.height - scrollViewPara.height - textPara.height));
        galleryPara.width = galleryPara.height * gallery.getBackground().getIntrinsicWidth() / gallery.getBackground().getIntrinsicHeight();
        gallery.setLayoutParams(galleryPara);

        ImageView gallery2 = (ImageView) findViewById(R.id.gallery2);
        LinearLayout.LayoutParams galleryPara2 = (LinearLayout.LayoutParams) gallery2.getLayoutParams();
        galleryPara2.height = (int) (0.25f * (screenHeight - statusBarHeight - faceTitlePara.height - lp.height - scrollViewPara.height - textPara.height));
        galleryPara2.width = galleryPara2.height * gallery2.getBackground().getIntrinsicWidth() / gallery2.getBackground().getIntrinsicHeight();
        gallery2.setLayoutParams(galleryPara2);

        Button choosePic = (Button) findViewById(R.id.choose_pic_click_button);
        LinearLayout.LayoutParams choosePicPram = (LinearLayout.LayoutParams) choosePic.getLayoutParams();
        choosePicPram.height = (int) (0.65f * (screenHeight - statusBarHeight - faceTitlePara.height - lp.height - scrollViewPara.height - textPara.height));
        choosePicPram.width = choosePicPram.height;
        choosePic.setLayoutParams(choosePicPram);

        initFaceListView();

        surfaceView.setVisibility(View.VISIBLE);
        photoView.setVisibility(View.INVISIBLE);
        photoView.setImageDrawable(null);
        View synthesis = findViewById(R.id.synthesis_picture);
        synthesis.setVisibility(View.INVISIBLE);

        HSGlobalNotificationCenter.addObserver(FACEMOJI_SAVED, mImeActionObserver);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();

        hideStatusBar();

        try {
            startCameraAndPreview(surfaceView.getHolder());
        } catch (RuntimeException e) {
            e.printStackTrace();
            finish();
            return;
        } catch (Exception e) {
            e.printStackTrace();
            finish();
            return;
        }

        if (!isSynthesisingImage) {
            surfaceView.setVisibility(View.VISIBLE);
            View switcherHolder = findViewById(R.id.switcher_holder);
            switcherHolder.setVisibility(View.VISIBLE);
            photoView.setVisibility(View.INVISIBLE);
            photoView.setImageDrawable(null);
            View synthesis = findViewById(R.id.synthesis_picture);
            synthesis.setVisibility(View.INVISIBLE);
        }
        findViewById(R.id.camera_click_button).setClickable(true);
        closeProcessingDialog();
    }

    @Override
    protected void onStop() {
        closeProcessingDialog();
        super.onStop();
    }

    @Override
    public void onBackPressed() {
        releaseCamera();
        super.onBackPressed();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_CAMERA: // 按下拍照按钮
                if (camera != null && event.getRepeatCount() == 0) {
                    takePicture(null);
                    findViewById(R.id.camera_click_button).setClickable(false);
                }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (RESULT_OK != resultCode) {
            photoView.setImageDrawable(null);
            photoView.setVisibility(View.INVISIBLE);
            View synthesis = findViewById(R.id.synthesis_picture);
            synthesis.setVisibility(View.INVISIBLE);
            surfaceView.setVisibility(View.VISIBLE);
            View switcherHolder = findViewById(R.id.switcher_holder);
            switcherHolder.setVisibility(View.VISIBLE);
            return;
        }

        switch (requestCode) {
            case SELECT_PIC:

                Matrix matrix = new Matrix();
                photoView.setImageMatrix(matrix);

                Uri uri = data.getData();
                //根据Uri获取文件路径
                String filePath = UriUtils.getImageAbsolutePath(this, uri);

                //压缩图片到屏幕尺寸
                Bitmap bitmap = BitmapUtils.compressBitmap(filePath, DisplayUtils.getScreenWidthForContent(), DisplayUtils.getScreenHeightForContent());

                photoView.setImageBitmap(bitmap);
                photoView.setOnTouchListener(new FaceTouchListener(photoView));

                break;
            default:
                break;
        }

    }

    private void initFaceListView() {

        final LinearLayout faceListView = (LinearLayout) findViewById(R.id.face_list_view);
        faceListView.setBackgroundColor(Color.BLACK);
        faceListView.setAlpha(0.85f);
        int faceWidth = 0;
        int target = 0;

        for (int i = 0; i < FACE_LIST_COUNT; i++) {
            ImageView iv = new ImageView(this);
            iv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    final LinearLayout faceListView = (LinearLayout) findViewById(R.id.face_list_view);
                    int index = faceListView.indexOfChild(v);
                    String uri = "drawable/face" + index + "_choosen";
                    int resourceId = getResources().getIdentifier(uri, null, getPackageName());
                    v.setBackgroundResource(resourceId);

                    ImageView maskView = (ImageView) findViewById(R.id.mask_view);
                    RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) maskView.getLayoutParams();
                    int resId = getResources().getIdentifier("drawable/facemask" + (index + 1), null, getPackageName());
                    boolean isTablet = getResources().getBoolean(R.bool.isTablet);
                    if (isTablet) {
                        Bitmap faceMask = BitmapUtils.drawMaskView(BitmapFactory.decodeResource(HSApplication.getContext().getResources(), resId), lp.width, lp.height);
                        maskView.setImageBitmap(faceMask);
                    } else {
                        maskView.setBackgroundResource(resId);
                    }


                    maskView.setTag("" + (index + 1));

                    for (int k = 0; k < faceListView.getChildCount(); k++) {
                        if (k == index) {
                            continue;
                        }

                        faceListView.getChildAt(k).setBackgroundResource(getResources().getIdentifier("drawable/face" + k, null, getPackageName()));
                    }

                }
            });

            HorizontalScrollView scrollView = (HorizontalScrollView) findViewById(R.id.face_list);
            RelativeLayout.LayoutParams scrollViewPara = (RelativeLayout.LayoutParams) scrollView.getLayoutParams();

            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(20, 20);
            layoutParams.setMargins((int) getResources().getDimension(R.dimen.facemoji_face_list_left_margin), 0,
                    (int) getResources().getDimension(R.dimen.facemoji_face_list_left_margin), (int) getResources().getDimension(R.dimen.facemoji_face_list_bottom_margin));

            String uri = "drawable/face" + i;
            int resourceId = getResources().getIdentifier(uri, null, getPackageName());

            iv.setBackgroundResource(resourceId);
            layoutParams.height = (int) (scrollViewPara.height * 0.8);
            layoutParams.width = layoutParams.height * iv.getBackground().getIntrinsicWidth() / iv.getBackground().getIntrinsicHeight();
            iv.setLayoutParams(layoutParams);

            faceListView.addView(iv);

            faceWidth += layoutParams.width + 2 * (int) getResources().getDimension(R.dimen.facemoji_face_list_left_margin);
            if (faceWidth < DisplayUtils.getScreenWidthForContent()) {
                target = i;
            }
        }

        if (faceWidth < DisplayUtils.getScreenWidthForContent()) {
            faceListView.setGravity(Gravity.CENTER_HORIZONTAL);
        }

        ImageView maskView = (ImageView) findViewById(R.id.mask_view);
        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) maskView.getLayoutParams();
        int resId = getResources().getIdentifier("drawable/facemask" + (target / 2 + 1), null, getPackageName());
        boolean isTablet = getResources().getBoolean(R.bool.isTablet);
        if (isTablet) {
            Bitmap faceMask = BitmapUtils.drawMaskView(BitmapFactory.decodeResource(HSApplication.getContext().getResources(), resId), lp.width, lp.height);
            maskView.setImageBitmap(faceMask);
        } else {
            maskView.setBackgroundResource(resId);
        }
        maskView.setTag("" + (target / 2 + 1));

        faceListView.getChildAt(target / 2).setBackgroundResource(getResources().getIdentifier("drawable/face" + (target / 2) + "_choosen", null, getPackageName()));
    }

    private void startCameraAndPreview(final SurfaceHolder holder) {
        releaseCamera();

        int cameraId = -1;
        int numberOfCameras = Camera.getNumberOfCameras();
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == currentCameraId) {
                cameraId = i;
                break;
            }
        }

        if (cameraId < 0) {
            // TODO: 4/16/16 inform user that camera can not be connected
            return;
        }

        camera = Camera.open(cameraId); // 打开摄像头
        final Camera.Parameters parameters = camera.getParameters();
        final FrameLayout.LayoutParams surfaceViewLayoutParams = (FrameLayout.LayoutParams) surfaceView.getLayoutParams();

        surfaceViewLayoutParams.width = DisplayUtils.getScreenWidthForContent();
        surfaceViewLayoutParams.height = parameters.getPreviewSize().width * surfaceViewLayoutParams.width / parameters.getPreviewSize().height;
        surfaceView.setLayoutParams(surfaceViewLayoutParams);

        Camera.Size pictureSize = getOptimalCameraSize(parameters.getSupportedPictureSizes(), surfaceViewLayoutParams.width, surfaceViewLayoutParams.height);
        parameters.setPictureSize(pictureSize.width, pictureSize.height);
        camera.setParameters(parameters);

        try {
            camera.setPreviewDisplay(holder); // 设置用于显示拍照影像的SurfaceHolder对象
        } catch (IOException e) {
            e.printStackTrace();
        }

        camera.setDisplayOrientation(getPreviewDegree());
        camera.startPreview(); // 开始预览
    }

    private void releaseCamera() {
        if (null != camera) {
            camera.stopPreview();
            camera.release();
            camera = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        HSGlobalNotificationCenter.removeObserver(mImeActionObserver);
    }

    private Camera.Size getOptimalCameraSize(List<Camera.Size> sizes, int w, int h) {
        double ASPECT_TOLERANCE = 0.1;
        double targetRatio = (double) h / w;

        if (sizes == null) return null;

        Camera.Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        int targetHeight = h;

        for (Camera.Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) {
                continue;
            }
            if (Math.abs(size.height - targetHeight) <= minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
                ASPECT_TOLERANCE = Math.abs(ratio - targetRatio);
            }
        }

        double tolerance = Double.MAX_VALUE;

        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : sizes) {
                double ratio = (double) size.width / size.height;
                if (Math.abs(ratio - targetRatio) > tolerance) {
                    continue;
                }

                tolerance = Math.abs(ratio - targetRatio);
                if (Math.abs(size.height - targetHeight) <= minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        return optimalSize;
    }

    private void hideStatusBar() {
        View decorView = getWindow().getDecorView();
        // Hide the status bar.
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
    }

    /**
     * 将拍下来的照片存放在SD卡中
     *
     * @param face
     */
    private static void saveToSDCard(Bitmap face, File pngFile) {

        FileOutputStream outputStream = null; // 文件输出流
        try {
            outputStream = new FileOutputStream(pngFile);
            face.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            outputStream.close(); // 关闭输出流
        } catch (IOException e) {
            e.printStackTrace();
        }
        FacemojiManager.getInstance().loadFaceList();
        FacemojiManager.setCurrentFacePicUri(Uri.fromFile(pngFile));
    }

    /**
     * 将拍下来的照片存放在SD卡中
     *
     * @param face
     */
    private static void saveToSDCardTempFile(Bitmap face, File pngFile) {
        FileOutputStream outputStream = null; // 文件输出流
        try {
            outputStream = new FileOutputStream(pngFile);
            face.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            outputStream.close(); // 关闭输出流
        } catch (IOException e) {
            e.printStackTrace();
        }

        FacemojiManager.setTempFacePicUri(Uri.fromFile(pngFile));
    }

    private String getHairStyle() {
        final ImageView maskView = (ImageView) findViewById(R.id.mask_view);
        int index = Integer.parseInt(maskView.getTag().toString());
        String result;
        switch (index) {
            case 1:
                result = "normal";
            case 2:
                result = "male1";
            case 3:
                result = "male2";
            case 4:
                result = "female1";
            case 5:
                result = "female2";
            default:
                result = "normal";

        }
        return result;
    }

    public static String getTempFaceFilePath() {
        return HSPictureManager.getTempDirectory() + TEMP_FACE_FILE_NAME;
    }

    public boolean dealBitmapWithMask(Bitmap bmp, Bitmap mask) {

        //crop the picture
        Bitmap crop = Bitmap.createBitmap(bmp.getWidth(), bmp.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas mCanvas = new Canvas(crop);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.XOR));
        mCanvas.drawBitmap(bmp, 0, 0, paint);
        mCanvas.drawBitmap(mask, 0, 0, paint);
        paint.setXfermode(null);

        //save face picture to 200x200
        Matrix matrix = new Matrix();
        matrix.setScale(200.0f / crop.getWidth(), 200.0f / crop.getHeight());
        final Bitmap saved = Bitmap.createBitmap(crop, 0, 0, crop.getWidth(), crop.getWidth(), matrix, true);

        // 存储路径
        String filePath = getTempFaceFilePath();
        HSLog.d(filePath);


        saveToSDCardTempFile(saved, HSFileUtils.createNewFile(filePath));

        Bitmap picture = BitmapUtils.addBorder(crop, Color.WHITE);

        if (!crop.isRecycled()) {
            crop.recycle();
        }
        saved.recycle();

        boolean result = (null != picture);

        picture.recycle();

        return result;
    }

    public int getPreviewDegree() {
        int rotation = getWindowManager().getDefaultDisplay()
                .getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }

        int result;
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(currentCameraId, info);
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }

        return result;
    }

    public void takePicture(View v) {
        try {
            if (camera != null) {
                camera.takePicture(null, null, new MyPictureCallback());
                findViewById(R.id.camera_click_button).setClickable(false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showGallery(View v) {
        Intent openAlbumIntent = new Intent(Intent.ACTION_GET_CONTENT);// ACTION_OPEN_DOCUMENT
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            openAlbumIntent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
            startActivityForResult(openAlbumIntent, CameraActivity.SELECT_PIC);
        } else {
            openAlbumIntent.addCategory(Intent.CATEGORY_OPENABLE);
            openAlbumIntent.setType("image/jpeg");
            startActivityForResult(openAlbumIntent, CameraActivity.SELECT_PIC);
        }

        photoView.setVisibility(View.VISIBLE);

        releaseCamera();
        View synthesis = findViewById(R.id.synthesis_picture);
        synthesis.setVisibility(View.VISIBLE);
        View switcherHolder = findViewById(R.id.switcher_holder);
        switcherHolder.setVisibility(View.INVISIBLE);
        surfaceView.setVisibility(View.INVISIBLE);
        isSynthesisingImage = true;
    }

    Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    synthesisImage(choose_pic_click_button);
                    break;
            }
        }
    };


    public void synthesisImage(View v) {
        View view = getWindow().getDecorView();
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache();
        Bitmap activityShot = view.getDrawingCache();

        int[] win = new int[2];
        int[] scr = new int[2];
        v.getLocationInWindow(win);
        v.getLocationOnScreen(scr);
        int statusBarHeight = Math.abs(win[1] - scr[1]);


        View titleBar = findViewById(R.id.face_title_bar);
        RelativeLayout.LayoutParams titleBarPara = (RelativeLayout.LayoutParams) titleBar.getLayoutParams();

        ImageView maskView = (ImageView) findViewById(R.id.mask_view);
        boolean isTablet = getResources().getBoolean(R.bool.isTablet);

        int cropWidth = maskView.getWidth();
        if (isTablet) {
            cropWidth = maskView.getHeight();
        }

        Bitmap activityShotRet = Bitmap.createBitmap(activityShot, (maskView.getWidth() - cropWidth) / 2, statusBarHeight + titleBarPara.height, cropWidth, maskView.getHeight());
        view.destroyDrawingCache();
        activityShot.recycle();

        Bitmap mask = BitmapFactory.decodeResource(getResources(),
                getResources().getIdentifier("drawable/facemask" + maskView.getTag() + "_black", null, getPackageName()));
        Matrix matrix = new Matrix();
        matrix.setScale(mask.getWidth() * 1.0f / activityShotRet.getWidth(), mask.getHeight() * 1.0f / activityShotRet.getHeight());
        Bitmap face = Bitmap.createBitmap(activityShotRet, 0, 0, activityShotRet.getWidth(), activityShotRet.getHeight(), matrix, true);

        activityShotRet.recycle();
        dealBitmapWithMask(face, mask);

        mask.recycle();
        face.recycle();

        showProcessingDialog();

        //跳转页面
        Intent i = new Intent(CameraActivity.this, MyFacemojiActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
        isSynthesisingImage = false;
    }
}