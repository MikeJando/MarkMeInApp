package com.example.mmi.Student;

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
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.example.mmi.DBUtility;
import com.example.mmi.R;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.ConnectionInfo;
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback;
import com.google.android.gms.nearby.connection.ConnectionResolution;
import com.google.android.gms.nearby.connection.ConnectionsClient;
import com.google.android.gms.nearby.connection.ConnectionsStatusCodes;
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo;
import com.google.android.gms.nearby.connection.DiscoveryOptions;
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback;
import com.google.android.gms.nearby.connection.Payload;
import com.google.android.gms.nearby.connection.PayloadCallback;
import com.google.android.gms.nearby.connection.PayloadTransferUpdate;
import com.google.android.gms.nearby.connection.Strategy;
import org.json.JSONObject;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Iterator;

public class StudentAttendanceDetailActivity extends AppCompatActivity {

    private HashMap<String, HashMap<String, String>> attendanceMap = new HashMap<>();
    private String cDate;
    private String userId;
    private String present;
    private String late;
    TextView presentText;
    TextView lateText;
    private ConnectionsClient connectionsClient;
    private static final String TAG = "MMI";
    private String connected = "false";

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
        setContentView(R.layout.activity_student_attendance_detail);
        Intent intent = getIntent();
        presentText = (TextView)findViewById(R.id.presentText);
        lateText = (TextView)findViewById(R.id.lateText);

        cDate = intent.getStringExtra("classDate");
        userId = intent.getStringExtra("userId");

        connectionsClient = Nearby.getConnectionsClient(this);
        SyncData orderData = new SyncData();

        Button checkIn = findViewById(R.id.button_checkIn);
        checkIn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {

                if (!hasPermissions(StudentAttendanceDetailActivity.this, REQUIRED_PERMISSIONS)) {
                    requestPermissions(REQUIRED_PERMISSIONS, REQUEST_CODE_REQUIRED_PERMISSIONS);
                }
                startDiscovery();
                orderData.execute(cDate, connected);
            }
        });

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
                    String connect = params[1];
                    String attendanceList;
                    Statement st = con.createStatement();
                    ResultSet rs = st.executeQuery("SELECT ATTENDANCELIST FROM mmi_classmeetings where CLASSDATE='" + classDate + "'");
                    try{
                        while(rs.next()) {
                            attendanceList = rs.getString("ATTENDANCELIST");
                            JSONObject attendance = new JSONObject(attendanceList);
                            HashMap<String, String> values = new HashMap<>();
                            JSONObject inner = new JSONObject(attendance.getString(userId));
                            Iterator<?>keys2 = inner.keys();
                            while(keys2.hasNext())
                            {
                                String key2 = (String)keys2.next();
                                String value = inner.getString(key2);
                                values.put(key2,value);
                            }
                            attendanceMap.put(userId, values);
                        }

                        if(connect.equals("True") && attendanceMap.get(userId).get("present").equals("False")) {
                            attendanceMap.get(userId).replace("present", "True");
                            String jsonMap = "JSON_REPLACE(ATTENDANCELIST, '$."+userId+"', True" ;
                            st.executeUpdate("UPDATE mmi_classmeetings" + " set ATTENDANCELIST='" + jsonMap + "' where CLASSDATE='" + classDate + "'");
                        }

                        present = attendanceMap.get(userId).get("present");
                        late = attendanceMap.get(userId).get("late");
                        if(present.equals("True"))
                        {
                            presentText.setText("Yes");
                            presentText.setTextColor(Color.parseColor("#00ff00"));
                        }
                        else
                        {
                            presentText.setText("No");
                            presentText.setTextColor(Color.parseColor("#ff0000"));
                        }
                        if(late.equals("True"))
                        {
                            lateText.setText("Yes");
                            lateText.setTextColor(Color.parseColor("#00ff00"));
                        }
                        else
                        {
                            lateText.setText("No");
                            lateText.setTextColor(Color.parseColor("#ff0000"));
                        }
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

        }
    }

    private void startDiscovery() {
        connectionsClient.startDiscovery(
                getPackageName(), endpointDiscoveryCallback,
                new DiscoveryOptions.Builder().setStrategy(Strategy.P2P_STAR).build());
    }

    private final EndpointDiscoveryCallback endpointDiscoveryCallback =
            new EndpointDiscoveryCallback() {
                @Override
                public void onEndpointFound(String endpointId, DiscoveredEndpointInfo info) {
                    Log.i(TAG, "onEndpointFound: endpoint found, connecting");
                    connectionsClient.requestConnection("Student Device", endpointId, connectionLifecycleCallback);
                }

                @Override
                public void onEndpointLost(String endpointId) {}
            };

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
                    connectionsClient.stopDiscovery();
                    connected = "true";
                }

                @Override
                public void onConnectionResult(String endpointId, ConnectionResolution result) {
                    switch (result.getStatus().getStatusCode()) {
                        case ConnectionsStatusCodes.STATUS_OK:
                            toastMsg("We're connected!");
                            break;
                        case ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED:
                            toastMsg("The connection was rejected by one or both sides.");
                            break;
                        case ConnectionsStatusCodes.STATUS_ERROR:
                            toastMsg("The connection broke before it was able to be accepted.");
                            break;
                        default:
                    }
                }

                @Override
                public void onDisconnected(String endpointId) {

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

    // Handles user acceptance (or denial) of the permission request.
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
                Toast.makeText(this, "Missing Permissions", Toast.LENGTH_LONG).show();
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

}