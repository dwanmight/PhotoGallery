package com.junior.dwan.photogallery;

import android.net.Uri;
import android.util.Log;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by Might on 14.12.2016.
 */

public class FlickrFetchr {

    public static final String TAG = "FlickrFetchr";

    public static final String API_KEY = "e1256a6bb08d3c4179da0c4e9b3c497d";
    public static final String END_POINT = "https://api.flickr.com/services/rest/";
    public static final String METHOD_GET_RECENT = "flickr.photos.getRecent";
    public static final String EXTRA_PARAMS = "extras";

    public static final String EXTRA_SMALL_URL = "url_s";

    public static final String XML_PHOTO = "photo";


    byte[] getUrlBytes(String urlSpec) throws IOException {
        URL url = new URL(urlSpec);
        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();

        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            InputStream inputStream = httpURLConnection.getInputStream();
            if (httpURLConnection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                return null;
            }
            int bytesRead = 0;
            byte[] buffer = new byte[1024];
            while ((bytesRead = inputStream.read(buffer)) > 0) {
                byteArrayOutputStream.write(buffer, 0, bytesRead);
            }
            byteArrayOutputStream.close();
            return byteArrayOutputStream.toByteArray();

        } finally {
            httpURLConnection.disconnect();
        }
    }


    public String getUrl(String urlSpec) throws IOException {
        return new String(getUrlBytes(urlSpec));
    }

    //    public void fetchItems(){
    public ArrayList<GalleryItem> fetchItems() {
        ArrayList<GalleryItem> items = new ArrayList<>();
        String url = Uri.parse(END_POINT).buildUpon()
                .appendQueryParameter("method", METHOD_GET_RECENT)
                .appendQueryParameter("api_key", API_KEY)
                .appendQueryParameter(EXTRA_PARAMS, EXTRA_SMALL_URL).build().toString();

        try {
            String xmlString = getUrl(url);
            Log.i(TAG, "Receiver xml : " + xmlString);
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = factory.newPullParser();
            parser.setInput(new StringReader(xmlString));
            parseitems(items, parser);

        } catch (IOException e) {
            Log.i(TAG, "Failed fetch to xml!", e);
            e.printStackTrace();
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        }
        return items;
    }

    void parseitems(ArrayList<GalleryItem> items, XmlPullParser parser) throws XmlPullParserException, IOException {
        int evenType = parser.next();

        while (evenType != XmlPullParser.END_DOCUMENT) {
            if (evenType == XmlPullParser.START_TAG && XML_PHOTO.equals(parser.getName())) {
                String id = parser.getAttributeValue(null, "id");
                String caption = parser.getAttributeValue(null, "title");
                String smallUrl = parser.getAttributeValue(null, EXTRA_SMALL_URL);

                GalleryItem item = new GalleryItem();
                item.setId(id);
                item.setCaption(caption);
                item.setUrl(smallUrl);
                items.add(item);
            }

            evenType = parser.next();
        }
    }

}
