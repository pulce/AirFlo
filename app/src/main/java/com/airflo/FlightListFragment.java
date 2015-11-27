package com.airflo;

import java.util.ArrayList;

import android.app.Activity;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.widget.ListView;

import com.airflo.datamodel.FlightData;
import com.airflo.datamodel.FlightDataComparer;

/**
 * 
 * This Class is part of AirFlo.
 * 
 * It provides a list fragment representing a list of Flights. This fragment
 * also supports tablet devices by allowing list items to be given an
 * 'activated' state upon selection. This helps indicate which item is currently
 * being viewed in a {@link FlightDetailFragment}.
 * <p>
 * Activities containing this fragment MUST implement the {@link Callbacks}
 * interface.
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
public class FlightListFragment extends ListFragment {

	private static final String STATE_ACTIVATED_POSITION = "activated_position";
	private Callbacks mCallbacks = sDummyCallbacks;
	private int mActivatedPosition = ListView.INVALID_POSITION;
	private FlightListAdapter adapter;
	private static FlightDataComparer compi = new FlightDataComparer();

	public interface Callbacks {
		void onItemSelected(String id);
	}

	private static Callbacks sDummyCallbacks = new Callbacks() {
		@Override
		public void onItemSelected(String id) {
		}
	};

	public FlightListFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		refreshAdapter();
	}

	/**
	 * This method can be called by the activity to refresh the view. It is also
	 * responsible for sorting the list according to the preference of the user.
	 */
	public void refreshAdapter() {
		ArrayList<FlightListAdapterOption> flights = new ArrayList<>();
		String sorter = PreferenceManager.getDefaultSharedPreferences(
				getActivity()).getString("list_pref_sort", "number");
		boolean ascend = PreferenceManager
				.getDefaultSharedPreferences(getActivity())
				.getString("list_pref_order", "list_pref_sort_decending")
				.equals("list_pref_sort_ascending");
		if (sorter.contains(";")) {
			String[] sortArray = sorter.split(";");
			compi.sortListBy(sortArray).reverse(ascend);
		} else {
			compi.sortListBy(sorter).reverse(ascend);
		}
		for (FlightData.FlightDataItem item : FlightData.ITEMS) {
			FlightListAdapterOption option = new FlightListAdapterOption(
					item.getheadItem(), item.getListItem(), item.getPictureHeaders().size() > 0, item.getIgcName() != null);
			flights.add(option);
		}

		adapter = new FlightListAdapter(getActivity(),
				R.layout.flight_list_adapter, flights);
		this.setListAdapter(adapter);
	}

	public void selectOnAdapter(String id) {
		adapter.select(id);
		adapter.notifyDataSetChanged();

	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		getListView().setFastScrollEnabled(true);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		// Restore the previously serialized activated item position.
		if (savedInstanceState != null
				&& savedInstanceState.containsKey(STATE_ACTIVATED_POSITION)) {
			setActivatedPosition(savedInstanceState
					.getInt(STATE_ACTIVATED_POSITION));
		}
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		// Activities containing this fragment must implement its callbacks.
		if (!(activity instanceof Callbacks)) {
			throw new IllegalStateException(
					"Activity must implement fragment's callbacks.");
		}

		mCallbacks = (Callbacks) activity;
	}

	@Override
	public void onDetach() {
		super.onDetach();
		// Reset the active callbacks interface to the dummy implementation.
		mCallbacks = sDummyCallbacks;
	}

	@Override
	public void onListItemClick(ListView listView, View view, int position,
			long id) {
		super.onListItemClick(listView, view, position, id);
		// Notify the active callbacks interface (the activity, if the
		// fragment is attached to one) that an item has been selected.
		mCallbacks.onItemSelected(FlightData.ITEMS.get(position).content
				.get("number"));
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (mActivatedPosition != ListView.INVALID_POSITION) {
			// Serialize and persist the activated item position.
			outState.putInt(STATE_ACTIVATED_POSITION, mActivatedPosition);
		}
    }

	/**
	 * Turns on activate-on-click mode. When this mode is on, list items will be
	 * given the 'activated' state when touched.
	 */
	public void setActivateOnItemClick(boolean activateOnItemClick) {
		// When setting CHOICE_MODE_SINGLE, ListView will automatically
		// give items the 'activated' state when touched.
		getListView().setChoiceMode(
				activateOnItemClick ? ListView.CHOICE_MODE_SINGLE
						: ListView.CHOICE_MODE_NONE);
	}

	private void setActivatedPosition(int position) {
		if (position == ListView.INVALID_POSITION) {
			getListView().setItemChecked(mActivatedPosition, false);
		} else {
			getListView().setItemChecked(position, true);
		}

		mActivatedPosition = position;
	}

}
