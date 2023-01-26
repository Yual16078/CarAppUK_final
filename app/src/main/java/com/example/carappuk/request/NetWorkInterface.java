package com.example.carappuk.request;


import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface NetWorkInterface {
    @GET("now?key=27608868d0f54a4bbcebe0b00982c7f2&lang=en")
    Call<WeatherReturns> getCall(@Query("location") String location);
}
