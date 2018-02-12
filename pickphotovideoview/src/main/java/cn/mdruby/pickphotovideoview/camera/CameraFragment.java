package cn.mdruby.pickphotovideoview.camera;

import android.Manifest;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

import cn.mdruby.pickphotovideoview.R;


/**
 * 自定义拍照
 */
public class CameraFragment extends Fragment implements SurfaceHolder.Callback, View.OnClickListener{
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final int VOICE_REQUEST_CODE = 0X89;

    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    private Camera mCamera;
    private SurfaceView surfaceView;
    private SurfaceHolder mHolder;
    private int mCameraId = 0;
    private Context context;

    //屏幕宽高
    private int screenWidth;
    private int screenHeight;
    private LinearLayout home_custom_top_relative;
    private ImageView camera_delay_time;
    private View homeCustom_cover_top_view;
    private View homeCustom_cover_bottom_view;
    private View home_camera_cover_top_view;
    private View home_camera_cover_bottom_view;
    private ImageView flash_light;
    private TextView camera_delay_time_text;
    private ImageView camera_square;
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

    public CameraFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment CameraFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static CameraFragment newInstance(String param1, String param2) {
        CameraFragment fragment = new CameraFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_camera,container,false);
        context = getActivity();
        initView(view);
        initData();

        requestPermission();

