package com.example.learningeasle.chats;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.learningeasle.PushNotifications.APIService;
import com.example.learningeasle.PushNotifications.Client;
import com.example.learningeasle.PushNotifications.Data;
import com.example.learningeasle.PushNotifications.MyResponse;
import com.example.learningeasle.PushNotifications.NotificationSender;
import com.example.learningeasle.R;
import com.example.learningeasle.chats.AdapterChat;
import com.example.learningeasle.chats.ModelChat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatDetailsActivity extends AppCompatActivity {


    androidx.appcompat.widget.Toolbar toolbar;
    RecyclerView recyclerView;
    ImageView profileIV;
    TextView nameTV, statusTV;
    EditText messageET;
    ImageButton sendbtn;
    FirebaseAuth firebaseAuth;

    String hisUid;
    String myUid, myName;
    String hisImage;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference usersDbRef;
    String name;

    ValueEventListener seenListener;
    DatabaseReference userRefForSeen;
    List<ModelChat> chatList;
    AdapterChat adapterChat;

    private APIService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_details);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("");
        recyclerView = findViewById(R.id.chat_recyclerView);
        profileIV = findViewById(R.id.profileIV);
        nameTV = findViewById(R.id.nameTV);
        statusTV = findViewById(R.id.status);
        messageET = findViewById(R.id.messageET);
        sendbtn = findViewById(R.id.send);

        apiService = Client.getClient("https://fcm.googleapis.com/").create(APIService.class); // For notifications

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        Intent intent = getIntent();

        hisUid = intent.getStringExtra("Id");

        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser user = firebaseAuth.getCurrentUser();
        myUid = user.getUid();


        firebaseDatabase = FirebaseDatabase.getInstance();
        usersDbRef = firebaseDatabase.getReference("Users");

        usersDbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    if (ds.getKey().equals(myUid)) {
                        myName = (String) ds.child("Name").getValue();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        usersDbRef.addValueEventListener(new ValueEventListener() {                                   // To set user's name and image to whom chatting
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    if (ds.getKey().equals(hisUid)) {
                        name = "" + ds.child("Name").getValue();
                        String typingStatus = "" + ds.child("typingTo").getValue();
                        if (typingStatus.equals(myUid)) {
                            statusTV.setText("typing..");
                        } else {
                            String onlineStatus = "" + ds.child("onlineStatus").getValue();
                            if (onlineStatus.equals("online")) {
                                statusTV.setText(onlineStatus);
                            } else {
                                Calendar calendar = Calendar.getInstance(Locale.getDefault());
                                calendar.setTimeInMillis(Long.parseLong(onlineStatus));

                                String pTime = DateFormat.format("dd/MM/yyyy hh:mm aa", calendar).toString();
                                statusTV.setText("Last seen : " + pTime);
                            }
                        }
                        hisImage = "" + ds.child("Url").getValue();
                        nameTV.setText(name);


                        try {
                            Picasso.get().load(hisImage).placeholder(R.drawable.ic_action_profile).into(profileIV);
                        } catch (Exception e) {
                            Picasso.get().load(R.drawable.ic_action_profile).into(profileIV);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        sendbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {                                                                     // When send button is clicked functions for sendnotification and sendmessage called
                final String message = messageET.getText().toString().trim();
                if (TextUtils.isEmpty(message)) {
                    Toast.makeText(getApplicationContext(), "Cannot send the empty message", Toast.LENGTH_SHORT
                    ).show();
                    return;
                } else {
                    sendMessage(message);

                    FirebaseDatabase.getInstance().getReference().child("Tokens").child(hisUid).child("token").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            String usertoken = dataSnapshot.getValue(String.class);
                            sendNotifications(usertoken, myName + "+-*/" + myUid, message);//
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                }

            }
        });

        messageET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().trim().length() == 0) {
                    checkTypingStatus("noOne");
                } else {
                    checkTypingStatus(hisUid);
                }

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        readMessages();
        seenMessage();

    }

    public void sendNotifications(String usertoken, String title, String message) {
        Data data = new Data(title, message);
        NotificationSender sender = new NotificationSender(data, usertoken);
        apiService.sendNotifcation(sender).enqueue(new Callback<MyResponse>() {
            @Override
            public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                if (response.code() == 200) {
                    if (response.body().isSuccessful != 1) {
                        Toast.makeText(getApplicationContext(), "Failed ", Toast.LENGTH_LONG);
                    }
                }
            }

            @Override
            public void onFailure(Call<MyResponse> call, Throwable t) {

            }
        });
    }

    private void checkOnlineStatus(String status) {
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Users").child(myUid);
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("onlineStatus", status);
        dbRef.updateChildren(hashMap);
    }

    private void checkTypingStatus(String typing) {
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Users").child(myUid);
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("typingTo", typing);
        dbRef.updateChildren(hashMap);
    }

    private void seenMessage() {                                                                          // To check whether message is seen by the receiver
        userRefForSeen = FirebaseDatabase.getInstance().getReference("Chats").child(myUid + hisUid);
        seenListener = userRefForSeen.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    ModelChat chat;
                    final HashMap<Object, String> hashMap = (HashMap<Object, String>) ds.getValue();
                    chat = new ModelChat(hashMap.get("message"), hashMap.get("receiver"),
                            hashMap.get("sender"), hashMap.get("timestamp"), hashMap.get("isSeen"));
                    if (chat.getSender().equals(hisUid) && chat.getReceiver().equals(myUid)) {
                        HashMap<String, Object> hasSeenHashMap = new HashMap<>();
                        hasSeenHashMap.put("isSeen", "1");
                        ds.getRef().updateChildren(hasSeenHashMap);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }


    private void readMessages() {
        chatList = new ArrayList<>();                                                                 // Reading from firebase and setting in adapter to display
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Chats").child(hisUid + myUid);
        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                chatList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    ModelChat chat;
                    final HashMap<Object, String> hashMap = (HashMap<Object, String>) ds.getValue();
                    chat = new ModelChat(hashMap.get("message"), hashMap.get("receiver"),
                            hashMap.get("sender"), hashMap.get("timestamp"), hashMap.get("isSeen"));
                    if (chat.getSender().equals(hisUid) && chat.getReceiver().equals(myUid) ||
                            chat.getReceiver().equals(hisUid) && chat.getSender().equals(myUid)) {
                        chatList.add(chat);
                    }
                    adapterChat = new AdapterChat(getApplicationContext(), chatList, hisImage);
                    adapterChat.notifyDataSetChanged();
                    recyclerView.setAdapter(adapterChat);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
//        DatabaseReference dbRef1 = FirebaseDatabase.getInstance().getReference("Chats").child(hisUid+myUid);
//        dbRef1.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                chatList.clear();
//                for (DataSnapshot ds : snapshot.getChildren()) {
//                    ModelChat chat;
//                    final HashMap<Object, String> hashMap = (HashMap<Object, String>) ds.getValue();
//                    chat = new ModelChat(hashMap.get("message"), hashMap.get("receiver"),
//                            hashMap.get("sender"), hashMap.get("timestamp"), hashMap.get("isSeen"));
//                    if (chat.getSender().equals(hisUid) && chat.getReceiver().equals(myUid) ||
//                            chat.getReceiver().equals(hisUid) && chat.getSender().equals(myUid)) {
//                        chatList.add(chat);
//                    }
//                    adapterChat = new AdapterChat(getApplicationContext(), chatList, hisImage);
//                    adapterChat.notifyDataSetChanged();
//                    recyclerView.setAdapter(adapterChat);
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });
    }

    private void sendMessage(String message) {                                                             // Setting data on firebase
        String timestamp = String.valueOf(System.currentTimeMillis());
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", myUid);
        hashMap.put("receiver", hisUid);
        hashMap.put("message", message);
        hashMap.put("timestamp", timestamp);
        hashMap.put("isSeen", "0");
        databaseReference.child("Chats").child(myUid + hisUid).push().setValue(hashMap);
        databaseReference.child("Chats").child(hisUid + myUid).push().setValue(hashMap);

        messageET.setText("");
    }

    @Override
    protected void onStart() {
        checkOnlineStatus("online");
        super.onStart();
    }

    @Override
    protected void onPause() {
        super.onPause();
        String timestamp = String.valueOf(System.currentTimeMillis());
        checkOnlineStatus(timestamp);
        checkTypingStatus("noOne");
        userRefForSeen.removeEventListener(seenListener);
    }

    @Override
    protected void onResume() {
        checkOnlineStatus("online");
        super.onResume();
    }

}