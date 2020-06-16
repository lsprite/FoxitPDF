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
package com.nemo.foxitpdf.pdfreader;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nemo.foxitpdf.App;
import com.nemo.foxitpdf.R;

import java.util.ArrayList;

public class MultiTabView {

    public class TabInfo {
        public int tabIndex;
        public String tabTitle;
        public String tabTarget;
//        public boolean isDel;
    }

    public interface ITabEventListener {
        void onTabChanged(TabInfo oldTabInfo, TabInfo newTabInfo);

        void onTabRemoved(TabInfo removedTab, TabInfo showTab);
    }

    private ArrayList<ITabEventListener> mTabEventListeners = new ArrayList<ITabEventListener>();

    public void registerTabEventListener(ITabEventListener listener) {
        if (!mTabEventListeners.contains(listener)) {
            mTabEventListeners.add(listener);
        }
    }

    public void unregisterTabEventListener(ITabEventListener listener) {
        mTabEventListeners.remove(listener);
    }

    private void onTabChanged(TabInfo oldTabInfo, TabInfo newTabInfo) {
        for (ITabEventListener listener : mTabEventListeners) {
            listener.onTabChanged(oldTabInfo, newTabInfo);
        }
    }

    private void onTabRemoved(TabInfo removedTab, TabInfo showTab) {
        for (ITabEventListener listener : mTabEventListeners) {
            listener.onTabRemoved(removedTab, showTab);
        }
    }

    public static final int MAX_NUM_TABS_PHONE = 3;
    public static final int MAX_NUM_TABS_PAD = 5;


    private Context mContext;
    private View mTabFatherView;
    private RelativeLayout mRelativeLayout;
    private LinearLayout mLinearLayout;

    private ArrayList<String> mHistoryFileNames = new ArrayList<String>();


    public MultiTabView() {

    }

    public boolean initialize() {
        mContext = App.instance().getApplicationContext();

        mTabFatherView = View.inflate(mContext, R.layout.multiple_tabview_father, null);
        mRelativeLayout = (RelativeLayout) mTabFatherView.findViewById(R.id._feature_rd_multiple_scroll);
        mLinearLayout = (LinearLayout) mRelativeLayout.findViewById(R.id._feature_rd_multiple_rl);
//            mTabView = View.inflate(mContext, R.layout.multiple_tabview, null);
        return true;
    }


    public View getTabView() {
        return mTabFatherView;
    }


    public boolean resetData() {
        mHistoryFileNames.clear();
        return true;
    }

    public void removeTab(TabInfo tabInfo) {
        mHistoryFileNames.remove(tabInfo.tabTarget);
    }

    public ArrayList<String> getHistoryFileNames() {
        return mHistoryFileNames;
    }

    public void refreshTopBar(final String docPath) {
        ImageView mTabCloseImageView;
        RelativeLayout mTabCloseRelativeLayout, mRelativeLayoutSignalTab, mRelativeLayoutSignalTab1;
        TextView mTabNameTextView, mTextView, mTextView1, mTextView2;

        if (!mHistoryFileNames.contains(docPath)) {
            mHistoryFileNames.add(docPath);
        }

        mLinearLayout.removeAllViews();
        final int count = mHistoryFileNames.size();
        for (int j = 0; j < count; j++) {
            final String name = mHistoryFileNames.get(j);
            View mView = View.inflate(mContext, R.layout.multiple_tabview, null);
            LinearLayout.LayoutParams tabParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1);
            RelativeLayout.LayoutParams rlParams = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
            mView.setLayoutParams(tabParams);
            mTabCloseImageView = (ImageView) mView.findViewById(R.id.multiple_tabview_ig_close);
            mTabCloseRelativeLayout = (RelativeLayout) mView.findViewById(R.id.multiple_rl_ig_close);
            mTabNameTextView = (TextView) mView.findViewById(R.id.multiple_tabview_tv_name);
            mTextView = (TextView) mView.findViewById(R.id.multiple_tabview_tv);
            mTextView1 = (TextView) mView.findViewById(R.id.multiple_tabview_tv1);
            mTextView2 = (TextView) mView.findViewById(R.id.multiple_tabview_tv2);
            mRelativeLayoutSignalTab = (RelativeLayout) mView.findViewById(R.id._feature_rd_multiple_tab_rl);
            mRelativeLayoutSignalTab1 = (RelativeLayout) mView.findViewById(R.id._feature_rd_multiple_tab_rl1);
            String changeColorDocName = new String();
            changeColorDocName = docPath;
            if (name.equals(changeColorDocName) && count > 1) {
                mTabCloseImageView.setImageDrawable(mContext.getResources().getDrawable(R.drawable._feature_rd_multiple_tab_close));
                mRelativeLayoutSignalTab1.setBackgroundColor(Color.parseColor("#C3C3C3"));
                mTabNameTextView.setBackgroundColor(Color.parseColor("#E5E5E5"));
                mTabCloseImageView.setBackgroundColor(Color.parseColor("#E5E5E5"));
                mTabCloseRelativeLayout.setBackgroundColor(Color.parseColor("#E5E5E5"));
                mTextView.setBackgroundColor(Color.parseColor("#E5E5E5"));
                mTextView1.setBackgroundColor(Color.parseColor("#E5E5E5"));
                mTextView2.setBackgroundColor(Color.parseColor("#E5E5E5"));
            }
            if (count == 1) {
//                mTabCloseImageView.setImageDrawable(null);
                mTabCloseImageView.setImageDrawable(mContext.getResources().getDrawable(R.drawable._feature_rd_multiple_tab_close));
                mRelativeLayoutSignalTab1.setBackgroundColor(Color.parseColor("#FAFAFA"));
                mTabNameTextView.setBackgroundColor(Color.parseColor("#FAFAFA"));
                mTabCloseImageView.setBackgroundColor(Color.parseColor("#FAFAFA"));
                mTabCloseRelativeLayout.setBackgroundColor(Color.parseColor("#FAFAFA"));
                mTextView.setBackgroundColor(Color.parseColor("#FAFAFA"));
                mTextView1.setBackgroundColor(Color.parseColor("#FAFAFA"));
                mTextView2.setBackgroundColor(Color.parseColor("#FAFAFA"));
            }

            if (j == 0/*(count - 1)*/) {
                rlParams.setMargins(2, 2, 2, 0);
                mRelativeLayoutSignalTab.setLayoutParams(rlParams);
            }
            mLinearLayout.addView(mView);
            mTabCloseImageView.setEnabled(true);
            mTabCloseRelativeLayout.setEnabled(true);
            mTabCloseRelativeLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mHistoryFileNames.size() == 1) {
//                        mHistoryFileNames.remove(name); // do after close doc

                        TabInfo tabInfo = new TabInfo();
                        tabInfo.tabTarget = name;

                        onTabRemoved(tabInfo, null);
                        return;
                    }

