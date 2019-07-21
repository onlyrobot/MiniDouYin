package com.pengyao.minidouyin.bean;


import com.google.gson.annotations.SerializedName;

public class PostVideoResponse {

    @SerializedName("success") private boolean success;
    @SerializedName("item") private Feed item;
    public boolean isSuccess(){
        return success;
    }
    public Feed getItem(){
        return item;
    }
}
