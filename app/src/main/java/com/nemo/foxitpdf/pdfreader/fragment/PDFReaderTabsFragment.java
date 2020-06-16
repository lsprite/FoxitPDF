/**
 * Copyright (C) 2003-2020, Foxit Software Inc..
 * All Rights Reserved.
 * <p>
 * http://www.foxitsoftware.com
 * <p>
 * The following code is copyrighted and is the proprietary of Foxit Software Inc.. It is not allowed to
 * distribute any parts of Foxit PDF SDK to third party or public without permission unless an agreement
 * is signed between Foxit Software Inc. and customers to explicitly grant customers permissions.
 * Review legal.txt for additional license and legal information.
 */
package com.nemo.foxitpdf.pdfreader.fragment;


import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;

import com.foxit.uiextensions.ToolHandler;
import com.foxit.uiextensions.UIExtensionsManager;
import com.foxit.uiextensions.home.IHomeModule;
import com.foxit.uiextensions.modules.signature.SignatureToolHandler;
import com.foxit.uiextensions.utils.AppFileUtil;
import com.nemo.foxitpdf.App;
import com.nemo.foxitpdf.R;
import com.nemo.foxitpdf.activity.HomeFragment;
import com.nemo.foxitpdf.activity.MainActivity;
import com.nemo.foxitpdf.pdfreader.MultiTabView;
import com.nemo.foxitpdf.util.LogUtil;

public class PDFReaderTabsFragment extends Fragment implements UIExtensionsManager.OnFinishListener {
    public static final String FRAGMENT_NAME = "READER_FRAGMENT";
    public static final String SINGLE_DOC_TAG = "SINGLE_DOC_TAG";

    private FragmentManager mFragmentManager;

    private String filter = App.FILTER_DEFAULT;
    private String filePath;

    public static PDFReaderTabsFragment newInstance(String filter) {
        PDFReaderTabsFragment fragment = new PDFReaderTabsFragment();
        Bundle args = new Bundle();
        args.putString(HomeFragment.BUNDLE_KEY_FILTER, filter);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_reader, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (getArguments() != null) {
            filter = getArguments().getString(HomeFragment.BUNDLE_KEY_FILTER);
        }
        mFragmentManager = getActivity().getSupportFragmentManager();
        App.instance().getTabsManager(filter).setFragmentManager(mFragmentManager);
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        handleIntent(getActivity().getIntent());
    }

    private void handleIntent(Intent intent) {
        if (intent != null) {
            String path = AppFileUtil.getFilePath(App.instance().getApplicationContext(), intent, IHomeModule.FILE_EXTRA);
            if (path != null) {
                changeReaderState(MainActivity.READER_STATE_READ);
                openDocument(intent);
            }
        }
    }

    public boolean onKeyDown(Activity activity, int keyCode, KeyEvent event) {
        BaseFragment currentFrag = App.instance().getTabsManager(filter).getCurrentFragment();

        if (App.instance().isMultiTab()) {
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                if (currentFrag.mUiExtensionsManager != null
                        && currentFrag.mUiExtensionsManager.backToNormalState()) {
                    return true;
                } else {
                    hideFragment(currentFrag);
                    changeReaderState(MainActivity.READER_STATE_HOME);
                    return true;
                }
            } else {
                return false;
            }
        }

