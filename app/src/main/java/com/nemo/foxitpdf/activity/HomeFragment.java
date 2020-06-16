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
package com.nemo.foxitpdf.activity;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.foxit.pdfscan.IPDFScanManagerListener;
import com.foxit.pdfscan.PDFScanManager;
import com.foxit.uiextensions.controls.dialog.AppDialogManager;
import com.foxit.uiextensions.controls.toolbar.BaseBar;
import com.foxit.uiextensions.controls.toolbar.impl.BaseItemImpl;
import com.foxit.uiextensions.home.IHomeModule;
import com.foxit.uiextensions.home.local.LocalModule;
import com.foxit.uiextensions.utils.AppResource;
import com.foxit.uiextensions.utils.AppUtil;
import com.foxit.uiextensions.utils.UIToast;
import com.nemo.foxitpdf.App;
import com.nemo.foxitpdf.R;
import com.nemo.foxitpdf.pdfreader.fragment.BaseFragment;

import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

public class HomeFragment extends Fragment {
    public static final String FRAGMENT_NAME = "HOME_FRAGMENT";
    public static final String BUNDLE_KEY_FILTER = "key_filter";

    private ViewGroup mRootView;
    private IHomeModule.onFileItemEventListener mOnFileItemEventListener;
    private IPDFScanManagerListener mScanListener;

    private String filter = App.FILTER_DEFAULT;

