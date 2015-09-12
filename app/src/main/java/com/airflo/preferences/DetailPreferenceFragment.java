package com.airflo.preferences;

import java.util.List;

import com.airflo.datamodel.FlightData;
import com.airflo.datamodel.Identi;
import com.airflo.R;

import android.os.Bundle;
import android.support.v4.app.ListFragment;

/**
 * 
 * This Class is part of AirFlo.
 * 
 * It provides the Fragment for flightdetail preferences.
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
public class DetailPreferenceFragment extends ListFragment {

	DetailPreferenceAdapter adapter;

	public DetailPreferenceFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		List<Identi> adList = FlightData.identis.getIdentis();
		adList.set(0, new Identi("Empty",
				getString(R.string.detail_pref_empty), "", false, ""));
		adapter = new DetailPreferenceAdapter(getActivity(),
				R.layout.detail_pref_view, adList);
		this.setListAdapter(adapter);
	}
}
