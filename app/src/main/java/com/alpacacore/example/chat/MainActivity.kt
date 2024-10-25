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
import android.widget.Toast
import java.io.File

class MainActivity : Activity() {
    companion object {
        private const val TAG = "MainActivity"

        private const val assetUrl =
            "https://huggingface.co/alpaca-core/ac-test-data-llama/resolve/main/gpt2-117m-q6_k.gguf"

        private val assetFname = assetUrl.substring(assetUrl.lastIndexOf('/') + 1)
    }

    private var downloadId = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val assetFile = getAssetFile();
        if (assetFile.exists()) {
            Log.i(TAG, "Asset exists at: $assetFile")
            onAssetReady(assetFile.absolutePath)
        } else {
            Log.i(TAG, "Asset not found. Downloading")
            downloadAsset()
            registerReceiver(
                onDownloadComplete,
                IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE),
                RECEIVER_EXPORTED
            )
        }
    }

    private fun onAssetReady(path: String?) {
        val intent = Intent(this, ChatActivity::class.java)
        intent.putExtra("assetPath", path)
        startActivity(intent)
    }

    private fun getAssetFile(): File {
        val downloadDir = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
        return File(downloadDir, assetFname)
    }

    private fun downloadAsset() {
        Log.i(TAG, "on download")

        val request = DownloadManager.Request(Uri.parse(assetUrl))
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
            .setTitle(assetFname)
            .setDestinationInExternalFilesDir(this, Environment.DIRECTORY_DOWNLOADS, assetFname)
            .setMimeType("application/octet-stream")

        val dm = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        downloadId = dm.enqueue(request) // add download request to the queue
    }

    private val onDownloadComplete: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            if (downloadId != id) return; // not our download

            Log.i(TAG, "Download ID: $downloadId")

            // Get file URI
            val dm = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
            val query = DownloadManager.Query()
            query.setFilterById(downloadId)
            val c = dm.query(query)
            if (c.moveToFirst()) {
                val colIndex = c.getColumnIndex(DownloadManager.COLUMN_STATUS)
                if (DownloadManager.STATUS_SUCCESSFUL == c.getInt(colIndex)) {
                    Log.i(TAG, "Download Complete")
                    Toast.makeText(this@MainActivity, "Download Complete", Toast.LENGTH_SHORT)
                        .show()

                    val uriString =
                        c.getString(c.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI))
                    val path = Uri.parse(uriString).path
                    assert(path == getAssetFile().getAbsolutePath())
                    Log.i(TAG, "URI: $uriString")
                    onAssetReady(path)
                } else {
                    Log.w(TAG, "Download Failed, Status Code: " + c.getInt(colIndex))
                    Toast.makeText(this@MainActivity, "Download Failed", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }
}
