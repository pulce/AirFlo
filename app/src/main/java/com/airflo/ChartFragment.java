package com.airflo;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.airflo.datamodel.FlightData;
import com.airflo.helpers.OnlyContext;
import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * This Class is part of AirFlo.
 * <p/>
 * A fragment providing a statistics screen. This fragment is
 * either contained in a {@link FlightListActivity} in two-pane mode (on
 * tablets) or a {@link FragActivity} on handsets.
 *
 * @author Florian Hauser Copyright (C) 2016
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

public class ChartFragment extends Fragment {
    private HorizontalBarChart chart;

    private static final int SUM = 0;
    private static final int MAXIMUM = 1;
    private static final int AVG = 2;

    private String[] sortValues = new String[]{
            "sum;total",
            "sum;duration",
            "avg;duration",
            "max;duration",
            "sum;heightgain",
            "avg;heightgain",
            "avg;maxvario",
            "avg;minvario",
            "max;maxheight",
            "avg;maxheight",
            "avg;maxvario",
            "avg;minvario",
            "avg;avgspeed",
            "sum;olc;km",
            "avg;olc;km",
            "sum;olc;pts",
            "avg;olc;pts"};
    private String[] sortEntries;
    private int[] sortFunction;
    private String[] catValues = new String[]{
            "total",
            "site",
            "site2",
            "country",
            "landingsite",
            "date;year",
            "date;month",
            "date;week",
            "date;starttime",
            "glidertyp",
            "gliderID",
            "starttype",
            "compID",
            "compClass"};
    private String[] catEntries;

    private int xCat;
    private int yCat;

    ArrayList<String> yNames;
    ArrayList<Float> yValues;
    ArrayList<Float> counter;

    public ChartFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        catEntries = new String[catValues.length];
        for (int i = 0; i < catEntries.length; i++) {
            if (catValues[i].equals("total"))
                catEntries[i] = getString(R.string.alltogether);
            else if (catValues[i].contains("date"))
                catEntries[i] = OnlyContext.rString(catValues[i].split(";")[1]);
            else
                catEntries[i] = OnlyContext.sortKeyToString(catValues[i]);
        }
        sortEntries = new String[sortValues.length];
        sortFunction = new int[sortValues.length];
        for (int i = 0; i < sortEntries.length; i++) {
            String[] splt = sortValues[i].split(";");
            sortEntries[i] = OnlyContext.sortKeyToString(splt[1]);
            if (splt[0].contains("sum")) {
                sortFunction[i] = SUM;
            }
            if (splt.length > 2) {
                sortEntries[i] += " " + splt[2];
                sortValues[i] = splt[1]+";"+splt[2];
            } else {
                sortValues[i] = splt[1];
            }
            if (splt[0].contains("max")) {
                sortFunction[i] = MAXIMUM;
                sortEntries[i] += " (Max)";
            }
            if (splt[0].contains("avg")) {
                sortFunction[i] = AVG;
                sortEntries[i] += " (Avg)";
            }
        }

        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater
                .inflate(R.layout.fragment_chart, container, false);

        chart = (HorizontalBarChart) rootView.findViewById(R.id.chart);

