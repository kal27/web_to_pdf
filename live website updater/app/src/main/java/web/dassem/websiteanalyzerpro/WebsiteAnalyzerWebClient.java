package web.dassem.websiteanalyzerpro;

import android.graphics.Bitmap;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;


public class WebsiteAnalyzerWebClient extends WebViewClient {
    private MainActivity mainActivity;

    public WebsiteAnalyzerWebClient(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }


    @Override
    public void onPageFinished(WebView view, String url) {
        mainActivity.hideProgressBar();
        if (!url.equalsIgnoreCase("about:blank")) {
            MainActivity.setWebsiteAddress(url);
        }

        super.onPageFinished(view, url);
    }


    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        if (url.toLowerCase().contains("youtube")) {
            mainActivity.showYoutubeViolationToast();
            return true;
        } else {
            if ((url.startsWith("http://")) || (url.startsWith("https://"))) {
                view.loadUrl(url);
                mainActivity.setEditText(url);
            } else {
                view.loadUrl("http://" + url);
                mainActivity.setEditText("http://" + url);
            }
        }
        return true;
    }


    @Override
    public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
        return super.shouldInterceptRequest(view, request);
    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        mainActivity.setProgressBar(0);
        mainActivity.showProgressBar();
        super.onPageStarted(view, url, favicon);
    }

}
