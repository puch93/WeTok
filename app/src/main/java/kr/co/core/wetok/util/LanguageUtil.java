package kr.co.core.wetok.util;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.util.Log;

import java.util.Locale;

import kr.co.core.wetok.preference.SystemPref;

/**
 * 사용법
 * LanguageUtil languageUtil = new LanguageUtil(act);
 * languageUtil.setSystemLanguage(new Locale(LanguageUtil.COUNTRY_CHINA, LanguageUtil.CN));
 * */

public class LanguageUtil {
    public static final String CN = "CN"; // 간체
    public static final String TW = "TW"; // 번체
    public static final String COUNTRY_CHINA = "zh"; // 중국어
    public static final String COUNTRY_KOREA = "ko"; // 한국어

    private Activity act;
    private Context ctx;


    public LanguageUtil(Activity act) {
        this.act = act;
        this.ctx = act.getApplicationContext();
    }


    /* 앱내 설정과 관계없이, 현재 스마트폰의 언어 정보 가져옴. ex) '한국어'로 설정되있으면 -> 'ko' 반환됨*/
    public String getSystemLanguage() {
        Locale locale = ctx.getResources().getConfiguration().locale;
        String language = locale.getLanguage();
        return language;
    }

    /* 앱 내에서만 언어변경 ( 시스템 언어변경 X ) */
    public void setSystemLanguage(Locale locale) {
        SystemPref.setLanguage(ctx, locale.toString());

        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;

        act.getBaseContext().getResources().updateConfiguration(config,
                act.getBaseContext().getResources().getDisplayMetrics());
    }
}
