package clockworktt.gaby.com;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.muddzdev.styleabletoastlibrary.StyleableToast;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LOGINACTIVITY";
    private EditText mEmail;
    private EditText mPassword;
    private Button mSignUp;
    private Button mLogin;
    private TextView mForgotPassword;

    private FirebaseAuth mAuth;
    private ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Hide status bar and ActionBar
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().hide();

        mEmail = findViewById(R.id.emailEditText);
        mPassword = findViewById(R.id.passwordEditText);
        mSignUp = findViewById(R.id.sign_up_button);
        mLogin = findViewById(R.id.login_button);
        mForgotPassword = findViewById(R.id.forgot_password);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please wait...");

        mAuth = FirebaseAuth.getInstance();

        mLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String email = mEmail.getText().toString();
                String password = mPassword.getText().toString();

                if(!email.equals("") && !password.equals("")){

                    loginUser(email, password);

                }
                else {
                    checkEmailPasswordToast();
                }

            }
        });

        mSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String email = mEmail.getText().toString();
                String password = mPassword.getText().toString();

                if(!email.equals("") && !password.equals("")){

                    signUpUser(email, password);

                }else {
                    checkEmailPasswordToast();
                }

            }
        });

        mForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //TODO add alerDialog and get user to input email and submit to change password

                AlertDialog.Builder mBuilder = new AlertDialog.Builder(LoginActivity.this);
                View forgot_password_view = getLayoutInflater().inflate(R.layout.forgot_password_email_input_layout, null);
                final EditText mForgotPasswordEditText = forgot_password_view.findViewById(R.id.forgot_password_edit_text);
                Button mResetPasswordButton = forgot_password_view.findViewById(R.id.forgot_password_reset_button);

                mForgotPasswordEditText.setText(mEmail.getText().toString());

                mBuilder.setView(forgot_password_view);
                final AlertDialog dialog = mBuilder.create();
                dialog.show();

                mResetPasswordButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        String resetEmail = mForgotPasswordEditText.getText().toString();

                        if(!TextUtils.isEmpty(resetEmail)){

                            sendResetPassword(resetEmail, dialog);

                        }else {
                            StyleableToast.makeText(LoginActivity.this, "Provide Your Email Address", R.style.loginErrorToastColor).show();
                        }

                    }
                });



            }
        });

    }//end onCreate()

    private void sendResetPassword(String resetEmail, final AlertDialog dialog) {

        mAuth.sendPasswordResetEmail(resetEmail).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    dialog.dismiss();
                    StyleableToast.makeText(LoginActivity.this, "We have sent you instructions to reset your password!",R.style.reset_password_sent_toast).show();
                } else {
                    Toast.makeText(LoginActivity.this, "Failed to send reset email!", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void checkEmailPasswordToast() {

        StyleableToast.makeText(this, "Check your Email and Password", R.style.loginErrorToastColor).show();

    }

    private void loginUser(String email, String password) {

        progressDialog.show();

        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                Log.d(TAG, "onComplete: logging user");

                if(task.isSuccessful()){

                    Log.d(TAG, "onComplete: logged in successfully");

                    if(FirebaseAuth.getInstance().getCurrentUser().isEmailVerified()){

                        Log.d(TAG, "onComplete: user verified");

                        if(FirebaseAuth.getInstance().getCurrentUser() != null){

                            final String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

                            DatabaseReference userDataRef = FirebaseDatabase.getInstance().getReference().child("Users").child(userId);
                            userDataRef.child("photo_uri").setValue("default").addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                    if(task.isSuccessful()){

                                        progressDialog.dismiss();

                                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                        finish();
                                    }else{

                                        progressDialog.dismiss();
                                        StyleableToast.makeText(LoginActivity.this, "Login Failed! Please check your connection!", R.style.loginErrorToastColor).show();

                                    }

                                }
                            });

                        }

                    }else {
                        progressDialog.dismiss();
                        Log.d(TAG, "onComplete: user unverified");
                        FirebaseAuth.getInstance().signOut();
                        StyleableToast.makeText(LoginActivity.this, "Verify Email address first!", R.style.loginErrorToastColor).show();
                        moveTaskToBack(true);
                        finish();
                    }


                } else {
                    progressDialog.dismiss();
                    StyleableToast.makeText(LoginActivity.this, "Error in signing in!", R.style.loginErrorToastColor).show();
                }

            }
        });

    }

    private void signUpUser(String email, String password) {

        progressDialog.show();

        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if(task.isSuccessful()) {
                    Log.d(TAG, "onComplete: " + "user created");
                    FirebaseAuth.getInstance().getCurrentUser().sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {

                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            Log.d(TAG, "onComplete: verification email sent");

                            if(task.isSuccessful()){
                                progressDialog.dismiss();
                                FirebaseAuth.getInstance().signOut();
                                Log.d(TAG, "onComplete: sign out user");
                                Toast.makeText(LoginActivity.this, "Please check your email and Verify that", Toast.LENGTH_SHORT).show();
                                moveTaskToBack(true);
                                finish();
                            }else{

                                Log.d(TAG, "onComplete: failed to sent verification email");
                                Toast.makeText(LoginActivity.this, "Check your email! Failed to send verification link", Toast.LENGTH_SHORT).show();
                            }

                        }
                    });

                }else {
                    Log.d(TAG, "onComplete: failed to create user");
                    progressDialog.dismiss();
                    StyleableToast.makeText(LoginActivity.this, "error in creating account!", R.style.loginErrorToastColor).show();
                }

            }
        });

    }
}
