package kr.co.core.wetok.util;

import android.app.Application;
import android.util.Log;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.exceptions.RealmMigrationNeededException;

public class CustomApplication extends Application {
    private Realm realm = null;
    private RealmConfiguration config;
    private String friend_count;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e(StringUtil.TAG, "onCreate from application");

        initRealm();
    }

    public void setFriend_count(String count) {
        this.friend_count = count;
    }

    public String getFriend_count() {
        return this.friend_count;
    }

    public Realm getRealmObject() {
        if(null != realm) {
            return realm;
        } else {
            return reInitRealm();
        }
    }

    public Realm reInitRealm() {
        Log.e(StringUtil.TAG, "reInitRealm");
        // initialize Realm and create Realm object
        Realm.init(getApplicationContext());

        config = new RealmConfiguration.Builder()
                .name("wetok.realm")
                .build();

        try {
            realm = Realm.getInstance(config);
        } catch (RealmMigrationNeededException e) {

            Realm.deleteRealm(config);
            realm = Realm.getInstance(config);
        }

        return realm;
    }

    public void initRealm() {
        Log.e(StringUtil.TAG, "initRealm");
        // initialize Realm and create Realm object
        Realm.init(getApplicationContext());

        config = new RealmConfiguration.Builder()
                .name("wetok.realm")
                .build();

        try {
            realm = Realm.getInstance(config);
        } catch (RealmMigrationNeededException e) {

            Realm.deleteRealm(config);
            realm = Realm.getInstance(config);
        }
    }
}
