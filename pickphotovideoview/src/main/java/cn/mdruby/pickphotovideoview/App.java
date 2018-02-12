package cn.mdruby.pickphotovideoview;

import android.app.Application;
import android.content.Context;


/**
 * Created by Went_Gone on 2018/1/8.
 */

public class App extends Application {
    private static App app;
    public static App getContext(){
        return app;
    }


    @Override
    public void onCreate() {
        app = this;
        super.onCreate();
    }
}
