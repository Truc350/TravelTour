package com.example.myapplication;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.data.model.Tour;
import com.example.myapplication.data.remote.ApiService;
import com.example.myapplication.data.remote.RetrofitClient;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * ChatbotActivity – Chatbot chăm sóc khách hàng ChillTour.
 * Cung cấp thông tin về các tour du lịch. Dữ liệu lấy từ API server thực.
 */
public class ChatbotActivity extends AppCompatActivity {

    // ─── Constants ───────────────────────────────────────────────────────────
    private static final int TYPE_BOT  = 0;
    private static final int TYPE_USER = 1;

    // ─── Views ────────────────────────────────────────────────────────────────
    private RecyclerView     recyclerChat;
    private EditText         etMessage;
    private ImageView        btnSend;
    private LinearLayout     layoutQuickReplies;

    // ─── Data ─────────────────────────────────────────────────────────────────
    private final List<ChatMessage> messages = new ArrayList<>();
    private ChatAdapter             adapter;
    private final Handler           handler  = new Handler(Looper.getMainLooper());

    /** Danh sách tour đã tải từ server */
    private List<Tour> tourList = new ArrayList<>();
    private boolean    toursLoaded = false;

    // ─── Quick reply topics ───────────────────────────────────────────────────
    private final String[] quickReplies = {
            "Tour miền Bắc", "Tour miền Trung", "Tour miền Nam",
            "Tour nước ngoài", "Tour rẻ nhất", "Tour đánh giá cao"
    };

