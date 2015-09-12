package com.airflo;

import android.support.annotation.NonNull;

import java.util.Locale;

/**
 * 
 * This Class is part of AirFlo.
 * 
 * It options for the FlightListAdapter. They consist of a header (1st line in
 * the cell) and data (2nd line).
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
public class FlightListAdapterOption implements
		Comparable<FlightListAdapterOption> {
	private String header;
	private String data;
	private boolean selected;

	public FlightListAdapterOption(String header, String data) {
		this.header = header;
		this.data = data;
	}

	public String getData() {
		return data;
	}

	public String getHeader() {
		return header;
	}
	
	public void makeSelected(boolean sel) {
		this.selected = sel;
	}
	
	public boolean isSelected() {
		return selected;
	}

	@Override
	public int compareTo(@NonNull FlightListAdapterOption o) {
		if (this.header != null)
			return this.header.toLowerCase(Locale.getDefault()).compareTo(
					o.getHeader().toLowerCase(Locale.getDefault()));
		else
			throw new IllegalArgumentException();
	}
}
