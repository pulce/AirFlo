package com.airflo;

import android.app.Dialog;
import android.content.Context;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.airflo.helpers.OnlyContext;
import com.google.android.gms.common.GoogleApiAvailability;

/**
 * 
 * This Class is part of AirFlo.
 * 
 * It adds an About Dialog to the given context.
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
public class AboutDialog {

	public static void makeDialog(Context context, String versionID) {
		final Dialog dialog = new Dialog(context);
		dialog.setCanceledOnTouchOutside(true);
		dialog.setContentView(R.layout.about);
		dialog.setTitle(dialog.getContext().getString(R.string.about));

		TextView text = (TextView) dialog.findViewById(R.id.text);
		text.setText(context.getString(R.string.about_airflo_version) + versionID);

		TextView mpchart = (TextView) dialog.findViewById(R.id.mpchart_text);
		mpchart.setText(Html.fromHtml(OnlyContext.getContext().getString(R.string.mp_chart_notice)));
		mpchart.setMovementMethod(LinkMovementMethod.getInstance());

		TextView mapquest = (TextView) dialog.findViewById(R.id.mapquest_text);
		mapquest.setText(Html.fromHtml(OnlyContext.getContext().getString(R.string.mapquest_notice)));
		mapquest.setMovementMethod(LinkMovementMethod.getInstance());

		TextView goog = (TextView) dialog.findViewById(R.id.google_text);
		goog.setText(context.getString(R.string.glide_cp) + "\n" + context.getString(R.string.about_google_api_legal) + "\n");

		final Button googButton = (Button)dialog.findViewById(R.id.googButton);


		Button confirmButton = (Button) dialog.findViewById(R.id.confButton);
		googButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				TextView googLong = (TextView) dialog.findViewById(R.id.google_loong_text);
				googLong.setText(GoogleApiAvailability.getInstance().getOpenSourceSoftwareLicenseInfo(OnlyContext.getContext()));
				googButton.setVisibility(Button.INVISIBLE);
			}
		});

		confirmButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});
		dialog.show();
	}
}
