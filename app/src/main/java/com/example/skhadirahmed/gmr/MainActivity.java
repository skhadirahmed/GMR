package com.example.skhadirahmed.gmr;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    private static final int RC_SIGNIN = 904;
    private static final int RC_PERMISSION = 904;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private WebView mWebView;
    private EditText mEditText;
    private CheckBox mCheckBox;
    private TextView mSGPA;
    private FloatingActionButton mFloatingActionButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mWebView = findViewById(R.id.webView_results);
        Button mGetResults = findViewById(R.id.button_results);
        mEditText = findViewById(R.id.rollnumber);
        mCheckBox = findViewById(R.id.calculateCGPA);
        mSGPA = findViewById(R.id.textview_sgpa);
        mFloatingActionButton = findViewById(R.id.floatingActionButton);
        mFloatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, GPA.class);
                startActivity(intent);
            }
        });

        mFirebaseAuth = FirebaseAuth.getInstance();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.INTERNET}, RC_PERMISSION);
        } else {
            FirebaseLogin();
            mGetResults.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (hasConnectivity()) {
                        getResults();
                    } else {
                        Toast.makeText(MainActivity.this, "Please use WIFI or Mobile Data", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    private void getWebsite(final String rollno) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final StringBuilder builder = new StringBuilder();
                final StringBuilder gpa_builder = new StringBuilder();

                try {
                    String url = "http://rvrjcce.ac.in/examcell/results/regnoresultsR1.php?q=" + rollno;
                    Document doc = Jsoup.connect(url).get();
                    Elements rows = doc.select("tr");
                    Log.v("rows", "number of rows = " + rows.size());
                    if (rows.size() > 1) {
                        ArrayList<HashMap> list = new ArrayList<>();
                        int r = 1;
                        for (Element row : rows) {
                            r = 1;
                            HashMap<Integer, String> hm = new HashMap();
//                        builder.append(r++).append(" -> ");
                            Elements columns = row.select("td");
                            for (Element column : columns) {
                                builder.append(column.text()).append("%");
                                hm.put(r++, column.text());
                            }
                            builder.append("\n");
                            list.add(hm);
                        }

                        HashMap<Integer, String> gr = list.get(2);

                        for (String x : gr.values())
                            Log.v("hashmap", x);

                        float cgpa = 0;
                        int nos = 0;
                        for (int i = 4; i < 15; i++) {
                            Log.v("cgpa", i + "->" + gr.get(i));
                            if (gr.get(i).length() == 1)
                                nos++;
                            switch (gr.get(i)) {
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
                        }
                        Log.v("cgpa", "" + cgpa);
                        Log.v("cgpa nos", "" + nos);
                        Log.v("cgpa is ", "" + cgpa / nos);
                        float gpa1 = cgpa / nos;
                        DecimalFormat decimalFormat = new DecimalFormat("#.##");
                        gpa_builder.append(gr.get(1) + "th Semester SGPA " + decimalFormat.format(gpa1));

                        HashMap<Integer, String> gr1 = list.get(3);
                        String a = gr.get(1);
                        String b = gr1.get(1);
                        Log.v("gr", "a is " + a);
                        Log.v("gr1", "b is " + b);
                        if (a.equals(b)) {
                            gpa_builder.delete(0, gpa_builder.length());
                            gpa_builder.append("Enter updated grades to get gpa");
                        }
                    } else {
                        gpa_builder.append("This is not a Valid Number");
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
//                        Toast.makeText(MainActivity.this, builder.toString(), Toast.LENGTH_LONG).show();
                        mSGPA.setText(gpa_builder.toString());
                    }
                });
            }
        }).start();
    }

    private boolean hasConnectivity() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        if (activeNetworkInfo == null) {
            //Not Connected
//            Toast.makeText(this, "Please Connect", Toast.LENGTH_SHORT).show();
            return false;
        }
        //Connected
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case RC_PERMISSION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Please give Internet permission in the App Info", Toast.LENGTH_LONG).show();
                }
        }
    }

    private boolean validId(String rollno) {
        if (rollno.isEmpty() || rollno.length() > 8 || rollno.length() < 8)
            return false;
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mAuthStateListener != null) {
            mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGNIN) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, "You have been successfully logged in", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void FirebaseLogin() {
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                if (firebaseUser != null) {
                    //user is already signed in
//                    Toast.makeText(MainActivity.this, "You are already signed in.", Toast.LENGTH_SHORT).show();
                } else {
                    //user is not signed in so give the option to sign in
                    Toast.makeText(MainActivity.this, "Please sign in", Toast.LENGTH_SHORT).show();
                    startActivityForResult(
                            AuthUI.getInstance()
                                    .createSignInIntentBuilder()
                                    .setIsSmartLockEnabled(false)
                                    .setAvailableProviders(Arrays.asList(
//                                            new AuthUI.IdpConfig.EmailBuilder().build(),
                                            new AuthUI.IdpConfig.GoogleBuilder().build())
                                    ).build(), RC_SIGNIN);
                }
            }
        };
    }

    private void signout(View view) {
        mFirebaseAuth.signOut();
        Toast.makeText(this, "You are now signed out :(", Toast.LENGTH_SHORT).show();
    }

    private void getResults() {
        String rollno = mEditText.getText().toString();
        if (validId(rollno)) {
            if (mCheckBox.isChecked()) {
                getWebsite(rollno);
            }
            mWebView.setWebViewClient(new MyBrowser());
            mWebView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
            mWebView.getSettings().setBuiltInZoomControls(true);
            mWebView.getSettings().setDisplayZoomControls(false);
            String url = "http://rvrjcce.ac.in/examcell/results/regnoresultsR1.php?q=" + rollno;
            mWebView.loadUrl(url);
        } else {
            Toast.makeText(MainActivity.this, "Enter a valid ID", Toast.LENGTH_SHORT).show();
        }
    }

    public class MyBrowser extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }
    }
}
