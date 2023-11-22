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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
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
        String number, name, type, date, duration, time;
        int imageResource = 0;

        // Define the columns to retrieve
        String[] projection = {
                CallLog.Calls.NUMBER,
                CallLog.Calls.CACHED_NAME,
                CallLog.Calls.TYPE,
                CallLog.Calls.DATE,
                CallLog.Calls.DURATION
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
                    name = name==null || name.equals("") ? "Unknown" : name;
                    type = cursor.getString(cursor.getColumnIndex(CallLog.Calls.TYPE));
                    date = cursor.getString(cursor.getColumnIndex(CallLog.Calls.DATE));
                    duration = cursor.getString(cursor.getColumnIndex(CallLog.Calls.DURATION));

                    duration = DurationFormat(duration);

                    SimpleDateFormat timeFormatter = new SimpleDateFormat("hh:mm:ss a", Locale.getDefault());
                    time = timeFormatter.format(new Date(Long.parseLong(date)));

// this give 24 hours format with am pm which is inserted with help og a symbole
//                    SimpleDateFormat timeFormatter = new SimpleDateFormat("HH:mm:ss a", Locale.getDefault());
//                    time = timeFormatter.format(new Date(Long.parseLong(date)));

                    SimpleDateFormat dateFormatter = new SimpleDateFormat("dd MMM yyyy");
                    date = dateFormatter.format(new Date(Long.parseLong(date)));



                    switch(Integer.parseInt(type)){
                        case CallLog.Calls.INCOMING_TYPE:
                            type = "Incoming";
                            imageResource = R.drawable.incoming_call_rounded_svgrepo_com;
                            break;
                        case CallLog.Calls.OUTGOING_TYPE:
                            imageResource = R.drawable.outgoing_call_svgrepo_com;
                            type = "Outgoing";
                            break;
                        case CallLog.Calls.MISSED_TYPE:
                            type = "Missed";
                            imageResource = R.drawable.call_miss_svgrepo_com;
                            break;
                        case CallLog.Calls.VOICEMAIL_TYPE:
                            type = "Voicemail";
                            imageResource = R.drawable.voicemail_svgrepo_com;
                            break;
                        case CallLog.Calls.REJECTED_TYPE:
                            type = "Rejected";
                            imageResource = R.drawable.rejectedcall;
                            break;
                        case CallLog.Calls.BLOCKED_TYPE:
                            type = "Blocked";
                            imageResource = R.drawable.callblocked;
                            break;
                        case CallLog.Calls.ANSWERED_EXTERNALLY_TYPE:
                            type = "Externally Answered";
                            imageResource = R.drawable.nacall;
                            break;
                        default:
                            type = "NA";
                            imageResource = R.drawable.nacall;
                    }

                    callLogEntries.add(new CallLogEntry(number, name, type, date.toString(), duration,time, imageResource));
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
                HashMap<String, Object> item = new HashMap<>();
                item.put("line1", entry.getImage());
                item.put("line2", "Name: "+entry.getName());
                item.put("line3", "Phone no.: "+entry.getNumber());
                item.put("line4", "        "+entry.getType());
                item.put("line5", "Date: "+entry.getDate());
                item.put("line6", "Time: "+entry.getTime());
                item.put("line7", "Duration: "+entry.getDuration());
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
                new String[]{"line1", "line2", "line3", "line4", "line5","line6","line7"},
                new int[] {R.id.imageViewCallType, R.id.textViewNumber, R.id.textViewName, R.id.textViewType, R.id.textViewDate, R.id.textViewTime, R.id.textViewDuration}
        );

        // Set the adapter to the ListView
        listView.setAdapter(sa);


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

    private String DurationFormat(String duration) {
        String durationFormatted=null;
        if(Integer.parseInt(duration) < 60){
            durationFormatted = duration+" sec";
        }
        else{
            int min = Integer.parseInt(duration)/60;
            int sec = Integer.parseInt(duration)%60;

            if(sec==0)
                durationFormatted = min + " min" ;
            else
                durationFormatted = min + " min " + sec + " sec";

        }
        return durationFormatted;
    }

}
