package com.example.genie.model;

import java.util.List;

public class ChatResponseObject {
    public List<Choice> choices;

    public static class Choice {
        public Message message;

        public static class Message {
            public String role;
            public String content;
        }
    }
}
