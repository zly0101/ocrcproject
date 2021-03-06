package com.example.shinelon.ocrcamera.helper;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;
import com.baidu.ocr.sdk.OCR;
import com.baidu.ocr.sdk.OnResultListener;
import com.baidu.ocr.sdk.exception.OCRError;
import com.baidu.ocr.sdk.model.AccessToken;
import com.squareup.leakcanary.LeakCanary;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Shinelon on 2017/9/12.
 */

public class CheckApplication extends Application {
   // public static Engine engine = null;
   // public static RecognitionManager manager = null;
    public static boolean isNotNativeRecognize = true;
    private Handler handler = null;
    public static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        LeakCanary.install(this);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        isNotNativeRecognize = preferences.getBoolean("connect",true);
        init();
        context = getApplicationContext();
    }

    public void init(){
        initAccessTokenWithAkSk();
        /**
        final DataSource assetDataSrouce = new AssetDataSource( this.getAssets() );
        final List<DataSource> dataSources = new ArrayList<>();
        dataSources.add( assetDataSrouce );

        Engine.loadNativeLibrary();
        try {
            engine = Engine.createInstance( dataSources, new FileLicense( assetDataSrouce,
                            "license", "com.example.shinelon.ocrcamera" ),
                    new Engine.DataFilesExtensions( ".mp3",
                            ".mp3",
                            ".mp3"));
            RecognitionConfiguration configuration = new RecognitionConfiguration();
            RecognitionLanguage language_zn = RecognitionLanguage.ChineseSimplified;
            Set<RecognitionLanguage> languages = new HashSet<>();
            languages.add(RecognitionLanguage.English);
            if(engine.isLanguageAvailableForOcr(language_zn)){
                languages.add(language_zn);
            }
            RecognitionLanguage language_zntr = RecognitionLanguage.ChineseTraditional;
            if(engine.isLanguageAvailableForOcr(language_zntr)){
                languages.add(language_zntr);
            }
            configuration.setRecognitionLanguages(languages);
            manager = engine.getRecognitionManager(configuration);

        } catch(Exception e ) {
            e.printStackTrace();
            Log.w("License",e.getMessage());
        }
         */
    }
    /**
    public static RecognitionManager getManager(){
        Log.e("manager",isNotNativeRecognize+" ");
        return manager;
    }*/

    private void initAccessTokenWithAkSk() {
        handler = new Handler();
        OCR.getInstance().initAccessTokenWithAkSk(new OnResultListener<AccessToken>() {
            @Override
            public void onResult(AccessToken result) {
                String token = result.getAccessToken();
                Log.e("百度token",token);
            }
            @Override
            public void onError(OCRError error) {
                error.printStackTrace();
                final String message = error.getMessage();
                handler.post(()->Toast.makeText(getApplicationContext(),"AK，SK方式获取token失败 " + message, Toast.LENGTH_SHORT).show());
            }
        }, getApplicationContext(),"qsv0ZAOxsT7cy5eIE5t92IUN","Kl3cv5v2FaHSaS8gUZu1a16Ny9LzTMXo");
    }

    public static Context getCotex(){
        return context;
    }

    public static boolean isNetWorkAvailable(){
          ConnectivityManager manager = (ConnectivityManager) getCotex().getSystemService(Context.CONNECTIVITY_SERVICE);
          if(manager==null){
              return false;
          }
        NetworkInfo info = manager.getActiveNetworkInfo();
        if(info==null || !info.isAvailable()){
            return false;
        }
        return true;
    }
}

