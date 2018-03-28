package cn.mdruby.pickphotovideoview.ui;

import android.app.Dialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.StyleRes;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;

import cn.mdruby.pickphotovideoview.R;


/**
 * Created by Went_Gone on0 2017/11/28.
 */

public class CustomLoadingDailog extends Dialog {
    private Context context;
    private ImageView mIVloading;

    public CustomLoadingDailog(@NonNull Context context) {
        this(context,0);
    }

    public CustomLoadingDailog(@NonNull Context context, @StyleRes int themeResId) {
        super(context, R.style.NoDialogTitleView);
        this.context = context;
        setContentView(R.layout.layout_loading_ui);
        initViews();
        setAnimation();

        Window dialogWindow = this.getWindow();
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
        lp.height = ViewGroup.LayoutParams.MATCH_PARENT;
        setCanceledOnTouchOutside(false);// 设置点击屏幕Dialog不消失
        lp.alpha = 0.7f; // 透明度
        dialogWindow.setAttributes(lp);
    }

    private void initViews() {
        mIVloading = findViewById(R.id.layout_loading_ui_IV);
    }

    private void setAnimation() {
        final RotateAnimation animation =new RotateAnimation(0f,360f, Animation.RELATIVE_TO_SELF,
                0.5f,Animation.RELATIVE_TO_SELF,0.5f);
        animation.setDuration(2500);//设置动画持续时间
        animation.setRepeatCount(Animation.INFINITE);
        mIVloading.setAnimation(animation);
        /** 开始动画 */
        animation.startNow();
    }

    @Override
    public void show() {
        super.show();
        setAnimation();
    }
}
