package com.alpacacore.example.chat;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.widget.TextView;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        chatView = (RecyclerView)findViewById(R.id.chat);
        chatLayoutMgr = new LinearLayoutManager(this);
        chatView.setLayoutManager(chatLayoutMgr);
        chatAdapter = new MsgListAdapter();
        chatView.setAdapter(chatAdapter);

        msgText = (EditText)findViewById(R.id.chat_msg);

        executor = Executors.newSingleThreadExecutor();

//        Intent intent = getIntent();
//        String assetPath = intent.getStringExtra("assetPath");
//        executor.execute(() -> {
//            ModelDesc desc = new ModelDesc(
//                "llama.cpp",
//                new ModelDesc.AssetInfo[]{new ModelDesc.AssetInfo(assetPath, ""), },
//                "gpt2");
//            Model model = AlpacaCore.createModel(desc, null, null);
//            instance = model.createInstance("general", null);
//            AlpacaCore.releaseModel(model);
//        });


//        Map result = (Map)instance.runOp("run",
//                Map.of(
//                        "prompt", "To get to the Moon",
//                        "max_tokens", 20
//                ),
//                null);
//        String opResult = (String)result.get("result");

        //textView.setText("Llama result: " + opResult);

//        chatAdapter.pushMessage(new ChatMsg(ChatMsg.Source.AI, "hello"));
//        chatAdapter.pushMessage(new ChatMsg(ChatMsg.Source.USER, "good day, sir"));
//        chatAdapter.pushMessage(new ChatMsg(ChatMsg.Source.AI, "how are your?"));
//        chatAdapter.pushMessage(new ChatMsg(ChatMsg.Source.USER, "I am glad to report that I am great"));
    }

    public void onSend(View v) {
        String text = msgText.getText().toString();
        msgText.setText("");
        chatAdapter.pushMessage(new ChatMsg(ChatMsg.Source.USER, text));
    }
}
