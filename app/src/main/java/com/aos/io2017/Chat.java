package com.aos.io2017;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import com.aos.io2017.entities.Message;
import com.aos.io2017.entities.User;
import com.aos.io2017.fcm.Messaging;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.RemoteMessage;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Chat extends AppCompatActivity {

    private DatabaseReference databaseReference;
    private DatabaseReference databaseReference2;
    private FirebaseUser user;
    private User destiny;

    private List<Message> messages = new ArrayList<Message>();

    private ListView list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        user = FirebaseAuth.getInstance().getCurrentUser();
        destiny = (User) getIntent().getExtras().getSerializable("user");
        getSupportActionBar().setTitle(destiny.getName());


        //start firebase database connection
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        databaseReference = database.getReference("chats/" + user.getUid() + "/messages/" + destiny.getId());
        databaseReference2 = database.getReference("chats/" + destiny.getId() + "/messages/" + user.getUid());

        if (destiny.getId().equals("0")) {
            databaseReference = database.getReference("chats/0/messages/0");
        }

        ChildEventListener listener = new ChildEventListener() {

            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String lastKey) {
                Message message = dataSnapshot.getValue(Message.class);
                messages.add(message);
                ((MessageAdapter) list.getAdapter()).notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        databaseReference.addChildEventListener(listener);


        MessageAdapter adapter = new MessageAdapter(Chat.this, messages);
        list = (ListView) findViewById(R.id.list);
        list.setAdapter(adapter);

        final EditText txtMessage = (EditText) findViewById(R.id.message);

        ((Button) findViewById(R.id.send)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (txtMessage.getText().toString() != "" && txtMessage.getText().toString().length() > 1) {
                    //envio a Firebase
                    final Message message = new Message();
                    message.setDate(new Date());
                    message.setUser(user.getDisplayName());
                    message.setDestinyId(destiny.getId());
                    message.setOriginId(user.getUid());
                    message.setMessage(txtMessage.getText().toString());
                    databaseReference.child(databaseReference.push().getKey()).setValue(message);
                    if (!(destiny.getId().equals("0"))) {
                        databaseReference2.child(databaseReference2.push().getKey()).setValue(message);

                        Thread thread = new Thread() {
                            @Override
                            public void run() {
                                try {
                                    Messaging.post(user.getDisplayName() + " ha escrito", message.getMessage(), destiny.getToken());
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        };
                        thread.start();
                    } else {
                        //envio al topic
                        Thread thread = new Thread() {
                            @Override
                            public void run() {
                                try {
                                    Messaging.post(user.getDisplayName() + " ha escrito", message.getMessage());
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        };
                        thread.start();
                    }


                    //actualizamos la interfaz
                    txtMessage.setText("");
                }
            }
        });
    }

    public class MessageAdapter extends ArrayAdapter<Message> {
        public MessageAdapter(Context context, List<Message> messages) {
            super(context, 0, messages);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Message message = getItem(position);
            if (message.getOriginId().equals(user.getUid())) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.message_item_right, parent, false);
            } else {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.message_item, parent, false);
            }
            TextView tvMessage = (TextView) convertView.findViewById(R.id.message);
            TextView tvUserName = (TextView) convertView.findViewById(R.id.userName);
            TextView tvDate = (TextView) convertView.findViewById(R.id.date);
            tvMessage.setText(message.getMessage());
            tvUserName.setText(message.getUser());
            tvDate.setText(new SimpleDateFormat("dd/MM/yyyy HH:mm").format(message.getDate()));
            return convertView;
        }
    }
}
