package cn.mdruby.pickphotovideoview.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.DragEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import cn.mdruby.pickphotovideoview.App;
import cn.mdruby.pickphotovideoview.DirImage;
import cn.mdruby.pickphotovideoview.GroupMedia;
import cn.mdruby.pickphotovideoview.MediaModel;
import cn.mdruby.pickphotovideoview.PickConfig;
import cn.mdruby.pickphotovideoview.PickData;
import cn.mdruby.pickphotovideoview.PickPhotoHelper;
import cn.mdruby.pickphotovideoview.PickPhotoListener;
import cn.mdruby.pickphotovideoview.PickPreferences;
import cn.mdruby.pickphotovideoview.R;
import cn.mdruby.pickphotovideoview.abstracts.OnItemPhotoClickListener;
import cn.mdruby.pickphotovideoview.abstracts.OnRVListClickListener;
import cn.mdruby.pickphotovideoview.adapter.RVPhotoGridAdapter;
import cn.mdruby.pickphotovideoview.adapter.RVPhotoListAdapter;
import cn.mdruby.pickphotovideoview.camera.AppConstant;
import cn.mdruby.pickphotovideoview.camera.activity.CameraVideoActivity;
import cn.mdruby.pickphotovideoview.ui.DividerItemDecoration;
import cn.mdruby.pickphotovideoview.util.PickUtils;

