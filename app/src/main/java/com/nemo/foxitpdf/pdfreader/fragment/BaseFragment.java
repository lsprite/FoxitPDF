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

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.foxit.uiextensions.UIExtensionsManager;

public class BaseFragment extends Fragment {
    
    public String name;

    private String path;

    private long fId;

    public boolean isOpenSuccess = false;
    private UIExtensionsManager.OnFinishListener onFinishListener;
    
    public UIExtensionsManager mUiExtensionsManager;
    public String filter;

    @Override
    public void onStart() {
        super.onStart();
        if (mUiExtensionsManager != null) {
            mUiExtensionsManager.onStart(getActivity());
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mUiExtensionsManager != null) {
            mUiExtensionsManager.onStop(getActivity());
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mUiExtensionsManager != null) {
            mUiExtensionsManager.onPause(getActivity());
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (mUiExtensionsManager != null) {
            mUiExtensionsManager.onHiddenChanged(hidden);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
            if (mUiExtensionsManager != null) {
                mUiExtensionsManager.onResume(getActivity());
            }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        onFinishListener = null;
        if (mUiExtensionsManager != null) {
            mUiExtensionsManager.onDestroy(getActivity());
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    public void setFId(long tag) {
        this.fId = tag;
    }

    public long getFId() {
        return fId;
    }

    public void setOnFinishListener(UIExtensionsManager.OnFinishListener onFinishListener) {
        this.onFinishListener = onFinishListener;
    }

    public UIExtensionsManager.OnFinishListener getOnFinishListener() {
        return onFinishListener;
    }

    public interface IFragmentEvent {
        void onRemove();
    }

    public void handleActivityResult(int requestCode, int resultCode, Intent data) {

    }
}