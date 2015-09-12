package com.airflo.datamodel;

import java.util.Collections;
import java.util.Comparator;
import android.annotation.SuppressLint;
import com.airflo.datamodel.FlightData.FlightDataItem;

/**
 * 
 * This Class is part of AirFlo.
 * 
 * It provides a flexible Comparator to sort FlightDataItems.
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
@SuppressLint("SimpleDateFormat")
public class FlightDataComparer {

	private Comparator<FlightDataItem> compi;

	public FlightDataComparer() {
	}

	public Comparator<FlightDataItem> getCompi() {
		return compi;
	}

	public FlightDataComparer sortListBy(final String compKey) {

		compi = new Comparator<FlightDataItem>() {
			public int compare(FlightDataItem o1, FlightDataItem o2) {
				String t1 = o1.sortContent.get(compKey);
				String t2 = o2.sortContent.get(compKey);
				return (t1.compareTo(t2));
			}
			
		};
		Collections.sort(FlightData.ITEMS, compi);
		return this;
	}

	/**
	 * Method to sort list by several criteria.
	 * @param keys
	 * Keys
	 * @return this FDC
	 */
	public FlightDataComparer sortListBy(final String[] keys) {
		for (int i = keys.length - 1; i >= 0; i--)
			sortListBy(keys[i]);
		return this;
	}

	/**
	 * Method to reverse sorting direction.
	 * @param ascending
	 * Ascending
	 */
	public void reverse(boolean ascending) {
		if (!ascending)
			Collections.reverse(FlightData.ITEMS);
	}

}
