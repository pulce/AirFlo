package com.airflo;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.airflo.datamodel.FlightData;
import com.airflo.datamodel.Identi;
import com.airflo.helpers.OnlyContext;

/**
 * This Class is part of AirFlo.
 * <p/>
 * A fragment representing a single Flight detail screen. This fragment is
 * either contained in a {@link FlightListActivity} in two-pane mode (on
 * tablets) or a {@link FragActivity} on handsets.
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

public class FlightDetailFragment extends Fragment {

    public static final String ARG_ITEM_ID = "item_id";
    private FlightData.FlightDataItem mItem;
    private TableLayout table;
    private View rootView;

    public static final String IGCNAME = "com.airflo.igcname";
    public static final String FLIGHTTITLE = "com.airflo.flighttitle";

    public FlightDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments().containsKey(ARG_ITEM_ID)) {
            mItem = FlightData.ITEM_MAP.get(getArguments().getString(
                    ARG_ITEM_ID));
            setHasOptionsMenu(true);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_flight_detail, container,
                false);
        table = (TableLayout) rootView.findViewById(R.id.table_detail);
        addViews();
        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (mItem.getIgcName() != null)
            inflater.inflate(R.menu.flightdetailmenu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_open_map:
                Intent fileChooser = new Intent(getActivity().getApplicationContext(),
                        MapsActivity.class).putExtra(IGCNAME, mItem.getIgcName()).putExtra(FLIGHTTITLE, mItem.getheadItem());
                startActivity(fileChooser);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * This method allows the corresponding Activity to refresh the view.
     */
    public void refresh() {
        table.removeAllViews();
        addViews();
    }

    /**
     * Method to build and add the View. It will consider preferences for
     * certain items, textsizes, and handle empty fields.
     */
    public void addViews() {
        table.setColumnShrinkable(1, true);

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(OnlyContext
                .getContext());
        float detSize = Float.valueOf(sharedPrefs.getString("detailtextsize",
                "20"));
        boolean hideEmpty = sharedPrefs.getBoolean("detailListPrefEmpty", true);
        for (Identi identi : FlightData.identis.getIdentis()) {
            String prefKey = "detailListPref" + identi.getKey();
            if (sharedPrefs.getBoolean(prefKey, true)) {
                if (hideEmpty) {
                    if (mItem.getFromKey(identi.getKey()) == null)
                        continue;
                    if (mItem.getFromKey(identi.getKey()).equals(""))
                        continue;
                }
                if (identi.getKey().equals("tag")) {
                    if (mItem.getFromKey(identi.getKey()).length() > 0) {
                        LinearLayout lnn = (LinearLayout) rootView
                                .findViewById(R.id.LinearAdditionLayout);
                        String[] tags = mItem.getFromKey(identi.getKey())
                                .split(";");
                        for (String tag : tags) {
                            if (tag.equals("off-field"))
                                tag = "off_field";
                            int resID = getResources().getIdentifier(tag,
                                    "drawable", "com.airflo");
                            ImageView img = new ImageView(getActivity());
                            img.setImageResource(resID);
                            img.setPadding(8, 14, 8, 0);
                            lnn.addView(img);
                        }
                    }

                } else {
                    TableRow row = new TableRow(getActivity());

                    TextView header = new TextView(getActivity());
                    header.setSingleLine();
                    header.setTextSize(detSize);
                    header.setText(identi.getStringRep());
                    header.setPadding(6, 4, 10, 4);
                    row.addView(header);

                    TextView cell = new TextView(getActivity());
                    cell.setSingleLine(false);
                    cell.setTextSize(detSize);
                    cell.setText(mItem.getFromKey(identi.getKey()));
                    cell.setPadding(6, 4, 6, 4);
                    row.addView(cell);

                    table.addView(row);
                }

            }
        }
    }
}
