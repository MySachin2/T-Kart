package com.phacsin.t__kart;


import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.stfalcon.smsverifycatcher.OnSmsCatchListener;
import com.stfalcon.smsverifycatcher.SmsVerifyCatcher;

import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity {


    private static final String TAG = "LoginActivity";
    // UI references.
    private EditText mPhoneView;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private String mVerificationId,code;
    private FirebaseAuth mAuth;
    private DatabaseReference mRef;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private SmsVerifyCatcher smsVerifyCatcher;
    private Button mEmailSignInButton;
    private OtpView otpView;
    private ProgressBar progressBar;
    private boolean otpsent = false;
    private PhoneAuthCredential credential;
    private TextView textView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences = getSharedPreferences(Utils.pref,MODE_PRIVATE);
        editor = sharedPreferences.edit();
        if(sharedPreferences.contains("uid"))
        {
            if(sharedPreferences.contains("address"))
                startActivity(new Intent(LoginActivity.this,MainActivity.class));
            else
                startActivity(new Intent(LoginActivity.this,RegisterActivity.class));
        }
        setContentView(R.layout.activity_login);
        mPhoneView = findViewById(R.id.edit_text_phone_number);
        mEmailSignInButton =  findViewById(R.id.btn_next);
        otpView = findViewById(R.id.pinview);
        textView = findViewById(R.id.textView);
        progressBar = findViewById(R.id.progressbar);
        mAuth = FirebaseAuth.getInstance();
        mRef = FirebaseDatabase.getInstance().getReference();
        smsVerifyCatcher = new SmsVerifyCatcher(LoginActivity.this, new OnSmsCatchListener<String>() {
            @Override
            public void onSmsCatch(String message) {
                Log.d("MESSAGE",message);
                code = parseCode(message);
                Log.d("CODE" , code);
                otpView.setOTP(code);
            }
        });
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!otpsent)
                    createDialog();
                else
                    signInWithPhoneAuthCredential(credential);
            }
        });
    }


    private void createDialog() {
        final Dialog dialog = new Dialog(LoginActivity.this);
        dialog.setContentView(R.layout.dialog_login);
        dialog.show();
        TextView phone_text = dialog.findViewById(R.id.phone_textview);
        phone_text.setText("+91 " + mPhoneView.getText().toString());
        Button edit = dialog.findViewById(R.id.btn_edit);
        Button ok = dialog.findViewById(R.id.btn_ok);
        edit.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(dialog.isShowing())
                {
                    dialog.dismiss();
                }
            }
        });
        ok.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(dialog.isShowing())
                {
                    findViewById(R.id.textInputLayout).setVisibility(View.GONE);
                    otpView.setVisibility(View.VISIBLE);
                    dialog.dismiss();
                    attemptLogin();
                }
            }
        });
    }


    private void attemptLogin() {

        // Reset errors.
        mPhoneView.setError(null);

        // Store values at the time of the login attempt.
        String phone = mPhoneView.getText().toString();

        boolean cancel = false;
        View focusView = null;


        // Check for a valid phone
        if (TextUtils.isEmpty(phone)) {
            mPhoneView.setError(getString(R.string.error_field_required));
            focusView = mPhoneView;
            cancel = true;
        } else if (!isPhoneValid(phone)) {
            mPhoneView.setError(getString(R.string.error_invalid_email));
            focusView = mPhoneView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                @Override
                public void onVerificationCompleted(final PhoneAuthCredential credential) {
                    // This callback will be invoked in two situations:
                    // 1 - Instant verification. In some cases the phone number can be instantly
                    //     verified without needing to send or enter a verification code.
                    // 2 - Auto-retrieval. On some devices Google Play services can automatically
                    //     detect the incoming verification SMS and perform verificaiton without
                    //     user action.
                    Log.d(TAG, "onVerificationCompleted:" + credential);
                    LoginActivity.this.credential = credential;
                    otpsent = true;
                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if(otpView.getOTP().equals(code))
                                signInWithPhoneAuthCredential(credential);
                        }
                    }, 1000);

                }

                @Override
                public void onVerificationFailed(FirebaseException e) {
                    // This callback is invoked in an invalid request for verification is made,
                    // for instance if the the phone number format is not valid.
                    Log.w(TAG, "onVerificationFailed", e);
                    progressBar.setVisibility(View.GONE);
                    if (e instanceof FirebaseAuthInvalidCredentialsException) {
                        // Invalid request
                        // ...
                    } else if (e instanceof FirebaseTooManyRequestsException) {
                        // The SMS quota for the project has been exceeded
                        // ...
                    }

                    // Show a message and update the UI
                    // ...
                }

                @Override
                public void onCodeSent(String verificationId,
                                       PhoneAuthProvider.ForceResendingToken token) {
                    // The SMS verification code has been sent to the provided phone number, we
                    // now need to ask the user to enter the code and then construct a credential
                    // by combining the code with a verification ID.
                    Log.d(TAG, "onCodeSent:" + verificationId);

                    // Save verification ID and resending token so we can use them later
                    mVerificationId = verificationId;
                    mResendToken = token;
                    otpsent = false;

                }
            };

            progressBar.setVisibility(View.VISIBLE);
            mEmailSignInButton.setVisibility(View.GONE);
            textView.setText("Verifying OTP");

            PhoneAuthProvider.getInstance().verifyPhoneNumber(
                    phone,        // Phone number to verify
                    60,                 // Timeout duration
                    TimeUnit.SECONDS,   // Unit of timeout
                    this,               // Activity (for callback binding)
                    mCallbacks);        }
        }


    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        progressBar.setVisibility(View.VISIBLE);
        if(code.equals(otpView.getOTP())) {
            textView.setText("Signing In");
            mAuth.signInWithCredential(credential)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                Log.d(TAG, "signInWithCredential:success");
                                final FirebaseUser user = task.getResult().getUser();
                                mRef = FirebaseDatabase.getInstance().getReference();
                                mRef.child("Phone Reference").child(user.getPhoneNumber()).setValue(user.getUid(), new DatabaseReference.CompletionListener() {
                                    @Override
                                    public void onComplete(DatabaseError
                                                                   databaseError, DatabaseReference databaseReference) {
                                        editor.putString("phone", user.getPhoneNumber());
                                        editor.putString("uid", user.getUid());
                                        editor.commit();
                                        startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
                                    }
                                });

                            } else {
                                // Sign in failed, display a message and update the UI
                                progressBar.setVisibility(View.GONE);
                                Log.w(TAG, "signInWithCredential:failure", task.getException());
                                if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                    // The verification code entered was invalid
                                }
                            }
                        }
                    });
        }
        else {
            progressBar.setVisibility(View.GONE);
            textView.setText("Incorrect OTP");
            mEmailSignInButton.setVisibility(View.VISIBLE);
        }
    }

    private boolean isPhoneValid(String phone) {
        if(phone.contains("+"))
        {
            return phone.length()==13;
        }
        else
            return phone.length()==10;
    }


    private String parseCode(String message) {
        Pattern p = Pattern.compile("\\b\\d{6}\\b");
        Matcher m = p.matcher(message);
        String code = "";
        while (m.find()) {
            code = m.group(0);
        }
        Log.d("CODE",code);
        return code;
    }


    @Override
    protected void onStart() {
        super.onStart();
        smsVerifyCatcher.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        smsVerifyCatcher.onStop();
    }

    /**
     * need for Android 6 real time permissions
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        smsVerifyCatcher.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

}