        return view;
    }

    private void requestPermission() {
        //判断是否开启摄像头权限
        if ((ContextCompat.checkSelfPermission(context,
                Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)) {
            StartListener();

            //判断是否开启语音权限
        } else {
            //请求获取摄像头权限
            ActivityCompat.requestPermissions((Activity) context,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, VOICE_REQUEST_CODE);
        }

    }

    /**
     * 请求权限回调
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == VOICE_REQUEST_CODE) {
            if ((grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                StartListener();
            } else {
                Toast.makeText(context, "已拒绝权限！", Toast.LENGTH_SHORT).show();
            }
        }
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

    private void initView(View view) {
        surfaceView = (SurfaceView) view.findViewById(R.id.surfaceView);
        mHolder = surfaceView.getHolder();
        mHolder.addCallback(this);
        img_camera = (ImageView) view.findViewById(R.id.img_camera);
        img_camera.setOnClickListener(this);

        //关闭相机界面按钮
        camera_close = (ImageView) view.findViewById(R.id.camera_close);
        camera_close.setOnClickListener(this);

        //top 的view
        home_custom_top_relative = (LinearLayout) view.findViewById(R.id.home_custom_top_relative);
        home_custom_top_relative.setAlpha(0.5f);

        //前后摄像头切换
        camera_frontback = (ImageView) view.findViewById(R.id.camera_frontback);
        camera_frontback.setOnClickListener(this);

        //延迟拍照时间
        camera_delay_time = (ImageView) view.findViewById(R.id.camera_delay_time);
        camera_delay_time.setOnClickListener(this);

        //正方形切换
        camera_square = (ImageView) view.findViewById(R.id.camera_square);
        camera_square.setOnClickListener(this);

        //切换正方形时候的动画
        homeCustom_cover_top_view = view.findViewById(R.id.homeCustom_cover_top_view);
        homeCustom_cover_bottom_view = view.findViewById(R.id.homeCustom_cover_bottom_view);

        homeCustom_cover_top_view.setAlpha(0.5f);
        homeCustom_cover_bottom_view.setAlpha(0.5f);

        //拍照时动画
        home_camera_cover_top_view = view.findViewById(R.id.home_camera_cover_top_view);
        home_camera_cover_bottom_view = view.findViewById(R.id.home_camera_cover_bottom_view);
        home_camera_cover_top_view.setAlpha(1);
        home_camera_cover_bottom_view.setAlpha(1);

        //闪光灯
        flash_light = (ImageView) view.findViewById(R.id.flash_light);
        flash_light.setOnClickListener(this);

        camera_delay_time_text = (TextView) view.findViewById(R.id.camera_delay_time_text);

        homecamera_bottom_relative = (RelativeLayout) view.findViewById(R.id.homecamera_bottom_relative);
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
        homecamera_bottom_relative.setLayoutParams(bottomParam);
    }

    private Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            int what = msg.what;
            switch (what) {
                case AppConstant.WHAT.SUCCESS:
                    if (delay_time > 0) {
                        camera_delay_time_text.setText("" + delay_time);
                    }

                    try {
                        if (delay_time == 0) {
                            captrue();
                            is_camera_delay = false;
                            camera_delay_time_text.setVisibility(View.GONE);
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
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.img_camera) {
            if (isview) {
                if (delay_time == 0) {
                    switch (light_num) {
                        case 0:
                            //关闭
                            CameraUtil.getInstance().turnLightOff(mCamera);
                            break;
                        case 1:
                            CameraUtil.getInstance().turnLightOn(mCamera);
                            break;
                        case 2:
                            //自动
                            CameraUtil.getInstance().turnLightAuto(mCamera);
                            break;
                    }
                    captrue();
                } else {
                    camera_delay_time_text.setVisibility(View.VISIBLE);
                    camera_delay_time_text.setText(String.valueOf(delay_time));
                    is_camera_delay = true;
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            while (delay_time > 0) {
                                //按秒数倒计时
                                try {
                                    Thread.sleep(1000);
                                } catch (InterruptedException e) {
                                    mHandler.sendEmptyMessage(AppConstant.WHAT.ERROR);
                                    return;
                                }
                                delay_time--;
                                mHandler.sendEmptyMessage(AppConstant.WHAT.SUCCESS);
                            }
                        }
                    }).start();
                }
                isview = false;
            }

        } else if (id == R.id.camera_square) {
            if (index == 0) {
                camera_square_0();
            } else if (index == 1) {
                camera_square_1();
            }


            //前后置摄像头拍照
        } else if (id == R.id.camera_frontback) {
            switchCamera();


            //退出相机界面 释放资源
        } else if (id == R.id.camera_close) {

            if (is_camera_delay) {
                Toast.makeText(context, "正在拍照请稍后...", Toast.LENGTH_SHORT).show();
                return;
            }


            //闪光灯
        } else if (id == R.id.flash_light) {
            if (mCameraId == 1) {
                //前置
                Toast.makeText(context, "请切换为后置摄像头开启闪光灯...", Toast.LENGTH_SHORT).show();
                return;
            }
            Camera.Parameters parameters = mCamera.getParameters();
            switch (light_num) {
                case 0:
                    //打开
                    light_num = 1;
                    flash_light.setImageResource(R.mipmap.btn_camera_flash_on);
                    parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);//开启
                    mCamera.setParameters(parameters);
                    break;
                case 1:
                    //自动
                    light_num = 2;
                    parameters.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
                    mCamera.setParameters(parameters);
                    flash_light.setImageResource(R.mipmap.btn_camera_flash_auto);
                    break;
                case 2:
                    //关闭
                    light_num = 0;
                    //关闭
                    parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                    mCamera.setParameters(parameters);
                    flash_light.setImageResource(R.mipmap.btn_camera_flash_off);
                    break;
            }


            //延迟拍照时间
        } else if (id == R.id.camera_delay_time) {
            switch (delay_time) {
                case 0:
                    delay_time = 3;
                    delay_time_temp = delay_time;
                    camera_delay_time.setImageResource(R.mipmap.btn_camera_timing_3);
                    break;

                case 3:
                    delay_time = 5;
                    delay_time_temp = delay_time;
                    camera_delay_time.setImageResource(R.mipmap.btn_camera_timing_5);
                    break;

                case 5:
                    delay_time = 10;
                    delay_time_temp = delay_time;
                    camera_delay_time.setImageResource(R.mipmap.btn_camera_timing_10);
                    break;

                case 10:
                    delay_time = 0;
                    delay_time_temp = delay_time;
                    camera_delay_time.setImageResource(R.mipmap.btn_camera_timing_0);
                    break;

            }
        }
    }

    public void switchCamera() {
        releaseCamera();
        mCameraId = (mCameraId + 1) % mCamera.getNumberOfCameras();
        mCamera = getCamera(mCameraId);
        if (mHolder != null) {
            startPreview(mCamera, mHolder);
        }
    }

    /**
     * 正方形拍摄
     */
    public void camera_square_0() {
        camera_square.setImageResource(R.mipmap.btn_camera_size1_n);

        //属性动画
        ValueAnimator anim = ValueAnimator.ofInt(0, animHeight);
        anim.setDuration(300);
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int currentValue = Integer.parseInt(animation.getAnimatedValue().toString());
                RelativeLayout.LayoutParams Params = new RelativeLayout.LayoutParams(screenWidth, currentValue);
                Params.setMargins(0, SystemUtils.dp2px(context, 44), 0, 0);
                homeCustom_cover_top_view.setLayoutParams(Params);

                RelativeLayout.LayoutParams bottomParams = new RelativeLayout.LayoutParams(screenWidth, currentValue);
                bottomParams.setMargins(0, screenHeight - menuPopviewHeight - currentValue, 0, 0);
                homeCustom_cover_bottom_view.setLayoutParams(bottomParams);
            }

        });
        anim.start();

        homeCustom_cover_top_view.bringToFront();
        home_custom_top_relative.bringToFront();
        homeCustom_cover_bottom_view.bringToFront();
        index++;
    }

    /**
     * 长方形方形拍摄
     */
    public void camera_square_1() {
        camera_square.setImageResource(R.mipmap.btn_camera_size2_n);

        ValueAnimator anim = ValueAnimator.ofInt(animHeight, 0);
        anim.setDuration(300);
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int currentValue = Integer.parseInt(animation.getAnimatedValue().toString());
                RelativeLayout.LayoutParams Params = new RelativeLayout.LayoutParams(screenWidth, currentValue);
                Params.setMargins(0, SystemUtils.dp2px(context, 44), 0, 0);
                homeCustom_cover_top_view.setLayoutParams(Params);

                RelativeLayout.LayoutParams bottomParams = new RelativeLayout.LayoutParams(screenWidth, currentValue);
                bottomParams.setMargins(0, screenHeight - menuPopviewHeight - currentValue, 0, 0);
                homeCustom_cover_bottom_view.setLayoutParams(bottomParams);
            }
        });
        anim.start();
        index = 0;

    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden){
            if (mCamera == null) {
                mCamera = getCamera(mCameraId);
                if (mHolder != null) {
                    startPreview(mCamera, mHolder);
                }
            }
        }else {
            releaseCamera();
        }
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
            setupCamera(camera);
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
                intent.putExtra(AppConstant.KEY.PIC_WIDTH, screenWidth);
                intent.putExtra(AppConstant.KEY.PIC_HEIGHT, picHeight);

//                context.setResult(AppConstant.RESULT_CODE.RESULT_OK, intent);

                //这里打印宽高 就能看到 CameraUtil.getInstance().getPropPictureSize(parameters.getSupportedPictureSizes(), 200);
                // 这设置的最小宽度影响返回图片的大小 所以这里一般这是1000左右把我觉得
//                Log.d("bitmapWidth==", bitmap.getWidth() + "");
//                Log.d("bitmapHeight==", bitmap.getHeight() + "");
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


    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        /*if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }*/
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
