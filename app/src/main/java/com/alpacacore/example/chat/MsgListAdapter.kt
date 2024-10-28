// Copyright (c) Alpaca Core
// SPDX-License-Identifier: MIT
//
package com.alpacacore.example.chat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MsgListAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val msgList = mutableListOf<ChatMsg>()

    fun pushMessage(msg: ChatMsg) {
        msgList.add(msg)
        notifyItemInserted(msgList.size - 1)
    }

    override fun getItemCount(): Int {
        return msgList.size
    }

    override fun getItemViewType(pos: Int): Int {
        val msg = msgList[pos]
        return msg.src.value
    }

    private open class MsgHolder(itemView: View, id: Int) : RecyclerView.ViewHolder(itemView) {
        protected final var textView: TextView
        init {
            textView = itemView.findViewById<View>(id) as TextView
        }
        fun setText(text: String) {
            textView.text = text
        }
    }

    private class AiMsgHolder(itemView: View) : MsgHolder(itemView, R.id.msg_ai_msg)

    private class MyMsgHolder(itemView: View) : MsgHolder(itemView, R.id.msg_me_msg)

    override fun onCreateViewHolder(parent: ViewGroup, type: Int): RecyclerView.ViewHolder {
        if (type == ChatMsg.Source.AI.value) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.msg_ai, parent, false)
            return AiMsgHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.msg_me, parent, false)
            return MyMsgHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, pos: Int) {
        val msg = msgList[pos]
        (holder as MsgHolder).setText(msg.text)
    }
}
