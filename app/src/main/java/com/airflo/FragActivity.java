package com.airflo;

import com.airflo.preferences.DetailPreferenceFragment;
import com.airflo.preferences.FilePreferenceFragment;
import com.airflo.preferences.ListPreferenceFragment;
import com.airflo.preferences.TypePreferenceFragment;

import android.annotation.SuppressLint;
import android.support.v4.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

/**
 * 
 * This Class is part of AirFlo.
 * 
 * It provides an Activity to handle flight detail preferences.
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
public class FragActivity extends AppCompatActivity {

	private Fragment frag = null;

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_frag);
		if (getSupportActionBar() != null) {
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);
			getSupportActionBar().setHomeButtonEnabled(true);
		}
		int thisType = getIntent().getIntExtra("FragType", 1);
		switch (thisType) {
			case FlightListActivity.LIST_DETAIL:
				setTitle(getString(R.string.title_flight_detail_prefs));
				frag = new DetailPreferenceFragment();
				break;
			case FlightListActivity.FILE_CHOOSER:
				setTitle(getString(R.string.title_file_preferece_activity));
				frag = new FilePreferenceFragment();
				break;
			case FlightListActivity.LIST_SET:
				setTitle(getString(R.string.title_list_preference_activity));
				frag = new ListPreferenceFragment();
				break;
			case FlightListActivity.TYPE_PREF:
				setTitle(getString(R.string.title_type_preference_activity));
				frag = new TypePreferenceFragment();
				break;
			case FlightListActivity.FLIGHT_DETAIL:
				setTitle(getString(R.string.title_flight_detail_activity));
				Bundle arguments = new Bundle();
				arguments.putString(FlightDetailFragment.ARG_ITEM_ID, getIntent()
						.getStringExtra(FlightDetailFragment.ARG_ITEM_ID));
				frag = new FlightDetailFragment();
				frag.setArguments(arguments);
				break;
		}
		getSupportFragmentManager().beginTransaction().replace(android.R.id.content, frag).commit();
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
