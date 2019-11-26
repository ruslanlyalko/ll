package com.ruslanlyalko.ll.presentation.ui.main.clients.contacts.adapter;

import androidx.core.app.ActivityOptionsCompat;

import java.util.List;

/**
 * Created by Ruslan Lyalko
 * on 12.11.2017.
 */

public interface OnContactClickListener {

    void onItemClicked(int position, ActivityOptionsCompat options);

    void onItemsCheckedChanged(List<String> contacts);
}
