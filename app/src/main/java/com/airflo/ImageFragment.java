package com.airflo;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.bumptech.glide.DrawableTypeRequest;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;

import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 *
 * This Class is part of Airflo.
 *
 *
 * @author Florian Hauser Copyright (C) 2015
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

public class ImageFragment extends Fragment {

	private ProgressBar pBar;
	private Matrix matrix = new Matrix();
	private Matrix savedMatrix = new Matrix();
	private PointF startPoint = new PointF();
	private PointF midPoint = new PointF();
	private float oldDist = 1f;
	static final int NONE = 0;
	static final int DRAG = 1;
	static final int ZOOM = 2;
	int mode = NONE;
	private int viewWidth = -1;
	private int viewHeight = -1;
	private int picWidth = -1;
	private int picHeight = -1;
	private int boxWidth = -1;
	private int boxHeigth = -1;

	private static final float ZOOMTRIGGER = 1.5f;
	private boolean maxZoomReached = false;
	private boolean zoomMode;
	private boolean loaded = false;
	private boolean ready = false;

	private float scaleFactor = 1;
	private SimpleTarget<GlideDrawable> glideTarget;
	private GlideDrawable tmpDrawable;

	private String url;

	private ViewTreeObserver vto;

	private SwipeSwitcher swipeSwitcher = dummy;

	public ImageFragment() {
	}


	public interface SwipeSwitcher {
		void swichSwipeAbility(boolean able);
	}

