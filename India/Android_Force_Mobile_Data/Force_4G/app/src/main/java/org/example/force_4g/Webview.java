package org.example.force_4g;

import android.content.Context;
import android.net.Network;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;


public class Webview extends AppCompatActivity {
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
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                Log.i("=shouldInterceptReq=", request.getUrl().toString());

                Network interfaceToUse;
                switch (getIntent().getStringExtra("network")) {
                    case "normal":
                        return null;
                    case "mobile_data":
                        interfaceToUse = MainActivity.getCellularNetwork();
                        break;
                    default:
                        return null;
                }
                    try {
                        Log.i("==Webview=", "start");
                        HttpURLConnection connection
                                = (HttpURLConnection)interfaceToUse.openConnection(new URL(request.getUrl().toString()));
                        connection.setInstanceFollowRedirects(true);
                        connection.setChunkedStreamingMode(0);
                        connection.connect();
                        Log.i("==Webview=", "stop");

                        return new WebResourceResponse(
                                    connection.getContentType(),
                                    connection.getContentEncoding(),
                                    connection.getInputStream());
                    } catch (final IOException e) {
                        return null;
                    }
            }

            @Override
            public void onPageFinished(WebView view, String url) {
            }

        });

        webView.loadUrl(getIntent().getStringExtra("url"));
    }
}