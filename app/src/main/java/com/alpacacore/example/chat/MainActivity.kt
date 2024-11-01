// Copyright (c) Alpaca Core
// SPDX-License-Identifier: MIT
//
package com.alpacacore.example.chat

import android.app.Activity
import android.app.AlertDialog
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

        lateinit var file: File

        val availableLocally
            get() = file.exists()

        var downloadId = 0L

        init {
            fileName = assetUrl.substring(assetUrl.lastIndexOf('/') + 1)
        }

        fun init(downloadDir: File?) {
            file = File(downloadDir, fileName)
        }
    }

    private val models = arrayOf(
        ModelData(
            "GPT2 117M",
            "131 MB download! Very fast and very stupid.",
            "https://huggingface.co/alpaca-core/ac-test-data-llama/resolve/main/gpt2-117m-q6_k.gguf"
        ),
        ModelData(
            "Tiny LLaMa 1.1B",
            "904 MB download! Good performance. Mostly coherent",
            "https://huggingface.co/TheBloke/TinyLlama-1.1B-Chat-v1.0-GGUF/resolve/main/tinyllama-1.1b-chat-v1.0.Q6_K.gguf"
        ),
        ModelData(
            "Mistral 7B Instruct",
            "5.6 GB download! Very slow. Adequate responses.",
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

    lateinit var modelListView: ListView
    private fun buildModelListView() {
        var list = ArrayList<Map<String, String>>()
        for (m in models) {
            var name = m.name
            if (m.downloadId != 0L) {
                name += " (download in progress)"
            }
            else if (!m.availableLocally) {
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
        adapter.notifyDataSetChanged()
    }

    private fun selectModel(model: ModelData) {
        if (model.downloadId != 0L) {
            Toast.makeText(
                this,
                "Dowload of ${model.name} assets is in progress",
                Toast.LENGTH_SHORT
            ).show()
        }
        else if (model.availableLocally) {
            val intent = Intent(this, ChatActivity::class.java)
            intent.putExtra("name", model.name)
            intent.putExtra("desc", model.desc)
            intent.putExtra("assetPath", model.file.absolutePath)
            startActivity(intent)
        }
        else {
            AlertDialog.Builder(this)
                .setMessage("Download assets for ${model.name}?")
                .setPositiveButton("Download") { dialog, id ->
                    initiateModelDownload(model)
                }
                .setNegativeButton("Cancel") { dialog, id -> }
                .show()
        }
    }

    fun initiateModelDownload(model: ModelData) {
        assert(!model.availableLocally)
        assert(model.downloadId == 0L)

        Log.i(TAG, "on download")

        val request = DownloadManager.Request(Uri.parse(model.assetUrl))
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
            .setTitle(model.fileName)
            .setDestinationInExternalFilesDir(this, Environment.DIRECTORY_DOWNLOADS, model.fileName)
            .setMimeType("application/octet-stream")

        val dm = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        model.downloadId = dm.enqueue(request) // add download request to the queue
        buildModelListView() // update model list
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initModels()

        setContentView(R.layout.activity_main)
        modelListView = findViewById<ListView>(R.id.model_list)
        buildModelListView()
        modelListView.setOnItemClickListener { parent, view, pos, id -> selectModel(models[pos]) }

        registerReceiver(
            onDownloadComplete,
            IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE),
            RECEIVER_EXPORTED
        )
    }

    private val onDownloadComplete: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)

            val model = models.firstOrNull { it.downloadId == id }
            if (model == null) return // not our download

            model.downloadId = 0L
            buildModelListView() // update list view

            val dm = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
            val query = DownloadManager.Query()
            query.setFilterById(id)
            val c = dm.query(query)
            if (c.moveToFirst()) {
                val colIndex = c.getColumnIndex(DownloadManager.COLUMN_STATUS)
                if (DownloadManager.STATUS_SUCCESSFUL == c.getInt(colIndex)) {
                    Log.i(TAG, "Download Complete")
                    Toast.makeText(this@MainActivity, "Download Complete", Toast.LENGTH_SHORT)
                        .show()
                    assert(model.availableLocally)
                } else {
                    Log.w(TAG, "Download Failed, Status Code: " + c.getInt(colIndex))
                    Toast.makeText(this@MainActivity, "Download Failed", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }
}
