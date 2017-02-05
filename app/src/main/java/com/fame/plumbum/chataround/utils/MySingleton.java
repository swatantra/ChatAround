package com.fame.plumbum.chataround.utils;

import android.support.multidex.MultiDexApplication;
import android.text.TextUtils;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

/**
 * Created by pankaj on 24/7/16.
 */
public class MySingleton extends MultiDexApplication {

    public static final String TAG = MySingleton.class.getSimpleName();
    private static MySingleton mInstance;
    private RequestQueue requestQueue;

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
        requestQueue = getRequestQueue();
    }

    public static synchronized MySingleton getInstance() {
        return mInstance;
    }

    public RequestQueue getRequestQueue() {
        if (requestQueue == null)
            requestQueue = Volley.newRequestQueue(getApplicationContext());

        return requestQueue;
    }

    public <T>void addToRequestQueue(Request<T> req, String tag) {
        req.setTag(TextUtils.isEmpty(tag) ? TAG : tag);
        req.setRetryPolicy(new DefaultRetryPolicy(5000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        getRequestQueue().add(req);
    }

    public <T>void addToRequestQueue(Request<T> req) {
        getRequestQueue().add(req);
    }


}
