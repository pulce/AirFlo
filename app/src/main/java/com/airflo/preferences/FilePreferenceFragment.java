package com.airflo.preferences;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

import com.airflo.FragActivity;
import com.airflo.R;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

/**
 * 
 * @author Florian Hauser 
 * 		   Part of the code comes from H3R3T1C File Chooser from
 *         http://www.dreamincode.net/forums/topic/190013-creating-simple-file-
 *         chooser/
 * 
 */
public class FilePreferenceFragment extends ListFragment {

	private File currentDir;
	private FilePreferenceAdapter adapter;
	private Stack<File> dirStack = new Stack<>();

	public FilePreferenceFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d("Dir:", Environment.getExternalStorageDirectory().getAbsolutePath());
		currentDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath());
		fill(currentDir);
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		FilePreferenceAdapterOption o = adapter.getItem(position);
		if (o.getData().equalsIgnoreCase("folder")) {
			dirStack.push(currentDir);
			currentDir = new File(o.getPath());
			fill(currentDir);
		} else if (o.getData().equalsIgnoreCase("parent directory")) {
			currentDir = dirStack.pop();
			fill(currentDir);
		} else {
			onFileClick(o);
		}
	}

	private void onFileClick(FilePreferenceAdapterOption o) {
		if (!o.getName().endsWith(".zip")) {
			Toast.makeText(getActivity(),
					getString(R.string.file_pref_message_nozip), Toast.LENGTH_SHORT)
					.show();
			return;
		}
		SharedPreferences.Editor edit = PreferenceManager.getDefaultSharedPreferences(getActivity()).edit();
		edit.putString("flightBookName", o.getPath());
		edit.commit();
		if (getActivity() instanceof FragActivity) {
			getActivity().finish();
		}
	}

	public void fill(File f) {
		File[] dirs = f.listFiles();
		getActivity().setTitle(
				getString(R.string.title_file_preferece_activity) + f.getName());
		List<FilePreferenceAdapterOption> dir = new ArrayList<>();
		List<FilePreferenceAdapterOption> fls = new ArrayList<>();
		try {
			for (File ff : dirs) {
				if (ff.isDirectory()) {
					dir.add(new FilePreferenceAdapterOption(ff.getName(), "Folder", ff
							.getAbsolutePath()));
				} else {
					fls.add(new FilePreferenceAdapterOption(ff.getName(), "File Size: "
							+ ff.length(), ff.getAbsolutePath()));
				}
			}
		} catch (Exception e) {
			Log.e("File Chooser Exception", e.toString());
		}
		Collections.sort(dir);
		Collections.sort(fls);
		dir.addAll(fls);
		if (dirStack.size() > 0)
			dir.add(0, new FilePreferenceAdapterOption("..", "Parent Directory", f.getParent()));
		adapter = new FilePreferenceAdapter(getActivity(), R.layout.file_view, dir);
		this.setListAdapter(adapter);

	}

}
