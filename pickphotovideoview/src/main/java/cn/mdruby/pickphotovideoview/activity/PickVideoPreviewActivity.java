package cn.mdruby.pickphotovideoview.activity;

import android.content.Context;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import chuangyuan.ycj.videolibrary.video.ManualPlayer;
import cn.jzvd.JZVideoPlayerStandard;
import cn.mdruby.pickphotovideoview.MediaModel;
import cn.mdruby.pickphotovideoview.OfficeDataSource;
import cn.mdruby.pickphotovideoview.R;

public class PickVideoPreviewActivity extends AppCompatActivity {
    public static final String MEDIA_BEAN = "media_bean";
    private MediaModel mediaModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pick_video_preview);
        mediaModel = (MediaModel) getIntent().getSerializableExtra(MEDIA_BEAN);

//        GiraffePlayer player = new GiraffePlayer(this);
//        player.play(mediaModel.getFile().getAbsolutePath());
//        String url = "https://cdsstest.mdruby.cn/hdata/video/358/12097da4924f4d0e887f3d0a6cffe883.mp4";
//        String url = "http://ips.ifeng.com/video19.ifeng.com/video09/2017/05/24/4664192-102-008-1012.mp4";
//        HttpProxyCacheServer proxy = getProxy(this);
//        String proxyUrl = proxy.getProxyUrl(url);
//        player.play(proxyUrl);
        ManualPlayer exoPlayerManager = new ManualPlayer(this,R.id.exo_play_context_id,new OfficeDataSource(this, null));
        exoPlayerManager.setPlayUri(mediaModel.getFile().getAbsolutePath());
//        exoPlayerManager.startPlayer();

        String path = mediaModel.getFile().getAbsolutePath();
        JZVideoPlayerStandard jzVideoPlayerStandard = (JZVideoPlayerStandard) findViewById(R.id.videoplayer);
        jzVideoPlayerStandard.setUp(path
                , JZVideoPlayerStandard.SCREEN_WINDOW_NORMAL, "");
    }

}
