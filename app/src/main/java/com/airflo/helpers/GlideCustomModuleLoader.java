package com.airflo.helpers;

import android.content.Context;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.module.GlideModule;

import java.io.InputStream;

/**
 *
 * This Class is part of AirFlo.
 *
 * It provides an Activity to handle flight detail preferences.
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
public class GlideCustomModuleLoader implements GlideModule {

    @Override
    public void applyOptions(Context context, GlideBuilder builder) {
    }

    @Override
    public void registerComponents(Context context, Glide glide) {
        glide.register(InputStream.class, InputStream.class, new GlideInputStreamLoader.Factory());
        //glide.register(GlideZipAddress.class, InputStream.class, new ZipLoader.Factory());
    }
}


/*
class ZipLoader implements StreamModelLoader<GlideZipAddress> {
    private final Context context;
    public ZipLoader(Context context) {
        this.context = context.getApplicationContext();
        Log.d("ZipLoader", "creating");
    }

    public boolean handles(GlideZipAddress model) {

        return true;
    }


    @Override
    public DataFetcher<InputStream> getResourceFetcher(final GlideZipAddress model, int width, int height) {
        Log.d("ZipLoader", "dataFetschercalled");
        return new ZipDataFetcher(context, model);
    }

    public static class Factory implements ModelLoaderFactory<GlideZipAddress, InputStream> {
        @Override public ModelLoader<GlideZipAddress, InputStream> build(Context context, GenericLoaderFactory factories) {
            Log.d("ZipLoader", "zipLoaderCalled");
            return new ZipLoader(context);
        }
        @Override public void teardown() {
            // nothing to do
        }
    }

    class ZipDataFetcher implements DataFetcher<InputStream> {
        private final Context context;
        private final GlideZipAddress path;
        private InputStream zipFileStream;

        public ZipDataFetcher(Context context, GlideZipAddress path) {
            Log.d("ZipLoader", "ZipDataFetcher initialized");
            this.context = context;
            this.path = path;
        }
        @Override public InputStream loadData(Priority priority) throws Exception {
            Log.d("ZipLoader", "load data called");
            SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(OnlyContext.getContext());
            String fileName = sharedPrefs.getString("flightBookName",
                    Environment.getExternalStorageDirectory().getPath() + "flightbookexample.xml");
            try {
                ZipFile zipFile = new ZipFile(fileName);
                Log.d("ZipLoader", path.zipPath.split("//")[1]);
                ZipEntry zipEntry = zipFile.getEntry(path.zipPath.split("//")[1]);
                return zipFileStream = zipFile.getInputStream(zipEntry);
            } catch(Exception e) {
                return null;
            }

        }
        @Override public void cleanup() {
            Log.d("ZipLoader", "cleanup called");
            try {
                if (zipFileStream != null) {
                    zipFileStream.close();
                }
            } catch (IOException e) {
                Log.w("OBBDataFetcher", "Cannot clean up after stream", e);
            }
        }
        @Override public String getId() {
            return context.getPackageName() + "@" + path;
        }
        @Override public void cancel() {
            // do nothing
        }
    }

}
*/