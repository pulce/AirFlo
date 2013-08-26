package com.airflo.datamodel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.util.Log;

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
public class FlightData {

	public static List<FlightDataItem> ITEMS = new ArrayList<FlightDataItem>();
	public static Map<String, FlightDataItem> ITEM_MAP = new HashMap<String, FlightDataItem>();
	public static Identis identis = new Identis();

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
	 * This Class is part of AirFlo. It represents one flight, fererred
	 * to by a certain key.
	 * 
	 * @author Florian Hauser Copyright (C) 2013
	 * 
	 */
	public static class FlightDataItem {
		public Map<String, String> content = new HashMap<String, String>();

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
		}

		/**
		 * This method handles units and special operations on data. It divides
		 * min - maxvario by 10 It creates UTC corrected starttimes It handles
		 * starttype It turns 0 and 00:00:00 to empty fields
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
			if (key.equals("starttime") && data.length() > 5) {
				try {
					String[] tims = data.split(":");
					if (getFromKey("UTCorr").length() < 6)
						return "";
					String[] utcs = getFromKey("UTCorr").split(":");
					int sec = Integer.valueOf(tims[2])
							- Integer.valueOf(utcs[2]);
					int min = Integer.valueOf(tims[1])
							- Integer.valueOf(utcs[1]);
					int hou = Integer.valueOf(tims[0])
							- Integer.valueOf(utcs[0]);

					if (sec < 0) {
						min -= 1;
						sec += 60;
					}
					if (min < 0) {
						hou -= 1;
						min += 60;
					}
					if (hou < 0)
						hou += 24;
					String se = (sec < 10 ? "0" + sec : "" + sec);
					String mi = (min < 10 ? "0" + min : "" + min);
					String ho = (hou < 10 ? "0" + hou : "" + hou);
					String utccorr = "" + ho + ":" + mi + ":" + se;
					putContent("UTCorr", utccorr);
				} catch (NullPointerException e) {
					Log.e("Exception", e.toString());
				} catch (NumberFormatException e) {
					Log.e("Exception", e.toString());
				} catch (ArrayIndexOutOfBoundsException e) {
					Log.e("Exception", e.toString());
				}
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
			if (data.equals("0"))
				return "";
			if (data.equals("00:00:00"))
				return "";
			modData += Identis.getIdenti(key).getUnit();
			return modData;
		}

		/**
		 * Method to get the content of one item for the main list.
		 * 
		 * @return String containing the text for one item.
		 */
		public String getListItem() {
			String item = "";
			ArrayList<String> listContent = identis.getListContent();
			for (int i = 0; i < listContent.size(); i++) {
				item += content.get(listContent.get(i));
				if (i < listContent.size() - 1)
					item += "  -  ";
			}
			return item;
		}

		public String getheadItem() {
			String item = "";
			ArrayList<String> listContent = identis.getHeadContent();
			for (int i = 0; i < listContent.size(); i++) {
				item += content.get(listContent.get(i));
				if (i < listContent.size() - 1)
					item += "  -  ";
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

	}
}
