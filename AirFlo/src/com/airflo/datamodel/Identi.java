package com.airflo.datamodel;

/**
 * 
 * This Class is part of AirFlo.
 * 
 * It provides an entity of Identis. It is used to store the key, the
 * string representation, and the unit of one dataentry.
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
public class Identi  implements Comparable<Identi>{
	private String key;
	private String stringRep;
	private String unit;
	
	public Identi(String key, String stringRep, String unit) {
		this.key = key;
		this.stringRep = stringRep;
		this.unit = unit;
	}
	
	public String getUnit() {
		return unit;
	}

	public String getKey() {
		return key;
	}
	
	public String getStringRep() {
		return stringRep;
	}
	
	@Override
	public int compareTo(Identi another) {
		return this.key.compareTo(another.getKey());
	}

}
