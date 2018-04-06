package vit.vn.mychat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class StatusActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private TextInputLayout txtStatus;
    private Button btnSave;

    private DatabaseReference userDatabase;
    private FirebaseUser currentUser;
    String current_uid;
    private ProgressDialog statusProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);

        //Firebase
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
         current_uid = currentUser.getUid();
        userDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(current_uid);

        setToolbar();

        addControls();
        addEvents();
    }

    private void addEvents() {
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Progress
                statusProgress = new ProgressDialog(StatusActivity.this);
                statusProgress.setTitle("Saving Changes");
                statusProgress.setMessage("Please wait");
                statusProgress.setCanceledOnTouchOutside(false);
                statusProgress.show();

                String status = txtStatus.getEditText().getText().toString();
                userDatabase.child("status").setValue(status).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()) {
                            statusProgress.dismiss();
                        }
                        else {
                            Toast.makeText(getApplicationContext(), "There was some error :(((", Toast.LENGTH_LONG).show();
                        }
                    }
                });
                Intent intent = new Intent(StatusActivity.this, SettingsActivity.class);
//                intent.putExtra("user_id", current_uid);
                startActivity(intent);
            }
        });
    }

    private void addControls() {
        txtStatus = findViewById(R.id.status_input);
        btnSave = findViewById(R.id.status_save_btn);

        String status_value = getIntent().getStringExtra("status_value");
        txtStatus.getEditText().setText(status_value);

    }

    private void setToolbar() {
        toolbar = findViewById(R.id.status_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Account Status");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
}
