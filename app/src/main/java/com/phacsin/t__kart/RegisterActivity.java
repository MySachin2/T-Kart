package com.phacsin.t__kart;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class RegisterActivity extends AppCompatActivity {

    private EditText name_edit,address_edit;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private ProgressDialog progressDialog;
    private static String phone,uid,role;
    private DatabaseReference mRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences = getSharedPreferences(Utils.pref,MODE_PRIVATE);
        editor = sharedPreferences.edit();
        if(sharedPreferences.contains("address"))
        {
            startActivity(new Intent(RegisterActivity.this, MainActivity.class));
            finish();
        }
        setContentView(R.layout.activity_register);
        name_edit = findViewById(R.id.name);
        mRef = FirebaseDatabase.getInstance().getReference();
        //checkAddress();
    }

    public void checkAddress()
    {
        if(sharedPreferences.contains("uid")) {
            uid = sharedPreferences.getString("uid", "");
            phone = sharedPreferences.getString("phone", "");
            mRef.child("Users").orderByKey().equalTo(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                        address_edit.setText(postSnapshot.child("Address").getValue(String.class));
                        name_edit.setText(postSnapshot.child("Name").getValue(String.class));
                        if (postSnapshot.hasChild("Role")) {
                            role = postSnapshot.child("Role").getValue(String.class);
                            editor.putString("role", role);
                            editor.commit();
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }
}
