package com.airflo.datamodel;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import com.airflo.helpers.OnlyContext;

import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * 
 * This Class is part of AirFlo.
 * 
 * It provides a blueprint for the whole datamodel. It defines a keyset (loaded
 * from the xml file in Assets) together with the string representations for all
 * items.
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
public class Identis {

	private static Map<String, Identi> identiMap = new LinkedHashMap<String, Identi>();
	private ArrayList<String> keySet;
	private SharedPreferences sharedPrefs;

	public Identis() {
		sharedPrefs = PreferenceManager.getDefaultSharedPreferences(OnlyContext
				.getContext());
		// Load all keys from the Assets dir.
		AssetManager assetManager = OnlyContext.getContext().getAssets();
		InputStream input;
		try {
			input = assetManager.open("bookitems.xml");

			keySet = new ArrayList<String>();
			XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
			factory.setNamespaceAware(true);
			XmlPullParser xpp = factory.newPullParser();
			xpp.setInput(input, null);

			while (xpp.getEventType() != XmlPullParser.END_DOCUMENT) {
				if (xpp.getEventType() == XmlPullParser.END_TAG) {
					if (xpp.getName().equals("ressources"))
						break;
				}
				if (xpp.getEventType() == XmlPullParser.START_TAG) {
					if (xpp.getName().equals("key")) {
						String key = xpp.getAttributeValue(0);
						String unit = xpp.getAttributeValue(1);
						keySet.add(key);
						identiMap.put(key, new Identi(key, OnlyContext.rString(key), unit));
					}
				}
				xpp.next();
			}
		} catch (IOException e) {
			Log.e("Identis: Parse keys", "Asset File not found");
		} catch (XmlPullParserException e) {
			Log.e("Identis: Pull Parser", e.toString());
		}
		// Derive the names associated to keys
	}
	
	public static Identi getIdenti(String key) {
		return identiMap.get(key);
	}

	public ArrayList<Identi> getIdentis() {
		ArrayList<Identi> content = new ArrayList<Identi>();
		Iterator<Map.Entry<String, Identi>> it = identiMap.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<String, Identi> pairs = it.next();
			content.add(pairs.getValue());
		}
		return content;
	}

	/**
	 * Method to derive all item to be showed in the main list. The items to be
	 * shown depend on user preferences
	 * 
	 * @return An array list with all item keys for the main list.
	 */
	public ArrayList<String> getListContent() {
		ArrayList<String> content = new ArrayList<String>();
		if (sharedPrefs.getBoolean("listprefnumber", false))
			content.add("number");
		if (sharedPrefs.getBoolean("listprefdate", false))
			content.add("date");
		if (sharedPrefs.getBoolean("listprefsite", true))
			content.add("site");
		if (sharedPrefs.getBoolean("listprefsite2", true))
			content.add("site2");
		if (sharedPrefs.getBoolean("listpreflandingsite", false))
			content.add("landingsite");
		return content;
	}

	public ArrayList<String> getHeadContent() {
		ArrayList<String> content = new ArrayList<String>();
		if (sharedPrefs.getBoolean("listheadnumber", true))
			content.add("number");
		if (sharedPrefs.getBoolean("listheaddate", true))
			content.add("date");
		if (sharedPrefs.getBoolean("listheadsite", false))
			content.add("site");
		if (sharedPrefs.getBoolean("listheadsite2", false))
			content.add("site2");
		if (sharedPrefs.getBoolean("listheadlandingsite", false))
			content.add("landingsite");
		return content;
	}

	public ArrayList<String> getKeySet() {
		return keySet;
	}

	public String getStringRep(String key) {
		String rep = "";
		if (identiMap.containsKey(key))
			rep = identiMap.get(key).getStringRep();
		return rep;
	}

	public boolean hasKey(String key) {
		if (identiMap.containsKey(key))
			return true;
		return false;
	}
}
