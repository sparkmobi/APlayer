package remix.myplayer.utils;

import android.content.Context;
import android.content.SharedPreferences;

import java.security.KeyStore;

/**
 * Created by taeja on 16-1-15.
 */
public class SharedPrefsUtil {
    public static SharedPrefsUtil mInstance;
    public static final String PLAYLIST = "PlayList";

    public SharedPrefsUtil() {
        if(mInstance == null)
            mInstance = this;
    }

    public static void putValue(Context context,String name,String key,int value)
    {
        SharedPreferences.Editor editor = context.getSharedPreferences(name,Context.MODE_PRIVATE).edit();
        editor.putInt(key,value).apply();
    }
    public static void putValue(Context context,String name,String key,String value)
    {
        SharedPreferences.Editor editor = context.getSharedPreferences(name,Context.MODE_PRIVATE).edit();
        editor.putString(key,value).apply();
    }
    public static int getValue(Context context,String name,String key,int dft)
    {
        return context.getSharedPreferences(name,Context.MODE_PRIVATE).getInt(key,dft);
    }
    public static String getValue(Context context,String name,String key,String dft)
    {
        return context.getSharedPreferences(name,Context.MODE_PRIVATE).getString(key,dft);
    }
    public static void deleteValue(Context context,String name,String key)
    {
        SharedPreferences.Editor editor = context.getSharedPreferences(name,Context.MODE_PRIVATE).edit();
        editor.remove(key).apply();
    }
    public static void deleteFile(Context context,String name)
    {
        SharedPreferences.Editor editor = context.getSharedPreferences(name,Context.MODE_PRIVATE).edit();
        editor.clear().apply();
    }
}