package com.example.mmi.Teacher;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.example.mmi.DBUtility;
import com.example.mmi.R;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.AdvertisingOptions;
import com.google.android.gms.nearby.connection.ConnectionInfo;
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback;
import com.google.android.gms.nearby.connection.ConnectionResolution;
import com.google.android.gms.nearby.connection.ConnectionsClient;
import com.google.android.gms.nearby.connection.ConnectionsStatusCodes;
import com.google.android.gms.nearby.connection.Payload;
import com.google.android.gms.nearby.connection.PayloadCallback;
import com.google.android.gms.nearby.connection.PayloadTransferUpdate;
import com.google.android.gms.nearby.connection.Strategy;
import org.json.JSONObject;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class AttendanceListActivity extends AppCompatActivity {

    private HashMap<String, HashMap<String, String>> attendanceMap = new HashMap<>();
    private boolean success = false;
    private ListView listView;
    private ArrayList<Attendance> itemArrayList;
    private AttendanceListActivity.MyAppAdapter myAppAdapter;
    private String cDate;
    private String cID;
    private Timer timer = new Timer("Timer");

    private ConnectionsClient connectionsClient;
    private static final String TAG = "MMI";

    private static final String[] REQUIRED_PERMISSIONS =
            new String[] {
                    Manifest.permission.BLUETOOTH,
                    Manifest.permission.BLUETOOTH_ADMIN,
                    Manifest.permission.ACCESS_WIFI_STATE,
                    Manifest.permission.CHANGE_WIFI_STATE,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
            };

    private static final int REQUEST_CODE_REQUIRED_PERMISSIONS = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_attendance_list);

        connectionsClient = Nearby.getConnectionsClient(this);

        Button refresh = findViewById(R.id.button_refresh);
        refresh.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                finish();
                startActivity(getIntent());
            }
        });

        Button attendance = findViewById(R.id.button_attendance);
        attendance.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (!hasPermissions(AttendanceListActivity.this, REQUIRED_PERMISSIONS)) {
                    requestPermissions(REQUIRED_PERMISSIONS, REQUEST_CODE_REQUIRED_PERMISSIONS);
                }
                toastMsg("Taking Attendance");
                startAdvertising();
                long delay = 300000;
                timer.schedule(task,delay);
            }
        });

        listView = findViewById(R.id.listView);
        itemArrayList = new ArrayList<Attendance>();

        Intent intent = getIntent();

        cDate = intent.getStringExtra("classDate");
        cID = intent.getStringExtra("classID");

        SyncData orderData = new SyncData();
        orderData.execute(cDate, cID);
    }

    private class SyncData extends AsyncTask<String, Void, String[]>
    {
        @Override
        protected String[] doInBackground(String... params)
        {
            try {
                Connection con = DBUtility.connect();
                if (!(con == null))
                {
                    String classDate = params[0];
                    String classID = params[1];
                    String attendanceList;
                    String studentList;
                    Statement st = con.createStatement();
                    ResultSet rs = st.executeQuery("SELECT ATTENDANCELIST FROM mmi_classmeetings where CLASSDATE='" + classDate + "'");
                        try{
                            while(rs.next()) {
                                attendanceList = rs.getString("ATTENDANCELIST");
                                JSONObject attendance = new JSONObject(attendanceList);
                                Iterator<?> keys = attendance.keys();
                                while(keys.hasNext())
                                {
                                    HashMap<String, String> values = new HashMap<>();
                                    String key = (String)keys.next();
                                    JSONObject inner = new JSONObject(attendance.getString(key));
                                    Iterator<?>keys2 = inner.keys();
                                    while(keys2.hasNext())
                                    {
                                        String key2 = (String)keys2.next();
                                        String value = inner.getString(key2);
                                        values.put(key2,value);
                                    }
                                    attendanceMap.put(key, values);
                                }
                            }
                            ResultSet rs2 = st.executeQuery("SELECT STUDENTLIST FROM mmi_classinfo where CLASSID='" + classID + "'");
                            while(rs2.next())
                            {
                                studentList = rs2.getString("STUDENTLIST");
                                JSONObject student = new JSONObject(studentList);
                                Iterator<?> keys3 = student.keys();
                                while(keys3.hasNext())
                                {
                                    String key = (String)keys3.next();
                                    String value = student.getString(key);
                                    String late = (String)attendanceMap.get(key).get("late");
                                    String note = (String)attendanceMap.get(key).get("note");
                                    String present = (String)attendanceMap.get(key).get("present");
                                    itemArrayList.add(new Attendance(key, value, late, note, present));
                                }
                            }
                            success = true;
                    } catch(Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            } catch (Exception e)
            {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String[] msg)
        {
            if(!success)
            {
            } else {
                try {
                    myAppAdapter = new AttendanceListActivity.MyAppAdapter(itemArrayList, AttendanceListActivity.this);
                    listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
                    listView.setAdapter(myAppAdapter);
                    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> adapter, View view, int position, long id)
                        {
                            Intent attendanceDetailIntent = new Intent(AttendanceListActivity.this, AttendanceDetailActivity.class);
                            attendanceDetailIntent.putExtra("id",itemArrayList.get(position).getId());
                            attendanceDetailIntent.putExtra("name",itemArrayList.get(position).getName());
                            attendanceDetailIntent.putExtra("late",itemArrayList.get(position).getLate());
                            attendanceDetailIntent.putExtra("note",itemArrayList.get(position).getNote());
                            attendanceDetailIntent.putExtra("present",itemArrayList.get(position).getPresent());
                            attendanceDetailIntent.putExtra("cDate", cDate);
                            attendanceDetailIntent.putExtra("cID", cID);
                            startActivityForResult(attendanceDetailIntent, 1);
                        }

                    });

                } catch (Exception ex)
                {
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==RESULT_OK){
            finish();
            overridePendingTransition(0, 0);
            startActivity(getIntent());
            overridePendingTransition(0, 0);
        }
    }

    public class MyAppAdapter extends BaseAdapter
    {
        public class ViewHolder {
            TextView textName;
        }

        public List<Attendance> parkingList;

        public Context context;
        ArrayList<Attendance> arraylist;

        private MyAppAdapter(List<Attendance> apps, Context context) {
            this.parkingList = apps;
            this.context = context;
            arraylist = new ArrayList<Attendance>();
            arraylist.addAll(parkingList);
        }

        @Override
        public int getCount() {
            return parkingList.size();
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent)
        {
            View rowView = convertView;
            AttendanceListActivity.MyAppAdapter.ViewHolder viewHolder = null;
            if (rowView == null) {
                LayoutInflater inflater = getLayoutInflater();
                rowView = inflater.inflate(R.layout.list_content, parent, false);
                viewHolder = new AttendanceListActivity.MyAppAdapter.ViewHolder();
                viewHolder.textName = (TextView) rowView.findViewById(R.id.textName);
                rowView.setTag(viewHolder);
            } else {
                viewHolder = (AttendanceListActivity.MyAppAdapter.ViewHolder) convertView.getTag();
            }
            viewHolder.textName.setText(parkingList.get(position).getName() + "");
            if(itemArrayList.get(position).getPresent().equals("true" + ""))
                rowView.setBackgroundColor(Color.parseColor("#00ff00"));
            else
                rowView.setBackgroundColor(Color.parseColor("#ff0000"));
            return rowView;
        }
    }

    private void startAdvertising() {
        connectionsClient.startAdvertising(
                "Teacher Device", getPackageName(), connectionLifecycleCallback,
                new AdvertisingOptions.Builder().setStrategy(Strategy.P2P_STAR).build());
    }

    private final PayloadCallback payloadCallback =
            new PayloadCallback() {
                @Override
                public void onPayloadReceived(String endpointId, Payload payload) {
                }

                @Override
                public void onPayloadTransferUpdate(String endpointId, PayloadTransferUpdate update) {
                }
            };

    private final ConnectionLifecycleCallback connectionLifecycleCallback =
            new ConnectionLifecycleCallback() {
                @Override
                public void onConnectionInitiated(String endpointId, ConnectionInfo connectionInfo) {
                    Log.i(TAG, "onConnectionInitiated: accepting connection");
                    connectionsClient.acceptConnection(endpointId, payloadCallback);

                }

                @Override
                public void onConnectionResult(String endpointId, ConnectionResolution result) {
                    switch (result.getStatus().getStatusCode()) {
                        case ConnectionsStatusCodes.STATUS_OK:
                            break;
                        case ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED:
                            break;
                        case ConnectionsStatusCodes.STATUS_ERROR:
                            break;
                        default:
                    }
                }

                @Override
                public void onDisconnected(String endpointId) {
                    // We've been disconnected from this endpoint. No more data can be
                    // sent or received.
                }
            };

    private static boolean hasPermissions(Context context, String... permissions) {
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(context, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    /** Handles user acceptance (or denial) of our permission request. */
    @CallSuper
    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode != REQUEST_CODE_REQUIRED_PERMISSIONS) {
            return;
        }

        for (int grantResult : grantResults) {
            if (grantResult == PackageManager.PERMISSION_DENIED) {
                toastMsg("Missing Permissions");
                finish();
                return;
            }
        }
        recreate();
    }

    public void toastMsg(String msg) {
        Toast toast = Toast.makeText(this, msg, Toast.LENGTH_LONG);
        toast.show();
    }

    TimerTask task = new TimerTask() {
        public void run() {
            connectionsClient.stopAdvertising();
            connectionsClient.stopAllEndpoints();

            Thread thread = new Thread(){
                public void run(){
                    runOnUiThread(() -> {
                        toastMsg("No Longer Taking Attendance");
                    });
                }
            };
            thread.start();
        }
    };
}
