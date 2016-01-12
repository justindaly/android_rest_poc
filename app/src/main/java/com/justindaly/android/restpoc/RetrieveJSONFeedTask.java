package com.justindaly.android.restpoc;

import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.util.JsonReader;
import android.util.JsonToken;

import com.justindaly.android.restpoc.dummy.DummyContent;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * parse the rest feed url's json content.
 */
public class RetrieveJSONFeedTask extends AsyncTask<String, Void, String> {

    protected RecyclerView.Adapter a;
    protected Throwable t;

    public RetrieveJSONFeedTask(RecyclerView.Adapter a) {
        this.a = a;
    }

    protected String doInBackground(String... params) {
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(params[0]);
            urlConnection = (HttpURLConnection) url.openConnection();
            InputStream in = new BufferedInputStream(urlConnection.getInputStream());
            JsonReader jr = new JsonReader(new InputStreamReader(in));

            // parse results as:
            // {
            //   "query": {
            //     ..,
            //     "results": {
            //       "item": [
            //         ..,
            //         "title", "description", "link"
            jr.beginObject();
            while (jr.hasNext()) {
                String name = jr.nextName();
                boolean isNull = jr.peek() == JsonToken.NULL;
                if (name.equals("query") && ! isNull) {

                    jr.beginObject();
                    while (jr.hasNext()) {
                        name = jr.nextName();
                        isNull = jr.peek() == JsonToken.NULL;

                        if (name.equals("results") && !isNull) {
                            jr.beginObject();
                            while (jr.hasNext()) {
                                name = jr.nextName();
                                isNull = jr.peek() == JsonToken.NULL;
                                if (name.equals("item") && !isNull) {
                                    jr.beginArray();

                                    while (jr.hasNext()) {
                                        jr.beginObject();
                                        String id = null;
                                        String content = null;
                                        String details = null;

                                        while (jr.hasNext()) {
                                            name = jr.nextName();
                                            if (name.equals("link")) {
                                                id = jr.nextString();
                                            } else if (name.equals("title")) {
                                                content = jr.nextString();
                                            } else if (name.equals("description")) {
                                                details = jr.nextString();
                                            } else {
                                                jr.skipValue();
                                            }
                                        }
                                        DummyContent.addItem(new DummyContent.DummyItem(id, content, details));

                                        jr.endObject();
                                    }
                                    jr.endArray();
                                } else {
                                    jr.skipValue();
                                }
                            }
                            jr.endObject();

                        } else {
                            jr.skipValue();
                        }
                    }
                    jr.endObject();
                } else {
                    jr.skipValue();
                }
            }
            jr.endObject();


        } catch (IOException ioe) {
            t = ioe;
            return "error";
        } finally {
            if(urlConnection != null) {
                urlConnection.disconnect();
            }
        }

        return "success";
    }

    protected void onPostExecute(String result) {
        if (result.equals("success")) {
            a.notifyDataSetChanged();
        }

    }

}
