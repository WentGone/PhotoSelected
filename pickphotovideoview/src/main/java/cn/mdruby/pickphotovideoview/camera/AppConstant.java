package cn.mdruby.pickphotovideoview.camera;

public class AppConstant {

    //WHAT 0-10 预留值
    public interface WHAT {
        int SUCCESS = 0;
        int FAILURE = 1;
        int ERROR = 2;
        int START_VIDEO = 3;
        int STOP_VIDEO = 4;
    }

    public interface KEY{
        String IMG_PATH = "IMG_PATH";
        String VIDEO_PATH = "VIDEO_PATH";
        String PIC_WIDTH = "PIC_WIDTH";
        String PIC_HEIGHT = "PIC_HEIGHT";
        String MEDIA_TYPE = "media_type";
    }

    public interface REQUEST_CODE {
        int CAMERA = 0;
    }

    public interface RESULT_CODE {
        int RESULT_OK = -1;
        int RESULT_CANCELED = 0;
        int RESULT_ERROR = 1;
    }

}
