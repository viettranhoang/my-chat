package vit.vn.mychat.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import de.hdodenhof.circleimageview.CircleImageView;
import vit.vn.mychat.R;

/**
 * Created by Admin on 30/1/2018.
 */

public class MessageViewHolder extends RecyclerView.ViewHolder {
    public TextView messageTextLeft;
    public TextView messageTextRight;
    public TextView timeSend;
    public CircleImageView profileImage;
    public ImageView messageImage;


    public MessageViewHolder(View itemView) {
        super(itemView);

        messageTextLeft = itemView.findViewById(R.id.message_text_left);
        messageTextRight = itemView.findViewById(R.id.message_text_right);
        timeSend = itemView.findViewById(R.id.time_text_layout);
        profileImage = itemView.findViewById(R.id.message_profile_layout);
        messageImage = itemView.findViewById(R.id.message_image_layout);
    }
}