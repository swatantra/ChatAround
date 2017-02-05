package com.fame.plumbum.chataround.activity;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.fame.plumbum.chataround.R;
import com.fame.plumbum.chataround.fragments.MyProfile;
import com.fame.plumbum.chataround.fragments.World;
import com.fame.plumbum.chataround.utils.Constants;
import com.fame.plumbum.chataround.utils.MySingleton;
import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by pankaj on 4/8/16.
 */
public class MainActivity extends AppCompatActivity{
    public double lat, lng;
    public boolean needSomethingTweet = false, needSomethingWorld = false;
    MyProfile profile;
    World world;
    BroadcastReceiver receiver;
    SharedPreferences sp;
    public int count = 0;
    String token;
    // 11-07 04:36:24.843 9484-9484/com.fame.plumbum.chataround E/response: {"Status": 200, "Message": "Posted", "PostId": "581fb7047c4ec2677d237669"}


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_window);

        if (receiver==null) {
            IntentFilter filter = new IntentFilter("Hello World");
            receiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    if (intent.getAction().contentEquals("Hello World")) {
                        lat = intent.getDoubleExtra("lat", 0.0);
                        lng = intent.getDoubleExtra("lng", 0.0);
                        world.lat = lat;
                        world.lng = lng;
                        if (needSomethingTweet || needSomethingWorld) {
                            needSomethingWorld = false;
                            needSomethingTweet = false;
                            getAllPosts(count);
                        }
                    }
                }
            };
            registerReceiver(receiver, filter);
        }
        sp = PreferenceManager.getDefaultSharedPreferences(this);
        initFCM();
        getSupportActionBar();
        getSupportActionBar().setDisplayHomeAsUpEnabled(false); // remove the left caret
        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        setupViewPager(viewPager);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.getTabAt(0).setIcon(R.drawable.user);
        tabLayout.getTabAt(1).setIcon(R.drawable.world);

        viewPager.setCurrentItem(1);
    }

    private void initFCM() {
        if (!sp.contains("token")){
            SharedPreferences.Editor editor = sp.edit();
            if (FirebaseInstanceId.getInstance()!=null){
                token = FirebaseInstanceId.getInstance().getToken();
                if (token != null) {
                    editor.putString("token", token);
                    editor.apply();
                    sendFCM(sp.getString("uid", ""));
                }
            }
        }else {
            sendFCM(sp.getString("uid", ""));
        }
    }

    public void setupViewPager(ViewPager upViewPager) {
        profile = new MyProfile();
        world = new World();
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(profile, "My Profile");
        adapter.addFragment(world, "World");
        upViewPager.setAdapter(adapter);
    }

    public void getAllPosts(int counter){
        StringRequest stringRequest = new StringRequest(Request.Method.GET, Constants.BASE_URL_DEFAULT + "ShowPost?UserId=" + profile.uid + "&Counter=" + counter + "&Latitude=" + lat + "&Longitude=" + lng,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            if (response!=null && response.length()>0) {
                                JSONObject jo = new JSONObject(response);
                                if (world != null && world.swipeRefreshLayout != null) {
                                    world.swipeRefreshLayout.setRefreshing(false);
                                }
                                if (profile != null && profile.swipeRefreshLayout != null) {
                                    profile.swipeRefreshLayout.setRefreshing(false);
                                }
                                if (jo.getJSONArray("Posts").length() > 0) {
                                    if (world != null && world.swipeRefreshLayout != null) {
                                        world.getAllPosts(response, count);
                                    }
                                    if (profile != null && profile.swipeRefreshLayout != null) {
                                        profile.getAllPosts(response, count);
                                    }
                                } else {
                                    Toast.makeText(MainActivity.this, "No more posts found!", Toast.LENGTH_SHORT).show();
                                    needSomethingTweet = false;
                                    needSomethingWorld = false;
                                    if (count>0) count -= 1;
                                }
                            }
                        } catch (JSONException ignored) {
                            if (world != null && world.swipeRefreshLayout != null) {
                                world.swipeRefreshLayout.setRefreshing(false);
                            }
                            if (profile != null && profile.swipeRefreshLayout != null) {
                                profile.swipeRefreshLayout.setRefreshing(false);
                            }
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (world != null && world.swipeRefreshLayout != null) {
                    world.swipeRefreshLayout.setRefreshing(false);
                }
                if (profile != null && profile.swipeRefreshLayout != null) {
                    profile.swipeRefreshLayout.setRefreshing(false);
                }
                Toast.makeText(MainActivity.this, "Error receiving data!", Toast.LENGTH_SHORT).show();
            }
        });
        MySingleton.getInstance().addToRequestQueue(stringRequest);
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return null;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_inside, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(id == R.id.action_shout){
            final Dialog dialog = new Dialog(MainActivity.this);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.dialog_create_post);
            final EditText content = (EditText) dialog.findViewById(R.id.post_content);
            final TextView mTextView = (TextView) dialog.findViewById(R.id.num_chars);
            final TextWatcher mTextEditorWatcher = new TextWatcher() {
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    //This sets a textview to the current length
                    mTextView.setText(String.valueOf(140-s.length())+"/140");
                }

                public void afterTextChanged(Editable s) {
                }
            };
            content.addTextChangedListener(mTextEditorWatcher);


            Button create_post = (Button) dialog.findViewById(R.id.post_button);
            create_post.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String content_txt = content.getText().toString();
                    if (lat != 0 && lng != 0) {
                        content_txt = content_txt.replace("\n", "%0A");
                        final String finalContent_txt = content_txt;
                        StringRequest stringRequest = new StringRequest(Request.Method.GET, Constants.BASE_URL_DEFAULT + "Post?UserId=" + profile.uid + "&UserName=" + profile.name + "&Post=" + content_txt.replace(" ", "%20") + "&Latitude=" + lat + "&Longitude=" + lng,
                                new Response.Listener<String>() {
                                    @Override
                                    public void onResponse(String response) {
                                        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                                        Log.e("MainActiv", response);
                                        try {
                                            Log.e("token", Constants.BASE_URL_DBMS + "post?token=" + sp.getString("token_animesh", "") + "&content=" + finalContent_txt.replace(" ", "%20") + "&ext_id=" + new JSONObject(response).getString("PostId"));
                                            StringRequest sr = new StringRequest(Request.Method.GET, Constants.BASE_URL_DBMS + "post?token" + sp.getString("token_animesh", "") + "&content=" + finalContent_txt.replace(" ", "%20") + "&ext_id=" + new JSONObject(response).getString("PostId"), new Response.Listener<String>() {
                                                @Override
                                                public void onResponse(String response) {
                                                    needSomethingWorld = true;
                                                }
                                            }, new Response.ErrorListener() {
                                                @Override
                                                public void onErrorResponse(VolleyError error) {
                                                    needSomethingWorld = true;
                                                }
                                            });
                                            MySingleton.getInstance().addToRequestQueue(sr);
                                        } catch (JSONException e) {
                                            Log.e("Error MA Anim", e.toString());
                                            e.printStackTrace();
                                        }
                                    }
                                }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Toast.makeText(MainActivity.this, "Error sending data!", Toast.LENGTH_SHORT).show();
                            }
                        });
                        MySingleton.getInstance().addToRequestQueue(stringRequest);
                    } else {
                        Toast.makeText(MainActivity.this, "Location Error", Toast.LENGTH_SHORT).show();
                    }
                    dialog.dismiss();
                }
            });
            dialog.show();
        }else{
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (receiver==null) {
            IntentFilter filter = new IntentFilter("Hello World");
            receiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    if (intent.getAction().contentEquals("Hello World")) {
                        lat = intent.getDoubleExtra("lat", 0.0);
                        lng = intent.getDoubleExtra("lng", 0.0);
                        world.lat = lat;
                        world.lng = lng;
                        if (needSomethingTweet || needSomethingWorld) {
                            needSomethingWorld = false;
                            needSomethingTweet = false;
                            getAllPosts(count);
                        }
                    }
                }
            };
            registerReceiver(receiver, filter);
        }
    }

    private void sendFCM(final String uid){
            StringRequest stringRequest = new StringRequest(Request.Method.POST,Constants.BASE_URL_DEFAULT + "GetFCMToken",
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {

                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(MainActivity.this, "Notifications not working!", Toast.LENGTH_SHORT).show();
                }
            }){
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("UserId", uid);
                    params.put("Token", sp.getString("token", ""));
                    return params;
                }
            };
            MySingleton.getInstance().addToRequestQueue(stringRequest);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (receiver!=null) {
            unregisterReceiver(receiver);
            receiver = null;
        }
    }
}


/*
public void getAllPostsAnimesh(){
        StringRequest myReq = new StringRequest(Request.Method.POST,
                Constants.BASE_URL_DBMS + "getpost",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            if (response != null ){
                                JSONObject jo = new JSONObject(response);
                                if (jo.has("data")) {
                                    JSONArray ja = jo.getJSONArray("data");
                                }
                            }
                        } catch (JSONException e) {

                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                }) {
            protected Map<String, String> getParams() throws com.android.volley.AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("token", sp.getString("token_animesh", null));
                return params;
            }
        };
        MySingleton.getInstance().addToRequestQueue(myReq);
    }
 */