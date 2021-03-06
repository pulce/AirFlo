package com.airflo.helpers;

import android.app.Application;
import android.content.Context;
import android.util.Log;

/**
 * 
 * This Class is part of AirFlo.
 * 
 * It provides a Context for other Classes that do not have one (poor ones...).
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
public class OnlyContext extends Application {

	private static Context mContext;

	@Override
	public void onCreate() {
		super.onCreate();
		mContext = this;
	}

	public static Context getContext() {
		return mContext;
	}

	/**
	 * Method to access Android resource strings.
	 * @param key
	 * Key
	 * @return
	 * String
	 */
	public static String rString(String key) {
		int nam = mContext.getResources().getIdentifier(key, "string",
				mContext.getPackageName());
		try {
		return mContext.getString(nam);
		} catch (Exception e) {
			Log.e("String not found", e.toString());
			return "";
		}
	}

	/**
	 * Method to get the Android resource string corresponding to a pref value.
	 * @param sortkey Key
	 * @return String Corresponding String
	 */
	public static String sortKeyToString(String sortkey) {
		if (!sortkey.contains(";"))
			return OnlyContext.rString(sortkey);
		String[] sortkeys = sortkey.split(";");
		String theString = OnlyContext.rString(sortkeys[0]);
		for (int i = 1; i < sortkeys.length; i++)
			theString += ("/" + OnlyContext.rString(sortkeys[i]));
		return theString;
	}

}