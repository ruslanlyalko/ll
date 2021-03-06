package com.ruslanlyalko.ll.data;

import android.arch.lifecycle.MutableLiveData;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.ruslanlyalko.ll.data.models.Contact;
import com.ruslanlyalko.ll.data.models.Lesson;
import com.ruslanlyalko.ll.data.models.SettingsSalary;
import com.ruslanlyalko.ll.data.models.User;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.ruslanlyalko.ll.data.configuration.DC.DB_CONTACTS;
import static com.ruslanlyalko.ll.data.configuration.DC.DB_LESSONS;
import static com.ruslanlyalko.ll.data.configuration.DC.DB_SETTINGS_SALARY;
import static com.ruslanlyalko.ll.data.configuration.DC.DB_USERS;
import static com.ruslanlyalko.ll.data.configuration.DC.FIELD_FULL_NAME;
import static com.ruslanlyalko.ll.data.configuration.DC.FIELD_NAME;
import static com.ruslanlyalko.ll.data.configuration.DC.FIELD_TOKEN;

public class DataManagerImpl implements DataManager {
    private static final String TAG = "DataManager";
    private static DataManagerImpl mInstance;
    private FirebaseAuth mAuth;
    private FirebaseDatabase mDatabase;
    private MutableLiveData<User> mCurrentUserLiveData;
    private MutableLiveData<List<User>> mAllUsersListLiveData;
    private MutableLiveData<List<Lesson>> mAllLessonsListLiveData;
    private MutableLiveData<List<SettingsSalary>> mAllSettingsSalaryLiveData;
    private MutableLiveData<List<Contact>> mAllContactsListLiveData;

    private DataManagerImpl() {
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance();
    }

    public static DataManager newInstance() {
        if (mInstance == null)
            mInstance = new DataManagerImpl();
        return mInstance;
    }

    @Override
    public Task<Void> saveUser(final User user) {
        if (user.getId() == null) {
            throw new RuntimeException("user can't be empty");
        }
        return mDatabase.getReference(DB_USERS)
                .child(user.getId())
                .setValue(user);
    }

