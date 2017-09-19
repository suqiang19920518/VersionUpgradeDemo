package com.thinkive.bank.versionupgradedemo.download;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.widget.Toast;

/**
 * @author: sq
 * @date: 2017/9/16
 * @corporation: 深圳市思迪信息技术股份有限公司
 * @description: 下载接收器，更新进度条，配合DownloadService一起使用
 */
public class DownloadReceiver extends ResultReceiver {

    private Context context;
    private ProgressDialog mProgressDialog;

    /**
     * Create a new ResultReceive to receive results.  Your
     * {@link #onReceiveResult} method will be called from the thread running
     * <var>handler</var> if given, or from an arbitrary thread if null.
     *
     * @param handler
     */
    public DownloadReceiver(Handler handler) {
        super(handler);
    }

    public DownloadReceiver(Context context, Handler handler, ProgressDialog progressDialog) {
        super(handler);
        this.context = context;
        mProgressDialog = progressDialog;
    }

    @Override
    protected void onReceiveResult(int resultCode, Bundle resultData) {
        super.onReceiveResult(resultCode, resultData);
        if (!mProgressDialog.isShowing()) {
            mProgressDialog.show();
        }
        if (resultCode == DownloadService.UPDATE_PROGRESS) {
            int progress = resultData.getInt(DownloadService.FLAG_PROGRESS);
            mProgressDialog.setProgress(progress);
            if (progress == 100) {
                mProgressDialog.dismiss();
                Toast.makeText(context, "下载成功", Toast.LENGTH_SHORT).show();
            }
        } else if (resultCode == DownloadService.DOWNLOAD_PAUSE) {
            Toast.makeText(context, "下载失败", Toast.LENGTH_SHORT).show();
            mProgressDialog.dismiss();
        }
    }
}
