package com.airflo.helpers;

import android.content.Context;
import android.util.Log;

import com.bumptech.glide.Priority;
import com.bumptech.glide.load.data.DataFetcher;
import com.bumptech.glide.load.model.GenericLoaderFactory;
import com.bumptech.glide.load.model.ModelLoader;
import com.bumptech.glide.load.model.ModelLoaderFactory;
import com.bumptech.glide.load.model.stream.StreamModelLoader;

import java.io.IOException;
import java.io.InputStream;

/**
 * This Class is part of AirFlo.
 * <p/>
 * It provides an Activity to handle flight detail preferences.
 *
 * @author Florian Hauser Copyright (C) 2015
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
public class GlideInputStreamLoader implements StreamModelLoader<InputStream> {
    @Override
    public DataFetcher<InputStream> getResourceFetcher(final InputStream model, int width, int height) {
        return new DataFetcher<InputStream>() {
            @Override
            public InputStream loadData(Priority priority) throws Exception {
                return model;
            }

            @Override
            public void cleanup() {
                try {
                    model.close();
                } catch (IOException e) {
                    Log.w("PassthroughDataFetcher", "Cannot clean up after stream", e);
                }
            }

            @Override
            public String getId() {
                return String.valueOf(System.currentTimeMillis());
            }

            @Override
            public void cancel() {
            }
        };
    }

    public static class Factory implements ModelLoaderFactory<InputStream, InputStream> {
        @Override
        public ModelLoader<InputStream, InputStream> build(Context context, GenericLoaderFactory factories) {
            return new GlideInputStreamLoader();
        }

        @Override
        public void teardown() {
        }
    }
}