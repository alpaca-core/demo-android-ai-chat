package com.alpacacore.example.chat

import android.app.Activity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.EditText
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.alpacacore.AlpacaCore
import com.alpacacore.Instance
import com.alpacacore.ModelDesc
import com.alpacacore.ModelDesc.AssetInfo
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class ChatActivity : Activity() {
    companion object {
        private const val TAG = "ChatActivity"
    }

    lateinit var executor: ExecutorService

    lateinit var chatView: RecyclerView
    lateinit var chatAdapter: MsgListAdapter
    lateinit var chatLayoutMgr: LinearLayoutManager

    lateinit var msgText: EditText

    lateinit var instance: Instance

    lateinit var mainHandler: Handler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        mainHandler = Handler.createAsync(Looper.getMainLooper())

        chatView = findViewById(R.id.chat)
        chatLayoutMgr = LinearLayoutManager(this).apply {
            stackFromEnd = true
        }
        chatView.layoutManager = chatLayoutMgr
        chatAdapter = MsgListAdapter()
        chatView.adapter = chatAdapter

        msgText = findViewById(R.id.chat_msg)

        executor = Executors.newSingleThreadExecutor()

        // init inference in executor thread
        val assetPath = intent.getStringExtra("assetPath")
        executor.execute {
            val desc = ModelDesc(
                "llama.cpp",
                arrayOf(AssetInfo(assetPath, "")),
                "gpt2"
            )
            val model = AlpacaCore.createModel(desc, null, null)
            instance = model.createInstance("general", null)
            AlpacaCore.releaseModel(model)

            val setup = "A chat between a human user and a helpful AI assistant";
            instance.runOp("begin-chat", mapOf("setup" to setup),null)
        }
    }

    private fun pushMessage(msg: ChatMsg) {
        chatAdapter.pushMessage(msg)
        chatView.scrollToPosition(chatAdapter.itemCount - 1)
    }

    fun onSend(v: View?) {
        val userText = msgText.text.toString()
        msgText.text.clear()
        pushMessage(ChatMsg(ChatMsg.Source.USER, userText))

        executor.execute {
            instance.runOp("add-chat-prompt", mapOf("prompt" to userText),null)
            val response = instance.runOp("get-chat-response", null, null) as Map<*, *>
            val aiText = response["response"] as String
            mainHandler.post {
                pushMessage(ChatMsg(ChatMsg.Source.AI, aiText))
            }
        }
    }
}
