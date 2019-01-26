package com.ruslanlyalko.ll.presentation.base;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.ruslanlyalko.ll.data.models.User;

import butterknife.ButterKnife;

/**
 * Created by Ruslan Lyalko
 * on 19.05.2018.
 */
public abstract class BaseFragment extends Fragment {

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(getLayoutResource(), container, false);
        ButterKnife.bind(this, rootView);
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        parseArguments();
        onViewReady(savedInstanceState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        try {
            Activity activity = getActivity();
            if(activity != null) {
                final InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
                if(imm != null && getView() != null) {
                    imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
                }
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    protected FirebaseUser getFirebaseUser() {
        return FirebaseAuth.getInstance().getCurrentUser();
    }

    protected User getCurrentUser() {
        return getBaseActivity().getCurrentUser();
    }

    protected DatabaseReference getDB(String db) {
        return FirebaseDatabase.getInstance().getReference(db);
    }

    protected BaseActivity getBaseActivity() {
        if(getActivity() == null)
            throw new RuntimeException("activity destroyed");
        return (BaseActivity) getActivity();
    }

    protected abstract void onViewReady(final Bundle savedInstanceState);

    @LayoutRes
    protected abstract int getLayoutResource();

    protected void parseArguments() {}
}
