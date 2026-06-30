package com.example.myapplication.data.model;

import com.google.gson.annotations.SerializedName;

public class ChatbotRequest {
    @SerializedName("message")
    private String message;

    @SerializedName("session_id")
    private String sessionId;

    @SerializedName("user_id")
    private Integer userId;

    public ChatbotRequest(String message, String sessionId, Integer userId) {
        this.message = message;
        this.sessionId = sessionId;
        this.userId = userId;
    }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }
}
