package cn.mdruby.pickphotovideoview;


/**
 * Created by wanbo on 2016/12/30.
 * PickPhotoView 配置文件
 */

public class PickConfig {
    // TAG
    public static final String TAG = "PickPhotoView";
    // intent data
    public static final String INTENT_PICK_DATA = "intent_pick_Data";
    // intent dirName
    public static final String INTENT_DIR_NAME = "intent_dir_name";
    // intent img path
    public static final String INTENT_IMG_PATH = "intent_img_path";
    // intent img list
    public static final String INTENT_IMG_LIST = "intent_img_list";
    // intent camera uri
    public static final String INTENT_CAMERA_URI = "intent_camera_uri";
    // intent img select list
    public static final String INTENT_IMG_LIST_SELECT = "intent_img_list_select";
    // all photos
//    public static final String ALL_PHOTOS = App.getContext().getString(R.string.pick_all_photo);
    public static final String ALL_PHOTOS = "所有图片";
    // Camera type
    public static final int CAMERA_TYPE = -1;
    // space
    public static final int ITEM_SPACE = 4;
    // intent requestCode
    public static final int PICK_PHOTO_DATA = 0x5521;
    // intent requestCode
    public static final int LIST_PHOTO_DATA = 0x0821;
    // intent requestCode
    public static final int CAMERA_PHOTO_DATA = 0x9949;
    // intent requestCode
    public static final int PREVIEW_PHOTO_DATA = 0x7763;
    // default size
    public static final int DEFAULT_PICK_SIZE = 9;
    // default span count
    public static final int DEFAULT_SPAN_COUNT = 4;
    // list scroll threshold
    public static final int SCROLL_THRESHOLD = 30;
    // toolbar icon color
//    public static final int PICK_BLACK_COLOR = R.color.pick_black;
//    public static final int PICK_WHITE_COLOR = R.color.pick_white;

    public static final String MEIDA_PREVIEW_MODEL = "intent_pre_view_model";

    public static class RequestCode {
        /**
         * 拍照(自定义相机)
         */
        public static final int TAKE_PHOTO_BY_SELF = 0x6756;

        /**
         * 选择照片
         */
        public static final int SELECT_PHOTO = 0x672;

        /**
         * 预览图片
         */
        public static final int PRE_PHOTO_CODE = 0x786;

        /**
         * 获取SD卡权限
         */
        public static final int GET_EXTERNAL_STORAGE_REQUEST_CODE = 0x875;
        public static final int CROP_IMAGE = 0x002;
    }

    public static class KEY{
        public static final String MEDIA_FILE_DATA = "media_file_data";
        public static final String PRE_PHOTO_FILE = "PRE_photo_file";
        public static final String MEDIA_COUNT = "media_count";
        public static final String MEDIA_NOW_COUNT = "media_now_count";
        public static final String PICK_DATA_INTENT = "pick_data_intent";
        public static final String MEDIA_DATA_ONE = "media_data_one";
        public static final String CROP_IMAGE_FILE_PATH = "crop_image_file_path";
    }
}
