package cn.mdruby.pickphotovideoview.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
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
import android.text.TextUtils;
import android.util.Log;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

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
import cn.mdruby.pickphotovideoview.camera.activity.CameraActivity;
import cn.mdruby.pickphotovideoview.camera.activity.CameraVideoActivity;
import cn.mdruby.pickphotovideoview.ui.CustomLoadingDailog;
import cn.mdruby.pickphotovideoview.ui.DividerItemDecoration;
import cn.mdruby.pickphotovideoview.util.PickUtils;
import top.zibin.luban.Luban;
import top.zibin.luban.OnCompressListener;

public class PickPhotoActivity extends AppCompatActivity implements OnItemPhotoClickListener{
    private static final String TAG = "PickPhotoActivity";
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
    private GroupMedia groupImage;
    private DirImage dirImage;
    private List<String> dirImageStrings;
    private HashMap<String,List<MediaModel>> groupImages;
    private LinearLayout mLLayoutContent;
    private int bottomBarViewRes = 0;
    private boolean showCheckedIcon = true;
    private boolean showBottomBar = true;
    private CustomLoadingDailog loading;
    private boolean canZip = false;
    private boolean canCrop = false;
    private String cropPath = "";
    private Uri outputUri;
    private boolean single = false;
    private boolean cameraComeBack = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pick_photo);
        initDataBefor();

        initPickData();

        mDatas = new ArrayList<>();
        mSelecteds = new ArrayList<>();
        mRV = (RecyclerView) findViewById(R.id.act_pick_photo_RV);
        mRVList = (RecyclerView) findViewById(R.id.act_pick_photo_list_RV);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.act_pick_DrawerLayout);
        toolbar = (Toolbar) findViewById(R.id.tl_custom);
        mTVtitle = (TextView) findViewById(R.id.act_pick_TV_title);
        mLLayoutContent = (LinearLayout) findViewById(R.id.act_pick_photo_LLayout_Content);
        loading = new CustomLoadingDailog(this);

        initBottomBar();

        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        mDrawerToggle = new OnDragScrollListener(this, mDrawerLayout, toolbar, R.string.open, R.string.close);
        mDrawerToggle.syncState();
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        mRV.setLayoutManager(new GridLayoutManager(this,4,GridLayoutManager.VERTICAL,false));
        mRVList.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new RVPhotoGridAdapter(this,mDatas,showCamera);
        mAdapter.setShowCheckIcon(showCheckedIcon);
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

    /**
     * 初始化pickDAta数据
     */
    private void initPickData() {
        pickData = (PickData) getIntent().getSerializableExtra(PICK_DATA);
        showVideo = pickData.isShowVideo();
        showCamera = pickData.isShowCamera();
        selectedCount = pickData.isCount();
        useLocalCamera = pickData.isUseLocalCamera();
        bottomBarViewRes = pickData.getBottomBarViewRes();
        showCheckedIcon = pickData.isShowChecked();
        showBottomBar = pickData.isShowBottomBar();
        canZip = pickData.isCanZip();
        canCrop = pickData.isCanCrop();
        single = pickData.isSingle();
        cameraComeBack = pickData.isCameraComeBack();
        //如果设置一张图片  那么为单选
        if (selectedCount == 1){
            single = true;
        }
    }

    private void initBottomBar() {
        int childCount = mLLayoutContent.getChildCount();
        View view = LayoutInflater.from(this).inflate((childCount<2 && bottomBarViewRes>0)?bottomBarViewRes:R.layout.pick_photo_bottom_bar,mLLayoutContent,false);
        if (showBottomBar){
            mLLayoutContent.addView(view);
        }
        mTVselected = (TextView) view.findViewById(R.id.act_pick_TV_bottom_selected);
        mTVcount = (TextView) view.findViewById(R.id.act_pick_TV_bottom_count);
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
//                this.finish();
            }
        }
    }

    private void callback() {
        if (mSelecteds.size()>0){
            if (canCrop && mSelecteds.size()==1){
                crop();
            }else {
                if (canZip){
                    if (!loading.isShowing()){
                        loading.show();
                    }
                    zip();
                }else {
                    Intent intent = getIntent();
                    intent.putExtra(PickConfig.KEY.MEDIA_FILE_DATA, (Serializable) mSelecteds);
                    setResult(RESULT_OK,intent);
                    PickPhotoActivity.this.finish();
                }
            }
        }else {
            Toast.makeText(this, "请选择一张图片", Toast.LENGTH_SHORT).show();
        }

    }

    /**
     * 裁剪
     */
    private void crop(){
        MediaModel mediaModel = mSelecteds.get(0);
        if (mediaModel.getFile() == null){
            mediaModel.setFile(new File(mediaModel.getPath()));
        }
        File file = new File(Environment.getExternalStorageDirectory(),System.currentTimeMillis()+".jpg");
        outputUri = Uri.fromFile(file);
        cropPath = outputUri.getPath();
        Intent intent = new Intent(this,PickCropActivity.class);
        intent.putExtra(PickConfig.KEY.MEDIA_DATA_ONE,mediaModel);
        startActivityForResult(intent,PickConfig.RequestCode.CROP_IMAGE);
    }

    private int counts = 0;

    /**
     * 压缩
     */
    private void zip(){
        if (loading.isShowing()){
            loading.show();
        }
        List<String> pathsSelected = new ArrayList<>();
        counts = 0;
        for (final MediaModel model :
                mSelecteds) {
            pathsSelected.add(TextUtils.isEmpty(model.getCropPath())?model.getPath():model.getCropPath());
            Luban.with(this)
                    .load(model.getFile())
                    .setCompressListener(new OnCompressListener() {
                        @Override
                        public void onStart() {

                        }

                        @Override
                        public void onSuccess(File file) {
                            counts++;
                            model.setFile(file);
                            if (counts == mSelecteds.size()){
                                loading.dismiss();
                                Intent intent = getIntent();
                                intent.putExtra(PickConfig.KEY.MEDIA_FILE_DATA, (Serializable) mSelecteds);
                                setResult(RESULT_OK,intent);
                                PickPhotoActivity.this.finish();
                            }
                        }

                        @Override
                        public void onError(Throwable e) {
                            counts++;
                            if (counts == mSelecteds.size()){
                                loading.dismiss();
                                Intent intent = getIntent();
                                intent.putExtra(PickConfig.KEY.MEDIA_FILE_DATA, (Serializable) mSelecteds);
                                setResult(RESULT_OK,intent);
                                PickPhotoActivity.this.finish();
                            }
                        }
                    }).launch();
        }
    }

    /**
     * 点击列表中的某一项
     * @param position
     */
    private void clickListItem(int position) {
        List<MediaModel> item = mListAdapter.getItem(position);
        if (mTVcount != null){
            mTVcount.setText("");
        }
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
//            Intent intent = new Intent(this, CameraVideoActivity.class);
            Intent intent = new Intent(this, CameraActivity.class);
            intent.putExtra(PICK_DATA,pickData);
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
        intent.putExtra(PickConfig.KEY.PICK_DATA_INTENT,pickData);
        startActivityForResult(intent,PickConfig.RequestCode.PRE_PHOTO_CODE);

//        String path = mediaModel.getPath();

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
        MediaModel item = mAdapter.getItem(position);
        if (mSelecteds.contains(item)){
            item.setSelected(!item.isSelected());
            mAdapter.notifyItemChanged(showCamera?(position+1):position);
            setSelected(item);
            mTVcount.setText(mSelecteds.size()+"");
        }else {
            if (mSelecteds.size()<selectedCount){
                item.setSelected(!item.isSelected());
                mAdapter.notifyItemChanged(showCamera?(position+1):position);
                setSelected(item);
                mTVcount.setText(mSelecteds.size()+"");
            }else {
                if (!single){
                    Toast.makeText(this, "选择的图片不能超过"+selectedCount+"张", Toast.LENGTH_SHORT).show();
                }
            }
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
                    if (!cameraComeBack){
                        mDatas.add(0,mediaModel);
                        mAdapter.notifyDataSetChanged();
                    }else {
                        //回调
                        mSelecteds.add(mediaModel);
                        callback();
                    }
                }else if (resultCode == 101){
                    String path = data.getStringExtra("path");
                    Log.e(TAG, "onActivityResult: "+path);
                    MediaModel mediaModel = new MediaModel();
                    mediaModel.setFile(new File(path));
                    mediaModel.setPath(path);
                    mediaModel.setThumPath(path);
                    mediaModel.setMimeType(path.endsWith(".mp4")?"video":"image");
                    if (path.endsWith(".mp4")){
                        MediaPlayer mediaPlayer = new MediaPlayer();
                        try {
                            mediaPlayer.setDataSource(path);
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
                    if (!cameraComeBack){
                        mDatas.add(0,mediaModel);
                        mAdapter.notifyDataSetChanged();
                    }else {
                        //回调
                        mSelecteds.add(mediaModel);
                        callback();
                    }
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
                mediaModel.setFile(new File(path));
//                mediaModels.add(mediaModel);
                if (cameraComeBack){
                    mSelecteds.add(mediaModel);
                    if (canZip){
                        if (!loading.isShowing()){
                            loading.show();
                        }
                        zip();
                    }else {
                        Intent intentReturn = getIntent();
                        intentReturn.putExtra(PickConfig.KEY.MEDIA_FILE_DATA, (Serializable) mSelecteds);
                        setResult(RESULT_OK,intentReturn);
                        PickPhotoActivity.this.finish();
                    }
                }else {
                    mDatas.add(0,mediaModel);
                    mAdapter.notifyDataSetChanged();
                }
                break;

            case PickConfig.RequestCode.CROP_IMAGE:
                if (resultCode == RESULT_OK){
                    cropPath = data.getStringExtra(PickConfig.KEY.CROP_IMAGE_FILE_PATH);
                    mSelecteds.get(0).setCropPath(cropPath);
                    mSelecteds.get(0).setFile(new File(cropPath));
                    if (canZip){
                         if (!loading.isShowing()){
                            loading.show();
                         }
                        zip();
                    }else {
                        Intent intentReturn = getIntent();
                        intentReturn.putExtra(PickConfig.KEY.MEDIA_FILE_DATA, (Serializable) mSelecteds);
                        setResult(RESULT_OK,intentReturn);
                        PickPhotoActivity.this.finish();
                    }
                }
                break;
        }
    }
}
