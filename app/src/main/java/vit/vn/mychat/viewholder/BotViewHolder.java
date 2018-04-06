package vit.vn.mychat.viewholder;


import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import vit.vn.mychat.R;

/**
 * Created by Admin on 28/1/18.
 */

public class BotViewHolder extends RecyclerView.ViewHolder  {



    public TextView leftText;
    public TextView rightText;
    public TextView leftUser;

    public BotViewHolder(View itemView){
        super(itemView);

        leftText = (TextView)itemView.findViewById(R.id.leftText);
        rightText = (TextView)itemView.findViewById(R.id.rightText);
        leftUser = itemView.findViewById(R.id.leftUser);

    }
}
