package com.example.myapplication;

import android.graphics.Bitmap;
import android.graphics.Color;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;

/**
 * Lớp tiện ích giúp sinh mã QR Code động dưới dạng Bitmap từ chuỗi dữ liệu.
 */
public class QrCodeGenerator {

    /**
     * Sinh ảnh Bitmap QR Code từ chuỗi ký tự đầu vào.
     *
     * @param data   Nội dung cần mã hóa vào mã QR
     * @param width  Chiều rộng ảnh (pixel)
     * @param height Chiều cao ảnh (pixel)
     * @return Bitmap chứa mã QR hoặc null nếu xảy ra lỗi
     */
    public static Bitmap generateQrCode(String data, int width, int height) {
        try {
            MultiFormatWriter writer = new MultiFormatWriter();
            BitMatrix bitMatrix = writer.encode(data, BarcodeFormat.QR_CODE, width, height);
            
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    bitmap.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }
            return bitmap;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
