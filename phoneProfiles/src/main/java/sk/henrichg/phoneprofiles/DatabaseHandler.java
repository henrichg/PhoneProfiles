package sk.henrichg.phoneprofiles;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

class DatabaseHandler extends SQLiteOpenHelper {

    // All Static variables

    // singleton fields
    private static DatabaseHandler instance;
    private static SQLiteDatabase writableDb;

    Context context;
    
    // Database Version
    private static final int DATABASE_VERSION = 1320;

    // Database Name
    private static final String DATABASE_NAME = "phoneProfilesManager";

    // Table names
    private static final String TABLE_PROFILES = "profiles";
    private static final String TABLE_SHORTCUTS = "shortcuts";

    // import/export
    private final String EXPORT_DBFILENAME = DATABASE_NAME + ".backup";


    // Profiles Table Columns names
    private static final String KEY_ID = "id";
    private static final String KEY_NAME = "name";
    private static final String KEY_ICON = "icon";
    private static final String KEY_CHECKED = "checked";
    private static final String KEY_PORDER = "porder";
    private static final String KEY_VOLUME_RINGER_MODE = "volumeRingerMode";
    private static final String KEY_VOLUME_ZEN_MODE = "volumeZenMode";
    private static final String KEY_VOLUME_RINGTONE = "volumeRingtone";
    private static final String KEY_VOLUME_NOTIFICATION = "volumeNotification";
    private static final String KEY_VOLUME_MEDIA = "volumeMedia";
    private static final String KEY_VOLUME_ALARM = "volumeAlarm";
    private static final String KEY_VOLUME_SYSTEM = "volumeSystem";
    private static final String KEY_VOLUME_VOICE = "volumeVoice";
    private static final String KEY_SOUND_RINGTONE_CHANGE = "soundRingtoneChange";
    private static final String KEY_SOUND_RINGTONE = "soundRingtone";
    private static final String KEY_SOUND_NOTIFICATION_CHANGE = "soundNotificationChange";
    private static final String KEY_SOUND_NOTIFICATION = "soundNotification";
    private static final String KEY_SOUND_ALARM_CHANGE = "soundAlarmChange";
    private static final String KEY_SOUND_ALARM = "soundAlarm";
    private static final String KEY_DEVICE_AIRPLANE_MODE = "deviceAirplaneMode";
    private static final String KEY_DEVICE_WIFI = "deviceWiFi";
    private static final String KEY_DEVICE_BLUETOOTH = "deviceBluetooth";
    private static final String KEY_DEVICE_SCREEN_TIMEOUT = "deviceScreenTimeout";
    private static final String KEY_DEVICE_BRIGHTNESS = "deviceBrightness";
    private static final String KEY_DEVICE_WALLPAPER_CHANGE = "deviceWallpaperChange";
    private static final String KEY_DEVICE_WALLPAPER = "deviceWallpaper";
    private static final String KEY_DEVICE_MOBILE_DATA = "deviceMobileData";
    private static final String KEY_DEVICE_MOBILE_DATA_PREFS = "deviceMobileDataPrefs";
    private static final String KEY_DEVICE_GPS = "deviceGPS";
    private static final String KEY_DEVICE_RUN_APPLICATION_CHANGE = "deviceRunApplicationChange";
    private static final String KEY_DEVICE_RUN_APPLICATION_PACKAGE_NAME = "deviceRunApplicationPackageName";
    private static final String KEY_DEVICE_AUTOSYNC = "deviceAutosync";
    private static final String KEY_DEVICE_AUTOROTATE = "deviceAutoRotate";
    private static final String KEY_DEVICE_LOCATION_SERVICE_PREFS = "deviceLocationServicePrefs";
    private static final String KEY_VOLUME_SPEAKER_PHONE = "volumeSpeakerPhone";
    private static final String KEY_DEVICE_NFC = "deviceNFC";
    private static final String KEY_DURATION = "duration";
    private static final String KEY_AFTER_DURATION_DO = "afterDurationDo";
    private static final String KEY_DEVICE_KEYGUARD = "deviceKeyguard";
    private static final String KEY_VIBRATE_ON_TOUCH = "vibrateOnTouch";
    private static final String KEY_DEVICE_WIFI_AP = "deviceWiFiAP";
    private static final String KEY_DEVICE_POWER_SAVE_MODE = "devicePowerSaveMode";
    private static final String KEY_SHOW_DURATION_BUTTON = "showDurationButton";
    private static final String KEY_ASK_FOR_DURATION = "askForDuration";
    private static final String KEY_DEVICE_NETWORK_TYPE = "deviceNetworkType";
    private static final String KEY_NOTIFICATION_LED = "notificationLed";
    private static final String KEY_VIBRATE_WHEN_RINGING = "vibrateWhenRinging";
    private static final String KEY_DEVICE_WALLPAPER_FOR = "deviceWallpaperFor";
    private static final String KEY_HIDE_STATUS_BAR_ICON = "hideStatusBarIcon";
    private static final String KEY_LOCK_DEVICE = "lockDevice";
    private static final String KEY_DEVICE_CONNECT_TO_SSID = "deviceConnectToSSID";

    // Shortcuts Colums names
    private static final String KEY_S_ID = "_id";  // for CursorAdapter must by this name
    private static final String KEY_S_INTENT = "intent";
    private static final String KEY_S_NAME = "name";

