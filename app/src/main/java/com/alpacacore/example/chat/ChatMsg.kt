package com.alpacacore.example.chat

class ChatMsg internal constructor(@JvmField var src: Source, @JvmField var text: String) {
    enum class Source(@JvmField val value: Int) {
        AI(0), USER(1)
    }
}
