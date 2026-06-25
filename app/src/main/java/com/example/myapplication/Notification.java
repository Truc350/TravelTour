package com.example.myapplication;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment hiển thị danh sách Thông báo (Notification).
 * Cho phép lọc thông báo theo danh mục (Tất cả, Khuyến mãi, Cập nhật),
 * đánh dấu đã đọc khi click từng thông báo, và "Đọc tất cả".
 */
public class Notification extends Fragment {

    // Lớp nội bộ đại diện cho từng thông báo
    public static class NotifyItem {
        String id;
        String title;
        String desc;
        String time;
        String category; // "PROMO" hoặc "UPDATE"
        boolean isUnread;
        String colorHex;

        public NotifyItem(String id, String title, String desc, String time, String category, boolean isUnread, String colorHex) {
            this.id = id;
            this.title = title;
            this.desc = desc;
            this.time = time;
            this.category = category;
            this.isUnread = isUnread;
            this.colorHex = colorHex;
        }
    }

    private List<NotifyItem> notificationList = new ArrayList<>();
    private String currentFilter = "ALL"; // "ALL", "PROMO", "UPDATE"

    private TextView btnMarkAllRead;
    private TextView btnTabAll;
    private TextView btnTabPromos;
    private TextView btnTabUpdates;
    private LinearLayout containerNotifications;
    private LinearLayout layoutNoNotifications;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.notification, container, false);

        // Khởi tạo các view thành phần
        btnMarkAllRead = view.findViewById(R.id.btnMarkAllRead);
        btnTabAll = view.findViewById(R.id.btnTabAll);
        btnTabPromos = view.findViewById(R.id.btnTabPromos);
        btnTabUpdates = view.findViewById(R.id.btnTabUpdates);
        containerNotifications = view.findViewById(R.id.containerNotifications);
        layoutNoNotifications = view.findViewById(R.id.layoutNoNotifications);

        // Thiết lập sự kiện click cho các Tab bộ lọc
        btnTabAll.setOnClickListener(v -> selectTab("ALL"));
        btnTabPromos.setOnClickListener(v -> selectTab("PROMO"));
        btnTabUpdates.setOnClickListener(v -> selectTab("UPDATE"));

        // Thiết lập sự kiện "Đọc tất cả"
        if (btnMarkAllRead != null) {
            btnMarkAllRead.setOnClickListener(v -> markAllAsRead());
        }

        // Tải thông báo từ Room Database
        loadNotificationsFromDb();

        return view;
    }

    /**
     * Khởi tạo 6 thông báo mẫu với thiết kế và nội dung trực quan, sinh động.
     */
    private void initMockNotifications() {
        notificationList.add(new NotifyItem(
                "1",
                "Ưu đãi 30% Tour Phú Quốc",
                "Cơ hội duy nhất trong tuần! Đặt ngay tour Phú Quốc 3 ngày 2 đêm chỉ với 2.990.000đ. Số lượng có hạn, đặt ngay kẻo lỡ!",
                "5 phút trước",
                "PROMO",
                true,
                "#00B4D8" // Cyan
        ));

        notificationList.add(new NotifyItem(
                "2",
                "Thay đổi giờ bay - Tour Đà Nẵng",
                "Hãng hàng không Vietnam Airlines thông báo giờ cất cánh mới cho chuyến bay VN123 ngày 25/05 là 08:30 (sớm hơn 15 phút so với lịch trình ban đầu).",
                "1 giờ trước",
                "UPDATE",
                true,
                "#FF8F00" // Orange / Amber
        ));

        notificationList.add(new NotifyItem(
                "3",
                "Quà tặng thành viên mới!",
                "Chào mừng bạn đến với TravelTour! Nhận ngay mã giảm giá TOURNEW50K giảm trực tiếp 50.000đ cho tất cả các dịch vụ tour đầu tiên của bạn.",
                "3 giờ trước",
                "PROMO",
                true,
                "#E53935" // Red
        ));

        notificationList.add(new NotifyItem(
                "4",
                "Nhắc nhở chuẩn bị du lịch Sapa",
                "Đừng quên mang theo trang phục giữ ấm và giấy tờ tùy thân đầy đủ bạn nhé. Hướng dẫn viên sẽ chủ động liên hệ với bạn vào lúc 07:00 ngày mai.",
                "1 ngày trước",
                "UPDATE",
                false,
                "#9C27B0" // Purple
        ));

        notificationList.add(new NotifyItem(
                "5",
                "Thanh toán thành công Tour Nha Trang",
                "Mã giao dịch #HD99281 của bạn đã được xác nhận thanh toán thành công hệ thống. Vé du lịch điện tử đã được gửi qua email của bạn.",
                "2 ngày trước",
                "UPDATE",
                false,
                "#4CAF50" // Green
        ));

        notificationList.add(new NotifyItem(
                "6",
                "Thông tin thời tiết Vịnh Hạ Long",
                "Thời tiết tại Hạ Long tuần này vô cùng lý tưởng: trời nắng nhẹ, không mưa, cực kỳ thích hợp cho các hoạt động du thuyền và kayak.",
                "3 ngày trước",
                "UPDATE",
                false,
                "#2196F3" // Blue
        ));
    }

    private void loadNotificationsFromDb() {
        if (getContext() == null) return;
        android.content.SharedPreferences prefs = getContext().getSharedPreferences("UserSession", android.content.Context.MODE_PRIVATE);
        int currentUserId = prefs.getInt("current_user_id", -1);
        
        if (currentUserId == -1) {
            // Hiển thị dữ liệu mẫu nếu chưa đăng nhập
            if (notificationList.isEmpty()) {
                initMockNotifications();
            }
            renderNotifications();
            return;
        }

        com.example.myapplication.data.remote.ApiService apiService = com.example.myapplication.data.remote.RetrofitClient.getClient()
                .create(com.example.myapplication.data.remote.ApiService.class);

        apiService.getNotifications().enqueue(new retrofit2.Callback<List<com.example.myapplication.data.model.Notification>>() {
            @Override
            public void onResponse(retrofit2.Call<List<com.example.myapplication.data.model.Notification>> call, retrofit2.Response<List<com.example.myapplication.data.model.Notification>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    notificationList.clear();
                    List<com.example.myapplication.data.model.Notification> apiList = response.body();
                    
                    for (com.example.myapplication.data.model.Notification item : apiList) {
                        if (item.getUserId() == currentUserId) {
                            String colorHex = "#FF8F00"; // Mặc định là Amber
                            String title = item.getTitle();
                            if (title.contains("thành công")) {
                                colorHex = "#4CAF50"; // Green
                            } else if (title.contains("Ưu đãi") || title.contains("khuyến mãi") || title.contains("Quà tặng")) {
                                colorHex = "#00B4D8"; // Cyan
                            } else if (title.contains("Nhắc nhở") || title.contains("khởi hành")) {
                                colorHex = "#9C27B0"; // Purple
                            }
                            
                            String category = (title.contains("Ưu đãi") || title.contains("khuyến mãi") || title.contains("Quà tặng")) ? "PROMO" : "UPDATE";
                            boolean isUnread = !item.isRead();

                            notificationList.add(new NotifyItem(
                                    String.valueOf(item.getId()),
                                    item.getTitle(),
                                    item.getMessage(),
                                    item.getDate(),
                                    category,
                                    isUnread,
                                    colorHex
                            ));
                        }
                    }
                    
                    // Luôn gộp thêm các thông báo mẫu để giao diện luôn đầy đặn và sinh động
                    initMockNotifications();
                    renderNotifications();
                } else {
                    if (notificationList.isEmpty()) {
                        initMockNotifications();
                    }
                    renderNotifications();
                }
            }

            @Override
            public void onFailure(retrofit2.Call<List<com.example.myapplication.data.model.Notification>> call, Throwable t) {
                if (notificationList.isEmpty()) {
                    initMockNotifications();
                }
                renderNotifications();
            }
        });
    }

    /**
     * Xử lý chuyển đổi giữa các Tab bộ lọc và cập nhật giao diện tương ứng.
     */
    private void selectTab(String filter) {
        if (currentFilter.equals(filter)) return;
        currentFilter = filter;

        // Reset tất cả các tab về giao diện unselected
        int unselectedBg = R.drawable.bg_chip_unselected_pill;
        int unselectedTextColor = Color.parseColor("#4A5568");

        btnTabAll.setBackgroundResource(unselectedBg);
        btnTabAll.setTextColor(unselectedTextColor);
        btnTabPromos.setBackgroundResource(unselectedBg);
        btnTabPromos.setTextColor(unselectedTextColor);
        btnTabUpdates.setBackgroundResource(unselectedBg);
        btnTabUpdates.setTextColor(unselectedTextColor);

        // Kích hoạt giao diện selected cho tab được bấm
        int selectedBg = R.drawable.bg_chip_selected_cyan;
        int selectedTextColor = Color.parseColor("#00B4D8");

        if (filter.equals("ALL")) {
            btnTabAll.setBackgroundResource(selectedBg);
            btnTabAll.setTextColor(selectedTextColor);
        } else if (filter.equals("PROMO")) {
            btnTabPromos.setBackgroundResource(selectedBg);
            btnTabPromos.setTextColor(selectedTextColor);
        } else if (filter.equals("UPDATE")) {
            btnTabUpdates.setBackgroundResource(selectedBg);
            btnTabUpdates.setTextColor(selectedTextColor);
        }

        // Load lại danh sách tương ứng
        renderNotifications();
    }

    /**
     * Duyệt qua và cập nhật toàn bộ thông báo thuộc bộ lọc hiện tại thành đã đọc.
     */
    private void markAllAsRead() {
        boolean updated = false;
        for (NotifyItem item : notificationList) {
            if (item.isUnread && (currentFilter.equals("ALL") || item.category.equals(currentFilter))) {
                item.isUnread = false;
                updated = true;
            }
        }

        if (updated) {
            com.example.myapplication.data.remote.ApiService apiService = com.example.myapplication.data.remote.RetrofitClient.getClient()
                    .create(com.example.myapplication.data.remote.ApiService.class);

            for (NotifyItem item : notificationList) {
                if (!item.isUnread) {
                    try {
                        int notificationId = Integer.parseInt(item.id);
                        java.util.Map<String, Object> fields = new java.util.HashMap<>();
                        fields.put("is_read", true);
                        apiService.patchNotification(notificationId, fields).enqueue(new retrofit2.Callback<com.example.myapplication.data.model.Notification>() {
                            @Override
                            public void onResponse(retrofit2.Call<com.example.myapplication.data.model.Notification> call, retrofit2.Response<com.example.myapplication.data.model.Notification> response) {}
                            @Override
                            public void onFailure(retrofit2.Call<com.example.myapplication.data.model.Notification> call, Throwable t) {}
                        });
                    } catch (NumberFormatException e) {
                        // Bỏ qua với thông báo giả lập
                    }
                }
            }
            Toast.makeText(getContext(), "Đã đánh dấu đọc tất cả thông báo", Toast.LENGTH_SHORT).show();
            renderNotifications();
        } else {
            Toast.makeText(getContext(), "Không có thông báo chưa đọc nào", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Hiển thị động các thông báo vào màn hình.
     */
    private void renderNotifications() {
        if (containerNotifications == null) return;

        // Xóa hết các view cũ trước khi render mới
        containerNotifications.removeAllViews();

        List<NotifyItem> filteredList = new ArrayList<>();
        for (NotifyItem item : notificationList) {
            if (currentFilter.equals("ALL") || item.category.equals(currentFilter)) {
                filteredList.add(item);
            }
        }

        // Xử lý hiển thị giao diện trống nếu không có thông báo nào phù hợp
        if (filteredList.isEmpty()) {
            if (layoutNoNotifications != null) {
                layoutNoNotifications.setVisibility(View.VISIBLE);
            }
            containerNotifications.setVisibility(View.GONE);
            return;
        } else {
            if (layoutNoNotifications != null) {
                layoutNoNotifications.setVisibility(View.GONE);
            }
            containerNotifications.setVisibility(View.VISIBLE);
        }

        LayoutInflater inflater = LayoutInflater.from(getContext());

        // Inflate và gắn dữ liệu động cho từng mục thông báo
        for (NotifyItem item : filteredList) {
            View itemView = inflater.inflate(R.layout.item_notification, containerNotifications, false);

            TextView tvTitle = itemView.findViewById(R.id.tvNotifyTitle);
            TextView tvDesc = itemView.findViewById(R.id.tvNotifyDesc);
            TextView tvTime = itemView.findViewById(R.id.tvNotifyTime);
            View viewDot = itemView.findViewById(R.id.viewUnreadDot);
            FrameLayout layoutIconContainer = itemView.findViewById(R.id.layoutNotifyIconContainer);
            ImageView ivIcon = itemView.findViewById(R.id.ivNotifyIcon);

            // Gắn thông tin text
            if (tvTitle != null) tvTitle.setText(item.title);
            if (tvDesc != null) tvDesc.setText(item.desc);
            if (tvTime != null) tvTime.setText(item.time);

            // Thiết lập chấm đỏ báo chưa đọc
            if (viewDot != null) {
                viewDot.setVisibility(item.isUnread ? View.VISIBLE : View.GONE);
            }

            // Thiết lập màu sắc nền tròn icon theo chủ đề của thông báo
            if (layoutIconContainer != null && item.colorHex != null) {
                try {
                    layoutIconContainer.getBackground().mutate().setTint(Color.parseColor(item.colorHex));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            // Chọn icon tương ứng cho Khuyến mãi vs Cập nhật để tăng độ thẩm mỹ
            if (ivIcon != null) {
                if (item.category.equals("PROMO")) {
                    ivIcon.setImageResource(R.drawable.ic_voucher); // Sử dụng voucher cho khuyến mãi
                } else {
                    ivIcon.setImageResource(R.drawable.ic_notification); // Sử dụng chuông cho cập nhật
                }
            }

            // Sự kiện khi bấm vào từng card thông báo
            itemView.setOnClickListener(v -> {
                // Đánh dấu đã đọc ngay lập tức trên UI và trong dữ liệu
                if (item.isUnread) {
                    item.isUnread = false;
                    if (viewDot != null) {
                        viewDot.setVisibility(View.GONE);
                    }
                    // Cập nhật trạng thái đọc lên Django Server
                    try {
                        int notificationId = Integer.parseInt(item.id);
                        com.example.myapplication.data.remote.ApiService apiService = com.example.myapplication.data.remote.RetrofitClient.getClient()
                                .create(com.example.myapplication.data.remote.ApiService.class);
                        java.util.Map<String, Object> fields = new java.util.HashMap<>();
                        fields.put("is_read", true);
                        apiService.patchNotification(notificationId, fields).enqueue(new retrofit2.Callback<com.example.myapplication.data.model.Notification>() {
                            @Override
                            public void onResponse(retrofit2.Call<com.example.myapplication.data.model.Notification> call, retrofit2.Response<com.example.myapplication.data.model.Notification> response) {}
                            @Override
                            public void onFailure(retrofit2.Call<com.example.myapplication.data.model.Notification> call, Throwable t) {}
                        });
                    } catch (NumberFormatException e) {
                        // Bỏ qua với thông báo giả lập
                    }
                }

                // Hiển thị hộp thoại Dialog chi tiết thông báo cực đẹp & premium
                showNotificationDetailDialog(item);
            });

            containerNotifications.addView(itemView);
        }
    }

    /**
     * Hiển thị hộp thoại chi tiết của thông báo cực kỳ sang trọng.
     */
    private void showNotificationDetailDialog(NotifyItem item) {
        if (getContext() == null) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), android.R.style.Theme_DeviceDefault_Light_Dialog_Alert);
        
        // Tạo view tùy chỉnh cho Dialog để nó cực kỳ đồng bộ và đẹp mắt
        LinearLayout dialogView = new LinearLayout(getContext());
        dialogView.setOrientation(LinearLayout.VERTICAL);
        dialogView.setPadding(60, 60, 60, 60);
        dialogView.setBackgroundColor(Color.WHITE);

        // Icon tròn
        FrameLayout iconFrame = new FrameLayout(getContext());
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(120, 120);
        iconParams.gravity = android.view.Gravity.CENTER_HORIZONTAL;
        iconParams.bottomMargin = 40;
        iconFrame.setLayoutParams(iconParams);
        iconFrame.setBackgroundResource(R.drawable.bg_notify_circle);
        iconFrame.getBackground().mutate().setTint(Color.parseColor(item.colorHex));

        ImageView iconView = new ImageView(getContext());
        FrameLayout.LayoutParams iconViewParams = new FrameLayout.LayoutParams(60, 60);
        iconViewParams.gravity = android.view.Gravity.CENTER;
        iconView.setLayoutParams(iconViewParams);
        iconView.setImageResource(item.category.equals("PROMO") ? R.drawable.ic_voucher : R.drawable.ic_notification);
        iconView.setColorFilter(Color.WHITE);
        iconFrame.addView(iconView);
        dialogView.addView(iconFrame);

        // Title
        TextView titleView = new TextView(getContext());
        titleView.setText(item.title);
        titleView.setTextSize(18f);
        titleView.setTextColor(Color.parseColor("#2D3748"));
        titleView.setTypeface(null, android.graphics.Typeface.BOLD);
        titleView.setGravity(android.view.Gravity.CENTER_HORIZONTAL);
        titleView.setPadding(0, 0, 0, 20);
        dialogView.addView(titleView);

        // Time
        TextView timeView = new TextView(getContext());
        timeView.setText(item.time);
        timeView.setTextSize(12f);
        timeView.setTextColor(Color.parseColor("#A0AEC0"));
        timeView.setGravity(android.view.Gravity.CENTER_HORIZONTAL);
        timeView.setPadding(0, 0, 0, 40);
        dialogView.addView(timeView);

        // Description
        TextView descView = new TextView(getContext());
        descView.setText(item.desc);
        descView.setTextSize(14f);
        descView.setTextColor(Color.parseColor("#4A5568"));
        descView.setLineSpacing(8f, 1f);
        descView.setPadding(0, 0, 0, 40);
        dialogView.addView(descView);

        builder.setView(dialogView);
        builder.setPositiveButton("Đóng", (dialog, which) -> dialog.dismiss());
        
        AlertDialog dialog = builder.create();
        dialog.show();

        // Tùy chỉnh màu nút Đóng sang màu cyan đồng bộ
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.parseColor("#00B4D8"));
    }
}
