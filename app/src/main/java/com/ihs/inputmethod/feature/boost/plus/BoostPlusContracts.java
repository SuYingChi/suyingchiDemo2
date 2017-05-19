package com.ihs.inputmethod.feature.boost.plus;

import android.content.Context;
import android.support.annotation.ColorRes;
import android.support.annotation.MenuRes;
import android.view.Menu;

import com.ihs.device.clean.memory.HSAppMemory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Interfaces holder.
 */
interface BoostPlusContracts {

    /**
     * Home page of Boost+. Responsible for providing clean apps list and requests callback when clean animation
     * dialog is dismissed.
     */
    interface HomePage {
        ArrayList<HSAppMemory> getAppsToClean();

        void onCleanFinished();

        void dismissBoostPlusCleanDialog();

        void onReturnFromCleanCancelled(List<HSAppMemory> cleanRemainingApps);
    }

    /**
     * View interface for MVP architecture.
     */
    interface View {
        Context getContext();

        void inflateOptionsMenu(@MenuRes int menuRes, Menu menu);

        void invalidateOptionsMenu();

        void setBannerColor(@ColorRes int resId, boolean animated);

        void showAuthorizeDialog(long sizeToClean);

        void showCleanAnimationDialog(int type);
    }

    /**
     * A presenter interface for Boost+ main activity that could get some code off the activity.
     */
    interface Presenter {
        boolean createOptionsMenu(Menu menu);

        void refreshBannerColor(final long totalSizeBytes, boolean animated);

        void startSettings();

        void startBoost(long size);

        void commitUserChoices(Collection<HSAppMemory> selectedApps, Collection<HSAppMemory> selectSuggestions);

        BoostPlusPresenter.UserChoices loadUserChoices();
    }
}
