package com.nemo.xxpermissiontool;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.Window;

import com.hjq.permissions.OnPermission;
import com.hjq.permissions.XXPermissions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.core.content.ContextCompat;

/**
 * 本工具依托于 https://github.com/getActivity/XXPermissions
 * 在其基础上进行封装，修改增加startActivityForResult到设置页面功能
 * www.gaohaiyan.com
 * vigiles
 */
public class XXPermissionTool {

    private XXPermissionTool() {
    }

    private static final String MARK = Build.MANUFACTURER.toLowerCase();
    private static OnCheckPermissionsFinishedListener onCheckPermissionsFinishedListener;
    private static List<String> targetPermissions;
    private static List<XXPermissionBean> targetPermissionBeans;
    private static Map<String, String> noteMap;
    private static int REQUEST_CODE;
    private static int step = 0;

    private static void showPermissionDialog(final Activity activity) {
        final Dialog mDialog = new Dialog(activity);
        String mAppName = activity.getApplicationInfo().loadLabel(activity.getPackageManager()).toString();
        String title = String.format(activity.getString(R.string.permission_dialog_title), mAppName);
        String msg = String.format(activity.getString(R.string.permission_dialog_msg), mAppName);
        PermissionView contentView = new PermissionView(activity);
        contentView.setGridViewColum(targetPermissions.size() < 3 ? targetPermissions.size() : 3);
        contentView.setTitle(title);
        contentView.setMsg(msg);
        contentView.setGridViewAdapter(new PermissionAdapter(targetPermissionBeans));
        int mStyleId = R.style.PermissionDefaultNormalStyle;
        int mFilterColor = activity.getResources().getColor(R.color.permissionColorGreen);
        contentView.setStyleId(mStyleId);
        contentView.setFilterColor(mFilterColor);
        contentView.setBtnOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mDialog != null && mDialog.isShowing())
                    mDialog.dismiss();
                //点击了确定
                requestPermissions(activity);
            }
        });
        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDialog.setContentView(contentView);
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.setCancelable(false);
        mDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        mDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                dialog.dismiss();
            }
        });
        mDialog.show();
    }

    /**
     * 启动权限检查
     *
     * @param activity        Activity
     * @param permissionBeans 要检查的权限集合
     * @param code            设置界面请求码
     * @param listener        检查回调
     */
    public static void checkPermissions(Activity activity, List<XXPermissionBean> permissionBeans, int code, OnCheckPermissionsFinishedListener listener) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return;
        }
        onCheckPermissionsFinishedListener = listener;
        REQUEST_CODE = code;
        noteMap = new HashMap<String, String>();
        step++;
        targetPermissions = new ArrayList<>();
        targetPermissionBeans = new ArrayList<>();
        for (int i = 0; i < permissionBeans.size(); i++) {
            String permission = permissionBeans.get(i).getPermission();
            noteMap.put(permission, permissionBeans.get(i).getTip());
            int i1 = ContextCompat.checkSelfPermission(activity, permission);
            if (i1 != 0) {
                targetPermissions.add(permission);
                targetPermissionBeans.add(permissionBeans.get(i));
            }
        }
        if (targetPermissions.size() > 0) {
            showPermissionDialog(activity);
        } else {
            step = 0;
            if (null != listener) {
                listener.onCheckPermissionsFinished(0);
            }
        }


    }


    private static void requestPermissions(final Activity activity) {
        XXPermissions.with(activity) //
                .permission(targetPermissions) //
                .request(new OnPermission() {
                    @Override
                    public void hasPermission(List<String> granted, boolean isAll) {
                        if (isAll) {
                            Log.e("ard", "获取权限成功：" + granted.toString());
                            step = 0;
                            if (null != onCheckPermissionsFinishedListener) {
                                onCheckPermissionsFinishedListener.onCheckPermissionsFinished(0);
                            }
                        } else {
                            Log.e("ard", "获取权限成功，部分权限不再提示：" + granted.toString());
                        }
                    }

                    @Override
                    public void noPermission(List<String> denied, boolean quick) {
                        if (quick) {
                            Log.e("ard", "被永久拒绝授权，请手动授予权限：" + denied);
                            //如果是被永久拒绝就跳转到应用权限系统设置页面
                            notePermissionSettings(activity, denied);
                        } else {
                            Log.e("ard", "获取权限失败");
                            notePermissionFails(activity, denied);
                        }
                    }
                });
    }

    private static void notePermissionFails(final Activity activity, List<String> denied) {
        String notes = getNotes(denied);
        new AlertDialog.Builder(activity) //
                .setTitle("权限说明") //
                .setMessage("您拒绝了必须的权限" + notes + "此时无法继续使用app") //
                .setNegativeButton("继续操作授权", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        requestPermissions(activity);
                    }
                }) //
                .setPositiveButton("不用了", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        step = 0;
                        if (null != onCheckPermissionsFinishedListener) {
                            onCheckPermissionsFinishedListener.onCheckPermissionsFinished(-1);
                        }
                    }
                }).show();
    }

    private static void notePermissionSettings(final Activity activity, List<String> denied) {

        String sb = getNotes(denied);

        new AlertDialog.Builder(activity) //
                .setTitle("权限说明") //
                .setMessage("第 " + step + " 次操作\n\n您上次勾选了\"不再提示\" " + sb.toString() + "如果不开启将无法继续使用") //
                .setNegativeButton("前往授权", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startSettingActivity(activity);
                    }
                }) //
                .setPositiveButton("不用了", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        step = 0;
                        if (null != onCheckPermissionsFinishedListener) {
                            onCheckPermissionsFinishedListener.onCheckPermissionsFinished(-1);
                        }
                    }
                }).show();
    }

    private static String getNotes(List<String> denied) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n");
        for (int i = 0; i < denied.size(); i++) {
            String note = noteMap.get(denied.get(i));
            sb.append(note + "\n");
        }

        return sb.toString();
    }

    private static void startSettingActivity(Activity activity) {
        startSettingActivity(activity, false);
    }

    private static void startSettingActivity(Activity activity, boolean newTask) {
        Log.e("ard", "MARK：" + MARK);

        Intent intent = null;
        if (MARK.contains("huawei")) {
            intent = huawei(activity);
        } else if (MARK.contains("xiaomi")) {
            intent = xiaomi(activity);
        } else if (MARK.contains("oppo")) {
            intent = oppo(activity);
        } else if (MARK.contains("vivo")) {
            intent = vivo(activity);
        } else if (MARK.contains("meizu")) {
            intent = meizu(activity);
        }

        if (intent == null || !hasIntent(activity, intent)) {
            intent = google(activity);
        }

        if (newTask) {
            // 如果用户在权限设置界面改动了权限，请求权限 Activity 会被重启，加入这个 Flag 就可以避免
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }

        try {
            activity.startActivityForResult(intent, REQUEST_CODE);
        } catch (Exception e) {
            Log.e("ard", "创建 setting intent err：" + e.getMessage());
            intent = google(activity);
            activity.startActivityForResult(intent, REQUEST_CODE);
        }
    }

    private static Intent google(Context context) {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.fromParts("package", context.getPackageName(), null));
        return intent;
    }

    private static Intent huawei(Context context) {
        Intent intent = new Intent();

        intent.setClassName("com.huawei.systemmanager", "com.huawei.permissionmanager.ui.SingleAppActivity");
        if (hasIntent(context, intent)) {
            return intent;
        }

        intent.setComponent(new ComponentName("com.android.packageinstaller", "com.android.packageinstaller.permission.ui.ManagePermissionsActivity"));
        if (hasIntent(context, intent)) {
            return intent;
        }

        intent.setComponent(new ComponentName("com.huawei.systemmanager", "com.huawei.permissionmanager.ui.MainActivity"));
        if (hasIntent(context, intent)) {
            return intent;
        }

        return intent;
    }

    private static Intent xiaomi(Context context) {
        Intent intent = new Intent("miui.intent.action.APP_PERM_EDITOR");
        intent.putExtra("extra_pkgname", context.getPackageName());
        if (hasIntent(context, intent)) {
            return intent;
        }

        intent.setPackage("com.miui.securitycenter");
        if (hasIntent(context, intent)) {
            return intent;
        }

        intent.setClassName("com.miui.securitycenter", "com.miui.permcenter.permissions.AppPermissionsEditorActivity");
        if (hasIntent(context, intent)) {
            return intent;
        }

        intent.setClassName("com.miui.securitycenter", "com.miui.permcenter.permissions.PermissionsEditorActivity");
        return intent;
    }

    private static Intent oppo(Context context) {
        Intent intent = new Intent();
        intent.putExtra("packageName", context.getPackageName());

        intent.setClassName("com.color.safecenter", "com.color.safecenter.permission.PermissionManagerActivity");
        if (hasIntent(context, intent)) {
            return intent;
        }

        intent.setClassName("com.coloros.safecenter", "com.coloros.safecenter.permission.PermissionManagerActivity");
        if (hasIntent(context, intent)) {
            return intent;
        }

        intent.setClassName("com.coloros.securitypermission", "com.coloros.securitypermission.permission.PermissionGroupsActivity");
        if (hasIntent(context, intent)) {
            return intent;
        }

        intent.setClassName("com.coloros.securitypermission", "com.coloros.securitypermission.permission.PermissionManagerActivity");
        if (hasIntent(context, intent)) {
            return intent;
        }

        intent.setClassName("com.oppo.safe", "com.oppo.safe.permission.PermissionAppListActivity");
        if (hasIntent(context, intent)) {
            return intent;
        }
        return intent;
    }

    private static Intent vivo(Context context) {
        Intent intent = new Intent();
        intent.putExtra("packagename", context.getPackageName());

        // vivo x7 Y67 Y85
        intent.setClassName("com.iqoo.secure", "com.iqoo.secure.safeguard.SoftPermissionDetailActivity");
        if (hasIntent(context, intent)) {
            return intent;
        }

        // vivo Y66 x20 x9
        intent.setClassName("com.vivo.permissionmanager", "com.vivo.permissionmanager.activity.SoftPermissionDetailActivity");
        if (hasIntent(context, intent)) {
            return intent;
        }

        // Y85
        intent.setClassName("com.vivo.permissionmanager", "com.vivo.permissionmanager.activity.PurviewTabActivity");
        if (hasIntent(context, intent)) {
            return intent;
        }

        // 跳转会报 java.lang.SecurityException: Permission Denial
        intent.setClassName("com.android.packageinstaller", "com.android.packageinstaller.permission.ui.ManagePermissionsActivity");
        if (hasIntent(context, intent)) {
            return intent;
        }

        intent.setComponent(new ComponentName("com.iqoo.secure", "com.iqoo.secure.safeguard.SoftPermissionDetailActivity"));
        return intent;
    }

    private static Intent meizu(Context context) {
        Intent intent = new Intent("com.meizu.safe.security.SHOW_APPSEC");
        intent.putExtra("packageName", context.getPackageName());
        intent.setComponent(new ComponentName("com.meizu.safe", "com.meizu.safe.security.AppSecActivity"));
        return intent;
    }

    private static boolean hasIntent(Context context, Intent intent) {
        return !context.getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY).isEmpty();
    }

    public interface OnCheckPermissionsFinishedListener {
        /**
         * @param flag 0=全都授权了，-1-有未授权，不再继续了，退出app
         */
        public void onCheckPermissionsFinished(int flag);
    }
}
