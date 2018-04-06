package vit.vn.mychat.fragment;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;
import vit.vn.mychat.ChatActivity;
import vit.vn.mychat.ProfileActivity;
import vit.vn.mychat.R;
import vit.vn.mychat.model.Conv;
import vit.vn.mychat.model.Friends;
import vit.vn.mychat.viewholder.ChatViewHolder;


/**
 * A simple {@link Fragment} subclass.
 */
public class RequestsFragment extends Fragment {

    private RecyclerView mChatsList;

    private DatabaseReference mChatsDatabase;
    private DatabaseReference mUsersDatabase;

    private FirebaseAuth mAuth;

    private String mCurrentUserId;

    private View mMainView;

    public RequestsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mMainView = inflater.inflate(R.layout.fragment_requests, container, false);

        mAuth = FirebaseAuth.getInstance();
        mCurrentUserId = mAuth.getCurrentUser().getUid();
        mChatsDatabase = FirebaseDatabase.getInstance().getReference().child("Friend_req").child(mCurrentUserId);
        mChatsDatabase.keepSynced(true);
        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        mUsersDatabase.keepSynced(true);

        mChatsList = mMainView.findViewById(R.id.request_list);
        mChatsList.setHasFixedSize(true);
        mChatsList.setLayoutManager(new LinearLayoutManager(getContext()));

        // Inflate the layout for this fragment
        return mMainView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter<Friends, ChatViewHolder> chatsRecyclerViewAdapter
                = new FirebaseRecyclerAdapter<Friends, ChatViewHolder>(
                Friends.class,
                R.layout.users_single_layout,
                ChatViewHolder.class,
                this.mChatsDatabase

        ) {
            @Override
            protected void populateViewHolder(final ChatViewHolder requestsViewHolder, Friends model, int position) {


                final String list_user_id = getRef(position).getKey();

                mUsersDatabase.child(list_user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        final String userName = dataSnapshot.child("name").getValue().toString();
                        final String userStatus = dataSnapshot.child("status").getValue().toString();
                        String userThumb = dataSnapshot.child("thumb_image").getValue().toString();
                        if (dataSnapshot.hasChild("online")) {
                            String userOnline = dataSnapshot.child("online").getValue().toString();
                            requestsViewHolder.setOnline(userOnline);

                        }
                        requestsViewHolder.setName(userName);
                        requestsViewHolder.setStatus(userStatus);
                        requestsViewHolder.setImage(userThumb, getContext());
                        requestsViewHolder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent profileIntent = new Intent(getContext(), ProfileActivity.class);
                                profileIntent.putExtra("user_id", list_user_id);
                                startActivity(profileIntent);
                            }
                        });
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
            }
        };

        this.mChatsList.setAdapter(chatsRecyclerViewAdapter);
    }



//    public static class ChatsViewHolder extends RecyclerView.ViewHolder {
//
//        View mView;
//
//        public ChatsViewHolder(View itemView) {
//            super(itemView);
//
//            mView = itemView;
//        }
//
//        public void setStatus(String date){
//            TextView userDateView = mView.findViewById(R.id.users_single_status);
//            userDateView.setText(date);
//        }
//
//        public void setName(String name) {
//            TextView userDateView = mView.findViewById(R.id.users_single_name);
//            userDateView.setText(name);
//        }
//
//        public void setImage(String thumb_image, Context ctx){
//            CircleImageView userImageView = mView.findViewById(R.id.users_single_image);
//            Picasso.with(ctx).load(thumb_image).placeholder(R.drawable.default_avatar).into(userImageView);
//
//        }
//
//        public void setOnline(String online_status) {
//            ImageView userOnlineView = mView.findViewById(R.id.users_single_online_icon);
//            if(online_status.equals("true")) {
//                userOnlineView.setVisibility(View.VISIBLE);
//            } else {
//                userOnlineView.setVisibility(View.INVISIBLE);
//            }
//
//        }
//    }
}
