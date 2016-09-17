package com.airflo;

import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.android.gms.maps.model.UrlTileProvider;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class MapsActivity extends AppCompatActivity {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private ArrayList<LatLng> pois = new ArrayList<>();

    private String[] tileServer;
    private String[] mapNames;

    private TileOverlay mSelectedTileOverlay;

    private SharedPreferences sharedPrefs;

    private static final String TILEPREF = "airflo_tile_preference";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        tileServer = getResources().getStringArray(R.array.tileservers);
        mapNames = getResources().getStringArray(R.array.mapnames);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        setTitle(getIntent().getStringExtra(FlightDetailFragment.FLIGHTTITLE));

        String fileName = sharedPrefs.getString("flightBookName",
                Environment.getExternalStorageDirectory().getPath() + "flightbookexample.xml");
        File file = new File(fileName);
        if (!file.exists()) {
            Log.e("Flightbook", "File does not exist.");
            finish();
            return;
        }

        try {
            ZipFile zipFile = new ZipFile(fileName);
            try {
                ZipEntry zipEntry = zipFile.getEntry(getIntent().getStringExtra(FlightDetailFragment.IGCNAME));
                if (zipEntry == null) {
                    Log.e("MapsActivity", "No igc found in zip");
                    finish();
                    return;
                }
                BufferedReader br = new BufferedReader(new InputStreamReader(zipFile.getInputStream(zipEntry)));
                String line;
                while ((line = br.readLine()) != null) {
                    if (line.startsWith("B")) {// && (cnt++ % 10 == 0)) {
                        pois.add(parseIgcLine(line));
                    }
                }
                br.close();
            } catch (Exception e){
                Log.e("Exception", e.toString());
            }
            finally {
                zipFile.close();
            }
        } catch (IOException e) {
            Log.e("IOException", e.toString());
        }
        setUpMapIfNeeded();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        int menuId=Menu.FIRST+1;
        for (int i = 0; i < tileServer.length; i++)
            menu.add(0, menuId++, i+1, mapNames[i]);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        updateOverlay(item.getOrder());
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putInt(TILEPREF, item.getOrder());
        editor.commit();
        return true;
    }

    public void updateOverlay(int order) {
        if (mMap != null) {
            if (order > tileServer.length) order = 1;
            if (mSelectedTileOverlay != null) {
                mSelectedTileOverlay.remove();
            }
            if (order <= 4)
                mMap.setMapType(order);
            else {
                mMap.setMapType(GoogleMap.MAP_TYPE_NONE);
                CustomUrlTileProvider mTileProvider = new CustomUrlTileProvider(
                        256, 256, tileServer[order - 1]);
                mSelectedTileOverlay = mMap.addTileOverlay(new TileOverlayOptions().tileProvider(mTileProvider).zIndex(0));
            }
        }
    }

    private void setUpMapIfNeeded() {
        if (mMap == null) {
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            if (mMap != null) {
                updateOverlay(sharedPrefs.getInt(TILEPREF, 1));
                PolylineOptions rectOptions = new PolylineOptions().zIndex(1).color(0x7F000000);
                if (pois.size() == 0) return;
                mMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)).position(pois.get(0)).title("Start"));
                mMap.addMarker(new MarkerOptions().position(pois.get(pois.size()-1)).title("Landing"));
                for (LatLng poi:pois) {
                    rectOptions.add(poi);
                }
                mMap.addPolyline(rectOptions);
                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(pois.get(0))
                        .zoom(10)
                        .build();
                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            }
        }
    }

    private double lonToDec(String lon) {
        int deg = Integer.parseInt(lon.substring(0, lon.length() - 5));
        int sec = Integer.parseInt(lon.substring(lon.length()-5, lon.length()));
        return (double) sec/60000 + deg;
    }

    private LatLng parseIgcLine(String line) throws ParseException {
        double lat = lonToDec(line.substring(7, 14));
        double lon = lonToDec(line.substring(15, 23));
        return new LatLng(lat,lon);
    }

    public class CustomUrlTileProvider extends UrlTileProvider {

        private String baseUrl;

        public CustomUrlTileProvider(int width, int height, String url) {
            super(width, height);
            this.baseUrl = url;
        }

        @Override
        public URL getTileUrl(int x, int y, int zoom) {
            try {
                return new URL(baseUrl.replace("{z}", "" + zoom).replace("{x}", "" + x)
                        .replace("{y}", "" + y));
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
