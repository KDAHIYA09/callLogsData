package com.example.calllogs;

import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.provider.CallLog;
import android.util.Log;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    ListView listView;
    SimpleAdapter sa;
    ArrayList list;
    private List<Map<String, String>> callLogList;

    private static final int REQUEST_READ_CALL_LOG = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listView = findViewById(R.id.callLogListView);

        // Check and request the necessary permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALL_LOG)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.READ_CALL_LOG},
                    REQUEST_READ_CALL_LOG
            );
        } else {
            // Permission already granted
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                readCallLogs();
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    private void readCallLogs() {
        ArrayList<CallLogEntry> callLogEntries = new ArrayList<>();
        int count = 0;
        String number, name, type, date;

        // Define the columns to retrieve
        String[] projection = {
                CallLog.Calls.NUMBER,
                CallLog.Calls.CACHED_NAME,
                CallLog.Calls.TYPE,
                CallLog.Calls.DATE
        };

        // Sort the results by date in descending order
        String sortOrder = CallLog.Calls.DATE + " DESC";

        // Query the call log
        try (Cursor cursor = getContentResolver().query(
                CallLog.Calls.CONTENT_URI,
                projection,
                null,
                null,
                sortOrder
        )) {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    // Retrieve call log details
                    number = cursor.getString(cursor.getColumnIndex(CallLog.Calls.NUMBER));
                    name = cursor.getString(cursor.getColumnIndex(CallLog.Calls.CACHED_NAME));
                    type = cursor.getString(cursor.getColumnIndex(CallLog.Calls.TYPE));
                    date = cursor.getString(cursor.getColumnIndex(CallLog.Calls.DATE));

                    // Convert the date to a readable format (optional)
                    Date callDate = new Date(Long.parseLong(date));
                    // Add the call log entry to the ArrayList
                    callLogEntries.add(new CallLogEntry(number, name, type, callDate.toString()));
                } while (cursor.moveToNext());
                count = count+cursor.getCount();
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        }

        Toast.makeText(this, "Data received total entries:" + count , Toast.LENGTH_SHORT).show();
        try {
            list = new ArrayList<>();
            for (CallLogEntry entry : callLogEntries) {
                HashMap<String, String> item = new HashMap<>();
                item.put("line1", entry.getName());
                item.put("line2", entry.getNumber());
                item.put("line3", entry.getType());
                item.put("line4", entry.getDate());
                list.add(item);
            }
        }catch (Exception e){
            e.printStackTrace();
            Toast.makeText(this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        sa = new SimpleAdapter(
                this,
                list,
                R.layout.sample_layout,
                new String[]{"line1", "line2", "line3", "line4", "line5"},
                new int[] {R.id.textViewNumber, R.id.textViewName, R.id.textViewType, R.id.textViewDate}
        );

        // Set the adapter to the ListView
        listView.setAdapter(sa);



//        // Do something with the callLogEntries ArrayList
//        for (CallLogEntry entry : callLogEntries) {
//           Log.d("CallLog", entry.getNumber() + " " + entry.getName() + " " + entry.getType() + " " + entry.getDate());
//           // Toast.makeText(this, "Data" + entry.getNumber() + " " + entry.getName() + " " + entry.getType() + " " + entry.getDate(), Toast.LENGTH_SHORT).show();
//        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_READ_CALL_LOG) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, read call logs
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    readCallLogs();
                }
            } else {
                // Permission denied, handle accordingly
                Log.e("Permission", "Read call log permission denied");
            }
        }
    }
}
