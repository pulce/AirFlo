package com.airflo.datamodel;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.airflo.helpers.OnlyContext;

/**
 * 
 * This Class is part of AirFlo.
 * 
 * It provides the data model for all Activities and Adapters.
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
public class FlightData {

	public static List<FlightDataItem> ITEMS = new ArrayList<FlightDataItem>();
	public static Map<String, FlightDataItem> ITEM_MAP = new HashMap<String, FlightDataItem>();
	public static Identis identis = new Identis();
	public static SharedPreferences sharedPrefs = PreferenceManager
			.getDefaultSharedPreferences(OnlyContext.getContext());

	public static void addItem(FlightDataItem item) {
		ITEMS.add(0, item);
		ITEM_MAP.put(item.content.get("number"), item);
	}

	/**
	 * This method will reset all data. Must be called when a new book is
	 * loaded.
	 */
	public static void reset() {
		ITEM_MAP = new HashMap<String, FlightDataItem>();
		ITEMS = new ArrayList<FlightDataItem>();
		identis = new Identis();
	}

	/**
	 * 
	 * This Class is part of AirFlo. It represents one flight, referred to by a
	 * certain key.
	 * 
	 * @author Florian Hauser Copyright (C) 2013
	 * 
	 */
	public static class FlightDataItem {
		public Map<String, String> content = new HashMap<String, String>();
		public Map<String, String> sortContent = new HashMap<String, String>();
		private DateFormat dateParser = new SimpleDateFormat("dd.MM.yyyy");
		private DateFormat dateFormatter = new SimpleDateFormat("yyyyMMdd");

		public FlightDataItem() {
		}

		/**
		 * Register content of one item. At this stage one can add units and
		 * perform special ops handeled by Identis.
		 * 
		 * @param key
		 * @param data
		 */
		public void putContent(String key, String data) {
			String modData = specialOp(key, data);
			content.put(key, modData);
			String sortData = transform(key, modData);
			sortContent.put(key, sortData);
		}

		/**
		 * This method handles units and special operations on data. It divides
		 * min - maxvario by 10 It creates UTC corrected starttimes. It handles
		 * starttype. It returns 0 and 00:00:00 to empty fields
		 * 
		 * @param data
		 * @return modified data
		 */
		public String specialOp(String key, String data) {
			String modData = data;
			// Divide climb / sink
			if (key.equals("maxvario") || key.equals("minvario")) {
				if (modData.length() > 1) {
					modData = data.substring(0, data.length() - 1) + "."
							+ data.substring(data.length() - 1, data.length());
				}
			}
			if (key.equals("avgspeed") && data.length() > 1) {
				if (modData.length() > 1) {
					modData = data.substring(0, data.length() - 2) + "."
							+ data.substring(data.length() - 2);
				}
			}
			if (key.equals("starttime") && data.length() > 5) {
				putContent("UTCorr", subTime(data, getFromKey("UTCorr")));
			}
			if (key.equals("duration")) {
				this.putContent("landingtime",
						addTime(getFromKey("starttime"), data));
			}
			// Should be handled by xml file!
			if (key.equals("starttype")) {
				if (data.equals("1"))
					modData = OnlyContext.rString("start_mountain");
				if (data.equals("2"))
					modData = OnlyContext.rString("start_winch");
				if (data.equals("3"))
					modData = OnlyContext.rString("start_ultralight");
				if (data.equals("4"))
					modData = OnlyContext.rString("start_other");
				if (data.equals("5"))
					modData = OnlyContext.rString("start_powered");
				if (data.equals("6"))
					modData = OnlyContext.rString("start_estart");
				if (data.equals("7"))
					modData = OnlyContext.rString("start_ground");
			}
			if (identis.getIdenti(key).getCompType().length() > 0)
				sortContent.put(key, transform(key, modData));
			if (data.equals("0"))
				return "";
			if (data.equals("00:00:00"))
				return "";
			modData += identis.getIdenti(key).getUnit();
			return modData;
		}

		private static String addTime(String base, String toAdd) {
			DateFormat formatter = new SimpleDateFormat("HH:mm:ss");
			try {
				Date start = (Date) formatter.parse(base);
				Date dur = (Date) formatter.parse(toAdd);
				Date sum = new Date(start.getTime() + dur.getTime());
				return formatter.format(sum);
			} catch (ParseException e) {
				return "";
			}
		}

		private static String subTime(String base, String toAdd) {
			DateFormat formatter = new SimpleDateFormat("HH:mm:ss");
			try {
				Date start = (Date) formatter.parse(base);
				Date dur = (Date) formatter.parse(toAdd);
				Date sum = new Date(start.getTime() - dur.getTime());
				return formatter.format(sum);
			} catch (ParseException e) {
				return "";
			}
		}

		/**
		 * Method to get the content of one item for the main list.
		 * 
		 * @return String containing the text for one item.
		 */
		public String getListItem() {
			String item = "";
			for (int i = 1; i <= 3; i++) {
				String key = sharedPrefs.getString("listsub" + i, "empty");
				if (!key.equals("empty")) {
					if (item.length() > 0)
						item += "  -  ";
					item += content.get(key);

				}
			}
			return item;
		}

		public String getheadItem() {
			String item = "";
			for (int i = 1; i <= 3; i++) {
				String key = sharedPrefs.getString("listhead" + i, "empty");
				if (!key.equals("empty")) {
					if (item.length() > 0)
						item += "  -  ";
					item += content.get(key);

				}
			}
			return item;
		}

		public String getFromKey(String key) {
			return content.get(key);
		}

		public ArrayList<String[]> getMap() {
			ArrayList<String[]> table = new ArrayList<String[]>();
			for (String key : identis.getKeySet()) {
				table.add(new String[] { identis.getStringRep(key),
						content.get(key) });
			}
			return table;
		}
		
		/**
		 * This method transforms each string into another sortable string.
		 * @param item
		 * @return sortable String
		 */
		private String transform(String compKey, String itemData) {
			String compType = FlightData.identis.getIdenti(compKey)
					.getCompType();
			if (itemData.length() == 0 || itemData == null)
				return "";
			if (compType.equals("int")) {
				try {
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
			return itemData;
		}



	}
}
