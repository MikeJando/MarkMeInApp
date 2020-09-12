package com.example.mmi.Teacher;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import com.example.mmi.DBUtility;
import com.example.mmi.R;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class MeetingListActivity extends AppCompatActivity
{
    private boolean success = false;
    private ListView listView;
    private ArrayList<Meetings> itemArrayList;
    private MeetingListActivity.MyAppAdapter myAppAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_class_list);


        listView = findViewById(R.id.listView);
        itemArrayList = new ArrayList<Meetings>();
        TextView tv;
        tv = findViewById(R.id.Textvv);

        Intent intent = getIntent();

        String name = intent.getStringExtra("name");
        String classID = intent.getStringExtra("classID");
        tv.setText(name);

        SyncData orderData = new SyncData();
        orderData.execute(classID);
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
                    String classID = params[0];
                    Statement st = con.createStatement();
                    ResultSet rs = st.executeQuery("SELECT * FROM mmi_classmeetings where CLASSID='"+ classID + "'");
                    if (rs != null)
                    {
                        while (rs.next())
                        {
                            try {
                                itemArrayList.add(new Meetings(rs.getString("CLASSDATE"), rs.getString("CLASSID")));
                            } catch (Exception ex)
                            {
                                ex.printStackTrace();
                            }
                        }
                        success = true;
                    }
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
            if(!success)
            {
            } else {
                try {
                    myAppAdapter = new MeetingListActivity.MyAppAdapter(itemArrayList, MeetingListActivity.this);
                    listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
                    listView.setAdapter(myAppAdapter);
                    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> adapter, View view, int position, long id)
                        {
                            Intent attendanceListIntent = new Intent(MeetingListActivity.this, AttendanceListActivity.class);
                            attendanceListIntent.putExtra("classDate",itemArrayList.get(position).getClassDate());
                            attendanceListIntent.putExtra("classID",itemArrayList.get(position).getClassID());
                            startActivity(attendanceListIntent);
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

        public List<Meetings> parkingList;

        public Context context;
        ArrayList<Meetings> arraylist;

        private MyAppAdapter(List<Meetings> apps, Context context) {
            this.parkingList = apps;
            this.context = context;
            arraylist = new ArrayList<Meetings>();
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
            MeetingListActivity.MyAppAdapter.ViewHolder viewHolder = null;
            if (rowView == null) {
                LayoutInflater inflater = getLayoutInflater();
                rowView = inflater.inflate(R.layout.list_content, parent, false);
                viewHolder = new MeetingListActivity.MyAppAdapter.ViewHolder();
                viewHolder.textName = (TextView) rowView.findViewById(R.id.textName);
                rowView.setTag(viewHolder);
            } else {
                viewHolder = (MeetingListActivity.MyAppAdapter.ViewHolder) convertView.getTag();
            }
            viewHolder.textName.setText(parkingList.get(position).getClassDate() + "");
            return rowView;
        }
    }
}