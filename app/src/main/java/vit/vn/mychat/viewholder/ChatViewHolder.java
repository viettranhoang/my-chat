package vit.vn.mychat.viewholder;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;
import vit.vn.mychat.R;

/**
 * Created by Admin on 29/1/2018.
 */

public class ChatViewHolder extends RecyclerView.ViewHolder {
    public View mView;

    public ChatViewHolder(View itemView) {
        super(itemView);

        mView = itemView;
    }

    public void setStatus(String message){
        TextView userDateView = mView.findViewById(R.id.users_single_status);
        userDateView.setText(message);
        if(message.equals("GONE")) userDateView.setVisibility(View.GONE);
    }

    public void setName(String name) {
        TextView userDateView = mView.findViewById(R.id.users_single_name);
        userDateView.setText(name);
    }

    public void setImage(String thumb_image, Context ctx){
        CircleImageView userImageView = mView.findViewById(R.id.users_single_image);
        Picasso.with(ctx).load(thumb_image).placeholder(R.drawable.default_avatar).into(userImageView);

    }

    public void setOnline(String online_status) {
        ImageView userOnlineView = mView.findViewById(R.id.users_single_online_icon);
        if(online_status.equals("true")) {
            userOnlineView.setVisibility(View.VISIBLE);
        } else {
            userOnlineView.setVisibility(View.INVISIBLE);
        }
    }

    public void setCheck(String check_status) {
        ImageView userCheckView = mView.findViewById(R.id.users_single_check_icon);
        ImageView userCheckOutlineView = mView.findViewById(R.id.users_single_check_outline_icon);
        if(check_status.equals("true")) {
            userCheckView.setVisibility(View.VISIBLE);
            userCheckOutlineView.setVisibility(View.INVISIBLE);
        } else {
            userCheckView.setVisibility(View.INVISIBLE);
            userCheckOutlineView.setVisibility(View.VISIBLE);
        }
    }
}
