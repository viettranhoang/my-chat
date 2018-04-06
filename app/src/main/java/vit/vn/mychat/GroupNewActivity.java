package vit.vn.mychat;

import android.content.Intent;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import vit.vn.mychat.model.Friends;
import vit.vn.mychat.viewholder.ChatViewHolder;

public class GroupNewActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private RecyclerView mFriendList;
    private ImageButton mNewGroupBtn;
    private TextInputLayout mNewGroupName;

    private DatabaseReference mFriendsDatabase;
    private DatabaseReference mUsersDatabase;
    private DatabaseReference mRootRef;
    private FirebaseAuth mAuth;

    private String mCurrentUserId;
    private String mGroupId;
    private String mGroupName;


    private String isChoose = "false";
    private List<String> arrChoose = new ArrayList();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_new);
        setToolbar();

        mAuth = FirebaseAuth.getInstance();
        mRootRef = FirebaseDatabase.getInstance().getReference();
        mCurrentUserId = mAuth.getCurrentUser().getUid();
        arrChoose.add(mCurrentUserId);
        mFriendsDatabase = FirebaseDatabase.getInstance().getReference().child("Friends").child(mCurrentUserId);
        mFriendsDatabase.keepSynced(true);
        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        mUsersDatabase.keepSynced(true);

        addControls();
        addEvents();
    }

    private void addEvents() {
        mNewGroupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mGroupName = mNewGroupName.getEditText().getText().toString();
                if (!TextUtils.isEmpty(mGroupName)) {
                    createNewGroup();

                    Intent intent = new Intent(GroupNewActivity.this, ChatGroupActivity.class);
                    intent.putExtra("group_id", mGroupId);
                    intent.putExtra("group_name", mGroupName);
                    startActivity(intent);
                }
                else Toast.makeText(getApplicationContext(), "Please enter group name!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void createNewGroup() {
        mGroupId =  mRootRef.child("Groups").push().getKey();

        Map map = new HashMap();
        map.put("Groups/" + mGroupId + "/name", mGroupName);

        for(String member_id : arrChoose) {
            map.put("Groups/" + mGroupId + "/members/" + member_id + "/temp", "");
            map.put("Groups_User/" + member_id + "/" + mGroupId + "/online", false);
        }

        mRootRef.updateChildren(map, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if (databaseError != null) {
                    Log.d("GROUPS_LOG", databaseError.getMessage().toString());
                }
            }
        });

    }

    private void addControls() {
        mFriendList = findViewById(R.id.group_new_list);
        mFriendList.setHasFixedSize(true);
        mFriendList.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

        mNewGroupBtn = findViewById(R.id.group_new_btn);
        mNewGroupName = findViewById(R.id.group_new_name);
    }

    private void setToolbar() {
        mToolbar = findViewById(R.id.group_new_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("New Group");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void onStart() {
        super.onStart();

        final boolean a[] = new boolean[1000];
        for (int i = 0; i < 1000; i++) {
            a[i] = false;
        }

        FirebaseRecyclerAdapter<Friends, ChatViewHolder> friendsRecyclerViewAdapter = new FirebaseRecyclerAdapter<Friends, ChatViewHolder>(
                Friends.class,
                R.layout.users_single_layout,
                ChatViewHolder.class,
                mFriendsDatabase

        ) {
            @Override
            protected void populateViewHolder(final ChatViewHolder friendsViewHolder, final Friends friends, final int position) {
                friendsViewHolder.setStatus(friends.getDate());

                final String list_user_id = getRef(position).getKey();

                mUsersDatabase.child(list_user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        final String userName = dataSnapshot.child("name").getValue().toString();
                        String userThumb = dataSnapshot.child("thumb_image").getValue().toString();
                        if (dataSnapshot.hasChild("online")) {
                            String userOnline = dataSnapshot.child("online").getValue().toString();
//                            friendsViewHolder.setOnline(userOnline);

                        }

                        friendsViewHolder.setName(userName);
                        friendsViewHolder.setImage(userThumb, getApplicationContext());
                        friendsViewHolder.setCheck("false");

                        friendsViewHolder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                if (a[position] == false) {
                                    a[position] = true;
                                    arrChoose.add(list_user_id);
                                    friendsViewHolder.setCheck("true");
                                } else {
                                    a[position] = false;
                                    arrChoose.remove(list_user_id);
                                    friendsViewHolder.setCheck("false");
                                }
                            }
                        });
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        };

        mFriendList.setAdapter(friendsRecyclerViewAdapter);
    }
}
