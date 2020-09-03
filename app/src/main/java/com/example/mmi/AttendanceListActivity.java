package com.example.mmi;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;


public class AttendanceListActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance_list);


        Intent intent = getIntent();
        String classDate = intent.getStringExtra("classDate");

        AttendanceListActivity.SyncData orderData = new AttendanceListActivity.SyncData();
        orderData.execute(classDate);
    }

    private class SyncData extends AsyncTask<String, Void, String>
    {
        @Override
        protected String doInBackground(String... params)
        {
            try {
                Connection con = DBUtility.connect();
                if (!(con == null))
                {
                    String classDate = params[0];
                    Statement st = con.createStatement();
                    ResultSet rs = st.executeQuery("SELECT * FROM mmi_classmeetings where CLASSDATE='"+ classDate + "'");

                }
            } catch (Exception e)
            {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String msg)
        {
        }
    }

}

