// Copyright (c) Alpaca Core
// SPDX-License-Identifier: MIT
//
package com.alpacacore.example.chat;

import android.app.Activity;
import android.app.DownloadManager;
import android.os.Bundle;
import android.os.Environment;
import android.net.Uri;
import android.util.Log;
import android.widget.TextView;
import android.view.View;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.widget.Toast;

import java.io.File;
import java.util.Map;

import com.alpacacore.*;

public class MainActivity extends Activity {
    private static final String TAG = "MainActivity";

    private static final String assetUrl =
        "https://huggingface.co/alpaca-core/ac-test-data-llama/resolve/main/gpt2-117m-q6_k.gguf";

    private final String assetFname;

    private long downloadId = 0L;

    public MainActivity() {
        super();
        assetFname = assetUrl.substring(assetUrl.lastIndexOf('/') + 1);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        File assetFile = getAssetFile();
        if (assetFile.exists()) {
            Log.i(TAG, "Asset exists at: " + assetFile);
            onAssetReady(assetFile.getAbsolutePath());
        }
        else {
            Log.i(TAG, "Asset not found. Downloading");
            downloadAsset();
            registerReceiver(onDownloadComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE), RECEIVER_EXPORTED);
        }
    }

    private void onAssetReady(String path) {
        TextView tv = (TextView)findViewById(R.id.main_text);
        //tv.setText("Adder result: " + Adder.add(43, 33));

        ModelDesc desc = new ModelDesc(
                "llama.cpp",
                new ModelDesc.AssetInfo[]{new ModelDesc.AssetInfo(path, ""), },
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

        tv.setText("Dummy result: " + opResult);
    }

    private File getAssetFile() {
        File downloadDir = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
        return new File(downloadDir, assetFname);
    }

    private void downloadAsset() {
        Log.i(TAG, "on download");

        // https://developer.android.com/reference/android/app/DownloadManager.Request
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(assetUrl))
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
                .setTitle(assetFname)
                .setDestinationInExternalFilesDir(this, Environment.DIRECTORY_DOWNLOADS, assetFname)
                .setMimeType("application/octet-stream");

        DownloadManager dm = (DownloadManager)getSystemService(DOWNLOAD_SERVICE);
        downloadId = dm.enqueue(request); // add download request to the queue
    }

    private final BroadcastReceiver onDownloadComplete = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get the download ID received with the broadcast
            long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);

            // Check if the ID matches our download ID
            if (downloadId == id) {
                Log.i(TAG, "Download ID: " + downloadId);

                // Get file URI
                DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                DownloadManager.Query query = new DownloadManager.Query();
                query.setFilterById(downloadId);
                Cursor c = dm.query(query);
                if (c.moveToFirst()) {
                    int colIndex = c.getColumnIndex(DownloadManager.COLUMN_STATUS);
                    if (DownloadManager.STATUS_SUCCESSFUL == c.getInt(colIndex)) {
                        Log.i(TAG, "Download Complete");
                        Toast.makeText(MainActivity.this, "Download Complete", Toast.LENGTH_SHORT).show();

                        final String uriString = c.getString(c.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));
                        final String path = Uri.parse(uriString).getPath();
                        assert(path.equals(getAssetFile().getAbsolutePath()));
                        Log.i(TAG, "URI: " + uriString);
                        onAssetReady(path);
                    } else {
                        Log.w(TAG, "Download Failed, Status Code: " + c.getInt(colIndex));
                        Toast.makeText(MainActivity.this, "Download Failed", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    };
}
