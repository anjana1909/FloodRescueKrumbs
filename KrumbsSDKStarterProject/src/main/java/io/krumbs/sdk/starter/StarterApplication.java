/*
 * Copyright (c) 2016 Krumbs Inc.
 * All rights reserved.
 *
 */
package io.krumbs.sdk.starter;

import android.app.Application;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.util.Log;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import io.krumbs.sdk.KIntentPanelConfiguration;
import io.krumbs.sdk.KrumbsIntentTheme;
import io.krumbs.sdk.KrumbsSDK;
import io.krumbs.sdk.KrumbsUser;
import io.krumbs.sdk.data.model.Media;
import io.krumbs.sdk.krumbscapture.KMediaUploadListener;


public class StarterApplication extends Application {
    public static final String KRUMBS_SDK_APPLICATION_ID = "io.krumbs.sdk.APPLICATION_ID";
    public static final String KRUMBS_SDK_CLIENT_KEY = "io.krumbs.sdk.CLIENT_KEY";
    public static final String SDK_STARTER_PROJECT_USER_FN = "JohnQ";
    public static final String SDK_STARTER_PROJECT_USER_SN = "Public";

    @Override
    public void onCreate() {
        super.onCreate();

        String appID = getMetadata(getApplicationContext(), KRUMBS_SDK_APPLICATION_ID);
        String clientKey = getMetadata(getApplicationContext(), KRUMBS_SDK_CLIENT_KEY);
        if (appID != null && clientKey != null) {
// SDK usage step 1 - initialize the SDK with your application id and client key
            KrumbsSDK.initialize(getApplicationContext(), appID, clientKey);

// Implement the interface KMediaUploadListener.
// After a Capture completes, the media (photo and audio) is uploaded to the cloud
// KMediaUploadListener will be used to listen for various state of media upload from the SDK.
            KMediaUploadListener kMediaUploadListener = new KMediaUploadListener() {
                // onMediaUpload listens to various status of media upload to the cloud.
                @Override
                public void onMediaUpload(String id, KMediaUploadListener.MediaUploadStatus mediaUploadStatus,
                                          Media.MediaType mediaType, URL mediaUrl) {
                    if (mediaUploadStatus != null) {
                        Log.i("KRUMBS-BROADCAST-RECV", mediaUploadStatus.toString());
                        if (mediaUploadStatus == KMediaUploadListener.MediaUploadStatus.UPLOAD_SUCCESS) {
                            if (mediaType != null && mediaUrl != null) {
                                Log.i("KRUMBS-BROADCAST-RECV", mediaType + ": " + id + ", " + mediaUrl);
                            }
                        }
                    }
                }
            };
            // pass the KMediaUploadListener object to the sdk
            KrumbsSDK.setKMediaUploadListener(this, kMediaUploadListener);

            try {

// SDK usage step 2 - register your customized Intent Panel with the SDK

                // Register the Intent Panel model
                // the emoji image assets will be looked up by name when the KCapture camera is started
                // Make sure to include the images in your resource directory before starting the KCapture
                // Use the 'asset-generator' tool to build your image resources from intent-categories.json
//                String assetPath = "IntentResourcesExample";
//                singlePanelSetup(assetPath);
                multiPanelSetup();

// SDK usage step 4 (optional) - register users so you can associate their ID (email) with created content with Cloud
// API
                // Register user information (if your app requires login)
                // to improve security on the mediaJSON created.
                String userEmail = DeviceUtils.getPrimaryUserID(getApplicationContext());
                KrumbsSDK.registerUser(new KrumbsUser.KrumbsUserBuilder()
                        .email(userEmail)
                        .firstName(SDK_STARTER_PROJECT_USER_FN)
                        .lastName(SDK_STARTER_PROJECT_USER_SN).build());


            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static void singlePanelSetup(String assetDir) throws IOException {
        KrumbsSDK.registerIntentCategories(assetDir);
    }

    private static void multiPanelSetup() throws IOException {
        // register the multiple themes and their respective intent categories
        List<KrumbsIntentTheme> krumbsThemes = new ArrayList<>();
        String smartCitiesThemeName = "SmartCities";
        String smartCitiesResourceAssetDir = "SmartCities";
        krumbsThemes.add(new KrumbsTheme(smartCitiesThemeName, smartCitiesResourceAssetDir));

        /*String agriTechThemeName = "AgriTech";
        String agriTechResourceDir = "AgriTech";
        krumbsThemes.add(new KrumbsTheme(agriTechThemeName, agriTechResourceDir));

        String housingThemeName = "MyHousing";
        String housingResourceAssetDir = "Housing";
        krumbsThemes.add(new KrumbsTheme(housingThemeName, housingResourceAssetDir));

        String cleanCityThemeName = "CleanCity";
        String cleanCityResourceDir = "CleanCity";
        krumbsThemes.add(new KrumbsTheme(cleanCityThemeName, cleanCityResourceDir));

        String personalThemeName = "Personal";
        String personalResourceDir = "IntentResourcesExample";
        krumbsThemes.add(new KrumbsTheme(personalThemeName, personalResourceDir));
*/
        KrumbsSDK.registerIntentCategories(krumbsThemes);

        // SDK usage step 3 (optional) - add your Intent Panel view customizations
        // configure the defualt intent panel style so that each theme has different styles
        KIntentPanelConfiguration defaults = KrumbsSDK.getIntentPanelViewConfigurationDefaults();
        KIntentPanelConfiguration.IntentPanelCategoryTextStyle categoryTextStyle = defaults.getIntentPanelCategoryTextStyle();
        categoryTextStyle.setTextColor(Color.BLACK);
        KIntentPanelConfiguration.IntentPanelEmojiTextStyle emojiTextStyle = defaults.getIntentPanelEmojiTextStyle();
        emojiTextStyle.setTextColor(Color.BLACK);
        KIntentPanelConfiguration housingThemeStyle = new KIntentPanelConfiguration.KIntentPanelConfigurationBuilder()
                .intentPanelBarColor(1, (int) (255 * 0.980), (int) (255 * 0.882), (int) (255 * 0.208))
                .intentPanelTextStyle(categoryTextStyle)
                .intentEmojiTextStyle(emojiTextStyle)
                .build();
        //KrumbsSDK.setIntentPanelConfiguration(housingThemeName, housingThemeStyle);

        /*categoryTextStyle = defaults.getIntentPanelCategoryTextStyle();
        categoryTextStyle.setTextColor(Color.YELLOW);
        emojiTextStyle = defaults.getIntentPanelEmojiTextStyle();
        emojiTextStyle.setTextColor(Color.YELLOW);
        KIntentPanelConfiguration agriTechThemeStyle = new KIntentPanelConfiguration.KIntentPanelConfigurationBuilder()
                .intentPanelBarColor("#9e3030")
                .intentPanelTextStyle(categoryTextStyle)
                .intentEmojiTextStyle(emojiTextStyle)
                .build();
        KrumbsSDK.setIntentPanelConfiguration(agriTechThemeName, agriTechThemeStyle);

        categoryTextStyle = defaults.getIntentPanelCategoryTextStyle();
        categoryTextStyle.setTextColor(Color.WHITE);
        emojiTextStyle = defaults.getIntentPanelEmojiTextStyle();
        emojiTextStyle.setTextColor(Color.WHITE);
        KIntentPanelConfiguration cleanCityThemeStyle = new KIntentPanelConfiguration.KIntentPanelConfigurationBuilder()
                .intentPanelBarColor("#9324c6")
                .intentPanelTextStyle(categoryTextStyle)
                .intentEmojiTextStyle(emojiTextStyle)
                .build();
        KrumbsSDK.setIntentPanelConfiguration(cleanCityThemeName, cleanCityThemeStyle);
*/
        categoryTextStyle = defaults.getIntentPanelCategoryTextStyle();
        categoryTextStyle.setTextColor(Color.YELLOW);
        emojiTextStyle = defaults.getIntentPanelEmojiTextStyle();
        emojiTextStyle.setTextColor(Color.YELLOW);
        KIntentPanelConfiguration smartCitiesThemeStyle = new KIntentPanelConfiguration.KIntentPanelConfigurationBuilder()
                .intentPanelBarColor("#029EE1")
                .intentPanelTextStyle(categoryTextStyle)
                .intentEmojiTextStyle(emojiTextStyle)
                .build();
        KrumbsSDK.setIntentPanelConfiguration(smartCitiesThemeName, smartCitiesThemeStyle);
    }

    private static class KrumbsTheme implements KrumbsIntentTheme {
        private String themeName;
        private String assetDirectoryName;
        KrumbsTheme(String themeName, String assetDirName) {
            this.themeName = themeName;
            this.assetDirectoryName = assetDirName;
        }
        @Override
        public String themeName() {
            return themeName;
        }

        @Override
        public String assetDirectoryName() {
            return assetDirectoryName;
        }
    }

    public String getMetadata(Context context, String name) {
        try {
            ApplicationInfo appInfo = context.getPackageManager().getApplicationInfo(
                    context.getPackageName(), PackageManager.GET_META_DATA);
            if (appInfo.metaData != null) {
                return appInfo.metaData.getString(name);
            }
        } catch (PackageManager.NameNotFoundException e) {
// if we can’t find it in the manifest, just return null
        }
        return null;
    }
}
