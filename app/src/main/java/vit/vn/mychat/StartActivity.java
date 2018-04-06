package vit.vn.mychat;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class StartActivity extends AppCompatActivity {

    @BindView(R.id.login_button_login)
    Button btnLogin;

    @BindView(R.id.start_button_signup)
    Button btnSignup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        ButterKnife.bind(this);
    }

    @OnClick(R.id.login_button_login)
    public void onLoginClick(View view) {
        Intent loginIntent = new Intent(StartActivity.this, LoginActivity.class);
        startActivity(loginIntent);
    }

    @OnClick(R.id.start_button_signup)
    public void onSignupClick(View view) {
        Intent regIntent = new Intent(StartActivity.this, RegisterActivity.class);
        startActivity(regIntent);
    }


}
