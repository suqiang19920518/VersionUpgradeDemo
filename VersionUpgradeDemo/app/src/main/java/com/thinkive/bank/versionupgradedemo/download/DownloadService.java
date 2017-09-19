package com.thinkive.bank.versionupgradedemo.download;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.ResultReceiver;
import android.support.v4.app.NotificationCompat;

import com.thinkive.bank.versionupgradedemo.R;
import com.thinkive.bank.versionupgradedemo.util.NetWorkUtils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * @author: sq
 * @date: 2017/9/15
 * @corporation: 深圳市思迪信息技术股份有限公司
 * @description: 启动service，后台进行文件下载，并配合DownloadReceiver一起使用，显示进度对话框
 */
public class DownloadService extends IntentService {

    private static final int NOTIFICATION_ID = 1;
    public static final int UPDATE_PROGRESS = 2;
    public static final int DOWNLOAD_PAUSE = 3;
    public static final String FLAG_URL = "url";
    public static final String FLAG_NAME = "file_name";
    public static final String FLAG_PROGRESS = "progress";
    public static final String FlAG_RECEIVER = "receiver";

    private String savePath;

    /**
     * IntentService必须包含一个无参构造器
     */
    public DownloadService() {
        this("DownloadService");
    }

    public DownloadService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String urlToDownload = intent.getStringExtra(FLAG_URL);//文件下载地址
        String fileName = intent.getStringExtra(FLAG_NAME);//文件名
        ResultReceiver receiver = (ResultReceiver) intent.getParcelableExtra(FlAG_RECEIVER);

        //检测网络状态
        boolean statu = true;//网络状态标识，true:正常，false:断开
        statu = NetWorkUtils.isNetworkAvailable(this);
        if (!statu) {
            receiver.send(DOWNLOAD_PAUSE, null);
        }


        /**
         * 根据下载的进度发送相应的通知，提醒用户进度
         */
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        //设置通知小图标
        builder.setSmallIcon(R.mipmap.icon);
        builder.setContentTitle("正在下载...");
        builder.setContentText("已下载 0%");
        //设置通知在状态栏的提示文本,第一次提示消息的时候显示在通知栏上
        builder.setTicker("开始下载...");
        builder.setAutoCancel(true);

        InputStream is = null;
        BufferedOutputStream bos = null;
        String saveDir = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
        savePath = saveDir + File.separator + fileName;

        HttpURLConnection conn = null;
        try {
            URL url = new URL(urlToDownload);
            conn = (HttpURLConnection) url.openConnection();

            if (conn != null && conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                is = conn.getInputStream();
                bos = new BufferedOutputStream(new FileOutputStream(new File(savePath)));

                int len = 0;
                int currentProgress = 0;
                byte[] buffer = new byte[1024 * 8];
                int totlaLength = conn.getContentLength();//获取文件的总长度

                while ((len = is.read(buffer)) != -1) {
                    currentProgress += len;
                    Bundle resultData = new Bundle();

                    //检测网络状态
                    statu = NetWorkUtils.isNetworkAvailable(this);
                    if (!statu) {
                        receiver.send(DOWNLOAD_PAUSE, null);
                        break;
                    }

                    resultData.putInt(FLAG_PROGRESS, (int) (currentProgress * 100.0f / totlaLength));
                    receiver.send(UPDATE_PROGRESS, resultData);

                    //发送通知，提醒用户当前下载进度
                    builder.setProgress(totlaLength, currentProgress, false);
                    builder.setContentText("已下载 " +
                            (int) (100.0f * currentProgress / totlaLength) + "%");
                    manager.notify(NOTIFICATION_ID, builder.build());

                    //将下载的流数据输出到Download文件夹下的文件中
                    bos.write(buffer, 0, len);
                    bos.flush();
                }

                Thread.sleep(1000);
                if (statu) {
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    Uri uri;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        uri = VersionFileProvider.getUriForFile(this, getPackageName() + ".versionProvider", new File(savePath));
                        i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    } else {
                        uri = Uri.fromFile(new File(savePath));
                    }
                    //设置intent的类型
                    i.setDataAndType(uri, "application/vnd.android.package-archive");
                    PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, i, 0);
                    builder.setContentIntent(pendingIntent);
                    builder.setTicker("任务下载完成");
                    builder.setContentTitle("下载完成");
                    builder.setContentText("点击安装");
                } else {
                    builder.setTicker("下载出错");
                    builder.setContentTitle("网络断开");
                    builder.setContentText("请重新下载");
                }
                // 设置系统默认的音乐、灯光
                builder.setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_LIGHTS);
                manager.notify(NOTIFICATION_ID, builder.build());

                //TODO 执行后续操作
                if (statu) {
                    installAPK();
                }

            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
                if (bos != null) {
                    bos.close();
                }
            } catch (Exception e) {

            }
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    /**
     * 安装APK，解决了7.0兼容问题
     */
    public void installAPK() {

        File apkFile = new File(savePath);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Uri uri;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {  //注意：compileSdkVersion 24
            uri = VersionFileProvider.getUriForFile(this, getPackageName() + ".versionProvider", apkFile);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } else {
            uri = Uri.fromFile(apkFile);
        }
        intent.setDataAndType(uri, "application/vnd.android.package-archive");
        startActivity(intent);
    }

}











