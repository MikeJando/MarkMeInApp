package com.example.mmi.Teacher;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;

import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import com.example.mmi.DBUtility;
import com.example.mmi.R;
import org.json.JSONObject;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class AttendanceListActivity extends AppCompatActivity {

    private HashMap<String, HashMap<String, String>> attendanceMap = new HashMap<>();
    //private HashMap<String,String> studentMap = new HashMap<>();
    private boolean success = false;
    private ListView listView;
    private ArrayList<Attendance> itemArrayList;
    private AttendanceListActivity.MyAppAdapter myAppAdapter;
    private String cDate;
    private String cID;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance_list);

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
                                    //studentMap.put(key,value);
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
                            startActivity(attendanceDetailIntent);
                        }

                    });

                } catch (Exception ex)
                {
                }
            }
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
            if(itemArrayList.get(position).getPresent().equals("True"))
                rowView.setBackgroundColor(Color.parseColor("#00ff00"));
            else
                rowView.setBackgroundColor(Color.parseColor("#ff0000"));
            return rowView;
        }
    }
}
