package com.example.mmi.Student;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.TextView;
import com.example.mmi.DBUtility;
import com.example.mmi.R;
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

        SyncData orderData = new SyncData();
        orderData.execute(cDate);
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
}