    /**
     * Constructor takes and keeps a reference of the passed context in order to
     * access to the application assets and resources.
     *
     * @param context
     *            the application context
     */	
    private DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    /**
     * Get default instance of the class to keep it a singleton
     *
     * @param context
     *            the application context
     */
    public static synchronized DatabaseHandler getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHandler(context);
        }
        return instance;
    }
    
    /**
     * Returns a writable database instance in order not to open and close many
     * SQLiteDatabase objects simultaneously
     *
     * @return a writable instance to SQLiteDatabase
     */
    private SQLiteDatabase getMyWritableDatabase() {
        if ((writableDb == null) || (!writableDb.isOpen())) {
            writableDb = this.getWritableDatabase();
        }
 
        return writableDb;
    }
 
    @Override
    public synchronized void close() {
        super.close();
        if (writableDb != null) {
            writableDb.close();
            writableDb = null;
        }
    }
    
    // be sure to call this method by: DatabaseHandler.getInstance().closeConnecion() 
    // when application is closed by somemeans most likely
    // onDestroy method of application
    synchronized void closeConnecion() {
        if (instance != null)
        {
            instance.close();
            instance = null;
        }
    }    
    
    @Override
    public void onCreate(SQLiteDatabase db) {
        final String CREATE_PROFILES_TABLE = "CREATE TABLE " + TABLE_PROFILES + "("
                + KEY_ID + " INTEGER PRIMARY KEY,"
                + KEY_NAME + " TEXT,"
                + KEY_ICON + " TEXT,"
                + KEY_CHECKED + " INTEGER,"
                + KEY_PORDER + " INTEGER,"
                + KEY_VOLUME_RINGER_MODE + " INTEGER,"
                + KEY_VOLUME_RINGTONE + " TEXT,"
                + KEY_VOLUME_NOTIFICATION + " TEXT,"
                + KEY_VOLUME_MEDIA + " TEXT,"
                + KEY_VOLUME_ALARM + " TEXT,"
                + KEY_VOLUME_SYSTEM + " TEXT,"
                + KEY_VOLUME_VOICE + " TEXT,"
                + KEY_SOUND_RINGTONE_CHANGE + " INTEGER,"
                + KEY_SOUND_RINGTONE + " TEXT,"
                + KEY_SOUND_NOTIFICATION_CHANGE + " INTEGER,"
                + KEY_SOUND_NOTIFICATION + " TEXT,"
                + KEY_SOUND_ALARM_CHANGE + " INTEGER,"
                + KEY_SOUND_ALARM + " TEXT,"
                + KEY_DEVICE_AIRPLANE_MODE + " INTEGER,"
                + KEY_DEVICE_WIFI + " INTEGER,"
                + KEY_DEVICE_BLUETOOTH + " INTEGER,"
                + KEY_DEVICE_SCREEN_TIMEOUT + " INTEGER,"
                + KEY_DEVICE_BRIGHTNESS + " TEXT,"
                + KEY_DEVICE_WALLPAPER_CHANGE + " INTEGER,"
                + KEY_DEVICE_WALLPAPER + " TEXT,"
                + KEY_DEVICE_MOBILE_DATA + " INTEGER,"
                + KEY_DEVICE_MOBILE_DATA_PREFS + " INTEGER,"
                + KEY_DEVICE_GPS + " INTEGER,"
                + KEY_DEVICE_RUN_APPLICATION_CHANGE + " INTEGER,"
                + KEY_DEVICE_RUN_APPLICATION_PACKAGE_NAME + " TEXT,"
                + KEY_DEVICE_AUTOSYNC + " INTEGER,"
                + KEY_DEVICE_AUTOROTATE + " INTEGER,"
                + KEY_DEVICE_LOCATION_SERVICE_PREFS + " INTEGER,"
                + KEY_VOLUME_SPEAKER_PHONE + " INTEGER,"
                + KEY_DEVICE_NFC + " INTEGER,"
                + KEY_DURATION + " INTEGER,"
                + KEY_AFTER_DURATION_DO + " INTEGER,"
                + KEY_VOLUME_ZEN_MODE + " INTEGER,"
                + KEY_DEVICE_KEYGUARD + " INTEGER,"
                + KEY_VIBRATE_ON_TOUCH + " INTEGER,"
                + KEY_DEVICE_WIFI_AP + " INTEGER,"
                + KEY_DEVICE_POWER_SAVE_MODE + " INTEGER,"
                + KEY_SHOW_DURATION_BUTTON + " INTEGER,"
                + KEY_ASK_FOR_DURATION + " INTEGER,"
                + KEY_DEVICE_NETWORK_TYPE + " INTEGER,"
                + KEY_NOTIFICATION_LED + " INTEGER,"
                + KEY_VIBRATE_WHEN_RINGING + " INTEGER,"
                + KEY_DEVICE_WALLPAPER_FOR + " INTEGER,"
                + KEY_HIDE_STATUS_BAR_ICON + " INTEGER,"
                + KEY_LOCK_DEVICE + " INTEGER,"
                + KEY_DEVICE_CONNECT_TO_SSID + " TEXT"
                + ")";
        db.execSQL(CREATE_PROFILES_TABLE);

        db.execSQL("CREATE INDEX IDX_PORDER ON " + TABLE_PROFILES + " (" + KEY_PORDER + ")");

        final String CREATE_SHORTCUTS_TABLE = "CREATE TABLE " + TABLE_SHORTCUTS + "("
                + KEY_S_ID + " INTEGER PRIMARY KEY,"
                + KEY_S_INTENT + " TEXT,"
                + KEY_S_NAME + " TEXT"
                + ")";
        db.execSQL(CREATE_SHORTCUTS_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        /*
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PROFILES);

        // Create tables again
        onCreate(db);
        */

        if (oldVersion < 16)
        {
            // pridame nove stlpce
            db.execSQL("ALTER TABLE " + TABLE_PROFILES + " ADD COLUMN " + KEY_DEVICE_WALLPAPER_CHANGE + " INTEGER");
            db.execSQL("ALTER TABLE " + TABLE_PROFILES + " ADD COLUMN " + KEY_DEVICE_WALLPAPER + " TEXT");

            // updatneme zaznamy
            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_DEVICE_WALLPAPER_CHANGE + "=0");
            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_DEVICE_WALLPAPER + "='-'");
        }

        if (oldVersion < 18)
        {
            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_ICON + "=replace(" + KEY_ICON + ",':','|')");
            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_VOLUME_RINGTONE + "=replace(" + KEY_VOLUME_RINGTONE + ",':','|')");
            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_VOLUME_NOTIFICATION + "=replace(" + KEY_VOLUME_NOTIFICATION + ",':','|')");
            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_VOLUME_MEDIA + "=replace(" + KEY_VOLUME_MEDIA + ",':','|')");
            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_VOLUME_ALARM + "=replace(" + KEY_VOLUME_ALARM + ",':','|')");
            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_VOLUME_SYSTEM + "=replace(" + KEY_VOLUME_SYSTEM + ",':','|')");
            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_VOLUME_VOICE + "=replace(" + KEY_VOLUME_VOICE + ",':','|')");
            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_DEVICE_BRIGHTNESS + "=replace(" + KEY_DEVICE_BRIGHTNESS + ",':','|')");
            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_DEVICE_WALLPAPER + "=replace(" + KEY_DEVICE_WALLPAPER + ",':','|')");
        }

        if (oldVersion < 19)
        {
            // pridame nove stlpce
            db.execSQL("ALTER TABLE " + TABLE_PROFILES + " ADD COLUMN " + KEY_DEVICE_MOBILE_DATA + " INTEGER");

            // updatneme zaznamy
            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_DEVICE_MOBILE_DATA + "=0");
        }

        if (oldVersion < 20)
        {
            // pridame nove stlpce
            db.execSQL("ALTER TABLE " + TABLE_PROFILES + " ADD COLUMN " + KEY_DEVICE_MOBILE_DATA_PREFS + " INTEGER");

            // updatneme zaznamy
            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_DEVICE_MOBILE_DATA_PREFS + "=0");
        }

        if (oldVersion < 21)
        {
            // pridame nove stlpce
            db.execSQL("ALTER TABLE " + TABLE_PROFILES + " ADD COLUMN " + KEY_DEVICE_GPS + " INTEGER");

            // updatneme zaznamy
            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_DEVICE_GPS + "=0");
        }

        if (oldVersion < 22)
        {
            // pridame nove stlpce
            db.execSQL("ALTER TABLE " + TABLE_PROFILES + " ADD COLUMN " + KEY_DEVICE_RUN_APPLICATION_CHANGE + " INTEGER");
            db.execSQL("ALTER TABLE " + TABLE_PROFILES + " ADD COLUMN " + KEY_DEVICE_RUN_APPLICATION_PACKAGE_NAME + " TEXT");

            // updatneme zaznamy
            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_DEVICE_RUN_APPLICATION_CHANGE + "=0");
            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_DEVICE_RUN_APPLICATION_PACKAGE_NAME + "=\"-\"");
        }

        if (oldVersion < 23)
        {
            // index na PORDER
            db.execSQL("CREATE INDEX IDX_PORDER ON " + TABLE_PROFILES + " (" + KEY_PORDER + ")");
        }

        if (oldVersion < 24)
        {
            // pridame nove stlpce
            db.execSQL("ALTER TABLE " + TABLE_PROFILES + " ADD COLUMN " + KEY_DEVICE_AUTOSYNC + " INTEGER");

            // updatneme zaznamy
            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_DEVICE_AUTOSYNC + "=0");
        }

        if (oldVersion < 51)
        {
            // pridame nove stlpce
            db.execSQL("ALTER TABLE " + TABLE_PROFILES + " ADD COLUMN " + KEY_DEVICE_AUTOROTATE + " INTEGER");

            // updatneme zaznamy
            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_DEVICE_AUTOROTATE + "=0");
        }

        if (oldVersion < 52)
        {
            // updatneme zaznamy
            // autorotate off -> rotation 0
            // autorotate on -> autorotate
            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_DEVICE_AUTOROTATE + "=1 WHERE " + KEY_DEVICE_AUTOROTATE + "=1");
            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_DEVICE_AUTOROTATE + "=1 WHERE " + KEY_DEVICE_AUTOROTATE + "=3");
            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_DEVICE_AUTOROTATE + "=2 WHERE " + KEY_DEVICE_AUTOROTATE + "=2");
        }

        if (oldVersion < 1015)
        {
            // pridame nove stlpce
            db.execSQL("ALTER TABLE " + TABLE_PROFILES + " ADD COLUMN " + KEY_DEVICE_LOCATION_SERVICE_PREFS + " INTEGER");

            // updatneme zaznamy
            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_DEVICE_LOCATION_SERVICE_PREFS + "=0");
        }

        if (oldVersion < 1020)
        {
            // pridame nove stlpce
            db.execSQL("ALTER TABLE " + TABLE_PROFILES + " ADD COLUMN " + KEY_VOLUME_SPEAKER_PHONE + " INTEGER");

            // updatneme zaznamy
            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_VOLUME_SPEAKER_PHONE + "=0");
        }

        if (oldVersion < 1035)
        {
            // pridame nove stlpce
            db.execSQL("ALTER TABLE " + TABLE_PROFILES + " ADD COLUMN " + KEY_DEVICE_NFC + " INTEGER");

            // updatneme zaznamy
            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_DEVICE_NFC + "=0");
        }

        if (oldVersion < 1120)
        {
            // pridame nove stlpce
            db.execSQL("ALTER TABLE " + TABLE_PROFILES + " ADD COLUMN " + KEY_DURATION + " INTEGER");
            db.execSQL("ALTER TABLE " + TABLE_PROFILES + " ADD COLUMN " + KEY_AFTER_DURATION_DO + " INTEGER");

            // updatneme zaznamy
            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_DURATION + "=" + Profile.AFTERDURATIONDO_UNDOPROFILE);
            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_AFTER_DURATION_DO + "=" +  + Profile.AFTERDURATIONDO_UNDOPROFILE);
        }

        if (oldVersion < 1150)
        {
            // pridame nove stlpce
            db.execSQL("ALTER TABLE " + TABLE_PROFILES + " ADD COLUMN " + KEY_VOLUME_ZEN_MODE + " INTEGER");

            // updatneme zaznamy
            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_VOLUME_ZEN_MODE + "=0");
        }

        if (oldVersion < 1156)
        {
            if (android.os.Build.VERSION.SDK_INT >= 21) // for Android 5.0: adaptive brightness
            {
                // updatneme zaznamy
                final String selectQuery = "SELECT " + KEY_ID + "," +
                                                KEY_DEVICE_BRIGHTNESS +
                                            " FROM " + TABLE_PROFILES;

                Cursor cursor = db.rawQuery(selectQuery, null);

                // looping through all rows and adding to list
                if (cursor.moveToFirst()) {
                    do {
                        long id = Long.parseLong(cursor.getString(0));
                        String brightness = cursor.getString(1);

                        //value|noChange|automatic|defaultProfile
                        String[] splits = brightness.split("\\|");

                        if (splits[2].equals("1")) // automatic is set
                        {
                            // hm, found brightness values without default profile :-/
                            if (splits.length == 4)
                                brightness = Profile.BRIGHTNESS_ADAPTIVE_BRIGHTNESS_NOT_SET+"|"+splits[1]+"|"+splits[2]+"|"+splits[3];
                            else
                                brightness = Profile.BRIGHTNESS_ADAPTIVE_BRIGHTNESS_NOT_SET+"|"+splits[1]+"|"+splits[2]+"|0";

                            db.execSQL("UPDATE " + TABLE_PROFILES +
                                         " SET " + KEY_DEVICE_BRIGHTNESS + "=\"" + brightness +"\"" +
                                        "WHERE " + KEY_ID + "=" + id);
                        }

                    } while (cursor.moveToNext());
                }

                cursor.close();
            }
        }

        if (oldVersion < 1160)
        {
            // pridame nove stlpce
            db.execSQL("ALTER TABLE " + TABLE_PROFILES + " ADD COLUMN " + KEY_DEVICE_KEYGUARD + " INTEGER");

            // updatneme zaznamy
            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_DEVICE_KEYGUARD + "=0");
        }

        if (oldVersion < 1165)
        {
            // updatneme zaznamy
            final String selectQuery = "SELECT " + KEY_ID + "," +
                                            KEY_DEVICE_BRIGHTNESS +
                                        " FROM " + TABLE_PROFILES;

            Cursor cursor = db.rawQuery(selectQuery, null);

            // looping through all rows and adding to list
            if (cursor.moveToFirst()) {
                do {
                    long id = Long.parseLong(cursor.getString(0));
                    String brightness = cursor.getString(1);

                    //value|noChange|automatic|defaultProfile
                    String[] splits = brightness.split("\\|");

                    int perc = Integer.parseInt(splits[0]);
                    perc = (int)Profile.convertBrightnessToPercents(perc, 255, 1);

                    // hm, found brightness values without default profile :-/
                    if (splits.length == 4)
                        brightness = perc+"|"+splits[1]+"|"+splits[2]+"|"+splits[3];
                    else
                        brightness = perc+"|"+splits[1]+"|"+splits[2]+"|0";

                    db.execSQL("UPDATE " + TABLE_PROFILES +
                                 " SET " + KEY_DEVICE_BRIGHTNESS + "=\"" + brightness +"\"" +
                                "WHERE " + KEY_ID + "=" + id);

                } while (cursor.moveToNext());
            }

            cursor.close();
        }

        if (oldVersion < 1175)
        {
            if (android.os.Build.VERSION.SDK_INT < 21)
            {
                // updatneme zaznamy
                final String selectQuery = "SELECT " + KEY_ID + "," +
                                                KEY_DEVICE_BRIGHTNESS +
                                            " FROM " + TABLE_PROFILES;

                Cursor cursor = db.rawQuery(selectQuery, null);

                // looping through all rows and adding to list
                if (cursor.moveToFirst()) {
                    do {
                        long id = Long.parseLong(cursor.getString(0));
                        String brightness = cursor.getString(1);

                        //value|noChange|automatic|defaultProfile
                        String[] splits = brightness.split("\\|");

                        if (splits[2].equals("1")) // automatic is set
                        {
                            int perc = 50;

                            // hm, found brightness values without default profile :-/
                            if (splits.length == 4)
                                brightness = perc+"|"+splits[1]+"|"+splits[2]+"|"+splits[3];
                            else
                                brightness = perc+"|"+splits[1]+"|"+splits[2]+"|0";

                            db.execSQL("UPDATE " + TABLE_PROFILES +
                                         " SET " + KEY_DEVICE_BRIGHTNESS + "=\"" + brightness +"\"" +
                                        "WHERE " + KEY_ID + "=" + id);
                        }

                    } while (cursor.moveToNext());
                }

                cursor.close();
            }
        }

        if (oldVersion < 1180)
        {
            // pridame nove stlpce
            db.execSQL("ALTER TABLE " + TABLE_PROFILES + " ADD COLUMN " + KEY_VIBRATE_ON_TOUCH + " INTEGER");

            // updatneme zaznamy
            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_VIBRATE_ON_TOUCH + "=0");
        }

        if (oldVersion < 1190)
        {
            // pridame nove stlpce
            db.execSQL("ALTER TABLE " + TABLE_PROFILES + " ADD COLUMN " + KEY_DEVICE_WIFI_AP + " INTEGER");

            // updatneme zaznamy
            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_DEVICE_WIFI_AP + "=0");
        }

        if (oldVersion < 1200)
        {
            final String selectQuery = "SELECT " + KEY_ID + "," +
                    KEY_DURATION +
                    " FROM " + TABLE_PROFILES;

            Cursor cursor = db.rawQuery(selectQuery, null);

            if (cursor.moveToFirst()) {
                do {
                    long id = Long.parseLong(cursor.getString(0));
                    int delayStart = cursor.getInt(1) * 60;  // conversiont to seconds

                    db.execSQL("UPDATE " + TABLE_PROFILES +
                            " SET " + KEY_DURATION + "=" + delayStart + " " +
                            "WHERE " + KEY_ID + "=" + id);

                } while (cursor.moveToNext());
            }

            cursor.close();
        }

        if (oldVersion < 1210)
        {
            final String selectQuery = "SELECT " + KEY_ID + "," +
                    KEY_VOLUME_ZEN_MODE +
                    " FROM " + TABLE_PROFILES;

            Cursor cursor = db.rawQuery(selectQuery, null);

            if (cursor.moveToFirst()) {
                do {
                    long id = Long.parseLong(cursor.getString(0));
                    int zenMode = cursor.getInt(1);

                    if ((zenMode == 6) && (android.os.Build.VERSION.SDK_INT < 23)) // Alarms only zen mode is supported from Android 6.0
                        db.execSQL("UPDATE " + TABLE_PROFILES +
                                " SET " + KEY_VOLUME_ZEN_MODE + "=3" + " " +
                                "WHERE " + KEY_ID + "=" + id);

                } while (cursor.moveToNext());
            }

            cursor.close();
        }

        if (oldVersion < 1220)
        {
            // pridame nove stlpce
            db.execSQL("ALTER TABLE " + TABLE_PROFILES + " ADD COLUMN " + KEY_DEVICE_POWER_SAVE_MODE + " INTEGER");

            // updatneme zaznamy
            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_DEVICE_POWER_SAVE_MODE + "=0");
        }

        if (oldVersion < 1230)
        {
            // pridame nove stlpce
            db.execSQL("ALTER TABLE " + TABLE_PROFILES + " ADD COLUMN " + KEY_SHOW_DURATION_BUTTON + " INTEGER");

            // updatneme zaznamy
            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_SHOW_DURATION_BUTTON + "=0");
        }

        if (oldVersion < 1240)
        {
            // pridame nove stlpce
            db.execSQL("ALTER TABLE " + TABLE_PROFILES + " ADD COLUMN " + KEY_ASK_FOR_DURATION + " INTEGER");

            // updatneme zaznamy
            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_ASK_FOR_DURATION + "=0");
        }

        if (oldVersion < 1250)
        {
            // pridame nove stlpce
            db.execSQL("ALTER TABLE " + TABLE_PROFILES + " ADD COLUMN " + KEY_DEVICE_NETWORK_TYPE + " INTEGER");

            // updatneme zaznamy
            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_DEVICE_NETWORK_TYPE + "=0");
        }

        if (oldVersion < 1260)
        {
            // pridame nove stlpce
            db.execSQL("ALTER TABLE " + TABLE_PROFILES + " ADD COLUMN " + KEY_NOTIFICATION_LED + " INTEGER");

            // updatneme zaznamy
            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_NOTIFICATION_LED + "=0");
        }

        if (oldVersion < 1270)
        {
            // pridame nove stlpce
            db.execSQL("ALTER TABLE " + TABLE_PROFILES + " ADD COLUMN " + KEY_VIBRATE_WHEN_RINGING + " INTEGER");

            // updatneme zaznamy
            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_VIBRATE_WHEN_RINGING + "=0");
        }

        if (oldVersion < 1280) {
            final String CREATE_SHORTCUTS_TABLE = "CREATE TABLE " + TABLE_SHORTCUTS + "("
                    + KEY_S_ID + " INTEGER PRIMARY KEY,"
                    + KEY_S_INTENT + " TEXT,"
                    + KEY_S_NAME + " TEXT"
                    + ")";
            db.execSQL(CREATE_SHORTCUTS_TABLE);
        }

        if (oldVersion < 1290)
        {
            // pridame nove stlpce
            db.execSQL("ALTER TABLE " + TABLE_PROFILES + " ADD COLUMN " + KEY_DEVICE_WALLPAPER_FOR + " INTEGER");

            // updatneme zaznamy
            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_DEVICE_WALLPAPER_FOR + "=0");
        }

        if (oldVersion < 1300)
        {
            // pridame nove stlpce
            db.execSQL("ALTER TABLE " + TABLE_PROFILES + " ADD COLUMN " + KEY_HIDE_STATUS_BAR_ICON + " INTEGER");

            // updatneme zaznamy
            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_HIDE_STATUS_BAR_ICON + "=0");
        }

        if (oldVersion < 1310)
        {
            // pridame nove stlpce
            db.execSQL("ALTER TABLE " + TABLE_PROFILES + " ADD COLUMN " + KEY_LOCK_DEVICE + " INTEGER");

            // updatneme zaznamy
            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_LOCK_DEVICE + "=0");
        }

        if (oldVersion < 1320)
        {
            // pridame nove stlpce
            db.execSQL("ALTER TABLE " + TABLE_PROFILES + " ADD COLUMN " + KEY_DEVICE_CONNECT_TO_SSID + " TEXT");

            // updatneme zaznamy
            db.execSQL("UPDATE " + TABLE_PROFILES + " SET " + KEY_DEVICE_CONNECT_TO_SSID + "=\""+Profile.CONNECTTOSSID_JUSTANY+"\"");
        }

    }

    @Override
    public void onOpen (SQLiteDatabase db)
    {
        //Log.e("DatabaseHandler.onOpen", "version="+db.getVersion());
    }

    /**
     * All CRUD(Create, Read, Update, Delete) Operations
     */

    // Adding new profile
    void addProfile(Profile profile) {

        int porder = getMaxPOrder() + 1;

        //SQLiteDatabase db = this.getWritableDatabase();
        SQLiteDatabase db = getMyWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_NAME, profile._name); // Profile Name
        values.put(KEY_ICON, profile._icon); // Icon
        values.put(KEY_CHECKED, (profile._checked) ? 1 : 0); // Checked
        values.put(KEY_PORDER, porder); // POrder
        values.put(KEY_VOLUME_RINGER_MODE, profile._volumeRingerMode);
        values.put(KEY_VOLUME_ZEN_MODE, profile._volumeZenMode);
        values.put(KEY_VOLUME_RINGTONE, profile._volumeRingtone);
        values.put(KEY_VOLUME_NOTIFICATION, profile._volumeNotification);
        values.put(KEY_VOLUME_MEDIA, profile._volumeMedia);
        values.put(KEY_VOLUME_ALARM, profile._volumeAlarm);
        values.put(KEY_VOLUME_SYSTEM, profile._volumeSystem);
        values.put(KEY_VOLUME_VOICE, profile._volumeVoice);
        values.put(KEY_SOUND_RINGTONE_CHANGE, profile._soundRingtoneChange);
        values.put(KEY_SOUND_RINGTONE, profile._soundRingtone);
        values.put(KEY_SOUND_NOTIFICATION_CHANGE, profile._soundNotificationChange);
        values.put(KEY_SOUND_NOTIFICATION, profile._soundNotification);
        values.put(KEY_SOUND_ALARM_CHANGE, profile._soundAlarmChange);
        values.put(KEY_SOUND_ALARM, profile._soundAlarm);
        values.put(KEY_DEVICE_AIRPLANE_MODE, profile._deviceAirplaneMode);
        values.put(KEY_DEVICE_WIFI, profile._deviceWiFi);
        values.put(KEY_DEVICE_BLUETOOTH, profile._deviceBluetooth);
        values.put(KEY_DEVICE_SCREEN_TIMEOUT, profile._deviceScreenTimeout);
        values.put(KEY_DEVICE_BRIGHTNESS, profile._deviceBrightness);
        values.put(KEY_DEVICE_WALLPAPER_CHANGE, profile._deviceWallpaperChange);
        values.put(KEY_DEVICE_WALLPAPER, profile._deviceWallpaper);
        values.put(KEY_DEVICE_MOBILE_DATA, profile._deviceMobileData);
        values.put(KEY_DEVICE_MOBILE_DATA_PREFS, profile._deviceMobileDataPrefs);
        values.put(KEY_DEVICE_GPS, profile._deviceGPS);
        values.put(KEY_DEVICE_RUN_APPLICATION_CHANGE, profile._deviceRunApplicationChange);
        values.put(KEY_DEVICE_RUN_APPLICATION_PACKAGE_NAME, profile._deviceRunApplicationPackageName);
        values.put(KEY_DEVICE_AUTOSYNC, profile._deviceAutosync);
        values.put(KEY_DEVICE_AUTOROTATE, profile._deviceAutoRotate);
        values.put(KEY_DEVICE_LOCATION_SERVICE_PREFS, profile._deviceLocationServicePrefs);
        values.put(KEY_VOLUME_SPEAKER_PHONE, profile._volumeSpeakerPhone);
        values.put(KEY_DEVICE_NFC, profile._deviceNFC);
        values.put(KEY_DURATION, profile._duration);
        values.put(KEY_AFTER_DURATION_DO, profile._afterDurationDo);
        values.put(KEY_DEVICE_KEYGUARD, profile._deviceKeyguard);
        values.put(KEY_VIBRATE_ON_TOUCH, profile._vibrationOnTouch);
        values.put(KEY_DEVICE_WIFI_AP, profile._deviceWiFiAP);
        values.put(KEY_DEVICE_POWER_SAVE_MODE, profile._devicePowerSaveMode);
        values.put(KEY_SHOW_DURATION_BUTTON, 0);
        values.put(KEY_ASK_FOR_DURATION, (profile._askForDuration) ? 1 : 0);
        values.put(KEY_DEVICE_NETWORK_TYPE, profile._deviceNetworkType);
        values.put(KEY_NOTIFICATION_LED, profile._notificationLed);
        values.put(KEY_VIBRATE_WHEN_RINGING, profile._vibrateWhenRinging);
        values.put(KEY_DEVICE_WALLPAPER_FOR, profile._deviceWallpaperFor);
        values.put(KEY_HIDE_STATUS_BAR_ICON, (profile._hideStatusBarIcon) ? 1 : 0);
        values.put(KEY_LOCK_DEVICE, profile._lockDevice);
        values.put(KEY_DEVICE_CONNECT_TO_SSID, profile._deviceConnectToSSID);

        // Inserting Row
        profile._id = db.insert(TABLE_PROFILES, null, values);
        //db.close(); // Closing database connection

        //profile.setPOrder(porder);
    }

    // Getting single profile
    Profile getProfile(long profile_id) {
        //SQLiteDatabase db = this.getReadableDatabase();
        SQLiteDatabase db = getMyWritableDatabase();

        Cursor cursor = db.query(TABLE_PROFILES,
                new String[]{KEY_ID,
                        KEY_NAME,
                        KEY_ICON,
                        KEY_CHECKED,
                        KEY_PORDER,
                        KEY_VOLUME_RINGER_MODE,
                        KEY_VOLUME_RINGTONE,
                        KEY_VOLUME_NOTIFICATION,
                        KEY_VOLUME_MEDIA,
                        KEY_VOLUME_ALARM,
                        KEY_VOLUME_SYSTEM,
                        KEY_VOLUME_VOICE,
                        KEY_SOUND_RINGTONE_CHANGE,
                        KEY_SOUND_RINGTONE,
                        KEY_SOUND_NOTIFICATION_CHANGE,
                        KEY_SOUND_NOTIFICATION,
                        KEY_SOUND_ALARM_CHANGE,
                        KEY_SOUND_ALARM,
                        KEY_DEVICE_AIRPLANE_MODE,
                        KEY_DEVICE_WIFI,
                        KEY_DEVICE_BLUETOOTH,
                        KEY_DEVICE_SCREEN_TIMEOUT,
                        KEY_DEVICE_BRIGHTNESS,
                        KEY_DEVICE_WALLPAPER_CHANGE,
                        KEY_DEVICE_WALLPAPER,
                        KEY_DEVICE_MOBILE_DATA,
                        KEY_DEVICE_MOBILE_DATA_PREFS,
                        KEY_DEVICE_GPS,
                        KEY_DEVICE_RUN_APPLICATION_CHANGE,
                        KEY_DEVICE_RUN_APPLICATION_PACKAGE_NAME,
                        KEY_DEVICE_AUTOSYNC,
                        KEY_DEVICE_AUTOROTATE,
                        KEY_DEVICE_LOCATION_SERVICE_PREFS,
                        KEY_VOLUME_SPEAKER_PHONE,
                        KEY_DEVICE_NFC,
                        KEY_DURATION,
                        KEY_AFTER_DURATION_DO,
                        KEY_VOLUME_ZEN_MODE,
                        KEY_DEVICE_KEYGUARD,
                        KEY_VIBRATE_ON_TOUCH,
                        KEY_DEVICE_WIFI_AP,
                        KEY_DEVICE_POWER_SAVE_MODE,
                        KEY_SHOW_DURATION_BUTTON,
                        KEY_ASK_FOR_DURATION,
                        KEY_DEVICE_NETWORK_TYPE,
                        KEY_NOTIFICATION_LED,
                        KEY_VIBRATE_WHEN_RINGING,
                        KEY_DEVICE_WALLPAPER_FOR,
                        KEY_HIDE_STATUS_BAR_ICON,
                        KEY_LOCK_DEVICE,
                        KEY_DEVICE_CONNECT_TO_SSID
                },
                KEY_ID + "=?",
                new String[]{String.valueOf(profile_id)}, null, null, null, null);

        Profile profile = null;

        if (cursor != null)
        {
            cursor.moveToFirst();

            if (cursor.getCount() > 0)
            {
                profile = new Profile(Long.parseLong(cursor.getString(0)),
                                              cursor.getString(1),
                                              cursor.getString(2),
                                              Integer.parseInt(cursor.getString(3)) == 1,
                                              Integer.parseInt(cursor.getString(4)),
                                              Integer.parseInt(cursor.getString(5)),
                                              cursor.getString(6),
                                              cursor.getString(7),
                                              cursor.getString(8),
                                              cursor.getString(9),
                                              cursor.getString(10),
                                              cursor.getString(11),
                                              Integer.parseInt(cursor.getString(12)),
                                              cursor.getString(13),
                                              Integer.parseInt(cursor.getString(14)),
                                              cursor.getString(15),
                                              Integer.parseInt(cursor.getString(16)),
                                              cursor.getString(17),
                                              Integer.parseInt(cursor.getString(18)),
                                              Integer.parseInt(cursor.getString(19)),
                                              Integer.parseInt(cursor.getString(20)),
                                              Integer.parseInt(cursor.getString(21)),
                                              cursor.getString(22),
                                              Integer.parseInt(cursor.getString(23)),
                                              cursor.getString(24),
                                              Integer.parseInt(cursor.getString(25)),
                                              Integer.parseInt(cursor.getString(26)),
                                              Integer.parseInt(cursor.getString(27)),
                                              Integer.parseInt(cursor.getString(28)),
                                              cursor.getString(29),
                                              Integer.parseInt(cursor.getString(30)),
                                              Integer.parseInt(cursor.getString(31)),
                                              Integer.parseInt(cursor.getString(32)),
                                              Integer.parseInt(cursor.getString(33)),
                                              Integer.parseInt(cursor.getString(34)),
                                              Integer.parseInt(cursor.getString(35)),
                                              Integer.parseInt(cursor.getString(36)),
                                              Integer.parseInt(cursor.getString(37)),
                                              Integer.parseInt(cursor.getString(38)),
                                              Integer.parseInt(cursor.getString(39)),
                                              cursor.isNull(40) ? 0 : Integer.parseInt(cursor.getString(40)),
                                              Integer.parseInt(cursor.getString(41)),
                                              Integer.parseInt(cursor.getString(43)) == 1,
                                              Integer.parseInt(cursor.getString(44)),
                                              Integer.parseInt(cursor.getString(45)),
                                              Integer.parseInt(cursor.getString(46)),
                                              Integer.parseInt(cursor.getString(47)),
                                              Integer.parseInt(cursor.getString(48)) == 1,
                                              Integer.parseInt(cursor.getString(49)),
                                              cursor.getString(50)
                                              );
            }
            cursor.close();
        }
        //db.close();

        // return profile
        return profile;
    }

    // Getting All Profiles
    List<Profile> getAllProfiles() {
        List<Profile> profileList = new ArrayList<>();
        // Select All Query
        final String selectQuery = "SELECT " + KEY_ID + "," +
                                         KEY_NAME + "," +
                                         KEY_ICON + "," +
                                         KEY_CHECKED + "," +
                                         KEY_PORDER + "," +
                                         KEY_VOLUME_RINGER_MODE + "," +
                                         KEY_VOLUME_RINGTONE + "," +
                                         KEY_VOLUME_NOTIFICATION + "," +
                                         KEY_VOLUME_MEDIA + "," +
                                         KEY_VOLUME_ALARM + "," +
                                         KEY_VOLUME_SYSTEM + "," +
                                         KEY_VOLUME_VOICE + "," +
                                         KEY_SOUND_RINGTONE_CHANGE + "," +
                                         KEY_SOUND_RINGTONE + "," +
                                         KEY_SOUND_NOTIFICATION_CHANGE + "," +
                                         KEY_SOUND_NOTIFICATION + "," +
                                         KEY_SOUND_ALARM_CHANGE + "," +
                                         KEY_SOUND_ALARM + "," +
                                         KEY_DEVICE_AIRPLANE_MODE + "," +
                                         KEY_DEVICE_WIFI + "," +
                                         KEY_DEVICE_BLUETOOTH + "," +
                                         KEY_DEVICE_SCREEN_TIMEOUT + "," +
                                         KEY_DEVICE_BRIGHTNESS + "," +
                                         KEY_DEVICE_WALLPAPER_CHANGE + "," +
                                         KEY_DEVICE_WALLPAPER + "," +
                                         KEY_DEVICE_MOBILE_DATA + "," +
                                         KEY_DEVICE_MOBILE_DATA_PREFS + "," +
                                         KEY_DEVICE_GPS + "," +
                                         KEY_DEVICE_RUN_APPLICATION_CHANGE + "," +
                                         KEY_DEVICE_RUN_APPLICATION_PACKAGE_NAME + ","+
                                         KEY_DEVICE_AUTOSYNC + "," +
                                         KEY_DEVICE_AUTOROTATE + "," +
                                         KEY_DEVICE_LOCATION_SERVICE_PREFS + "," +
                                         KEY_VOLUME_SPEAKER_PHONE + "," +
                                         KEY_DEVICE_NFC + "," +
                                         KEY_DURATION + "," +
                                         KEY_AFTER_DURATION_DO + "," +
                                         KEY_VOLUME_ZEN_MODE + "," +
                                         KEY_DEVICE_KEYGUARD + "," +
                                         KEY_VIBRATE_ON_TOUCH + "," +
                                         KEY_DEVICE_WIFI_AP + "," +
                                         KEY_DEVICE_POWER_SAVE_MODE + "," +
                                         KEY_SHOW_DURATION_BUTTON + "," +
                                         KEY_ASK_FOR_DURATION + "," +
                                         KEY_DEVICE_NETWORK_TYPE + "," +
                                         KEY_NOTIFICATION_LED + "," +
                                         KEY_VIBRATE_WHEN_RINGING + "," +
                                         KEY_DEVICE_WALLPAPER_FOR + "," +
                                         KEY_HIDE_STATUS_BAR_ICON + "," +
                                         KEY_LOCK_DEVICE +"," +
                                         KEY_DEVICE_CONNECT_TO_SSID +
                             " FROM " + TABLE_PROFILES + " ORDER BY " + KEY_PORDER;

        //SQLiteDatabase db = this.getReadableDatabase();
        SQLiteDatabase db = getMyWritableDatabase();

        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Profile profile = new Profile();
                profile._id = Long.parseLong(cursor.getString(0));
                profile._name = cursor.getString(1);
                profile._icon = cursor.getString(2);
                profile._checked = Integer.parseInt(cursor.getString(3)) == 1;
                profile._porder = Integer.parseInt(cursor.getString(4));
                profile._volumeRingerMode = Integer.parseInt(cursor.getString(5));
                profile._volumeRingtone = cursor.getString(6);
                profile._volumeNotification = cursor.getString(7);
                profile._volumeMedia = cursor.getString(8);
                profile._volumeAlarm = cursor.getString(9);
                profile._volumeSystem = cursor.getString(10);
                profile._volumeVoice = cursor.getString(11);
                profile._soundRingtoneChange = Integer.parseInt(cursor.getString(12));
                profile._soundRingtone = cursor.getString(13);
                profile._soundNotificationChange = Integer.parseInt(cursor.getString(14));
                profile._soundNotification = cursor.getString(15);
                profile._soundAlarmChange = Integer.parseInt(cursor.getString(16));
                profile._soundAlarm = cursor.getString(17);
                profile._deviceAirplaneMode = Integer.parseInt(cursor.getString(18));
                profile._deviceWiFi = Integer.parseInt(cursor.getString(19));
                profile._deviceBluetooth = Integer.parseInt(cursor.getString(20));
                profile._deviceScreenTimeout = Integer.parseInt(cursor.getString(21));
                profile._deviceBrightness = cursor.getString(22);
                profile._deviceWallpaperChange = Integer.parseInt(cursor.getString(23));
                profile._deviceWallpaper = cursor.getString(24);
                profile._deviceMobileData = Integer.parseInt(cursor.getString(25));
                profile._deviceMobileDataPrefs = Integer.parseInt(cursor.getString(26));
                profile._deviceGPS = Integer.parseInt(cursor.getString(27));
                profile._deviceRunApplicationChange = Integer.parseInt(cursor.getString(28));
                profile._deviceRunApplicationPackageName = cursor.getString(29);
                profile._deviceAutosync = Integer.parseInt(cursor.getString(30));
                profile._deviceAutoRotate = Integer.parseInt(cursor.getString(31));
                profile._deviceLocationServicePrefs = Integer.parseInt(cursor.getString(32));
                profile._volumeSpeakerPhone = Integer.parseInt(cursor.getString(33));
                profile._deviceNFC = Integer.parseInt(cursor.getString(34));
                profile._duration = Integer.parseInt(cursor.getString(35));
                profile._afterDurationDo = Integer.parseInt(cursor.getString(36));
                profile._volumeZenMode = Integer.parseInt(cursor.getString(37));
                profile._deviceKeyguard = Integer.parseInt(cursor.getString(38));
                profile._vibrationOnTouch = Integer.parseInt(cursor.getString(39));
                profile._deviceWiFiAP = cursor.isNull(40) ? 0 : Integer.parseInt(cursor.getString(40));
                profile._devicePowerSaveMode = Integer.parseInt(cursor.getString(41));
                profile._askForDuration = Integer.parseInt(cursor.getString(43)) == 1;
                profile._deviceNetworkType = Integer.parseInt(cursor.getString(44));
                profile._notificationLed = Integer.parseInt(cursor.getString(45));
                profile._vibrateWhenRinging = Integer.parseInt(cursor.getString(46));
                profile._deviceWallpaperFor = Integer.parseInt(cursor.getString(47));
                profile._hideStatusBarIcon = Integer.parseInt(cursor.getString(48)) == 1;
                profile._lockDevice = Integer.parseInt(cursor.getString(49));
                profile._deviceConnectToSSID = cursor.getString(50);
                // Adding contact to list
                profileList.add(profile);
            } while (cursor.moveToNext());
        }

        cursor.close();
        //db.close();

        // return profile list
        return profileList;
    }

    // Updating single profile
    int updateProfile(Profile profile) {
        //SQLiteDatabase db = this.getWritableDatabase();
        SQLiteDatabase db = getMyWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_NAME, profile._name);
        values.put(KEY_ICON, profile._icon);
        values.put(KEY_CHECKED, (profile._checked) ? 1 : 0);
        values.put(KEY_PORDER, profile._porder);
        values.put(KEY_VOLUME_RINGER_MODE, profile._volumeRingerMode);
        values.put(KEY_VOLUME_ZEN_MODE, profile._volumeZenMode);
        values.put(KEY_VOLUME_RINGTONE, profile._volumeRingtone);
        values.put(KEY_VOLUME_NOTIFICATION, profile._volumeNotification);
        values.put(KEY_VOLUME_MEDIA, profile._volumeMedia);
        values.put(KEY_VOLUME_ALARM, profile._volumeAlarm);
        values.put(KEY_VOLUME_SYSTEM, profile._volumeSystem);
        values.put(KEY_VOLUME_VOICE, profile._volumeVoice);
        values.put(KEY_SOUND_RINGTONE_CHANGE, profile._soundRingtoneChange);
        values.put(KEY_SOUND_RINGTONE, profile._soundRingtone);
        values.put(KEY_SOUND_NOTIFICATION_CHANGE, profile._soundNotificationChange);
        values.put(KEY_SOUND_NOTIFICATION, profile._soundNotification);
        values.put(KEY_SOUND_ALARM_CHANGE, profile._soundAlarmChange);
        values.put(KEY_SOUND_ALARM, profile._soundAlarm);
        values.put(KEY_DEVICE_AIRPLANE_MODE, profile._deviceAirplaneMode);
        values.put(KEY_DEVICE_WIFI, profile._deviceWiFi);
        values.put(KEY_DEVICE_BLUETOOTH, profile._deviceBluetooth);
        values.put(KEY_DEVICE_SCREEN_TIMEOUT, profile._deviceScreenTimeout);
        values.put(KEY_DEVICE_BRIGHTNESS, profile._deviceBrightness);
        values.put(KEY_DEVICE_WALLPAPER_CHANGE, profile._deviceWallpaperChange);
        values.put(KEY_DEVICE_WALLPAPER, profile._deviceWallpaper);
        values.put(KEY_DEVICE_MOBILE_DATA, profile._deviceMobileData);
        values.put(KEY_DEVICE_MOBILE_DATA_PREFS, profile._deviceMobileDataPrefs);
        values.put(KEY_DEVICE_GPS, profile._deviceGPS);
        values.put(KEY_DEVICE_RUN_APPLICATION_CHANGE, profile._deviceRunApplicationChange);
        values.put(KEY_DEVICE_RUN_APPLICATION_PACKAGE_NAME, profile._deviceRunApplicationPackageName);
        values.put(KEY_DEVICE_AUTOSYNC, profile._deviceAutosync);
        values.put(KEY_DEVICE_AUTOROTATE, profile._deviceAutoRotate);
        values.put(KEY_DEVICE_LOCATION_SERVICE_PREFS, profile._deviceLocationServicePrefs);
        values.put(KEY_VOLUME_SPEAKER_PHONE, profile._volumeSpeakerPhone);
        values.put(KEY_DEVICE_NFC, profile._deviceNFC);
        values.put(KEY_DURATION, profile._duration);
        values.put(KEY_AFTER_DURATION_DO, profile._afterDurationDo);
        values.put(KEY_DEVICE_KEYGUARD, profile._deviceKeyguard);
        values.put(KEY_VIBRATE_ON_TOUCH, profile._vibrationOnTouch);
        values.put(KEY_DEVICE_WIFI_AP, profile._deviceWiFiAP);
        values.put(KEY_DEVICE_POWER_SAVE_MODE, profile._devicePowerSaveMode);
        values.put(KEY_SHOW_DURATION_BUTTON, 0);
        values.put(KEY_ASK_FOR_DURATION, (profile._askForDuration) ? 1 : 0);
        values.put(KEY_DEVICE_NETWORK_TYPE, profile._deviceNetworkType);
        values.put(KEY_NOTIFICATION_LED, profile._notificationLed);
        values.put(KEY_VIBRATE_WHEN_RINGING, profile._vibrateWhenRinging);
        values.put(KEY_DEVICE_WALLPAPER_FOR, profile._deviceWallpaperFor);
        values.put(KEY_HIDE_STATUS_BAR_ICON, (profile._hideStatusBarIcon) ? 1 : 0);
        values.put(KEY_LOCK_DEVICE, profile._lockDevice);
        values.put(KEY_DEVICE_CONNECT_TO_SSID, profile._deviceConnectToSSID);

        // updating row
        return db.update(TABLE_PROFILES, values, KEY_ID + " = ?",
                        new String[] { String.valueOf(profile._id) });
        //db.close();
        
        //return r;
    }

    // Deleting single profile
    void deleteProfile(Profile profile) {
        //SQLiteDatabase db = this.getWritableDatabase();
        SQLiteDatabase db = getMyWritableDatabase();

        db.beginTransaction();
        try {

            // unlink shortcuts from profile
            String[] splits = profile._deviceRunApplicationPackageName.split("\\|");
            for (String split : splits) {
                boolean shortcut = ApplicationsCache.isShortcut(split);
                if (shortcut) {
                    long shortcutId = ApplicationsCache.getShortcutId(split);
                    deleteShortcut(shortcutId);
                }
            }

            db.delete(TABLE_PROFILES, KEY_ID + " = ?",
                    new String[] { String.valueOf(profile._id) });

            db.setTransactionSuccessful();
        } catch (Exception e){
            //Error in between database transaction
        } finally {
            db.endTransaction();
        }

        //db.close();
    }

    // Deleting all profile2
    void deleteAllProfiles() {
        //SQLiteDatabase db = this.getWritableDatabase();
        SQLiteDatabase db = getMyWritableDatabase();

        db.beginTransaction();

        try {

            db.delete(TABLE_PROFILES, null, null);

            db.delete(TABLE_SHORTCUTS, null, null);

            db.setTransactionSuccessful();
        } catch (Exception e){
            //Error in between database transaction
        } finally {
            db.endTransaction();
        }

        //db.close();
    }

    // Getting profiles Count
    int getProfilesCount() {
        final String countQuery = "SELECT  count(*) FROM " + TABLE_PROFILES;

        //SQLiteDatabase db = this.getReadableDatabase();
        SQLiteDatabase db = getMyWritableDatabase();

        Cursor cursor = db.rawQuery(countQuery, null);

        int r;

        if (cursor != null)
        {
            cursor.moveToFirst();
            r = Integer.parseInt(cursor.getString(0));
            cursor.close();
        }
        else
            r = 0;

        //db.close();

        return r;
    }

    // Getting max(porder)
    private int getMaxPOrder() {
        String countQuery = "SELECT MAX(PORDER) FROM " + TABLE_PROFILES;
        //SQLiteDatabase db = this.getReadableDatabase();
        SQLiteDatabase db = getMyWritableDatabase();

        Cursor cursor = db.rawQuery(countQuery, null);

        int r;

        if (cursor.getCount() == 0)
            r = 0;
        else
        {
            if (cursor.moveToFirst())
                // return max(porder)
                r = cursor.getInt(0);
            else
                r = 0;
        }

        cursor.close();
        //db.close();

        return r;

    }

    private void doActivateProfile(Profile profile, boolean activate)
    {
        //SQLiteDatabase db = this.getWritableDatabase();
        SQLiteDatabase db = getMyWritableDatabase();

        db.beginTransaction();
        try {
            // update all profiles checked to false
            ContentValues valuesAll = new ContentValues();
            valuesAll.put(KEY_CHECKED, 0);
            db.update(TABLE_PROFILES, valuesAll, null, null);

            // updating checked = true for profile
            //profile.setChecked(true);

            if (activate && (profile != null))
            {
                ContentValues values = new ContentValues();
                //values.put(KEY_CHECKED, (profile.getChecked()) ? 1 : 0);
                values.put(KEY_CHECKED, 1);

                db.update(TABLE_PROFILES, values, KEY_ID + " = ?",
                                new String[] { String.valueOf(profile._id) });
            }

            db.setTransactionSuccessful();
         } catch (Exception e){
             //Error in between database transaction
         } finally {
            db.endTransaction();
         }	

         //db.close();
    }

    public void activateProfile(Profile profile)
    {
        doActivateProfile(profile, true);
    }

    void deactivateProfile()
    {
        doActivateProfile(null, false);
    }

    public Profile getActivatedProfile()
    {
        //SQLiteDatabase db = this.getReadableDatabase();
        SQLiteDatabase db = getMyWritableDatabase();

        Profile profile;

        Cursor cursor = db.query(TABLE_PROFILES,
                                 new String[] { KEY_ID,
                                                KEY_NAME,
                                                KEY_ICON,
                                                KEY_CHECKED,
                                                KEY_PORDER,
                                                KEY_VOLUME_RINGER_MODE,
                                                KEY_VOLUME_RINGTONE,
                                                KEY_VOLUME_NOTIFICATION,
                                                KEY_VOLUME_MEDIA,
                                                KEY_VOLUME_ALARM,
                                                KEY_VOLUME_SYSTEM,
                                                KEY_VOLUME_VOICE,
                                                KEY_SOUND_RINGTONE_CHANGE,
                                                KEY_SOUND_RINGTONE,
                                                KEY_SOUND_NOTIFICATION_CHANGE,
                                                KEY_SOUND_NOTIFICATION,
                                                KEY_SOUND_ALARM_CHANGE,
                                                KEY_SOUND_ALARM,
                                                KEY_DEVICE_AIRPLANE_MODE,
                                                KEY_DEVICE_WIFI,
                                                KEY_DEVICE_BLUETOOTH,
                                                KEY_DEVICE_SCREEN_TIMEOUT,
                                                KEY_DEVICE_BRIGHTNESS,
                                                KEY_DEVICE_WALLPAPER_CHANGE,
                                                KEY_DEVICE_WALLPAPER,
                                                KEY_DEVICE_MOBILE_DATA,
                                                KEY_DEVICE_MOBILE_DATA_PREFS,
                                                KEY_DEVICE_GPS,
                                                KEY_DEVICE_RUN_APPLICATION_CHANGE,
                                                KEY_DEVICE_RUN_APPLICATION_PACKAGE_NAME,
                                                KEY_DEVICE_AUTOSYNC,
                                                KEY_DEVICE_AUTOROTATE,
                                                KEY_DEVICE_LOCATION_SERVICE_PREFS,
                                                KEY_VOLUME_SPEAKER_PHONE,
                                                KEY_DEVICE_NFC,
                                                KEY_DURATION,
                                                KEY_AFTER_DURATION_DO,
                                                KEY_VOLUME_ZEN_MODE,
                                                KEY_DEVICE_KEYGUARD,
                                                KEY_VIBRATE_ON_TOUCH,
                                                KEY_DEVICE_WIFI_AP,
                                                KEY_DEVICE_POWER_SAVE_MODE,
                                                KEY_SHOW_DURATION_BUTTON,
                                                KEY_ASK_FOR_DURATION,
                                                KEY_DEVICE_NETWORK_TYPE,
                                                KEY_NOTIFICATION_LED,
                                                KEY_VIBRATE_WHEN_RINGING,
                                                KEY_DEVICE_WALLPAPER_FOR,
                                                KEY_HIDE_STATUS_BAR_ICON,
                                                KEY_LOCK_DEVICE,
                                                KEY_DEVICE_CONNECT_TO_SSID
                                                },
                                 KEY_CHECKED + "=?",
                                 new String[] { "1" }, null, null, null, null);
        if (cursor != null)
        {
            cursor.moveToFirst();

            int rc = cursor.getCount();

            if (rc == 1)
            {

                profile = new Profile(Long.parseLong(cursor.getString(0)),
                                              cursor.getString(1),
                                              cursor.getString(2),
                                              Integer.parseInt(cursor.getString(3)) == 1,
                                              Integer.parseInt(cursor.getString(4)),
                                              Integer.parseInt(cursor.getString(5)),
                                              cursor.getString(6),
                                              cursor.getString(7),
                                              cursor.getString(8),
                                              cursor.getString(9),
                                              cursor.getString(10),
                                              cursor.getString(11),
                                              Integer.parseInt(cursor.getString(12)),
                                              cursor.getString(13),
                                              Integer.parseInt(cursor.getString(14)),
                                              cursor.getString(15),
                                              Integer.parseInt(cursor.getString(16)),
                                              cursor.getString(17),
                                              Integer.parseInt(cursor.getString(18)),
                                              Integer.parseInt(cursor.getString(19)),
                                              Integer.parseInt(cursor.getString(20)),
                                              Integer.parseInt(cursor.getString(21)),
                                              cursor.getString(22),
                                              Integer.parseInt(cursor.getString(23)),
                                              cursor.getString(24),
                                              Integer.parseInt(cursor.getString(25)),
                                              Integer.parseInt(cursor.getString(26)),
                                              Integer.parseInt(cursor.getString(27)),
                                              Integer.parseInt(cursor.getString(28)),
                                              cursor.getString(29),
                                              Integer.parseInt(cursor.getString(30)),
                                              Integer.parseInt(cursor.getString(31)),
                                              Integer.parseInt(cursor.getString(32)),
                                              Integer.parseInt(cursor.getString(33)),
                                              Integer.parseInt(cursor.getString(34)),
                                              Integer.parseInt(cursor.getString(35)),
                                              Integer.parseInt(cursor.getString(36)),
                                              Integer.parseInt(cursor.getString(37)),
                                              Integer.parseInt(cursor.getString(38)),
                                              Integer.parseInt(cursor.getString(39)),
                                              cursor.isNull(40) ? 0 : Integer.parseInt(cursor.getString(40)),
                                              Integer.parseInt(cursor.getString(41)),
                                              Integer.parseInt(cursor.getString(43)) == 1,
                                              Integer.parseInt(cursor.getString(44)),
                                              Integer.parseInt(cursor.getString(45)),
                                              Integer.parseInt(cursor.getString(46)),
                                              Integer.parseInt(cursor.getString(47)),
                                              Integer.parseInt(cursor.getString(48)) == 1,
                                              Integer.parseInt(cursor.getString(49)),
                                              cursor.getString(50)
                                              );
            }
            else
                profile = null;
            cursor.close();
        }
        else
            profile = null;


        //db.close();

        // return profile
        return profile;

    }

    Profile getFirstProfile()
    {
        final String selectQuery = "SELECT " + KEY_ID + "," +
                                         KEY_NAME + "," +
                                         KEY_ICON + "," +
                                         KEY_CHECKED + "," +
                                         KEY_PORDER + "," +
                                         KEY_VOLUME_RINGER_MODE + "," +
                                         KEY_VOLUME_RINGTONE + "," +
                                         KEY_VOLUME_NOTIFICATION + "," +
                                         KEY_VOLUME_MEDIA + "," +
                                         KEY_VOLUME_ALARM + "," +
                                         KEY_VOLUME_SYSTEM + "," +
                                         KEY_VOLUME_VOICE + "," +
                                         KEY_SOUND_RINGTONE_CHANGE + "," +
                                         KEY_SOUND_RINGTONE + "," +
                                         KEY_SOUND_NOTIFICATION_CHANGE + "," +
                                         KEY_SOUND_NOTIFICATION + "," +
                                         KEY_SOUND_ALARM_CHANGE + "," +
                                         KEY_SOUND_ALARM + "," +
                                         KEY_DEVICE_AIRPLANE_MODE + "," +
                                         KEY_DEVICE_WIFI + "," +
                                         KEY_DEVICE_BLUETOOTH + "," +
                                         KEY_DEVICE_SCREEN_TIMEOUT + "," +
                                         KEY_DEVICE_BRIGHTNESS + "," +
                                         KEY_DEVICE_WALLPAPER_CHANGE + "," +
                                         KEY_DEVICE_WALLPAPER + "," +
                                         KEY_DEVICE_MOBILE_DATA + "," +
                                         KEY_DEVICE_MOBILE_DATA_PREFS + "," +
                                         KEY_DEVICE_GPS + "," +
                                         KEY_DEVICE_RUN_APPLICATION_CHANGE + "," +
                                         KEY_DEVICE_RUN_APPLICATION_PACKAGE_NAME + "," +
                                         KEY_DEVICE_AUTOSYNC + "," +
                                         KEY_DEVICE_AUTOROTATE + "," +
                                         KEY_DEVICE_LOCATION_SERVICE_PREFS + "," +
                                         KEY_VOLUME_SPEAKER_PHONE + "," +
                                         KEY_DEVICE_NFC + "," +
                                         KEY_DURATION + "," +
                                         KEY_AFTER_DURATION_DO + "," +
                                         KEY_VOLUME_ZEN_MODE + "," +
                                         KEY_DEVICE_KEYGUARD + "," +
                                         KEY_VIBRATE_ON_TOUCH + "," +
                                         KEY_DEVICE_WIFI_AP + "," +
                                         KEY_DEVICE_POWER_SAVE_MODE + "," +
                                         KEY_SHOW_DURATION_BUTTON + "," +
                                         KEY_ASK_FOR_DURATION + "," +
                                         KEY_DEVICE_NETWORK_TYPE + "," +
                                         KEY_NOTIFICATION_LED + "," +
                                         KEY_VIBRATE_WHEN_RINGING + "," +
                                         KEY_DEVICE_WALLPAPER_FOR + "," +
                                         KEY_HIDE_STATUS_BAR_ICON + "," +
                                         KEY_LOCK_DEVICE + "," +
                                         KEY_DEVICE_CONNECT_TO_SSID +
                            " FROM " + TABLE_PROFILES + " ORDER BY " + KEY_PORDER;

        //SQLiteDatabase db = this.getReadableDatabase();
        SQLiteDatabase db = getMyWritableDatabase();

        Cursor cursor = db.rawQuery(selectQuery, null);

        Profile profile = null;

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            profile = new Profile();
            profile._id = Long.parseLong(cursor.getString(0));
            profile._name = cursor.getString(1);
            profile._icon = (cursor.getString(2));
            profile._checked = Integer.parseInt(cursor.getString(3)) == 1;
            profile._porder = (Integer.parseInt(cursor.getString(4)));
            profile._volumeRingerMode = Integer.parseInt(cursor.getString(5));
            profile._volumeRingtone = cursor.getString(6);
            profile._volumeNotification = cursor.getString(7);
            profile._volumeMedia = cursor.getString(8);
            profile._volumeAlarm = cursor.getString(9);
            profile._volumeSystem = cursor.getString(10);
            profile._volumeVoice = cursor.getString(11);
            profile._soundRingtoneChange = Integer.parseInt(cursor.getString(12));
            profile._soundRingtone = cursor.getString(13);
            profile._soundNotificationChange = Integer.parseInt(cursor.getString(14));
            profile._soundNotification = cursor.getString(15);
            profile._soundAlarmChange = Integer.parseInt(cursor.getString(16));
            profile._soundAlarm = cursor.getString(17);
            profile._deviceAirplaneMode = Integer.parseInt(cursor.getString(18));
            profile._deviceWiFi = Integer.parseInt(cursor.getString(19));
            profile._deviceBluetooth = Integer.parseInt(cursor.getString(20));
            profile._deviceScreenTimeout = Integer.parseInt(cursor.getString(21));
            profile._deviceBrightness = cursor.getString(22);
            profile._deviceWallpaperChange = Integer.parseInt(cursor.getString(23));
            profile._deviceWallpaper = cursor.getString(24);
            profile._deviceMobileData = Integer.parseInt(cursor.getString(25));
            profile._deviceMobileDataPrefs = Integer.parseInt(cursor.getString(26));
            profile._deviceGPS = Integer.parseInt(cursor.getString(27));
            profile._deviceRunApplicationChange = Integer.parseInt(cursor.getString(28));
            profile._deviceRunApplicationPackageName = cursor.getString(29);
            profile._deviceAutosync = Integer.parseInt(cursor.getString(30));
            profile._deviceAutoRotate = Integer.parseInt(cursor.getString(31));
            profile._deviceLocationServicePrefs = Integer.parseInt(cursor.getString(32));
            profile._volumeSpeakerPhone = Integer.parseInt(cursor.getString(33));
            profile._deviceNFC = Integer.parseInt(cursor.getString(34));
            profile._duration = Integer.parseInt(cursor.getString(35));
            profile._afterDurationDo = Integer.parseInt(cursor.getString(36));
            profile._volumeZenMode = Integer.parseInt(cursor.getString(37));
            profile._deviceKeyguard = Integer.parseInt(cursor.getString(38));
            profile._vibrationOnTouch = Integer.parseInt(cursor.getString(39));
            profile._deviceWiFiAP = cursor.isNull(40) ? 0 : Integer.parseInt(cursor.getString(40));
            profile._devicePowerSaveMode = Integer.parseInt(cursor.getString(41));
            profile._askForDuration = Integer.parseInt(cursor.getString(43)) == 1;
            profile._deviceNetworkType = Integer.parseInt(cursor.getString(44));
            profile._notificationLed = Integer.parseInt(cursor.getString(45));
            profile._vibrateWhenRinging = Integer.parseInt(cursor.getString(46));
            profile._deviceWallpaperFor = Integer.parseInt(cursor.getString(47));
            profile._hideStatusBarIcon = Integer.parseInt(cursor.getString(48)) == 1;
            profile._lockDevice = Integer.parseInt(cursor.getString(49));
            profile._deviceConnectToSSID = cursor.getString(50);
        }

        cursor.close();
        //db.close();

        // return profile list
        return profile;

    }

    int getProfilePosition(Profile profile)
    {
        final String selectQuery = "SELECT " + KEY_ID +
                               " FROM " + TABLE_PROFILES + " ORDER BY " + KEY_PORDER;

        //SQLiteDatabase db = this.getReadableDatabase();
        SQLiteDatabase db = getMyWritableDatabase();

        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        long lid;
        int position = 0;
        if (cursor.moveToFirst()) {
            do {
                lid = Long.parseLong(cursor.getString(0));
                if (lid == profile._id)
                    return position;
                position++;
            } while (cursor.moveToNext());
        }

        cursor.close();
        //db.close();

        // return profile list
        return -1;


    }

    void setPOrder(List<Profile> list)
    {
        //SQLiteDatabase db = this.getWritableDatabase();
        SQLiteDatabase db = getMyWritableDatabase();

        ContentValues values = new ContentValues();

        db.beginTransaction();
        try {

            for (Profile profile : list)
            {
                values.put(KEY_PORDER, profile._porder);

                db.update(TABLE_PROFILES, values, KEY_ID + " = ?",
                            new String[] { String.valueOf(profile._id) });
            }

            db.setTransactionSuccessful();
         } catch (Exception e){
             //Error in between database transaction
         } finally {
            db.endTransaction();
         }	

        //db.close();
    }

    public void setChecked(List<Profile> list)
    {
        //SQLiteDatabase db = this.getWritableDatabase();
        SQLiteDatabase db = getMyWritableDatabase();

        ContentValues values = new ContentValues();

        db.beginTransaction();
        try {

            for (Profile profile : list)
            {
                values.put(KEY_CHECKED, profile._checked);

                db.update(TABLE_PROFILES, values, KEY_ID + " = ?",
                            new String[] { String.valueOf(profile._id) });
            }

            db.setTransactionSuccessful();
         } catch (Exception e){
             //Error in between database transaction
         } finally {
            db.endTransaction();
         }	

        //db.close();
    }

    /*
    public int getActiveProfileSpeakerphone()
    {
        //SQLiteDatabase db = this.getReadableDatabase();
        SQLiteDatabase db = getMyWritableDatabase();

        Cursor cursor = db.query(TABLE_PROFILES,
                new String[]{KEY_VOLUME_SPEAKER_PHONE},
                KEY_CHECKED + "=?",
                new String[]{"1"}, null, null, null, null);

        int speakerPhone;

        if (cursor != null)
        {
            cursor.moveToFirst();

            int rc = cursor.getCount();

            if (rc == 1)
            {
                speakerPhone = Integer.parseInt(cursor.getString(0));
            }
            else
                speakerPhone = 0;

            cursor.close();
        }
        else
            speakerPhone = 0;

        //db.close();

        return speakerPhone;
    }
    */

    void getProfileIcon(Profile profile)
    {
        //SQLiteDatabase db = this.getReadableDatabase();
        SQLiteDatabase db = getMyWritableDatabase();

        Cursor cursor = db.query(TABLE_PROFILES,
                new String[] { KEY_ICON },
                KEY_ID + "=?",
                new String[] { Long.toString(profile._id) }, null, null, null, null);

        if (cursor != null)
        {
            if (cursor.moveToFirst())
                profile._icon = cursor.getString(0);
            cursor.close();
        }

        //db.close();

    }

    int disableNotAllowedPreferences(Context context)
    {
        int ret = 0;

        final String selectQuery = "SELECT " + KEY_ID + "," +
                                        KEY_DEVICE_AIRPLANE_MODE + "," +
                                        KEY_DEVICE_WIFI + "," +
                                        KEY_DEVICE_BLUETOOTH + "," +
                                        KEY_DEVICE_MOBILE_DATA + "," +
                                        KEY_DEVICE_MOBILE_DATA_PREFS + "," +
                                        KEY_DEVICE_GPS + "," +
                                        KEY_DEVICE_LOCATION_SERVICE_PREFS + "," +
                                        KEY_DEVICE_NFC + "," +
                                        KEY_VOLUME_RINGER_MODE + "," +
                                        KEY_DEVICE_WIFI_AP + "," +
                                        KEY_DEVICE_POWER_SAVE_MODE + "," +
                                        KEY_VOLUME_ZEN_MODE + "," +
                                        KEY_DEVICE_NETWORK_TYPE + "," +
                                        KEY_NOTIFICATION_LED + "," +
                                        KEY_VIBRATE_WHEN_RINGING + "," +
                                        KEY_DEVICE_CONNECT_TO_SSID +
                            " FROM " + TABLE_PROFILES;

        //SQLiteDatabase db = this.getWritableDatabase();
        SQLiteDatabase db = getMyWritableDatabase();

        ContentValues values = new ContentValues();

        Cursor cursor = db.rawQuery(selectQuery, null);

        db.beginTransaction();
        try {

            if (cursor.moveToFirst()) {
                do {
                        if ((Integer.parseInt(cursor.getString(1)) != 0) &&
                            (PPApplication.isProfilePreferenceAllowed(PPApplication.PREF_PROFILE_DEVICE_AIRPLANE_MODE, context) == PPApplication.PREFERENCE_NOT_ALLOWED))
                        {
                            values.clear();
                            values.put(KEY_DEVICE_AIRPLANE_MODE, 0);
                            db.update(TABLE_PROFILES, values, KEY_ID + " = ?",
                               new String[] { String.valueOf(Integer.parseInt(cursor.getString(0))) });
                        }

                        if ((Integer.parseInt(cursor.getString(2)) != 0) &&
                            (PPApplication.isProfilePreferenceAllowed(PPApplication.PREF_PROFILE_DEVICE_WIFI, context) == PPApplication.PREFERENCE_NOT_ALLOWED))
                        {
                            values.clear();
                            values.put(KEY_DEVICE_WIFI, 0);
                            db.update(TABLE_PROFILES, values, KEY_ID + " = ?",
                               new String[] { String.valueOf(Integer.parseInt(cursor.getString(0))) });
                        }

                        if ((Integer.parseInt(cursor.getString(3)) != 0) &&
                            (PPApplication.isProfilePreferenceAllowed(PPApplication.PREF_PROFILE_DEVICE_BLUETOOTH, context) == PPApplication.PREFERENCE_NOT_ALLOWED))
                        {
                            values.clear();
                            values.put(KEY_DEVICE_BLUETOOTH, 0);
                            db.update(TABLE_PROFILES, values, KEY_ID + " = ?",
                               new String[] { String.valueOf(Integer.parseInt(cursor.getString(0))) });
                        }

                        if ((Integer.parseInt(cursor.getString(4)) != 0) &&
                            (PPApplication.isProfilePreferenceAllowed(PPApplication.PREF_PROFILE_DEVICE_MOBILE_DATA, context) == PPApplication.PREFERENCE_NOT_ALLOWED))
                        {
                            values.clear();
                            values.put(KEY_DEVICE_MOBILE_DATA, 0);
                            db.update(TABLE_PROFILES, values, KEY_ID + " = ?",
                               new String[] { String.valueOf(Integer.parseInt(cursor.getString(0))) });
                        }

                        if ((Integer.parseInt(cursor.getString(5)) != 0) &&
                            (PPApplication.isProfilePreferenceAllowed(PPApplication.PREF_PROFILE_DEVICE_MOBILE_DATA_PREFS, context) == PPApplication.PREFERENCE_NOT_ALLOWED))
                        {
                            values.clear();
                            values.put(KEY_DEVICE_MOBILE_DATA_PREFS, 0);
                            db.update(TABLE_PROFILES, values, KEY_ID + " = ?",
                               new String[] { String.valueOf(Integer.parseInt(cursor.getString(0))) });
                        }

                        if ((Integer.parseInt(cursor.getString(6)) != 0) &&
                            (PPApplication.isProfilePreferenceAllowed(PPApplication.PREF_PROFILE_DEVICE_GPS, context) == PPApplication.PREFERENCE_NOT_ALLOWED))
                        {
                            values.clear();
                            values.put(KEY_DEVICE_GPS, 0);
                            db.update(TABLE_PROFILES, values, KEY_ID + " = ?",
                               new String[] { String.valueOf(Integer.parseInt(cursor.getString(0))) });
                        }

                        if ((Integer.parseInt(cursor.getString(7)) != 0) &&
                            (PPApplication.isProfilePreferenceAllowed(PPApplication.PREF_PROFILE_DEVICE_LOCATION_SERVICE_PREFS, context) == PPApplication.PREFERENCE_NOT_ALLOWED))
                        {
                            values.clear();
                            values.put(KEY_DEVICE_LOCATION_SERVICE_PREFS, 0);
                            db.update(TABLE_PROFILES, values, KEY_ID + " = ?",
                               new String[] { String.valueOf(Integer.parseInt(cursor.getString(0))) });
                        }

                        if ((Integer.parseInt(cursor.getString(8)) != 0) &&
                            (PPApplication.isProfilePreferenceAllowed(PPApplication.PREF_PROFILE_DEVICE_NFC, context) == PPApplication.PREFERENCE_NOT_ALLOWED))
                        {
                            values.clear();
                            values.put(KEY_DEVICE_NFC, 0);
                            db.update(TABLE_PROFILES, values, KEY_ID + " = ?",
                               new String[] { String.valueOf(Integer.parseInt(cursor.getString(0))) });
                        }

                        if ((Integer.parseInt(cursor.getString(10)) != 0) &&
                                (PPApplication.isProfilePreferenceAllowed(PPApplication.PREF_PROFILE_DEVICE_WIFI_AP, context) == PPApplication.PREFERENCE_NOT_ALLOWED))
                        {
                            values.clear();
                            values.put(KEY_DEVICE_WIFI_AP, 0);
                            db.update(TABLE_PROFILES, values, KEY_ID + " = ?",
                                    new String[] { String.valueOf(Integer.parseInt(cursor.getString(0))) });
                        }

                        if (Integer.parseInt(cursor.getString(9)) == 5) {
                            boolean notRemove = PPApplication.canChangeZenMode(context, true);
                            if (!notRemove) {
                                int zenMode = cursor.getInt(12);
                                int ringerMode = 0;
                                switch (zenMode) {
                                    case 1:
                                        ringerMode = 1;
                                        break;
                                    case 2:
                                        ringerMode = 4;
                                        break;
                                    case 3:
                                        ringerMode = 4;
                                        break;
                                    case 4:
                                        ringerMode = 2;
                                        break;
                                    case 5:
                                        ringerMode = 3;
                                        break;
                                    case 6:
                                        ringerMode = 4;
                                        break;
                                }
                                values.clear();
                                values.put(KEY_VOLUME_RINGER_MODE, ringerMode);
                                db.update(TABLE_PROFILES, values, KEY_ID + " = ?",
                                        new String[] { String.valueOf(Integer.parseInt(cursor.getString(0))) });
                            }
                        }

                        if ((Integer.parseInt(cursor.getString(11)) != 0) &&
                                (PPApplication.isProfilePreferenceAllowed(PPApplication.PREF_PROFILE_DEVICE_POWER_SAVE_MODE, context)
                                        == PPApplication.PREFERENCE_NOT_ALLOWED))
                        {
                            values.clear();
                            values.put(KEY_DEVICE_POWER_SAVE_MODE, 0);
                            db.update(TABLE_PROFILES, values, KEY_ID + " = ?",
                                    new String[] { String.valueOf(Integer.parseInt(cursor.getString(0))) });
                        }

                        if ((Integer.parseInt(cursor.getString(13)) != 0) &&
                                (PPApplication.isProfilePreferenceAllowed(PPApplication.PREF_PROFILE_DEVICE_NETWORK_TYPE, context)
                                        == PPApplication.PREFERENCE_NOT_ALLOWED))
                        {
                            values.clear();
                            values.put(KEY_DEVICE_NETWORK_TYPE, 0);
                            db.update(TABLE_PROFILES, values, KEY_ID + " = ?",
                                    new String[] { String.valueOf(Integer.parseInt(cursor.getString(0))) });
                        }

                        if ((Integer.parseInt(cursor.getString(14)) != 0) &&
                                (PPApplication.isProfilePreferenceAllowed(PPApplication.PREF_PROFILE_NOTIFICATION_LED, context)
                                        == PPApplication.PREFERENCE_NOT_ALLOWED))
                        {
                            values.clear();
                            values.put(KEY_NOTIFICATION_LED, 0);
                            db.update(TABLE_PROFILES, values, KEY_ID + " = ?",
                                    new String[] { String.valueOf(Integer.parseInt(cursor.getString(0))) });
                        }

                        if ((Integer.parseInt(cursor.getString(15)) != 0) &&
                                (PPApplication.isProfilePreferenceAllowed(PPApplication.PREF_PROFILE_VIBRATE_WHEN_RINGING, context)
                                        == PPApplication.PREFERENCE_NOT_ALLOWED))
                        {
                            values.clear();
                            values.put(KEY_VIBRATE_WHEN_RINGING, 0);
                            db.update(TABLE_PROFILES, values, KEY_ID + " = ?",
                                    new String[] { String.valueOf(Integer.parseInt(cursor.getString(0))) });
                        }

                        if ((Integer.parseInt(cursor.getString(16)) != 0) &&
                                (PPApplication.isProfilePreferenceAllowed(PPApplication.PREF_PROFILE_DEVICE_CONNECT_TO_SSID, context) == PPApplication.PREFERENCE_NOT_ALLOWED))
                        {
                            values.clear();
                            values.put(KEY_DEVICE_CONNECT_TO_SSID, Profile.CONNECTTOSSID_JUSTANY);
                            db.update(TABLE_PROFILES, values, KEY_ID + " = ?",
                                    new String[] { String.valueOf(Integer.parseInt(cursor.getString(0))) });
                        }

                } while (cursor.moveToNext());
            }

            cursor.close();

            db.setTransactionSuccessful();

            ret = 1;
       } catch (Exception e){
            //Error in between database transaction
            Log.e("DatabaseHandler.updateForHardware", e.toString());
           ret = 0;
       } finally {
           db.endTransaction();
       }	

       //db.close();

       return ret;
    }