                    if (!name.equals(docPath)) {
//                        mHistoryFileNames.remove(name);// do after close doc

                        TabInfo tabInfo = new TabInfo();
                        tabInfo.tabTarget = name;

                        TabInfo showTab = new TabInfo();
                        showTab.tabTarget = docPath;
                        onTabRemoved(tabInfo, showTab);
//                        refreshTopBar(docPath);
                        return;
                    }

                    // remove the current tab
                    int curTabIndex = 0;
                    for (int i = 0; i < mHistoryFileNames.size(); i++) {
                        String tabName = mHistoryFileNames.get(i);
                        if (tabName.equals(docPath)) {
                            curTabIndex = i;
                            break;
                        }
                    }
                    String path = "";
                    if (curTabIndex == 0 || curTabIndex < mHistoryFileNames.size() - 1) {
                        path = mHistoryFileNames.get(curTabIndex + 1); // get the previous tab
                    } else {
                        path = mHistoryFileNames.get(curTabIndex - 1); // get the previous tab
                    }
//                    mHistoryFileNames.remove(name); // do after close doc
                    TabInfo oldTabInfo = new TabInfo();
                    oldTabInfo.tabTarget = name;

                    TabInfo newTabInfo = new TabInfo();
                    newTabInfo.tabTarget = path;
                    onTabRemoved(oldTabInfo, newTabInfo);
//                    refreshTopBar(path);
                }
            });
            mTabCloseImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mHistoryFileNames.size() == 1) {
//                        mHistoryFileNames.remove(name);// do after close doc

                        TabInfo tabInfo = new TabInfo();
                        tabInfo.tabTarget = name;

                        onTabRemoved(tabInfo, null);
                        return;
                    }

                    if (!name.equals(docPath)) {
//                        mHistoryFileNames.remove(name);// do after close doc
                        TabInfo tabInfo = new TabInfo();
                        tabInfo.tabTarget = name;

                        TabInfo showTab = new TabInfo();
                        showTab.tabTarget = docPath;
                        onTabRemoved(tabInfo, showTab);
//                        refreshTopBar(docPath);
                        return;
                    }

                    // remove the current tab
                    int curTabIndex = 0;
                    for (int i = 0; i < mHistoryFileNames.size(); i++) {
                        String tabName = mHistoryFileNames.get(i);
                        if (tabName.equals(docPath)) {
                            curTabIndex = i;
                            break;
                        }
                    }
                    String path = "";
                    if (curTabIndex == 0 || curTabIndex < mHistoryFileNames.size() - 1) {
                        path = mHistoryFileNames.get(curTabIndex + 1); // get the previous tab
                    } else {
                        path = mHistoryFileNames.get(curTabIndex - 1); // get the previous tab
                    }
//                    mHistoryFileNames.remove(name); // do after close doc

                    TabInfo oldTabInfo = new TabInfo();
                    oldTabInfo.tabTarget = name;

                    TabInfo newTabInfo = new TabInfo();
                    newTabInfo.tabTarget = path;
                    onTabRemoved(oldTabInfo, newTabInfo);

//                    refreshTopBar(path);
                }
            });

            mTabNameTextView.setEnabled(true);
            mTabNameTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (name.equals(docPath)) return;
                    int FileItemSize = mHistoryFileNames.size();
                    for (int h = 0; h < FileItemSize; h++) {
                        if (name.equals(mHistoryFileNames.get(h))) {
                            TabInfo oldTabInfo = new TabInfo();
                            oldTabInfo.tabTarget = docPath;

                            TabInfo newTabInfo = new TabInfo();
                            newTabInfo.tabTarget = name;
                            onTabChanged(oldTabInfo, newTabInfo);
                        }
                    }

                    refreshTopBar(name);
                }
            });
            mTabNameTextView.setText(name.substring(name.lastIndexOf("/") + 1, name.length()));
        }
    }
}