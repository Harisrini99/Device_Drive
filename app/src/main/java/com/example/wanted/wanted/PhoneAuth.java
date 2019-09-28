package com.example.wanted.wanted;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.EventListener;
import java.util.concurrent.TimeUnit;

public class PhoneAuth extends AppCompatActivity {


    private EditText phonenumber;
    private EditText otpnn;
    private Button sendnn;
    private Button resendnn;
    private ProgressDialog progressDialog;
    private Button verifynn;
    static final Integer WRITE_EXST = 0x3;
    public static Activity context;
    public boolean verifi = false;
    private static final String TAG = "PhoneAuthActivity";
    private static String userid;
    private static final String KEY_VERIFY_IN_PROGRESS = "key_verify_in_progress";
    private static final int STATE_INITIALIZED = 1;
    private static final int STATE_CODE_SENT = 2;
    private static final int STATE_VERIFY_FAILED = 3;
    private static final int STATE_VERIFY_SUCCESS = 4;
    private static final int STATE_SIGNIN_FAILED = 5;
    private static final int STATE_SIGNIN_SUCCESS = 6;
    private static final String PREF_VALUE = "check";
    private FirebaseAuth mAuth;
    private static final String PREF_UNAME = "Username";
    private String UnameValue;
    private static boolean use;
    private EventListener eventListener;
    private boolean DefaultValue = false;
    private static final String PREFS_NAME = "preferences";
    private final String DefaultUnameValue = "";

