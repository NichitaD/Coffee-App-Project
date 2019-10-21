package com.myprojects.corso;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.facebook.login.Login;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "RegisterActivity";
    private EditText mEmailField;
    private EditText mPasswordField;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private Dialog dialog;
    private int type;
    private TextView emailRequestField;
    private TextView nameRequestField;
    private TextView passRequestField;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        mAuth = FirebaseAuth.getInstance();
        mEmailField = findViewById(R.id.fieldEmail);
        mPasswordField = findViewById(R.id.fieldPassword);
        findViewById(R.id.emailCreateAccountButton).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.emailCreateAccountButton) {
            type = 1;
            createAccount(mEmailField.getText().toString(), mPasswordField.getText().toString());
        }
        if(i == R.id.createCoffeeShop){
            Log.d("Button", "called");
            LayoutInflater inflater = LayoutInflater.from(this);
            final Dialog dialog3 = new Dialog(this);
            Window window = dialog3.getWindow();
            WindowManager.LayoutParams wlp = window.getAttributes();
            wlp.gravity = Gravity.CENTER;
            wlp.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
            wlp.y = 50;
            window.setAttributes(wlp);
            View view3 = inflater.inflate(R.layout.request_dialog, null);
            dialog3.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog3.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog3.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
            dialog3.setContentView(view3);
            dialog3.show();
            emailRequestField = dialog3.findViewById(R.id.email_request);
            nameRequestField = dialog3.findViewById(R.id.name_request);
            passRequestField = dialog3.findViewById(R.id.pass_request);
            dialog = dialog3;
        }
        if(i == R.id.send_request){
            type = 2;
            dialog.dismiss();
            createAccount(emailRequestField.getText().toString(), passRequestField.getText().toString());
            addRequest(emailRequestField.getText().toString(), nameRequestField.getText().toString());
            Toast.makeText(RegisterActivity.this, "Request sent!",
                    Toast.LENGTH_SHORT).show();
        }
        if(i == R.id.facebook_register){
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            intent.putExtra("option","fb");
            RegisterActivity.this.startActivity(intent);
        }
        if(i == R.id.google_register){
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            intent.putExtra("option","google");
            RegisterActivity.this.startActivity(intent);
        }
    }

    private void createAccount(String email, String password) {
        Log.d(TAG, "createAccount:" + email);
        if (type == 1 && !validateForm()) {
            return;
        }
        showProgressDialog();
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success");
                            sendEmailVerification();
                        } else {
                            // If creating account  fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(RegisterActivity.this, "Failed to create account",
                                    Toast.LENGTH_SHORT).show();
                        }
                        hideProgressDialog();
                    }
                });
    }

    private void sendEmailVerification() {
        // Send verification email
        final FirebaseUser user = mAuth.getCurrentUser();
        Log.d(TAG, "Got to eamil verification, user is :" + user);
        user.sendEmailVerification()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            hideProgressDialog();
                            Toast.makeText(RegisterActivity.this,
                                    "Verification email sent to " + user.getEmail(),
                                    Toast.LENGTH_SHORT).show();
                            Intent myIntent = new Intent(RegisterActivity.this, LoginActivity.class);
                            RegisterActivity.this.startActivity(myIntent);
                            if(type == 1) {
                                addToDatabase(user.getEmail());
                            } else {
                                addCoffeeShop(user.getEmail(), nameRequestField.getText().toString());
                            }
                        } else {
                            Log.e(TAG, "sendEmailVerification", task.getException());
                            Toast.makeText(RegisterActivity.this,
                                    "Failed to send verification email.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private boolean validateForm() {
        boolean valid = true;
        String email = mEmailField.getText().toString();
        if (TextUtils.isEmpty(email)) {
            mEmailField.setError("Required.");
            valid = false;
        } else {
            mEmailField.setError(null);
        }
        String password = mPasswordField.getText().toString();
        if (TextUtils.isEmpty(password)) {
            mPasswordField.setError("Required.");
            valid = false;
        } else {
            mPasswordField.setError(null);
        }
        return valid;
    }

    public ProgressDialog mProgressDialog;
    public void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage(getString(R.string.loading));
            mProgressDialog.setIndeterminate(true);
        }
        mProgressDialog.show();
    }

    public void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        hideProgressDialog();
    }

    private void addToDatabase (String email){
        Map<String, Object>  tracker = new HashMap<>();
        tracker.put("Monday", 0);
        tracker.put("Tuesday", 0);
        tracker.put("Wednesday", 0);
        tracker.put("Thursday",0);
        tracker.put("Friday",0);
        tracker.put("Saturday",0);
        tracker.put("Sunday",0);
        tracker.put("today", 0);
        tracker.put("coffee_shop",false);
        tracker.put("access",true);
        tracker.put("last_access_time", Calendar.getInstance());
        db.collection("users").document(email)
                .set(tracker)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot successfully written!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error writing document", e);
                    }
                });
    }

    private void addCoffeeShop (String email, String name){
        Map<String, Object>  tracker = new HashMap<>();
        tracker.put("access",false);
        tracker.put("coffee_shop",true);
        tracker.put("name", name);
        db.collection("users").document(email)
                .set(tracker)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot successfully written!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error writing document", e);
                    }
                });
    }

    private void addRequest (String email, String name){
        Map<String, Object>  tracker = new HashMap<>();
        tracker.put("name", name);
        tracker.put("email", email);
        db.collection("requests").document(name)
                .set(tracker)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot successfully written!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error writing document", e);
                    }
                });
    }
}

