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
import com.airflo.helpers.CheckForUpdates;
import com.airflo.helpers.DrawerListAdapter;
import com.airflo.helpers.NavItem;
import com.airflo.preferences.DetailPreferenceFragment;
import com.airflo.preferences.FilePreferenceFragment;
import com.airflo.preferences.ListPreferenceFragment;
import com.airflo.preferences.TypePreferenceFragment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

public class FlightListActivity extends AppCompatActivity implements
		FlightListFragment.Callbacks, SharedPreferences.OnSharedPreferenceChangeListener {

	private SharedPreferences sharedPrefs;

	public static final String APPURL = "https://github.com/pulce/AirFlo/releases/latest";
	public static final String VERSIONID = "1.0.0";
	public static final int TYPE_PREF = 1;
	public static final int FILE_CHOOSER = 2;
	public static final int LIST_SET = 3;
	public static final int LIST_DETAIL = 4;
	public static final int FLIGHT_DETAIL = 5;
	private static final String LIST_STATE = "listState";
	private Parcelable savedListState = null;
	private boolean mTwoPane;
	private FlightDetailFragment flightDetailFragment;

	TextView mDrawerTitle;
	ListView mDrawerList;
	RelativeLayout mDrawerPane;
	private ActionBarDrawerToggle mDrawerToggle;
	private DrawerLayout mDrawerLayout;
	ArrayList<NavItem> mNavItems = new ArrayList<>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_flight_list);

		sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

		makeNonExistingPref("listhead1", "number");
		makeNonExistingPref("listhead2", "site");
		makeNonExistingPref("listsub1", "date");
		makeNonExistingPref("listsub2", "starttime");


		if (getSupportActionBar() != null) {
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);
			getSupportActionBar().setHomeButtonEnabled(true);
		}

		if (findViewById(R.id.flight_detail_container) != null) {
			mTwoPane = true;
			((FlightListFragment) getSupportFragmentManager().findFragmentById(
					R.id.flight_list)).setActivateOnItemClick(true);
		}

		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
		mDrawerTitle = (TextView) findViewById(R.id.drawer_title);
		mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.drawer_open, R.string.drawer_close) {
			public void onDrawerOpened(View drawerView) {
				super.onDrawerOpened(drawerView);
				invalidateOptionsMenu();
			}

			public void onDrawerClosed(View view) {
				super.onDrawerClosed(view);
				invalidateOptionsMenu();
			}
		};
		mDrawerToggle.setDrawerIndicatorEnabled(true);
		mDrawerLayout.setDrawerListener(mDrawerToggle);

		mDrawerPane = (RelativeLayout) findViewById(R.id.drawerPane);
		mDrawerList = (ListView) findViewById(R.id.navList);
		final DrawerListAdapter adapter = new DrawerListAdapter(this, mNavItems);
		mDrawerList.setAdapter(adapter);
		mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				onMenuItemSelected(mNavItems.get(position).getId());
			}
		});

		mNavItems.add(new NavItem(R.id.action_load, R.string.main_menu_loadbook, R.drawable.ic_input_grey600_36dp));
		mNavItems.add(new NavItem(R.id.action_typesize, R.string.main_menu_typesize, R.drawable.ic_mode_edit_grey600_36dp));
		mNavItems.add(new NavItem(R.id.action_preflist, R.string.main_menu_preflist, R.drawable.ic_view_list_grey600_36dp));
		mNavItems.add(new NavItem(R.id.action_prefdetail, R.string.main_menu_prefdetail, R.drawable.ic_description_grey600_36dp));
		mNavItems.add(new NavItem(R.id.action_about, R.string.about, R.drawable.ic_info_outline_grey600_36dp));
		mNavItems.add(new NavItem(R.id.action_update, R.string.check_for_updates, R.drawable.ic_file_download_grey600_36dp));

		if (!ParseFlightBook.isBookLoaded()) {
			tryToLoadBook();
		}
		setTitle(getString(R.string.app_name));


	}
	
	private void makeNonExistingPref(String key, String value) {
		Editor edit = sharedPrefs.edit();
		if (sharedPrefs.getString(key, null) == null)
			edit.putString(key, value);
		edit.apply();
	}

	/**
	 * Method needed to restore scroll position
	 */
	@Override
	protected void onRestoreInstanceState(@NonNull Bundle state) {
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
		sharedPrefs.registerOnSharedPreferenceChangeListener(this);
		refreshView();

	}

	@Override
	protected void onPause() {
		super.onPause();
		sharedPrefs.unregisterOnSharedPreferenceChangeListener(this);
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


	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		mDrawerToggle.syncState();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		mDrawerToggle.onConfigurationChanged(newConfig);
	}


	/**
	 * Method will start activity for result.
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return mDrawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
	}


	private boolean onMenuItemSelected(int id) {
		mDrawerLayout.closeDrawer(Gravity.START);
		switch (id) {
			case R.id.action_load:
				if (mTwoPane) {
					getSupportFragmentManager().beginTransaction()
							.replace(R.id.flight_detail_container, new FilePreferenceFragment()).commit();
				} else {
					Intent fileChooser = new Intent(getApplicationContext(),
							FragActivity.class).putExtra("FragType", FILE_CHOOSER);
					startActivity(fileChooser);
				}
				break;
			case R.id.action_typesize:
				if (mTwoPane) {
					getSupportFragmentManager().beginTransaction()
							.replace(R.id.flight_detail_container, new TypePreferenceFragment()).commit();
					setTitle(getString(R.string.title_type_preference_activity));
				} else {
					Intent typeSetActivity = new Intent(getApplicationContext(),
							FragActivity.class).putExtra("FragType", TYPE_PREF);
					startActivity(typeSetActivity);
				}
				break;
			case R.id.action_preflist:
				if (mTwoPane) {
					getSupportFragmentManager().beginTransaction()
							.replace(R.id.flight_detail_container, new ListPreferenceFragment()).commit();
					setTitle(getString(R.string.title_list_preference_activity));
				} else {
					Intent listSetActivity = new Intent(getApplicationContext(),
							FragActivity.class).putExtra("FragType", LIST_SET);
					startActivity(listSetActivity);
				}
				break;
			case R.id.action_prefdetail:
				if (mTwoPane) {
					getSupportFragmentManager().beginTransaction()
							.replace(R.id.flight_detail_container, new DetailPreferenceFragment()).commit();
					setTitle(getString(R.string.title_flight_detail_prefs));
				} else {
					Intent listDetailActivity = new Intent(getApplicationContext(),
							FragActivity.class).putExtra("FragType", LIST_DETAIL);
					startActivity(listDetailActivity);
				}
				break;
			case R.id.action_about:
				AboutDialog.makeDialog(this, VERSIONID);
				break;
			case R.id.action_update:
				new CheckForUpdates(this).execute(VERSIONID);
				break;
			default:
		}
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
			setTitle(R.string.app_name);
		} else {
			// In single-pane mode, simply start the detail activity
			// for the selected item ID.
			Intent detailIntent = new Intent(this, FragActivity.class).putExtra("FragType", FLIGHT_DETAIL);
			detailIntent.putExtra(FlightDetailFragment.ARG_ITEM_ID, id);
			startActivity(detailIntent);
		}
	}
	
	/**
	 * Method will advise the fragment to flush the data adapter.
	 */
	public void refreshView() {
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
					FragActivity.class).putExtra("FragType", FILE_CHOOSER);
			startActivityForResult(fileChooser, FILE_CHOOSER);
		} else {
			String[] path = sharedPrefs.getString("flightBookName",
					"flightbook.xml").split("/");
			//this.setTitle(getString(R.string.app_name) + ": "
			//		+ path[path.length - 1]);
			mDrawerTitle.setText(path[path.length - 1]);
		}
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		if (key.equals("flightBookName")) {
			FlightData.reset();
			tryToLoadBook();
		}
		refreshView();
	}
}
