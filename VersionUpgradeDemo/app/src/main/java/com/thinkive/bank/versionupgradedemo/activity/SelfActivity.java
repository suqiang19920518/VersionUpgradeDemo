package com.thinkive.bank.versionupgradedemo.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.thinkive.bank.versionupgradedemo.R;
import com.thinkive.bank.versionupgradedemo.bean.UpdateAppInfo;
import com.thinkive.bank.versionupgradedemo.download.DownloadReceiver;
import com.thinkive.bank.versionupgradedemo.download.DownloadService;
import com.thinkive.bank.versionupgradedemo.download.DownloadTask;
import com.thinkive.bank.versionupgradedemo.http.BaseCallback;
import com.thinkive.bank.versionupgradedemo.http.OkHttpHelper;
import com.thinkive.bank.versionupgradedemo.util.AppInfoUtils;
import com.thinkive.bank.versionupgradedemo.util.DialogHelper;
import com.thinkive.bank.versionupgradedemo.download.DownloadManagerUtils;
import com.thinkive.bank.versionupgradedemo.util.ToastUtils;

import java.util.HashMap;
import java.util.Map;

public class SelfActivity extends AppCompatActivity {

    private static final String UPGRADE_URL = "http://bz.budejie.com";

    private Context context;
    //安装包路径 此处模拟为百度新闻APP地址
    private String downPath = "http://gdown.baidu.com/data/wisegame/f98d235e39e29031/baiduxinwen.apk";

    //给用户显示的版本号
    private String versionName;
    //用户不可见控制版本号
    private int versionCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_self);
        context = this;
        versionCode = AppInfoUtils.getApp(this).getVersionCode(this);//当前版本号
        versionName = AppInfoUtils.getApp(this).getVersionName(this);//当前版本名称
    }

    @Override
    protected void onStop() {
        super.onStop();
        //注销广播
        DownloadManagerUtils.getInstance(context).unregisterReceiver();
        //静态类资源释放，垃圾回收
        DownloadManagerUtils.getInstance(context).release();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //注销广播
        DownloadManagerUtils.getInstance(context).unregisterReceiver();
        //静态类资源释放，垃圾回收
        DownloadManagerUtils.getInstance(context).release();
    }

    public void onClick(View view) {
        if (view.getId() == R.id.btn_self_manager) {  //采用DownloadManager下载

            checkUpdate();//检测版本号，进行更新

        } else if (view.getId() == R.id.btn_self_service) {  //采用DownloadService、DownloadReceiver下载

            ProgressDialog pd = createDialog();
            Intent intent = new Intent(this, DownloadService.class);
            intent.putExtra(DownloadService.FLAG_URL, downPath);
            intent.putExtra(DownloadService.FLAG_NAME, "百度.apk");
            intent.putExtra(DownloadService.FlAG_RECEIVER, new DownloadReceiver(this, new Handler(), pd));
            startService(intent);

        } else if (view.getId() == R.id.btn_self_task) {  //采用DownloadTask下载
            ProgressDialog pd = createDialog();
            DownloadTask task = new DownloadTask(context, pd);
            task.execute(downPath, "百度.apk");
//            task.cancel(true);//中途取消下载，会调用onCancelled()方法
        }
    }

    /**
     * 创建进度对话框
     *
     * @return
     */
    public ProgressDialog createDialog() {
        // 创建对话框
        ProgressDialog pd = new ProgressDialog(context);
        // 设置对话框水平样式
        pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        // 设置titile
        pd.setTitle("提示：");
        // 设置message
        pd.setMessage("正在下载中...");
        pd.setCancelable(false);
        return pd;
    }

    /**
     * 发送网络请求，从服务器获取版本信息
     */
    private void checkUpdate() {

        OkHttpHelper.getInstance().get(UPGRADE_URL, new BaseCallback<UpdateAppInfo>() {

            @Override
            public void onBeforeRequest(Request request) {

                DialogHelper.showWaitDialog(context, null, "正在获取最新版本信息...");
            }

            @Override
            public void onFailure(Request request, Exception e) {
                DialogHelper.hiddenWaitDialog();
                ToastUtils.showToast(context, "网络异常，请检测网络设置");
            }

            @Override
            public void onResponse(Response response) {

            }

            @Override
            public void onSuccess(Response response, UpdateAppInfo updateAppInfo) {
                DialogHelper.hiddenWaitDialog();
                Integer errorCode = updateAppInfo.getError_code();
                if (errorCode == 1) {

                    final UpdateAppInfo.UpdateInfo data = updateAppInfo.getData();
                    String serviceCode = data.getVersionCode();

                    if (Integer.valueOf(serviceCode) > versionCode) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                showUpdateDialog(data.getVersionName());//更新提示对话框
                            }
                        });

                    } else {
                        ToastUtils.showToast(context, "当前为最新版本，无需更新");
                    }

                } else {
                    ToastUtils.showToast(context, updateAppInfo.getError_msg());
                }
            }

            @Override
            public void onError(Response response, int code, Exception e) {
                DialogHelper.hiddenWaitDialog();
                ToastUtils.showToast(context, "获取数据异常，请检测服务器配置");
            }
        });

    }

    /**
     * 更新提示对话框
     *
     * @param versionName 从服务器获取的最新版本名称
     */
    private void showUpdateDialog(String versionName) {

        String temp = getResources().getString(R.string.newest_apk_down);
        String message = String.format(temp, versionName);
        // 显示版本更新对话框
        DialogHelper.showConfirmDialog(context, -1, "版本更新", message,
                "立即更新", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        // 初始化下载管理器DownloadManager的配置参数
                        Map<String, String> params = new HashMap<>();
                        params.put(DownloadManagerUtils.PARAM_TITLE, "百度.apk");
                        params.put(DownloadManagerUtils.PARAM_DESCRIPTION, "正在下载中...");
                        params.put(DownloadManagerUtils.PARAM_MIME_TYPE, "application/vnd.android.package-archive");
                        params.put(DownloadManagerUtils.PARAM_PATH, downPath);
                        DownloadManagerUtils.getInstance(context).setCallback(new DownloadManagerUtils.DownloadCallback() {
                            @Override
                            public void onPause() {
                                ToastUtils.showToast(context, "下载已暂停");
                            }

                            @Override
                            public void onPending() {
                                ToastUtils.showToast(context, "下载已延迟");
                            }

                            @Override
                            public void onRunning() {
                                ToastUtils.showToast(context, "正在下载中...");
                            }

                            @Override
                            public void onSuccess() {
                                //安装APK
                                DownloadManagerUtils.getInstance(context).installAPK();
                            }

                            @Override
                            public void onFailed() {
                                ToastUtils.showToast(context, "下载失败");
                            }
                        });
                        DownloadManagerUtils.getInstance(context).downloadFile(params);
                    }
                }, "下次再说", null);
    }

}