	private static SwipeSwitcher dummy = new SwipeSwitcher() {
		@Override
		public void swichSwipeAbility(boolean able) {
		}
	};

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		// Activities containing this fragment must implement its callbacks.
		if (!(activity instanceof SwipeSwitcher)) {
			throw new IllegalStateException(
					"Activity must implement fragment's callbacks.");
		}
		swipeSwitcher = (SwipeSwitcher) activity;
	}

	@Override
	public void onDetach() {
		super.onDetach();
		// Reset the active callbacks interface to the dummy implementation.
		swipeSwitcher = dummy;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		url = getArguments().getString("urrrl");
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		cleanup();
		View rootView = inflater
				.inflate(R.layout.fragment_image, container, false);
		pBar = (ProgressBar) rootView.findViewById(R.id.pbProgess);
		final ImageView imageDetail = (ImageView) rootView.findViewById(R.id.glideImage);
		loadGlideImage(imageDetail, zoomMode);

		imageDetail.setOnTouchListener(new View.OnTouchListener() {
            long lastClickTime = 0;

            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final ImageView view = (ImageView) v;
                if ((event.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_DOWN) {
                    lastClickTime = System.currentTimeMillis();
                    savedMatrix.set(matrix);
                    startPoint.set(event.getX(), event.getY());
                    mode = DRAG;
                    //Log.d("Mode", "Drag");
                    return (true);
                } else if ((event.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_UP) {
                    mode = NONE;
                    //Log.d("Mode", "None");
                    if ((lastClickTime != 0) && (System.currentTimeMillis() - lastClickTime < 100) &&
                            (Math.abs(event.getX() - startPoint.x) + Math.abs(event.getY() - startPoint.y)) < 50) {
                        zoomMode = !zoomMode;
                        swipeSwitcher.swichSwipeAbility(!zoomMode);
                        loadGlideImage(imageDetail, zoomMode);
                        return (true);
                    }
                    lastClickTime = 0;
                    if (scaleFactor != 1 && ready) {
                        updateImageDrawable(tmpDrawable, view);
                    }
                }
                if (!zoomMode) return (true);

                switch (event.getAction() & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_POINTER_DOWN:
                        oldDist = spacing(event);
                        if (oldDist > 10f) {
                            savedMatrix.set(matrix);
                            midPoint(midPoint, event);
                            mode = ZOOM;
                            //Log.d("Mode", "Zoom");
                        }
                        break;

                    case MotionEvent.ACTION_POINTER_UP:
                        mode = NONE;
                        //Log.d("Mode", "None");
                        break;

                    case MotionEvent.ACTION_MOVE:
                        if (mode == DRAG) {
                            matrix.set(savedMatrix);
                            matrix.postTranslate(event.getX() - startPoint.x,
                                    event.getY() - startPoint.y);
                            //Log.d("Draaag", "" + matrix.toString());
                        } else if (mode == ZOOM) {
                            float newDist = spacing(event);
                            if (newDist > 10f) {
                                matrix.set(savedMatrix);
                                float scale = newDist / oldDist;
                                matrix.postScale(scale, scale, midPoint.x,
                                        midPoint.y);
                                matrix.postTranslate(event.getX()
                                        - startPoint.x, event.getY()
                                        - startPoint.y); // new
                                //Log.d("Zooooom", "" + matrix.mapRadius(1f));
                            }
                        }
                        break;
                }

                if (matrix.mapRadius(1f) > ZOOMTRIGGER && !loaded && !maxZoomReached) {
                    Log.d("resizing", "triggered");
                    if (((float) Math.max(boxWidth, boxHeigth) * ZOOMTRIGGER) > ImageActivity.maxZoomPix) {
                        Log.d("Max Zoom", " reached: " + (float) Math.max(boxWidth, boxHeigth) * ZOOMTRIGGER);
                        if (boxWidth > boxHeigth)
                            scaleFactor = (float) ImageActivity.maxZoomPix / (float) boxWidth;
                        else
                            scaleFactor = (float) ImageActivity.maxZoomPix / (float) boxHeigth;
                        maxZoomReached = true;
                    } else {
                        Log.d("resizing", "" + ZOOMTRIGGER);
                        scaleFactor = ZOOMTRIGGER;
                    }
                    boxHeigth = (int) ((float) boxHeigth * scaleFactor);
                    boxWidth = (int) ((float) boxWidth * scaleFactor);
                    glideTarget = new CommonGlideTarget(imageDetail, boxWidth, boxHeigth);
                    getGlide(url, true).fitCenter().into(glideTarget);
                    loaded = true;
                }
                view.setImageMatrix(matrix);


                return true;


            }

            private float spacing(MotionEvent event) {
                float x = event.getX(0) - event.getX(1);
                float y = event.getY(0) - event.getY(1);
                return (float) Math.sqrt(x * x + y * y);
            }

            private void midPoint(PointF point, MotionEvent event) {
                float x = event.getX(0) + event.getX(1);
                float y = event.getY(0) + event.getY(1);
                point.set(x / 2, y / 2);
            }
        });

		return rootView;
	}

	@Override
	public void onResume() {
		final ImageView imageDetail = (ImageView) this.getActivity().findViewById(R.id.glideImage);
		vto = imageDetail.getViewTreeObserver();
		vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                //Log.d("viewsize", "" + imageDetail.getHeight() + " " + imageDetail.getWidth());
                if (imageDetail.getHeight() > 0) {
                    viewHeight = imageDetail.getHeight();
                    viewWidth = imageDetail.getWidth();
                }
            }
        });
		super.onResume();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt("viewHeigth", viewHeight);
		outState.putInt("viewWidth", viewWidth);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		if(savedInstanceState != null) {
			viewWidth = savedInstanceState.getInt("viewWidth");
			viewHeight = savedInstanceState.getInt("viewHeight");
		}
	}

	class CommonGlideTarget extends SimpleTarget<GlideDrawable> {
		private ImageView targetView;
		public CommonGlideTarget(ImageView targetView, int x, int y) {
			super(x,y);
			this.targetView = targetView;
		}

		@Override
		public void onResourceReady(GlideDrawable resource, GlideAnimation<? super GlideDrawable> glideAnimation) {
			if (mode == NONE) {
				updateImageDrawable(resource, targetView);
			} else {
				tmpDrawable = resource;
				ready = true;
			}
		}
	}

	private void updateImageDrawable(GlideDrawable drawable, ImageView view) {
		if (zoomMode) {
			float[] values = new float[9];
			matrix.getValues(values);
			values[0] = values[0] / scaleFactor;
			values[4] = values[4] / scaleFactor;
			matrix.setValues(values);
			view.setImageMatrix(matrix);
			view.setImageDrawable(drawable);
			scaleFactor = 1;
			loaded = false;
		}
		glideTarget = null;
	}

	private void loadGlideImage(final ImageView view, final boolean zoomMode) {
		cleanup();
		pBar.setVisibility(ProgressBar.VISIBLE);
		if (zoomMode) {
			float scale;
			if (viewHeight == picHeight)
				scale = (float) viewWidth/ (float) picWidth;
			else
				scale = (float) viewHeight/ (float) picHeight;
			boxHeigth = (int) ((float) viewHeight * scale);
			boxWidth = (int) ((float) viewWidth * scale);
		}
		DrawableTypeRequest requestManager = getGlide(url, zoomMode);
        if (null == requestManager) {
            Drawable error = ContextCompat.getDrawable(getActivity(), R.drawable.ic_error_red_48dp);
            view.setImageDrawable(error);
            if (viewWidth > 0) {
                matrix.postTranslate((viewWidth - error.getIntrinsicWidth()) / 2, (viewHeight - error.getIntrinsicHeight()) / 2);
                view.setImageMatrix(matrix);
            }
            pBar.setVisibility(ProgressBar.GONE);
            maxZoomReached = true;
            return;
        }
        requestManager.fitCenter().listener(new RequestListener<Object, GlideDrawable>() {
            @Override
            public boolean onException(Exception e, Object model, Target<GlideDrawable> target, boolean isFirstResource) {
                return false;
            }

            @Override
            public boolean onResourceReady(GlideDrawable resource, Object model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                picHeight = resource.getIntrinsicHeight();
                picWidth = resource.getIntrinsicWidth();
                //Log.d("imagesizexxxxx", "" + picHeight + " " + picWidth);
                //Log.d("viewsizexxxxxx", "" + viewHeight + " " + viewWidth);
                matrix.postTranslate((viewWidth - picWidth) / 2, (viewHeight - picHeight) / 2);
                view.setImageMatrix(matrix);
                pBar.setVisibility(ProgressBar.GONE);
                return false;
            }
        }).into(view);
    }

	private void cleanup() {
		matrix = new Matrix();
		savedMatrix = new Matrix();
		startPoint = new PointF();
		midPoint = new PointF();
		oldDist = 1f;
		maxZoomReached = false;
		loaded = false;
	}

	private DrawableTypeRequest getGlide(String url, boolean zoomMode) {
		DrawableTypeRequest requestManager;
		if (url.startsWith("zip")) {
            //requestManager = (DrawableTypeRequest) Glide.with(this).load(new GlideZipAddress(url)).diskCacheStrategy(ImageActivity.diskCacheStrategy);
			SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
			String fileName = sharedPrefs.getString("flightBookName",
					Environment.getExternalStorageDirectory().getPath() + "flightbookexample.xml");
			try {
				ZipFile zipFile = new ZipFile(fileName);
				ZipEntry zipEntry = zipFile.getEntry(url.split("//")[1]);
				if (zipEntry == null)
                    return null;
                else
        		    requestManager = (DrawableTypeRequest) Glide.with(this).load(zipFile.getInputStream(zipEntry)).error(R.drawable.ic_error_red_48dp).diskCacheStrategy(DiskCacheStrategy.NONE);
			} catch(Exception e) {
				Log.e("Ziploaderror:", e.toString());
                return null;
			}
		} else {
			requestManager = (DrawableTypeRequest) Glide.with(this).load(url).error(R.drawable.ic_error_red_48dp).diskCacheStrategy(ImageActivity.diskCacheStrategy);
		}
		if (zoomMode) {
			requestManager.override(boxWidth, boxHeigth);
		}
		return requestManager;
	}
}
