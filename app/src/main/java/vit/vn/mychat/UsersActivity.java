package vit.vn.mychat;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

import butterknife.BindView;
import butterknife.OnTextChanged;
import de.hdodenhof.circleimageview.CircleImageView;
import vit.vn.mychat.model.Users;
import vit.vn.mychat.viewholder.ChatViewHolder;

public class UsersActivity extends AppCompatActivity {

    @BindView(R.id.users_custom_bar_image)
    CircleImageView mImageProfile;

    @BindView(R.id.users_custom_bar_text_search)
    EditText mInputSearch;

    @BindView(R.id.users_list)
    RecyclerView mUsersList;

    private Toolbar mToolbar;

    private DatabaseReference mUsersDatabase;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);

        setToolbar();

        mAuth = FirebaseAuth.getInstance();
        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");

        mUsersList.setHasFixedSize(true);
        mUsersList.setLayoutManager(new LinearLayoutManager(this));

        addTextSearchListener();
    }


    private void addTextSearchListener() {

        mInputSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //search
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

    }

    private void setToolbar() {
        mToolbar = findViewById(R.id.users_toolbar);
        setSupportActionBar(mToolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View action_bar_view = inflater.inflate(R.layout.users_custom_bar_layout, null);

        actionBar.setCustomView(action_bar_view);

    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter<Users, ChatViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Users, ChatViewHolder>(
                Users.class,
                R.layout.users_single_layout,
                ChatViewHolder.class,
                mUsersDatabase

        ) {
            @Override
            protected void populateViewHolder(ChatViewHolder usersViewHolder, Users users, int position) {

                usersViewHolder.setName(users.getName());
                usersViewHolder.setStatus(users.getStatus());
                usersViewHolder.setImage(users.getThumb_image(), getApplicationContext());

                final String user_id = getRef(position).getKey();

                usersViewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        Intent profileIntent = new Intent(UsersActivity.this, ProfileActivity.class);
                        profileIntent.putExtra("user_id", user_id);
                        startActivity(profileIntent);

                    }
                });
            }
        };


        mUsersList.setAdapter(firebaseRecyclerAdapter);

    }
}
