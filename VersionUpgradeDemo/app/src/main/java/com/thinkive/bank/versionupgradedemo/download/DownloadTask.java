package com.thinkive.bank.versionupgradedemo.download;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.widget.Toast;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * @author: sq
 * @date: 2017/9/17
 * @corporation: 深圳市思迪信息技术股份有限公司
 * @description: Download异步任务
 */
public class DownloadTask extends AsyncTask<String, Integer, String> {

    private Context context;
    private String saveDir;
    private String savePath;
    private ProgressDialog progressDialog;

    public DownloadTask(Context context, ProgressDialog dialog) {
        this.progressDialog = dialog;
        this.context = context;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        progressDialog.show();
        saveDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
    }

    @Override
    protected String doInBackground(String... params) {
        InputStream is = null;
        BufferedOutputStream bos = null;
        savePath = saveDir + File.separator + params[1];

        HttpURLConnection conn = null;
        try {
            URL url = new URL(params[0]);
            conn = (HttpURLConnection) url.openConnection();
            if (conn == null || conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                return "Server returned HTTP " + conn.getResponseCode()
                        + " " + conn.getResponseMessage();
            }

            is = conn.getInputStream();
            bos = new BufferedOutputStream(new FileOutputStream(new File(savePath)));

            int len = 0;
            int currentProgress = 0;
            byte[] buffer = new byte[1024 * 8];
            int totlaLength = conn.getContentLength();//获取文件的总长度
            while ((len = is.read(buffer)) != -1) {

                if (isCancelled()) {
                    is.close();
                    bos.close();
                    return null;
                }

                currentProgress += len;
                if (totlaLength > 0) {
                    publishProgress((int) (currentProgress * 100 / totlaLength));
                }

                //将下载的流数据输出到Download文件夹下的文件中
                bos.write(buffer, 0, len);
                bos.flush();

            }

        } catch (Exception e) {
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

        return null;
    }

    @Override
    protected void onProgressUpdate(Integer... progress) {
        super.onProgressUpdate(progress);
        progressDialog.setIndeterminate(false);
        progressDialog.setMax(100);
        progressDialog.setProgress(progress[0]);
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        int progress = progressDialog.getProgress();
        progressDialog.dismiss();
        if (progress != 100) {
            Toast.makeText(context, "下载失败，请点击重新下载", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(context, "下载完成", Toast.LENGTH_SHORT).show();
            installAPK();
        }

    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        progressDialog.setProgress(0);
        progressDialog.dismiss();
    }

    /**
     * 安装APK，解决了7.0兼容问题
     */
    public void installAPK() {

        File apkFile = new File(savePath);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Uri uri;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {   //注意：compileSdkVersion 24
            uri = VersionFileProvider.getUriForFile(context, context.getPackageName() + ".versionProvider", apkFile);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } else {
            uri = Uri.fromFile(apkFile);
        }
        intent.setDataAndType(uri, "application/vnd.android.package-archive");
        context.startActivity(intent);
    }

}
