package com.myprojects.corso;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class User {

    private String username;
    private String email;
    private String password;
    private DatabaseReference mDatabase;
    public User(){
        mDatabase = FirebaseDatabase.getInstance().getReference();
    }

    public void  setUserId (String userId){
        this.username = userId;
    }
    public void  setEmail (String email){
        this.email = email;
    }
    public void  setPassword (String password){
        this.password = password;
    }

    public String getUserId() {
        return this.username;
    }
    public String getEmail() {
        return this.email;
    }
    public String getPassword() {
        return this.password;
    }

}
