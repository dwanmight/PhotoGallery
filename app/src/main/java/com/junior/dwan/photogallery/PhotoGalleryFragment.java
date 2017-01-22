package com.junior.dwan.photogallery;

import android.app.Activity;
import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.SearchView;

import java.util.ArrayList;

/**
 * Created by Might on 14.12.2016.
 */

public class PhotoGalleryFragment extends VisibleFragment {
    GridView mGridView;
    ArrayList<GalleryItem> mGalleryItems;
    ThumbnailDownloader<ImageView> mThumbnailDownloader;
    private static final String TAG = "PhotoGalleryFragment";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setHasOptionsMenu(true);
        updateItem();

//        Intent i=new Intent(getActivity(),PollService.class);
//        getActivity().startService(i);

//        PollService.setServiceAlarm(getActivity(),true);

//        mThumbnailDownloader=new ThumbnailDownloader<>();
        mThumbnailDownloader = new ThumbnailDownloader<>(new Handler());
        mThumbnailDownloader.setListener(new ThumbnailDownloader.Listener<ImageView>() {
            @Override
            public void onThumbnailDownloaded(ImageView imageView, Bitmap thumbnail) {
                if (isVisible()) {
                    imageView.setImageBitmap(thumbnail);
                }
            }
        });
        mThumbnailDownloader.start();
        mThumbnailDownloader.getLooper();
        Log.i(TAG, "Background thread started");

    }

    public void updateItem() {
        new FetchItemTask().execute();
    }

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_photo_gallery, container, false);

        mGridView = (GridView) v.findViewById(R.id.gridView);
        setupAdapter();
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                GalleryItem galleryItem = mGalleryItems.get(position);

                Uri photoPageUri = Uri.parse(galleryItem.getPhotoPageUrl());
//                Intent intent=new Intent(Intent.ACTION_VIEW, photoPageUri);
                Intent intent=new Intent(getActivity(),PhotoPageActivity.class);
                intent.setData(photoPageUri);
                startActivity(intent);
            }
        });
        return v;
    }

    private class FetchItemTask extends AsyncTask<Void, Void, ArrayList<GalleryItem>> {


        @Override
        protected ArrayList<GalleryItem> doInBackground(Void... params) {
//            try {
//                String result=new FlickrFetchr().getUrl("https://www.google.com.ua");
//                Log.i(TAG,"Fetched contents from url:" + result);
//            } catch (IOException e) {
//                Log.i(TAG,"failed to fetch URL: "+e);
//                e.printStackTrace();
//            }
            Activity activity = getActivity();
            if (activity == null) {
                return new ArrayList<GalleryItem>();
            }

//            String query = "android"; // Только для тестирования
            String query = PreferenceManager.getDefaultSharedPreferences(activity)
                    .getString(FlickrFetchr.PREF_SEARCH_QUERY, null);
            if (query != null) {
                return new FlickrFetchr().search(query);
            } else {
                return new FlickrFetchr().fetchItems();
            }
        }

        @Override
        protected void onPostExecute(ArrayList<GalleryItem> items) {
            mGalleryItems = items;
            setupAdapter();

        }
    }

    void setupAdapter() {
        if (getActivity() == null && mGridView == null) return;
        if (mGalleryItems != null) {
//            mGridView.setAdapter(new ArrayAdapter<GalleryItem>(getActivity(),
//                    android.R.layout.simple_gallery_item, mGalleryItems));
            mGridView.setAdapter(new GalleryItemAdapter(mGalleryItems));
        } else {
            mGridView.setAdapter(null);
        }

    }

    private class GalleryItemAdapter extends ArrayAdapter<GalleryItem> {

        public GalleryItemAdapter(ArrayList<GalleryItem> items) {
            super(getActivity(), 0, items);
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getActivity().getLayoutInflater().inflate(R.layout.gallery_item, parent, false);
            }

            ImageView imageView = (ImageView) convertView.findViewById(R.id.gallery_item_ImageView);
            imageView.setImageResource(R.drawable.brian_up_close);
            GalleryItem item = getItem(position);
            mThumbnailDownloader.queueThumbnail(imageView, item.getUrl());

            return convertView;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mThumbnailDownloader.quit();
        Log.i(TAG, "Background thread destroyed");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mThumbnailDownloader.clearQueue();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_photo_gallery, menu);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            // Получение SearchView
            MenuItem searchItem = menu.findItem(R.id.menu_item_search);
            SearchView searchView = (SearchView) searchItem.getActionView();

            // Получение данных из файла searchable.xml
            // в виде объекта SearchableInfo
            SearchManager searchManager = (SearchManager) getActivity()
                    .getSystemService(Context.SEARCH_SERVICE);
            ComponentName name = getActivity().getComponentName();
            SearchableInfo searchableInfo = searchManager.getSearchableInfo(name);

            searchView.setSearchableInfo(searchableInfo);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_search:
                getActivity().onSearchRequested();
                return true;
            case R.id.menu_item_clear:
                PreferenceManager.getDefaultSharedPreferences(getActivity())
                        .edit()
                        .putString(FlickrFetchr.PREF_SEARCH_QUERY, null)
                        .apply();
                updateItem();
                return true;
            case R.id.menu_item_toggle_polling:
                boolean shouldStartAlarm = !PollService.isServiceAlarmOn(getActivity());
                PollService.setServiceAlarm(getActivity(), shouldStartAlarm);

                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB) {
                    getActivity().invalidateOptionsMenu();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);

        }
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        MenuItem menuItem = (MenuItem) menu.findItem(R.id.menu_item_toggle_polling);
        if (PollService.isServiceAlarmOn(getActivity())) {
            menuItem.setTitle(R.string.stop_polling);
        } else {
            menuItem.setTitle(R.string.start_polling);
        }
    }
}