    @Override
    public MutableLiveData<User> getMyUser() {
        if (mCurrentUserLiveData != null) return mCurrentUserLiveData;
        String key = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;
        if (TextUtils.isEmpty(key)) {
            Log.w(TAG, "getMyUser: user is not logged in");
            return mCurrentUserLiveData;
        }
        mCurrentUserLiveData = new MutableLiveData<>();
        mDatabase.getReference(DB_USERS)
                .child(key)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {
                        Log.d(TAG, "getMyUser:onDataChange, Key:" + key);
                        if (mCurrentUserLiveData != null)
                            mCurrentUserLiveData.postValue(dataSnapshot.getValue(User.class));
                    }

                    @Override
                    public void onCancelled(@NonNull final DatabaseError databaseError) {
                    }
                });
        return mCurrentUserLiveData;
    }

    @Override
    public MutableLiveData<User> getUser(final String key) {
        final MutableLiveData<User> userLiveData = new MutableLiveData<>();
        if (TextUtils.isEmpty(key)) {
            Log.w(TAG, "getUser has wrong argument");
            return userLiveData;
        }
        mDatabase.getReference(DB_USERS)
                .child(key)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {
                        Log.d(TAG, "getUser:onDataChange, Key:" + key);
                        userLiveData.postValue(dataSnapshot.getValue(User.class));
                    }

                    @Override
                    public void onCancelled(@NonNull final DatabaseError databaseError) {
                    }
                });
        return userLiveData;
    }

    @Override
    public MutableLiveData<List<User>> getAllUsers() {
        if (mAllUsersListLiveData == null) {
            mAllUsersListLiveData = new MutableLiveData<>();
            mDatabase.getReference(DB_USERS)
                    .orderByChild(FIELD_FULL_NAME)
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {
                            Log.d(TAG, "getAllUsers:onDataChange");
                            List<User> list = new ArrayList<>();
                            List<User> listBlocked = new ArrayList<>();
                            for (DataSnapshot snap : dataSnapshot.getChildren()) {
                                User user = snap.getValue(User.class);
                                if (user != null) {
                                    if (user.getIsBlocked())
                                        listBlocked.add(user);
                                    else
                                        list.add(user);
                                }
                            }
                            list.addAll(listBlocked);
                            if (mAllUsersListLiveData != null)
                                mAllUsersListLiveData.postValue(list);
                        }

                        @Override
                        public void onCancelled(@NonNull final DatabaseError databaseError) {
                        }
                    });
        }
        return mAllUsersListLiveData;
    }

    @Override
    public MutableLiveData<List<Lesson>> getAllLessons() {
        if (mAllLessonsListLiveData == null) {
            mAllLessonsListLiveData = new MutableLiveData<>();
            mDatabase.getReference(DB_LESSONS)
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {
                            Log.d(TAG, "getAllLessons:onDataChange");
                            List<Lesson> lessons = new ArrayList<>();
                            for (DataSnapshot datYears : dataSnapshot.getChildren()) {
                                for (DataSnapshot datYear : datYears.getChildren()) {
                                    for (DataSnapshot datMonth : datYear.getChildren()) {
                                        for (DataSnapshot datDay : datMonth.getChildren()) {
                                            Lesson lesson = datDay.getValue(Lesson.class);
                                            if (lesson != null) {
                                                lessons.add(lesson);
                                            }
                                        }
                                    }
                                }
                            }
                            Collections.sort(lessons, (o1, o2) ->
                                    Long.compare(o2.getDateTime().getTime(), o1.getDateTime().getTime()));
                            mAllLessonsListLiveData.postValue(lessons);
                        }

                        @Override
                        public void onCancelled(@NonNull final DatabaseError databaseError) {
                        }
                    });
        }
        return mAllLessonsListLiveData;
    }

    @Override
    public MutableLiveData<List<Contact>> getAllContacts() {
        if (mAllContactsListLiveData == null) {
            mAllContactsListLiveData = new MutableLiveData<>();
            mDatabase.getReference(DB_CONTACTS)
                    .orderByChild(FIELD_NAME)
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {
                            Log.d(TAG, "getAllContacts:onDataChange");
                            List<Contact> list = new ArrayList<>();
                            List<Contact> listArchived = new ArrayList<>();
                            for (DataSnapshot snap : dataSnapshot.getChildren()) {
                                Contact contact = snap.getValue(Contact.class);
                                if (contact != null && contact.getIsArchived())
                                    listArchived.add(contact);
                                else
                                    list.add(contact);
                            }
                            list.addAll(listArchived);
                            if (mAllContactsListLiveData != null)
                                mAllContactsListLiveData.postValue(list);
                        }

                        @Override
                        public void onCancelled(@NonNull final DatabaseError databaseError) {
                        }
                    });
        }
        return mAllContactsListLiveData;
    }

    @Override
    public MutableLiveData<List<SettingsSalary>> getAllSettingsSalary() {
        if (mAllSettingsSalaryLiveData == null) {
            mAllSettingsSalaryLiveData = new MutableLiveData<>();
            mDatabase.getReference(DB_SETTINGS_SALARY)
                    .orderByChild("dateFrom/time")
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(final DataSnapshot dataSnapshot) {
                            List<SettingsSalary> settingsSalaries = new ArrayList<>();
                            for (DataSnapshot data : dataSnapshot.getChildren()) {
                                SettingsSalary settingSalary = data.getValue(SettingsSalary.class);
                                if (settingSalary != null) {
                                    settingsSalaries.add(settingSalary);
                                }
                            }
                            mAllSettingsSalaryLiveData.postValue(settingsSalaries);
                        }

                        @Override
                        public void onCancelled(final DatabaseError databaseError) {
                        }
                    });
        }
        return mAllSettingsSalaryLiveData;

    }

    @Override
    public Task<Void> changePassword(final String newPassword) {
        if (mAuth.getCurrentUser() == null) return null;
        return mAuth.getCurrentUser().updatePassword(newPassword);
    }

    @Override
    public void updateToken() {
        if (mAuth.getCurrentUser() == null) return;
        String token = FirebaseInstanceId.getInstance().getToken();
        if (token != null && !TextUtils.isEmpty(token))
            mDatabase.getReference(DB_USERS)
                    .child(mAuth.getCurrentUser().getUid())
                    .child(FIELD_TOKEN)
                    .setValue(token);
    }

    @Override
    public void logout() {
        if (mAuth.getCurrentUser() == null) return;
        String token = FirebaseInstanceId.getInstance().getToken();
        if (token != null && !TextUtils.isEmpty(token))
            mDatabase.getReference(DB_USERS)
                    .child(mAuth.getCurrentUser().getUid())
                    .child(FIELD_TOKEN)
                    .removeValue();
        mCurrentUserLiveData = null;
        mAllUsersListLiveData = null;
        mAllContactsListLiveData = null;
        mAuth.signOut();
    }
}
