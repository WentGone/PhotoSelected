package cn.mdruby.pickphotovideoview.abstracts;

import cn.mdruby.pickphotovideoview.OnRVItemClickListener;

/**
 * Created by Administrator on 2018/1/7.
 */

public interface OnItemPhotoClickListener{
    void onCameraClick();
    void onPhotoClick(int position);
    void onVideoClick(int position);
    void onSelectClick(int position);
}
