package cn.mdruby.pickphotovideoview.camera.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;

import net.lucode.hackware.magicindicator.MagicIndicator;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.CommonNavigator;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.CommonNavigatorAdapter;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerIndicator;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerTitleView;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.titles.SimplePagerTitleView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import cn.mdruby.pickphotovideoview.R;
import cn.mdruby.pickphotovideoview.camera.AppConfig;
import cn.mdruby.pickphotovideoview.camera.AppConstant;
import cn.mdruby.pickphotovideoview.camera.BitmapUtils;
import cn.mdruby.pickphotovideoview.camera.CameraFragment;
import cn.mdruby.pickphotovideoview.camera.CameraUtil;
import cn.mdruby.pickphotovideoview.camera.SystemUtils;

public class CameraVideoActivity extends AppCompatActivity implements SurfaceHolder.Callback{
    private static final int CAMERA_REQUEST_CODE = 0X787;
    private MagicIndicator mMagicIndicator;
    private ImageView mIVStart;
    private ImageView mIVShowPhoto;
    private CameraFragment.OnFragmentInteractionListener mListener;
    public static final String MEDIA_IMAGE_TYPE = "image";
    public static final String MEDIA_VIDEO_TYPE = "video";
    private Camera mCamera;
    private SurfaceView surfaceView;
    private SurfaceHolder mHolder;
    private int mCameraId = 0;
    private Context context;
    private boolean isVideo = false;
    private boolean videoStarted = false;
    private float startX = 0;

    //屏幕宽高
    private int screenWidth;
    private int screenHeight;
    private int index;
    //底部高度 主要是计算切换正方形时的动画高度
    private int menuPopviewHeight;
    //动画高度
    private int animHeight;
    //闪光灯模式 0:关闭 1: 开启 2: 自动
    private int light_num = 0;
    //延迟时间
    private int delay_time;
    private int delay_time_temp;
    private boolean isview = false;
    private boolean is_camera_delay;
    private ImageView camera_frontback;
    private ImageView camera_close;
    private RelativeLayout homecamera_bottom_relative;
    private ImageView img_camera;
    private int picHeight;

