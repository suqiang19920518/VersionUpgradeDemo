package com.thinkive.bank.versionupgradedemo.download;


import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static android.content.Context.DOWNLOAD_SERVICE;

/**
 * @author: sq
 * @date: 2017/9/17
 * @corporation: 深圳市思迪信息技术股份有限公司
 * @description: 下载管理器DownloadManager封装类【注意：最后注销广播、资源释放】
 */
public class DownloadManagerUtils {

    private static DownloadManagerUtils instance = null;
    private static final Object obj = new Object();

    public static final String PARAM_TITLE = "title";
    public static final String PARAM_DESCRIPTION = "description";
    public static final String PARAM_MIME_TYPE = "mimeType";
    public static final String PARAM_PATH = "path";

    private Context context;
    private long downloadId;
    private boolean isRegisterReceiver;
    private DownloadCallback callback;
    private DownloadManager mDownloadManager;
    private Map<String, String> params = new HashMap<>();//下载管理器DownloadManager的配置参数
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            checkStatus();

        }
    };

    public interface DownloadCallback {
        void onPause();//下载暂停

        void onPending();//下载延迟

        void onRunning();//正在下载

        void onSuccess();//下载完成

        void onFailed();//下载失败
    }

    public void setCallback(DownloadCallback callback) {
        this.callback = callback;
    }

    private DownloadManagerUtils(Context context) {
        this.context = context;
        mDownloadManager = ((DownloadManager) context.getSystemService(DOWNLOAD_SERVICE));
    }

    public static DownloadManagerUtils getInstance(Context context) {
        if (instance == null) {
            synchronized (obj) {
                if (instance == null) {
                    instance = new DownloadManagerUtils(context);
                }
            }
        }
        return instance;
    }

    /**
     * 下载文件
     *
     * @param params DownloadManager的配置参数
     */
    public void downloadFile(Map<String, String> params) {

        this.params = params;
        //使用系统下载类
        mDownloadManager = (DownloadManager) context.getSystemService(DOWNLOAD_SERVICE);

        // 构造Request请求对象
        Uri uri = Uri.parse(params.get(PARAM_PATH));
        DownloadManager.Request request = new DownloadManager.Request(uri);
        //设置下载中通知栏提示的标题
        request.setTitle(params.get(PARAM_TITLE));
        //设置下载中通知栏提示的介绍
        request.setDescription(params.get(PARAM_DESCRIPTION));
        //是否允许漫游状态下，执行下载操作
        request.setAllowedOverRoaming(false);
        //设置允许使用的网络类型，这里是移动网络和wifi都可以
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE | DownloadManager.Request.NETWORK_WIFI);
        //机型适配
        request.setMimeType(params.get(PARAM_MIME_TYPE));
        //设置显示下载界面
        request.setVisibleInDownloadsUi(true);
        // 设置下载完毕之后，通知依然显示
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        //创建目录下载
        request.setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS, params.get(PARAM_TITLE));

        // 将Request对象添加到请求队列中，依次进行网络请求
        downloadId = mDownloadManager.enqueue(request);
        //注册广播，监听下载完成动作
        context.registerReceiver(mReceiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
        isRegisterReceiver = true;
    }

    /**
     * 检查下载状态
     */
    private void checkStatus() {
        DownloadManager.Query query = new DownloadManager.Query();
        query.setFilterById(downloadId);
        Cursor cursor = mDownloadManager.query(query);
        if (cursor.moveToFirst()) {
            int status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));
            switch (status) {
                case DownloadManager.STATUS_PAUSED:  //下载暂停
                    if (callback != null) {
                        callback.onPause();
                    }
                    break;
                case DownloadManager.STATUS_PENDING:  //下载延迟
                    if (callback != null) {
                        callback.onPending();
                    }
                    break;
                case DownloadManager.STATUS_RUNNING:  //正在下载
                    if (callback != null) {
                        callback.onRunning();
                    }
                    break;
                case DownloadManager.STATUS_SUCCESSFUL:  //下载完成
                    if (callback != null) {
                        callback.onSuccess();
                    }
                    break;
                case DownloadManager.STATUS_FAILED:  //下载失败
                    if (callback != null) {
                        callback.onFailed();
                    }
                    break;
            }
        }
        cursor.close();//关闭游标，防止出现内存泄露
    }

    /**
     * 安装APK，解决了7.0兼容问题
     */
    public void installAPK() {

        File apkFile = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), params.get(PARAM_TITLE));
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Uri uri;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {  //注意：compileSdkVersion 24
            uri = VersionFileProvider.getUriForFile(context, context.getPackageName() + ".versionProvider", apkFile);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } else {
            uri = Uri.fromFile(apkFile);
        }
        intent.setDataAndType(uri, "application/vnd.android.package-archive");
        context.startActivity(intent);
    }

    /**
     * 注销广播，防止出现内存泄露
     */
    public void unregisterReceiver() {
        if (isRegisterReceiver) {
            context.unregisterReceiver(mReceiver);
            isRegisterReceiver = false;
        }
    }

    /**
     * 资源释放，防止出现内存泄露
     */
    public void release() {
        instance = null;
    }

}
