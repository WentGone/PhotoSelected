package cn.mdruby.pickphotovideoview.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;


import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;

import cn.mdruby.pickphotovideoview.MediaModel;
import cn.mdruby.pickphotovideoview.PickConfig;
import cn.mdruby.pickphotovideoview.R;
import cn.mdruby.pickphotovideoview.util.FileUtil;

/**
 * 裁剪图片界面
 */
public class PickCropActivity extends AppCompatActivity {
    private CropImageView cropImageView;
    private MediaModel mediaModel;
    private Button mBtnCrop,mBtnCancle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pick_crop);
        mediaModel = (MediaModel) getIntent().getSerializableExtra(PickConfig.KEY.MEDIA_DATA_ONE);
        cropImageView = (CropImageView) findViewById(R.id.cropImageView);
        mBtnCrop = (Button) findViewById(R.id.act_pick_crop_Btn_Crop);
        mBtnCancle = (Button) findViewById(R.id.act_pick_crop_Btn_Cancle);

        cropImageView.setImageUriAsync(Uri.fromFile(mediaModel.getFile()));
        cropImageView.setAspectRatio(1,1);


        mBtnCrop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bitmap croppedImage = cropImageView.getCroppedImage();
                File file = FileUtil.saveImage(PickCropActivity.this,croppedImage);
                Intent intent = getIntent();
                intent.putExtra(PickConfig.KEY.CROP_IMAGE_FILE_PATH,file.getAbsolutePath());
                setResult(RESULT_OK,intent);
                PickCropActivity.this.finish();
            }
        });

        mBtnCancle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PickCropActivity.this.finish();
            }
        });
    }
}
