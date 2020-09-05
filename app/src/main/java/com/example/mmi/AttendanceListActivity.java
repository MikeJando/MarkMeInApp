package com.example.mmi;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import org.json.JSONObject;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;



public class AttendanceListActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance_list);


        Intent intent = getIntent();
        String cDate = intent.getStringExtra("classDate");
        String cID = intent.getStringExtra("classID");


        AttendanceListActivity.SyncData orderData = new AttendanceListActivity.SyncData();
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
                                System.out.println(attendance);
                            }
                            ResultSet rs2 = st.executeQuery("SELECT STUDENTLIST FROM mmi_classinfo where CLASSID='" + classID + "'");
                            while(rs2.next())
                            {
                                studentList = rs2.getString("STUDENTLIST");
                                JSONObject student = new JSONObject(studentList);
                                System.out.println(student);
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