        Spinner xSpinner = (Spinner) rootView.findViewById(R.id.xSpinner);
        ArrayAdapter<String> adp1=new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_list_item_1, catEntries);
        adp1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        xSpinner.setAdapter(adp1);
        xSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                xCat = position;
                makeData();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        Spinner ySpinner = (Spinner) rootView.findViewById(R.id.ySpinner);
        ArrayAdapter<String> adp2=new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_list_item_1, sortEntries);
        adp2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        ySpinner.setAdapter(adp2);
        ySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                yCat = position;
                makeData();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        xCat = 0;
        yCat = 0;
        makeData();
        return rootView;
    }


    private void makeData () {
        yNames = new ArrayList<>();
        yValues = new ArrayList<>();
        counter = new ArrayList<>();

        for (FlightData.FlightDataItem item:FlightData.ITEMS) {
            Float toAdd = 1f;
            String itemsXValue;
            if (catValues[xCat].contains("date")) {
                String datetype = catValues[xCat].split(";")[1];
                if (datetype.equals("starttime")) {
                    itemsXValue = item.getFromKey("starttime");
                    if (itemsXValue.length() < 3) continue;
                    Log.d("try to get time", itemsXValue);
                    itemsXValue = itemsXValue.substring(0,2);
                }
                else {
                    itemsXValue = item.getFromKey("date");
                    if (itemsXValue.length() < 6) continue;
                    Log.d("try to get Year", itemsXValue);
                    if (datetype.equals("year"))
                        itemsXValue = itemsXValue.substring(6);
                    if (datetype.equals("month"))
                        itemsXValue = itemsXValue.substring(3, 5);
                    if (datetype.equals("week")) {
                        SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy");
                        try {
                            Date date = format.parse(itemsXValue);
                            Calendar c = Calendar.getInstance();
                            c.setTime(date);
                            itemsXValue = "" + c.get(Calendar.WEEK_OF_YEAR);
                            if (itemsXValue.length()==1) itemsXValue = "0" + itemsXValue;
                        } catch (ParseException e) {
                            continue;
                        }
                    }

                }
            }
            else if (catValues[xCat].equals("total"))
                itemsXValue = getString(R.string.alltogether);
            else
                itemsXValue = item.getFromKey(catValues[xCat]);
            if (yCat > 0) {
                toAdd = getValue(item, sortValues[yCat]);
                if (toAdd == null) continue;
            }
            if (yNames.contains(itemsXValue)) {
                int index = yNames.indexOf(itemsXValue);
                if (sortFunction[yCat] == SUM || sortFunction[yCat] == AVG) {
                    yValues.set(index, yValues.get(index) + toAdd);
                    counter.set(index, counter.get(index) + 1);
                } else if (sortFunction[yCat] == MAXIMUM) {
                    if (yValues.get(index) < toAdd)
                        yValues.set(index, toAdd);
                }
            } else {
                yNames.add(itemsXValue);
                yValues.add(toAdd);
                counter.add(1f);
            }
        }
        ArrayList<BarEntry> entries = new ArrayList<>();
        for (int i = 0; i < yNames.size(); i++) {
             if (sortFunction[yCat] == AVG)
                yValues.set(i, yValues.get(i) / counter.get(i));
        }
        if (catValues[xCat].contains("date"))
            qSort(yNames, 0, yValues.size() - 1);
        else
            qSort(yValues, 0, yValues.size() - 1);

        for (int i = 0; i < yNames.size(); i++) {
            entries.add(new BarEntry(yValues.get(i), i));
        }

        BarDataSet barDataSet = new BarDataSet(entries, null);
        barDataSet.setColor(ContextCompat.getColor(getActivity(), R.color.accent));
        BarData barData = new BarData(yNames, barDataSet);
        chart.setData(barData);
        chart.setDescription("");
        chart.getLegend().setEnabled(false);
        XAxis xAxis = chart.getXAxis();
        xAxis.setLabelsToSkip(0);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        chart.animateXY(800, 2000);
    }

    private Float getValue(FlightData.FlightDataItem item, String key) {
        String olcType = null;
        if (key.contains(";")) {
            olcType = key.split(";")[1];
            key = key.split(";")[0];
        }
        String tmp = item.getFromKey(key);
        if (tmp.contains("pts")) {
            Log.d("Chart Fragment ES", tmp);
            String[] splt = tmp.split(" ");
            if (olcType.equals("km"))
                tmp = splt[2].replace("km", "").replace(",", "");
            if (olcType.equals("pts"))
                tmp = splt[3].replace("pts", "");
            Log.d("Chart Fragment RS", tmp);
        }
        if (tmp.contains(":"))
            return getDuration(tmp);
        String unit = FlightData.identis.getIdenti(key).getUnit();
        if (tmp.length() > unit.length()) {
            tmp = tmp.substring(0, tmp.length()-unit.length());
        }
        Float result = null;

        Log.d("String rep", tmp);
        try {
            result = Float.parseFloat(tmp);
        } catch (Exception e) {
            Log.e("Chart Fragment ES", tmp);
        }
        return result;
    }

    private void qSort(ArrayList sorter, int links, int rechts) {
        if (links < rechts) {
            int i = partition(sorter, links,rechts);
            qSort(sorter, links,i-1);
            qSort(sorter, i+1,rechts);
        }
    }

    private int partition(ArrayList sorter, int links, int rechts) {
        String xHelp;
        Comparable pivot;
        float help;
        int i, j;
        pivot = (Comparable) sorter.get(rechts);
        i     = links;
        j     = rechts-1;
        while(i<=j) {
            if (pivot.compareTo(sorter.get(i)) < 0) {
                help = yValues.get(i);
                yValues.set(i, yValues.get(j));
                yValues.set(j, help);
                xHelp = yNames.get(i);
                yNames.set(i, yNames.get(j));
                yNames.set(j, xHelp);
                j--;
            } else i++;
        }
        help = yValues.get(i);
        yValues.set(i, yValues.get(rechts));
        yValues.set(rechts, help);
        xHelp = yNames.get(i);
        yNames.set(i, yNames.get(rechts));
        yNames.set(rechts, xHelp);
        return i;
    }

    private static Float getDuration(String base) {
        DateFormat formatter = new SimpleDateFormat("HH:mm:ss");
        Float dur = 0f;
        try {
            Date start = formatter.parse(base);
            dur += start.getSeconds()/(60f*60f);
            dur += start.getMinutes()/60f;
            dur += start.getHours();
            return dur;
        } catch (ParseException e) {
            return null;
        }
    }

}
