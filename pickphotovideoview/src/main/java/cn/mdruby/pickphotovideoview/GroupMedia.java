package cn.mdruby.pickphotovideoview;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Went_Gone on 2018/1/5.
 */

public class GroupMedia implements Serializable {
    private HashMap<String,List<MediaModel>> mGroupMedias;

    public HashMap<String, List<MediaModel>> getGroupMedias() {
        return mGroupMedias;
    }

    public void setGroupMedias(HashMap<String, List<MediaModel>> mGroupMedias) {
        this.mGroupMedias = mGroupMedias;
    }
}
