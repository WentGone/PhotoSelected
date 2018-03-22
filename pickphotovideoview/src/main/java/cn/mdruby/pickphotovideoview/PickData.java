package cn.mdruby.pickphotovideoview;

import java.io.Serializable;

/**
 * Created by Went_Gone on 2018/1/5.
 */

public class PickData implements Serializable{
    private boolean showCamera;
    private boolean showVideo;
    private boolean useLocalCamera = false;
    private int count;
    private int bottomBarViewRes;
    private boolean showChecked;
    private boolean showBottomBar = true;

    public boolean isShowCamera() {
        return showCamera;
    }

    public void setShowCamera(boolean showCamera) {
        this.showCamera = showCamera;
    }

    public boolean isShowVideo() {
        return showVideo;
    }

    public void setShowVideo(boolean showVideo) {
        this.showVideo = showVideo;
    }

    public boolean isUseLocalCamera() {
        return useLocalCamera;
    }

    public void setUseLocalCamera(boolean useLocalCamera) {
        this.useLocalCamera = useLocalCamera;
    }

    public int isCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getBottomBarViewRes() {
        return bottomBarViewRes;
    }

    public void setBottomBarViewRes(int bottomBarViewRes) {
        this.bottomBarViewRes = bottomBarViewRes;
    }

    public boolean isShowChecked() {
        return showChecked;
    }

    public void setShowChecked(boolean showChecked) {
        this.showChecked = showChecked;
    }

    public boolean isShowBottomBar() {
        return showBottomBar;
    }

    public void setShowBottomBar(boolean showBottomBar) {
        this.showBottomBar = showBottomBar;
    }
}
