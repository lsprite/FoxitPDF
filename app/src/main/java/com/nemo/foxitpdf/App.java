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
package com.nemo.foxitpdf;

import android.content.Context;
import android.os.Environment;

import com.foxit.sdk.common.Constants;
import com.foxit.sdk.common.Library;
import com.foxit.uiextensions.controls.toolbar.IBaseItem;
import com.foxit.uiextensions.home.local.LocalModule;
import com.foxit.uiextensions.utils.UIToast;
import com.nemo.foxitpdf.pdfreader.MultiTabView;
import com.nemo.foxitpdf.pdfreader.fragment.AppTabsManager;
import com.nemo.foxitpdf.util.FoxitPDFUtil;

import java.io.File;
import java.util.HashMap;

public class App {
    private static String sn = FoxitPDFUtil.sn;
    private static String key = FoxitPDFUtil.key;

    private Context mContext;
    private int errCode = Constants.e_ErrSuccess;
    private static App INSTANCE = new App();

    public static App instance() {
        return INSTANCE;
    }

    private App() {
        errCode = Library.initialize(sn, key);
    }

    public boolean checkLicense() {
        switch (errCode) {
            case Constants.e_ErrSuccess:
                break;
            case Constants.e_ErrInvalidLicense:
                UIToast.getInstance(mContext).show(mContext.getString(R.string.fx_the_license_is_invalid));
                return false;
            default:
                UIToast.getInstance(mContext).show(mContext.getString(R.string.fx_failed_to_initialize_the_library));
                return false;
        }
        return true;
    }

    public void setApplicationContext(Context context) {
        mContext = context;
    }

    public Context getApplicationContext() {
        return mContext;
    }

    HashMap<String, LocalModule> mLocalModules = new HashMap<String, LocalModule>();

    public LocalModule getLocalModule(String filter) {
        if (mLocalModules.get(filter) == null) {
            LocalModule module = new LocalModule(mContext);
            module.loadModule();
            mLocalModules.put(filter, module);
        }
        return mLocalModules.get(filter);
    }

    public void onDestroy() {
        for (LocalModule module : mLocalModules.values()) {
            module.unloadModule();
        }

        mLocalModules.clear();
    }

    public void unloadLocalModule(String filter) {
        LocalModule module = mLocalModules.get(filter);
        if (module != null) {
            module.unloadModule();

            mLocalModules.remove(filter);
        }
    }

    HashMap<String, MultiTabView> mMultiTabViews = new HashMap<String, MultiTabView>();

    public MultiTabView getMultiTabView(String filter) {
        if (mMultiTabViews.get(filter) == null) {
            MultiTabView view = new MultiTabView();
            view.initialize();
            mMultiTabViews.put(filter, view);
        }
        return mMultiTabViews.get(filter);
    }

    boolean mIsMultiTab = false;

    public boolean isMultiTab() {
        return mIsMultiTab;
    }

    public void setMultiTabFlag(boolean isMultiTab) {
        mIsMultiTab = isMultiTab;
    }

    HashMap<String, AppTabsManager> mTabsManagers = new HashMap<String, AppTabsManager>();

    public AppTabsManager getTabsManager(String filter) {
        if (mTabsManagers.get(filter) == null) {
            AppTabsManager manager = new AppTabsManager();
            mTabsManagers.put(filter, manager);
        }
        return mTabsManagers.get(filter);
    }

    public void copyGuideFiles(LocalModule localModule) {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            String curPath = Environment.getExternalStorageDirectory().getPath() + File.separator + "FoxitSDK";
            File file = new File(curPath);
            if (!file.exists())
                file.mkdirs();
            File sampleFile = new File(curPath + File.separator + "Sample.pdf");
            if (!sampleFile.exists()) {
                localModule.copyFileFromAssertsToTargetFile(sampleFile);
            }

            File guideFile = new File(curPath + File.separator + "complete_pdf_viewer_guide_android.pdf");
            if (!guideFile.exists()) {
                localModule.copyFileFromAssertsToTargetFile(guideFile);
            }
        }
    }

    HashMap<String, IBaseItem> mTabsButtons = new HashMap<String, IBaseItem>();

    public void setTabsButton(String filter, IBaseItem button) {
        mTabsButtons.put(filter, button);
    }

    public IBaseItem getTabsButton(String filter) {
        return mTabsButtons.get(filter);
    }

    public void onBack() {
        mTabsButtons.clear();
        mTabsManagers.clear();
        mMultiTabViews.clear();
    }

    public static final String FILTER_DEFAULT = "default_filter";
}
