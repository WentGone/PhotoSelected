package cn.mdruby.pickphotodemo;

import android.content.Context;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.exoplayer2.ExoPlaybackException;

import chuangyuan.ycj.videolibrary.listener.VideoInfoListener;
import chuangyuan.ycj.videolibrary.video.ManualPlayer;
import cn.jzvd.JZVideoPlayerStandard;
import cn.mdruby.pickphotovideoview.OfficeDataSource;

public class VideoActivity extends AppCompatActivity {
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);
        mContext = this;
        String path = Environment.getExternalStorageDirectory()+"/1521528772476.mp4";
        path = "https://cdsstest.mdruby.cn/hdata/video/358/12097da4924f4d0e887f3d0a6cffe883.mp4";
        ManualPlayer exoPlayerManager = new ManualPlayer(this,R.id.exo_play_context_id,new OfficeDataSource(this, null));
        exoPlayerManager.setPlayUri(path);
//        exoPlayerManager.startPlayer();

        exoPlayerManager.setVideoInfoListener(new VideoInfoListener() {
            @Override
            public void onPlayStart() {
                //开始播放
                Toast.makeText(mContext, "==start", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onLoadingChanged() {
                //加载变化
                Toast.makeText(mContext, "==change", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onPlayerError(ExoPlaybackException e) {
                //加载错误
                Toast.makeText(mContext, "==error", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onPlayEnd() {
                //播放结束
                Toast.makeText(mContext, "==end", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void isPlaying(boolean playWhenReady) {
                Toast.makeText(mContext, "==ing"+playWhenReady, Toast.LENGTH_SHORT).show();
            }
        });


        initJZ();
    }

    private void initJZ() {
        String path = Environment.getExternalStorageDirectory()+"/1521528772476.mp4";
//        path = "http://jzvd.nathen.cn/c6e3dc12a1154626b3476d9bf3bd7266/6b56c5f0dc31428083757a45764763b0-5287d2089db37e62345123a1be272f8b.mp4";
        path = "https://cdsstest.mdruby.cn/hdata/video/358/12097da4924f4d0e887f3d0a6cffe883.mp4";
        JZVideoPlayerStandard jzVideoPlayerStandard = (JZVideoPlayerStandard) findViewById(R.id.videoplayer);
        jzVideoPlayerStandard.setUp(path
                , JZVideoPlayerStandard.SCREEN_WINDOW_NORMAL, "饺子闭眼睛");
    }

//    private class My
}
