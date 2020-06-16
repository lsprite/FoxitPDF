package com.nemo.foxitpdf.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.nemo.foxitpdf.R;
import com.nemo.xxpermissiontool.XXPermissionBean;
import com.nemo.xxpermissiontool.XXPermissionTool;

import java.util.ArrayList;
import java.util.List;

public class StartAvtivity extends Activity {
    //权限
    private List<XXPermissionBean> permissionBeans;
    private static final int code_request_permission = 90000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!this.isTaskRoot()) { // 判断该Activity是不是任务空间的源Activity，“非”也就是说是被系统重新实例化出来
            // 如果你就放在launcher Activity中话，这里可以直接return了
            Intent mainIntent = getIntent();
            String action = mainIntent.getAction();
            if (mainIntent.hasCategory(Intent.CATEGORY_LAUNCHER)
                    && action.equals(Intent.ACTION_MAIN)) {
                finish();
                return;// finish()之后该活动会继续执行后面的代码，你可以logCat验证，加return避免可能的exception
            }
        }
        setContentView(R.layout.activity_start);
        //
        permissionBeans = new ArrayList<XXPermissionBean>();
        permissionBeans.add(new XXPermissionBean(Manifest.permission.WRITE_EXTERNAL_STORAGE, "存储", "开启访问本地存储权限", com.nemo.xxpermissiontool.R.drawable.permission_ic_storage));
        permissionBeans.add(new XXPermissionBean(Manifest.permission.CAMERA, "相机", "开启相机权限", com.nemo.xxpermissiontool.R.drawable.permission_ic_camera));
        checkPermissions();
    }

    private void checkPermissions() {
        XXPermissionTool.checkPermissions(this, //
                permissionBeans, //
                code_request_permission, //
                new XXPermissionTool.OnCheckPermissionsFinishedListener() {
                    @Override
                    public void onCheckPermissionsFinished(int flag) {
                        if (0 == flag) {
                            Log.e("ard", "全都授权了");
//                            nextAty();
                            Intent intent = new Intent(StartAvtivity.this, MainActivity.class);
                            startActivity(intent);
                            overridePendingTransition(0, 0);
                            finish();
                            overridePendingTransition(0, 0);
                        } else {
                            Log.e("ard", "退出app");
                            finish();
                        }
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == code_request_permission) {
            checkPermissions();
        }
    }
}