package com.ruslanlyalko.ll.presentation;

import android.app.Application;

import com.crashlytics.android.Crashlytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ruslanlyalko.ll.data.configuration.DC;
import com.ruslanlyalko.ll.data.models.User;
import com.ruslanlyalko.ll.presentation.ui.login.LoginActivity;
import com.ruslanlyalko.ll.presentation.utils.PreferenceHelper;

import io.fabric.sdk.android.Fabric;

/**
 * Created by Ruslan Lyalko
 * on 11.11.2017.
 */

public class LlApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());
        initDb();
    }

    private void initDb() {
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        FirebaseDatabase.getInstance().getReference(DC.DB_LESSONS).keepSynced(true);
        FirebaseDatabase.getInstance().getReference(DC.DB_DIALOGS).keepSynced(true);
        FirebaseDatabase.getInstance().getReference(DC.DB_MESSAGES).keepSynced(true);
        loadUserData();
    }

    private void loadUserData() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user == null) return;
        FirebaseDatabase.getInstance().getReference(DC.DB_USERS)
                .child(user.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(final DataSnapshot dataSnapshot) {
                        User user = dataSnapshot.getValue(User.class);
                        if(user != null) {
                            if(user.getIsBlocked()) {
                                FirebaseAuth.getInstance().signOut();
                                startActivity(LoginActivity.getLaunchIntent(getApplicationContext()));
                            }
                            PreferenceHelper.newInstance(getApplicationContext()).setUser(user);
                        }
                    }

                    @Override
                    public void onCancelled(final DatabaseError databaseError) {
                    }
                });
    }
}
