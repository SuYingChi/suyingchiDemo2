package com.ihs.inputmethod.uimodules.ui.settings.activities;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ihs.commons.utils.HSLog;
import com.ihs.inputmethod.language.GoogleDictionaryDownloader;
import com.ihs.inputmethod.api.language.HSDictionaryDownloader;
import com.ihs.inputmethod.api.language.HSImeSubtypeListItem;
import com.ihs.inputmethod.language.api.HSImeSubtypeManager;
import com.ihs.inputmethod.uimodules.R;

import java.util.Locale;


public class SwipeLayout extends LinearLayout {

    private TextView viewTitle;
    private String mLocale;
    private View mSegmentView;
    private View mTick;
    private OnLanguageChangedListener mListener;
    private HSDictionaryDownloader downloader;
    private TextView layoutText;

    private HSImeSubtypeListItem subtypeListItem;

    public interface OnLanguageChangedListener {
        void onLanguageDeletedClick(final SwipeLayout language);
        void onLanguageChanged(final SwipeLayout language);
        void onKeyboardLayoutClick(final SwipeLayout language);
    }

    public SwipeLayout(Context context) {
        this(context, null);
    }

    public SwipeLayout(Context context, AttributeSet attrs) {
        this(context, attrs, -1);
    }

    public SwipeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        this.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if(mListener!=null){
                    mListener.onLanguageChanged(SwipeLayout.this);
                    mTick.setVisibility(View.VISIBLE);
                    invalidate();
                }
            }
        });

        this.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View arg0) {
                if(mListener!=null){
                    mListener.onLanguageDeletedClick(SwipeLayout.this);
                    return true;
                }
                return false;
            }          
        });

        inflate(getContext(), R.layout.swipe_layout, this);

        mTick = findViewById(R.id.tick);
        viewTitle = findViewById(R.id.title);
        layoutText= findViewById(R.id.kbd_layout_name);

    }

// --Commented out by Inspection START (18/1/11 下午2:41):
//    public void download() {
//        if(downloader == null){
//            downloader = new GoogleDictionaryDownloader(this.mLocale);
//        }
//        if (!HSImeSubtypeManager.dictionaryExists(mLocale)) {
//            HSLog.e("dic not exist "+mLocale);
//            downloader.downloadDictionary(HSImeSubtypeManager.getDictDownloadCallback());
//        }
//    }
// --Commented out by Inspection STOP (18/1/11 下午2:41)

    public void setTick(final boolean visible) {
        if (visible) {
            this.mTick.setVisibility(View.VISIBLE);
        } else {
            this.mTick.setVisibility(View.INVISIBLE);
        }
        invalidate();
    }
    
    public void setTitle(final CharSequence title) {
        viewTitle.setText(title);
    }
    
    public String getTitle() {
        return viewTitle.getText().toString();
    }
    
    public String getLocale() {
        return this.mLocale;
    }

    public void setKBDLayout(final String layoutName) {
        final String layout=layoutName.toUpperCase(Locale.ROOT);
        layoutText.setText(layout);
        final String title=viewTitle.getText().toString();
        if(title.contains(layout)){
            viewTitle.setText(title.substring(0,title.indexOf(layout)-1));
        }
    }

    public String getKBDLayout(){
        return layoutText.getText().toString();
    }

    public View getSegmentView() {
        return this.mSegmentView;
    }
    
    public void setSegmentView(final View view) {
        this.mSegmentView = view;
    }

    public void setImeSubtypeListItem(HSImeSubtypeListItem hsImeSubtypeListItem) {
        this.subtypeListItem=hsImeSubtypeListItem;
        this.mLocale=subtypeListItem.getInputMethodSubtype().getLocale();
        if (mLocale.equals("en_US")) {
            this.setOnLongClickListener(null);
        }
        if(HSImeSubtypeManager.hasAdditionalLayoutLocale(mLocale)){
            findViewById(R.id.kbd_layout_bg).setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(mListener!=null){
                        mListener.onKeyboardLayoutClick(SwipeLayout.this);
                    }
                }
            });
        }else{
            findViewById(R.id.kbd_layout_bg).setVisibility(GONE);
        }
    }

    public HSImeSubtypeListItem getImeSubtypeListItem() {
        return subtypeListItem;
    }

    public void setListener(final OnLanguageChangedListener listener) {
        this.mListener = listener;
    }

}
