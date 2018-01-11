package com.ihs.inputmethod.uimodules.ui.settings.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ihs.inputmethod.language.GoogleDictionaryDownloader;
import com.ihs.inputmethod.api.language.HSDictionaryDownloader;
import com.ihs.inputmethod.api.language.HSDictionaryDownloader.IHSDownloadResultCallback;
import com.ihs.inputmethod.api.language.HSImeSubtypeListItem;
import com.ihs.inputmethod.language.api.HSImeSubtypeManager;
import com.ihs.inputmethod.uimodules.R;

public class LanguageLoadingPreference extends LinearLayout {

    public interface OnLanguageDownloadedListener {
        void onLanguageDownloaded(LanguageLoadingPreference preference);
    }
    
    private IHSDownloadResultCallback mDictDownloadCallback = HSImeSubtypeManager.getDictDownloadCallback();


    String pTitle = null;

    private TextView pTitleView;
    
    private TextView mDownloadButton;
    private ProgressBar mDownloadProgressBar;
    private OnLanguageDownloadedListener mListener;
    private String mLocale;
    private View mSegmentView;
    
    private HSDictionaryDownloader downloader;
    private Thread downloadThread;   
    private int emulateProgress = 0;

    private HSImeSubtypeListItem subtypeListItem;

    private static final int MSG_UPDATE_PROGRESS = 0;
    private static final int MSG_DOWNLOAD_COMPLETED = 1;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(final Message msg) {
            switch (msg.what) {
                case MSG_UPDATE_PROGRESS:
                    mDownloadProgressBar.setProgress(msg.arg1);
                    break;
                    
                case MSG_DOWNLOAD_COMPLETED:
                    downloadThread = null;
                    mListener.onLanguageDownloaded(LanguageLoadingPreference.this);
                    break;
            }
        }
    };
    

    @SuppressLint("Recycle")
    public LanguageLoadingPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public LanguageLoadingPreference(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }
    
    public LanguageLoadingPreference(Context context) {
        this(context, null, 0);
    }

    private void init() {
        inflate(getContext(), R.layout.language_loading_preference, this);
        pTitleView = findViewById(R.id.prefs_title);
        pTitleView.setText(pTitle);
        
        mDownloadButton = findViewById(R.id.prefs_download_button);
        mDownloadProgressBar = findViewById(R.id.prefs_download_progressbar);
        mDownloadButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (isDownloading()) {
                    return;
                }
                
                mDownloadButton.setVisibility(View.INVISIBLE);
                mDownloadProgressBar.setVisibility(View.VISIBLE);
                download();
            }
        });
    }

    private void download() {
        emulateDownload();
        if(downloader == null){
            downloader = new GoogleDictionaryDownloader(this.mLocale);
        }
        if (!HSImeSubtypeManager.dictionaryExists(mLocale)) {
            downloader.downloadDictionary(mDictDownloadCallback);
        }else{
            this.mDictDownloadCallback.onDownloadSucceded(this.mLocale);
        }
    }
    
    private void emulateDownload() {
        emulateProgress = 0;
        downloadThread = new Thread() {
            @Override
            public void run() {
                while (emulateProgress < 100) {
                    try {
                        Thread.sleep(100);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    emulateProgress += 25;
                    mHandler.sendMessage(mHandler.obtainMessage(MSG_UPDATE_PROGRESS, emulateProgress, 0));
                }
                    
                mHandler.sendMessage(mHandler.obtainMessage(MSG_DOWNLOAD_COMPLETED));
            }
        };
        downloadThread.start();
    }
    
    public void setListener(final OnLanguageDownloadedListener listener) {
        this.mListener = listener;
    }
    
    public String getTitle() {
        return pTitleView.getText().toString();
    }
    
    public void setTitle(final String title) {
        pTitleView.setText(title);
    }
    
    public String getLocale() {
        return this.mLocale;
    }
    
    public View getSegmentView() {
        return this.mSegmentView;
    }
    
    public void setSegmentView(final View view) {
        this.mSegmentView = view;
    }

    public void setImeSubtypeListItem(HSImeSubtypeListItem hsImeSubtypeListItem) {
        this.subtypeListItem=hsImeSubtypeListItem;
        this.mLocale = subtypeListItem.getInputMethodSubtype().getLocale();
    }

    public HSImeSubtypeListItem getImeSubtypeListItem() {
        return subtypeListItem;
    }

    public boolean isDownloading() {
        return downloadThread != null;
    }
}
