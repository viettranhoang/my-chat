package vit.vn.mychat;

import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

import vit.vn.mychat.adapter.SectionsPagerAdapter;

public class MainActivity extends AppCompatActivity{
    private FirebaseAuth mAuth;
    private Toolbar toolbar;

    private ViewPager viewPager;
    private SectionsPagerAdapter sectionsPagerAdapter;
    private TabLayout tabLayout;

    private DatabaseReference mUserRef;

    private ImageButton btnHome;
    private ImageButton btnGroup;
    private ImageButton btnUser;
    private ImageButton btnBot;
    private ImageButton btnWifi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();

        if (mAuth.getCurrentUser() != null) {
            mUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());
        }

        setToolbar();
        addControls();
        addEvents();

    }

    private void addEvents() {
        btnUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intentUser = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intentUser);
            }
        });

        btnBot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intentUser = new Intent(MainActivity.this, BotChatActivity.class);
                startActivity(intentUser);
            }
        });

        btnGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intentGroup = new Intent(MainActivity.this, UsersActivity.class);
                startActivity(intentGroup);
            }
        });

        btnWifi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intentWifi = new Intent(MainActivity.this, HotspotActivity.class);
                startActivity(intentWifi);
            }
        });
    }

    private void addControls() {
        //Tabs
        viewPager = findViewById(R.id.main_tabPager);
        sectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(sectionsPagerAdapter);

        tabLayout = findViewById(R.id.main_tabs);
        tabLayout.setupWithViewPager(viewPager);

        btnHome = findViewById(R.id.main_home_btn);
        btnGroup = findViewById(R.id.main_group_btn);
        btnUser = findViewById(R.id.main_user_btn);
        btnBot = findViewById(R.id.main_bot_btn);
        btnWifi = findViewById(R.id.main_wifi_btn);

    }

    private void setToolbar() {
        toolbar = findViewById(R.id.main_page_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("My Chat");
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if(currentUser == null) {
            sendToStart();
        }
        else {
            mUserRef.child("online").setValue("true");
        }
    }

//    @Override
//    protected void onStop() {
//        super.onStop();
//        FirebaseUser currentUser = mAuth.getCurrentUser();
//
//        if (currentUser != null) {
//            mUserRef.child("online").setValue(ServerValue.TIMESTAMP);
//        }
//    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.main_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        if (item.getItemId() == R.id.main_logout_btn) {
            FirebaseAuth.getInstance().signOut();
            sendToStart();
        }
        return true;
    }

    private void sendToStart() {
        Intent startIntent = new Intent(MainActivity.this, StartActivity.class);
        startActivity(startIntent);
        finish();
    }
}
