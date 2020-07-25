package com.example.mmi;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class TeacherActivity extends AppCompatActivity
{
    private boolean success = false;
    private ListView listView;
    private ArrayList<ClassListItems> itemArrayList;
    private MyAppAdapter myAppAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher);

        listView = (ListView) findViewById(R.id.listView);
        itemArrayList = new ArrayList<ClassListItems>();

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
            String userId = params[0];
            try {
                Connection con = DBUtility.connect();
                if (con == null) {
                    success = false;
                } else {
                    Statement st = con.createStatement();
                    ResultSet rs = st.executeQuery("SELECT * FROM mmi_classinfo where USERID='"+ userId + "'");
                    if (rs != null)
                    {
                        while (rs.next())
                        {
                            try {
                                itemArrayList.add(new ClassListItems(rs.getString("CLASSNAME")));
                            } catch (Exception ex)
                            {
                                ex.printStackTrace();
                            }
                        }
                        success = true;
                    } else {
                        success = false;
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
            if (!success) {
            } else {
                try {
                    myAppAdapter = new MyAppAdapter(itemArrayList, TeacherActivity.this);
                    listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
                    listView.setAdapter(myAppAdapter);
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

        public List<ClassListItems> parkingList;

        public Context context;
        ArrayList<ClassListItems> arraylist;

        private MyAppAdapter(List<ClassListItems> apps, Context context) {
            this.parkingList = apps;
            this.context = context;
            arraylist = new ArrayList<ClassListItems>();
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