    private File file;
    private String pathName;
    private int video_width;
    private int video_height;
    private CamcorderProfile profile;
    private File file2;
    private int recorderRotation;
    private MediaRecorder mediaRecorder;

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            int what = msg.what;
            switch (what) {
                case AppConstant.WHAT.SUCCESS:
                    if (delay_time > 0) {
                    }

                    try {
                        if (delay_time == 0) {
                            captrue();
                            is_camera_delay = false;
                        }
                    } catch (Exception e) {
                        return;
                    }

                    break;

                case AppConstant.WHAT.ERROR:
                    is_camera_delay = false;
                    break;

            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_video);
        context = this;
        mMagicIndicator = (MagicIndicator) findViewById(R.id.act_camera_video_MagicIndicator);
        surfaceView = (SurfaceView) findViewById(R.id.act_camera_video_SurfaceView);
        mIVStart = (ImageView) findViewById(R.id.act_camera_video_IV_start);
        mIVShowPhoto = (ImageView) findViewById(R.id.act_camera_video_IV_showPhoto);
        mHolder = surfaceView.getHolder();
        initData();
        requestPermission();
        setListener();
        mIVStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isVideo){
//                    StartListener();
                    if (mCamera == null) {
                        mCamera = getCamera(mCameraId);
                        if (mHolder != null) {
                            startPreview(mCamera, mHolder);
                        }
                    }
                    captrue();
                }else {
                    if (!videoStarted){
                        recorderRotation = CameraUtil.getInstance().getRecorderRotation(mCameraId);
                        setupCameraVideo(mCamera);
                        startVideo();
                    }else {
                        endVideoRecord();
                    }
                }
            }
        });

      surfaceView.setOnTouchListener(new View.OnTouchListener() {
          @Override
          public boolean onTouch(View view, MotionEvent motionEvent) {
              switch (motionEvent.getAction()){
                  case MotionEvent.ACTION_DOWN:
                      startX = motionEvent.getX();
                      break;
                  case MotionEvent.ACTION_MOVE:
                      float x = motionEvent.getX();
                      if (x-startX<-40){
                          isVideo = true;
                          mMagicIndicator.onPageSelected(1);
                      }
                      if (x-startX>40){
                          isVideo = false;
                          mMagicIndicator.onPageSelected(0);
                      }
                      break;
              }
              return true;
          }
      });
    }

    private void requestPermission() {
        //判断是否开启摄像头权限
        if ((ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
        &&(ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED)) {
            StartListener();

            //判断是否开启语音权限
        } else {
            //请求获取摄像头权限
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA,Manifest.permission.RECORD_AUDIO}, CAMERA_REQUEST_CODE);
        }
    }

    /**
     * 请求权限回调
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_REQUEST_CODE) {
            if ((grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                StartListener();
            } else {
                Toast.makeText(this, "已拒绝权限！", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void initData() {
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        screenWidth = dm.widthPixels;
        screenHeight = dm.heightPixels;

        menuPopviewHeight = screenHeight - screenWidth * 4 / 3;
        animHeight = (screenHeight - screenWidth - menuPopviewHeight - SystemUtils.dp2px(context, 44)) / 2;

        //这里相机取景框我这是为宽高比3:4 所以限制底部控件的高度是剩余部分
        RelativeLayout.LayoutParams bottomParam = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, menuPopviewHeight);
        bottomParam.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
//        homecamera_bottom_relative.setLayoutParams(bottomParam);
    }

    private void StartListener() {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mCamera == null) {
                    mCamera = getCamera(mCameraId);
                    if (mHolder != null) {
                        startPreview(mCamera, mHolder);
                    }
                }
            }
        },500);
    }

    protected void startVideo() {
        try {
            pathName = System.currentTimeMillis() + "";
            //视频存储路径
            file = new File(Environment.getExternalStorageDirectory() + File.separator + pathName + AppConfig.MP4);

            //如果没有要创建
            BitmapUtils.makeDir(file);

            //初始化一个MediaRecorder
            if (mediaRecorder == null) {
                mediaRecorder = new MediaRecorder();
            } else {
                mediaRecorder.reset();
            }

//
            mCamera.unlock();
            mediaRecorder.setCamera(mCamera);
            //设置视频输出的方向 很多设备在播放的时候需要设个参数 这算是一个文件属性
            mediaRecorder.setOrientationHint(recorderRotation);

            //视频源类型
            mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            if(Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1 ){
                mediaRecorder.setAudioSamplingRate(11025);
            }
            mediaRecorder.setAudioChannels(2);
            // 设置视频图像的录入源
            // 设置录入媒体的输出格式
//            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            // 设置音频的编码格式
//            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            // 设置视频的编码格式
//            mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
            if (CamcorderProfile.hasProfile(CamcorderProfile.QUALITY_720P)) {
                profile = CamcorderProfile.get(CamcorderProfile.QUALITY_720P);
            } else if (CamcorderProfile.hasProfile(CamcorderProfile.QUALITY_720P)) {
                profile = CamcorderProfile.get(CamcorderProfile.QUALITY_720P);
            }  else if (CamcorderProfile.hasProfile(CamcorderProfile.QUALITY_1080P)) {
                profile = CamcorderProfile.get(CamcorderProfile.QUALITY_1080P);
            } else if (CamcorderProfile.hasProfile(CamcorderProfile.QUALITY_HIGH)) {
                profile = CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH);
            } else if (CamcorderProfile.hasProfile(CamcorderProfile.QUALITY_LOW)) {
                profile = CamcorderProfile.get(CamcorderProfile.QUALITY_LOW);
            }

            profile = CamcorderProfile.get(CamcorderProfile.QUALITY_LOW);
            if (profile != null) {
                profile.audioCodec = MediaRecorder.AudioEncoder.AAC;
                profile.audioChannels = 1;
                profile.audioSampleRate = 16000;

                profile.videoCodec = MediaRecorder.VideoEncoder.H264;
                mediaRecorder.setProfile(profile);
            }

            //视频尺寸
            mediaRecorder.setVideoSize(video_width, video_height);


            //数值越大 视频质量越高
//            mediaRecorder.setVideoEncodingBitRate(5 * 1024 * 1024);
//            mediaRecorder.setVideoEncodingBitRate(1 * 1024 * 1024);
//            mediaRecorder.setVideoEncodingBitRate(1 * 720 * 720);
            mediaRecorder.setVideoEncodingBitRate(4 * 720 * 720);

            // 设置视频的采样率，每秒帧数
            mediaRecorder.setVideoFrameRate(5);

            // 设置录制视频文件的输出路径
            mediaRecorder.setOutputFile(file.getAbsolutePath());

            // 设置捕获视频图像的预览界面
            mediaRecorder.setPreviewDisplay(surfaceView.getHolder().getSurface());

            mediaRecorder.setOnErrorListener(new MediaRecorder.OnErrorListener() {

                @Override
                public void onError(MediaRecorder mr, int what, int extra) {
                    Toast.makeText(context, "错误", Toast.LENGTH_SHORT).show();
                    // 发生错误，停止录制
                    if (mediaRecorder != null) {
                        mediaRecorder.stop();
                        mediaRecorder.release();
                        mediaRecorder = null;
                    }
                }
            });

            mediaRecorder.setOnInfoListener(new MediaRecorder.OnInfoListener() {
                @Override
                public void onInfo(MediaRecorder mr, int what, int extra) {
                    //录制完成
                }
            });

            // 准备、开始
            mediaRecorder.prepare();
            mediaRecorder.start();
            videoStarted = true;
        } catch (Exception e) {
            videoStarted = false;
            e.printStackTrace();
        }
    }

    private void endVideoRecord(){
        if (mediaRecorder != null) {
            mediaRecorder.stop();
            mediaRecorder.release();
            mediaRecorder = null;
            releaseCamera();
            StartListener();
        }
        RequestOptions options = new RequestOptions();
        options.transform(new CircleCrop());
        Glide.with(context).load(file.getAbsolutePath())
                .apply(options).into(mIVShowPhoto);
        Intent intent = new Intent();
        intent.putExtra(AppConstant.KEY.IMG_PATH, file.getAbsolutePath());
        intent.putExtra(AppConstant.KEY.MEDIA_TYPE,MEDIA_VIDEO_TYPE);
        intent.putExtra(AppConstant.KEY.PIC_WIDTH, screenWidth);
        intent.putExtra(AppConstant.KEY.PIC_HEIGHT, picHeight);
        setResult(RESULT_OK,intent);
        CameraVideoActivity.this.finish();
    }

    /**
     * 获取Camera实例
     *
     * @return
     */
    private Camera getCamera(int id) {
        Camera camera = null;
        try {
            camera = Camera.open(id);
        } catch (Exception e) {

        }
        return camera;
    }

    /**
     * 预览相机
     */
    private void startPreview(Camera camera, SurfaceHolder holder) {
        try {
            if (!isVideo){
                setupCamera(camera);
            }else {
                recorderRotation = CameraUtil.getInstance().getRecorderRotation(mCameraId);
                setupCameraVideo(camera);
            }
            camera.setPreviewDisplay(holder);
            //亲测的一个方法 基本覆盖所有手机 将预览矫正
            CameraUtil.getInstance().setCameraDisplayOrientation((Activity) context, mCameraId, camera);
            camera.setDisplayOrientation(90);
            camera.startPreview();
            isview = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void captrue() {
        mCamera.takePicture(null, null, new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
                isview = false;
                //将data 转换为位图 或者你也可以直接保存为文件使用 FileOutputStream
                //这里我相信大部分都有其他用处把 比如加个水印 后续再讲解
                Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                Bitmap saveBitmap = CameraUtil.getInstance().setTakePicktrueOrientation(mCameraId, bitmap);

                saveBitmap = Bitmap.createScaledBitmap(saveBitmap, screenWidth, picHeight, true);

                if (index == 1) {
                    //正方形 animHeight(动画高度)
                    saveBitmap = Bitmap.createBitmap(saveBitmap, 0, animHeight + SystemUtils.dp2px(context, 44), screenWidth, screenWidth);
                } else {
                    //正方形 animHeight(动画高度)
//                    saveBitmap = Bitmap.createBitmap(saveBitmap, 0, 0, screenWidth, screenWidth * 4/3);
                    saveBitmap = Bitmap.createBitmap(saveBitmap, 0, 0, screenWidth, screenWidth * 4/3);
                }

                String img_path = context.getExternalFilesDir(Environment.DIRECTORY_DCIM).getPath() +
                        File.separator + System.currentTimeMillis() + ".jpeg";

                BitmapUtils.saveJPGE_After(context, saveBitmap, img_path, 100);

                if(!bitmap.isRecycled()){
                    bitmap.recycle();
                }

                if(!saveBitmap.isRecycled()){
                    saveBitmap.recycle();
                }

                Intent intent = new Intent();
                intent.putExtra(AppConstant.KEY.IMG_PATH, img_path);
                intent.putExtra(AppConstant.KEY.MEDIA_TYPE,MEDIA_IMAGE_TYPE);
                intent.putExtra(AppConstant.KEY.PIC_WIDTH, screenWidth);
                intent.putExtra(AppConstant.KEY.PIC_HEIGHT, picHeight);
                setResult(RESULT_OK,intent);
                CameraVideoActivity.this.finish();
//                Toast.makeText(context, "=="+img_path, Toast.LENGTH_SHORT).show();
                /*RequestOptions options = new RequestOptions();
                options.transform(new CircleCrop());
                Glide.with(context).load(img_path)
                        .apply(options)
                        .into(mIVShowPhoto);*/
            }
        });
    }

    /**
     * 设置
     */
    private void setupCamera(Camera camera) {
        Camera.Parameters parameters = camera.getParameters();

        if (parameters.getSupportedFocusModes().contains(
                Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        }

        //这里第三个参数为最小尺寸 getPropPreviewSize方法会对从最小尺寸开始升序排列 取出所有支持尺寸的最小尺寸
        Camera.Size previewSize = CameraUtil.getInstance().getPropSizeForHeight(parameters.getSupportedPreviewSizes(), 800);
        parameters.setPreviewSize(previewSize.width, previewSize.height);

        Camera.Size pictrueSize = CameraUtil.getInstance().getPropSizeForHeight(parameters.getSupportedPictureSizes(), 800);
        parameters.setPictureSize(pictrueSize.width, pictrueSize.height);

        camera.setParameters(parameters);

        /**
         * 设置surfaceView的尺寸 因为camera默认是横屏，所以取得支持尺寸也都是横屏的尺寸
         * 我们在startPreview方法里面把它矫正了过来，但是这里我们设置设置surfaceView的尺寸的时候要注意 previewSize.height<previewSize.width
         * previewSize.width才是surfaceView的高度
         * 一般相机都是屏幕的宽度 这里设置为屏幕宽度 高度自适应 你也可以设置自己想要的大小
         *
         */

        picHeight = (screenWidth * pictrueSize.width) / pictrueSize.height;

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(screenWidth, (screenWidth * pictrueSize.width) / pictrueSize.height);
        //这里当然可以设置拍照位置 比如居中 我这里就置顶了
        //params.gravity = Gravity.CENTER;
        surfaceView.setLayoutParams(params);
    }

    /**
     * 设置Video
     */
    private void setupCameraVideo(Camera camera) {
        if (camera != null) {
            Camera.Parameters parameters = camera.getParameters();

            List<String> focusModes = parameters.getSupportedFocusModes();
            if (focusModes != null && focusModes.size() > 0) {
                if (focusModes.contains(
                        Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                    //设置自动对焦
                    parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
                }
            }

            List<Camera.Size> videoSiezes = null;
            if (parameters != null) {
                //获取相机所有支持尺寸
                videoSiezes = parameters.getSupportedVideoSizes();
                for (Camera.Size size : videoSiezes) {
                }
            }

            if (videoSiezes != null && videoSiezes.size() > 0) {
                //拿到一个预览宽度最小为720像素的预览值
                Camera.Size videoSize = CameraUtil.getInstance().getPropVideoSize(videoSiezes, 720);
                video_width = videoSize.width;
                video_height = videoSize.height;
            }

            //这里第三个参数为最小尺寸 getPropPreviewSize方法会对从最小尺寸开始升序排列 取出所有支持尺寸的最小尺寸
            Camera.Size previewSize = CameraUtil.getInstance().getPropPreviewSize(parameters.getSupportedPreviewSizes(), video_width);
            parameters.setPreviewSize(previewSize.width, previewSize.height);

            Camera.Size pictrueSize = CameraUtil.getInstance().getPropPictureSize(parameters.getSupportedPictureSizes(), video_width);
            parameters.setPictureSize(pictrueSize.width, pictrueSize.height);

            camera.setParameters(parameters);

            /**
             * 设置surfaceView的尺寸 因为camera默认是横屏，所以取得支持尺寸也都是横屏的尺寸
             * 我们在startPreview方法里面把它矫正了过来，但是这里我们设置设置surfaceView的尺寸的时候要注意 previewSize.height<previewSize.width
             * previewSize.width才是surfaceView的高度
             * 一般相机都是屏幕的宽度 这里设置为屏幕宽度 高度自适应 你也可以设置自己想要的大小
             */
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(screenWidth, (screenWidth * video_width) / video_height);
            //这里当然可以设置拍照位置 比如居中 我这里就置顶了
            surfaceView.setLayoutParams(params);
        }
    }

    /**
     * 释放相机资源
     */
    private void releaseCamera() {
        if (mCamera != null) {
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (mCamera == null){
            mCamera = getCamera(mCameraId);
        }
        startPreview(mCamera, holder);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        mCamera.stopPreview();
        if (mCamera == null){
            mCamera = getCamera(mCameraId);
        }
        startPreview(mCamera, holder);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        releaseCamera();
    }

    private void setListener() {
        final List<String> mTitleDataList = new ArrayList<>();
        mTitleDataList.add(getString(R.string.take_photo));
        mTitleDataList.add(getString(R.string.take_video));
        CommonNavigator commonNavigator = new CommonNavigator(this);
        commonNavigator.setAdjustMode(true);
        commonNavigator.setAdapter(new CommonNavigatorAdapter() {

            @Override
            public int getCount() {
                return mTitleDataList == null ? 0 : mTitleDataList.size();
            }

            @Override
            public IPagerTitleView getTitleView(Context context, final int index) {
                SimplePagerTitleView simplePagerTitleView = new SimplePagerTitleView(context);
                simplePagerTitleView.setText(mTitleDataList.get(index));
                simplePagerTitleView.setNormalColor(Color.GRAY);
                simplePagerTitleView.setSelectedColor(Color.GREEN);
                simplePagerTitleView.setTextSize(16);
                simplePagerTitleView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        isVideo = index != 0;
                        mMagicIndicator.onPageSelected(index);
                    }
                });
                return simplePagerTitleView;
            }

            @Override
            public IPagerIndicator getIndicator(Context context) {
                return null;
            }
        });
        mMagicIndicator.setNavigator(commonNavigator);
    }
}
