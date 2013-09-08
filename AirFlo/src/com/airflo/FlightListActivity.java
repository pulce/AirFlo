package com.airflo;

/**
 * 
 * This Class is the main class of AirFlo.
 * 
 * It provides the main Activity representing a list of Flights. This activity has different
 * presentations for handset and tablet-size devices. On handsets, the activity
 * presents a list of items, which when touched, lead to a
 * {@link FlightDetailActivity} representing item details. On tablets, the
 * activity presents the list of items and item details side-by-side using two
 * vertical panes.
 * <p>
 * The activity makes heavy use of fragments. The list of items is a
 * {@link FlightListFragment} and the item details (if present) is a
 * {@link FlightDetailFragment}.
 * <p>
 * This activity also implements the required
 * {@link FlightListFragment.Callbacks} interface to listen for item selections.
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
import com.airflo.datamodel.FlightData;
import com.airflo.datamodel.ParseFlightBook;
import com.airflo.preferences.DetailPreferenceActivity;
import com.airflo.preferences.FilePreferenceActivity;
import com.airflo.preferences.ListPreferenceActivity;
import com.airflo.preferences.TypePreferenceActivity;
import com.airflo.R;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class FlightListActivity extends FragmentActivity implements
		FlightListFragment.Callbacks {

	private SharedPreferences sharedPrefs;
	private static final String VERSIONID = "0.7";
	private static final int TYPE_PREF = 1;
	private static final int FILE_CHOOSER = 2;
	private static final int LIST_SET = 3;
	private static final int LIST_DETAIL = 4;
	private static final String LIST_STATE = "listState";
	private Parcelable savedListState = null;
	private boolean mTwoPane;
	private FlightDetailFragment flightDetailFragment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		makeNonExistingPref("listhead1", "number");
		makeNonExistingPref("listhead2", "site");
		makeNonExistingPref("listsub1", "date");
		makeNonExistingPref("listsub2", "starttime");
				
		if (!ParseFlightBook.isBookLoaded()) {
			tryToLoadBook();
		}
		setContentView(R.layout.activity_flight_list);
		if (findViewById(R.id.flight_detail_container) != null) {
			// The detail container view will be present only in the
			// large-screen layouts (res/values-large and
			// res/values-sw600dp). If this view is present, then the
			// activity should be in two-pane mode.
			mTwoPane = true;

			// In two-pane mode, list items should be given the
			// 'activated' state when touched.
			((FlightListFragment) getSupportFragmentManager().findFragmentById(
					R.id.flight_list)).setActivateOnItemClick(true);
		}
	}
	
	private void makeNonExistingPref(String key, String value) {
		Editor edit = sharedPrefs.edit();
		if (sharedPrefs.getString(key, null) == null)
			edit.putString(key, value);
		edit.commit();
	}

	/**
	 * Method needed to restore scroll position
	 */
	@Override
	protected void onRestoreInstanceState(Bundle state) {
		super.onRestoreInstanceState(state);
		savedListState = state.getParcelable(LIST_STATE);
	}

	/**
	 * Method needed to restore scroll position
	 */
	@Override
	protected void onResume() {
		super.onResume();
		if (savedListState != null) {
			((FlightListFragment) getSupportFragmentManager().findFragmentById(
					R.id.flight_list)).getListView().onRestoreInstanceState(
					savedListState);
		}
		savedListState = null;
	}

	/**
	 * Method needed to restore scroll position
	 */
	@Override
	protected void onSaveInstanceState(Bundle state) {
		super.onSaveInstanceState(state);
		savedListState = ((FlightListFragment) getSupportFragmentManager()
				.findFragmentById(R.id.flight_list)).getListView()
				.onSaveInstanceState();
		state.putParcelable(LIST_STATE, savedListState);
	}

	/**
	 * Method will start activity for result.
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_load:
			Intent fileChooser = new Intent(getApplicationContext(),
					FilePreferenceActivity.class);
			startActivityForResult(fileChooser, FILE_CHOOSER);
			break;
		case R.id.action_typesize:
			Intent typeSetActivity = new Intent(getApplicationContext(),
					TypePreferenceActivity.class);
			startActivityForResult(typeSetActivity, TYPE_PREF);
			break;
		case R.id.action_preflist:
			Intent listSetActivity = new Intent(getApplicationContext(),
					ListPreferenceActivity.class);
			startActivityForResult(listSetActivity, LIST_SET);
			break;
		case R.id.action_prefdetail:
			Intent listDetailActivity = new Intent(getApplicationContext(),
					DetailPreferenceActivity.class);
			startActivityForResult(listDetailActivity, LIST_DETAIL);
			break;
		case R.id.action_about:
			AboutDialog.makeDialog(this, VERSIONID);
			break;
		default:
			break;
		}
		return true;
	}

	/**
	 * Return here with result from activity
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case FILE_CHOOSER:
			if (resultCode == RESULT_OK) {
				Editor edit = sharedPrefs.edit();
				edit.putString("flightBookName", data.getStringExtra("result"));
				edit.commit();
				FlightData.reset();
				tryToLoadBook();
				refreshView();
				break;
			}
		}
		refreshView();
	}

	/**
	 * Create the main menu
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.mainmenu, menu);
		return true;
	}

	/**
	 * Callback method from {@link FlightListFragment.Callbacks} indicating that
	 * the item with the given ID was selected.
	 */
	@Override
	public void onItemSelected(String id) {

		if (mTwoPane) {
			// In two-pane mode, show the detail view in this activity by
			// adding or replacing the detail fragment using a
			// fragment transaction.
			FlightListFragment frag = (FlightListFragment) getSupportFragmentManager().findFragmentById(
					R.id.flight_list);
			frag.selectOnAdapter(id);
			
			Bundle arguments = new Bundle();
			arguments.putString(FlightDetailFragment.ARG_ITEM_ID, id);
			flightDetailFragment = new FlightDetailFragment();
			flightDetailFragment.setArguments(arguments);
			getSupportFragmentManager()
					.beginTransaction()
					.replace(R.id.flight_detail_container, flightDetailFragment)
					.commit();

		} else {
			// In single-pane mode, simply start the detail activity
			// for the selected item ID.
			Intent detailIntent = new Intent(this, FlightDetailActivity.class);
			detailIntent.putExtra(FlightDetailFragment.ARG_ITEM_ID, id);
			startActivity(detailIntent);
		}
	}
	
	/**
	 * Method will advise the fragment to flush the data adapter.
	 */
	private void refreshView() {
		FlightListFragment fragment = (FlightListFragment) getSupportFragmentManager()
				.findFragmentById(R.id.flight_list);
		if (fragment != null && fragment.isInLayout()) {
			fragment.refreshAdapter();
		}
		if (flightDetailFragment != null && flightDetailFragment.isVisible())
			flightDetailFragment.refresh();
	}

	/**
	 * Method will try to load the book referred to in the preferences. If that
	 * fails, it will start the filechooser activity.
	 */
	private void tryToLoadBook() {
		if (!ParseFlightBook.loadBook(sharedPrefs.getString("flightBookName",
				Environment.getExternalStorageDirectory().getPath() + "flightbookexample.xml"))) {
			Intent fileChooser = new Intent(getApplicationContext(),
					FilePreferenceActivity.class);
			startActivityForResult(fileChooser, FILE_CHOOSER);
		} else {
			String[] path = sharedPrefs.getString("flightBookName",
					"flightbook.xml").split("/");
			this.setTitle(getString(R.string.app_name) + ": "
					+ path[path.length - 1]);
		}
	}
}
