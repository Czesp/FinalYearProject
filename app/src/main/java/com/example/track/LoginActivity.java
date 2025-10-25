package com.example.track;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import android.animation.ObjectAnimator;
import android.text.TextUtils;
import android.view.View;
import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import io.github.rupinderjeet.kprogresshud.KProgressHUD;

public class LoginActivity extends AppCompatActivity {

    private EditText edEmail,edPassword;

    private FirebaseAuth firebaseAuth;

    private KProgressHUD ProgressHUD;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        edEmail=findViewById(R.id.inputEmail);;
        edPassword=findViewById(R.id.inputPassword);
        AppCompatButton signInButton = findViewById(R.id.btn_sign_in);

        firebaseAuth=FirebaseAuth.getInstance();

        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SignInUser();
            }
        });

        TextView signupTab = findViewById(R.id.tab_signup);
        signupTab.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            finish();
        });
        View underline = findViewById(R.id.underline);
        ObjectAnimator.ofFloat(underline, "translationX", -250f).setDuration(300).start();
    }

    private void ProgressBar(){
        ProgressHUD = KProgressHUD.create(LoginActivity.this)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setMaxProgress(100)
                .setBackgroundColor(R.color.primary_blue)
                .show();
        ProgressHUD.setProgress(90);
        }

    private void SignInUser() {
        String email = edEmail.getText().toString().trim();
        String pass = edPassword.getText().toString().trim();

        if( TextUtils.isEmpty(email) || TextUtils.isEmpty(pass)){
            Toast.makeText(LoginActivity.this,"Please enter email and password",Toast.LENGTH_SHORT).show();
            return;
        }else {
            ProgressBar();
            firebaseAuth.signInWithEmailAndPassword(email,pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){
                        ProgressHUD.dismiss();
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                    } else{
                        Toast.makeText(LoginActivity.this,
                                "Signup failed: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(LoginActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

}
