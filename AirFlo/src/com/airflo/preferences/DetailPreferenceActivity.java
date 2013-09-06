package com.airflo.preferences;

import com.airflo.FlightListActivity;
import com.airflo.R;
import android.annotation.SuppressLint;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;

/**
 * 
 * This Class is part of AirFlo.
 * 
 * It provides an Activity to handle flight detail preferences.
 * 
 * @author Florian Hauser Copyright (C) 2013
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
public class DetailPreferenceActivity extends android.support.v4.app.FragmentActivity{

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_detail_prefs);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			   getActionBar().setDisplayHomeAsUpEnabled(true);
		}		
		DetailPreferenceFragment prefFragment = new DetailPreferenceFragment();
		FragmentManager fragmentManager = getSupportFragmentManager();
		FragmentTransaction fragmentTransaction = fragmentManager
				.beginTransaction();
		fragmentTransaction.replace(android.R.id.content, prefFragment);
		fragmentTransaction.commit();

	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			Intent returnIntent = new Intent();
			returnIntent.putExtra("result", "justADummy");
			setResult(1, returnIntent);
			Intent intent = new Intent(this, FlightListActivity.class);  
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); //Resume saved State!		
			NavUtils.navigateUpTo(this, intent);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
