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
    private void sendMessage(String text) {
        addMessage(new ChatMessage(text, TYPE_USER, now()));
        handler.postDelayed(() -> {
            String reply = generateReply(text);
            addMessage(new ChatMessage(reply, TYPE_BOT, now()));
        }, 800);
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

    // ─── Reply engine ─────────────────────────────────────────────────────────
    private String generateReply(String input) {
        String q = input.toLowerCase().trim();

        // Chào hỏi
        if (matches(q, "xin chào", "hello", "hi", "chào", "hey", "alo")) {
            return "Xin chào! 😊 Tôi là ChillBot.\nBạn muốn biết thông tin về tour nào? "
                    + "Hãy hỏi tôi về tên tour, giá cả, vùng miền hoặc đánh giá nhé!";
        }

        // Không có dữ liệu
        if (!toursLoaded || tourList.isEmpty()) {
            return "Đang tải dữ liệu tour, vui lòng thử lại sau giây lát... ⏳";
        }

        // Tour theo vùng miền
        if (matches(q, "miền bắc", "mien bac", "bắc", "hà nội", "ha noi", "hạ long", "ha long", "sapa", "sa pa")) {
            return getToursByRegion("Miền Bắc");
        }
        if (matches(q, "miền trung", "mien trung", "trung", "đà nẵng", "da nang", "huế", "hue", "nha trang")) {
            return getToursByRegion("Miền Trung");
        }
        if (matches(q, "miền nam", "mien nam", "nam", "hồ chí minh", "hcm", "phú quốc", "phu quoc", "miền tây", "mien tay")) {
            return getToursByRegion("Miền Nam");
        }
        if (matches(q, "nước ngoài", "nuoc ngoai", "quốc tế", "quoc te", "singapore", "đài loan", "dai loan", "thai lan", "thái lan", "nhật", "nhat", "hàn", "han")) {
            return getToursByRegion("Quốc tế");
        }

        // Tour rẻ nhất
        if (matches(q, "rẻ nhất", "re nhat", "giá rẻ", "gia re", "rẻ", "re", "thấp nhất", "thap nhat")) {
            return getCheapestTours();
        }

        // Tour đánh giá cao
        if (matches(q, "đánh giá cao", "danh gia cao", "tốt nhất", "tot nhat", "rating", "điểm cao", "diem cao", "nổi bật", "noi bat", "hot", "phổ biến")) {
            return getTopRatedTours();
        }

        // Giá tour
        if (matches(q, "giá", "gia", "giá tiền", "gia tien", "bao nhiêu tiền", "bao nhieu tien", "cost", "price")) {
            return getTourPriceInfo(q);
        }

        // Tất cả tour / danh sách
        if (matches(q, "tất cả", "tat ca", "danh sách", "danh sach", "list", "có những tour", "co nhung tour", "các tour", "cac tour")) {
            return getAllToursOverview();
        }

        // Hỏi về mô tả / thông tin
        if (matches(q, "mô tả", "mo ta", "thông tin", "thong tin", "giới thiệu", "gioi thieu", "chi tiết", "chi tiet", "describe")) {
            return getTourDescription(q);
        }

        // Hỏi cụ thể tên tour → tìm kiếm theo keyword
        Tour found = findTourByKeyword(q);
        if (found != null) {
            return buildTourDetail(found);
        }

        // Fallback
        return "Tôi chưa tìm thấy thông tin phù hợp với câu hỏi của bạn.\n\n"
                + "Thử hỏi tôi:\n"
                + "• \"Tour miền Bắc có gì?\"\n"
                + "• \"Tour nào giá rẻ nhất?\"\n"
                + "• \"Tour Phú Quốc thông tin?\"\n"
                + "• \"Tour đánh giá cao nhất?\"";
    }

    // ─── Tour query methods ───────────────────────────────────────────────────

    /** Tìm tour theo vùng miền và trả về danh sách */
    private String getToursByRegion(String regionKeyword) {
        List<Tour> filtered = new ArrayList<>();
        for (Tour t : tourList) {
            String region = t.getRegion() != null ? t.getRegion() : "";
            String title  = t.getTitle()  != null ? t.getTitle()  : "";
            if (region.toLowerCase().contains(regionKeyword.toLowerCase())
                    || title.toLowerCase().contains(regionKeyword.toLowerCase())) {
                filtered.add(t);
            }
        }
        if (filtered.isEmpty()) {
            return "Hiện tại ChillTour chưa có tour " + regionKeyword + " trong hệ thống.";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("🗺️ Tour ").append(regionKeyword).append(" hiện có:\n\n");
        int count = Math.min(filtered.size(), 5);
        for (int i = 0; i < count; i++) {
            Tour t = filtered.get(i);
            sb.append("• ").append(t.getTitle()).append("\n");
            sb.append("  💰 ").append(formatPrice(t.getDiscountPrice())).append("\n");
            sb.append("  ⭐ ").append(t.getRatingScore()).append(" (").append(t.getReviewsCount()).append(" đánh giá)\n\n");
        }
        if (filtered.size() > 5) {
            sb.append("... và ").append(filtered.size() - 5).append(" tour khác.\n\n");
        }
        sb.append("Bạn muốn biết chi tiết tour nào? Hỏi tôi tên tour nhé!");
        return sb.toString();
    }

    /** Tour giá rẻ nhất */
    private String getCheapestTours() {
        if (tourList.isEmpty()) return "Không có dữ liệu tour.";
        List<Tour> sorted = new ArrayList<>(tourList);
        sorted.sort((a, b) -> Double.compare(
                a.getDiscountPrice() > 0 ? a.getDiscountPrice() : a.getOriginalPrice(),
                b.getDiscountPrice() > 0 ? b.getDiscountPrice() : b.getOriginalPrice()));

        StringBuilder sb = new StringBuilder("💰 Top 5 tour giá rẻ nhất tại ChillTour:\n\n");
        int count = Math.min(sorted.size(), 5);
        for (int i = 0; i < count; i++) {
            Tour t = sorted.get(i);
            double price = t.getDiscountPrice() > 0 ? t.getDiscountPrice() : t.getOriginalPrice();
            sb.append(i + 1).append(". ").append(t.getTitle()).append("\n");
            sb.append("   💵 ").append(formatPrice(price)).append("/người\n\n");
        }
        sb.append("Hỏi tôi tên tour để biết thêm chi tiết!");
        return sb.toString();
    }

    /** Tour đánh giá cao nhất */
    private String getTopRatedTours() {
        if (tourList.isEmpty()) return "Không có dữ liệu tour.";
        List<Tour> sorted = new ArrayList<>(tourList);
        sorted.sort((a, b) -> Double.compare(b.getRatingScore(), a.getRatingScore()));

        StringBuilder sb = new StringBuilder("⭐ Top 5 tour được đánh giá cao nhất:\n\n");
        int count = Math.min(sorted.size(), 5);
        for (int i = 0; i < count; i++) {
            Tour t = sorted.get(i);
            sb.append(i + 1).append(". ").append(t.getTitle()).append("\n");
            sb.append("   ⭐ ").append(t.getRatingScore())
              .append(" – ").append(t.getReviewsCount()).append(" đánh giá\n");
            sb.append("   💰 ").append(formatPrice(t.getDiscountPrice())).append("\n\n");
        }
        sb.append("Muốn biết chi tiết tour nào? Cứ hỏi tôi nhé!");
        return sb.toString();
    }

    /** Thông tin giá */
    private String getTourPriceInfo(String q) {
        Tour found = findTourByKeyword(q);
        if (found != null) {
            return buildTourDetail(found);
        }
        // Thống kê chung
        double min = Double.MAX_VALUE, max = 0;
        for (Tour t : tourList) {
            double p = t.getDiscountPrice() > 0 ? t.getDiscountPrice() : t.getOriginalPrice();
            if (p < min) min = p;
            if (p > max) max = p;
        }
        return "💰 Thông tin giá tour tại ChillTour:\n\n"
                + "• Giá thấp nhất: " + formatPrice(min) + "/người\n"
                + "• Giá cao nhất: " + formatPrice(max) + "/người\n\n"
                + "Bạn hỏi cụ thể tên tour nào để tôi tra giá chính xác nhé!";
    }

    /** Danh sách tổng quan tất cả tour */
    private String getAllToursOverview() {
        if (tourList.isEmpty()) return "Hiện tại không có tour nào trong hệ thống.";
        StringBuilder sb = new StringBuilder("📋 Danh sách tour của ChillTour (" + tourList.size() + " tour):\n\n");
        int count = Math.min(tourList.size(), 8);
        for (int i = 0; i < count; i++) {
            Tour t = tourList.get(i);
            sb.append("• ").append(t.getTitle()).append("\n");
        }
        if (tourList.size() > 8) sb.append("... và ").append(tourList.size() - 8).append(" tour khác.\n");
        sb.append("\nBạn hỏi tên tour cụ thể để tôi cung cấp thông tin chi tiết nhé!");
        return sb.toString();
    }

    /** Mô tả tour theo keyword */
    private String getTourDescription(String q) {
        Tour found = findTourByKeyword(q);
        if (found != null) return buildTourDetail(found);
        return "Bạn muốn tìm hiểu tour nào?\nHãy nhắn tên địa điểm hoặc tên tour để tôi tra thông tin!";
    }

    /** Tìm tour khớp với keyword bất kỳ trong câu */
    private Tour findTourByKeyword(String q) {
        // Ưu tiên khớp chính xác nhiều từ nhất
        Tour bestMatch = null;
        int  bestScore = 0;
        for (Tour t : tourList) {
            String title = t.getTitle() != null ? t.getTitle().toLowerCase() : "";
            String desc  = t.getDescription() != null ? t.getDescription().toLowerCase() : "";
            String region = t.getRegion() != null ? t.getRegion().toLowerCase() : "";
            int score = 0;
            // Tính điểm khớp theo từng từ trong query
            String[] words = q.split("\\s+");
            for (String w : words) {
                if (w.length() < 2) continue;
                if (title.contains(w))  score += 3;
                if (region.contains(w)) score += 2;
                if (desc.contains(w))   score += 1;
            }
            if (score > bestScore) {
                bestScore = score;
                bestMatch = t;
            }
        }
        return bestScore >= 2 ? bestMatch : null;
    }

    /** Tạo thẻ thông tin chi tiết một tour */
    private String buildTourDetail(Tour t) {
        StringBuilder sb = new StringBuilder();
        sb.append("🏖️ ").append(t.getTitle()).append("\n");
        sb.append("─────────────────────\n");

        if (t.getRegion() != null && !t.getRegion().isEmpty()) {
            sb.append("📍 Vùng: ").append(t.getRegion()).append("\n");
        }
        if (t.getProvider() != null && !t.getProvider().isEmpty()) {
            sb.append("🏢 Nhà cung cấp: ").append(t.getProvider()).append("\n");
        }

        double price = t.getDiscountPrice() > 0 ? t.getDiscountPrice() : t.getOriginalPrice();
        sb.append("💰 Giá: ").append(formatPrice(price)).append("/người\n");
        if (t.getDiscountPrice() > 0 && t.getOriginalPrice() > t.getDiscountPrice()) {
            sb.append("   Gốc: ~~").append(formatPrice(t.getOriginalPrice())).append("~~\n");
        }

        sb.append("⭐ Đánh giá: ").append(t.getRatingScore())
          .append("/5 (").append(t.getReviewsCount()).append(" đánh giá)\n");

        if (t.getDescription() != null && !t.getDescription().isEmpty()) {
            String desc = t.getDescription();
            if (desc.length() > 200) desc = desc.substring(0, 200) + "...";
            sb.append("\n📝 ").append(desc).append("\n");
        }

        if (t.getDescriptionTourInclude() != null && !t.getDescriptionTourInclude().isEmpty()) {
            sb.append("\n✅ Tour bao gồm:\n").append(t.getDescriptionTourInclude()).append("\n");
        }

        return sb.toString();
    }

    private String formatPrice(double price) {
        if (price <= 0) return "Liên hệ";
        DecimalFormat df = new DecimalFormat("#,###");
        return df.format((long) price) + " đ";
    }

    private boolean matches(String input, String... keywords) {
        for (String kw : keywords) {
            if (input.contains(kw)) return true;
        }
        return false;
    }

    // ─── Data model ───────────────────────────────────────────────────────────
    static class ChatMessage {
        final String content;
        final int    type;
        final String time;

        ChatMessage(String content, int type, String time) {
            this.content = content;
            this.type    = type;
            this.time    = time;
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
            } else {
                return new UserVH(inf.inflate(R.layout.item_chat_user, parent, false));
            }
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int pos) {
            ChatMessage msg = data.get(pos);
            if (holder instanceof BotVH)  ((BotVH)  holder).bind(msg);
            else                          ((UserVH) holder).bind(msg);
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
    }
}
