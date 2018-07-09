package com.synertone.butterknife;

import android.app.Activity;
public class Butterknife {
    public static void bind(Activity activity) {
        try {
            Class<? extends Activity> activityClass = activity.getClass();
            ClassLoader classLoader = activityClass.getClassLoader();
            String className = activityClass.getName() + "$ViewBinder";
            Class<?> viewBinderClass = classLoader.loadClass(className);
            Object instance = viewBinderClass.newInstance();
            if(instance instanceof ViewBinder){
               ViewBinder viewBinder= (ViewBinder) instance;
                viewBinder.bind(activity);
            }
            if( instance instanceof ViewBinderClick){
                ViewBinderClick viewBinderClick= (ViewBinderClick) instance;
                viewBinderClick.onClick(activity);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
