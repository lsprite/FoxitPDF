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

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;

import com.foxit.pdfscan.IPDFScanManagerListener;
import com.foxit.uiextensions.home.IHomeModule;
import com.foxit.uiextensions.home.local.LocalModule;
import com.foxit.uiextensions.utils.AppFileUtil;
import com.foxit.uiextensions.utils.AppTheme;
import com.nemo.foxitpdf.App;
import com.nemo.foxitpdf.R;
import com.nemo.foxitpdf.pdfreader.fragment.PDFReaderTabsFragment;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

/**
 * @author Administrator
 */
public class MainActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback,
        IHomeModule.onFileItemEventListener, LocalModule.ICompareListener, IPDFScanManagerListener {
    public static final int REQUEST_EXTERNAL_STORAGE = 1;
    public static final int READER_STATE_HOME = 1;
    public static final int READER_STATE_READ = 2;

    private int mReaderState = READER_STATE_HOME;
    private boolean mLicenseValid = false;
    private static final String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    private String filter = App.FILTER_DEFAULT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLicenseValid = App.instance().checkLicense();
        if (!mLicenseValid) {
            return;
        }

        AppTheme.setThemeFullScreen(this);
        AppTheme.setThemeNeedMenuKey(this);
        setContentView(R.layout.activity_reader);

        if (Build.VERSION.SDK_INT >= 23) {
            int permission = ContextCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if (permission != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE);
            }
        }

        Intent intent = getIntent();
        if (intent != null) {
            filter = intent.getAction();
        }
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        HomeFragment homeFragment = getHomeFragment(fm);
        PDFReaderTabsFragment readerFragment = getReaderFragment(fm);

        if (homeFragment == null) {
            homeFragment = HomeFragment.newInstance(filter);
            ft.add(R.id.reader_container, homeFragment, HomeFragment.FRAGMENT_NAME);
        }
        if (readerFragment == null) {
            readerFragment = PDFReaderTabsFragment.newInstance(filter);
            ft.add(R.id.reader_container, readerFragment, PDFReaderTabsFragment.FRAGMENT_NAME);
        }
        if (mReaderState == READER_STATE_HOME) {
            ft.hide(readerFragment);
            ft.show(homeFragment);
        } else {
            ft.hide(homeFragment);
            ft.show(readerFragment);
        }
        ft.commit();
    }

    private HomeFragment getHomeFragment(FragmentManager fm) {
        Fragment fragment = fm.findFragmentByTag(HomeFragment.FRAGMENT_NAME);
        if (fragment != null) {
            return (HomeFragment) fragment;
        }
        return null;
    }

    private PDFReaderTabsFragment getReaderFragment(FragmentManager fm) {
        Fragment fragment = fm.findFragmentByTag(PDFReaderTabsFragment.FRAGMENT_NAME);
        if (fragment != null) {
            return (PDFReaderTabsFragment) fragment;
        }
        return null;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (!mLicenseValid) {
            return;
        }
        setIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (intent != null) {
            String path = AppFileUtil.getFilePath(App.instance().getApplicationContext(), intent, IHomeModule.FILE_EXTRA);
            if (path != null) {
                onFileItemClicked(IHomeModule.FILE_EXTRA, path);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (mLicenseValid && requestCode == REQUEST_EXTERNAL_STORAGE) {
            if (verifyPermissions(grantResults)) {
                Fragment fragment = getSupportFragmentManager().findFragmentByTag(HomeFragment.FRAGMENT_NAME);
                if (fragment instanceof HomeFragment) {
                    fragment.onRequestPermissionsResult(requestCode, permissions, grantResults);
                }
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private boolean verifyPermissions(int[] grantResults) {
        if (grantResults.length < 1) {
            return false;
        }
        for (int grantResult : grantResults) {
            if (grantResult != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void onFileItemClicked(String fileExtra, String filePath) {
        mReaderState = READER_STATE_READ;

        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        HomeFragment homeFragment = getHomeFragment(fm);
        PDFReaderTabsFragment readerFragment = getReaderFragment(fm);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);

        Intent intent = new Intent();
        intent.putExtra(fileExtra, filePath);
        intent.putExtra(HomeFragment.BUNDLE_KEY_FILTER, filter);
        if (homeFragment != null) {
            ft.hide(homeFragment);
        }
        if (readerFragment != null) {
            readerFragment.openDocument(intent);
            ft.show(readerFragment);
        }
        ft.commitAllowingStateLoss();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        FragmentManager fm = getSupportFragmentManager();
        if (mReaderState == READER_STATE_HOME) {
            HomeFragment homeFragment = getHomeFragment(fm);
            if (homeFragment != null && homeFragment.onKeyDown(this, keyCode, event)) {
                return true;
            }
        } else {
            PDFReaderTabsFragment readerFragment = getReaderFragment(fm);
            if (readerFragment != null && readerFragment.onKeyDown(this, keyCode, event)) {
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    public void changeReaderState(int state) {
        mReaderState = state;
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        HomeFragment homeFragment = getHomeFragment(fm);
        PDFReaderTabsFragment readerFragment = getReaderFragment(fm);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE);

        if (mReaderState == READER_STATE_HOME) {
            if (readerFragment != null) {
                ft.hide(readerFragment);
            }
            if (homeFragment != null) {
                ft.show(homeFragment);
            }
        } else {
            if (homeFragment != null) {
                ft.hide(homeFragment);
            }
            if (readerFragment != null) {
                ft.show(readerFragment);
            }
        }
        ft.commitAllowingStateLoss();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        FragmentManager fm = getSupportFragmentManager();
        HomeFragment fragment = getHomeFragment(fm);
        if (fragment != null) {
            fragment.handleActivityResult(requestCode, resultCode, data);
        }
        PDFReaderTabsFragment readerFragment = getReaderFragment(fm);
        if (readerFragment != null) {
            readerFragment.handleActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onCompareClicked(int state, String filePath) {
        if (state == LocalModule.ICompareListener.STATE_SUCCESS) {
            onFileItemClicked(IHomeModule.FILE_EXTRA, filePath);
        }
    }

    @Override
    public void onDocumentAdded(int errorCode, String path) {
        if (errorCode == IPDFScanManagerListener.e_ErrSuccess) {
            onFileItemClicked(IHomeModule.FILE_EXTRA, path);
        }
    }

}
