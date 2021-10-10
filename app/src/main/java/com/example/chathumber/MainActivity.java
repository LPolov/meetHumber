package com.example.chathumber;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseListAdapter;
import com.firebase.ui.database.FirebaseListOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import android.text.format.DateFormat;

public class MainActivity extends AppCompatActivity {

    private static int SING_IN_CODE = 1;
    private RelativeLayout activity_main;
    private FirebaseListAdapter<Message> adapter;
    private FloatingActionButton sendBtn;
    private ListView listOfMessages;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SING_IN_CODE) {
            if (resultCode == RESULT_OK) {
                Snackbar.make(activity_main, "You are authorized", Snackbar.LENGTH_LONG).show();
                displayAllMessages();
            } else {
                Snackbar.make(activity_main, "You are not authorized", Snackbar.LENGTH_LONG).show();
                finish();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        activity_main = findViewById(R.id.activity_main);
        listOfMessages = findViewById(R.id.list_of_messages);
        sendBtn = findViewById(R.id.btnSend);
        sendBtn.setOnClickListener(view -> {
            EditText textField = findViewById(R.id.messageField);
            if(textField.getText().toString() == "")
                return;

            FirebaseDatabase.getInstance().getReference().push()
                    .setValue(new Message
                            (FirebaseAuth.getInstance().getCurrentUser().getEmail(),
                                    textField.getText().toString()
                            )
                    );
            displayAllMessages();
            textField.setText("");
        });

//user does nor auth authorized
        if (FirebaseAuth.getInstance().getCurrentUser() == null)
            startActivityForResult(AuthUI.getInstance().createSignInIntentBuilder().build(), SING_IN_CODE);
        else {
            Snackbar.make(activity_main, "You are authorized", Snackbar.LENGTH_LONG).show();
            displayAllMessages();
        }
    }

    private void displayAllMessages() {

        Query query = FirebaseDatabase.getInstance().getReference();
        FirebaseListOptions<Message> options = new FirebaseListOptions.Builder<Message>()
                .setLayout(R.layout.list_item)
                .setQuery(query, Message.class)
                .setLifecycleOwner(this)   //Added this
                .build();
        // Get references to the views of message.xml
         adapter = new FirebaseListAdapter<Message>(options) {
            @Override
            protected void populateView(View v, Message model, int position) {
                // Get references to the views of message.xml
                TextView mess_text, mess_user, mess_time;
                mess_user = (TextView) v.findViewById(R.id.message_user);
                mess_text = (TextView) v.findViewById(R.id.message_text);
                mess_time = (TextView) v.findViewById(R.id.message_time);
                mess_user.setText(model.getUserName());
                mess_time.setText(DateFormat.format("dd-mm-yyy HH:mm:ss", model.getMessageTime()));
                mess_text.setText(model.getTextMessage());
            }
        };
        listOfMessages.setAdapter(adapter);
    }
}