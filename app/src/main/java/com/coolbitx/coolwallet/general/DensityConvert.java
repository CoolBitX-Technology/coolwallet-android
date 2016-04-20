package com.coolbitx.coolwallet.general;

import android.app.Activity;
import android.content.Context;
import android.util.DisplayMetrics;

/**
 * Created by kunmingliu on 15/9/12.
 */
public class DensityConvert {
    public static float mMagnificationWidth = 1.0f;
    public static float mMagnificationHeight = 1.0f;



    public DensityConvert(Context context) {

        DisplayMetrics dm = new DisplayMetrics();
        float density = dm.density; // 屏幕密度（像素比例：0.75/1.0/1.5/2.0）
        int densityDPI = dm.densityDpi; // 屏幕密度（每寸像素：120/160/240/320）
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(dm); // 取得螢幕實際寬高尺寸
        int scaleWidth = dm.widthPixels; // 螢幕寬(dip)
        int scaleHeight = dm.heightPixels;// 螢幕高(dip)


        float scaledDensity = 2.0f;
        float magnificationscaledDensity = 1.0f;
        scaledDensity = context.getResources().getDisplayMetrics().scaledDensity;
        magnificationscaledDensity = (float) ((float) scaledDensity / 2);
        mMagnificationWidth = (float) ((float) scaleWidth / 720) / magnificationscaledDensity;
        mMagnificationHeight = (float) ((float) scaleHeight / 1280) / magnificationscaledDensity;
    }


}
