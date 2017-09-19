package com.thinkive.bank.versionupgradedemo.bean;

/**
 * @author: sq
 * @date: 2017/9/14
 * @corporation: 深圳市思迪信息技术股份有限公司
 * @description: 版本更新的实体【与服务器的字段相匹配】
 */
public class UpdateAppInfo {
    public UpdateInfo data; // 更新信息
    public Integer error_code; // 错误代码
    public String error_msg; // 错误信息

    public static class UpdateInfo {
        // app名字
        public String appName;
        //版本号
        public String versionCode;
        //版本名称
        public String versionName;
        //是否强制升级（0：否，1：是）
        public String lastForce;
        //app最新版本地址
        public String updateUrl;
        //升级信息
        public String upgradeInfo;

        public String getAppName() {
            return appName;
        }

        public void setAppName(String appName) {
            this.appName = appName;
        }

        public String getVersionCode() {
            return versionCode;
        }

        public void setVersionCode(String versionCode) {
            this.versionCode = versionCode;
        }

        public String getVersionName() {
            return versionName;
        }

        public void setVersionName(String versionName) {
            this.versionName = versionName;
        }

        public String getLastForce() {
            return lastForce;
        }

        public void setLastForce(String lastForce) {
            this.lastForce = lastForce;
        }

        public String getUpdateUrl() {
            return updateUrl;
        }

        public void setUpdateUrl(String updateUrl) {
            this.updateUrl = updateUrl;
        }

        public String getUpgradeInfo() {
            return upgradeInfo;
        }

        public void setUpgradeInfo(String upgradeInfo) {
            this.upgradeInfo = upgradeInfo;
        }
    }

    public UpdateInfo getData() {
        return data;
    }

    public void setData(UpdateInfo data) {
        this.data = data;
    }

    public Integer getError_code() {
        return error_code;
    }

    public void setError_code(Integer error_code) {
        this.error_code = error_code;
    }

    public String getError_msg() {
        return error_msg;
    }

    public void setError_msg(String error_msg) {
        this.error_msg = error_msg;
    }
}
