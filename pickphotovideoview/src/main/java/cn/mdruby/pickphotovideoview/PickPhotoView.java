package cn.mdruby.pickphotovideoview;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import cn.mdruby.pickphotovideoview.activity.PickPhotoActivity;

/**
 *
 * Created by Went_Gone on 2018/1/5.
 */
public class PickPhotoView {
    private Context context;
    private PickData pickData;

    public PickPhotoView(Bulid bulid){
        pickData = bulid.pickData;
        context = bulid.context;
    }

    private void startPickActivity(){
        Intent intent = new Intent(context,PickPhotoActivity.class);
        intent.putExtra(PickPhotoActivity.PICK_DATA,pickData);
        ((Activity)context).startActivityForResult(intent,PickConfig.RequestCode.SELECT_PHOTO);
    }


    public static class Bulid{
        private PickData pickData;
        private Context context;

        public Bulid(Context context) {
            pickData = new PickData();
            this.context = context;
        }

        public Bulid showCamera(boolean showCamera){
            pickData.setShowCamera(showCamera);
            return this;
        }

        public Bulid showVideo(boolean showVideo){
            pickData.setShowVideo(showVideo);
            return this;
        }

        public Bulid useLocalCamera(boolean use){
            pickData.setUseLocalCamera(use);
            return this;
        }

        /**
         * 最多可以选择几张照片
         * @param count
         * @return
         */
        public Bulid setCount(int count){
            pickData.setCount(count);
            return this;
        }

        public Bulid setBottomBarViewRes(int viewLayoutRes){
            pickData.setBottomBarViewRes(viewLayoutRes);
            return this;
        }


        public Bulid showBottomBar(boolean show){
            pickData.setShowBottomBar(show);
            return this;
        }

        public Bulid showCheckedIcon(boolean show){
            pickData.setShowChecked(show);
            return this;
        }

        /**
         * 是否可以压缩
         * @param canZip
         * @return
         */
        public Bulid setCanZip(boolean canZip){
            pickData.setCanZip(canZip);
            return this;
        }

        /**
         * 是否可以剪裁  [暂时只能与单张图片剪裁]
         * @param canCrop
         * @return
         */
        public Bulid setCanCrop(boolean canCrop){
            pickData.setCanCrop(canCrop);
            return this;
        }

        private PickPhotoView create(){
            return new PickPhotoView(this);
        }

        public void start(){
            create().startPickActivity();
        }
    }
}
