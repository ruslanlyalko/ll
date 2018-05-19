package com.ruslanlyalko.ll.presentation.ui.main.clients.contacts;

import android.os.Bundle;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.ruslanlyalko.ll.R;
import com.ruslanlyalko.ll.common.Keys;
import com.ruslanlyalko.ll.common.UserType;
import com.ruslanlyalko.ll.data.configuration.DC;
import com.ruslanlyalko.ll.data.models.Contact;
import com.ruslanlyalko.ll.presentation.base.BaseFragment;
import com.ruslanlyalko.ll.presentation.ui.main.clients.OnFilterListener;
import com.ruslanlyalko.ll.presentation.ui.main.clients.contacts.adapter.ContactsAdapter;
import com.ruslanlyalko.ll.presentation.ui.main.clients.contacts.adapter.OnContactClickListener;
import com.ruslanlyalko.ll.presentation.ui.main.clients.contacts.details.ContactDetailsActivity;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import butterknife.OnTextChanged;

/**
 * Created by Ruslan Lyalko
 * on 29.01.2018.
 */
public class ContactsFragment extends BaseFragment implements OnContactClickListener {

    @BindView(R.id.list_contacts) RecyclerView mListContacts;
    @BindView(R.id.edit_filter_name) EditText mEditFilterName;
    @BindView(R.id.edit_filter_phone) EditText mEditFilterPhone;
    @BindView(R.id.image_clear) ImageView mImageClear;
    @BindView(R.id.check_box_my) CheckBox mCheckBoxMy;
    private ContactsAdapter mContactsAdapter;
    private OnFilterListener mOnFilterListener;
    private int mUserType = UserType.ADULT;
    private boolean mIsSelectable;
    private List<String> mSelectedClients;
    private String mTeacherId = "";

    public ContactsFragment() {
    }

    public static ContactsFragment newInstance(final int tabIndex, final boolean isSelectable) {
        ContactsFragment fragment = new ContactsFragment();
        Bundle args = new Bundle();
        args.putInt(Keys.Extras.EXTRA_TAB_INDEX, tabIndex);
        args.putBoolean(Keys.Extras.EXTRA_IS_SELCTABLE, isSelectable);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected void onViewReady(final Bundle savedInstanceState) {
        mContactsAdapter = new ContactsAdapter(this, getActivity(), mIsSelectable);
        if (mSelectedClients != null)
            mContactsAdapter.setSelectedCotacts(mSelectedClients);
        TextWatcher watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(final CharSequence s, final int start, final int count, final int after) {
            }

            @Override
            public void onTextChanged(final CharSequence s, final int start, final int before, final int count) {
                mImageClear.setVisibility(mEditFilterName.getText().length() > 0
                        || mEditFilterPhone.getText().length() > 0
                        ? View.VISIBLE : View.INVISIBLE);
            }

            @Override
            public void afterTextChanged(final Editable s) {
            }
        };
        mEditFilterName.addTextChangedListener(watcher);
        mEditFilterPhone.addTextChangedListener(watcher);
        setupRecycler();
        loadContacts();
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.fragment_contacts;
    }

    @Override
    protected void parseArguments() {
        if (getArguments() == null) return;
        mUserType = getArguments().getInt(Keys.Extras.EXTRA_TAB_INDEX, 0);
        mIsSelectable = getArguments().getBoolean(Keys.Extras.EXTRA_IS_SELCTABLE, false);
    }

    private void setupRecycler() {
        mListContacts.setLayoutManager(new LinearLayoutManager(getContext()));
        mListContacts.setAdapter(mContactsAdapter);
    }

    private void loadContacts() {
        Query ref = FirebaseDatabase.getInstance()
                .getReference(DC.DB_CONTACTS)
                .orderByChild("name");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {
                List<Contact> contacts = new ArrayList<>();
                for (DataSnapshot clientSS : dataSnapshot.getChildren()) {
                    Contact contact = clientSS.getValue(Contact.class);
                    if (contact != null && contact.getUserType() == mUserType) {
                        contacts.add(contact);
                    }
                }
                mContactsAdapter.setData(contacts);
                onFilterTextChanged();
            }

            @Override
            public void onCancelled(final DatabaseError databaseError) {
            }
        });
    }

    @OnTextChanged({R.id.edit_filter_name, R.id.edit_filter_phone})
    void onFilterTextChanged() {
        String name = mEditFilterName.getText().toString().trim();
        String phone = mEditFilterPhone.getText().toString().trim();
        if (name.equals(""))
            name = " ";
        if (phone.equals(""))
            phone = " ";
        mContactsAdapter.getFilter().filter(name + "/" + phone + mTeacherId);
        if (mOnFilterListener != null)
            mOnFilterListener.onFilterChanged(name, phone);
    }

    @OnCheckedChanged(R.id.check_box_my)
    void onMyChanged(boolean isChecked) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null && isChecked)
            mTeacherId = "/" + user.getUid();
        else
            mTeacherId = "";
        onFilterTextChanged();
    }

    @Override
    public void onItemClicked(final int position, ActivityOptionsCompat options) {
        startActivity(ContactDetailsActivity.getLaunchIntent(getContext(), mContactsAdapter.getItem(position)), options.toBundle());
    }

    @Override
    public void onItemsCheckedChanged(final List<String> contacts) {
        mSelectedClients = contacts;
    }

    @OnClick(R.id.image_clear)
    public void onClearClick() {
        mEditFilterName.setText("");
        mEditFilterPhone.setText("");
        //todo hide keyboard
    }

    public void updateSelected(final List<String> clients) {
        mSelectedClients = clients;
        if (mContactsAdapter != null)
            mContactsAdapter.setSelectedCotacts(clients);
    }

    public List<String> getSelected() {
        return mSelectedClients;
    }
}
