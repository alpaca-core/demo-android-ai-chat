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


import java.util.Map;

import com.alpacacore.*;

public class MainActivity extends Activity {
    private static final String TAG = "MainActivity";
    private long downloadId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        registerReceiver(onDownloadComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE), RECEIVER_EXPORTED);

        TextView tv = (TextView)findViewById(R.id.main_text);
        //tv.setText("Adder result: " + Adder.add(43, 33));

        ModelDesc desc = new ModelDesc("dummy", null, "synthetic");
        Model model = AlpacaCore.createModel(desc, null, null);
        Instance instance = model.createInstance("general", null);
        Map result = (Map)instance.runOp("run", Map.of("input", new String[]{"a", "b"}), null);
        String opResult = (String)result.get("result");

        tv.setText("Dummy result: " + opResult);
    }

    public void onDownload(View v) {
        Log.i(TAG, "on download");
        String url = "https://huggingface.co/datasets/alpaca-core/ac-test-dataset-dummy/resolve/main/data/hello-world.txt";
        String fileName = url.substring(url.lastIndexOf('/') + 1);

        // https://developer.android.com/reference/android/app/DownloadManager.Request
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url))
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
                .setDestinationInExternalFilesDir(this, Environment.DIRECTORY_DOWNLOADS, fileName)
                .setTitle(fileName)
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

                        String uriString = c.getString(c.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));
                        Log.i(TAG, "URI: " + uriString);
                    } else {
                        Log.w(TAG, "Download Unsuccessful, Status Code: " + c.getInt(colIndex));
                        Toast.makeText(MainActivity.this, "Download Unsuccessful", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    };
}
