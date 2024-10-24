package com.alpacacore.example.chat;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.widget.TextView;

import com.alpacacore.AlpacaCore;
import com.alpacacore.Instance;
import com.alpacacore.Model;
import com.alpacacore.ModelDesc;

import java.util.Map;

public class ChatActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        Intent intent = getIntent();
        String assetPath = intent.getStringExtra("assetPath");

        TextView tv = (TextView)findViewById(R.id.textView);

        ModelDesc desc = new ModelDesc(
                "llama.cpp",
                new ModelDesc.AssetInfo[]{new ModelDesc.AssetInfo(assetPath, ""), },
                "gpt2");
        Model model = AlpacaCore.createModel(desc, null, null);
        Instance instance = model.createInstance("general", null);
        Map result = (Map)instance.runOp("run",
                Map.of(
                        "prompt", "To get to the Moon",
                        "max_tokens", 20
                ),
                null);
        String opResult = (String)result.get("result");

        tv.setText("Llama result: " + opResult);
    }
}
