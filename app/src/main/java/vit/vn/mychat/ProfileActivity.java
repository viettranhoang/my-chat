package vit.vn.mychat;

import android.app.ProgressDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import vit.vn.mychat.model.EProfileCurrentState;

public class ProfileActivity extends AppCompatActivity {
    private ImageView mProfileImage;
    private TextView mProfileName, mProfileStatus, mProfileFriendsCount;
    private Button mProfileSendReqBtn, mProfileDeclineBtn;

    private String user_id;

    private DatabaseReference mUsersDatabase;
    private DatabaseReference mFriendReqDatabase;
    private DatabaseReference mFriendDatabase;
    private DatabaseReference mRootRef;

    private ProgressDialog mProgressDialog;

    private FirebaseUser mCurrentUser;

    private EProfileCurrentState mCurrentState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        user_id = getIntent().getStringExtra("user_id");

        mRootRef = FirebaseDatabase.getInstance().getReference();
        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(user_id);
        mFriendReqDatabase = FirebaseDatabase.getInstance().getReference().child("Friend_req");
        mFriendDatabase = FirebaseDatabase.getInstance().getReference().child("Friends");
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();


        addControls();

        addEvents();
    }

    private void addEvents() {
        mUsersDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String display_name = dataSnapshot.child("name").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();
                String image = dataSnapshot.child("image").getValue().toString();

                mProfileName.setText(display_name);
                mProfileStatus.setText(status);
                Picasso.with(ProfileActivity.this).load(image).placeholder(R.drawable.default_avatar).into(mProfileImage);

                //------------------------- FRIENDS LIST / REQUEST FEATURE -------------

                mFriendReqDatabase.child(mCurrentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.hasChild(user_id)) {
                            String req_type = dataSnapshot.child(user_id).child("request_type").getValue().toString();

                            if (req_type.equals("received")) {
                                mCurrentState = EProfileCurrentState.REQUEST_RECEIVED;
                                mProfileSendReqBtn.setText("Accept Friend Request");

                                mProfileDeclineBtn.setVisibility(View.VISIBLE);
                                mProfileDeclineBtn.setEnabled(true);

                            } else if (req_type.equals("sent")){
                                mCurrentState = EProfileCurrentState.REQUEST_SENT;
                                mProfileSendReqBtn.setText("Cancel Friend Request");

                                mProfileDeclineBtn.setVisibility(View.INVISIBLE);
                                mProfileDeclineBtn.setEnabled(false);

                            }
                            mProgressDialog.dismiss();
                        } else {
                            mFriendDatabase.child(mCurrentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {

                                    if (dataSnapshot.hasChild(user_id)) {

                                        mCurrentState = EProfileCurrentState.FRIENDS;
                                        mProfileSendReqBtn.setText("Unfriend");

                                        mProfileDeclineBtn.setVisibility(View.INVISIBLE);
                                        mProfileDeclineBtn.setEnabled(false);

                                    }

                                    mProgressDialog.dismiss();
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    mProgressDialog.dismiss();
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mProfileSendReqBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                mProfileSendReqBtn.setEnabled(false);

                switch (mCurrentState) {
                    case NOT_FRIENDS:
                        notFriends(); //--------------- NOT FRIENDS STATE ------------------
                        break;
                    case REQUEST_SENT:
                        reqSent();  //--------------- CANCEL REQUEST STATE ------------------
                        break;
                    case REQUEST_RECEIVED:
                        reqReceived(); //--------------- REQUEST RECEIVED STATE ------------------
                        break;
                    case FRIENDS:
                        friends(); //--------------- UNFRIEND ------------------
                        break;
                }
            }
        });

        mProfileDeclineBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                reqSent();
            }
        });
    }

    private void friends() {
        Map unfriendstMap = new HashMap();
        unfriendstMap.put("Friends/" + mCurrentUser.getUid() + "/" + user_id + "/date", null);
        unfriendstMap.put("Friends/" + user_id + "/" + mCurrentUser.getUid() + "/date", null);

        mRootRef.updateChildren(unfriendstMap, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if (databaseError == null) {
                    mCurrentState = EProfileCurrentState.NOT_FRIENDS;
                    mProfileSendReqBtn.setText("Send Friend Request");

                    mProfileDeclineBtn.setVisibility(View.INVISIBLE);
                    mProfileDeclineBtn.setEnabled(false);
                } else {
                    String error = databaseError.getMessage();
                    Toast.makeText(ProfileActivity.this, error, Toast.LENGTH_SHORT).show();
                }
                mProfileSendReqBtn.setEnabled(true);
            }
        });

    }

    private void reqReceived() {
        final String currentDate = DateFormat.getDateTimeInstance().format(new Date());

        Map friendstMap = new HashMap();
        friendstMap.put("Friends/" + mCurrentUser.getUid() + "/" + user_id + "/date", currentDate);
        friendstMap.put("Friends/" + user_id + "/" + mCurrentUser.getUid() + "/date", currentDate);
        friendstMap.put("Friend_req/" + mCurrentUser.getUid() + "/" + user_id, null);
        friendstMap.put("Friend_req/" + user_id + "/" + mCurrentUser.getUid(), null);

        mRootRef.updateChildren(friendstMap, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if (databaseError == null) {
                    mProfileSendReqBtn.setEnabled(true);
                    mCurrentState = EProfileCurrentState.FRIENDS;
                    mProfileSendReqBtn.setText("Unfriend");

                    mProfileDeclineBtn.setVisibility(View.INVISIBLE);
                    mProfileDeclineBtn.setEnabled(false);
                } else {
                    Toast.makeText(ProfileActivity.this, "There was some error", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void reqSent() {

        Map sentRequestMap = new HashMap();
        sentRequestMap.put("Friend_req/" + mCurrentUser.getUid() + "/" + user_id, null);
        sentRequestMap.put("Friend_req/" + user_id + "/" + mCurrentUser.getUid(), null);


        mRootRef.updateChildren(sentRequestMap, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if (databaseError == null) {
                    mProfileSendReqBtn.setEnabled(true);
                    mCurrentState = EProfileCurrentState.NOT_FRIENDS;
                    mProfileSendReqBtn.setText("Send Friend Request");

                    mProfileDeclineBtn.setVisibility(View.INVISIBLE);
                    mProfileDeclineBtn.setEnabled(false);
                } else {
                    String error = databaseError.getMessage();
                    Toast.makeText(ProfileActivity.this, error, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void notFriends() {
        Map requestMap = new HashMap();
        requestMap.put("Friend_req/" + mCurrentUser.getUid() + "/" + user_id + "/request_type", "sent");
        requestMap.put("Friend_req/" + user_id + "/" + mCurrentUser.getUid() + "/request_type", "received");

        mRootRef.updateChildren(requestMap, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if (databaseError == null) {
                    mProfileSendReqBtn.setEnabled(true);

                    mCurrentState = EProfileCurrentState.REQUEST_SENT;
                    mProfileSendReqBtn.setText("Cancel Friend Request");
                } else {
                    Toast.makeText(ProfileActivity.this, "There was some error", Toast.LENGTH_SHORT).show();
                }



            }
        });
    }

    private void addControls() {
        mProfileImage = findViewById(R.id.profile_image);
        mProfileName = findViewById(R.id.profile_displayName);
        mProfileStatus = findViewById(R.id.profile_status);
        mProfileFriendsCount = findViewById(R.id.profile_totalFriends);
        mProfileSendReqBtn = findViewById(R.id.profile_send_req_btn);
        mProfileDeclineBtn = findViewById(R.id.profile_decline_btn);

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle("Loading User Data");
        mProgressDialog.setMessage("Please wait");
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.show();

        mCurrentState = EProfileCurrentState.NOT_FRIENDS;
        mProfileDeclineBtn.setVisibility(View.INVISIBLE);
        mProfileDeclineBtn.setEnabled(false);

    }
}
