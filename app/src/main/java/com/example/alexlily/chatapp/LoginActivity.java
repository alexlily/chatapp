package com.example.alexlily.chatapp;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;


public class LoginActivity extends Activity {

    public String username, password, sitename;
    EditText loginEditText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
    }

    public void onClick(View view){
        // To do: check to make sure login credentials are legit.
        Intent intent = new Intent(this, ContactPageActivity.class);
        loginEditText = (EditText) findViewById(R.id.usernameEditText);
        username = loginEditText.getText().toString();
        loginEditText = (EditText) findViewById(R.id.passwordEditText);
        password = loginEditText.getText().toString();
        loginEditText = (EditText) findViewById(R.id.siteNameEditText);
        sitename = loginEditText.getText().toString();

        intent.putExtra(getString(R.string.username_label), username);
        intent.putExtra(getString(R.string.password_label), password);
        intent.putExtra(getString(R.string.site_label), sitename);

        startActivity(intent);
    }


}
