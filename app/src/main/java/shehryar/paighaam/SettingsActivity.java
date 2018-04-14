/* Copyright (C) 2016 CodingInfinite Technologies - All Rights Reserved */

package shehryar.paighaam;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class SettingsActivity extends AppCompatActivity {

    EditText limitEditText, pauseEditText;
    CheckBox checkBox;
    Button saveButton;
    SharedPreferences sharedpreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pause_setting);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.setDisplayHomeAsUpEnabled(true);
        TextView textView = findViewById(R.id.textView2);
        textView.setClickable(true);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
        String text = "Powered by: " + "<a href='http://codinginfinite.com'>Coding Infinite</a>";
        textView.setText(Html.fromHtml(text));


        checkBox = findViewById(R.id.checkBox1);

        if (MainActivity.deleteNum == 1) {
            checkBox.setChecked(true);
        } else {
            checkBox.setChecked(false);
        }

        limitEditText = findViewById(R.id.editText);
        limitEditText.setText(String.valueOf(Integer.toString(MainActivity.pause)));

        pauseEditText = findViewById(R.id.editText2);
        pauseEditText.setText(String.valueOf(Integer.toString(MainActivity.limit)));

        saveButton = findViewById(R.id.button);
        saveButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                sharedpreferences = getSharedPreferences("smsppppt", Context.MODE_PRIVATE);

                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putString("limit", pauseEditText.getText().toString());
                editor.putString("pause", limitEditText.getText().toString());


                if (checkBox.isChecked()) {
                    MainActivity.deleteNum = 1;
                    editor.putString("deleteNum", "1");
                } else {
                    MainActivity.deleteNum = 0;
                    editor.putString("deleteNum", "0");
                }

                editor.apply();


                MainActivity.pause = Integer.parseInt(limitEditText.getText().toString());
                MainActivity.limit = Integer.parseInt(pauseEditText.getText().toString());

                Toast.makeText(SettingsActivity.this, "Saved!", Toast.LENGTH_SHORT).show();
                finish();
            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // app icon in action bar clicked; go home
                finish();
                return true;

        }

        return true;
    }
}
