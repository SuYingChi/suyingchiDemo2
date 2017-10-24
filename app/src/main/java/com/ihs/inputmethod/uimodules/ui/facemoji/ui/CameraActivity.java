package com.ihs.inputmethod.uimodules.ui.facemoji.ui;

import android.app.Dialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.ColorDrawable;
import android.hardware.Camera;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
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
import com.ihs.feature.common.VectorCompat;
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

import jp.co.cyberagent.android.gpuimage.GPUImage;
import jp.co.cyberagent.android.gpuimage.MagicBeautyFilter;
import pl.droidsonroids.gif.GifImageView;


public class CameraActivity extends HSAppCompatActivity {
    public static final String FACEMOJI_SAVED = "FACEMOJI_SAVED";
    public static final String FACE_CHANGED = "FACE_CHANGED";
    public static final String FACE_DELETED = "FACE_DELETED";
    private static final int SAMPLE_SIZE = 8;
    private Bitmap srcBitmap;
    private Bitmap beautyBitmap;
    private boolean useBeautyNow = true;

    private View cameraLayout;

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
            Bitmap src = BitmapFactory.decodeByteArray(data, 0, data.length);
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

            synthesisingPhoto(des);
        }
    }


    private final class SurfaceCallback implements SurfaceHolder.Callback {

        // 拍照状态变化时调用该方法
        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            camera.startPreview();
            final Camera.Parameters parameters = camera.getParameters();
            final ViewGroup.LayoutParams previewContainerLayoutParams =  previewContainer.getLayoutParams();
            Camera.Size size = getOptimalCameraSize(parameters.getSupportedPictureSizes(), previewContainerLayoutParams.width, previewContainerLayoutParams.height);
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
                WindowManager manager = getWindowManager();
                DisplayMetrics outMetrics = new DisplayMetrics();
                manager.getDefaultDisplay().getMetrics(outMetrics);
                ViewGroup.LayoutParams lp = mCreatingView.getLayoutParams();
                lp.height = lp.width = outMetrics.widthPixels / 2;
                mCreatingView.setLayoutParams(lp);
            }
            mCreatingView.setImageURI(Uri.parse("android.resource://" + HSApplication.getContext().getPackageName() + "/" + R.raw.creating));
            mCreatingDialog.show();
        } else {
            if (!mCreatingDialog.isShowing()) {
                mCreatingView.setImageURI(Uri.parse("android.resource://" + HSApplication.getContext().getPackageName() + "/" + R.raw.creating));
                mCreatingDialog.show();
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
            cameraLayout.setClickable(true);

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
    private FrameLayout previewContainer;
    private int currentCameraId = Camera.CameraInfo.CAMERA_FACING_FRONT;
    private Dialog mCreatingDialog;
    private GifImageView mCreatingView;
    private boolean isSynthesisingImage;
    private View confirmMakeFaceBtn;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT < 16) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
        setContentView(R.layout.camera_activity);
        hideStatusBar();
        initView();


        HSGlobalNotificationCenter.addObserver(FACEMOJI_SAVED, mImeActionObserver);
    }

    private void initView() {
        // 照相预览界面
        surfaceView = (SurfaceView) findViewById(R.id.preview);
        surfaceView.getHolder().setKeepScreenOn(true);// 屏幕常亮
        surfaceView.getHolder().addCallback(new SurfaceCallback());//为SurfaceView的句柄添加一个回调函数

        photoView = (ImageView) findViewById(R.id.photo_view);
        photoView.setVisibility(View.INVISIBLE);

        previewContainer = (FrameLayout) findViewById(R.id.preview_container);

        //relayout
        View faceTitleBar = findViewById(R.id.face_title_bar);
        faceTitleBar.setBackgroundColor(Color.BLACK);
        faceTitleBar.setAlpha(0.85f);
        RelativeLayout.LayoutParams faceTitlePara = (RelativeLayout.LayoutParams) faceTitleBar.getLayoutParams();
        faceTitlePara.height = (DisplayUtils.getScreenHeightForContent() - DisplayUtils.getStatusBarHeight(getWindow())) / 10;
        faceTitleBar.setLayoutParams(faceTitlePara);

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

        View backBtn = findViewById(R.id.back);
        backBtn.setOnClickListener(new View.OnClickListener() {
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

        int captureBtnHeight = (int) (0.65f * (screenHeight - statusBarHeight - faceTitlePara.height - lp.height - scrollViewPara.height - textPara.height));

        cameraLayout = findViewById(R.id.camera_click_button);
        LinearLayout.LayoutParams cameraPara = (LinearLayout.LayoutParams) cameraLayout.getLayoutParams();
        cameraPara.height = captureBtnHeight;
        cameraPara.width = captureBtnHeight;
        cameraLayout.setBackgroundDrawable(com.ihs.inputmethod.uimodules.utils.RippleDrawableUtils.getCompatCircleRippleDrawable(getResources().getColor(R.color.colorPrimary), 0));
        cameraLayout.setLayoutParams(cameraPara);
        cameraLayout.setClickable(true);

        confirmMakeFaceBtn = findViewById(R.id.confirm_make_face);
        LinearLayout.LayoutParams choosePicPram = (LinearLayout.LayoutParams) confirmMakeFaceBtn.getLayoutParams();
        choosePicPram.height = captureBtnHeight;
        choosePicPram.width = captureBtnHeight;
        confirmMakeFaceBtn.setLayoutParams(choosePicPram);
        confirmMakeFaceBtn.setBackgroundDrawable(com.ihs.inputmethod.uimodules.utils.RippleDrawableUtils.getCompatCircleRippleDrawable(getResources().getColor(R.color.colorPrimary), 0));
        confirmMakeFaceBtn.setOnClickListener(new View.OnClickListener() {
                                                  @Override
                                                  public void onClick(View v) {
                                                      showProcessingDialog();
                                                      handler.sendEmptyMessageDelayed(1, 1000);
                                                  }
                                              }
        );

        initFaceListView();

        surfaceView.setVisibility(View.VISIBLE);
        photoView.setVisibility(View.INVISIBLE);
        photoView.setOnTouchListener(new FaceTouchListener(photoView));
        photoView.setImageDrawable(null);
        View synthesis = findViewById(R.id.synthesis_picture);
        synthesis.setVisibility(View.INVISIBLE);
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
        cameraLayout.setClickable(true);
        closeProcessingDialog();
    }

    @Override
    protected void onStop() {
        closeProcessingDialog();
        super.onStop();
    }

    @Override
    public void onBackPressed() {
        if (isSynthesisingImage) {
            backToCaptureStatus();
        } else {
            releaseCamera();
            super.onBackPressed();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_CAMERA: // 按下拍照按钮
                if (camera != null && event.getRepeatCount() == 0) {
                    takePicture(null);
                    cameraLayout.setClickable(false);
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
                synthesisingPhoto(bitmap);
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
        final ViewGroup.LayoutParams previewContainerLayoutParams = previewContainer.getLayoutParams();

        previewContainerLayoutParams.width = DisplayUtils.getScreenWidthForContent();
        previewContainerLayoutParams.height = parameters.getPreviewSize().width * previewContainerLayoutParams.width / parameters.getPreviewSize().height;

        previewContainer.setLayoutParams(previewContainerLayoutParams);

        Camera.Size pictureSize = getOptimalCameraSize(parameters.getSupportedPictureSizes(), previewContainerLayoutParams.width, previewContainerLayoutParams.height);
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
                cameraLayout.setClickable(false);
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

        releaseCamera();
    }

    private void synthesisingPhoto(Bitmap bitmap) {
        isSynthesisingImage = true;

        ImageView beautyBtn = (ImageView) findViewById(R.id.beauty_button);
        VectorDrawableCompat beautyDrawable = VectorCompat.createVectorDrawable(HSApplication.getContext(), R.drawable.ic_beauty_black_24dp);
        DrawableCompat.setTintList(beautyDrawable, new ColorStateList(
                new int[][]
                        {
                                new int[]{android.R.attr.state_selected},
                                new int[]{}
                        },
                new int[]
                        {
                                getResources().getColor(R.color.colorPrimary),
                                getResources().getColor(R.color.standard_gray)
                        }
        ));
        beautyBtn.setImageDrawable(beautyDrawable);
        beautyBtn.setSelected(useBeautyNow);

        GPUImage gpuImage = new GPUImage(CameraActivity.this);
        MagicBeautyFilter magicBeautyFilter = new MagicBeautyFilter(CameraActivity.this);
        magicBeautyFilter.setBeautyLevel(4);
        gpuImage.setFilter(magicBeautyFilter);
        srcBitmap = bitmap;
        beautyBitmap = gpuImage.getBitmapWithFilterApplied(srcBitmap);

        float scale = (float)previewContainer.getWidth()/ bitmap.getWidth();
        Matrix matrix = new Matrix();
        matrix.setScale(scale,scale);
        photoView.setImageMatrix(matrix);
        photoView.setImageBitmap(useBeautyNow ? beautyBitmap : srcBitmap);

        updateUIIfStatusChange();
    }

    private void updateUIIfStatusChange() {
        cameraLayout.setClickable(!isSynthesisingImage);

        photoView.setVisibility(isSynthesisingImage ? View.VISIBLE : View.GONE);

        View synthesis = findViewById(R.id.synthesis_picture);
        synthesis.setVisibility(isSynthesisingImage ? View.VISIBLE : View.GONE);

        View switcherHolder = findViewById(R.id.switcher_holder);
        switcherHolder.setVisibility(isSynthesisingImage ? View.INVISIBLE : View.VISIBLE);

        surfaceView.setVisibility(isSynthesisingImage ? View.INVISIBLE : View.VISIBLE);
    }

    private void backToCaptureStatus() {
        isSynthesisingImage = false;
        updateUIIfStatusChange();
    }

    Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    synthesisImage(confirmMakeFaceBtn);
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

    public void useBeauty(View v) {
        useBeautyNow = !useBeautyNow;
        v.setSelected(useBeautyNow);
        photoView.setImageBitmap(useBeautyNow ? beautyBitmap : srcBitmap);
    }
}