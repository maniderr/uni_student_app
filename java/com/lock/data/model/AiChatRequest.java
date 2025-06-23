package com.lock.data.model;

import java.util.List;

public class AiChatRequest {
    private String model;
    private List<AiMessage> messages;
    private double temperature;

    public AiChatRequest(String model, List<AiMessage> messages, double temperature) {
        this.model = model;
        this.messages = messages;
        this.temperature = temperature;
    }
}
