package com.pengyao.minidouyin.bean;


import com.google.gson.annotations.SerializedName;

import java.util.List;

public class FeedResponse {

    @SerializedName("feeds") private List<Feed> feeds;
    @SerializedName("success") private boolean success;

    public List<Feed> getFeeds(){
        return feeds;
    }
    public boolean isSuccess(){
        return success;
    }
}
