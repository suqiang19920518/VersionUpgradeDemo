package com.thinkive.bank.versionupgradedemo.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.pgyersdk.crash.PgyCrashManager;
import com.pgyersdk.feedback.PgyFeedbackShakeManager;
import com.pgyersdk.javabean.AppBean;
import com.pgyersdk.update.PgyUpdateManager;
import com.pgyersdk.update.UpdateManagerListener;
import com.thinkive.bank.versionupgradedemo.R;
import com.thinkive.bank.versionupgradedemo.util.DialogHelper;
import com.thinkive.bank.versionupgradedemo.util.PermissionUtils;
import com.thinkive.bank.versionupgradedemo.util.ToastUtils;

/**
 * @author: sq
 * @date: 2017/9/17
 * @corporation: 深圳市思迪信息技术股份有限公司
 * @description: 借助蒲公英第三方平台，进行app应用更新升级。并注册crash接口，收集crash日志
 */
public class PgyerActivity extends AppCompatActivity {

    private Context context;
    private boolean isPermitted;

    /**
     * 权限授予通过，回调
     */
    private PermissionUtils.PermissionGrant mPermissionGrant = new PermissionUtils.PermissionGrant() {
        @Override
        public void onPermissionGranted(int requestCode) {
            switch (requestCode) {
                case PermissionUtils.CODE_WRITE_EXTERNAL_STORAGE:
                    isPermitted = true;
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pgyer);
        context = this;
//        PgyCrashManager.register(this);//注册Crash接口(蒲公英)
    }

    @Override
    protected void onResume() {
        super.onResume();

        //申请权限——读取存储空间
        PermissionUtils.requestPermission(this, PermissionUtils.CODE_WRITE_EXTERNAL_STORAGE, mPermissionGrant, false);

    }

    public void onClick(View view) {
        if (view.getId() == R.id.btn_upgrade) {  //默认更新对话框

            if (isPermitted) {
                PgyUpdateManager.setIsForced(false); //设置是否强制更新。true为强制更新；false为不强制更新（默认值）。
                PgyUpdateManager.register(PgyerActivity.this, "provide");
            } else {
                ToastUtils.showToast(context, "请授予读写权限");
            }

        } else if (view.getId() == R.id.btn_upgrade_self) {  //自定义更新对话框

            if (isPermitted) {
                PgyUpdateManager.register(PgyerActivity.this, "provide",
                        new UpdateManagerListener() {

                            @Override
                            public void onUpdateAvailable(final String result) {

                                // 将新版本信息封装到AppBean中
                                final AppBean appBean = getAppBeanFromString(result);
                                DialogHelper.showConfirmDialog(context, -1, "更新提示：", "当前检测到有最新版本，是否进行更新？",
                                        "立即更新", new DialogInterface.OnClickListener() {

                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                startDownloadTask(PgyerActivity.this, appBean.getDownloadURL());
                                            }
                                        }, "下次再说", null);

                            }

                            @Override
                            public void onNoUpdateAvailable() {
                                ToastUtils.showToast(context, "当前为最新版本，无需更新");
                            }
                        });
            } else {
                ToastUtils.showToast(this, "请授予读写权限");
            }
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        PgyCrashManager.unregister();//解除注册
        PgyFeedbackShakeManager.unregister();//解除注册
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        PgyCrashManager.unregister();//解除注册
        PgyFeedbackShakeManager.unregister();//解除注册
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(final int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        PermissionUtils.requestPermissionsResult(this, requestCode, permissions, grantResults, mPermissionGrant, false);
    }

}
