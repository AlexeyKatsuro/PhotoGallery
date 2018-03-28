package com.dedalexey.myapplication1111;


import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class FlickrFetchr {

    private static final String TAG = "FlickrFetchr";
    private static final String API_KEY = "f7a3c51cd96d1f12c91b629294072bc9";
    private static final String FETCH_RECENTS_METHOD = "flickr.photos.getRecent";
    private static final String SEARCH_METHOD = "flickr.photos.search";

    private int mPage=0;
    private static final Uri ENDPOINT = Uri
            .parse("https://api.flickr.com/services/rest/")
            .buildUpon()
            .appendQueryParameter("api_key", API_KEY)
            .appendQueryParameter("format", "json")
            .appendQueryParameter("nojsoncallback", "1")
            .appendQueryParameter("extras", "url_s")
            .build();


    public byte[] getUrlBytes(String urlSpec) throws IOException {
        URL url = new URL(urlSpec);
        HttpURLConnection connection = (HttpURLConnection)url.openConnection();
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            InputStream in = connection.getInputStream();
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new IOException(connection.getResponseMessage() +
                        ": with " +
                        urlSpec);
            }
            int bytesRead = 0;
            byte[] buffer = new byte[1024];
            while ((bytesRead = in.read(buffer)) > 0) {
                out.write(buffer, 0, bytesRead);
            }
            out.close();
            return out.toByteArray();
        } finally {
            connection.disconnect();
        }
    }
    public String getUrlString(String urlSpec) throws IOException {
        return new String(getUrlBytes(urlSpec));
    }

    private List<GalleryItem> downloadGalleryItems(String url) {

        List<GalleryItem> items = new ArrayList<>();
        try {
//            String url = Uri.parse("https://api.flickr.com/services/rest/")
//                    .buildUpon()
//                    .appendQueryParameter("method", "flickr.photos.getRecent")
//                    .appendQueryParameter("api_key", API_KEY)
//                    .appendQueryParameter("format", "json")
//                    .appendQueryParameter("page", String.valueOf(page))
//                    .appendQueryParameter("nojsoncallback", "1")
//                    .appendQueryParameter("extras", "url_s")
//                    .build().toString();
            String jsonString = getUrlString(url);
            Log.i(TAG, "Received JSON: " + jsonString);
            PhotoRequestResult gsonParse = new GsonBuilder().create().fromJson(jsonString,PhotoRequestResult.class);
            //JSONObject jsonBody = new JSONObject(jsonString);
            //parseItems(items, jsonBody);
            items = gsonParse.getResults();
            Log.i(TAG,"gsonParse.getItemCount() "+String.valueOf(gsonParse.getItemCount()));
        } catch (IOException ioe) {
            Log.e(TAG, "Failed to fetch items", ioe);
        }
        //catch (JSONException je) {
//            Log.e(TAG, "Failed to parse JSON", je);
//        }

        return items;
    }

    public FlickrFetchr setPage(int page) {
        mPage = page;
        return this;
    }

    private  String buildUrl(String method, String query){
        Uri.Builder urlBuilder = ENDPOINT.buildUpon()
                .appendQueryParameter("method",method)
                .appendQueryParameter("page",String.valueOf(mPage));

        if(method.equals(SEARCH_METHOD)){
            urlBuilder.appendQueryParameter("text",query);
        }
        return urlBuilder.build().toString();
    }

    public List<GalleryItem> fetchRecentPhotos(){
        String uri = buildUrl(FETCH_RECENTS_METHOD, null);
        return downloadGalleryItems(uri);
    }

    public List<GalleryItem> searchPhotos(String query){
        String uri = buildUrl(SEARCH_METHOD, query);
        return downloadGalleryItems(uri);
    }

    private void parseItems(List<GalleryItem> items, JSONObject jsonBody)
            throws IOException, JSONException {

        JSONObject photosJsonObject = jsonBody.getJSONObject("photos");
        JSONArray photoJsonArray = photosJsonObject.getJSONArray("photo");

        for (int i = 0; i < photoJsonArray.length(); i++) {
            JSONObject photoJsonObject = photoJsonArray.getJSONObject(i);

            GalleryItem item = new GalleryItem();
            item.setId(photoJsonObject.getString("id"));
            item.setCaption(photoJsonObject.getString("title"));
            
            if (!photoJsonObject.has("url_s")) {
                continue;
            }
            
            item.setUrl(photoJsonObject.getString("url_s"));
            items.add(item);
        }
    }

}
