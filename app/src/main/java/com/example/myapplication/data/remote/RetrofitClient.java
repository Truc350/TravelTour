package com.example.myapplication.data.remote;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * LỚP TRUNG GIAN MẠNG: Khởi tạo và quản lý Retrofit Client (Singleton).
 * Vai trò trong luồng: Cung cấp kết nối HTTP để gửi các yêu cầu API (như tạo booking tại api/bookings/)
 * từ client Android lên Django Backend Server.
 */
public class RetrofitClient {

    // URL cho API calls từ giả lập Android (Emulator)
    // 10.0.2.2 là địa chỉ đặc biệt được Android Emulator sử dụng để mapping tới 'localhost' (127.0.0.1) của máy tính host.
    public static final String BASE_URL = "http://10.0.2.2:8000/";

    // URL cho QR Code - dùng IP LAN thực của máy tính để điện thoại thật quét được
    // Thay 10.51.180.224 bằng IP Wi-Fi thực của máy tính nếu thay đổi mạng. Có thể cập nhật động.
    public static String QR_BASE_URL = "http://10.51.180.224:8000/";

    private static Retrofit retrofit;

    // BƯỚC 5.1a: Cung cấp instance Retrofit duy nhất (Singleton Pattern)
    public static Retrofit getClient() {

        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(
                            GsonConverterFactory.create()) // Tự động parse JSON qua Object nhờ thư viện GSON
                    .build();
        }

        return retrofit;
    }
}