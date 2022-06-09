package org.jun1or.smallvideo.util;

import android.app.Activity;
import android.content.Context;
import android.util.TypedValue;
import android.view.Window;
import android.view.WindowManager;

/**
 * 显示相关
 * Created by Junior on 2018/1/25.
 */

public class DisplayUtil {

    /**
     * dp 转 px
     *
     * @param dpValue dp 值
     * @return px 值
     */
    public static int dp2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * px 转 dp
     *
     * @param pxValue px 值
     * @return dp 值
     */
    public static int px2dp(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    /**
     * 将sp值转换为px值
     *
     * @param spValue
     * @return
     */
    public static int sp2px(Context context, float spValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }

    /**
     * 将px值转换为sp值
     *
     * @param pxValue
     * @return
     */

    public static int px2sp(Context context, float pxValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (pxValue / fontScale + 0.5f);
    }

    /**
     * 获取屏幕宽度
     *
     * @return
     */
    public static int getScreenWidth(Context context) {
        return context.getResources().getDisplayMetrics().widthPixels;
    }

    /**
     * 获取屏幕高度
     *
     * @return
     */
    public static int getScreenHeight(Context context) {
        return context.getResources().getDisplayMetrics().heightPixels;
    }

    /**
     * 获取ActionBar高度
     *
     * @param context
     * @return
     */
    public static int getActionBarHeight(Context context) {
        int actionBarHeight = 0;
        TypedValue tv = new TypedValue();
        if (context.getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
            actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data,
                    context.getResources().getDisplayMetrics());
        }
        return actionBarHeight;
    }

    /**
     * 设置全屏
     *
     * @param activity
     */
    public static void setFullScreen(Activity activity) {
        Window window = activity.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

    }

    /**
     * 取消全屏
     *
     * @param activity
     */
    public static void cancelFullScreen(Activity activity) {
        Window window = activity.getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        window.clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
    }

    /**
     * 判断是否全屏
     *
     * @param activity
     * @return 是否全屏
     */
    public static boolean isFullScreen(Activity activity) {
        WindowManager.LayoutParams params = activity.getWindow().getAttributes();
        return (params.flags & WindowManager.LayoutParams.FLAG_FULLSCREEN) == WindowManager.LayoutParams.FLAG_FULLSCREEN;
    }

}
