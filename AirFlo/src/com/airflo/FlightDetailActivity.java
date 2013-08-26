package com.airflo;

import com.airflo.R;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;

/**
 * 
 * This Class is part of AirFlo.
 * 
 * An activity representing a single Flight detail screen. This activity is only
 * used on handset devices. On tablet-size devices, item details are presented
 * side-by-side with a list of items in a {@link FlightListActivity}.
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

public class FlightDetailActivity extends FragmentActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_flight_detail);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		if (savedInstanceState == null) {
			Bundle arguments = new Bundle();
			arguments.putString(FlightDetailFragment.ARG_ITEM_ID, getIntent()
					.getStringExtra(FlightDetailFragment.ARG_ITEM_ID));
			FlightDetailFragment fragment = new FlightDetailFragment();
			fragment.setArguments(arguments);
			getSupportFragmentManager().beginTransaction()
					.add(R.id.flight_detail_container, fragment).commit();
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			Intent intent = new Intent(this, FlightListActivity.class);  
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); //Resume saved State!		
			NavUtils.navigateUpTo(this, intent);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
