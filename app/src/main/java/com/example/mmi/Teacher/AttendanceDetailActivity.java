package com.example.mmi.Teacher;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import com.example.mmi.DBUtility;
import com.example.mmi.R;
import org.json.JSONObject;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Iterator;

public class AttendanceDetailActivity extends AppCompatActivity {

    private HashMap<String, HashMap<String, String>> attendanceMap = new HashMap<>();
    private HashMap<String,String> studentMap = new HashMap<>();
    private String classDate;
    private String classID;
    private String id;
    private String name;
    private String late;
    private String note;
    private String present;
    RadioGroup radioGroup;
    RadioButton radioButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance_detail);
        Intent intent = getIntent();

        radioGroup = findViewById(R.id.radioGroup);

        classDate = intent.getStringExtra("cDate");
        classID = intent.getStringExtra("cID");
        id = intent.getStringExtra("id");
        name = intent.getStringExtra("name");
        late = intent.getStringExtra("late");
        note = intent.getStringExtra("note");
        present = intent.getStringExtra("present");

        Button buttonSubmit = findViewById(R.id.button_submit);
        buttonSubmit.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                int radioID = radioGroup.getCheckedRadioButtonId();
                radioButton = findViewById(radioID);
                String newPresent = (String)radioButton.getText();
                SyncData orderData = new SyncData();
                orderData.execute(classDate, newPresent);
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
                    String newPresent = params[1];
                    String attendanceList;
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
                        attendanceMap.get(id).replace("present",newPresent);
                        JSONObject jsonMap = new JSONObject(attendanceMap);
                        st.executeUpdate("UPDATE mmi_classmeetings" + " set ATTENDANCELIST='" + jsonMap + "' where CLASSDATE='" + classDate + "'");

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
