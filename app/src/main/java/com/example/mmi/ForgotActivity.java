package com.example.mmi;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class ForgotActivity extends AppCompatActivity
{
    EditText mTextEmail;
    Button mButtongetPass;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot);

        mTextEmail = (EditText) findViewById(R.id.edittext_email);
        mButtongetPass = (Button) findViewById(R.id.button_getPass);


        mButtongetPass.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                String email = mTextEmail.getText().toString().trim();
            }
        });
    }
}