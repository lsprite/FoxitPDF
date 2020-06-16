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

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.foxit.sdk.PDFViewCtrl;
import com.foxit.sdk.common.Constants;
import com.foxit.sdk.pdf.PDFDoc;
import com.foxit.uiextensions.IPDFReader;
import com.foxit.uiextensions.Module;
import com.foxit.uiextensions.UIExtensionsManager;
import com.foxit.uiextensions.config.Config;
import com.foxit.uiextensions.controls.dialog.AppDialogManager;
import com.foxit.uiextensions.controls.dialog.MatchDialog;
import com.foxit.uiextensions.controls.dialog.UITextEditDialog;
import com.foxit.uiextensions.controls.dialog.fileselect.UIFolderSelectDialog;
import com.foxit.uiextensions.home.IHomeModule;
import com.foxit.uiextensions.home.local.LocalModule;
import com.foxit.uiextensions.modules.dynamicxfa.DynamicXFAModule;
import com.foxit.uiextensions.utils.AppDmUtil;
import com.foxit.uiextensions.utils.AppFileUtil;
import com.foxit.uiextensions.utils.AppResource;
import com.foxit.uiextensions.utils.AppUtil;
import com.foxit.uiextensions.utils.UIToast;
import com.nemo.foxitpdf.App;
import com.nemo.foxitpdf.R;
import com.nemo.foxitpdf.activity.HomeFragment;
import com.nemo.foxitpdf.activity.MainActivity;

import java.io.File;
import java.io.FileFilter;
import java.io.InputStream;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import static com.foxit.sdk.common.Constants.e_ErrSuccess;

public class PDFReaderFragment extends BaseFragment {

    private final static String TAG = PDFReaderFragment.class.getSimpleName();
    private PDFViewCtrl pdfViewerCtrl;
    private AlertDialog mSaveAlertDlg;
    private ProgressDialog mProgressDlg;
    private IFragmentEvent mFragmentEvent = null;
    private UIFolderSelectDialog mFolderSelectDialog;

    private String mSavePath = null;
    private String mProgressMsg = null;
    private String mDocPath;
    private String currentFileCachePath;
    private boolean isSaveDocInCurPath = false;
    private boolean isCloseDocAfterSaving = false;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (mUiExtensionsManager != null) {
            return mUiExtensionsManager.getContentView();
        }
        if (savedInstanceState != null) {
            filter = savedInstanceState.getString(HomeFragment.BUNDLE_KEY_FILTER);
            setPath(savedInstanceState.getString(IHomeModule.FILE_EXTRA));
        }
        InputStream stream = getActivity().getApplicationContext().getResources().openRawResource(R.raw.uiextensions_config);
        Config config = new Config(stream);

        pdfViewerCtrl = new PDFViewCtrl(getActivity().getApplicationContext());
        mUiExtensionsManager = new UIExtensionsManager(getActivity().getApplicationContext(), pdfViewerCtrl, config);

        if (App.instance().isMultiTab()) {
            pdfViewerCtrl.registerDocEventListener(mDocEventListener);
        }

        pdfViewerCtrl.setUIExtensionsManager(mUiExtensionsManager);
        pdfViewerCtrl.setAttachedActivity(getActivity());
        mUiExtensionsManager.setAttachedActivity(getActivity());
        mUiExtensionsManager.registerModule(App.instance().getLocalModule(filter)); // use to refresh file list

        mUiExtensionsManager.onCreate(getActivity(), pdfViewerCtrl, savedInstanceState);
        mUiExtensionsManager.openDocument(getPath(), null);
        mUiExtensionsManager.setOnFinishListener(getOnFinishListener());
        setName("");

