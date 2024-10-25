package com.alpacacore.example.chat;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class MsgListAdapter extends RecyclerView.Adapter {
    private final List<ChatMsg> msgList = new ArrayList<ChatMsg>();

    public void pushMessage(ChatMsg msg) {
        msgList.add(msg);
        notifyItemInserted(msgList.size() - 1);
    }

    @Override
    public int getItemCount() {
        return msgList.size();
    }

    @Override
    public int getItemViewType(int pos) {
        ChatMsg msg = msgList.get(pos);
        return msg.src.value;
    }

    private static class MsgHolder extends RecyclerView.ViewHolder {
        protected TextView textView;
        MsgHolder(View itemView) {
            super(itemView);
        }
        public void setText(String text) {
            textView.setText(text);
        }
    }

    private static class AiMsgHolder extends MsgHolder {
        AiMsgHolder(View itemView) {
            super(itemView);
            textView = (TextView)itemView.findViewById(R.id.msg_ai_msg);
        }
    }

    private static class MyMsgHolder extends MsgHolder {
        MyMsgHolder(View itemView) {
            super(itemView);
            textView = (TextView)itemView.findViewById(R.id.msg_me_msg);
        }
    }

    @Override
    public @NonNull RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int type) {
        if (type == ChatMsg.Source.AI.value) {
            View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.msg_ai, parent, false);
            return new AiMsgHolder(view);
        }
        else {
            View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.msg_me, parent, false);
            return new MyMsgHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int pos) {
        ChatMsg msg = msgList.get(pos);
        ((MsgHolder)holder).setText(msg.text);
    }
}