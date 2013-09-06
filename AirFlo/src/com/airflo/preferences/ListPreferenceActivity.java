package com.airflo.preferences;

import com.airflo.FlightListActivity;
import com.airflo.R;
import com.airflo.datamodel.FlightData;
import com.airflo.helpers.OnlyContext;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;

/**
 * 
 * This Class is part of AirFlo.
 * 
 * It provides an Activity to handle flight list preferences.
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
public class ListPreferenceActivity extends PreferenceActivity {

	private PreferenceScreen root;

	@SuppressWarnings("deprecation")
	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			getActionBar().setDisplayHomeAsUpEnabled(true);
		}

		root = getPreferenceManager().createPreferenceScreen(
				this);
		
		//Array of sorting preference keys to be included. 
		String[] sortValues = new String[]{
				"number", 
				"date;starttime", 
				"site;date;starttime", 
				"duration",
				"maxheight",
				"maxvario",
				"starttype;date;starttime"};
		//Generate according Entry Strings.
		String[] sortEntries = new String[sortValues.length];
		for (int i = 0; i < sortEntries.length; i++)
			sortEntries[i] = sortKeyToString(sortValues[i]);
		
		//Make sorting category
		PreferenceCategory cat = new PreferenceCategory(this);
		cat.setTitle(R.string.list_pref_cat_sort_title);
		root.addPreference(cat);
		final ListPreference listPref = new ListPreference(this);
		listPref.setKey("list_pref_sort");
		listPref.setEntries(sortEntries);
		listPref.setEntryValues(sortValues);
		listPref.setDialogTitle(R.string.list_pref_sort_criterium);
		listPref.setSummary(sortKeyToString(root.getSharedPreferences().getString("list_pref_sort", "number")));
		listPref.setTitle(R.string.list_pref_sort_criterium);
		cat.addPreference(listPref);
		listPref.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			public boolean onPreferenceChange(Preference preference,
					Object newValue) {
				listPref.setSummary(sortKeyToString((String) newValue));
				return true;
			}
		});
		final ListPreference orderPref = new ListPreference(this);
		orderPref.setKey("list_pref_order");
		orderPref.setEntries(new String[]{OnlyContext.rString("list_pref_sort_ascending"), OnlyContext.rString("list_pref_sort_decending")});
		orderPref.setEntryValues(new String[]{"list_pref_sort_ascending", "list_pref_sort_decending"});
		orderPref.setDialogTitle(R.string.list_pref_sort_sequence);
		orderPref.setSummary(OnlyContext.rString(root.getSharedPreferences().getString("list_pref_order", "list_pref_sort_decending")));
		orderPref.setTitle(R.string.list_pref_sort_sequence);
		cat.addPreference(orderPref);
		orderPref.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			public boolean onPreferenceChange(Preference preference,
					Object newValue) {
				orderPref.setSummary(OnlyContext.rString((String) newValue));
				return true;
			}
		});
		
		//Make item categories
		addManyListItems(R.string.list_pref_cat1_title, "listhead", 3);
		addManyListItems(R.string.list_pref_cat2_title, "listsub", 3);
	

		
		this.setPreferenceScreen(root);
	}
	
	/**
	 * Method to get the Android resource string corresponding to a pref value.
	 * @param sortkey
	 * @return String
	 */
	private static String sortKeyToString(String sortkey) {
		if (!sortkey.contains(";"))
			return OnlyContext.rString(sortkey);
		String[] sortkeys = sortkey.split(";");
		String theString = OnlyContext.rString(sortkeys[0]);
		for (int i = 1; i < sortkeys.length; i++)
			theString += ("/" + OnlyContext.rString(sortkeys[i]));
		return theString;
	}

	/**
	 * Add three slider preferences to a category.
	 * @param catTitle
	 * @param key
	 * @param no
	 */
	private void addManyListItems(int catTitle, String key, int no) {
		PreferenceCategory cat = new PreferenceCategory(this);
		cat.setTitle(catTitle);
		root.addPreference(cat);
		String title = getString(R.string.list_pref_hint_selector);
		for (int i = 1; i <= no; i++) {
			String akey = key + i;
			String atitle = title + i;
			addOneListItem(cat, akey, atitle);
		}
	}

	/**
	 * Add one slider preference to the category.
	 * @param cat
	 * @param key
	 * @param title
	 */
	private void addOneListItem(PreferenceCategory cat, String key, String title) {
		final ListPreference listPref = new ListPreference(this);
		listPref.setKey(key);
		listPref.setEntries(FlightData.identis.getPrefListChars(true));
		listPref.setEntryValues(FlightData.identis.getPrefListChars(false));
		listPref.setDialogTitle(title);
		listPref.setSummary(FlightData.identis.getIdenti(root.getSharedPreferences().getString(key, "empty")).getStringRep());
		listPref.setTitle(title);
		cat.addPreference(listPref);
		listPref.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			public boolean onPreferenceChange(Preference preference,
					Object newValue) {
				listPref.setSummary(FlightData.identis.getIdenti((String) newValue).getStringRep());
				return true;
			}
		});
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			Intent returnIntent = new Intent();
			returnIntent.putExtra("result", "justADummy");
			setResult(1, returnIntent);
			Intent intent = new Intent(this, FlightListActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // Resume saved
																// State!
			NavUtils.navigateUpTo(this, intent);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
