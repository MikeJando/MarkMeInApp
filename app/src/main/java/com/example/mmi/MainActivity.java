package com.example.mmi;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mmi.Student.StudentClassListActivity;
import com.example.mmi.Teacher.ClassListActivity;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class MainActivity extends AppCompatActivity {

    EditText mTextUsername;
    EditText mTextPassword;
    Button mButtonLogin;
    TextView mTextViewForgot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextUsername = (EditText) findViewById(R.id.edittext_username);
        mTextPassword = (EditText) findViewById(R.id.edittext_password);
        mButtonLogin = (Button) findViewById(R.id.button_login);
        mTextViewForgot = (TextView) findViewById(R.id.textview_forgot);
        mTextViewForgot.setOnClickListener(view -> {
            Intent forgotIntent = new Intent (MainActivity.this,ForgotActivity.class);
            startActivity(forgotIntent);
        });

        OnTaskCompleteListener listener = message -> Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();

        mButtonLogin.setOnClickListener(view -> {

                String user = mTextUsername.getText().toString().trim();
                String pwd = mTextPassword.getText().toString().trim();

                new MyTask(listener).execute(user, pwd);

                });
                }

private class MyTask extends AsyncTask<String, Void, String[]>
{
    private OnTaskCompleteListener listener;

    public MyTask(OnTaskCompleteListener listener) {
        this.listener = listener;
    }

    Boolean statement = false;

    String[] data = new String[3];
    @Override
    protected String[] doInBackground(String... params)
    {
        if(android.os.Debug.isDebuggerConnected())
            android.os.Debug.waitForDebugger();
        try
        {
            String user = params[0];
            String pass = params[1];
            Connection con = DBUtility.connect();
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM mmi_userinfo where USERNAME='"+ user + "' and USER_PASSWORD='" + pass + "'");

            if (rs.next())
            {
                statement = true;
                data[0] = rs.getString("DESIGNATION");
                data[1] = rs.getString("USERID");
                data[2] = rs.getString("CLASSDATA");
            }
        }catch(Exception e)
        {
            e.printStackTrace();
        }

        return data;
    }

    @Override
    protected void onPostExecute(String[] userinfo)
    {
        if(userinfo[0] != null) {
            if (userinfo[0].equals("Professor")) {
                Intent teacherIntent = new Intent(MainActivity.this, ClassListActivity.class);
                teacherIntent.putExtra("userid",userinfo[1]);
                startActivity(teacherIntent);
            }
            else if (userinfo[0].equals("Student")) {
                Intent studentIntent = new Intent(MainActivity.this, StudentClassListActivity.class);
                studentIntent.putExtra("userid",userinfo[1]);
                studentIntent.putExtra("classList",userinfo[2]);
                startActivity(studentIntent);
            }
            else
            {
                listener.onTaskComplete("Login Error");
            }
        }
        else
        {
            listener.onTaskComplete("Login Error");
        }
    }
}

interface OnTaskCompleteListener {
    void onTaskComplete(String message);
}

}