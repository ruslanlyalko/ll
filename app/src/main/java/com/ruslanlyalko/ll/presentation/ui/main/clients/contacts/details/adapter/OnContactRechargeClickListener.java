package com.ruslanlyalko.ll.presentation.ui.main.clients.contacts.details.adapter;

import com.ruslanlyalko.ll.data.models.ContactRecharge;

/**
 * Created by Ruslan Lyalko
 * on 12.11.2017.
 */

public interface OnContactRechargeClickListener {

    void onRemoveClicked(ContactRecharge contactRecharge);

    void onEditClicked(ContactRecharge contactRecharge);
}