    // ─── Lifecycle ────────────────────────────────────────────────────────────
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatbot);

        bindViews();
        setupRecycler();
        setupListeners();
        loadTours();   // Tải dữ liệu tour từ API

        // Tin nhắn chào mừng
        postBotMessage(
                "Xin chào! 👋 Tôi là ChillBot – trợ lý tư vấn tour của ChillTour.\n\n"
                + "Bạn có thể hỏi tôi về:\n"
                + "• Thông tin, mô tả tour\n"
                + "• Giá tour\n"
                + "• Đánh giá & điểm số tour\n"
                + "• Tour theo vùng miền\n\n"
                + "Hỏi tôi bất cứ điều gì về tour bạn quan tâm! 😊", 500);
    }

    // ─── Init ─────────────────────────────────────────────────────────────────
    private void bindViews() {
        recyclerChat       = findViewById(R.id.recyclerChat);
        etMessage          = findViewById(R.id.etMessage);
        btnSend            = findViewById(R.id.btnSend);
        layoutQuickReplies = findViewById(R.id.layoutQuickReplies);

        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> onBackPressed());
    }

    private void setupRecycler() {
        adapter = new ChatAdapter(messages);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setStackFromEnd(true);
        recyclerChat.setLayoutManager(llm);
        recyclerChat.setAdapter(adapter);
    }

    private void setupListeners() {
        btnSend.setOnClickListener(v -> trySend());
        etMessage.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                trySend();
                return true;
            }
            return false;
        });
    }

    private void trySend() {
        String text = etMessage.getText().toString().trim();
        if (!text.isEmpty()) {
            sendMessage(text);
            etMessage.setText("");
        }
    }

    // ─── API ──────────────────────────────────────────────────────────────────
    private void loadTours() {
        ApiService api = RetrofitClient.getClient().create(ApiService.class);
        api.getTours().enqueue(new Callback<List<Tour>>() {
            @Override
            public void onResponse(@NonNull Call<List<Tour>> call,
                                   @NonNull Response<List<Tour>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    tourList = response.body();
                    toursLoaded = true;
                    // Hiển thị quick replies sau khi tải xong
                    handler.post(() -> setupQuickReplies());
                }
            }
            @Override
            public void onFailure(@NonNull Call<List<Tour>> call, @NonNull Throwable t) {
                toursLoaded = true; // vẫn cho phép chat dù không có dữ liệu
                handler.post(() -> setupQuickReplies());
            }
        });
    }

    private void setupQuickReplies() {
        layoutQuickReplies.removeAllViews();
        for (String label : quickReplies) {
            layoutQuickReplies.addView(makeChipTextView(label));
        }
    }

    private TextView makeChipTextView(String label) {
        TextView tv = new TextView(this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMarginEnd(dp(8));
        tv.setLayoutParams(lp);
        tv.setText(label);
        tv.setTextColor(0xFF185FA5);
        tv.setTextSize(13f);
        tv.setPadding(dp(14), dp(8), dp(14), dp(8));
        tv.setBackground(getDrawable(R.drawable.bg_chip_unselected));
        tv.setClickable(true);
        tv.setFocusable(true);
        tv.setOnClickListener(v -> sendMessage(label));
        return tv;
    }

    private int dp(int v) {
        return (int) (v * getResources().getDisplayMetrics().density);
    }

    // ─── Chat logic ───────────────────────────────────────────────────────────
    // Session ID duy nhất để ghi nhớ lịch sử hội thoại trong phiên
    private String chatSessionId = null;
    private static final int TYPE_BOT  = 0;
    private static final int TYPE_USER = 1;
    private static final int TYPE_TOUR = 2; // Loại tin nhắn chứa thẻ Tour Card trực quan

    private void sendMessage(String text) {
        addMessage(new ChatMessage(text, TYPE_USER, now()));
        
        // Gọi API Chatbot lên Django Server
        ApiService api = RetrofitClient.getClient().create(ApiService.class);
        android.content.SharedPreferences prefs = getSharedPreferences("UserSession", MODE_PRIVATE);
        int currentUserId = prefs.getInt("current_user_id", -1);
        Integer userIdParam = (currentUserId == -1) ? null : currentUserId;

        com.example.myapplication.data.model.ChatbotRequest request = 
                new com.example.myapplication.data.model.ChatbotRequest(text, chatSessionId, userIdParam);

        api.chatWithBot(request).enqueue(new Callback<com.example.myapplication.data.model.ChatbotResponse>() {
            @Override
            public void onResponse(@NonNull Call<com.example.myapplication.data.model.ChatbotResponse> call,
                                   @NonNull Response<com.example.myapplication.data.model.ChatbotResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    com.example.myapplication.data.model.ChatbotResponse body = response.body();
                    chatSessionId = body.getSessionId(); // Lưu session_id để duy trì ngữ cảnh các tin nhắn sau

                    // Thêm câu trả lời văn bản từ chatbot
                    addMessage(new ChatMessage(body.getResponse(), TYPE_BOT, now()));

                    // Nếu có tour được đề xuất phù hợp, hiển thị dưới dạng thẻ card
                    if (body.getTours() != null && !body.getTours().isEmpty()) {
                        for (Tour tour : body.getTours()) {
                            addMessage(new ChatMessage(tour, TYPE_TOUR, now()));
                        }
                    }
                } else {
                    addMessage(new ChatMessage("Xin lỗi, tôi gặp sự cố khi xử lý câu hỏi này. Bạn vui lòng thử lại nhé!", TYPE_BOT, now()));
                }
            }

            @Override
            public void onFailure(@NonNull Call<com.example.myapplication.data.model.ChatbotResponse> call, @NonNull Throwable t) {
                addMessage(new ChatMessage("Không thể kết nối đến trợ lý ảo. Vui lòng kiểm tra lại kết nối mạng.", TYPE_BOT, now()));
            }
        });
    }

    private void addMessage(ChatMessage msg) {
        messages.add(msg);
        adapter.notifyItemInserted(messages.size() - 1);
        recyclerChat.scrollToPosition(messages.size() - 1);
    }

    private void postBotMessage(String text, long delayMs) {
        handler.postDelayed(() -> addMessage(new ChatMessage(text, TYPE_BOT, now())), delayMs);
    }

    private String now() {
        return new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
    }

    // ─── Data model ───────────────────────────────────────────────────────────
    static class ChatMessage {
        final String content;
        final int    type;
        final String time;
        final Tour   tour; // Chỉ sử dụng khi type == TYPE_TOUR

        ChatMessage(String content, int type, String time) {
            this.content = content;
            this.type    = type;
            this.time    = time;
            this.tour    = null;
        }

        ChatMessage(Tour tour, int type, String time) {
            this.content = null;
            this.type    = type;
            this.time    = time;
            this.tour    = tour;
        }
    }

    // ─── RecyclerView Adapter ─────────────────────────────────────────────────
    static class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private final List<ChatMessage> data;

        ChatAdapter(List<ChatMessage> data) { this.data = data; }

        @Override public int getItemViewType(int pos) { return data.get(pos).type; }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inf = LayoutInflater.from(parent.getContext());
            if (viewType == TYPE_BOT) {
                return new BotVH(inf.inflate(R.layout.item_chat_bot, parent, false));
            } else if (viewType == TYPE_USER) {
                return new UserVH(inf.inflate(R.layout.item_chat_user, parent, false));
            } else {
                // Sử dụng lại card layout của tour để hiển thị trong chat list
                return new TourVH(inf.inflate(R.layout.item_tour_card, parent, false));
            }
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int pos) {
            ChatMessage msg = data.get(pos);
            if (holder instanceof BotVH)  ((BotVH)  holder).bind(msg);
            else if (holder instanceof UserVH) ((UserVH) holder).bind(msg);
            else if (holder instanceof TourVH) ((TourVH) holder).bind(msg);
        }

        @Override public int getItemCount() { return data.size(); }

        static class BotVH extends RecyclerView.ViewHolder {
            final TextView tvMsg, tvTime;
            BotVH(View v) { super(v); tvMsg = v.findViewById(R.id.tvBotMessage); tvTime = v.findViewById(R.id.tvBotTime); }
            void bind(ChatMessage m) { tvMsg.setText(m.content); tvTime.setText(m.time); }
        }

        static class UserVH extends RecyclerView.ViewHolder {
            final TextView tvMsg, tvTime;
            UserVH(View v) { super(v); tvMsg = v.findViewById(R.id.tvUserMessage); tvTime = v.findViewById(R.id.tvUserTime); }
            void bind(ChatMessage m) { tvMsg.setText(m.content); tvTime.setText(m.time); }
        }

        static class TourVH extends RecyclerView.ViewHolder {
            TextView tvTourTitle, tvOldPrice, tvNewPrice, tvRibbonBadge, btnViewTour;
            android.widget.ImageView ivTourImage;

            TourVH(View itemView) {
                super(itemView);
                tvTourTitle    = itemView.findViewById(R.id.tvTourTitle);
                tvOldPrice     = itemView.findViewById(R.id.tvOldPrice);
                tvNewPrice     = itemView.findViewById(R.id.tvNewPrice);
                tvRibbonBadge  = itemView.findViewById(R.id.tvRibbonBadge);
                btnViewTour    = itemView.findViewById(R.id.btnViewTour);
                ivTourImage    = itemView.findViewById(R.id.ivTourImage);
            }

            void bind(ChatMessage m) {
                final Tour tour = m.tour;
                if (tour == null) return;

                tvTourTitle.setText(tour.getTitle());

                // Định dạng tiền tệ VNĐ
                java.text.NumberFormat formatter = java.text.NumberFormat.getNumberInstance(new java.util.Locale("vi", "VN"));

                // Giá gốc gạch ngang
                tvOldPrice.setText(formatter.format(tour.getOriginalPrice()));
                tvOldPrice.setPaintFlags(
                        tvOldPrice.getPaintFlags() | android.graphics.Paint.STRIKE_THRU_TEXT_FLAG
                );

                // Giá khuyến mãi
                tvNewPrice.setText(formatter.format(tour.getDiscountPrice()));

                // Badge nhà cung cấp
                tvRibbonBadge.setText(tour.getProvider());

                // Nhấp vào Xem tour để mở chi tiết
                android.view.View.OnClickListener listener = v -> {
                    android.content.Intent intent = new android.content.Intent(itemView.getContext(), MainActivity.class);
                    intent.putExtra("open_tour_id", tour.getId());
                    itemView.getContext().startActivity(intent);
                };

                btnViewTour.setOnClickListener(listener);
                itemView.setOnClickListener(listener);

                // Load hình ảnh
                if (ivTourImage != null) {
                    String imageUrl = null;
                    if (tour.getImages() != null && !tour.getImages().isEmpty()) {
                        imageUrl = tour.getImages().get(0).getImageUrl();
                    }
                    if (imageUrl != null && !imageUrl.isEmpty()) {
                        if (imageUrl.startsWith("/")) {
                            imageUrl = "http://10.0.2.2:8000" + imageUrl;
                        }
                        com.bumptech.glide.Glide.with(itemView.getContext())
                                .load(imageUrl)
                                .placeholder(R.drawable.img_taiwan_tour)
                                .centerCrop()
                                .into(ivTourImage);
                    } else {
                        ivTourImage.setImageResource(R.drawable.img_taiwan_tour);
                    }
                }
            }
        }
    }
}
