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

import androidx.fragment.app.FragmentManager;

import java.util.HashMap;

public class AppTabsManager {
    private FragmentManager mFragmentManager;

    private String filePath;

    private HashMap<String, BaseFragment> mFragmentMap = new HashMap<String, BaseFragment>();

    private BaseFragment mCurFragment;

    public AppTabsManager() {
    }

    public FragmentManager getFragmentManager() {
        return mFragmentManager;
    }

    public void setFragmentManager(FragmentManager mFragmentManager) {
        this.mFragmentManager = mFragmentManager;
    }


    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public HashMap<String, BaseFragment> getFragmentMap() {
        return mFragmentMap;
    }

    public void addFragment(String key, BaseFragment value) {
        mFragmentMap.put(key, value);
    }

    public void removeFragment(String key) {
        mFragmentMap.remove(key);
    }

    public void clearFragment() {
        mFragmentMap.clear();
    }

    public BaseFragment getCurrentFragment() {
        return mCurFragment;
    }

    public void setCurrentFragment(BaseFragment fragment) {
        mCurFragment = fragment;
    }
}
