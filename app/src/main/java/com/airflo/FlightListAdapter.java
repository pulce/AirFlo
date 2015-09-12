package com.airflo;

import java.util.List;

import com.airflo.R;
import com.airflo.datamodel.FlightData;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

/**
 * 
 * This Class is part of AirFlo.
 * 
 * It provides an Adapter to create the flightlist.
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
public class FlightListAdapter extends ArrayAdapter<FlightListAdapterOption> {

	private Context context;
	private int id;
	private List<FlightListAdapterOption> objects;

	public FlightListAdapter(Context context, int textViewResourceId,
			List<FlightListAdapterOption> objects) {
		super(context, textViewResourceId, objects);
		this.context = context;
		id = textViewResourceId;
		this.objects = objects;
	}

	public FlightListAdapterOption getItem(int i) {
		return objects.get(i);
	}
	
	public void select(String id) {
		for (int i = 0; i < objects.size(); i++) {
			objects.get(i).makeSelected(id == FlightData.ITEMS.get(i).content.get("number") ? true : false);
		}
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
		if (v == null) {
			LayoutInflater vi = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = vi.inflate(id, null);
		}
		SharedPreferences sharedPrefs = PreferenceManager
				.getDefaultSharedPreferences(getContext());
		final FlightListAdapterOption o = objects.get(position);
		
		if (o != null) {

			TextView header = (TextView) v.findViewById(R.id.ListHeader);
			header.setTextSize(Float.valueOf(sharedPrefs.getString(
					"list_head_size", "20")));

			header.setText(o.getHeader());

			if (o.isSelected())
				v.setBackgroundColor(Color.LTGRAY);
			else
				v.setBackgroundColor(Color.WHITE);
			TextView body = (TextView) v.findViewById(R.id.ListContent);
			if (o.getData().length() == 0) {
				body.setHeight(0);
			} else {
				body.setTextSize(Float.valueOf(sharedPrefs.getString(
						"list_type_size", "16")));
				body.setText(o.getData());
			}
		}
		return v;
	}
}