package com.ruslanlyalko.ll.data;

import android.arch.lifecycle.MutableLiveData;

import com.google.android.gms.tasks.Task;
import com.ruslanlyalko.ll.data.models.Contact;
import com.ruslanlyalko.ll.data.models.User;

import java.util.Date;
import java.util.List;

/**
 * Created by Ruslan Lyalko
 * on 05.09.2018.
 */
public interface DataManager {

    //Users
    Task<Void> saveUser(User user);

    MutableLiveData<User> getMyUser();

    MutableLiveData<User> getUser(String key);

    MutableLiveData<List<User>> getAllUsers();

    Task<Void> changePassword(String newPassword);

    void updateToken();

    void logout();

    // Contacts
    MutableLiveData<List<Contact>> getAllContacts();

}
