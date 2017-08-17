# Android Force Mobile Data

Example on how to use Android Webview Client to force traffic via "Mobile Data" even when its connected to Wifi

By using @Overide on "shouldInterceptRequest"

https://developer.android.com/reference/android/webkit/WebViewClient.html#shouldInterceptRequest(android.webkit.WebView, android.webkit.WebResourceRequest)

Obtaining the Mobile Data "Network" object from ConnectivityManager.requestNetwork specifying Capability and TransportType(TRANSPORT_CELLULAR)

https://developer.android.com/reference/android/net/ConnectivityManager.html#requestNetwork(android.net.NetworkRequest, android.net.ConnectivityManager.NetworkCallback)

Then with the Network object of "Mobile Data", perform a

Network.openConnection 

https://developer.android.com/reference/android/net/Network.html#openConnection(java.net.URL)

and return response as "WebResourceResponse" to the "shouldInterceptRequest"

