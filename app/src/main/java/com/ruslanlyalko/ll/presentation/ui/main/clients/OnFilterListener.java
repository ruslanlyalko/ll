package com.ruslanlyalko.ll.presentation.ui.main.clients;

import com.ruslanlyalko.ll.common.UserType;
import com.ruslanlyalko.ll.data.models.Contact;

import java.util.List;

/**
 * Created by Ruslan Lyalko
 * on 02.02.2018.
 */

public interface OnFilterListener {

    void onFilterChanged(String name, String phone);
    void onCheckedChanged(List<String> selected, UserType userType);
}
