// Copyright (c) Alpaca Core
// SPDX-License-Identifier: MIT
//
package com.alpacacore.example.chat

import android.app.Activity
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.ListView
import android.widget.SimpleAdapter
import android.widget.Toast
import java.io.File

class MainActivity : Activity() {
    class ModelData(var name: String, var desc: String, var assetUrl: String) {
        val fileName: String
        var exists: Boolean = false
        var downloadId = 0L

        init {
            fileName = assetUrl.substring(assetUrl.lastIndexOf('/') + 1)
        }

        fun init(downloadDir: File?) {
            val file = File(downloadDir, fileName)
            exists = file.exists()
        }
    }

    private val models = arrayOf(
        ModelData(
            "GPT2 117M",
            "very fast and very stupid",
            "https://huggingface.co/alpaca-core/ac-test-data-llama/resolve/main/gpt2-117m-q6_k.gguf"
        ),
        ModelData(
            "Tiny LLaMa 1.1B",
            "good performance, mostly coherent",
            "https://huggingface.co/TheBloke/TinyLlama-1.1B-Chat-v1.0-GGUF/resolve/main/tinyllama-1.1b-chat-v1.0.Q6_K.gguf"
        ),
        ModelData(
            "Mistral 7B Instruct",
            "Very slow. Adequate responses.",
            "https://huggingface.co/TheBloke/Mistral-7B-Instruct-v0.2-GGUF/resolve/main/mistral-7b-instruct-v0.2.Q6_K.gguf"
        )
    )

    companion object {
        private const val TAG = "MainActivity"
    }

    private fun initModels() {
        val downloadDir = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
        for (m in models) {
            m.init(downloadDir)
        }
    }

    lateinit var modelListView: ListView;
    private fun buildModelListView() {
        var list = ArrayList<Map<String, String>>()
        for (m in models) {
            var name = m.name;
            if (!m.exists) {
                name += " (needs download)"
            }
            list.add(mapOf(
                "name" to name,
                "desc" to m.desc
            ))
        }

        var from = arrayOf("name", "desc")
        var to = intArrayOf(R.id.model_name, R.id.model_desc)

        var adapter = SimpleAdapter(this, list, R.layout.model_list_item, from, to)
        modelListView.adapter = adapter
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initModels()

        setContentView(R.layout.activity_main)
        modelListView = findViewById<ListView>(R.id.model_list)
        buildModelListView()


//        val assetFile = getAssetFile();
//        if (assetFile.exists()) {
//            Log.i(TAG, "Asset exists at: $assetFile")
//            onAssetReady(assetFile.absolutePath)
//        } else {
//            Log.i(TAG, "Asset not found. Downloading")
//            downloadAsset()
//            registerReceiver(
//                onDownloadComplete,
//                IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE),
//                RECEIVER_EXPORTED
//            )
//        }
    }

    private fun onAssetReady(path: String?) {
        val intent = Intent(this, ChatActivity::class.java)
        intent.putExtra("assetPath", path)
        startActivity(intent)
    }

//    private fun downloadAsset() {
//        Log.i(TAG, "on download")
//
//        val request = DownloadManager.Request(Uri.parse(assetUrl))
//            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
//            .setTitle(assetFname)
//            .setDestinationInExternalFilesDir(this, Environment.DIRECTORY_DOWNLOADS, assetFname)
//            .setMimeType("application/octet-stream")
//
//        val dm = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
//        downloadId = dm.enqueue(request) // add download request to the queue
//    }

//    private val onDownloadComplete: BroadcastReceiver = object : BroadcastReceiver() {
//        override fun onReceive(context: Context, intent: Intent) {
//            val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
//            if (downloadId != id) return; // not our download
//
//            Log.i(TAG, "Download ID: $downloadId")
//
//            // Get file URI
//            val dm = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
//            val query = DownloadManager.Query()
//            query.setFilterById(downloadId)
//            val c = dm.query(query)
//            if (c.moveToFirst()) {
//                val colIndex = c.getColumnIndex(DownloadManager.COLUMN_STATUS)
//                if (DownloadManager.STATUS_SUCCESSFUL == c.getInt(colIndex)) {
//                    Log.i(TAG, "Download Complete")
//                    Toast.makeText(this@MainActivity, "Download Complete", Toast.LENGTH_SHORT)
//                        .show()
//
//                    val uriString =
//                        c.getString(c.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI))
//                    val path = Uri.parse(uriString).path
//                    assert(path == getAssetFile().getAbsolutePath())
//                    Log.i(TAG, "URI: $uriString")
//                    onAssetReady(path)
//                } else {
//                    Log.w(TAG, "Download Failed, Status Code: " + c.getInt(colIndex))
//                    Toast.makeText(this@MainActivity, "Download Failed", Toast.LENGTH_SHORT)
//                        .show()
//                }
//            }
//        }
//    }
}
