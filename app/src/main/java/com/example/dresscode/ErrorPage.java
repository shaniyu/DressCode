package com.example.dresscode;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class ErrorPage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_error_page);
        getSupportActionBar().hide();

        TextView errorHeader= findViewById(R.id.errorHeader2);
        errorHeader.setText("Oops...");

        TextView txtForDBError = findViewById(R.id.txtForDBError2);
        txtForDBError.setText("An error occurred in our database.\n\nContact us if you want:\n\nyuvalvahaba@gmail.com\n\ninbal.valdman@gmail.com\n\nshaniyunker@gmail.com");
    }
}
