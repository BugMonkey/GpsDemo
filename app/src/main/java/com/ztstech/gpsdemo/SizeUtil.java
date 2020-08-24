package com.ztstech.gpsdemo;

import android.content.Context;
import android.graphics.Point;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.WindowManager;

/**
 * Created by smm on 2017/6/1.
 * 各种尺寸相关工具类
 */

public class SizeUtil {

    /**
     * 获得屏幕宽度
     */
    @SuppressWarnings("deprecation")
    public static int getPhoneWidth(Context ctx) {
        if (ctx == null) {
            return 0;
        }
        WindowManager wm = (WindowManager) ctx
                .getSystemService(Context.WINDOW_SERVICE);

        int width = wm.getDefaultDisplay().getWidth();
        return width;
    }

    // 获得屏幕宽度
    public static int getScreenWidth(Context context) {
        if (context == null) {
            return 0;
        }
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(outMetrics);
        return outMetrics.widthPixels;
    }

    /**
     * 获得屏幕高度
     */
    @SuppressWarnings("deprecation")
    public static int getPhoneHeight(Context ctx) {
        if (ctx == null) {
            return 0;
        }
        WindowManager wm = (WindowManager) ctx
                .getSystemService(Context.WINDOW_SERVICE);

        int height = wm.getDefaultDisplay().getHeight();
        return height;
    }


    /**
     * 获取屏幕高度，适配虚拟键的高度()
     * 解决部分弹窗无法占满底部屏幕的问题
     *
     * @param context
     * @return
     */
    public static int getScreenHeight(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        if (wm == null) {
            return context.getResources().getDisplayMetrics().heightPixels;
        }
        Point point = new Point();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            wm.getDefaultDisplay().getRealSize(point);
        } else {
            wm.getDefaultDisplay().getSize(point);
        }
        return point.y;
    }

    /**
     * 虚拟键的高度
     *
     *
     * @param context
     * @return
     */
    public static int getNavigatorHeight(Context context) {
        int result = 0;
        int resourceId = context.getResources().getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    /**
     * 获得屏幕高宽比
     */
    public static double getPhoneScale(Context context) {
        return getPhoneHeight(context) / getPhoneWidth(context);
    }

    /**
     * 将dp转换成px
     */
    public static int dip2px(Context context, int dpValue) {
        if (context == null) {
            return dpValue;
        }
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * 根据手机的分辨率从 px(像素)转成为 dp
     */
    public static int px2dip(Context context, int pxValue) {
        if (context == null) {
            return pxValue;
        }
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    // 将px值转换为sp值，保证文字大小不变
    public static int px2sp(Context context, float pxValue) {
        if (context == null) {
            return (int) pxValue;
        }
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (pxValue / fontScale + 0.5f);
    }

    // 将sp值转换为px值，保证文字大小不变
    public static int sp2px(Context context, float spValue) {
        if (context == null) {
            return (int) spValue;
        }
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }



}
