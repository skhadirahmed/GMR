package com.example.skhadirahmed.gmr;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;

import java.text.DecimalFormat;

public class GPA extends AppCompatActivity {

    private EditText mEditText;
    private Button mButton;
    private TextView mTextView;
    private Button mCrashButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gpa);
        mEditText = findViewById(R.id.grades);
        mButton = findViewById(R.id.button);
        mTextView = findViewById(R.id.result);
        mCrashButton = findViewById(R.id.button2);

        mCrashButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Force a crash
                Crashlytics.getInstance().crash();
            }
        });

        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String g = mEditText.getText().toString().toUpperCase();
                String[] gr = new String[g.length()];
                for (int i = 0; i < g.length(); i++)
                    gr[i] = "" + g.charAt(i);
                float cgpa = 0;

                for (int i = 0; i < gr.length; i++)
                    switch (gr[i]) {
                        case "S":
                            cgpa += 10;
                            break;
                        case "A":
                            cgpa += 9;
                            break;
                        case "B":
                            cgpa += 8;
                            break;
                        case "C":
                            cgpa += 7;
                            break;
                        case "D":
                            cgpa += 6;
                            break;
                        case "E":
                            cgpa += 5;
                            break;
                        case "F":
                            cgpa += 0;
                            break;
                        case "W":
                            cgpa += 0;
                            break;
                        default:
                            cgpa += 0;
                    }
                DecimalFormat decimalFormat = new DecimalFormat("#.##");
                float gpa = cgpa / gr.length;
                Log.v("gpa", "gpa is " + gpa);
                String r = "GPA Is " + gpa;
                mTextView.setText(r);
            }
        });
    }
}
