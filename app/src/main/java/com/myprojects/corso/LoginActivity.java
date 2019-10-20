package com.myprojects.corso;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "Login";
    private EditText mEmailField;
    private EditText mPasswordField;
    private FirebaseAuth mAuth;
    public ProgressDialog mProgressDialog;
    private FirebaseFirestore db;
    private boolean check;
    private boolean access;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        setContentView(R.layout.activity_login);
        mEmailField = findViewById(R.id.fieldEmail);
        mPasswordField = findViewById(R.id.fieldPassword);
        findViewById(R.id.emailSignInButton).setOnClickListener(this);
        findViewById(R.id.emailCreateAccountButton).setOnClickListener(this);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(mAuth.getCurrentUser() == null)updateUI(mAuth.getCurrentUser());
        else {isCoffeeShop();}
    }

    private void signIn(String email, String password) {
        Log.d(TAG, "signIn:" + email);
        if (!validateForm()) {
            return;
        }
        showProgressDialog();
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            if(!mAuth.getCurrentUser().isEmailVerified()){
                                Toast.makeText(LoginActivity.this, "Email adress is not verified",
                                        Toast.LENGTH_SHORT).show();
                            }else {
                                Log.d(TAG, "signInWithEmail:success");
                                isCoffeeShop();
                            }
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            try {
                                if (!mAuth.getCurrentUser().isEmailVerified()) {
                                    Toast.makeText(LoginActivity.this, "Email adress is not verified",
                                            Toast.LENGTH_SHORT).show();
                                }
                            } catch(Exception e){}

                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                        Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }
                        hideProgressDialog();
                    }
                });
    }


    public void signOut_public(FirebaseAuth auth){
        auth.signOut();
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

    private void updateUI(FirebaseUser user) {
        hideProgressDialog();

        if (user != null) {
            if(check == true){
                Log.d(TAG, "updateUI: got after shop test =--------------------");
                if(access == true){
                    Log.d(TAG, "updateUI: got after access test -----------");
                    Intent myIntent = new Intent(LoginActivity.this, ShopOwnerActivity.class);
                    LoginActivity.this.startActivity(myIntent);
                } else{
                    Toast.makeText(LoginActivity.this, "Your account has not been created yet!",
                            Toast.LENGTH_SHORT).show();
                }
            }
            else if(user.isEmailVerified()) {
                Log.d(TAG, "updateUI: moved to else if");
                Intent myIntent = new Intent(LoginActivity.this, MenuActivity.class);
                LoginActivity.this.startActivity(myIntent);
            }
        }
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.emailCreateAccountButton) {
            Intent myIntent = new Intent(LoginActivity.this, RegisterActivity.class);
            LoginActivity.this.startActivity(myIntent);
        }
        if (i == R.id.emailSignInButton) {
            signIn(mEmailField.getText().toString(), mPasswordField.getText().toString());
        }
    }

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

    private void isCoffeeShop () {
        Log.d(TAG, "isCoffeeShop: called");
        DocumentReference doc_ref = db.collection("users").document(mAuth.getCurrentUser().getEmail());
        doc_ref.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                Log.d(TAG, "Task is started ----------------");
                if (task.isSuccessful()) {
                    Log.d(TAG, "Task is succcesful ----------------");
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        check = (boolean) document.get("coffee_shop");
                        access = (boolean) document.get("access");
                        updateUI(mAuth.getCurrentUser());
                    } else {
                        Log.d(TAG, "No such document");
                        updateUI(mAuth.getCurrentUser());
                        return;
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                    return;
                }
            }
        });
    }
}

