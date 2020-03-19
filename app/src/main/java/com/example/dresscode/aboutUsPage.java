package com.example.dresscode;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class aboutUsPage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_us_page);
        getSupportActionBar().hide();

        TextView textAboutUs = findViewById(R.id.aboutUsText);
        textAboutUs.setText(constants.aboutUsText);

        TextView namesTxt = findViewById(R.id.namesTxt);
        namesTxt.setText(constants.ourNames);
    }
}
