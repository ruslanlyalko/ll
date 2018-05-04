package com.ruslanlyalko.ll.presentation.ui.main.rooms;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.ruslanlyalko.ll.R;
import com.ruslanlyalko.ll.presentation.base.BaseActivity;

import butterknife.BindView;

public class RoomsTabActivity extends BaseActivity {

    @BindView(R.id.container) ViewPager mViewPager;
    @BindView(R.id.tabs) TabLayout tabLayout;

    public static Intent getLaunchIntent(final Context launchIntent) {
        return new Intent(launchIntent, RoomsTabActivity.class);
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_mk_tab;
    }

    @Override
    protected void setupView() {
        initToolbar();
        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(sectionsPagerAdapter);
        tabLayout.setupWithViewPager(mViewPager);
    }

    private void initToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_mk_tab, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }
        if (id == R.id.action_plan) {
            //todo set date
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public static class PlaceholderFragment extends Fragment {

        private static final String ARG_SECTION_NUMBER = "section_number";

        public PlaceholderFragment() {
        }

        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_mk_tab, container, false);
            String currentRoom = null;
            switch (getArguments().getInt(ARG_SECTION_NUMBER)) {
                case 0:
                    currentRoom = (getString(R.string.tab_room_1));
                    break;
                case 1:
                    currentRoom = (getString(R.string.tab_room_2));
                    break;
                case 2:
                    currentRoom = (getString(R.string.tab_room_3));
                case 3:
                    currentRoom = (getString(R.string.tab_room_4));
                    break;
            }
            return rootView;
        }
    }

    private class SectionsPagerAdapter extends FragmentPagerAdapter {

        SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return PlaceholderFragment.newInstance(position);
        }

        @Override
        public int getCount() {
            return 4;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return getString(R.string.tab_room_1);
                case 1:
                    return getString(R.string.tab_room_2);
                case 2:
                    return getString(R.string.tab_room_3);
                case 3:
                    return getString(R.string.tab_room_4);
            }
            return null;
        }
    }
}
