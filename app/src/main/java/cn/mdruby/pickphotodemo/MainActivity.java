package cn.mdruby.pickphotodemo;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.util.List;

import cn.mdruby.pickphotovideoview.MediaModel;
import cn.mdruby.pickphotovideoview.PickConfig;
import cn.mdruby.pickphotovideoview.PickPhotoView;

public class MainActivity extends AppCompatActivity {
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
                        .setCount(9)
                        .showVideo(true)
                        .start();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case PickConfig.RequestCode.SELECT_PHOTO:
                if (resultCode == RESULT_OK){
                    List<MediaModel> mediaModels = (List<MediaModel>) data.getSerializableExtra(PickConfig.KEY.MEDIA_FILE_DATA);
                    Glide.with(this).load(mediaModels.get(0).getPath()).into(iv);
                }
                break;
        }
    }
}