    private boolean mVerificationInProgress = false;
    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    private EditText n91;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.phone_auth);
        context = PhoneAuth.this;


        phonenumber = (EditText) findViewById(R.id.phonenumber);
        otpnn = (EditText) findViewById(R.id.otpn);
        sendnn = (Button) findViewById(R.id.sendn);
        resendnn = (Button)findViewById(R.id.resendn);
        verifynn = (Button) findViewById(R.id.verifyn);
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        boolean val = loadPreferences();
        if (user != null)
        {
            FirebaseDatabase.getInstance().setPersistenceEnabled(true);
            Intent intent = new Intent(PhoneAuth.this, Home.class);
            startActivity(intent);
            finish();

        }
        DatabaseReference r = FirebaseDatabase.getInstance().getReference();
        r.keepSynced(true);
        mAuth = FirebaseAuth.getInstance();
        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            @Override
            public void onVerificationCompleted(PhoneAuthCredential credential) {

                Log.d(TAG, "onVerificationCompleted:" + credential);
                mVerificationInProgress = false;
                updateUI(STATE_VERIFY_SUCCESS, credential);
                otpnn.setText(credential.getSmsCode());
                // progressDialog.dismiss();
                signInWithPhoneAuthCredential(credential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                mVerificationInProgress = false;

                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    progressDialog.dismiss();
                    phonenumber.setError("Invalid phone number.");
                } else if (e instanceof FirebaseTooManyRequestsException) {
                    progressDialog.dismiss();
                    Toast.makeText(PhoneAuth.this,
                            "Too Much of Attempts!!\nPlease try again later", Toast.LENGTH_SHORT).show();
                }

                updateUI(STATE_VERIFY_FAILED);
            }

            @Override
            public void onCodeSent(String verificationId,
                                   PhoneAuthProvider.ForceResendingToken token) {
                progressDialog.dismiss();
                Toast.makeText(PhoneAuth.this,
                        "OTP Sent", Toast.LENGTH_LONG).show();
                mVerificationId = verificationId;
                mResendToken = token;
                updateUI(STATE_CODE_SENT);
            }
        };


    }
    private void askForPermission(String permission, Integer requestCode) {
        if (ContextCompat.checkSelfPermission(PhoneAuth.this, permission) != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(PhoneAuth.this, permission)) {

                //This is called if user has denied the permission before
                //In this case I am just asking the permission again
                ActivityCompat.requestPermissions(PhoneAuth.this, new String[]{permission}, requestCode);

            } else {

                ActivityCompat.requestPermissions(PhoneAuth.this, new String[]{permission}, requestCode);
            }
        }

    }


    public void savePreferences(boolean valu,Context context)

    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        Boolean statusLocked = prefs.edit().putBoolean("locked", valu).commit();
    }

    public boolean loadPreferences()
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        Boolean yourLocked = prefs.getBoolean("locked", false);
        return yourLocked;
    }

    public void setTrue()
    {
        // savePreferences(false);
    }
    protected boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_VERIFY_IN_PROGRESS, mVerificationInProgress);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mVerificationInProgress = savedInstanceState.getBoolean(KEY_VERIFY_IN_PROGRESS);
    }

    private void verifyPhoneNumberWithCode(String verificationId, String code)
    {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);

        signInWithPhoneAuthCredential(credential);
    }

    private void resendVerificationCode(String phoneNumber,
                                        PhoneAuthProvider.ForceResendingToken token) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,               // Activity (for callback binding)
                mCallbacks,         // OnVerificationStateChangedCallbacks
                token);             // ForceResendingToken from callbacks
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        boolean val = loadPreferences();

                        if (task.isSuccessful())
                        {
                            {
                                FirebaseUser user = task.getResult().getUser();
                                setUser(user.getUid());
                                verifi = true;
                                updateUI(STATE_SIGNIN_SUCCESS, user);
                                progressDialog.dismiss();
                                Intent intent = new Intent(PhoneAuth.this,Home.class);
                                startActivity(intent);
                                finish();



                            }


                        }
                        else
                        {
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException)
                            {
                                progressDialog.dismiss();
                                otpnn.setError("Invalid code.");
                            }
                            updateUI(STATE_SIGNIN_FAILED);
                        }
                    }
                });
    }

    private void signOut() {
        mAuth.signOut();
        updateUI(STATE_INITIALIZED);
    }

    @Override
    public void onBackPressed()
    {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setMessage("Do You Really Want To Exit")
                // .setIcon(R.drawable.alert)
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i)
                    {

                    }
                })
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id)
                    {
                        finish();
                    }
                });
        android.app.AlertDialog alert = builder.create();
        alert.show();
    }

    private void updateUI(int uiState) {
        updateUI(uiState, mAuth.getCurrentUser(), null);
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            updateUI(STATE_SIGNIN_SUCCESS, user);
        } else {
            updateUI(STATE_INITIALIZED);
        }
    }


    public void setUser(String id) {
        userid = id;
    }

    public String getUser() {
        return userid;
    }

    private void updateUI(int uiState, FirebaseUser user) {
        updateUI(uiState, user, null);
    }

    private void updateUI(int uiState, PhoneAuthCredential cred) {
        updateUI(uiState, null, cred);
    }

    private void updateUI(int uiState, FirebaseUser user, PhoneAuthCredential cred) {
        switch (uiState) {
            case STATE_INITIALIZED:
                enableViews(sendnn, phonenumber);
                //   disableViews(ver, resend, otp);
                //  mDetailText.setText(null);
                break;
            case STATE_CODE_SENT:
                // Code sent state, show the verification field, the
                enableViews(verifynn, resendnn, phonenumber, otpnn);
                sendnn.setVisibility(View.GONE);
                verifynn.setVisibility(View.VISIBLE);
                //disableViews(ok);
                //   mDetailText.setText(R.string.status_code_sent);
                break;
            case STATE_VERIFY_FAILED:
                // Verification has failed, show all options
                enableViews(sendnn, verifynn, resendnn, phonenumber, otpnn);
                // mDetailText.setText(R.string.status_verification_failed);
                break;
            case STATE_VERIFY_SUCCESS:
                // Verification has succeeded, proceed to firebase sign in
                verifi = true;
                disableViews(sendnn, verifynn, resendnn, phonenumber,
                        otpnn);
                // mDetailText.setText(R.string.status_verification_succeeded);

                // Set the verification text based on the credential
                if (cred != null) {
                    if (cred.getSmsCode() != null) {
                        otpnn.setText(cred.getSmsCode());
                    } else {
                        // ver.setText(R.string.instant_validation);
                    }
                }

                break;
            case STATE_SIGNIN_FAILED:
                break;
            case STATE_SIGNIN_SUCCESS:
                verifi = true;
                break;
        }

   /*     if (user == null)
        {
            // Signed out
          //  mPhoneNumberViews.setVisibility(View.VISIBLE);
           // mSignedInViews.setVisibility(View.GONE);

          //  mStatusText.setText(R.string.signed_out);
        } else {
            // Signed in
          //  mPhoneNumberViews.setVisibility(View.GONE);
          //  mSignedInViews.setVisibility(View.VISIBLE);

          //  enableViews(ph, mVerificationField);
           // ph.setText(null);
           // mVerificationField.setText(null);

           // mStatusText.setText(R.string.signed_in);
           // mDetailText.setText(getString(R.string.firebase_status_fmt, user.getUid()));
        }   */
    }

    private void enableViews(View... views) {
        for (View v : views) {
            v.setEnabled(true);
        }
    }

    private void disableViews(View... views) {
        for (View v : views) {
            v.setEnabled(false);
        }
    }

    private boolean validatePhoneNumber() {

        String phoneNumber = "+91" + phonenumber.getText().toString();
        if (TextUtils.isEmpty(phoneNumber)) {
            phonenumber.setError("Invalid phone number.");
            progressDialog.dismiss();
            return false;
        }

        return true;
    }

    private void startPhoneNumberVerification(String phoneNumber) {
        // [START start_phone_auth]
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,               // Activity (for callback binding)
                mCallbacks);        // OnVerificationStateChangedCallbacks
        // [END start_phone_auth]

        mVerificationInProgress = true;
    }

    public void sendn(View view) {
        if (isOnline()) {
            if (!validatePhoneNumber())
            {
                return;
            }
            PackageManager manager = getPackageManager();

            {
                startPhoneNumberVerification("+91" + phonenumber.getText().toString());
                progressDialog = ProgressDialog.show(context, "Sending Otp", "Please wait...", false, false);
            }

        } else
        {
            Toast.makeText(PhoneAuth.this,
                    "No Internet", Toast.LENGTH_SHORT).show();
        }
    }

    public void resend(View view) {
        if (isOnline()) {
            resendVerificationCode("+91" + phonenumber.getText().toString(), mResendToken);
            progressDialog = ProgressDialog.show(this, "Sending Otp", "Please wait...", false, false);


        } else {
            Toast.makeText(PhoneAuth.this,
                    "No Internet", Toast.LENGTH_SHORT).show();
        }

    }

    public void verify(View view)
    {

        String code = otpnn.getText().toString();
        if (TextUtils.isEmpty(code))
        {
            otpnn.setError("Cannot be empty.");
            return;
        }
        verifyPhoneNumberWithCode(mVerificationId, code);
        progressDialog = ProgressDialog.show(this, "Verifing", "Please wait...", false, false);

    }

}
