package com.example.mmi;

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
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class TeacherActivity extends AppCompatActivity
{
    private boolean success = false;
    private ListView listView;
    private ArrayList<Classes> itemArrayList;
    private MyAppAdapter myAppAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher);

        listView = findViewById(R.id.listView);
        itemArrayList = new ArrayList<Classes>();

        Intent intent = getIntent();

        String userId = intent.getStringExtra("userid");

        SyncData orderData = new SyncData();
        orderData.execute(userId);
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
                    String userId = params[0];
                    Statement st = con.createStatement();
                    ResultSet rs = st.executeQuery("SELECT * FROM mmi_classinfo where USERID='"+ userId + "'");
                    if (rs != null)
                    {
                        while (rs.next())
                        {
                            try {

                                LocalDate today = LocalDate.now();
                                LocalDate start = LocalDate.parse(rs.getString("STARTDATE"));
                                LocalDate end = LocalDate.parse(rs.getString("ENDDATE"));

                                if(today.isAfter(start) && today.isBefore(end))
                                    itemArrayList.add(new Classes(rs.getString("CLASSNAME"),rs.getString("CLASSID")));
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
                    myAppAdapter = new MyAppAdapter(itemArrayList, TeacherActivity.this);
                    listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
                    listView.setAdapter(myAppAdapter);
                    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> adapter, View view, int position, long id)
                        {
                            Intent classListIntent = new Intent(TeacherActivity.this, ClassListActivity.class);
                            classListIntent.putExtra("name",itemArrayList.get(position).getName());
                            classListIntent.putExtra("classID",itemArrayList.get(position).getClassID());
                            startActivity(classListIntent);
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

        public List<Classes> parkingList;

        public Context context;
        ArrayList<Classes> arraylist;

        private MyAppAdapter(List<Classes> apps, Context context) {
            this.parkingList = apps;
            this.context = context;
            arraylist = new ArrayList<Classes>();
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
            ViewHolder viewHolder = null;
            if (rowView == null) {
                LayoutInflater inflater = getLayoutInflater();
                rowView = inflater.inflate(R.layout.list_content, parent, false);
                viewHolder = new ViewHolder();
                viewHolder.textName = (TextView) rowView.findViewById(R.id.textName);
                rowView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            viewHolder.textName.setText(parkingList.get(position).getName() + "");
            return rowView;
        }
    }
}