        return currentFrag != null && currentFrag.mUiExtensionsManager != null && currentFrag.mUiExtensionsManager.onKeyDown(getActivity(), keyCode, event);
    }


    @Override
    public void onFinish() {
        BaseFragment currentFrag = App.instance().getTabsManager(filter).getCurrentFragment();
        if (App.instance().isMultiTab()) {
            if (currentFrag != null && !currentFrag.isOpenSuccess) {
                hideFragment(currentFrag);
                changeReaderState(MainActivity.READER_STATE_HOME);
            }
        } else {
            hideFragment(currentFrag);
            App.instance().getTabsManager(filter).setFilePath(null);
            changeReaderState(MainActivity.READER_STATE_HOME);
        }
    }

    private void changeReaderState(int state) {
        ((MainActivity) getActivity()).changeReaderState(state);
    }

    private void hideFragment(Fragment fragment) {
        if (fragment.isVisible()) {
            FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
            fragmentTransaction.hide(fragment).commitAllowingStateLoss();
        }
    }

    private void removeFragment(BaseFragment fragment) {
        FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
        fragmentTransaction.remove(fragment).commitAllowingStateLoss();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (App.instance().isMultiTab()) {
            App.instance().getMultiTabView(filter).unregisterTabEventListener(mTabEventListener);
        }

        mTabEventListener = null;
    }

    public void openDocument(Intent intent) {
        if (!App.instance().checkLicense()) {
            openEmptyView();
            return;
        }
        String oldPath = App.instance().getTabsManager(filter).getFilePath();
        if (oldPath != null) {
            PDFReaderFragment oldFragment = (PDFReaderFragment) App.instance().getTabsManager(filter).getFragmentMap().get(oldPath);
            if (oldFragment != null && oldFragment.isOpenSuccess) {
                oldFragment.mUiExtensionsManager.stopHideToolbarsTimer();
            }
        }

        filePath = AppFileUtil.getFilePath(App.instance().getApplicationContext(), intent, IHomeModule.FILE_EXTRA);
        App.instance().getTabsManager(filter).setFilePath(filePath);
        PDFReaderFragment fragment = (PDFReaderFragment) App.instance().getTabsManager(filter).getFragmentMap().get(filePath);
        boolean needReset = oldPath != null && !filePath.equals(oldPath) && fragment != null && fragment.isOpenSuccess;

        if (App.instance().isMultiTab()) {
            openMultiDocument(false);
        } else {
            openSingleDocument();
        }
        if (needReset) {
            resetTabView(true);
        }
    }

    private void openSingleDocument() {
        filePath = App.instance().getTabsManager(filter).getFilePath();
        FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
        PDFReaderFragment fragment = (PDFReaderFragment) mFragmentManager.findFragmentByTag(SINGLE_DOC_TAG);
        if (fragment == null) {
            fragment = new PDFReaderFragment();
            fragment.setPath(filePath);
            fragment.setOnFinishListener(this);
            fragment.filter = filter;
        } else {
            fragment.setPath(filePath);
            fragment.setOnFinishListener(this);
            fragment.filter = filter;
            fragment.openDocument();
        }

        if (!fragment.isAdded()) {
            fragmentTransaction.add(R.id.reader_container, fragment, SINGLE_DOC_TAG);
        }

        fragmentTransaction.show(fragment).commitAllowingStateLoss();
        App.instance().getTabsManager(filter).addFragment(filePath, fragment);
        App.instance().getTabsManager(filter).setCurrentFragment(fragment);
    }

    private void openMultiDocument(boolean isRemoveCurFragment) {
        App.instance().getMultiTabView(filter).registerTabEventListener(mTabEventListener);

        filePath = App.instance().getTabsManager(filter).getFilePath();
        FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();

        PDFReaderFragment fragment = (PDFReaderFragment) App.instance().getTabsManager(filter).getFragmentMap().get(filePath);
        if (fragment == null) {
            fragment = new PDFReaderFragment();
            App.instance().getTabsManager(filter).addFragment(filePath, fragment);
            fragment.setPath(filePath);
            fragment.setOnFinishListener(this);
            fragment.filter = filter;
        } else {
            if (!fragment.isOpenSuccess) {
                App.instance().getTabsManager(filter).removeFragment(filePath);
                fragmentTransaction.remove(fragment);

                fragment = new PDFReaderFragment();
                App.instance().getTabsManager(filter).addFragment(filePath, fragment);
                fragment.setPath(filePath);
                fragment.setOnFinishListener(this);
                fragment.filter = filter;
            }
        }

        if (!fragment.isAdded()) {
            fragmentTransaction.add(R.id.reader_container, fragment);
        }

        Fragment currentFragment = App.instance().getTabsManager(filter).getCurrentFragment();
        if (currentFragment != null && !currentFragment.equals(fragment)) {
            if (isRemoveCurFragment) {
                fragmentTransaction.remove(currentFragment);
            } else {
                fragmentTransaction.hide(currentFragment);
            }
        }

        fragmentTransaction.show(fragment).commitAllowingStateLoss();
        App.instance().getTabsManager(filter).setCurrentFragment(fragment);
    }

    /**
     * when App license is valid, it should open a empty view use Fragment also.
     */
    private void openEmptyView() {
        FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.reader_container, new EmptyViewFragment());
        fragmentTransaction.commitAllowingStateLoss();
    }

    private MultiTabView.ITabEventListener mTabEventListener = new MultiTabView.ITabEventListener() {
        @Override
        public void onTabChanged(MultiTabView.TabInfo oldTabInfo, MultiTabView.TabInfo newTabInfo) {
            PDFReaderFragment fragment = (PDFReaderFragment) App.instance().getTabsManager(filter).getFragmentMap().get(oldTabInfo.tabTarget);
            fragment.mUiExtensionsManager.stopHideToolbarsTimer();
            changeViewerState(fragment);
            filePath = newTabInfo.tabTarget;
            App.instance().getTabsManager(filter).setFilePath(filePath);

            PDFReaderFragment newfragment = (PDFReaderFragment) App.instance().getTabsManager(filter).getFragmentMap().get(filePath);
            newfragment.mUiExtensionsManager.getDocumentManager().resetActionCallback();

            openMultiDocument(false);
            resetTabView(false);

            newfragment.mUiExtensionsManager.getMainFrame().showToolbars();
            newfragment.mUiExtensionsManager.resetHideToolbarsTimer();
        }

        @Override
        public void onTabRemoved(final MultiTabView.TabInfo removedTab, final MultiTabView.TabInfo showTab) {
            final PDFReaderFragment fragment = (PDFReaderFragment) App.instance().getTabsManager(filter).getFragmentMap().get(removedTab.tabTarget);
            if (removedTab.tabTarget.equals(App.instance().getTabsManager(filter).getFilePath())) {
                changeViewerState(fragment);

                fragment.doClose(new BaseFragment.IFragmentEvent() {
                    @Override
                    public void onRemove() {
                        App.instance().getMultiTabView(filter).removeTab(removedTab);
                        if (showTab != null) {
                            filePath = showTab.tabTarget;
                            App.instance().getTabsManager(filter).setFilePath(filePath);

                            PDFReaderFragment newfragment = (PDFReaderFragment) App.instance().getTabsManager(filter).getFragmentMap().get(filePath);
                            newfragment.mUiExtensionsManager.getDocumentManager().resetActionCallback();

                            openMultiDocument(true);
                            resetTabView(false);
                            App.instance().getTabsManager(filter).getFragmentMap().remove(removedTab.tabTarget);
                            App.instance().getMultiTabView(filter).refreshTopBar(showTab.tabTarget);
                        } else {
                            App.instance().getTabsManager(filter).setFilePath(null);

                            // only one tab
                            removeFragment(fragment);
                            App.instance().getTabsManager(filter).setCurrentFragment(null);
                            App.instance().getTabsManager(filter).clearFragment();

                            changeReaderState(MainActivity.READER_STATE_HOME);
                        }
                    }
                });
            } else {
                fragment.doClose(new BaseFragment.IFragmentEvent() {
                    @Override
                    public void onRemove() {
                        App.instance().getMultiTabView(filter).removeTab(removedTab);
                        removeFragment(fragment);
                        App.instance().getTabsManager(filter).getFragmentMap().remove(removedTab.tabTarget);
                        App.instance().getMultiTabView(filter).refreshTopBar(showTab.tabTarget);
                    }
                });
            }
        }
    };

    private void resetTabView(boolean needRefresh) {
        if (needRefresh) {
            App.instance().getMultiTabView(filter).refreshTopBar(App.instance().getTabsManager(filter).getFilePath());
        }
        PDFReaderFragment fragment = (PDFReaderFragment) App.instance().getTabsManager(filter).getFragmentMap().get(App.instance().getTabsManager(filter).getFilePath());
        int h = fragment.mUiExtensionsManager.getMainFrame().getTopToolbar().getContentView().getHeight();
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 2 * h / 3);
        params.topMargin = -10;
        ViewGroup parent = (ViewGroup) App.instance().getMultiTabView(filter).getTabView().getParent();
        if (parent != null) {
            parent.removeView(App.instance().getMultiTabView(filter).getTabView());
        }
        fragment.mUiExtensionsManager.getMainFrame().addSubViewToTopBar(App.instance().getMultiTabView(filter).getTabView(), 1, params);
    }

    private void changeViewerState(PDFReaderFragment fragment) {
        LogUtil.e("----changeViewerState");
        UIExtensionsManager uiExtensionsManager = (UIExtensionsManager) fragment.getPdfViewCtrl().getUIExtensionsManager();
        uiExtensionsManager.triggerDismissMenuEvent();
        uiExtensionsManager.getDocumentManager().setCurrentAnnot(null);
        uiExtensionsManager.exitPanZoomMode();
        ToolHandler toolHandler = uiExtensionsManager.getCurrentToolHandler();
        if (toolHandler != null) {
            uiExtensionsManager.setCurrentToolHandler(null);
            if (toolHandler instanceof SignatureToolHandler) {
                fragment.getPdfViewCtrl().invalidate();
            }
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Activity activity = getActivity();
        if (activity == null)
            return;
        if (isHidden())
            return;

        BaseFragment currentFrag = App.instance().getTabsManager(filter).getCurrentFragment();
        if (App.instance().isMultiTab()) {
            if (currentFrag != null && currentFrag.isOpenSuccess) {
                App.instance().getMultiTabView(filter).refreshTopBar(currentFrag.getPath());
            }
        }

        if (currentFrag == null) return;
        if (currentFrag.mUiExtensionsManager != null) {
            currentFrag.mUiExtensionsManager.onConfigurationChanged(getActivity(), newConfig);
        }
    }

    public void handleActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        BaseFragment currentFrag = App.instance().getTabsManager(filter).getCurrentFragment();
        if (currentFrag == null) return;
        currentFrag.handleActivityResult(requestCode, resultCode, data);
    }

}
