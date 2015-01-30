package com.example.alexlily.chatapp;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;


public class ContactPageActivity extends Activity {
    public static final String CONTACT = "contact";
    public static final String TYPE = "intent type";
    public static final String newConvo = "new conversation";
    public static final String MESSAGE = "message";

    String username;


    String TAG = "contact page activity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_page);

        Intent intent = getIntent();
        username = intent.getStringExtra(LoginActivity.USERNAME);

        // add in the contact buttons
        Button contactButton;

        // fake data
        String[] contactList = {getString(R.string.test_receiver), "frodo", "sam"};
        TextView title = (TextView) findViewById(R.id.contactTitle);
        title.setText(username);
        LinearLayout contactArea = (LinearLayout)findViewById(R.id.contactList);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(5,5,5,5);
        for (String person: contactList){
            contactButton = new Button(this);
            contactButton.setText(person);
            contactButton.setTextColor(Color.BLACK);
            contactButton.setTextSize(32);
            contactButton.setBackgroundResource(R.drawable.contact_page_entry);
            contactButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(ContactPageActivity.this, MainActivity.class);
                    intent.putExtra(CONTACT, ((Button)v).getText());
                    intent.putExtra(LoginActivity.USERNAME, username);
                    intent.putExtra(TYPE, newConvo);
                    startActivity(intent);
                }
            });
            contactArea.addView(contactButton, lp);
        }

    }


}