// SHORTCUTS ----------------------------------------------------------------------

    // Adding new shortcut
    void addShortcut(Shortcut shortcut) {

        //SQLiteDatabase db = this.getWritableDatabase();
        SQLiteDatabase db = getMyWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_S_INTENT, shortcut._intent);
        values.put(KEY_S_NAME, shortcut._name);

        db.beginTransaction();

        try {
            // Inserting Row
            shortcut._id = db.insert(TABLE_SHORTCUTS, null, values);

            db.setTransactionSuccessful();

        } catch (Exception e){
            //Error in between database transaction
        } finally {
            db.endTransaction();
        }

        //db.close(); // Closing database connection
    }

    // Getting single shortcut
    Shortcut getShortcut(long shortcutId) {
        //SQLiteDatabase db = this.getReadableDatabase();
        SQLiteDatabase db = getMyWritableDatabase();

        Cursor cursor = db.query(TABLE_SHORTCUTS,
                new String[]{KEY_S_ID,
                        KEY_S_INTENT,
                        KEY_S_NAME
                },
                KEY_S_ID + "=?",
                new String[]{String.valueOf(shortcutId)}, null, null, null, null);

        Shortcut shortcut = null;

        if (cursor != null)
        {
            cursor.moveToFirst();

            if (cursor.getCount() > 0)
            {
                shortcut = new Shortcut();
                shortcut._id = Long.parseLong(cursor.getString(0));
                shortcut._intent = cursor.getString(1);
                shortcut._name = cursor.getString(2);
            }

            cursor.close();
        }

        //db.close();

        return shortcut;
    }

    /*
    // Updating single shortcut
    public int updateShortcut(Shortcut shortcut) {
        //SQLiteDatabase db = this.getWritableDatabase();
        SQLiteDatabase db = getMyWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_S_INTENT, shortcut._intent);
        values.put(KEY_S_NAME, shortcut._name);

        int r = 0;

        db.beginTransaction();

        try {
            // updating row
            r = db.update(TABLE_SHORTCUTS, values, KEY_S_ID + " = ?",
                    new String[] { String.valueOf(shortcut._id) });

            db.setTransactionSuccessful();

        } catch (Exception e){
            //Error in between database transaction
            Log.e("DatabaseHandler.updateEvent", e.toString());
            r = 0;
        } finally {
            db.endTransaction();
        }

        //db.close();

        return r;
    }
    */

    // Deleting single shortcut
    void deleteShortcut(long shortcutId) {
        //SQLiteDatabase db = this.getWritableDatabase();
        SQLiteDatabase db = getMyWritableDatabase();

        db.beginTransaction();

        try {

            // delete geofence
            db.delete(TABLE_SHORTCUTS, KEY_S_ID + " = ?",
                    new String[]{String.valueOf(shortcutId)});

            db.setTransactionSuccessful();

        } catch (Exception e){
            //Error in between database transaction
            Log.e("DatabaseHandler.deleteGeofence", e.toString());
        } finally {
            db.endTransaction();
        }

        //db.close();
    }

