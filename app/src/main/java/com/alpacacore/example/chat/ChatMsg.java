package com.alpacacore.example.chat;

public class ChatMsg {
    public static enum Source {
        AI(0), USER(1);

        public final int value;

        private Source(int value) {
            this.value = value;
        }
    }
    Source src;
    String text;

    ChatMsg(Source src, String text) {
        this.src = src;
        this.text = text;
    }
}
