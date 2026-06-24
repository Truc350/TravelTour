package com.example.myapplication.data.remote;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    // URL cho API calls từ giả lập Android (Emulator)
    // 10.0.2.2 là địa chỉ đặc biệt để giả lập kết nối vào máy tính host
    public static final String BASE_URL = "http://10.0.2.2:8000/";

    // URL cho QR Code - dùng IP LAN thực của máy tính để điện thoại thật quét được
    // Thay 10.51.180.224 bằng IP Wi-Fi thực của máy tính nếu thay đổi mạng. Có thể cập nhật động.
    public static String QR_BASE_URL = "http://10.51.180.224:8000/";

    private static Retrofit retrofit;

    public static Retrofit getClient() {

        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(
                            GsonConverterFactory.create())
                    .build();
        }

        return retrofit;
    }
}