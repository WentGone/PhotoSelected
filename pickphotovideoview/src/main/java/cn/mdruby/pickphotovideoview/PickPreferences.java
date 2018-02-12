package cn.mdruby.pickphotovideoview;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.content.SharedPreferences.Editor;
import android.text.TextUtils;

/**
 * Created by wanbo on 2017/1/3.
 */

public class PickPreferences {

    private static PickPreferences mInstance = null;
    private final SharedPreferences mSharedPreferences;
    private Context context;

    public static final String IMAGE_LIST = "image_list";
    private static final String DIR_NAMES = "dir_names";
    private static final String PICK_DATA = "pick_data";
    private GroupMedia listImage;
    private DirImage dirImage;
    private PickData pickData;


    public static PickPreferences getInstance(Context context) {
        if (mInstance == null) {
            synchronized (PickPreferences.class) {
                if (mInstance == null) {
                    mInstance = new PickPreferences(context);
                }
            }
        }
        return mInstance;
    }

    private PickPreferences(Context context) {
        this.context = context;
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public boolean saveImageList(GroupMedia images){
        listImage = images;
        Editor editor = mSharedPreferences.edit();
        editor.putString(IMAGE_LIST, PickGson.toJson(images));
        boolean result = editor.commit();
        return result;
    }

    public GroupMedia getListImage(){
        if(listImage == null) {
            String ss = mSharedPreferences.getString(IMAGE_LIST, "");
            if(TextUtils.isEmpty(ss)) {
                return null;
            } else {
                listImage = PickGson.fromJson(GroupMedia.class, ss);
            }
        }
        return listImage;
    }

    public boolean saveDirNames(DirImage images){
        dirImage = images;
        Editor editor = mSharedPreferences.edit();
        editor.putString(DIR_NAMES, PickGson.toJson(images));
        boolean result = editor.commit();
        return result;
    }

    public DirImage getDirImage(){
        if(dirImage == null) {
            String ss = mSharedPreferences.getString(DIR_NAMES, "");
            if(TextUtils.isEmpty(ss)) {
                return null;
            } else {
                dirImage = PickGson.fromJson(DirImage.class, ss);
            }
        }
        return dirImage;
    }

    public boolean savePickData(PickData data){
        pickData = data;
        Editor editor = mSharedPreferences.edit();
        editor.putString(PICK_DATA, PickGson.toJson(data));
        boolean result = editor.commit();
        return result;
    }

    public PickData getPickData(){
        if(pickData == null) {
            String ss = mSharedPreferences.getString(PICK_DATA, "");
            if(TextUtils.isEmpty(ss)) {
                return null;
            } else {
                pickData = PickGson.fromJson(PickData.class, ss);
            }
        }
        return pickData;
    }
}
