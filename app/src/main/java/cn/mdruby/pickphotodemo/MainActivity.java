package cn.mdruby.pickphotodemo;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import cameralibrary.JCameraView;
import cn.mdruby.pickphotovideoview.MediaModel;
import cn.mdruby.pickphotovideoview.PickConfig;
import cn.mdruby.pickphotovideoview.PickPhotoView;
import cn.mdruby.pickphotovideoview.camera.activity.CameraVideoActivity;
import rx.Observable;
import rx.Observer;
import rx.Subscriber;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final int RECORD_SYSTEM_VIDEO = 0x12;
    ImageView iv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button btn = (Button) findViewById(R.id.click);
        iv = (ImageView) findViewById(R.id.image);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new PickPhotoView.Bulid(MainActivity.this)
                        .useLocalCamera(false)
                        .showCamera(true)
                        .setCount(1)
                        .setCameraComeBack(false)
                        .setVideoDuration(5*60)
                        .setCanZip(true)
                        .setVideoRate(JCameraView.MEDIA_QUALITY_HIGH)
                        .showCheckedIcon(true)
                        .setBottomBarViewRes(R.layout.pick_photo_bottom_bars)
                        .showVideo(false)
                        .start();
            }
        });

        Button videoBtn = (Button) findViewById(R.id.video);
        videoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this,VideoActivity.class));
            }
        });

        Button cameraBtn = (Button) findViewById(R.id.camera);
        cameraBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentCamera = new Intent(MainActivity.this, CameraVideoActivity.class);
                startActivityForResult(intentCamera,0x01);
            }
        });

        Button otherApp = (Button) findViewById(R.id.otherApp);
        otherApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               /* ComponentName localComponentName = new ComponentName(
                        "cn.mdruby.hideapplication",
                        "cn.mdruby.hideapplication.MainActivity");
                Intent localIntent = new Intent();
                localIntent.setComponent(localComponentName);
                startActivity(localIntent);*/
            }
        });

        Button mBtnXITONG = (Button) findViewById(R.id.btn_xitong);
        mBtnXITONG.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reconverIntent(v);
            }
        });

        Button mBtnCamera = (Button) findViewById(R.id.Cameravideo);
        mBtnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, CameraVideoActivity.class);
                startActivityForResult(intent, PickConfig.RequestCode.TAKE_PHOTO_BY_SELF);
            }
        });
    }

    private File getOutputMediaFile() {
        if(!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())){
            Toast.makeText(this, "请检查SDCard！", Toast.LENGTH_SHORT).show();
            return null;
        }

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory
                (Environment.DIRECTORY_DCIM), "MyCameraApp");
        if (!mediaStorageDir.exists()) {
            mediaStorageDir.mkdirs();
        }
        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile = new File(mediaStorageDir.getPath() + File.separator + "VID_" + timeStamp + ".mp4");
        return mediaFile;
    }



    /**
     * 启用系统相机录制
     *
     * @param view
     */
    public void reconverIntent(View view) {
//        Uri fileUri = Uri.fromFile(getOutputMediaFile());
        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 10); //限制的录制时长 以秒为单位
        Uri fileUri = getUri(intent,getOutputMediaFile());
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
        //intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY,1); //设置拍摄的质量最小是0，最大是1（建议不要设置中间值，不同手机似乎效果不同。。。）
        //intent.putExtra(MediaStore.EXTRA_SIZE_LIMIT, 1024 * 1024);//限制视频文件大小 以字节为单位
        startActivityForResult(intent, RECORD_SYSTEM_VIDEO);
    }

    private Uri getUri(Intent intent, File file) {
        Uri uri = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            //判断版本是否在7.0以上
            uri =
                    FileProvider.getUriForFile(this,
                            this.getPackageName() + ".provider",
                            file);
            //添加这一句表示对目标应用临时授权该Uri所代表的文件
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } else {
            uri = Uri.fromFile(file);
        }
        return uri;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK){
            return;
        }
        switch (requestCode){
            case PickConfig.RequestCode.SELECT_PHOTO:
                if (resultCode == RESULT_OK){
                    List<MediaModel> mediaModels = (List<MediaModel>) data.getSerializableExtra(PickConfig.KEY.MEDIA_FILE_DATA);
                    Log.e(TAG, "onActivityResult: "+mediaModels.get(0).getFile().getAbsolutePath() );
                    Glide.with(this).load(mediaModels.get(0).getFile()).into(iv);
                    File file = mediaModels.get(0).getFile();
                    FileInputStream fis = null;
                    try {
                        Glide.with(this).load(file.getAbsoluteFile()).into(iv);
                        fis = new FileInputStream(file);
                        long size = fis.available();
                        String formatSize = getFormatSize(size);
                        Log.e(TAG, "onActivityResult: "+formatSize );
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case RECORD_SYSTEM_VIDEO:
                Log.e(TAG, "onActivityResult: "+data.getData());
                break;
            case PickConfig.RequestCode.TAKE_PHOTO_BY_SELF:
                Toast.makeText(this, "Success", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    /**
     * 格式化单位
     *
     * @param size
     * @return
     */
    public static String getFormatSize(double size) {
        double kiloByte = size / 1024;
        if (kiloByte < 1) {
//            return size + "B";
            return "0.0M";
        }

        double megaByte = kiloByte / 1024;
        if (megaByte < 1) {
            BigDecimal result1 = new BigDecimal(Double.toString(kiloByte));
            return result1.setScale(2, BigDecimal.ROUND_HALF_UP)
                    .toPlainString() + "K";
//            return  "0.0M";
        }

        double gigaByte = megaByte / 1024;
        if (gigaByte < 1) {
            BigDecimal result2 = new BigDecimal(Double.toString(megaByte));
            return result2.setScale(2, BigDecimal.ROUND_HALF_UP)
                    .toPlainString() + "M";
        }

        double teraBytes = gigaByte / 1024;
        if (teraBytes < 1) {
            BigDecimal result3 = new BigDecimal(Double.toString(gigaByte));
            return result3.setScale(2, BigDecimal.ROUND_HALF_UP)
                    .toPlainString() + "GB";
        }
        BigDecimal result4 = new BigDecimal(teraBytes);
        return result4.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString()
                + "TB";
    }
}
