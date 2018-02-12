package cn.mdruby.pickphotovideoview;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;

/**
 * Created by wanbo on 2017/1/3.
 */

public class PickGson {

    private static Gson gson = new Gson();

    public synchronized static <T> T fromJson(Class<T> cls, String srcStr) {
        T result;
        if (TextUtils.isEmpty(srcStr)) {
            return null;
        }
        try {
            result = gson.fromJson(srcStr, cls);
        } catch (Exception e) {
            e.printStackTrace();
            result = null;
        }
        return result;
    }

    public synchronized static  <T> T getObj(T t, String s){
        Type type = new TypeToken<T>() {
        }.getType();
        T o = gson.fromJson(s, type);
        return o;
    }

    public synchronized static String toJson(Object object) {
        return gson.toJson(object);
    }


}