// OTHERS -----------------------------------------------------------------

    private boolean tableExists(String tableName, SQLiteDatabase db)
    {
        @SuppressWarnings("unused")

        boolean tableExists = false;

        /* get cursor on it */
        try
        {
            Cursor c = db.query(tableName, null,
                    null, null, null, null, null);
            tableExists = true;
            c.close();
        }
        catch (Exception e) {
            /* not exists ? */
        }

        return tableExists;
    }

    //@SuppressWarnings("resource")
    int importDB(String applicationDataPath)
    {
        int ret = 0;

        // Close SQLiteOpenHelper so it will commit the created empty
        // database to internal storage
        //close();

        try {

            File sd = Environment.getExternalStorageDirectory();
            //File data = Environment.getDataDirectory();

            //File dataDB = new File(data, DB_FILEPATH + "/" + DATABASE_NAME);
            File exportedDB = new File(sd, applicationDataPath + "/" + EXPORT_DBFILENAME);

            if (exportedDB.exists())
            {
                // zistenie verzie zalohy
                SQLiteDatabase exportedDBObj = SQLiteDatabase.openDatabase(exportedDB.getAbsolutePath(), null, SQLiteDatabase.OPEN_READONLY);

                // db z SQLiteOpenHelper
                //SQLiteDatabase db = this.getWritableDatabase();
                SQLiteDatabase db = getMyWritableDatabase();

                Cursor cursorExportedDB = null;
                String[] columnNamesExportedDB;
                Cursor cursorImportDB = null;
                ContentValues values = new ContentValues();

                try {
                    db.beginTransaction();

                    db.execSQL("DELETE FROM " + TABLE_PROFILES);

                    // cursor for profiles exportedDB
                    cursorExportedDB = exportedDBObj.rawQuery("SELECT * FROM "+TABLE_PROFILES, null);
                    columnNamesExportedDB = cursorExportedDB.getColumnNames();

                    // cursor for profiles of destination db
                    cursorImportDB = db.rawQuery("SELECT * FROM "+TABLE_PROFILES, null);

                    int duration = 0;
                    int zenMode = 0;

                    if (cursorExportedDB.moveToFirst()) {
                        do {
                            values.clear();
                            for (int i = 0; i < columnNamesExportedDB.length; i++)
                            {
                                // put only when columnNamesExportedDB[i] exists in cursorImportDB
                                if (cursorImportDB.getColumnIndex(columnNamesExportedDB[i]) != -1)
                                {
                                    String value = cursorExportedDB.getString(i);

                                    // update values
                                    if (((exportedDBObj.getVersion() < 52) && (applicationDataPath.equals(PPApplication.EXPORT_PATH)))
                                        ||
                                        ((exportedDBObj.getVersion() < 1002) && (applicationDataPath.equals(GlobalGUIRoutines.REMOTE_EXPORT_PATH))))
                                    {
                                        if (columnNamesExportedDB[i].equals(KEY_DEVICE_AUTOROTATE))
                                        {
                                            // change values:
                                            // autorotate off -> rotation 0
                                            // autorotate on -> autorotate
                                            if (value.equals("1") || value.equals("3"))
                                                value = "1";
                                            if (value.equals("2"))
                                                value = "2";
                                        }
                                    }
                                    if (exportedDBObj.getVersion() < 1156)
                                    {
                                        if (columnNamesExportedDB[i].equals(KEY_DEVICE_BRIGHTNESS))
                                        {
                                            if (android.os.Build.VERSION.SDK_INT >= 21) // for Android 5.0: adaptive brightness
                                            {
                                                //value|noChange|automatic|defaultProfile
                                                String[] splits = value.split("\\|");

                                                if (splits[2].equals("1")) // automatic is set
                                                {
                                                    // hm, found brightness values without default profile :-/
                                                    if (splits.length == 4)
                                                        value = Profile.BRIGHTNESS_ADAPTIVE_BRIGHTNESS_NOT_SET+"|"+splits[1]+"|"+splits[2]+"|"+splits[3];
                                                    else
                                                        value = Profile.BRIGHTNESS_ADAPTIVE_BRIGHTNESS_NOT_SET+"|"+splits[1]+"|"+splits[2]+"|0";
                                                }
                                            }
                                        }
                                    }
                                    if (exportedDBObj.getVersion() < 1165)
                                    {
                                        if (columnNamesExportedDB[i].equals(KEY_DEVICE_BRIGHTNESS))
                                        {
                                            //value|noChange|automatic|defaultProfile
                                            String[] splits = value.split("\\|");

                                            int perc = Integer.parseInt(splits[0]);
                                            perc = (int)Profile.convertBrightnessToPercents(perc, 255, 1);

                                            // hm, found brightness values without default profile :-/
                                            if (splits.length == 4)
                                                value = perc+"|"+splits[1]+"|"+splits[2]+"|"+splits[3];
                                            else
                                                value = perc+"|"+splits[1]+"|"+splits[2]+"|0";
                                        }
                                    }
                                    if (exportedDBObj.getVersion() < 1175)
                                    {
                                        if (columnNamesExportedDB[i].equals(KEY_DEVICE_BRIGHTNESS))
                                        {
                                            if (android.os.Build.VERSION.SDK_INT < 21)
                                            {
                                                //value|noChange|automatic|defaultProfile
                                                String[] splits = value.split("\\|");

                                                if (splits[2].equals("1")) // automatic is set
                                                {
                                                    int perc = 50;

                                                    // hm, found brightness values without default profile :-/
                                                    if (splits.length == 4)
                                                        value = perc+"|"+splits[1]+"|"+splits[2]+"|"+splits[3];
                                                    else
                                                        value = perc+"|"+splits[1]+"|"+splits[2]+"|0";
                                                }
                                            }
                                        }
                                    }
                                    if (applicationDataPath.equals(GlobalGUIRoutines.REMOTE_EXPORT_PATH))
                                    {
                                        if (columnNamesExportedDB[i].equals(KEY_AFTER_DURATION_DO))
                                        {
                                            // in PhoneProfilesPlus value=3 is restart events
                                            if (value.equals("3"))
                                                value = "1";
                                        }
                                    }

                                    values.put(columnNamesExportedDB[i], value);
                                }
                                if (columnNamesExportedDB[i].equals(KEY_DURATION))
                                    duration = cursorExportedDB.getInt(i);
                                if (columnNamesExportedDB[i].equals(KEY_VOLUME_ZEN_MODE))
                                    zenMode = cursorExportedDB.getInt(i);

                            }

                            // for non existent fields set default value
                            if (exportedDBObj.getVersion() < 19)
                            {
                                values.put(KEY_DEVICE_MOBILE_DATA, 0);
                            }
                            if (exportedDBObj.getVersion() < 20)
                            {
                                values.put(KEY_DEVICE_MOBILE_DATA_PREFS, 0);
                            }
                            if (exportedDBObj.getVersion() < 21)
                            {
                                values.put(KEY_DEVICE_GPS, 0);
                            }
                            if (exportedDBObj.getVersion() < 22)
                            {
                                values.put(KEY_DEVICE_RUN_APPLICATION_CHANGE, 0);
                                values.put(KEY_DEVICE_RUN_APPLICATION_PACKAGE_NAME, "-");
                            }
                            if (exportedDBObj.getVersion() < 24)
                            {
                                values.put(KEY_DEVICE_AUTOSYNC, 0);
                            }
                            if (exportedDBObj.getVersion() < 31)
                            {
                                values.put(KEY_DEVICE_AUTOSYNC, 0);
                            }
                            if (((exportedDBObj.getVersion() < 51) && (applicationDataPath.equals(PPApplication.EXPORT_PATH)))
                                ||
                                ((exportedDBObj.getVersion() < 1001) && (applicationDataPath.equals(GlobalGUIRoutines.REMOTE_EXPORT_PATH))))
                            {
                                values.put(KEY_DEVICE_AUTOROTATE, 0);
                            }
                            if (exportedDBObj.getVersion() < 1015)
                            {
                                values.put(KEY_DEVICE_LOCATION_SERVICE_PREFS, 0);
                            }
                            if (exportedDBObj.getVersion() < 1020)
                            {
                                values.put(KEY_VOLUME_SPEAKER_PHONE, 0);
                            }
                            if (exportedDBObj.getVersion() < 1035)
                            {
                                values.put(KEY_DEVICE_NFC, 0);
                            }
                            if (exportedDBObj.getVersion() < 1120)
                            {
                                values.put(KEY_DURATION, 0);
                                values.put(KEY_AFTER_DURATION_DO, Profile.AFTERDURATIONDO_UNDOPROFILE);
                            }
                            if (exportedDBObj.getVersion() < 1150)
                            {
                                values.put(KEY_VOLUME_ZEN_MODE, 0);
                            }
                            if (exportedDBObj.getVersion() < 1160)
                            {
                                values.put(KEY_DEVICE_KEYGUARD, 0);
                            }
                            if (exportedDBObj.getVersion() < 1180)
                            {
                                values.put(KEY_VIBRATE_ON_TOUCH, 0);
                            }
                            if (exportedDBObj.getVersion() < 1190)
                            {
                                values.put(KEY_DEVICE_WIFI_AP, 0);
                            }
                            if (exportedDBObj.getVersion() < 1200)
                            {
                                values.put(KEY_DURATION, duration * 60); // conversion to seconds
                            }
                            if (exportedDBObj.getVersion() < 1210)
                            {
                                if ((zenMode == 6) && (android.os.Build.VERSION.SDK_INT < 23))
                                    values.put(KEY_VOLUME_ZEN_MODE, 3); // Alarms only zen mode is supported from Android 6.0
                            }
                            if (exportedDBObj.getVersion() < 1220)
                            {
                                values.put(KEY_DEVICE_POWER_SAVE_MODE, 0);
                            }

                            if (exportedDBObj.getVersion() < 1230)
                            {
                                values.put(KEY_SHOW_DURATION_BUTTON, 0);
                            }

                            if (exportedDBObj.getVersion() < 1240)
                            {
                                values.put(KEY_ASK_FOR_DURATION, 0);
                            }

                            if (exportedDBObj.getVersion() < 1250)
                            {
                                values.put(KEY_DEVICE_NETWORK_TYPE, 0);
                            }

                            if (exportedDBObj.getVersion() < 1260)
                            {
                                values.put(KEY_NOTIFICATION_LED, 0);
                            }

                            if (exportedDBObj.getVersion() < 1270)
                            {
                                values.put(KEY_VIBRATE_WHEN_RINGING, 0);
                            }

                            if (exportedDBObj.getVersion() < 1290)
                            {
                                values.put(KEY_DEVICE_WALLPAPER_FOR, 0);
                            }

                            if (exportedDBObj.getVersion() < 1300)
                            {
                                values.put(KEY_HIDE_STATUS_BAR_ICON, 0);
                            }

                            if (exportedDBObj.getVersion() < 1310)
                            {
                                values.put(KEY_LOCK_DEVICE, 0);
                            }

                            if (exportedDBObj.getVersion() < 1320) {
                                values.put(KEY_DEVICE_CONNECT_TO_SSID, Profile.CONNECTTOSSID_JUSTANY);
                            }

                            // Inserting Row do db z SQLiteOpenHelper
                            db.insert(TABLE_PROFILES, null, values);
                        } while (cursorExportedDB.moveToNext());
                    }

                    cursorExportedDB.close();
                    cursorImportDB.close();

                    db.execSQL("DELETE FROM " + TABLE_SHORTCUTS);

                    if (tableExists(TABLE_SHORTCUTS, exportedDBObj)) {
                        // cusor for events exportedDB
                        cursorExportedDB = exportedDBObj.rawQuery("SELECT * FROM " + TABLE_SHORTCUTS, null);
                        columnNamesExportedDB = cursorExportedDB.getColumnNames();

                        // cursor for profiles of destination db
                        cursorImportDB = db.rawQuery("SELECT * FROM " + TABLE_SHORTCUTS, null);

                        if (cursorExportedDB.moveToFirst()) {
                            do {
                                values.clear();
                                for (int i = 0; i < columnNamesExportedDB.length; i++) {
                                    // put only when columnNamesExportedDB[i] exists in cursorImportDB
                                    if (cursorImportDB.getColumnIndex(columnNamesExportedDB[i]) != -1) {
                                        values.put(columnNamesExportedDB[i], cursorExportedDB.getString(i));
                                    }
                                }

                                // for non existent fields set default value
                                /*if (exportedDBObj.getVersion() < 1480) {
                                    values.put(KEY_G_CHECKED, 0);
                                }
                                if (exportedDBObj.getVersion() < 1510) {
                                    values.put(KEY_G_TRANSITION, 0);
                                }*/

                                // Inserting Row do db z SQLiteOpenHelper
                                db.insert(TABLE_SHORTCUTS, null, values);

                            } while (cursorExportedDB.moveToNext());
                        }

                        cursorExportedDB.close();
                        cursorImportDB.close();
                    }

                    db.setTransactionSuccessful();

                    ret = 1;
                }
                finally {
                    db.endTransaction();
                    if ((cursorExportedDB != null) && (!cursorExportedDB.isClosed()))
                        cursorExportedDB.close();
                    if ((cursorImportDB != null) && (!cursorImportDB.isClosed()))
                        cursorImportDB.close();
                    //db.close();
                }
            }
        } catch (Exception e) {
            Log.e("DatabaseHandler.importDB", e.toString());
        }

        return ret;
    }

    @SuppressWarnings("resource")
    int exportDB()
    {
        int ret = 0;

        try {

            File sd = Environment.getExternalStorageDirectory();
            File data = Environment.getDataDirectory();

            File dataDB = new File(data, GlobalGUIRoutines.DB_FILEPATH + "/" + DATABASE_NAME);
            File exportedDB = new File(sd, PPApplication.EXPORT_PATH + "/" + EXPORT_DBFILENAME);

            if (dataDB.exists())
            {
                // close db
                close();

                File exportDir = new File(sd, PPApplication.EXPORT_PATH);
                if (!(exportDir.exists() && exportDir.isDirectory()))
                {
                    //noinspection ResultOfMethodCallIgnored
                    exportDir.mkdirs();
                }

                FileChannel src = new FileInputStream(dataDB).getChannel();
                FileChannel dst = new FileOutputStream(exportedDB).getChannel();
                dst.transferFrom(src, 0, src.size());
                src.close();
                dst.close();

                ret = 1;
            }
        } catch (Exception e) {
            Log.e("DatabaseHandler.exportDB", e.toString());
        }

        return ret;
    }

}
