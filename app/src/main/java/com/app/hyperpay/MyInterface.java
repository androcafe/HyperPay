package com.app.hyperpay;

import org.json.JSONObject;

import java.util.HashMap;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.HeaderMap;
import retrofit2.http.POST;

public interface MyInterface {
    public static String Base_url = "YOUR_BASE_URL";

    //GENERATE CHECKOUT ID
    @POST("checkout_id")
    Call<ResponseBody> generate_id(@HeaderMap HashMap<Object, Object> headers,@Body JSONObject jsonObject);

}
