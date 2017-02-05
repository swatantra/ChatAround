package com.fame.plumbum.chataround.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.fame.plumbum.chataround.R;
import com.fame.plumbum.chataround.utils.Constants;
import com.fame.plumbum.chataround.utils.MySingleton;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by pankaj on 19/8/16.
 */
public class GetProfileDetails extends AppCompatActivity {
    static String email_id;
    static String password;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_edit);
        final EditText name_edit = (EditText) findViewById(R.id.name);
        final EditText phone_edit = (EditText) findViewById(R.id.phone);
        final EditText id_edit = (EditText) findViewById(R.id.college_id);
        final EditText dob_edit = (EditText) findViewById(R.id.dob);
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(GetProfileDetails.this);
        email_id = sp.getString("email", "");
        password = sp.getString("password", "password");
        Button update = (Button) findViewById(R.id.update);
        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (phone_edit.getText().toString().length() > 9 && name_edit.getText().toString().length() != 0) {
                    String name = name_edit.getText().toString();
                    name = convertToUpperCase(name);
                    String phone = phone_edit.getText().toString();
                    sendDataToAnimesh(name, phone, id_edit.getText().toString(), dob_edit.getText().toString());
                } else
                    Toast.makeText(GetProfileDetails.this, "Invalid Entries!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendDataToAnimesh(final String name, final String phone, final String s, final String s1) {
        StringRequest myReq = new StringRequest(Request.Method.POST,
                Constants.BASE_URL_DBMS + "register",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.e("Response From animesh", response);
                        try {
                            JSONObject jo = new JSONObject(response);
                            if (jo.has("token")) {
                                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(GetProfileDetails.this);
                                SharedPreferences.Editor editor = sp.edit();
                                editor.putString("token_animesh", jo.getString("token"));
                                editor.apply();
                                sendData(name, phone);
                            }else{
                                Toast.makeText(GetProfileDetails.this, "User alreadt exists", Toast.LENGTH_SHORT).show();
                            }
                        }catch(JSONException e){
                                e.printStackTrace();
                            }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(GetProfileDetails.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                    }
                }) {
            protected Map<String, String> getParams() throws com.android.volley.AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                // SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(GetProfileDetails.this);
                Log.e("Data", name + " " + email_id + " " + password + " " + phone + " " + s + " " + s1 + " " );
                params.put("name", name);
                params.put("email", email_id);
                params.put("password", password);
                params.put("phone", phone);
                params.put("college_id", s);
                params.put("dob", s1);
                return params;
            }
        };
        MySingleton.getInstance().addToRequestQueue(myReq);
    }

    private static void animeshDetails(String email, String password){
        email_id = email;
        GetProfileDetails.password = password;
    }

    private String convertToUpperCase(String name) {
        if (name!=null && name.length()>0) {
            name = name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase();
            for (int i = 0; ; ) {
                i = name.indexOf(" ", i + 1);
                if (i < 0)
                    break;
                else {
                    if (i < name.length()-2)
                        name = name.substring(0, i + 1) + name.substring(i + 1, i + 2).toUpperCase() + name.substring(i + 2);
                    else if (i == name.length()-2) {
                        name = name.substring(0, i + 1) + name.substring(i + 1, i + 2).toUpperCase();
                        break;
                    }
                }
            }
        }
        return name;
    }

    private void sendData(final String name, final String phone) {
        StringRequest myReq = new StringRequest(Request.Method.POST,
                Constants.BASE_URL_DEFAULT + "AddProfile",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jO = new JSONObject(response);
                            if (jO.getString("Status").contentEquals("200")){
                                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(GetProfileDetails.this);
                                SharedPreferences.Editor editor = sp.edit();
                                editor.putString("edited", "1");
                                editor.putString("user_name", name.replace("%20", " "));
                                editor.putString("user_phone", phone);
                                editor.apply();
                                Toast.makeText(GetProfileDetails.this, "Data sent!", Toast.LENGTH_SHORT).show();

                                Intent intent = new Intent(GetProfileDetails.this, OTP.class);
                                startActivity(intent);
                                finish();
                            }else if(jO.getString("Status").contentEquals("400")){
                                Toast.makeText(GetProfileDetails.this, "Couldnt update entries!", Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException ignored) {
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(GetProfileDetails.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                    }
                }) {
            protected Map<String, String> getParams() throws com.android.volley.AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(GetProfileDetails.this);
                params.put("UserId", sp.getString("uid", ""));
                params.put("Mobile", phone);
                params.put("Name", name);
                params.put("IsEditing", "0");
                return params;
            }
        };
        MySingleton.getInstance().addToRequestQueue(myReq);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}