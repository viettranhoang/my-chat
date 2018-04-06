package vit.vn.mychat.adapter;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import vit.vn.mychat.R;
import vit.vn.mychat.model.Messages;
import vit.vn.mychat.utils.GetTimeAgo;
import vit.vn.mychat.viewholder.MessageViewHolder;

/**
 * Created by Admin on 27/1/2018.
 */

public class MessageAdapter extends RecyclerView.Adapter<MessageViewHolder>{

    private DatabaseReference mUserDatabase;
    private FirebaseAuth mAuth;

    private List<Messages> mMessageList;

    public MessageAdapter(List<Messages> mMessageList) {
        this.mMessageList = mMessageList;
    }

    @Override
    public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_single_layout, parent, false);
        return new MessageViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final MessageViewHolder viewHolder, int position) {
        Messages m = mMessageList.get(position);

        String from_user = m.getFrom();
        String message_type = m.getType();
        long time = m.getTime();

        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(from_user);
        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String name = dataSnapshot.child("name").getValue().toString();
                String image = dataSnapshot.child("thumb_image").getValue().toString();


                Picasso.with(viewHolder.profileImage.getContext()).load(image).placeholder(R.drawable.default_avatar).into(viewHolder.profileImage);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mAuth = FirebaseAuth.getInstance();
        String current_user_id = mAuth.getCurrentUser().getUid();

        if (message_type.equals("text")) {
            viewHolder.messageImage.setVisibility(View.GONE);
            if (from_user.equals(current_user_id)) {
                viewHolder.messageTextRight.setText(m.getMessage());
                viewHolder.messageTextRight.setVisibility(View.VISIBLE);

                viewHolder.messageTextLeft.setVisibility(View.GONE);
                viewHolder.profileImage.setVisibility(View.GONE);
                viewHolder.timeSend.setVisibility(View.GONE);
            } else {
                viewHolder.messageTextLeft.setText(m.getMessage());
                viewHolder.messageTextLeft.setVisibility(View.VISIBLE);
                viewHolder.profileImage.setVisibility(View.VISIBLE);
                viewHolder.timeSend.setVisibility(View.VISIBLE);
                viewHolder.timeSend.setText(GetTimeAgo.getTimeAgo(time, viewHolder.timeSend.getContext()));

                viewHolder.messageTextRight.setVisibility(View.GONE);
            }


        } else {
            viewHolder.messageTextLeft.setVisibility(View.INVISIBLE);
            viewHolder.messageTextRight.setVisibility(View.INVISIBLE);
            if (from_user.equals(current_user_id)) {
                viewHolder.profileImage.setVisibility(View.GONE);
                viewHolder.timeSend.setVisibility(View.GONE);
            } else {
                viewHolder.profileImage.setVisibility(View.VISIBLE);
                viewHolder.timeSend.setVisibility(View.VISIBLE);
                viewHolder.timeSend.setText(GetTimeAgo.getTimeAgo(time, viewHolder.timeSend.getContext()));
            }


            Picasso.with(viewHolder.profileImage.getContext()).load(m.getMessage()).placeholder(R.drawable.default_avatar).into(viewHolder.messageImage);
        }
    }

    @Override
    public int getItemCount() {
        return mMessageList.size();
    }



}
