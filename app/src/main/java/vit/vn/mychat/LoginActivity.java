package vit.vn.mychat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class LoginActivity extends AppCompatActivity {

    @BindView(R.id.register_input_email)
        TextInputLayout inputEmail;
    @BindView(R.id.register_input_password)
        TextInputLayout inputPassword;
    @BindView(R.id.login_button_login)
        Button btnLogin;
    @BindView(R.id.login_toolbar)
        Toolbar toolbar;

    private ProgressDialog mLoginDialog;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        mAuth = FirebaseAuth.getInstance();
        mLoginDialog = new ProgressDialog(this);

        setToolbar();
    }

    @OnClick(R.id.login_button_login)
    public void onLoginClick(View view) {
        String email = inputEmail.getEditText().getText().toString();
        String password = inputPassword.getEditText().getText().toString();

        if (!TextUtils.isEmpty(email) || !TextUtils.isEmpty(password)) {
            mLoginDialog.setTitle("Logning In");
            mLoginDialog.setMessage("Please wait");
            mLoginDialog.setCanceledOnTouchOutside(false);
            mLoginDialog.show();

            loginUser(email, password);
        }
    }

    private void setToolbar() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Login");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void loginUser(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            mLoginDialog.dismiss();

                            Intent mainIntent = new Intent(LoginActivity.this, MainActivity.class);
                            mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(mainIntent);
                            finish();
                        } else {
                            mLoginDialog.hide();
                            Toast.makeText(LoginActivity.this, "Cannot Sign in. Please check the from and try again",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

}