    public static HomeFragment newInstance(String filter) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putString(BUNDLE_KEY_FILTER, filter);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof IHomeModule.onFileItemEventListener)
            mOnFileItemEventListener = (IHomeModule.onFileItemEventListener) context;
        if (context instanceof IPDFScanManagerListener)
            mScanListener = (IPDFScanManagerListener) context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (getArguments() != null) {
            filter = getArguments().getString(BUNDLE_KEY_FILTER);
        }
        App.instance().getLocalModule(filter).setAttachedActivity(getActivity());
        App.instance().copyGuideFiles(App.instance().getLocalModule(filter));
        App.instance().getLocalModule(filter).setFileItemEventListener(new IHomeModule.onFileItemEventListener() {
            @Override
            public void onFileItemClicked(String fileExtra, String filePath) {
                if (mOnFileItemEventListener != null) {
                    mOnFileItemEventListener.onFileItemClicked(fileExtra, filePath);
                }
            }
        });

        App.instance().getLocalModule(filter).setCompareListener(new LocalModule.ICompareListener() {
            @Override
            public void onCompareClicked(int state, String filePath) {
                LocalModule.ICompareListener compareListener = (LocalModule.ICompareListener) getActivity();
                if (compareListener != null) {
                    compareListener.onCompareClicked(state, filePath);
                }
            }
        });

        View view = App.instance().getLocalModule(filter).getContentView(App.instance().getApplicationContext());
        ViewGroup parent = (ViewGroup) view.getParent();
        if (parent != null) {
            parent.removeView(view);
        }
        mRootView = new RelativeLayout(App.instance().getApplicationContext());
        mRootView.addView(view, new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup
                .LayoutParams.MATCH_PARENT));

        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        ImageView ivScan = new ImageView(getContext());
        ivScan.setImageResource(R.drawable.fx_floatbutton_scan);
        ivScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!PDFScanManager.isInitializeScanner()) {
                    long framework1 = 0;
                    long framework2 = 0;
                    PDFScanManager.initializeScanner(getActivity().getApplication(), framework1, framework2);
                }
                if (!PDFScanManager.isInitializeCompression()) {
                    long compression1 = 0;
                    long compression2 = 0;
                    PDFScanManager.initializeCompression(getActivity().getApplication(), compression1, compression2);
                }
                if (PDFScanManager.isInitializeScanner() && PDFScanManager.isInitializeCompression()) {
                    showScannerList();
                } else {
                    UIToast.getInstance(App.instance().getApplicationContext())
                            .show(AppResource.getString(App.instance().getApplicationContext(),R.string.rv_invalid_license));
                }
            }
        });
        PDFScanManager.registerManagerListener(mManagerListener);

        layoutParams.bottomMargin = 80;
        layoutParams.rightMargin = 50;
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        ivScan.setLayoutParams(layoutParams);
        mRootView.addView(ivScan);
        return mRootView;
    }

    private DialogFragment mScannerList;

    private void showScannerList() {
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        mScannerList = (DialogFragment) fragmentManager.findFragmentByTag("ScannerList");
        if (mScannerList == null)
            mScannerList = PDFScanManager.createScannerFragment(null);
        AppDialogManager.getInstance().showAllowManager(mScannerList, fragmentManager, "ScannerList", null);
    }

    private void dismissScannerList() {
        AppDialogManager.getInstance().dismiss(mScannerList);
    }

    private IPDFScanManagerListener mManagerListener = new IPDFScanManagerListener() {
        @Override
        public void onDocumentAdded(int errorCode, String path) {
            if (mScanListener != null) {
                dismissScannerList();
                updateThumbnail(path);
                mScanListener.onDocumentAdded(errorCode, path);
            }
        }
    };

    private void updateThumbnail(String path) {
        if (!AppUtil.isEmpty(path)) {
            App.instance().getLocalModule(filter).updateThumbnail(path);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MainActivity.REQUEST_EXTERNAL_STORAGE) {
            App.instance().copyGuideFiles(App.instance().getLocalModule(filter));
            App.instance().getLocalModule(filter).updateStoragePermissionGranted();
            initTabsButton(App.instance().getLocalModule(filter), getActivity());
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        App.instance().setTabsButton(filter, null);
        initTabsButton(App.instance().getLocalModule(filter), getActivity());
        App.instance().getLocalModule(filter).registerFinishEditListener(mFinishEditListener);
    }

    @Override
    public void onDestroy() {
        App.instance().getLocalModule(filter).unregisterFinishEditListener(mFinishEditListener);
        App.instance().unloadLocalModule(filter);
        PDFScanManager.unregisterManagerListener(mManagerListener);
        super.onDestroy();
    }

    private void initTabsButton(LocalModule localModule, final Activity activity) {
        BaseItemImpl singleMultiBtn = (BaseItemImpl) App.instance().getTabsButton(filter);
        if (singleMultiBtn != null) {
            if (singleMultiBtn.getContentView().getParent() == null) {
                localModule.getTopToolbar().addView(singleMultiBtn, BaseBar.TB_Position.Position_RB);
            }
            return;
        }
        singleMultiBtn = new BaseItemImpl(App.instance().getApplicationContext());
        App.instance().setTabsButton(filter, singleMultiBtn);

        if (App.instance().isMultiTab()) {
            singleMultiBtn.setImageResource(R.drawable.rd_multi_tab_selector);
            singleMultiBtn.setId(R.id.rd_multi_tab);
        } else {
            singleMultiBtn.setImageResource(R.drawable.rd_single_tab_selector);
            singleMultiBtn.setId(R.id.rd_single_tab);
        }

        final Context context = App.instance().getApplicationContext();
        final BaseItemImpl finalSingleMultiBtn = singleMultiBtn;
        singleMultiBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                String readerMode = !App.instance().isMultiTab() ? context.getString(R.string.fx_tabs_reader_mode) : context.getString(R.string.fx_single_reader_mode);
                String msg = context.getString(R.string.fx_swith_reader_mode_toast, readerMode);
                String title = "";
                Dialog dialog = new AlertDialog.Builder(activity).setCancelable(true).setTitle(title)
                        .setMessage(msg)
                        .setPositiveButton(context.getString(R.string.fx_string_yes),
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        if (v.getId() == R.id.rd_single_tab) {
                                            finalSingleMultiBtn.setImageResource(R.drawable.rd_multi_tab_selector);
                                            finalSingleMultiBtn.setId(R.id.rd_multi_tab);
                                            App.instance().setMultiTabFlag(true);
                                        } else if (v.getId() == R.id.rd_multi_tab) {
                                            finalSingleMultiBtn.setImageResource(R.drawable.rd_single_tab_selector);
                                            finalSingleMultiBtn.setId(R.id.rd_single_tab);
                                            App.instance().setMultiTabFlag(false);
                                        }
                                        FragmentManager mFragmentManager = App.instance().getTabsManager(filter).getFragmentManager();
                                        if (mFragmentManager != null) {
                                            FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
                                            for (Map.Entry<String, BaseFragment> entry : App.instance().getTabsManager(filter).getFragmentMap().entrySet()) {
                                                fragmentTransaction.remove(entry.getValue());
                                            }
                                            fragmentTransaction.commitAllowingStateLoss();
                                        }

                                        App.instance().getTabsManager(filter).setCurrentFragment(null);
                                        App.instance().getTabsManager(filter).clearFragment();
                                        App.instance().getMultiTabView(filter).resetData();

                                        dialog.dismiss();
                                    }
                                }).setNegativeButton(context.getString(R.string.fx_string_no),
                                new DialogInterface.OnClickListener() {

                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                }).create();
                dialog.show();
            }
        });
        localModule.getTopToolbar().addView(singleMultiBtn, BaseBar.TB_Position.Position_RB);
    }

    @Override
    public Animation onCreateAnimation(int transit, boolean enter, int nextAnim) {
        TranslateAnimation animation = null;
        if (transit == FragmentTransaction.TRANSIT_FRAGMENT_OPEN) {
            if (enter) {
                animation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 1, Animation.RELATIVE_TO_SELF, 0,
                        Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0);
            } else {
                animation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, -1,
                        Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0);
            }
        } else if (FragmentTransaction.TRANSIT_FRAGMENT_CLOSE == transit) {
            if (enter) {
                animation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, -1, Animation.RELATIVE_TO_SELF, 0,
                        Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0);
            } else {
                animation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 1,
                        Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0);
            }
        }
        if (animation == null) {
            animation = new TranslateAnimation(0, 0, 0, 0);
        }
        animation.setDuration(300);
        return animation;
    }

    public boolean onKeyDown(Activity activity, int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && App.instance().isMultiTab()) {
            Intent launcherIntent = new Intent(Intent.ACTION_MAIN);
            launcherIntent.addCategory(Intent.CATEGORY_HOME);
            startActivity(launcherIntent);
            return true;
        }
        App.instance().onBack();
        return false;
    }

    public void handleActivityResult(int requestCode, int resultCode, Intent data) {
    }

    LocalModule.IFinishEditListener mFinishEditListener = new LocalModule.IFinishEditListener() {
        @Override
        public void onFinishEdit() {
            if (App.instance().getLocalModule(filter) != null) {
                initTabsButton(App.instance().getLocalModule(filter), getActivity());
            }
        }
    };

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        App.instance().getLocalModule(filter).onConfigurationChanged(newConfig);
    }

}
