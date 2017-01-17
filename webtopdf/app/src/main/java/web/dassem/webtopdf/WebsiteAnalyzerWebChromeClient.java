package web.dassem.webtopdf;

import android.webkit.WebChromeClient;
import android.webkit.WebView;


public class WebsiteAnalyzerWebChromeClient extends WebChromeClient {
    private MainActivity mainActivity;

    WebsiteAnalyzerWebChromeClient(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    @Override
    public void onProgressChanged(WebView view, int newProgress) {
        mainActivity.setProgressBar(newProgress);
        super.onProgressChanged(view, newProgress);
    }

}
