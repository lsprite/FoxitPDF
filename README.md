https://github.com/getActivity/XXPermissions
https://github.com/yewei02538/HiPermission

## 使用方法

``` java
private List<XXPermissionBean> permissionBeans;
private static final int code_request_permission = 90000;
permissionBeans = new ArrayList<XXPermissionBean>();
permissionBeans.add(new XXPermissionBean(Manifest.permission.WRITE_EXTERNAL_STORAGE, "存储", "开启访问本地存储权限", com.nemo.xxpermissiontool.R.drawable.permission_ic_storage));
permissionBeans.add(new XXPermissionBean(Manifest.permission.CAMERA, "相机", "开启相机权限", com.nemo.xxpermissiontool.R.drawable.permission_ic_camera));
checkPermissions();
```

``` java
private void checkPermissions() {
      XXPermissionTool.checkPermissions(this, //
              permissionBeans, //
              code_request_permission, //
              new XXPermissionTool.OnCheckPermissionsFinishedListener() {
                  @Override
                  public void onCheckPermissionsFinished(int flag) {
                      if (0 == flag) {
                          Log.e("ard", "全都授权了");
//                        nextAty();
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
```

## 是否有这个权限
if (XXPermissions.hasPermission(this, Permission.Group.STORAGE)) {

}
## 跳转到设置页面
XXPermissions.startPermissionActivity(this);

## 混淆方法

-keep class com.hjq.permissions.** {*;}