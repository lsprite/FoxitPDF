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

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import com.nemo.foxitpdf.R;

import androidx.annotation.Nullable;

public class EmptyViewFragment extends BaseFragment {

    private final static String TAG = EmptyViewFragment.class.getSimpleName();

    private View view;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_empty, container, false);
        return view;
    }

    @Override
    public String getName() {
        return "Error page.";
    }
}
