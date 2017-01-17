package web.dassem.webtopdf;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebSettings;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfWriter;
import com.nononsenseapps.filepicker.FilePickerActivity;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import static web.dassem.webtopdf.R.id.webview;


public class MainActivity extends AppCompatActivity {
    private String webPageAddress = null;
    private CustomWebview myWebView;
    private EditText getWebPageAddress;
    private ProgressBar progressBar;
    private Menu menu;
    private static String url = null;
    private MainActivity mainActivity;
    private boolean firstTime = false;
    private RelativeLayout dimBackground;
    private final int FILE_CODE = 1337;
    private final int DIR_CODE = 1339;
    private Bitmap currentBitmap;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= 21) {
            myWebView.enableSlowWholeDocumentDraw();
        }
        setContentView(R.layout.activity_main);
        initialize();
        setOnActionListener();
    }


    public void showYoutubeViolationToast() {
        Toast.makeText(mainActivity, "Google policy disallows viewing youtube videos.", Toast.LENGTH_SHORT).show();
    }


    public static void setWebsiteAddress(String currentWebURL) {
        url = currentWebURL;
    }


    private void initialize() {

        mainActivity = this;
        progressBar = (ProgressBar) this.findViewById(R.id.progressBar1);
        progressBar.setMax(100);
        progressBar.setVisibility(View.GONE);
        WebsiteAnalyzerWebChromeClient websiteAnalyzerWebClient = new WebsiteAnalyzerWebChromeClient(this);
        WebsiteAnalyzerWebClient webClient = new WebsiteAnalyzerWebClient(this);
        myWebView = (CustomWebview) findViewById(webview);
        dimBackground = (RelativeLayout) findViewById(R.id.bac_dim_layout);
        myWebView.setWebViewClient(webClient);
        myWebView.setWebChromeClient(websiteAnalyzerWebClient);
        WebSettings webSettings = myWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        myWebView.getSettings().setDomStorageEnabled(true);
        webSettings.setDisplayZoomControls(false);
        webSettings.setBuiltInZoomControls(true);
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setCustomView(R.layout.action_bar_view);
        getWebPageAddress = (EditText) actionBar.getCustomView().findViewById(
                R.id.searchfield);
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                11);

        actionBar.setDisplayOptions(android.support.v7.app.ActionBar.DISPLAY_SHOW_CUSTOM | android.support.v7.app.ActionBar.DISPLAY_SHOW_HOME);
    }

    private void extractBitmap() {
        myWebView.measure(View.MeasureSpec.makeMeasureSpec(
                View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        myWebView.layout(0, 0, myWebView.getMeasuredWidth(),
                myWebView.getMeasuredHeight());
        myWebView.setDrawingCacheEnabled(true);
        myWebView.buildDrawingCache();
        currentBitmap = Bitmap.createBitmap(myWebView.getMeasuredWidth(),
                myWebView.getMeasuredHeight(), Bitmap.Config.RGB_565);

        Canvas canvas = new Canvas(currentBitmap);
        myWebView.draw(canvas);


        startChooseFileIntent(true);

    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == DIR_CODE && resultCode == Activity.RESULT_OK) {
            if (data.getBooleanExtra(FilePickerActivity.EXTRA_ALLOW_MULTIPLE, false)) {
                // For JellyBean and above
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    ClipData clip = data.getClipData();

                    if (clip != null) {
                        for (int i = 0; i < clip.getItemCount(); i++) {
                            Uri uri = clip.getItemAt(i).getUri();
                            saveFile(uri);

                        }
                    }
                    // For Ice Cream Sandwich
                } else {
                    ArrayList<String> paths = data.getStringArrayListExtra
                            (FilePickerActivity.EXTRA_PATHS);

                    if (paths != null) {
                        for (String path : paths) {
                            Uri uri = Uri.parse(path);
                            saveFile(uri);


                        }
                    }
                }

            } else {
                Uri uri = data.getData();
                saveFile(uri);

            }
        }
    }


    private void saveFile(Uri uri) {
        try {
            Document document = new Document();
            File file = new File(uri.getPath() + "/" + "site" + ".pdf");
            file.createNewFile();
            PdfWriter.getInstance(document, new FileOutputStream(file));
            document.open();
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            currentBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            com.itextpdf.text.Image image = com.itextpdf.text.Image.getInstance(stream.toByteArray());
            currentBitmap.recycle();
            if (currentBitmap.getHeight() > 14400) {
                document.setPageSize(new Rectangle(currentBitmap.getWidth(), 14400));
            } else {
                document.setPageSize(image);
            }
            document.newPage();
            image.setAbsolutePosition(0, 0);
            document.add(image);
            document.close();
            Toast.makeText(this, "File sucessfully saved in " + uri.getPath(), Toast.LENGTH_SHORT).show();

        } catch (DocumentException | IOException e) {
            e.printStackTrace();
        }
    }


    private void setOnActionListener() {
        getWebPageAddress.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                showMenu(false);
                return false;
            }
        });
        getWebPageAddress.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId,
                                          KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_GO) {
                    hideKeyboard();
                    showMenu(true);
                    getWebPageAddress.clearFocus();
                    webPageAddress = getWebPageAddress.getText() + "";
                    if (webPageAddress.startsWith("http://") || (webPageAddress.startsWith("https://"))) {
                        myWebView.loadUrl(webPageAddress);
                    } else {
                        webPageAddress = String.valueOf("http://" + getWebPageAddress.getText());
                        myWebView.loadUrl(webPageAddress);
                    }
                    progressBar.setProgress(0);
                    handled = true;

                }
                return handled;

            }
        });
        getWebPageAddress.setOnFocusChangeListener(new TextView.OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    showMenu(true);
                    hideKeyboard();
                    getWebPageAddress.clearFocus();
                }
            }
        });
    }

    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public void setEditText(String url) {
        getWebPageAddress.setText(url);
    }

    private void showMenu(Boolean hide) {
        menu.setGroupVisible(R.id.main_menu_group, hide);
        if (menu.hasVisibleItems()) {
            dimBackground.setVisibility(View.GONE);
        } else {
            dimBackground.bringToFront();
            dimBackground.setVisibility(View.VISIBLE);
        }
    }

    private int dpToPx(int dp) {
        DisplayMetrics displayMetrics = this.getResources().getDisplayMetrics();
        int px = Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
        return px;
    }

    private void startChooseFileIntent(boolean isDir) {
        Intent i = new Intent(this, FilePicker.class);
        // This works if you defined the intent filter
        // Intent i = new Intent(Intent.ACTION_GET_CONTENT);

        // Set these depending on your use case. These are the defaults.
        i.putExtra(FilePickerActivity.EXTRA_ALLOW_MULTIPLE, false);
        i.putExtra(FilePickerActivity.EXTRA_ALLOW_CREATE_DIR, false);
        if (isDir) {
            i.putExtra(FilePickerActivity.EXTRA_MODE, FilePickerActivity.MODE_DIR);
        } else {
            i.putExtra(FilePickerActivity.EXTRA_MODE, FilePickerActivity.MODE_FILE);
        }

        // Configure initial directory by specifying a String.
        // You could specify a String like "/storage/emulated/0/", but that can
        // dangerous. Always use Android's API calls to get paths to the SD-card or
        // internal memory.
        i.putExtra(FilePickerActivity.EXTRA_START_PATH, Environment.getExternalStorageDirectory().getPath());

        if (isDir) {
            startActivityForResult(i, DIR_CODE);
        } else {
            startActivityForResult(i, FILE_CODE);
        }
    }

    public void setProgressBar(int i) {
        progressBar.setProgress(i);
    }

    public void hideProgressBar() {
        progressBar.setVisibility(View.GONE);
    }

    public void showProgressBar() {
        progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        this.menu = menu;
        final View activityRootView = findViewById(R.id.activity_root);
        activityRootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int heightDiff = activityRootView.getRootView().getHeight() - activityRootView.getHeight();
                if (heightDiff > dpToPx(200)) { // if more than 200 dp, it's probably a keyboard...
                    if (!firstTime) {
                        firstTime = true;
                    }
                } else {
                    if (firstTime) {
                        showMenu(true);
                        firstTime = false;
                    }
                }
            }
        });
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.refresh) {
            if ((webPageAddress != null) && (!webPageAddress.equals(""))) {
                myWebView.loadUrl(webPageAddress);
            } else {
                myWebView.reload();
            }
        } else if (id == R.id.request_desktop) {
            item.setChecked(!item.isChecked());
            myWebView.setDesktopMode(item.isChecked());
        } else if (id == R.id.take_screenshot) {
            extractBitmap();
        }


        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onBackPressed() {
        if (myWebView.canGoBack()) {
            myWebView.goBack();

        } else {
            super.onBackPressed();
        }

    }


}
