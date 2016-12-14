package com.junior.dwan.photogallery;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;

import java.util.ArrayList;

/**
 * Created by Might on 14.12.2016.
 */

public class PhotoGalleryFragment extends Fragment {
    GridView mGridView;
    ArrayList<GalleryItem> mGalleryItems;
    private static final String TAG = "PhotoGalleryFragment";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        new FetchItemTask().execute();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_photo_gallery, container, false);

        mGridView = (GridView) v.findViewById(R.id.gridView);
        setupAdapter();
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
            return new FlickrFetchr().fetchItems();
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
            mGridView.setAdapter(new ArrayAdapter<GalleryItem>(getActivity(),
                    android.R.layout.simple_gallery_item, mGalleryItems));
        } else {
            mGridView.setAdapter(null);
        }

    }
}
