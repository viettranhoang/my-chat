package vit.vn.mychat;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import vit.vn.mychat.adapter.MessageAdapter;
import vit.vn.mychat.model.Messages;
import vit.vn.mychat.utils.GetTimeAgo;

public class ChatActivity extends AppCompatActivity {

    private String mChatUserId;

    private Toolbar mChatToolbar;

    private DatabaseReference mRootRef;
    private FirebaseAuth mAuth;
    private  String mCurrentUserId;

    private TextView mTitleView;
    private TextView mLastView;
    private CircleImageView mProfileImage;

    private ImageButton mChatAddBtn;
    private ImageButton mChatSendBtn;
    private EditText mChatMessageView;
    private TextView mChatSeen;

    private RecyclerView mMessagesList;
    private SwipeRefreshLayout mRefreshLayout; //F5 load message

    private final List<Messages> messagesList = new ArrayList<>();
    private LinearLayoutManager mLinearLayout;
    private MessageAdapter mAdapter;

//    private static  final int TOTAL_ITEMS_TO_LOAD = 10;
//    private int mCurrentPage = 0;

    private static final int GALLERY_PICK = 1;

    StorageReference mImageStorage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mRootRef = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        mCurrentUserId = mAuth.getCurrentUser().getUid();

        mChatUserId = getIntent().getStringExtra("user_id");

        mImageStorage = FirebaseStorage.getInstance().getReference();



