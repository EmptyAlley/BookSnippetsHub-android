package com.booksnippetshub.fragment;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.booksnippetshub.AuthorizationHeaderInterceptor;
import com.booksnippetshub.CONFIG;
import com.booksnippetshub.MenuItemContainer;
import com.booksnippetshub.R;
import com.facebook.drawee.view.SimpleDraweeView;


import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class MeFragment extends Fragment {

    OkHttpClient okHttpClient;

    private LinearLayout menu_item_container;

    private TextView nickNameTextView;
    private AppCompatActivity activity;
    private SimpleDraweeView avatarDraweeView;


    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";


    @Override
    public void onPause() {
        super.onPause();
        Log.d("lifecycle", "onPause");
    }

    @Override
    public void onResume() {
        super.onResume();

        Log.d("lifecycle", "onResume");
    }


    public MeFragment() {
        this.setArguments(new Bundle());

    }


    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        Log.d("onHiddenChanged", String.valueOf(hidden));

    }

    public static MeFragment newInstance(String param1, String param2) {
        MeFragment fragment = new MeFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("lifecycle", "onCreate");
        if (getArguments() != null) {

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d("lifecycle", "onCreateView");
        return inflater.inflate(R.layout.fragment_me, container, false);
    }


    public void onButtonPressed(Uri uri) {

    }

    @Override
    public void onAttach(Context context) {
        Log.d("lifecycle", "onAttach ");
        super.onAttach(context);
        this.activity = (AppCompatActivity) getActivity();
    }

    @Override
    public void onDestroyView() {
        Log.d("lifecycle", "onDestroyView");
        super.onDestroyView();
    }

    @Override
    public void onDetach() {
        Log.d("lifecycle", "onDetach ");
        super.onDetach();

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        Log.d("lifecycle", "onActivityCreated");
        super.onActivityCreated(savedInstanceState);
        menu_item_container = activity.findViewById(R.id.menu_item_container);

        //添加菜单项
        MenuItemContainer a = new MenuItemContainer(getActivity());
        a.setDetails(R.drawable.in, "设置");
        menu_item_container.addView(a);

        setUserInfo();

    }

    private void setUserInfo() {
        avatarDraweeView = activity.findViewById(R.id.avatarDraweeView);
        nickNameTextView = activity.findViewById(R.id.nickName);

        okHttpClient = new OkHttpClient.Builder().addInterceptor(new AuthorizationHeaderInterceptor()).build();

        Request request = new Request.Builder().url(CONFIG.baseUrl + "/getuserinfo").build();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                JSONObject responejson = JSONObject.parseObject(response.body().string());
                String nickname = responejson.getString("nickname");
                String avatarUrl = responejson.getString("avatarUrl");

                if (avatarUrl.startsWith("/")) {
                    avatarUrl = CONFIG.baseUrl + avatarUrl;
                }
                Uri avataruri = Uri.parse(avatarUrl);
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        avatarDraweeView.setImageURI(avataruri);
                        nickNameTextView.setText(nickname);
                    }
                });
            }
        });
    }
}