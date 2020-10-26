package com.example.mmi.Teacher;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import com.example.mmi.DBUtility;
import com.example.mmi.R;
import java.sql.Connection;
import java.sql.Statement;
import java.util.Timer;

public class AttendanceDetailActivity extends AppCompatActivity {

    private String classDate;
    private String classID;
    private String id;
    private String name;
    private String late;
    private String note;
    private String present;
    private RadioGroup radioGroup;
    private RadioButton radioButton;
    private RadioGroup radioGroup2;
    private RadioButton radioButton2;
    private RadioButton true1;
    private RadioButton true2;
    private RadioButton false1;
    private RadioButton false2;
    private EditText newNotes;
    private Boolean presentLate = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance_detail);
        Intent intent = getIntent();

        radioGroup = findViewById(R.id.radioGroup);
        radioGroup2 = findViewById(R.id.radioGroup2);
        true1 = findViewById(R.id.radio_true);
        true2 = findViewById(R.id.radio_true2);
        false1 = findViewById(R.id.radio_false);
        false2 = findViewById(R.id.radio_false2);
        classDate = intent.getStringExtra("cDate");
        classID = intent.getStringExtra("cID");
        id = intent.getStringExtra("id");
        name = intent.getStringExtra("name");
        late = intent.getStringExtra("late");
        note = intent.getStringExtra("note");
        present = intent.getStringExtra("present");
        newNotes = (EditText) findViewById(R.id.comments);
        newNotes.setText(note);
        OnTaskCompleteListener listener = message -> Toast.makeText(AttendanceDetailActivity.this, message, Toast.LENGTH_SHORT).show();

        if(present.equals("true"))
            true1.setChecked(true);
        else
            false1.setChecked(true);

        if(late.equals("true"))
            true2.setChecked(true);
        else
            false2.setChecked(true);

        Button buttonSubmit = findViewById(R.id.button_submit);
        buttonSubmit.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                int radioID = radioGroup.getCheckedRadioButtonId();
                radioButton = findViewById(radioID);
                String newPresent = (String)radioButton.getText();

                int radioID2 = radioGroup2.getCheckedRadioButtonId();
                radioButton2 = findViewById(radioID2);
                String newLate = (String)radioButton2.getText();

                String updatedNotes = newNotes.getText().toString().trim();

                SyncData orderData = new SyncData(listener);
                orderData.execute(classDate, newPresent, newLate, updatedNotes);
            }
        });
    }

    private class SyncData extends AsyncTask<String, Void, String[]>
    {
        private OnTaskCompleteListener listener;

        public SyncData(AttendanceDetailActivity.OnTaskCompleteListener listener) {
            this.listener = listener;
        }
        @Override
        protected String[] doInBackground(String... params)
        {
            try {
                Connection con = DBUtility.connect();
                if (!(con == null))
                {
                    String classDate = params[0];
                    String newPresent = params[1];
                    String newLate = params[2];
                    String updatedNotes = params[3];
                    Statement st = con.createStatement();
                    try{

                        if(!(newPresent.equals("False")&&newLate.equals("True")))
                        {
                            st.executeUpdate("UPDATE mmi_classmeetings set ATTENDANCELIST= JSON_SET(ATTENDANCELIST, '$."+'"'+id+'"'+".present', "+newPresent+") where CLASSDATE='"+classDate+"'");
                            st.executeUpdate("UPDATE mmi_classmeetings set ATTENDANCELIST= JSON_SET(ATTENDANCELIST, '$."+'"'+id+'"'+".late', "+newLate+") where CLASSDATE='"+classDate+"'");
                            st.executeUpdate("UPDATE mmi_classmeetings set ATTENDANCELIST= JSON_SET(ATTENDANCELIST, '$."+'"'+id+'"'+".note', "+'"'+updatedNotes+'"'+") where CLASSDATE='"+classDate+"'");
                        }
                        else {
                            presentLate = false;
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
            if(presentLate) {
                setResult(RESULT_OK, null);
                finish();
            }
            else{
                listener.onTaskComplete("Late cannot be True if Present is False");
                setResult(RESULT_OK, null);
                finish();
                overridePendingTransition(0, 0);
                startActivity(getIntent());
                overridePendingTransition(0, 0);
            }
        }
    }
    interface OnTaskCompleteListener {
        void onTaskComplete(String message);
    }
}