        mUiExtensionsManager.setBackEventListener(mBackEventListener);
        return mUiExtensionsManager.getContentView();
    }

    public void openDocument() {
        mUiExtensionsManager.openDocument(getPath(), null);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(IHomeModule.FILE_EXTRA, getPath());
        outState.putString(HomeFragment.BUNDLE_KEY_FILTER, filter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (App.instance().isMultiTab()) {
            mUiExtensionsManager.getMainFrame().removeSubViewFromTopBar(App.instance().getMultiTabView(filter).getTabView());
            if (pdfViewerCtrl != null) {
                pdfViewerCtrl.unregisterDocEventListener(mDocEventListener);
            }

            mUiExtensionsManager.setBackEventListener(null);
        }

        mDocEventListener = null;
        mBackEventListener = null;
    }

    public PDFViewCtrl getPdfViewCtrl() {
        return pdfViewerCtrl;
    }

    PDFViewCtrl.IDocEventListener mDocEventListener = new PDFViewCtrl.IDocEventListener() {
        @Override
        public void onDocWillOpen() {

        }

        @Override
        public void onDocOpened(PDFDoc document, int errCode) {
            isOpenSuccess = errCode == Constants.e_ErrSuccess;
            if (App.instance().isMultiTab() && errCode == Constants.e_ErrSuccess) {
                mDocPath = pdfViewerCtrl.getFilePath();

                PDFReaderFragment fragment = (PDFReaderFragment) App.instance().getTabsManager(filter).getFragmentMap().get(mDocPath);
                if (!getPath().equals(mDocPath)) {
                    //Remove the same file that has been opened
                    if (fragment != null) {
                        App.instance().getMultiTabView(filter).getHistoryFileNames().remove(mDocPath);

                        FragmentManager mFragmentManager = App.instance().getTabsManager(filter).getFragmentManager();
                        FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
                        fragmentTransaction.remove(fragment).commitAllowingStateLoss();

                        App.instance().getTabsManager(filter).getFragmentMap().remove(mDocPath);
                    }

                    App.instance().getTabsManager(filter).getFragmentMap().remove(getPath());
                    App.instance().getTabsManager(filter).addFragment(mDocPath, PDFReaderFragment.this);
                    App.instance().getTabsManager(filter).setFilePath(mDocPath);

                    int index = App.instance().getMultiTabView(filter).getHistoryFileNames().indexOf(getPath());
                    App.instance().getMultiTabView(filter).getHistoryFileNames().set(index, mDocPath);

                    setPath(mDocPath);
                    App.instance().getMultiTabView(filter).refreshTopBar(mDocPath);
                } else {
                    if (fragment != null && fragment != PDFReaderFragment.this) {
                        FragmentManager mFragmentManager = App.instance().getTabsManager(filter).getFragmentManager();
                        FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
                        fragmentTransaction.remove(fragment).commitAllowingStateLoss();
                        App.instance().getTabsManager(filter).addFragment(getPath(), PDFReaderFragment.this);
                    }
                    App.instance().getMultiTabView(filter).refreshTopBar(getPath());
                }

                int h = mUiExtensionsManager.getMainFrame().getTopToolbar().getContentView().getHeight();
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 2 * h / 3);
                params.topMargin = -10;
                ViewGroup parent = (ViewGroup) App.instance().getMultiTabView(filter).getTabView().getParent();
                if (parent != null) {
                    parent.removeView(App.instance().getMultiTabView(filter).getTabView());
                }
                mUiExtensionsManager.getMainFrame().addSubViewToTopBar(App.instance().getMultiTabView(filter).getTabView(), 1, params);
            }
        }

        @Override
        public void onDocWillClose(PDFDoc document) {

        }

        @Override
        public void onDocClosed(PDFDoc document, int errCode) {
            if (isSaveDocInCurPath) {
                File file = new File(currentFileCachePath);
                File docFile = new File(mDocPath);
                Context context = App.instance().getApplicationContext();
                if (file.exists()) {
                    docFile.delete();
                    if (!file.renameTo(docFile))
                        UIToast.getInstance(context).show(context.getString(R.string.fx_save_file_failed));
                } else {
                    UIToast.getInstance(context).show(context.getString(R.string.fx_save_file_failed));
                }

            }

            if (errCode == e_ErrSuccess && isSaveDocInCurPath) {
                updateThumbnail(mSavePath);
            }

            if (mFragmentEvent != null) {
                mFragmentEvent.onRemove();
            }
        }

        @Override
        public void onDocWillSave(PDFDoc document) {

        }

        @Override
        public void onDocSaved(PDFDoc document, int errCode) {
            if (errCode == e_ErrSuccess && !isSaveDocInCurPath) {
                updateThumbnail(mSavePath);
            }

            if (isCloseDocAfterSaving) {
                closeAndSaveDoc(mFragmentEvent);
            }
        }
    };


    private IPDFReader.BackEventListener mBackEventListener = new IPDFReader.BackEventListener() {
        @Override
        public boolean onBack() {
            if (App.instance().isMultiTab()) {
                FragmentManager fragmentManager = App.instance().getTabsManager(filter).getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                BaseFragment currentFrag = App.instance().getTabsManager(filter).getCurrentFragment();
                fragmentTransaction.hide(currentFrag).commitAllowingStateLoss();

                ((MainActivity) getActivity()).changeReaderState(MainActivity.READER_STATE_HOME);
                return true;
            }
            return false;
        }
    };

    public void doClose(final IFragmentEvent callback) {
        if (pdfViewerCtrl == null) return;

        if (pdfViewerCtrl.isDynamicXFA()) {
            DynamicXFAModule dynamicXFAModule = (DynamicXFAModule) mUiExtensionsManager.getModuleByName(Module.MODULE_NAME_DYNAMICXFA);
            if (dynamicXFAModule != null && dynamicXFAModule.getCurrentXFAWidget() != null) {
                dynamicXFAModule.setCurrentXFAWidget(null);
            }
        }

        final Context context = App.instance().getApplicationContext();
        if (pdfViewerCtrl.getDoc() == null || !mUiExtensionsManager.getDocumentManager().isDocModified()) {
            mProgressMsg = context.getString(R.string.fx_string_closing);
            closeAndSaveDoc(callback);
            return;
        }

        final boolean hideSave = !pdfViewerCtrl.isDynamicXFA() && !mUiExtensionsManager.canModifyContents();
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        String[] items;
        if (hideSave) {
            items = new String[]{
                    context.getString(R.string.rv_back_save_to_new_file),
                    context.getString(R.string.rv_back_discard_modify),
            };
        } else {
            items = new String[]{
                    context.getString(R.string.rv_back_save_to_original_file),
                    context.getString(R.string.rv_back_save_to_new_file),
                    context.getString(R.string.rv_back_discard_modify),
            };
        }

        builder.setItems(items, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (hideSave) {
                    which += 1;
                }
                switch (which) {
                    case 0: // save
                        isCloseDocAfterSaving = true;
                        String userSavePath = mUiExtensionsManager.getSavePath();
                        if (userSavePath != null && userSavePath.length() > 0 && !userSavePath.equalsIgnoreCase(mDocPath)) {
                            File userSaveFile = new File(userSavePath);
                            File defaultSaveFile = new File(mDocPath);
                            if (userSaveFile.getParent().equalsIgnoreCase(defaultSaveFile.getParent())) {
                                isSaveDocInCurPath = true;
                                mSavePath = userSavePath;
                            } else {
                                isSaveDocInCurPath = false;
                            }
                            pdfViewerCtrl.saveDoc(userSavePath, mUiExtensionsManager.getSaveDocFlag());
                        } else {
                            isSaveDocInCurPath = true;
                            pdfViewerCtrl.saveDoc(getCacheFile(), mUiExtensionsManager.getSaveDocFlag());
                        }
                        isSaveDocInCurPath = true;
                        mProgressMsg = context.getString(R.string.fx_string_saving);
                        mFragmentEvent = callback;
                        showProgressDialog();
                        break;
                    case 1: // save as
                        mProgressMsg = context.getString(R.string.fx_string_saving);
                        onSaveAsClicked();
                        break;
                    case 2: // discard modify
                        mProgressMsg = context.getString(R.string.fx_string_closing);
                        closeAndSaveDoc(callback);
                        break;
                    default:
                        break;
                }
                dialog.dismiss();
                mSaveAlertDlg = null;
            }

            void showInputFileNameDialog(final String fileFolder) {
                String newFilePath = fileFolder + "/" + AppFileUtil.getFileName(pdfViewerCtrl.getFilePath());
                final String filePath = AppFileUtil.getFileDuplicateName(newFilePath);
                final String fileName = AppFileUtil.getFileNameWithoutExt(filePath);

                final UITextEditDialog rmDialog = new UITextEditDialog(getActivity());
                rmDialog.setPattern("[/\\:*?<>|\"\n\t]");
                rmDialog.setTitle(AppResource.getString(context, R.string.fx_string_saveas));
                rmDialog.getPromptTextView().setVisibility(View.GONE);
                rmDialog.getInputEditText().setText(fileName);
                rmDialog.getInputEditText().selectAll();
                rmDialog.show();
                AppUtil.showSoftInput(rmDialog.getInputEditText());

                rmDialog.getOKButton().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        rmDialog.dismiss();
                        String inputName = rmDialog.getInputEditText().getText().toString();
                        String newPath = fileFolder + "/" + inputName;
                        newPath += ".pdf";
                        File file = new File(newPath);
                        if (file.exists()) {
                            showAskReplaceDialog(fileFolder, newPath);
                        } else {
                            isCloseDocAfterSaving = true;
                            mSavePath = newPath;
                            pdfViewerCtrl.saveDoc(newPath, mUiExtensionsManager.getSaveDocFlag());
                            mFragmentEvent = callback;
                            showProgressDialog();
                        }
                    }
                });
            }

            void showAskReplaceDialog(final String fileFolder, final String newPath) {
                final UITextEditDialog rmDialog = new UITextEditDialog(getActivity());
                rmDialog.setTitle(AppResource.getString(context, R.string.fx_string_saveas));
                rmDialog.getPromptTextView().setText(AppResource.getString(context, R.string.fx_string_filereplace_warning));
                rmDialog.getInputEditText().setVisibility(View.GONE);
                rmDialog.show();

                rmDialog.getOKButton().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        rmDialog.dismiss();
                        isCloseDocAfterSaving = true;
                        mSavePath = newPath;
                        if (newPath.equalsIgnoreCase(pdfViewerCtrl.getFilePath())) {
                            isSaveDocInCurPath = true;
                            pdfViewerCtrl.saveDoc(getCacheFile(), mUiExtensionsManager.getSaveDocFlag());
                        } else {
                            isSaveDocInCurPath = false;
                            pdfViewerCtrl.saveDoc(newPath, mUiExtensionsManager.getSaveDocFlag());
                        }
                        mFragmentEvent = callback;
                        showProgressDialog();
                    }
                });

                rmDialog.getCancelButton().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        rmDialog.dismiss();
                        showInputFileNameDialog(fileFolder);
                    }
                });
            }

            void onSaveAsClicked() {
                mFolderSelectDialog = new UIFolderSelectDialog(getActivity());
                mFolderSelectDialog.setFileFilter(new FileFilter() {
                    @Override
                    public boolean accept(File pathname) {
                        return !(pathname.isHidden() || !pathname.canRead()) && !pathname.isFile();
                    }
                });
                mFolderSelectDialog.setTitle(AppResource.getString(context, R.string.fx_string_saveas));
                mFolderSelectDialog.setButton(MatchDialog.DIALOG_OK | MatchDialog.DIALOG_CANCEL);
                mFolderSelectDialog.setListener(new MatchDialog.DialogListener() {
                    @Override
                    public void onResult(long btType) {
                        if (btType == MatchDialog.DIALOG_OK) {
                            String fileFolder = mFolderSelectDialog.getCurrentPath();
                            showInputFileNameDialog(fileFolder);
                        }
                        mFolderSelectDialog.dismiss();
                    }

                    @Override
                    public void onBackClick() {
                    }
                });
                mFolderSelectDialog.showDialog();
            }
        });

        mSaveAlertDlg = builder.create();
        mSaveAlertDlg.setCanceledOnTouchOutside(true);
        mSaveAlertDlg.show();
    }

    private String getCacheFile() {
        mSavePath = pdfViewerCtrl.getFilePath();
        File file = new File(mSavePath);
        String dir = file.getParent() + "/";
        while (file.exists()) {
            currentFileCachePath = dir + AppDmUtil.randomUUID(null) + ".pdf";
            file = new File(currentFileCachePath);
        }
        return currentFileCachePath;
    }

    private void showProgressDialog() {
        if (mProgressDlg == null && getActivity() != null) {
            mProgressDlg = new ProgressDialog(getActivity());
            mProgressDlg.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mProgressDlg.setCancelable(false);
            mProgressDlg.setIndeterminate(false);
            mProgressDlg.setMessage(mProgressMsg);
            AppDialogManager.getInstance().showAllowManager(mProgressDlg, null);
        }
    }

    private void closeAndSaveDoc(IFragmentEvent callback) {
        showProgressDialog();
        pdfViewerCtrl.closeDoc();
        mFragmentEvent = callback;
    }

    private void updateThumbnail(String path) {
        LocalModule module = (LocalModule) mUiExtensionsManager.getModuleByName(Module.MODULE_NAME_LOCAL);
        if (module != null && path != null) {
            module.updateThumbnail(path);
        }
    }

    @Override
    public void handleActivityResult(int requestCode, int resultCode, Intent data) {
        mUiExtensionsManager.handleActivityResult(getActivity(), requestCode, resultCode, data);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (mFolderSelectDialog != null && mFolderSelectDialog.isShowing()){
            mFolderSelectDialog.setHeight(mFolderSelectDialog.getDialogHeight());
            mFolderSelectDialog.showDialog();
        }
    }
}
