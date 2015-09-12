package com.airflo.datamodel;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import com.airflo.helpers.OnlyContext;

import android.util.Log;

/**
 * This Class is part of AirFlo.
 * <p/>
 * It is responsible for reading and parsing the flightbook zip file. It will
 * pass the data straight to the static class FlightData.
 *
 * @author Florian Hauser Copyright (C) 2013
 *         <p/>
 *         This program is free software: you can redistribute it and/or modify
 *         it under the terms of the GNU General Public License as published by
 *         the Free Software Foundation, either version 3 of the License, or (at
 *         your option) any later version.
 *         <p/>
 *         This program is distributed in the hope that it will be useful, but
 *         WITHOUT ANY WARRANTY; without even the implied warranty of
 *         MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *         General Public License for more details.
 *         <p/>
 *         You should have received a copy of the GNU General Public License
 *         along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
public class ParseFlightBook {

    private static boolean bookLoaded = false;
    private static int jFlightDepth;

    public static boolean isBookLoaded() {
        return bookLoaded;
    }

    /**
     * Method responsible for loading the flightbook and parsing the xml file.
     *
     * @param fileName File Name
     * @return boolean success
     */
    public static boolean loadBook(String fileName) {
        File file = new File(fileName);
        if (!file.exists()) {
            Log.e("Parser", "File does not exist.");
            bookLoaded = false;
            return false;
        }

        try {
            FlightData.reset();
            ZipFile zipFile = new ZipFile(fileName);
            try {
                ZipEntry zipEntry;
                zipEntry = zipFile.getEntry("flugbuch.xml");
                if (zipEntry == null) {
                    Log.e("Parser", "Zip corrupt - no flugbuch.xml");
                    bookLoaded = false;
                    return false;
                }
                InputStream input = zipFile.getInputStream(zipEntry);
                XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                factory.setNamespaceAware(true);
                XmlPullParser xpp = factory.newPullParser();

                xpp.setInput(input, null);

                while (xpp.getEventType() != XmlPullParser.END_DOCUMENT) {
                    if (xpp.getEventType() != XmlPullParser.START_TAG) {
                        xpp.next();
                        continue;
                    }
                    if (!xpp.getName().equals("JFlight")) {
                        xpp.next();
                        continue;
                    }
                    jFlightDepth = xpp.getDepth();
                    // Analyze JFlight
                    FlightData.FlightDataItem item = new FlightData.FlightDataItem();
                    while (!(xpp.getEventType() == XmlPullParser.END_TAG && xpp
                            .getName().equals("JFlight"))) {
                        if (xpp.getDepth() > jFlightDepth + 1) {
                            xpp.next();
                            continue;
                        }

                        if (xpp.getEventType() == XmlPullParser.START_TAG) {
                            String key = xpp.getName();
                            if (FlightData.identis.hasKey(key)) {
                                if (key.equals("olc")) {
                                    item.putContent(key, getOLC(xpp));
                                }
                                if (key.equals("tag")) {
                                    item.putContent(key, getTag(xpp));
                                }
                                if (xpp.getAttributeCount() > 0) {
                                    item.putContent(key, getRecent(xpp));
                                    xpp.next();
                                    continue;
                                }
                                xpp.next();
                                if (xpp.getEventType() == XmlPullParser.END_TAG) {
                                    continue;
                                }
                                String txt = xpp.getText();
                                if (txt.trim().length() > 0) {
                                    item.putContent(key, txt);
                                    continue;
                                }

                                xpp.next();
                                if (xpp.getEventType() == XmlPullParser.END_TAG) {
                                    continue;
                                }
                                if (xpp.getDepth() > jFlightDepth
                                        && xpp.getAttributeCount() > 0) {
                                    for (int i = 0; i < xpp.getAttributeCount(); i++) {
                                        if (xpp.getAttributeName(i).equals("year")) {
                                            item.putContent(key, readDate(xpp));
                                        }
                                    }
                                    for (int i = 0; i < xpp.getAttributeCount(); i++) {
                                        if (xpp.getAttributeName(i).equals("hour")) {
                                            item.putContent(key, readTime(xpp));
                                        }
                                    }
                                    continue;
                                }
                            }
                        }
                        xpp.next();
                    }
                    FlightData.addItem(item);
                    xpp.next();
                }

                final Enumeration<? extends ZipEntry> entries = zipFile.entries();
                while ( entries.hasMoreElements() )
                {
                    final ZipEntry entry = entries.nextElement();
                    if (entry.getName().toLowerCase().endsWith(".igc")) {
                        if (entry.getName().split("/").length > 0) {
                            String flightNumString = entry.getName().split("/")[1];
                            FlightData.ITEM_MAP.get(flightNumString).setIgcName(entry.getName());
                        }
                    }
                 }

            } catch (Exception e) {
                Log.e("Exception", e.toString());
            } finally {
                zipFile.close();
            }
        } catch (IOException e) {
            Log.e("IOException", e.toString());
        }
        bookLoaded = true;
        return true;
    }


    /**
     * Special treatment for parsing OLC.
     *
     * @param xpp Pull Parser
     * @return OLCstring
     * @throws XmlPullParserException
     * @throws IOException
     */
    private static String getOLC(XmlPullParser xpp)
            throws XmlPullParserException, IOException {
        String olc = "";
        xpp.nextTag();
        int cat = Integer.valueOf(xpp.nextText());
        switch (cat) {
            case 0:
                olc += OnlyContext.rString("olcfree");
                break;
            case 1:
                olc += OnlyContext.rString("olctri");
                break;
            case 2:
                olc += OnlyContext.rString("olcfia");
                break;
            default:
                return "";
        }
        olc += (": " + getOlcPt(xpp, cat) + "km, ");
        while (true) {
            xpp.next();
            if (xpp.getEventType() == XmlPullParser.START_TAG)
                if (xpp.getName().equals("FlightPoints"))
                    break;
        }
        olc += (getOlcPt(xpp, cat) + "pts");
        return olc;
    }

    /**
     * Method to extract OLC points from XML.
     *
     * @param xpp Pull Parser
     * @param cat Category
     * @return OLCPointString
     * @throws XmlPullParserException
     * @throws IOException
     */
    public static String getOlcPt(XmlPullParser xpp, int cat)
            throws XmlPullParserException, IOException {
        int catc = cat;
        while (catc >= 0) {
            if (xpp.getEventType() == XmlPullParser.START_TAG)
                if (xpp.getName().equals("int"))
                    catc--;
            xpp.next();
        }
        double pt = Double.valueOf(xpp.getText()) / 1000;
        return String.format(Locale.ENGLISH, "%.3f", pt);
    }

    /**
     * Special treatment for parsing tags.
     *
     * @param xpp Pull Parser
     * @return tagString
     * @throws XmlPullParserException
     * @throws IOException
     */
    private static String getTag(XmlPullParser xpp)
            throws XmlPullParserException, IOException {
        String tag = "";
        boolean stillTag = true;
        do {
            xpp.next();
            if (xpp.getEventType() == XmlPullParser.START_TAG)
                if (xpp.getName().equals("string")) {
                    xpp.next();
                    if (tag.length() > 0)
                        tag += ";";
                    tag += xpp.getText();
                }

            if (xpp.getEventType() == XmlPullParser.END_TAG)
                if (xpp.getName().equals("tag"))
                    stillTag = false;
        } while (stillTag);
        return tag;
    }

    /**
     * Handle entries that cover new and org. New will be preferred over org.
     *
     * @param xpp PullParser
     * @return All attributes under new (preferred) or org.
     * @throws Exception
     */
    private static String getRecent(XmlPullParser xpp) throws Exception {
        Map<String, String> attrs = getAttributes(xpp);
        if (attrs.containsKey("new"))
            return attrs.get("new");
        if (attrs.containsKey("org"))
            return attrs.get("org");
        return "";
    }

    /**
     * Method to handle time entries.
     *
     * @param xpp PullParser
     * @return Time entry
     * @throws Exception
     */
    private static String readTime(XmlPullParser xpp) throws Exception {
        Map<String, String> attrs = getRecentAttributes(xpp);
        String hour = attrs.get("hour");
        if (hour.length() < 2)
            hour = "0" + hour;
        String minute = attrs.get("minute");
        if (minute.length() < 2)
            minute = "0" + minute;
        String second = attrs.get("second");
        if (second.length() < 2)
            second = "0" + second;
        return hour + ":" + minute + ":" + second;
    }

    /**
     * Method to handle dates
     *
     * @param xpp PullParser
     * @return Date entry
     * @throws Exception
     */
    private static String readDate(XmlPullParser xpp) throws Exception {
        Map<String, String> attrs = getRecentAttributes(xpp);
        String year = attrs.get("year");
        String month = attrs.get("month");
        if (month.length() < 2)
            month = "0" + month;
        String day = attrs.get("day");
        if (day.length() < 2)
            day = "0" + day;
        return (day + "." + month + "." + year);
    }

    /**
     * Handle entries that cover new and org. New will be preferred over org.
     *
     * @param xpp PullParser
     * @return A map of all most recent Attributes.
     * @throws Exception
     */
    private static Map<String, String> getRecentAttributes(XmlPullParser xpp)
            throws Exception {
        // int initialDepth = xpp.getDepth();
        // xpp.next();
        Map<String, String> attrs = null;
        while (true) {
            if (xpp.getEventType() == XmlPullParser.END_TAG) {
                if (xpp.getDepth() == jFlightDepth + 1) {
                    break;
                }
            }
            if (xpp.getEventType() == XmlPullParser.START_TAG) {
                if (xpp.getName().equals("org")) {
                    attrs = getAttributes(xpp);
                }
                if (xpp.getName().equals("new")) {
                    attrs = getAttributes(xpp);
                    return attrs;
                }
            }
            xpp.next();
        }
        return attrs;
    }

    /**
     * Method to get Attributes
     *
     * @param xpp PullParser
     * @return A map of all Attributes
     * @throws Exception
     */
    private static Map<String, String> getAttributes(XmlPullParser xpp)
            throws Exception {
        Map<String, String> attrs;
        int acount = xpp.getAttributeCount();
        if (acount != -1) {
            attrs = new HashMap<>(acount);
            for (int x = 0; x < acount; x++) {
                attrs.put(xpp.getAttributeName(x), xpp.getAttributeValue(x));
            }
        } else {
            throw new Exception("Required entity attributes missing");
        }
        return attrs;
    }
}
