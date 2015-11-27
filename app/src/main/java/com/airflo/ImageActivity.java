package com.airflo;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import com.airflo.helpers.CustomViewPager;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.io.File;
import java.util.ArrayList;

/**
 *
 * This Class is part of Airflo.
 *
 * @author Florian Hauser Copyright (C) 2015
 *
 *         This program is free software: you can redistribute it and/or modify
 *         it under the terms of the GNU General Public License as published by
 *         the Free Software Foundation, either version 3 of the License, or (at
 *         your option) any later version.
 *
 *         This program is distributed in the hope that it will be useful, but
 *         WITHOUT ANY WARRANTY; without even the implied warranty of
 *         MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *         General Public License for more details.
 *
 *         You should have received a copy of the GNU General Public License
 *         along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

public class ImageActivity extends AppCompatActivity implements ImageFragment.SwipeSwitcher{

    private CustomViewPager mPager;
    private ArrayList<String> urls;
    public static int maxZoomPix;
    public static DiskCacheStrategy diskCacheStrategy = DiskCacheStrategy.ALL;

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);
        urls = getIntent().getStringArrayListExtra(FlightDetailFragment.PICURLS);
        mPager = (CustomViewPager) findViewById(R.id.station_container);
        PagerAdapter mPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
        mPager.setAdapter(mPagerAdapter);
        mPager.setCurrentItem(0);
        mPagerAdapter.notifyDataSetChanged();

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        maxZoomPix = Integer.parseInt(sharedPrefs.getString("list_pic_size", "3840"));
        String strat = sharedPrefs.getString("list_pic_cache", "Full");
        if (strat.equals("Full")) diskCacheStrategy = DiskCacheStrategy.ALL;
        if (strat.equals("Source")) diskCacheStrategy = DiskCacheStrategy.SOURCE;
        if (strat.equals("Display")) diskCacheStrategy = DiskCacheStrategy.RESULT;
        if (strat.equals("None")) diskCacheStrategy = DiskCacheStrategy.NONE;

        Log.d("Max Picsize:", "" + maxZoomPix);
        Log.d("Pref:", "" + sharedPrefs.getString("list_pic_size", "1"));
        String fileName = sharedPrefs.getString("flightBookName",
                Environment.getExternalStorageDirectory().getPath() + "flightbookexample.xml");
        File file = new File(fileName);
        if (!file.exists()) {
            Log.e("Flightbook", "File does not exist.");
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        Intent returnIntent = new Intent();
        returnIntent.putExtra("selected", mPager.getCurrentItem());
        setResult(RESULT_OK, returnIntent);
        super.onBackPressed();
    }

    @Override
    public void swichSwipeAbility(boolean able) {
        if (mPager!=null) {
            mPager.setPagingEnabled(able);
        }
    }

    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
        public ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            Bundle bundle = new Bundle();
            if (position >= urls.size())
                position = 0;
            bundle.putInt("position", position);
            bundle.putString("urrrl", urls.get(position));
            ImageFragment fragment = new ImageFragment();
            fragment.setArguments(bundle);
            return fragment;

        }

        @Override
        public int getCount() {
            return urls.size();
        }
    }
}