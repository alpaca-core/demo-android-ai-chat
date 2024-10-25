package com.alpacacore.example.chat;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.widget.EditText;
import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alpacacore.AlpacaCore;
import com.alpacacore.Instance;
import com.alpacacore.Model;
import com.alpacacore.ModelDesc;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatActivity extends Activity {
    private static final String TAG = "ChatActivity";

    ExecutorService executor;

    RecyclerView chatView;
    MsgListAdapter chatAdapter;
    LinearLayoutManager chatLayoutMgr;

    EditText msgText;

    Instance instance;

    Handler mainHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mainHandler = Handler.createAsync(Looper.getMainLooper());

        chatView = (RecyclerView)findViewById(R.id.chat);
        chatLayoutMgr = new LinearLayoutManager(this);
        chatLayoutMgr.setStackFromEnd(true);
        chatView.setLayoutManager(chatLayoutMgr);
        chatAdapter = new MsgListAdapter();
        chatView.setAdapter(chatAdapter);

        msgText = (EditText)findViewById(R.id.chat_msg);

        executor = Executors.newSingleThreadExecutor();

        // init inference in executor thread
        Intent intent = getIntent();
        String assetPath = intent.getStringExtra("assetPath");
        executor.execute(() -> {
            ModelDesc desc = new ModelDesc(
                "llama.cpp",
                new ModelDesc.AssetInfo[]{new ModelDesc.AssetInfo(assetPath, ""), },
                "gpt2");
            Model model = AlpacaCore.createModel(desc, null, null);
            instance = model.createInstance("general", null);
            AlpacaCore.releaseModel(model);
        });
    }

    private void pushMessage(ChatMsg msg) {
        chatAdapter.pushMessage(msg);
        chatView.scrollToPosition(chatAdapter.getItemCount() - 1);
    }

    public void onSend(View v) {
        String userText = msgText.getText().toString();
        msgText.setText("");
        pushMessage(new ChatMsg(ChatMsg.Source.USER, userText));

        executor.execute(() -> {
            Map result = (Map)instance.runOp("run",
                Map.of(
                    "prompt", userText,
                    "max_tokens", 20
                ),
                null);
            String aiText = (String)result.get("result");
            mainHandler.post(() -> {
                pushMessage(new ChatMsg(ChatMsg.Source.AI, aiText));
            });
        });
    }
}
