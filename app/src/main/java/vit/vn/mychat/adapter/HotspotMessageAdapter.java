package vit.vn.mychat.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import vit.vn.mychat.HotspotChatActivity;
import vit.vn.mychat.R;
import vit.vn.mychat.model.ChatBotMessage;
import vit.vn.mychat.viewholder.BotViewHolder;

/**
 * Created by Admin on 31/1/2018.
 */

public class HotspotMessageAdapter extends RecyclerView.Adapter<BotViewHolder> {

    private List<ChatBotMessage> lst;

    public HotspotMessageAdapter(List<ChatBotMessage> mMessageList) {
        this.lst = mMessageList;
    }

    @Override
    public BotViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_hotspot_layout, parent, false);
        return new BotViewHolder(v);
    }

    @Override
    public void onBindViewHolder(BotViewHolder viewHolder, int position) {
        ChatBotMessage model = lst.get(position);

        if (model.getMsgUser().equals(HotspotChatActivity.username)) {
            viewHolder.rightText.setText(model.getMsgText());

            viewHolder.rightText.setVisibility(View.VISIBLE);
            viewHolder.leftUser.setVisibility(View.GONE);
            viewHolder.leftText.setVisibility(View.GONE);
        }
        else {
            viewHolder.leftText.setText(model.getMsgText());
            viewHolder.leftUser.setText(model.getMsgUser());

            viewHolder.rightText.setVisibility(View.GONE);
            viewHolder.leftUser.setVisibility(View.VISIBLE);
            viewHolder.leftText.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return lst.size();
    }
}
