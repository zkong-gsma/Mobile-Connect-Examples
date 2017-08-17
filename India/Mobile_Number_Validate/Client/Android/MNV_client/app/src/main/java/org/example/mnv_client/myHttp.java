package org.example.mnv_client;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class myHttp extends AsyncTask<String, Void, String> {
    public MainActivity activity;
    private String callback;

    myHttp(MainActivity a, String cb) {
            super();
            activity = a;
            callback = cb;
        }

    @Override
    protected void onPreExecute() {
    }

            @Override
            protected String doInBackground(String... params) {
                String resString = null;
                try {
                    URL url = new URL(params[0]);
                    URLConnection urlConnection = url.openConnection();
                    //if(params.length > 1 && params[1] != null) {
                    //    urlConnection.addRequestProperty("Cookie", "PHPSESSID=" + params[1]);
                    //}
                    InputStream is = new BufferedInputStream(urlConnection.getInputStream());

                    BufferedReader reader = new BufferedReader(new InputStreamReader(is, "iso-8859-1"), 8);
                    StringBuilder sb = new StringBuilder();
                    String line = null;
                    while ((line = reader.readLine()) != null) // Read line by line
                        sb.append(line + "\n");

                    resString = sb.toString(); // Result is here
                    is.close(); // Close the stream

                } catch (UnsupportedEncodingException e) {
                } catch (MalformedURLException e) {
                } catch (IOException e) {
                }

                return resString;
            }

    @Override
    protected void onPostExecute(String result)
    {
         activity.callback(callback, result);
    }
}

