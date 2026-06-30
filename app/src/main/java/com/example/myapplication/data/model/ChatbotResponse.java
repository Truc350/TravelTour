package com.example.myapplication.data.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class ChatbotResponse {
    @SerializedName("session_id")
    private String sessionId;

    @SerializedName("response")
    private String response;

    @SerializedName("tours")
    private List<Tour> tours;

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }

    public String getResponse() { return response; }
    public void setResponse(String response) { this.response = response; }

    public List<Tour> getTours() { return tours; }
    public void setTours(List<Tour> tours) { this.tours = tours; }
}