public class PickPhotoActivity extends AppCompatActivity implements OnItemPhotoClickListener{
    public static final String PICK_DATA="pick_data";
    private static final int CAMERA_REQUEST_CODE = 0x67;
    private PickData pickData;
    private RecyclerView mRV;
    private RecyclerView mRVList;
    private RVPhotoGridAdapter mAdapter;
    private RVPhotoListAdapter mListAdapter;
    private List<MediaModel> mDatas;
    private List<MediaModel> mSelecteds;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private Toolbar toolbar;
    private TextView mTVtitle;
    private int photoSize = 100;
    private boolean showVideo = true;
    private boolean showCamera = false;
    private boolean useLocalCamera = false;
    private int selectedCount = 3;
    private TextView mTVselected,mTVcount;
    private int isPosition = -1;
    private GroupMedia groupImage;
    private DirImage dirImage;
    private List<String> dirImageStrings;
    private HashMap<String,List<MediaModel>> groupImages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pick_photo);
        initDataBefor();


        pickData = (PickData) getIntent().getSerializableExtra(PICK_DATA);
        showVideo = pickData.isShowVideo();
        showCamera = pickData.isShowCamera();
        selectedCount = pickData.isCount();
        useLocalCamera = pickData.isUseLocalCamera();
        mDatas = new ArrayList<>();
        mSelecteds = new ArrayList<>();
        mRV = (RecyclerView) findViewById(R.id.act_pick_photo_RV);
        mRVList = (RecyclerView) findViewById(R.id.act_pick_photo_list_RV);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.act_pick_DrawerLayout);
        toolbar = (Toolbar) findViewById(R.id.tl_custom);
        mTVtitle = (TextView) findViewById(R.id.act_pick_TV_title);
        mTVselected = (TextView) findViewById(R.id.act_pick_TV_bottom_selected);
        mTVcount = (TextView) findViewById(R.id.act_pick_TV_bottom_count);

        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        mDrawerToggle = new OnDragScrollListener(this, mDrawerLayout, toolbar, R.string.open, R.string.close);
        mDrawerToggle.syncState();
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        mRV.setLayoutManager(new GridLayoutManager(this,4,GridLayoutManager.VERTICAL,false));
        mRVList.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new RVPhotoGridAdapter(this,mDatas,showCamera);
        mRV.setAdapter(mAdapter);

        groupImage = PickPreferences.getInstance(this).getListImage();
        dirImage = PickPreferences.getInstance(this).getDirImage();

        if (groupImage != null && dirImage != null){
            groupImages = groupImage.getGroupMedias();
            dirImageStrings = dirImage.dirName;
        }
        mListAdapter = new RVPhotoListAdapter(this,groupImages,dirImageStrings);
        mRVList.addItemDecoration(new DividerItemDecoration(this,DividerItemDecoration.HORIZONTAL_LIST,15, Color.TRANSPARENT));
        mRVList.setAdapter(mListAdapter);

        mAdapter.setOnItemPhotoClickListener(this);
        mListAdapter.setOnRVListClickListener(new OnRVListClickListener() {
            @Override
            public void onClickItem(int position) {
                clickListItem(position);
            }
        });

        mTVselected.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //回调
                callback();
            }
        });

        getPermission();

        mDrawerLayout.setOnDragListener(new View.OnDragListener() {
            @Override
            public boolean onDrag(View view, DragEvent dragEvent) {
                return false;
            }
        });
    }

    private void initDataBefor() {
        dirImageStrings = new ArrayList<>();
        groupImages = new HashMap<>();
    }

    private class OnDragScrollListener extends ActionBarDrawerToggle{

        public OnDragScrollListener(Activity activity, DrawerLayout drawerLayout, @StringRes int openDrawerContentDescRes, @StringRes int closeDrawerContentDescRes) {
            super(activity, drawerLayout, openDrawerContentDescRes, closeDrawerContentDescRes);
        }

        public OnDragScrollListener(Activity activity, DrawerLayout drawerLayout, Toolbar toolbar, @StringRes int openDrawerContentDescRes, @StringRes int closeDrawerContentDescRes) {
            super(activity, drawerLayout, toolbar, openDrawerContentDescRes, closeDrawerContentDescRes);
        }

        @Override
        public void onDrawerOpened(View drawerView) {
            if (groupImages.size()==0 && dirImageStrings.size()==0){
                groupImage = PickPreferences.getInstance(PickPhotoActivity.this).getListImage();
                dirImage = PickPreferences.getInstance(PickPhotoActivity.this).getDirImage();
                groupImages.putAll(groupImage.getGroupMedias());
                dirImageStrings.addAll(dirImage.dirName);
                mListAdapter.notifyDataSetChanged();
            }
            super.onDrawerOpened(drawerView);
        }

    }

    private void getPermission() {
        //判断是否开启读写SD权限
        if ((ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) &&
                (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
                ) {
            getPictures();

            //判断是否开启语音权限
        } else {
            //请求获取摄像头权限
            ActivityCompat.requestPermissions((Activity) this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE},PickConfig.RequestCode.GET_EXTERNAL_STORAGE_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PickConfig.RequestCode.GET_EXTERNAL_STORAGE_REQUEST_CODE) {
            if ((grantResults[0] == PackageManager.PERMISSION_GRANTED) && (grantResults[1] == PackageManager.PERMISSION_GRANTED) ) {
                getPictures();
            } else {
                Toast.makeText(this, "已拒绝权限！", Toast.LENGTH_SHORT).show();
            }
        }
        if (requestCode == CAMERA_REQUEST_CODE) {
            if ((grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                successPermisson();
            } else {
                Toast.makeText(this, "已拒绝权限！", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void callback() {
        Intent intent = getIntent();
        intent.putExtra(PickConfig.KEY.MEDIA_FILE_DATA, (Serializable) mSelecteds);
        setResult(RESULT_OK,intent);
        this.finish();
    }

    /**
     * 点击列表中的某一项
     * @param position
     */
    private void clickListItem(int position) {
        List<MediaModel> item = mListAdapter.getItem(position);
        mTVcount.setText("");
        mSelecteds.clear();
        mTVtitle.setText(mListAdapter.getDirName(position));
        mDatas.clear();
        mDatas.addAll(item);
        mDrawerLayout.closeDrawers();
        mAdapter.setShowCamera(mListAdapter.getDirName(position).equals(PickConfig.ALL_PHOTOS)?showCamera:false);
        mAdapter.notifyDataSetChanged();
    }

//    public static final String ALL_PHOTOS = "All Photos";
    public static final String ALL_PHOTOS = "所有图片";
//    public static final String ALL_PHOTOS = App.getContext().getString(R.string.pick_all_photo);

    /**
     * 获取所有的照片
     */
    private void getPictures() {
      PickPhotoHelper vhlepr = new PickPhotoHelper(this, new PickPhotoListener() {
          @Override
          public void pickSuccess() {
              mDatas.clear();
              List<MediaModel> mediaModels = PickPreferences.getInstance(PickPhotoActivity.this).getListImage().getGroupMedias().get(ALL_PHOTOS);
              if (mediaModels == null){
                  mediaModels = new ArrayList<>();
              }
              mDatas.addAll(mediaModels);
              mAdapter.notifyDataSetChanged();
          }
      });
        vhlepr.getImages(true);
        vhlepr.setShowVideo(showVideo);
    }

    @Override
    public void onCameraClick() {
        if (!useLocalCamera){
            Intent intent = new Intent(this, CameraVideoActivity.class);
            startActivityForResult(intent, PickConfig.RequestCode.TAKE_PHOTO_BY_SELF);
        }else {
            checkCameraPermission();
        }
    }


    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)  {

            //判断是否开启权限
            successPermisson();
        } else {
//            requestPermisson();
            ActivityCompat.requestPermissions((Activity) this,
                    new String[]{Manifest.permission.CAMERA}, CAMERA_REQUEST_CODE);
        }
    }

    private void successPermisson() {
        try {
            File photoFile = PickUtils.getInstance(this).getPhotoFile(this);
            photoFile.delete();
            if (photoFile.createNewFile()) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, PickUtils.getInstance(this).getUri(photoFile));
                startActivityForResult(intent, PickConfig.CAMERA_PHOTO_DATA);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPhotoClick(int position) {
        Intent intent = new Intent(this,PickPhotoPreviewActivity.class);
        MediaModel mediaModel = mDatas.get(position);
        isPosition = position;
        if (mDatas.size()>=photoSize){
            int startPos = (position-photoSize/2)<0?0:(position-photoSize/2);
            int endPos = ((mDatas.size()-position)>=photoSize)?(position+photoSize/2):mDatas.size();

//            List<MediaModel> mediaModels = new ArrayList<MediaModel>(mDatas.subList((position - photoSize/2) < 0 ? 0 : (position - photoSize/2), mDatas.size() - position > photoSize/2 ? photoSize/2+position : (mDatas.size() - photoSize/2)));
            List<MediaModel> mediaModels = new ArrayList<MediaModel>(mDatas.subList(startPos, endPos));
            List<MediaModel> a = new ArrayList<>();
            for (int i = 0; i < mediaModels.size(); i++) {
                MediaModel mediaModel1 = mediaModels.get(i);
                if (!mediaModel1.getMimeType().contains("video")){
                    a.add(mediaModel1);
                }
            }
            intent.putExtra(PickPhotoPreviewActivity.MEDIA_DATAS, (Serializable) a);
            int po = 0;
            for (int i = 0; i < a.size(); i++) {
                if (a.get(i).getPath().equals(mediaModel.getPath())){
                    po = i;
                    break;
                }
            }
            intent.putExtra(PickPhotoPreviewActivity.POSITION_COUNT,po);
        }else {
            List<MediaModel> a = new ArrayList<>();
            for (int i = 0; i < mDatas.size(); i++) {
                MediaModel mediaModel1 = mDatas.get(i);
                if (!mediaModel1.getMimeType().contains("video")){
                    a.add(mediaModel1);
                }
            }
            intent.putExtra(PickPhotoPreviewActivity.MEDIA_DATAS, (Serializable) a);
            intent.putExtra(PickPhotoPreviewActivity.POSITION_COUNT,position);
        }
        intent.putExtra(PickConfig.KEY.MEDIA_COUNT,selectedCount);
        intent.putExtra(PickConfig.KEY.MEDIA_NOW_COUNT,mSelecteds.size());
        startActivityForResult(intent,PickConfig.RequestCode.PRE_PHOTO_CODE);
    }

    @Override
    public void onVideoClick(int position) {
        MediaModel mediaModel = mDatas.get(position);
        Intent intent = new Intent(this,PickVideoPreviewActivity.class);
        intent.putExtra(PickVideoPreviewActivity.MEDIA_BEAN,mediaModel);
        startActivity(intent);
    }

    @Override
    public void onSelectClick(int position) {
        if (mSelecteds.size()<selectedCount){
            MediaModel item = mAdapter.getItem(position);
            item.setSelected(!item.isSelected());
            mAdapter.notifyItemChanged(showCamera?(position+1):position);
            setSelected(item);
            mTVcount.setText(mSelecteds.size()+"");
        }else {
            Toast.makeText(this, "选择的图片不能超过"+selectedCount+"张", Toast.LENGTH_SHORT).show();
        }
    }

    private void setSelected(MediaModel item) {
        if (item.isSelected()){
            if (!mSelecteds.contains(item)){
                mSelecteds.add(item);
            }
        }else {
            if (mSelecteds.contains(item)){
                mSelecteds.remove(item);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            case PickConfig.RequestCode.TAKE_PHOTO_BY_SELF:
                if (resultCode == RESULT_OK){
                    String imagePath = data.getStringExtra(AppConstant.KEY.IMG_PATH);
                    MediaModel mediaModel = new MediaModel();
                    mediaModel.setFile(new File(imagePath));
                    mediaModel.setPath(imagePath);
                    mediaModel.setThumPath(imagePath);
                    mediaModel.setMimeType(imagePath.endsWith(".mp4")?"video":"image");
                    if (imagePath.endsWith(".mp4")){
                        MediaPlayer mediaPlayer = new MediaPlayer();
                        try {
                            mediaPlayer.setDataSource(imagePath);
                            mediaPlayer.prepare();
                            int duration = mediaPlayer.getDuration();
                            String converted = String.format("%02d:%02d",
                                    TimeUnit.MILLISECONDS.toMinutes(duration),
                                    TimeUnit.MILLISECONDS.toSeconds(duration) -
                                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration))
                            );
                            mediaModel.setDuration(duration);
                            mediaModel.setDurationStr(converted);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    mDatas.add(0,mediaModel);
                    mAdapter.notifyDataSetChanged();
                }
                break;
            case PickConfig.RequestCode.PRE_PHOTO_CODE:
                List<MediaModel> pres = (List<MediaModel>) data.getSerializableExtra(PickConfig.KEY.PRE_PHOTO_FILE);
                for (int i = 0; i < mDatas.size(); i++) {
                    for (int j = 0; j < pres.size(); j++) {
                        if (mDatas.get(i).compareTo(pres.get(j)) == 0){
                            mDatas.get(i).setSelected(pres.get(j).isSelected());
                        }
                    }
                }
                mAdapter.notifyDataSetChanged();
                mSelecteds.clear();
                for (int i = 0; i < mDatas.size(); i++) {
                    setSelected(mDatas.get(i));
                }
                mTVcount.setText(mSelecteds.size()+"");
                break;
            case PickConfig.CAMERA_PHOTO_DATA:
                String path;
                if (data != null) {
                    path = data.getData().getPath();
                    if (path.contains("/pick_camera")) {
                        path = path.replace("/pick_camera", "/storage/emulated/0/DCIM/Camera");
                    }
                } else {
                    path = PickUtils.getInstance(PickPhotoActivity.this).getFilePath(PickPhotoActivity.this);
                }
                sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + path)));
                Intent intent = new Intent();
                ArrayList<String> list = new ArrayList<>();
                list.add(path);
                ArrayList<MediaModel> mediaModels = new ArrayList<>();
                MediaModel mediaModel = new MediaModel();
                mediaModel.setMimeType("image/jpg");
                mediaModel.setThumPath(path);
                mediaModel.setPath(path);
//                mediaModels.add(mediaModel);
                mDatas.add(0,mediaModel);
                mAdapter.notifyDataSetChanged();
                break;
        }
    }
}
