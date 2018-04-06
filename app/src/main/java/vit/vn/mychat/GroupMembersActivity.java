package vit.vn.mychat;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
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

import vit.vn.mychat.model.Users;
import vit.vn.mychat.viewholder.ChatViewHolder;

public class GroupMembersActivity extends AppCompatActivity {

    private Toolbar mToolbar;

    private DatabaseReference mGroupsDatabase;
    private DatabaseReference mUsersDatabase;
    private DatabaseReference mRootRef;
    private FirebaseAuth mAuth;

    private RecyclerView mGroupsList;

    private String mCurrentGroupId;
    private String mCurrentUserId;
    private String mCurrentGroupName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_members);

        setToolbar();
        mCurrentGroupId = getIntent().getStringExtra("group_id");
        mCurrentGroupName = getIntent().getStringExtra("group_name");

        mAuth = FirebaseAuth.getInstance();
        mCurrentUserId = mAuth.getCurrentUser().getUid();
        mGroupsDatabase = FirebaseDatabase.getInstance().getReference().child("Groups").child(mCurrentGroupId).child("members");
        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        mRootRef = FirebaseDatabase.getInstance().getReference();

        addControls();
    }

    private void addControls() {
        mGroupsList = findViewById(R.id.group_members_list);
        mGroupsList.setHasFixedSize(true);
        mGroupsList.setLayoutManager(new LinearLayoutManager(this));

    }

    private void setToolbar() {
        mToolbar = findViewById(R.id.group_members_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Info Group");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter<Users, ChatViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Users, ChatViewHolder>(
                Users.class,
                R.layout.users_single_layout,
                ChatViewHolder.class,
                mGroupsDatabase

        ) {
            @Override
            protected void populateViewHolder(final ChatViewHolder usersViewHolder, Users users, int position) {
                final String list_user_id = getRef(position).getKey();

                mUsersDatabase.child(list_user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        final String userName = dataSnapshot.child("name").getValue().toString();
                        String userStatus = dataSnapshot.child("status").getValue().toString();
                        String userThumb = dataSnapshot.child("thumb_image").getValue().toString();
                        if (dataSnapshot.hasChild("online")) {
                            String userOnline = dataSnapshot.child("online").getValue().toString();
                            usersViewHolder.setOnline(userOnline);

                        }
                        usersViewHolder.setName(userName);
                        usersViewHolder.setStatus(userStatus);
                        usersViewHolder.setImage(userThumb, getApplicationContext());
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        };


        mGroupsList.setAdapter(firebaseRecyclerAdapter);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.group_members_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        switch (item.getItemId()) {
            case R.id.group_members_add_btn:
                Intent intentAdd = new Intent(GroupMembersActivity.this, GroupAddMemberActivity.class);
                intentAdd.putExtra("group_id", mCurrentGroupId);
                intentAdd.putExtra("group_name", mCurrentGroupName);
                startActivity(intentAdd);
                break;
            case R.id.group_members_delete_btn:
                delGroup();
                break;
            case R.id.group_members_change_btn:
                changeGroupName();
                break;
            case R.id.group_members_out_btn:
                outGroup();
            case android.R.id.home:
                finish();
        }
        return true;
    }

    private void outGroup() {
        Map outMap = new HashMap();
        outMap.put("Groups/" + mCurrentGroupId + "/members/" + mCurrentUserId, null);
        outMap.put("Groups_User/" + mCurrentUserId + "/" + mCurrentGroupId, null);

        mRootRef.updateChildren(outMap, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if (databaseError != null) {
                    Log.d("GROUP_OUT_LOG", databaseError.getMessage().toString());
                }
            }
        });

        Intent intent = new Intent(GroupMembersActivity.this, MainActivity.class);
        startActivity(intent);
    }

    private void changeGroupName() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.custom_dialog, null);
        dialogBuilder.setView(dialogView);

        final EditText groupName = dialogView.findViewById(R.id.edit1);
        groupName.setText(mCurrentGroupName);
        dialogBuilder.setTitle("Change Group Name");
        dialogBuilder.setMessage("");
        dialogBuilder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                mRootRef.child("Groups").child(mCurrentGroupId).child("name").setValue(groupName.getText().toString());
                dialog.cancel();
            }
        });
        dialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.cancel();
            }
        });
        AlertDialog b = dialogBuilder.create();
        b.show();
    }

    private void delGroup() {
        Intent intent = new Intent(GroupMembersActivity.this, MainActivity.class);
        startActivity(intent);
        mGroupsDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot idSnapshot : dataSnapshot.getChildren()){
                    String memberId = idSnapshot.getKey().toString();
                    Map delMap = new HashMap();
                    delMap.put("Groups_User/" + memberId + "/" + mCurrentGroupId, null);
                    mRootRef.updateChildren(delMap);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        Map delMap = new HashMap();
        delMap.put("Groups/" + mCurrentGroupId, null);

        mRootRef.updateChildren(delMap, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if (databaseError != null) {
                    Log.d("GROUP_OUT_LOG", databaseError.getMessage().toString());
                }
            }
        });
    }
}
