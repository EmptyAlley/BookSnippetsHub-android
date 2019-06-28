package com.booksnippetshub.utils;

import android.app.Activity;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.booksnippetshub.AuthorizationHeaderInterceptor;
import com.booksnippetshub.CONFIG;
import com.booksnippetshub.FeedAdapter;
import com.booksnippetshub.FeedListRefresh;
import com.booksnippetshub.R;
import com.booksnippetshub.model.FeedModel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class GenerateFeedRecyclerView {

    public static RecyclerView generate(Activity activity, List<FeedModel> feedModels, boolean doNotRefresh, String url, FeedListRefresh feedListRefresh) {


        RecyclerView discoveryfeedlist = activity.findViewById(R.id.discoveryfeedlist);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(activity);
        discoveryfeedlist.setLayoutManager(linearLayoutManager);

        discoveryfeedlist.setLayoutManager(linearLayoutManager);
        discoveryfeedlist.addOnScrollListener(new RecyclerView.OnScrollListener() {
            boolean isfirstrun = false;


            FeedAdapter feedAdapter = null;
            LinearLayoutManager layoutManager = null;

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if (layoutManager == null) {
                    layoutManager = (LinearLayoutManager) discoveryfeedlist.getLayoutManager();
                }

                if (feedAdapter == null) {
                    feedAdapter = (FeedAdapter) discoveryfeedlist.getAdapter();
                }


                if (isfirstrun) {
                    if (layoutManager.findLastVisibleItemPosition() == discoveryfeedlist.getAdapter().getItemCount()) {
                        feedAdapter.setDonothavemore(true);
                    }
                    if (doNotRefresh) {
                        feedAdapter.setDonothavemore(true);
                    }
                }
                isfirstrun = false;

                int lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition();

                if ((lastVisibleItemPosition >= feedAdapter.getItemCount() - 1) && (feedAdapter.isDonothavemore() == false)) {

                    OkHttpClient okHttpClient = new OkHttpClient.Builder().addInterceptor(new AuthorizationHeaderInterceptor()).build();

                    JSONObject requstbody = new JSONObject();
                    Map<String, Object> requestjson = feedListRefresh.requestParam();
                    for (String s : requestjson.keySet()) {
                        requstbody.put(s, requestjson.get(s));
                    }


//                    requstbody.put("allrecommendfeedsid", feedAdapter.getAllrecommendfeedsid());

                    Request request = new Request.Builder().post(RequestBody.create(MediaType.parse("application/json"), requstbody.toJSONString())).url(CONFIG.baseUrl + url).build();
                    okHttpClient.newCall(request).enqueue(new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            String responestring = response.body().string();

                            feedListRefresh.onRespone(responestring);
                            JSONArray responsearray = JSONArray.parseArray(responestring);

                            if (responsearray.size() == 0) {
                                feedAdapter.setDonothavemore(true);
                                activity.runOnUiThread(() -> {
                                    feedAdapter.notifyDataSetChanged();
                                });

                            } else {
                                List<FeedModel> tempFeedModels = new ArrayList<>();

                                for (int i = 0; i < responsearray.size(); i++) {
                                    JSONObject feedjson = responsearray.getJSONObject(i);
                                    FeedModel feedModel = feedjson.toJavaObject(FeedModel.class);
                                    tempFeedModels.add(feedModel);
                                    feedAdapter.getAllrecommendfeedsid().add(feedModel.getId());
                                }

                                activity.runOnUiThread(() -> {
                                    feedAdapter.getFeedModels().addAll(tempFeedModels);
                                    feedAdapter.notifyDataSetChanged();

                                });
                            }
                        }
                    });
                }
            }
        });

        FeedAdapter feedAdapter = new FeedAdapter(feedModels);
        feedAdapter.setContext(activity);

        discoveryfeedlist.setAdapter(feedAdapter);
        return discoveryfeedlist;
    }

}
