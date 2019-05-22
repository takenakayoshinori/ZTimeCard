package com.example.uo.timecard;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class InputUserActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input_user);

        //初期値設定
        EditText textUser = findViewById(R.id.text_user);
        textUser.setText(getSaveString(KEY_USER_ID,""));

        // ボタンを設定
        // API Level 26 から総称型対応となりました
        Button buttonBack = findViewById(R.id.button_back);
        Button buttonSetup = findViewById(R.id.button_userSetup);
        buttonSetup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                EditText textUser = findViewById(R.id.text_user);
                EditText textPasswd = findViewById(R.id.text_passwd);

                setSaveString(KEY_USER_ID, textUser.getText().toString());
                setSaveString(KEY_PASSWORD, textPasswd.getText().toString(),true);
                setResult(MainActivity.RELOAD);
                finish();
            }
        });
        buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }
}
