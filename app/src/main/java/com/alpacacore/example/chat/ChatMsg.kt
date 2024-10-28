// Copyright (c) Alpaca Core
// SPDX-License-Identifier: MIT
//
package com.alpacacore.example.chat

data class ChatMsg(var src: Source, var text: String) {
    enum class Source(val value: Int) {
        AI(0), USER(1)
    }
}
