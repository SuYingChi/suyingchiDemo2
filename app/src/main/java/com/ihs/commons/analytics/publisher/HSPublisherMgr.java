package com.ihs.commons.analytics.publisher;

import com.ihs.commons.config.HSConfig;
import com.ihs.inputmethod.api.HSUIApplication;

import org.json.JSONObject;

/**
 * Created by Arthur on 2018/1/31.
 */

public class HSPublisherMgr {
    public static HSPublisherMgr.PublisherData getPublisherData(HSUIApplication hsuiApplication) {
        return new PublisherData();
    }

    public static class PublisherData {
        private HSPublisherMgr.PublisherData.InstallMode installMode;
        private String mediaSource;
        private String campaign;
        private String campaignID;
        private boolean isDefault;
        private String uaAge;
        private HSPublisherMgr.PublisherData.Gender uaGender;
        private JSONObject appsflyerData;
        private String agency;
        private String adset;
        private String adsetId;
        private String adId;

        public PublisherData() {
            this.installMode = HSPublisherMgr.PublisherData.InstallMode.ORGANIC;
            this.mediaSource = "Others";
            this.campaignID = "";
            this.campaign = "";
            this.uaAge = "unknown";
            this.uaGender = HSPublisherMgr.PublisherData.Gender.UNKNOWN;
            this.agency = "";
            this.adset = "";
            this.adsetId = "";
            this.adId = "";
            this.setDefault(true);
        }

        public boolean isDefault() {
            return this.isDefault;
        }

        void setDefault(boolean aDefault) {
            this.isDefault = aDefault;
        }

        public String getCampaign() {
            return this.campaign;
        }

        void setCampaign(String campaign) {
            this.campaign = campaign;
        }

        public String getCampaignID() {
            return this.campaignID;
        }

        void setCampaignID(String campaignID) {
            this.campaignID = campaignID;
        }

        public String getAdset() {
            return this.adset;
        }

        void setAdset(String adset) {
            this.adset = adset;
        }

        public String getAdsetId() {
            return this.adsetId;
        }

        void setAdsetId(String adsetId) {
            this.adsetId = adsetId;
        }

        public String getAdId() {
            return this.adId;
        }

        void setAdId(String adId) {
            this.adId = adId;
        }

        void setAgency(String agency) {
            this.agency = agency;
        }

        public HSPublisherMgr.PublisherData.InstallMode getInstallMode() {
            return this.installMode;
        }

        void setInstallMode(HSPublisherMgr.PublisherData.InstallMode installMode) {
            this.installMode = installMode;
        }

        public String getMediaSource() {
            return this.mediaSource;
        }

        void setMediaSource(String mediaSource) {
            this.mediaSource = mediaSource;
        }

        public String getDownloadCHannel() {
            return HSConfig.optString("GP", new String[]{"libCommons", "Market", "3rdChannel"});
        }

        void setAppsflyerData(JSONObject appsflyerData) {
            this.appsflyerData = appsflyerData;
        }

        public JSONObject getAppsflyerData() {
            return this.appsflyerData;
        }

        public String getUaAge() {
            return this.uaAge;
        }

        public String getAgency() {
            return this.agency;
        }

        void setUaAge(String uaAge) {
            this.uaAge = uaAge;
        }

        public HSPublisherMgr.PublisherData.Gender getUaGender() {
            return this.uaGender;
        }

        void setUaGender(HSPublisherMgr.PublisherData.Gender uaGender) {
            this.uaGender = uaGender;
        }

        public static enum Gender {
            UNKNOWN,
            MALE,
            FEMALE;

            private Gender() {
            }
        }

        public static enum InstallMode {
            UNKNOWN,
            ORGANIC,
            NON_ORGANIC;

            private InstallMode() {
            }
        }
    }
}
