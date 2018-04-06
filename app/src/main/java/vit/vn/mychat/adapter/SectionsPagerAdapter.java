package vit.vn.mychat.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import vit.vn.mychat.fragment.ChatsFragment;
import vit.vn.mychat.fragment.FriendsFragment;
import vit.vn.mychat.fragment.GroupFragment;
import vit.vn.mychat.fragment.RequestsFragment;

/**
 * Created by Admin on 24/1/2018.
 */

public class SectionsPagerAdapter extends FragmentPagerAdapter{
    public SectionsPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 2:
                RequestsFragment requestsFragment = new RequestsFragment();
                return requestsFragment;
            case 0:
                ChatsFragment chatsFragment = new ChatsFragment();
                return chatsFragment;
            case 1:
                FriendsFragment friendsFragment = new FriendsFragment();
                return friendsFragment;
            case 3:
                GroupFragment groupFragment = new GroupFragment();
                return groupFragment;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return 4;
    }

    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return "CHATS";
            case 2:
                return "REQUESTS";
            case 1:
                return "FRIENDS";
            case 3:
                return "GROUPS";
            default:
                return null;
        }
    }
}
