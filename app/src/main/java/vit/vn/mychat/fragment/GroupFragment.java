package vit.vn.mychat.fragment;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import vit.vn.mychat.ChatGroupActivity;
import vit.vn.mychat.GroupNewActivity;
import vit.vn.mychat.R;
import vit.vn.mychat.model.Group;
import vit.vn.mychat.viewholder.ChatViewHolder;

/**
 * A simple {@link Fragment} subclass.
 */
public class GroupFragment extends Fragment {
    private RecyclerView mGroupsList;

    private ImageButton mAddGroup;
    FirebaseRecyclerAdapter<Group, ChatViewHolder> adapter;

    private DatabaseReference mGroupsDatabase;
    private DatabaseReference mGroupsUserDatabase;

    private FirebaseAuth mAuth;

    private String mCurrentUserId;

    private View mMainView;




    public GroupFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mMainView = inflater.inflate(R.layout.fragment_group, container, false);

        mAuth = FirebaseAuth.getInstance();
        mCurrentUserId = mAuth.getCurrentUser().getUid();

        mGroupsUserDatabase = FirebaseDatabase.getInstance().getReference().child("Groups_User").child(mCurrentUserId);
        mGroupsUserDatabase.keepSynced(true);
        mGroupsDatabase = FirebaseDatabase.getInstance().getReference().child("Groups");
        mGroupsDatabase.keepSynced(true);

        mGroupsList = mMainView.findViewById(R.id.groups_list);
        mGroupsList.setHasFixedSize(true);
        mGroupsList.setLayoutManager(new LinearLayoutManager(getContext()));

        mAddGroup = mMainView.findViewById(R.id.groups_btn);
        mAddGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), GroupNewActivity.class);
                startActivity(intent);
            }
        });

        return mMainView;
    }

    @Override
    public void onStart() {
        super.onStart();
        adapter = new FirebaseRecyclerAdapter<Group, ChatViewHolder>(
                Group.class, R.layout.users_single_layout, ChatViewHolder.class, mGroupsUserDatabase) {
            @Override
            protected void populateViewHolder(final ChatViewHolder chatsViewHolder, Group model, int position) {
                final String list_group_id = getRef(position).getKey();

                mGroupsDatabase.child(list_group_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.hasChildren()) {
                            final String groupName = dataSnapshot.child("name").getValue().toString();
//                        if (dataSnapshot.hasChild("online")) {
//                            String userOnline = dataSnapshot.child("online").getValue().toString();
//                            chatsViewHolder.setOnline(userOnline);
//                        }
                            chatsViewHolder.setName(groupName);
                            chatsViewHolder.setStatus("GONE");
                            chatsViewHolder.mView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    Intent chatIntent = new Intent(getContext(), ChatGroupActivity.class);
                                    chatIntent.putExtra("group_id", list_group_id);
                                    chatIntent.putExtra("group_name", groupName);
                                    startActivity(chatIntent);
                                }
                            });
                        }
//                        try {
//
//                        } catch (Exception e){
//
//                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        };

        mGroupsList.setAdapter(adapter);
    }
}
