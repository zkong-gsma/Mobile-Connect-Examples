package org.example.mnv_client;

import android.content.Context;
import android.content.Intent;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.SslErrorHandler;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class webview extends AppCompatActivity {
    private WebView webView;
    private String cookie;
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.webview);

        webView = (WebView) findViewById(R.id.webView);

        if (Build.VERSION.SDK_INT >= 19)
        {
            webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        }
        else
        {
            webView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }

        webView.setScrollBarStyle(WebView.SCROLLBARS_INSIDE_OVERLAY);
        webView.setFocusable(true);
        webView.setFocusableInTouchMode(true);
        webView.requestFocus(View.FOCUS_DOWN);

        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        webView.getSettings().setSupportMultipleWindows(false);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setDatabaseEnabled(true);
        webView.getSettings().setSupportZoom(false);
        webView.getSettings().setUseWideViewPort(false);

        String databasePath = this.getApplicationContext().getDir("database", Context.MODE_PRIVATE).getPath();
        webView.getSettings().setDatabasePath(databasePath);

        webView.setWebViewClient(new WebViewClient() {
            @Override
		      /*
		       * This is to handle 302 redirect
		       */
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
		    	/*
		    	 * the server redirect_uri.jsp will redirect to http://complete/?xxxxxx
		    	 * exit webview and return result when match.
		    	 */
                if (url.startsWith("http://complete")) {
                    Intent returnIntent = new Intent();
                    String result = url.substring(url.indexOf("?") + 1, url.length());
                    returnIntent.putExtra("result",result);
                    setResult(RESULT_OK, returnIntent);

                    finish();
                    return true;
                }
                //return false to force webview to load itself
                return false;
            }

            @Override
		      /*
		       * This is to handle ssl certificate error. (self sign certificate)
		       */
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                // this will ignore the Ssl error and proceed
                handler.proceed();
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                Log.e("error", "|" + errorCode + "|" + description + "|" + failingUrl);
            }
        });

        webView.loadUrl(getIntent().getStringExtra("url"));
    }
}
