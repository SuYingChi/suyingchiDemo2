package com.ihs.inputmethod.uimodules.ui.settings.activities;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodSubtype;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ihs.app.framework.activity.HSAppCompatActivity;
import com.ihs.inputmethod.api.dialogs.HSAlertDialog;
import com.ihs.inputmethod.language.api.HSImeSubtypeManager;
import com.ihs.inputmethod.settings.AdditionalSubtypeUtil;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.ui.settings.activities.SwipeLayout.OnLanguageChangedListener;
import com.kc.commons.utils.KCCommonUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public final class MoreLanguageActivity2 extends HSAppCompatActivity implements LanguageLoadingPreference.OnLanguageDownloadedListener, OnLanguageChangedListener {
    private LinearLayout mAvailableLanguagesListLayout;
    private LinearLayout mCurrentLanguagesListLayout;
    private SwipeLayout mCurrentLanguage;
    private List<SwipeLayout> mCurrentLanguageCache = new ArrayList<>();
    private List<LanguageLoadingPreference> mAvailableLanguageCache = new ArrayList<>();
    private Dialog dialog;
    private int dialogSelectPosition = -1;

    @Override
    protected void onCreate(final Bundle savedState) {
        super.onCreate(savedState);

        HSImeSubtypeManager.initSubtypeLocale();

        setContentView(R.layout.more_language_layout2);

        mAvailableLanguagesListLayout = findViewById(R.id.ll_available_languages);
        mCurrentLanguagesListLayout = findViewById(R.id.ll_current_languages_list);

        init();

        if(getSupportActionBar()!=null){
            getSupportActionBar().setTitle(getString(R.string.language));
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void addToCurrentLanguage(final LanguageLoadingPreference preference) {

        final String locale = preference.getLocale();
        final InputMethodSubtype subtype = HSImeSubtypeManager.getImeSubtypeByLocale(locale);
        if(subtype==null){
            return;
        }
        HSImeSubtypeManager.setSubtypeEnableStateByLocale(locale, true);

        SwipeLayout item = new SwipeLayout(this);
        item.setTitle(preference.getTitle());
        item.setListener(this);
        item.setImeSubtypeListItem(preference.getImeSubtypeListItem());
        final String kbdLayout=HSImeSubtypeManager.getCurrentKeyboardLayout(locale);
        item.setKBDLayout(kbdLayout);

        int position = findPositionToInsertInCurrentList(item);
        mCurrentLanguagesListLayout.addView(item, Math.min(mCurrentLanguagesListLayout.getChildCount(),position*2));

        TextView tv = new TextView(this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 2);
        tv.setLayoutParams(lp);
        tv.setBackgroundColor(this.getResources().getColor(R.color.settings_divder_line_color));
        mCurrentLanguagesListLayout.addView(tv, Math.min(mCurrentLanguagesListLayout.getChildCount(),position * 2 + 1));
        item.setSegmentView(tv);

        mCurrentLanguageCache.add(position,item);
    }

    private int findPositionToInsertInCurrentList(SwipeLayout item){
        if (mCurrentLanguageCache.size()==0){
            return 0;
        }
        int index =mCurrentLanguageCache.size();
        for (int i=1;i<mCurrentLanguageCache.size();i++){
            if (item.getImeSubtypeListItem().compareTo(mCurrentLanguageCache.get(i).getImeSubtypeListItem())<=0){
                index = i;
                break;
            }
        }
        return index;
    }

    private int findPositionToInsertInAvailableList(LanguageLoadingPreference item){
        if (mAvailableLanguageCache.size()==0){
            return 0;
        }
        int index =mAvailableLanguageCache.size();
        for (int i=0;i<mAvailableLanguageCache.size();i++){
            if (item.getImeSubtypeListItem().compareTo(mAvailableLanguageCache.get(i).getImeSubtypeListItem())<=0){
                index = i;
                break;
            }
        }
        return index;
    }

    private void removeFromAvailableLanguage(final LanguageLoadingPreference preference) {
        mAvailableLanguagesListLayout.removeView(preference);
        mAvailableLanguagesListLayout.removeView(preference.getSegmentView());
        mAvailableLanguageCache.remove(preference);
        // If delete the last item, should delete previous segment line
        if (mAvailableLanguagesListLayout.getChildCount() > 0) {
            View lastView = mAvailableLanguagesListLayout.getChildAt(mAvailableLanguagesListLayout.getChildCount() - 1);
            if (!(lastView instanceof LanguageLoadingPreference)) {
                mAvailableLanguagesListLayout.removeView(lastView);
            }
        }

    }

    private void setPreviousAsCurrentLanguage(SwipeLayout language) {
        int i;
        for (i = 0; i < this.mCurrentLanguageCache.size(); ++i) {
            if (mCurrentLanguageCache.get(i) == language) {
                break;
            }
        }

        if (i - 1 >= 0) {
            this.onLanguageChanged(mCurrentLanguageCache.get(i - 1));
        }
    }

    private void addToAvailableLanguage(final SwipeLayout language) {
        LanguageLoadingPreference preference = new LanguageLoadingPreference(this);
        HSImeSubtypeManager.setSubtypeEnableStateByLocale(language.getLocale(), false);
        preference.setTitle(language.getTitle());
        preference.setListener(this);
        preference.setImeSubtypeListItem(language.getImeSubtypeListItem());
        int position=findPositionToInsertInAvailableList(preference);
        mAvailableLanguageCache.add(position,preference);
        mAvailableLanguagesListLayout.addView(preference,  Math.min(mAvailableLanguagesListLayout.getChildCount(),position * 2 ));

        TextView tv = new TextView(this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 2);
        tv.setLayoutParams(lp);
        tv.setBackgroundColor(this.getResources().getColor(R.color.settings_divder_line_color));
        mAvailableLanguagesListLayout.addView(tv,  Math.min(mAvailableLanguagesListLayout.getChildCount(),position * 2 +1));

    }

    private void removeFromCurrentLanguage(final SwipeLayout language) {
        mCurrentLanguagesListLayout.removeView(language.getSegmentView());
        mCurrentLanguagesListLayout.removeView(language);
        mCurrentLanguageCache.remove(language);
        HSImeSubtypeManager.setSubtypeEnableStateByLocale(language.getLocale(), false);
    }

    private void releaseDialog() {
        if (dialog != null) {
            KCCommonUtils.dismissDialog(dialog);
            dialog = null;
        }
    }

    private void init() {
//        initCurrentLanguages();
//        initAvailableLanguages();
    }

//    private void initCurrentLanguages() {
//        mCurrentLanguagesListLayout.removeAllViews();
//        final InputMethodInfo imi = HSInputMethod.getInputMethodInfoOfThisIme();
//        CharSequence displayName;
//        InputMethodSubtype subtype;
//        TextView tv;
//        SwipeLayout languageItem;
//
//        final List<HSImeSubtypeListItem> enabledSubs = HSImeSubtypeManager.getSortedInputMethodSubtypeList(true);
//        final int count =enabledSubs.size();
//        for (int i = 0; i < count; ++i) {
//            String locale = enabledSubs.get(i).getInputMethodSubtype().getLocale();
//            subtype = HSImeSubtypeManager.getImeSubtypeByLocale(locale);
//            if(subtype==null){
//                continue;
//            }
//            displayName = subtype.getDisplayName(this, imi.getPackageName(), imi.getServiceInfo().applicationInfo);
//            languageItem = new SwipeLayout(this);
//            languageItem.setTitle(displayName);
//            languageItem.setListener(this);
//            languageItem.setImeSubtypeListItem(enabledSubs.get(i));
//            final String kbdLayout=HSImeSubtypeManager.getCurrentKeyboardLayout(locale);
//            languageItem.setKBDLayout(kbdLayout);
//
//            // set current
//            if (HSImeSubtypeManager.isCurrentSubtype(subtype)) {
//                this.mCurrentLanguage = languageItem;
//                this.mCurrentLanguage.setTick(true);
//            }
//
//            mCurrentLanguagesListLayout.addView(languageItem);
//            mCurrentLanguageCache.add(languageItem);
//
//            tv = new TextView(this);
//            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 2);
//            tv.setLayoutParams(lp);
//            tv.setBackgroundColor(this.getResources().getColor(R.color.settings_divder_line_color));
//            mCurrentLanguagesListLayout.addView(tv);
//            languageItem.setSegmentView(tv);
//        }
//    }
//
//    private void initAvailableLanguages() {
//        mAvailableLanguagesListLayout.removeAllViews();
//
//        final InputMethodInfo imi = HSInputMethod.getInputMethodInfoOfThisIme();
//
//        // add english us language
//        String displayName;
//
//        String locale;
//        InputMethodSubtype subtype;
//        TextView tv;
//        LanguageLoadingPreference preference;
//        final List<HSImeSubtypeListItem> unEnabledSubs = HSImeSubtypeManager.getSortedInputMethodSubtypeList(false);
//        final int count = unEnabledSubs.size();
//        final boolean isApiBelow19= Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT;
//        for (int i = 0; i < count; ++i) {
//            locale =unEnabledSubs.get(i).getInputMethodSubtype().getLocale();
//            if(isApiBelow19&&HSImeSubtypeManager.removedBelowApi19(locale)){
//                continue;
//            }
//            subtype = HSImeSubtypeManager.getImeSubtypeByLocale(locale);
//            if(subtype==null){
//                continue;
//            }
//            displayName = subtype.getDisplayName(this, imi.getPackageName(), imi.getServiceInfo().applicationInfo).toString();
//
//            preference = new LanguageLoadingPreference(this);
//            preference.setTitle(displayName);
//            preference.setId(i);
//            preference.setListener(this);
//            preference.setImeSubtypeListItem(unEnabledSubs.get(i));
//            mAvailableLanguagesListLayout.addView(preference);
//            mAvailableLanguageCache.add(preference);
//            tv = new TextView(this);
//            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 2);
//            tv.setLayoutParams(lp);
//            tv.setBackgroundColor(this.getResources().getColor(R.color.settings_divder_line_color));
//            mAvailableLanguagesListLayout.addView(tv);
//
//            preference.setSegmentView(tv);
//        }
//    }

    @Override
    public void onLanguageDownloaded(LanguageLoadingPreference preference) {
        enableLanguageSyn(preference);
    }

    private synchronized void enableLanguageSyn(LanguageLoadingPreference preference) {
        removeFromAvailableLanguage(preference);
        addToCurrentLanguage(preference);
    }

    private void onLanguageDeleted(SwipeLayout language) {
        if (this.mCurrentLanguage == language) {
            setPreviousAsCurrentLanguage(language);
        }
        removeFromCurrentLanguage(language);
        addToAvailableLanguage(language);
    }

    @Override
    public void onLanguageDeletedClick(final SwipeLayout language) {
        dialog = new Dialog(this, R.style.CommonAlert);

        dialog.setContentView(R.layout.alert_common);

        View view = dialog.findViewById(R.id.layout);

        TextView title = view.findViewById(R.id.title);
        title.setTextColor(getResources().getColor(R.color.alert_message));
        title.setText(language.getTitle());

        TextView cancel = view.findViewById(R.id.negative_button);
        cancel.setText(getString(R.string.cancel).toUpperCase());
        cancel.setTextColor(getResources().getColor(R.color.alert_positive_button_text));
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                releaseDialog();
            }
        });

        TextView delete = view.findViewById(R.id.positive_button);
        delete.setText(getString(R.string.delete).toUpperCase());
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                releaseDialog();
                onLanguageDeleted(language);
            }
        });

        KCCommonUtils.showDialog(dialog);
    }

    @Override
    public void onLanguageChanged(final SwipeLayout language) {
        this.mCurrentLanguage.setTick(false);
        this.mCurrentLanguage = language;
        this.mCurrentLanguage.setTick(true);
        HSImeSubtypeManager.setCurrentSubtypeByLocale(language.getLocale());
    }

    @Override
    public void onKeyboardLayoutClick(final SwipeLayout language) {
        final List<String> data=HSImeSubtypeManager.getAdditionalLayout(language.getLocale());
        final String defaultKeyboardLayout=HSImeSubtypeManager.getDefaultKeyboardLayout(language.getLocale());
        if(!data.contains(defaultKeyboardLayout)){
            data.add(0,defaultKeyboardLayout);
        }
        showDialog(data,data.indexOf(language.getKBDLayout().toLowerCase(Locale.ROOT)),language);
    }

    private void showDialog(final List<String> items,final int checkedItem,final SwipeLayout language) {
	    final List<String> copy=new ArrayList<>();
        for(int i = 0 ;i<items.size();i++){
	        copy.add(items.get(i).toUpperCase(Locale.ROOT));
        }
        dialogSelectPosition = -1;

        AlertDialog alertDialog = HSAlertDialog.build(this).setTitle(getResources().getString(R.string.key_layout_dialog_title))
                .setSingleChoiceItems(copy.toArray(new CharSequence[copy.size()]), checkedItem, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialogSelectPosition = which;
                    }
                })
                .setPositiveButton(getString(android.R.string.ok),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // positive button logic
                                if (dialogSelectPosition != -1 && dialogSelectPosition != checkedItem) {
                                    onLayoutClick(copy.get(dialogSelectPosition).toLowerCase(Locale.ROOT), language);
                                }
                                KCCommonUtils.dismissDialog((Dialog) dialog);
                            }
                        })
                .setNegativeButton(getString(android.R.string.cancel),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // negative button logic
                                KCCommonUtils.dismissDialog((Dialog) dialog);
                            }
                        })
                .create();
        KCCommonUtils.showDialog(alertDialog);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        HSImeSubtypeManager.updateEnabledSubtypes();
        releaseDialog();
        super.onPause();
    }

    private void onLayoutClick(final String layout,final SwipeLayout language) {
        releaseDialog();
        InputMethodSubtype subtype= AdditionalSubtypeUtil.createAdditionalSubtype(language.getLocale(),layout);
        HSImeSubtypeManager.updateSubtype(language.getLocale(),layout,subtype);
        language.setKBDLayout(layout);
    }
}
