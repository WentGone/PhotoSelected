package cn.mdruby.pickphotovideoview.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.io.Serializable;
import java.util.List;

import cn.mdruby.pickphotovideoview.MediaModel;
import cn.mdruby.pickphotovideoview.PickConfig;
import cn.mdruby.pickphotovideoview.PickData;
import cn.mdruby.pickphotovideoview.PickPhotoView;
import cn.mdruby.pickphotovideoview.R;
import cn.mdruby.pickphotovideoview.ui.SlideViewPager;
import uk.co.senab.photoview.PhotoView;
import uk.co.senab.photoview.PhotoViewAttacher;

public class PickPhotoPreviewActivity extends AppCompatActivity{
    public static final String MEDIA_DATAS = "media_datas";
    public static final String POSITION_COUNT = "position_count";
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private SlideViewPager mViewPager;
    private static List<MediaModel> mediaModels;
    private int position = 0;
    private static ImageView mIVselected;
    private int count;
    private int nowCount;
    private boolean showCheckedIcon;
    private PickData pickData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pick_photo_preview);
        mediaModels = (List<MediaModel>) getIntent().getSerializableExtra(MEDIA_DATAS);
        position = getIntent().getIntExtra(POSITION_COUNT,0);
        count = getIntent().getIntExtra(PickConfig.KEY.MEDIA_COUNT,0);
        nowCount = getIntent().getIntExtra(PickConfig.KEY.MEDIA_NOW_COUNT,0);
        pickData = (PickData) getIntent().getSerializableExtra(PickConfig.KEY.PICK_DATA_INTENT);
        showCheckedIcon = pickData.isShowChecked();

        mIVselected = (ImageView) findViewById(R.id.act_pick_photo_preview_IV_selected);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("");
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        mViewPager = (SlideViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setCurrentItem(position);
        mIVselected.setVisibility(showCheckedIcon?View.VISIBLE:View.GONE);
        if (showCheckedIcon){
            mIVselected.setImageResource(mediaModels.get(position).isSelected()?R.mipmap.pick_ic_select:R.mipmap.pick_ic_un_select);
        }
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                MediaModel mediaModel = mediaModels.get(position);
                if (showCheckedIcon){
                    mIVselected.setImageResource(mediaModel.isSelected()?R.mipmap.pick_ic_select:R.mipmap.pick_ic_un_select);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        mIVselected.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int currentItem = mViewPager.getCurrentItem();
                if (mediaModels.get(currentItem).isSelected()){
                    //一开始选中
                    nowCount-=1;
                }
                if (nowCount<count){
                    mediaModels.get(currentItem).setSelected(!mediaModels.get(currentItem).isSelected());
                    if (mediaModels.get(currentItem).isSelected()){
                        nowCount+=1;
                    }
                    if (showCheckedIcon){
                        mIVselected.setImageResource(mediaModels.get(currentItem).isSelected()?R.mipmap.pick_ic_select:R.mipmap.pick_ic_un_select);
                    }
                }else {
                    Toast.makeText(PickPhotoPreviewActivity.this, "选择的图片已达"+count+"张", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            //重写ToolBar返回按钮的行为，防止重新打开父Activity重走生命周期方法
            case android.R.id.home:
                Intent intent = getIntent();
                intent.putExtra(PickConfig.KEY.PRE_PHOTO_FILE, (Serializable) mediaModels);
                setResult(RESULT_OK,intent);
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        Intent intent = getIntent();
        intent.putExtra(PickConfig.KEY.PRE_PHOTO_FILE, (Serializable) mediaModels);
        setResult(RESULT_OK,intent);
        super.onBackPressed();
    }

    public static class PlaceholderFragment extends Fragment {
        private static final String ARG_SECTION_NUMBER = "section_number";
        PhotoView iv;

        public PlaceholderFragment() {
        }

        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_pick_photo_preview, container, false);
            iv = rootView.findViewById(R.id.frag_pick_photo_preview);
            Glide.with(getActivity()).load(mediaModels.get(getArguments().getInt(ARG_SECTION_NUMBER)).getPath()).into(iv);
            PhotoViewAttacher mAttacher = new PhotoViewAttacher(iv);
            mAttacher.update();
//            PickPhotoPreviewActivity.mIVselected.setImageResource(mediaModels.get(getArguments().getInt(ARG_SECTION_NUMBER)).isSelected()?R.mipmap.pick_ic_select:R.mipmap.pick_ic_un_select);
            return rootView;
        }

    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
//            return PlaceholderFragment.newInstance(position + 1);
            return PlaceholderFragment.newInstance(position);
        }

        @Override
        public int getCount() {
            return mediaModels == null?0:mediaModels.size();
        }
    }
}
