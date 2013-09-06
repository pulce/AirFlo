package com.airflo.datamodel;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
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
	private DateFormat timeParser = new SimpleDateFormat("HH:mm:ss");
	private DateFormat timeFormatter = new SimpleDateFormat("HHmmss");
	private DateFormat dateParser = new SimpleDateFormat("dd.MM.yyyy");
	private DateFormat dateFormatter = new SimpleDateFormat("yyyyMMdd");

	public FlightDataComparer() {
	}

	public Comparator<FlightDataItem> getCompi() {
		return compi;
	}

	public FlightDataComparer sortListBy(final String compKey) {
		final String compType = FlightData.identis.getIdenti(compKey)
				.getCompType();

		compi = new Comparator<FlightDataItem>() {
			public int compare(FlightDataItem o1, FlightDataItem o2) {
				String t1 = (transform(o1));
				String t2 = (transform(o2));
				return (t1.compareTo(t2));
			}
			
			/**
			 * This method transforms each string into another sortable string.
			 * @param item
			 * @return sortable String
			 */
			private String transform(FlightDataItem item) {
				String itemData = item.getFromKey(compKey);
				// if itemData
				if (itemData.length() == 0 || itemData == null)
					return "";
				if (compType.equals("string"))
					return itemData;
				if (compType.equals("int")) {
					try {
						itemData = itemData.replaceAll("[^0-9.]", "");
						Double val = Double.valueOf(itemData);
						DecimalFormat df = new DecimalFormat(
								"00000000000000.000");
						return df.format(val);
					} catch (Exception e) {
						return "";
					}
				}
				if (compType.equals("date")) {
					try {
						Date dt = (Date) dateParser.parse(itemData);
						return dateFormatter.format(dt);
					} catch (ParseException e) {
						return "";
					}
				}
				if (compType.equals("time")) {
					try {
						Date dt = (Date) timeParser.parse(itemData);
						return timeFormatter.format(dt);
					} catch (ParseException e) {
						return "";
					}
				}
				return "";
			}
		};
		Collections.sort(FlightData.ITEMS, compi);
		return this;
	}

	/**
	 * Method to sort list by several criteria.
	 * @param keys
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
	 */
	public void reverse(boolean ascending) {
		if (!ascending)
			Collections.reverse(FlightData.ITEMS);
	}

}
