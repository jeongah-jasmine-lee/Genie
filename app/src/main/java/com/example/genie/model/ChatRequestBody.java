package com.example.genie.model;

import java.util.List;

public class ChatRequestBody {
    public String model;
    public List<Message> messages;
    public double temperature;

    public ChatRequestBody(String model, List<Message> messages, double temperature) {
        this.model = model;
        this.messages = messages;
        this.temperature = temperature;
    }

    public static class Message {
        public String role;
        public String content;

        public Message(String role, String content) {
            this.role = role;
            this.content = content;
        }
    }
}
