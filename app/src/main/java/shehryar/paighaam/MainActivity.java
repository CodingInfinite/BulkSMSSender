/* Copyright (C) 2016 CodingInfinite Technologies - All Rights Reserved
 * NOTICE:  All information contained herein is, and remains
 * the property of CodingInfinite Technologies and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to CodingInfinite Technologies
 * and its suppliers and may be covered by Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from CodingInfinite Technologies.
 */


package shehryar.paighaam;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.annotation.NonNull;
import android.telephony.SmsManager;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    ImageButton sendSMSBtn;
    EditText smsMessageET;
    ListView nmbrsList;
    int count = 0, i = 0;
    PendingIntent sentPI;

    public static int limit, pause, deleteNum;

    ProgressDialog progressBar;
    ArrayList<String> nmbers = new ArrayList<>();

    SharedPreferences sharedpreferences;

    String filePath = "";
    PowerManager.WakeLock wl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        sharedpreferences = getSharedPreferences("smsppppt", Context.MODE_PRIVATE);
        if (sharedpreferences.getAll().isEmpty()) {
            SharedPreferences.Editor editor = sharedpreferences.edit();
            editor.putString("limit", "900");
            editor.putString("pause", "3");
            editor.putString("deleteNum", "1");
            editor.putString("key", "0");
            editor.apply();

            limit = 900;
            pause = 3;
            deleteNum = 1;
        } else {
            limit = Integer.parseInt(sharedpreferences.getString("limit", ""));
            pause = Integer.parseInt(sharedpreferences.getString("pause", ""));
            deleteNum = Integer.parseInt(sharedpreferences.getString("deleteNum", ""));
        }
        nmbrsList = findViewById(R.id.listView1);
        Button dirChooserButton1 = findViewById(R.id.button1);
        dirChooserButton1.setOnClickListener(new View.OnClickListener() {
            String m_chosen;

            @Override
            public void onClick(View v) {
                SimpleFileDialog FileOpenDialog = new SimpleFileDialog(MainActivity.this, "FileOpen",
                        new SimpleFileDialog.SimpleFileDialogListener() {
                            @Override
                            public void onChosenDir(String chosenDir) {
                                m_chosen = chosenDir;
                                filePath = m_chosen;
                                Toast.makeText(MainActivity.this, "Chosen File: " + m_chosen, Toast.LENGTH_SHORT).show();
                                BufferedReader br;
                                try {
                                    br = new BufferedReader(new FileReader(m_chosen));
                                    String line;
                                    while ((line = br.readLine()) != null) {
                                        nmbers.add(line);
                                    }
                                    String[] namesArr = nmbers.toArray(new String[nmbers.size()]);
                                    ArrayAdapter<String> adaptr = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_list_item_1, namesArr);
                                    nmbrsList.setAdapter(adaptr);
                                } catch (IOException io) {
                                    io.printStackTrace();
                                }

                            }
                        });
                FileOpenDialog.Default_File_Name = "";
                FileOpenDialog.chooseFile_or_Dir();
                PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
                wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "My Tag");
                wl.acquire(10*60*1000L /*10 minutes*/);
                progressBar = new ProgressDialog(v.getContext());
                progressBar.setCancelable(false);
                progressBar.setMessage("SMS Sending...");
                progressBar.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                progressBar.setProgress(0);
            }
        });
        sendSMSBtn = findViewById(R.id.btnSendSMS);
        sendSMSBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                smsMessageET = findViewById(R.id.editText1);
                if (smsMessageET.getText().toString().isEmpty())
                    Toast.makeText(getBaseContext(), "Message can not be empty!", Toast.LENGTH_SHORT).show();
                else if (filePath.isEmpty()) {
                    Toast.makeText(getBaseContext(), "Please choose a file!", Toast.LENGTH_SHORT).show();
                } else
                    sendSMS();
            }
        });

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        if (id == R.id.send_sms) {
        } else if (id == R.id.settings) {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
        } else if (id == R.id.about) {
            Intent intent = new Intent(MainActivity.this, AboutActivity.class);
            startActivity(intent);
        } else if (id == R.id.exit_app) {
            finish();
        }
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    protected void sendSMS() {
        String SENT = "SENT_SMS_ACTION";
        sentPI = PendingIntent.getBroadcast(this, 0, new Intent(SENT), 0);
        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context arg0, Intent arg1) {
                count++;
                i++;
                switch (getResultCode()) {
                    case Activity.RESULT_OK:
                        if (deleteNum == 1) {
                            String nm = nmbers.get(i - 1);
                            nmbers.remove(nm);
                            i--;
                            PrintWriter writer;
                            try {
                                writer = new PrintWriter(filePath);
                                for (int t = 0; t < nmbers.size(); t++) {
                                    writer.println(nmbers.get(t));
                                }
                                writer.close();
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            }
                        }
                        break;
                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                        Toast.makeText(getBaseContext(), "Generic failure",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NO_SERVICE:
                        Toast.makeText(getBaseContext(), "No service",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NULL_PDU:
                        Toast.makeText(getBaseContext(), "Null PDU",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_RADIO_OFF:
                        Toast.makeText(getBaseContext(), "Radio off",
                                Toast.LENGTH_SHORT).show();
                        break;
                }
                if (i < nmbers.size()) {
                    progressBar.setProgress(count);
                    if (count % limit == 0 && count > 0) {
                        try {
                            Thread.sleep((pause * 1000));
                        } catch (InterruptedException e) {
                            //e.printStackTrace();
                        }
                    }
                    sendIt();
                } else {
                    progressBar.dismiss();
                    Toast.makeText(getBaseContext(), "All SMS Sent", Toast.LENGTH_SHORT).show();
                    wl.release();
                }
            }
        }, new IntentFilter(SENT));
        progressBar.setMax(nmbers.size());
        progressBar.show();
        sendIt();
    }

    void sendIt() {
        try {

            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(nmbers.get(i), null, smsMessageET.getText().toString(), sentPI, null);
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "failed to " + nmbers.get(i), Toast.LENGTH_LONG).show();
        }
    }
}
