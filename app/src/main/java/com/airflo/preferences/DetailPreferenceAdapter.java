package com.airflo.preferences;

import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;

import com.airflo.datamodel.Identi;
import com.airflo.helpers.OnlyContext;
import com.airflo.R;

/**
 * 
 * This Class is part of AirFlo.
 * 
 * It provides an adapter for the DetailPrefFragment.
 * It refers to all available keys and stores user preferences accordingly.
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
public class DetailPreferenceAdapter extends ArrayAdapter<Identi> {

	private Context context;
	private int id;
	private List<Identi> items;

	public DetailPreferenceAdapter(Context context, int textViewResourceId,
			List<Identi> objects) {
		super(context, textViewResourceId, objects);
		this.context = context;
		id = textViewResourceId;
		items = objects;
	}

	public Identi getItem(int i) {
		return items.get(i);
	}

	/**
	 * This method will add a checkbox to the given view, containing one
	 * preference for a certain list item.
	 */
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = convertView;
		if (view == null) {
			LayoutInflater vi = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			view = vi.inflate(id, null);
		}
		final Identi identi = items.get(position);
		if (identi != null) {
			CheckBox checkMe = (CheckBox) view.findViewById(R.id.DetailPrefCheck01);
			final String prefKey = "detailListPref" + identi.getKey();
			final SharedPreferences prefOfBox = PreferenceManager.getDefaultSharedPreferences(OnlyContext.getContext());
			checkMe.setChecked(prefOfBox.getBoolean(prefKey, true));
			if (checkMe != null) {
				checkMe.setText(identi.getStringRep());
				checkMe.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						Editor edit = prefOfBox.edit();
						edit.putBoolean(prefKey, ((CheckBox) v).isChecked());
						edit.commit();
					}
				});
			}

		}
		return view;
	}
}