        mRootRef.child("Chat").child(mCurrentUserId).child(mChatUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                try {
                    boolean seen = (boolean) dataSnapshot.child("seen").getValue();
                    String time = dataSnapshot.child("timestamp").getValue().toString();
                    if(seen==true) {
                        mChatSeen.setText("Seen " + GetTimeAgo.getTimeAgo(Long.parseLong(time), getApplicationContext()));
                    }
                    else {
                        mChatSeen.setText("");
                    }
                } catch (Exception e) {
                    mRootRef.child("Chat").child(mCurrentUserId).child(mChatUserId).child("seen").setValue(false);
                    mRootRef.child("Chat").child(mCurrentUserId).child(mChatUserId).child("timestamp").setValue(ServerValue.TIMESTAMP);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        setToolbar();
        addControls();
        loadMessages();
        refreshMessage();
        addEvents();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mRootRef.child("Chat").child(mChatUserId).child(mCurrentUserId).child("seen").setValue(true);
        mRootRef.child("Chat").child(mChatUserId).child(mCurrentUserId).child("timestamp").setValue(ServerValue.TIMESTAMP);

        if (mAuth.getCurrentUser() != null) {
            mRootRef.child("Users").child(mAuth.getCurrentUser().getUid()).child("online").setValue("true");
        }

    }

    private void refreshMessage() {

    }

    private void loadMessages() {
        mRootRef.child("messages").child(mCurrentUserId).child(mChatUserId).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Messages message = dataSnapshot.getValue(Messages.class);
                messagesList.add(message);
                mAdapter.notifyDataSetChanged();

                mMessagesList.scrollToPosition(messagesList.size() - 1);
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
        });
    }

    private void addEvents() {
        mChatSendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage();

                Map chatAddMap = new HashMap();
                chatAddMap.put("seen", false);
                chatAddMap.put("timestamp", ServerValue.TIMESTAMP);

                Map chatUserMap = new HashMap();
                chatUserMap.put("Chat/" + mCurrentUserId + "/" + mChatUserId, chatAddMap);
                chatUserMap.put("Chat/" + mChatUserId + "/" + mCurrentUserId, chatAddMap);

                mRootRef.updateChildren(chatUserMap, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                        if (databaseError != null) {
                            Log.d("CHAT_LOG", databaseError.getMessage().toString());
                        }
                    }
                });
            }
        });

        mChatAddBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryIntent = new Intent();
                galleryIntent.setType("image/*");
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);

                startActivityForResult(Intent.createChooser(galleryIntent, "SELECT IMAGE"), GALLERY_PICK);
            }
        });
    }

    private void sendMessage() {
        String message = mChatMessageView.getText().toString();

        if (!TextUtils.isEmpty(message)) {
            String current_user_ref = "messages/" + mCurrentUserId + "/" + mChatUserId;
            String chat_user_ref = "messages/" + mChatUserId + "/" + mCurrentUserId;

            DatabaseReference user_message_push = mRootRef.child("messages").child(mCurrentUserId).child(mChatUserId).push();
            String push_id = user_message_push.getKey();

            Map messageMap = new HashMap();
            messageMap.put("message", message);
            messageMap.put("seen", false);
            messageMap.put("type", "text");
            messageMap.put("time", ServerValue.TIMESTAMP);
            messageMap.put("from", mCurrentUserId);

            Map messageUserMap = new HashMap();
            messageUserMap.put(current_user_ref + "/" + push_id, messageMap);
            messageUserMap.put(chat_user_ref + "/" + push_id, messageMap);

            mChatMessageView.setText("");

            mRootRef.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    if (databaseError != null) {
                        Log.d("CHAT_LOG", databaseError.getMessage().toString());
                    }
                }
            });
        }
    }

    private void addControls() {
        mChatAddBtn = findViewById(R.id.chat_add_btn);
        mChatSendBtn = findViewById(R.id.chat_send_btn);
        mChatMessageView = findViewById(R.id.chat_message_view);
        mChatSeen = findViewById(R.id.chat_seen);

        mAdapter = new MessageAdapter(messagesList);
        mLinearLayout = new LinearLayoutManager(this);
        mMessagesList = findViewById(R.id.messages_list);
        mMessagesList.setHasFixedSize(true);
        mMessagesList.setLayoutManager(mLinearLayout);
        mMessagesList.setAdapter(mAdapter);

        mRefreshLayout = findViewById(R.id.message_swipe_layout);

    }

    private void setToolbar() {
        mChatToolbar = findViewById(R.id.chat_toolbar);
        setSupportActionBar(mChatToolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View action_bar_view = inflater.inflate(R.layout.chat_custom_bar_layout, null);

        actionBar.setCustomView(action_bar_view);

        mTitleView = findViewById(R.id.custom_bar_title);
        mLastView = findViewById(R.id.custom_bar_seen);
        mProfileImage = findViewById(R.id.custom_bar_image);

        String userName = getIntent().getStringExtra("user_name");
        mTitleView.setText(userName);

        mRootRef.child("Users").child(mChatUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String online = dataSnapshot.child("online").getValue().toString();
                String image = dataSnapshot.child("thumb_image").getValue().toString();


                Picasso.with(mProfileImage.getContext()).load(image).placeholder(R.drawable.default_avatar).into(mProfileImage);

                if (online.equals("true")) {
                    mLastView.setText("Online");
                } else {
                    String lastSeenTime = GetTimeAgo.getTimeAgo(Long.parseLong(online), getApplicationContext());
                    mLastView.setText(lastSeenTime);
                }


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_PICK && resultCode == RESULT_OK) {
            Uri imageUri = data.getData();

            final String current_user_ref = "messages/" + mCurrentUserId + "/" + mChatUserId;
            final String chat_user_ref = "messages/" + mChatUserId + "/" + mCurrentUserId;

            DatabaseReference user_message_push = mRootRef.child("messages").child(mCurrentUserId).child(mChatUserId).push();
            final String push_id = user_message_push.getKey();


            StorageReference filepath = mImageStorage.child("message_images").child(push_id + ".ipg");
            filepath.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if (task.isSuccessful()) {
                        String download_url = task.getResult().getDownloadUrl().toString();

                        Map messageMap = new HashMap();
                        messageMap.put("message", download_url);
                        messageMap.put("seen", false);
                        messageMap.put("type", "image");
                        messageMap.put("time", ServerValue.TIMESTAMP);
                        messageMap.put("from", mCurrentUserId);

                        Map messageUserMap = new HashMap();
                        messageUserMap.put(current_user_ref + "/" + push_id, messageMap);
                        messageUserMap.put(chat_user_ref + "/" + push_id, messageMap);

                        mChatMessageView.setText("");

                        mRootRef.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                if (databaseError != null) {
                                    Log.d("CHAT_LOG", databaseError.getMessage().toString());
                                }
                            }
                        });

                    }
                }
            });

        }
    }
}
