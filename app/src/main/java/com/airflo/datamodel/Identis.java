package com.airflo.datamodel;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import com.airflo.helpers.OnlyContext;
import android.content.res.AssetManager;
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

	private static Map<String, Identi> identiMap = new LinkedHashMap<>();

	public Identis() {
		// Load all keys from the Assets dir.
		identiMap.put("empty", new Identi("empty", OnlyContext.rString("list_pref_empty_field"), "", true, ""));
		AssetManager assetManager = OnlyContext.getContext().getAssets();
		InputStream input;
		try {
			input = assetManager.open("bookitems.xml");
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
						boolean listPref = xpp.getAttributeValue(2).equals("true");
						String compType = xpp.getAttributeValue(3);
						identiMap.put(key, new Identi(key, OnlyContext.rString(key), unit, listPref, compType));
					}
				}
				xpp.next();
			}
		} catch (IOException e) {
			Log.e("Identis: Parse keys", "Asset File not found");
		} catch (XmlPullParserException e) {
			Log.e("Identis: Pull Parser", e.toString());
		}
	}
	
	public Identi getIdenti(String key) {
		return identiMap.get(key);
	}

	public ArrayList<Identi> getIdentis() {
		ArrayList<Identi> content = new ArrayList<>();
		for (Map.Entry<String, Identi> pairs : identiMap.entrySet()) {
			content.add(pairs.getValue());
		}
		return content;
	}
	
	public CharSequence[] getPrefListChars(boolean names) {
		ArrayList<String> listPref = new ArrayList<>();
		for (Identi ide:getIdentis()) {
			if (ide.isListPref()) {
				if (names)
					listPref.add(ide.getStringRep());
				else
					listPref.add(ide.getKey());
			}
		}
		CharSequence[] resl = new CharSequence[listPref.size()];
		for (int i = 0; i < resl.length; i++)
			resl[i] = listPref.get(i);
		return resl;
	}

	public boolean hasKey(String key) {
		return identiMap.containsKey(key);
	}
}
