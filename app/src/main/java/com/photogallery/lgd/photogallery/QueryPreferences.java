package com.photogallery.lgd.photogallery;

import android.content.Context;
import android.preference.PreferenceManager;

/**
 * Created by Administrator on 2017/4/22.
 */

public class QueryPreferences {

    private static final String PREF_SEARCH_QUERY = "searchQuery";//用作查询字符串的存储key，读取写入时都要用到它

    //读取查询
    public static String getStoredQuery(Context context){
        return PreferenceManager.getDefaultSharedPreferences(context).getString(PREF_SEARCH_QUERY, null);
    }

    //写入查询
    public static void setStoredQuery(Context context, String query){
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putString(PREF_SEARCH_QUERY, query)
                .apply();
    }
}
