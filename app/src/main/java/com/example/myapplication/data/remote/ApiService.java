package com.example.myapplication.data.remote;

import com.example.myapplication.data.model.Tour;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

public interface ApiService {

    @GET("api/tours/")
    Call<List<Tour>> getTours();
}