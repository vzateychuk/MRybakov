package ru.vzateychuk.mr2.view;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import ru.vzateychuk.mr2.R;
import ru.vzateychuk.mr2.common.LifecycleLoggingActivity;
import ru.vzateychuk.mr2.common.Utils;
import ru.vzateychuk.mr2.ops.AlarmNotificationReceiver;

/**
* This activity runs on application starts
* It's initializes data during startup
* */
public class InitialActivity extends LifecycleLoggingActivity {

    // intervals when it's trying to check new articles. First check in 2 minutes, check period =
     private static final long INITIAL_ALARM_DELAY = 5 * 60 * 1000;
     private static final long INTERVAL_ALARM_DELAY = AlarmManager.INTERVAL_HOUR;

    // dialog shows welcome message during initiation
    private ProgressDialog progressDialog;

    //  default epoch timestamp 1357002061 = 01/01/2013
    public static final long TIMESTAMP_DEFAULT = 1357002061000l;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, ".onCreate()");

        setContentView(R.layout.activity_initial);

        // show application version out from Manifest
        // showAppVersion();

        progressDialog = Utils.showProgressDialog(this, getString(R.string.warning_call_in_progress));

        // initial preparation (data copy from assets)
        new DataInitializerTask().execute((Void) null);

        // initializes AlarmManager and Alarm
        setRepeatingAlarm();

    }

    /**
     * display application version on splash screen
     * doesn't used now (version 11)
     */
    private void showAppVersion() {
        Log.d(TAG, ".showAppVersion()");
        int versionCode;

        try {
            versionCode = getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
            // versionCode = getPackageManager().getPackageInfo(this.getPackageName(), 0).versionName;

            TextView tvVersionCode = (TextView) findViewById(R.id.tvVersionCode);
            tvVersionCode.setText(R.string.version_code + "10");

        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, ".onCreate(), PackageManager.NameNotFoundException: ", e);
        }
    }

    /**
     * Creates and starts main activity when application is ready
     */
    private void startMainActivity() {
        Log.d(TAG, ".startMainActivity()");
        /*Intent intent = new Intent(this, ArticlesActivity.class);*/
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    /**
     * Display error message if something goes wrong during initial load
     * */
    private void showInitErrorDialog(String message) {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(getString(R.string.initialization_error_text))
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setMessage(message)
                .setNegativeButton(getString(R.string.initial_dialog_bb_cancel),
                        new OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                dialog.cancel();
                                InitialActivity.this.finish();
                            }
                        }).create();
        dialog.show();
    }

    /**
     * Creates and setup periodic Alarm which initiated download process
     * and check web server on new data (by class AlarmNotificationReciever)
     */
    private void setRepeatingAlarm() {
        Log.d(TAG, ".setRepeatingAlarm()");

        // alarm notification
        PendingIntent mNotifReceiverPendingIntent;

        // Get the AlarmManager Service
        AlarmManager mAlarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        // Create an Intent to broadcast to the AlarmNotificationReceiver
        Intent mNotificationReceiverIntent = new Intent(this, AlarmNotificationReceiver.class);
        mNotificationReceiverIntent.setAction(AlarmNotificationReceiver.ALARM_TO_CHECK_NEW_DATA);

        // Create an PendingIntent that holds the NotificationReceiverIntent
        mNotifReceiverPendingIntent = PendingIntent.getBroadcast(this,
                0,
                mNotificationReceiverIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        // Set repeating alarm for data load (recieved by Alarm Notification manager)
        mAlarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME,
                SystemClock.elapsedRealtime() + INITIAL_ALARM_DELAY,
                INTERVAL_ALARM_DELAY,
                mNotifReceiverPendingIntent);
    }

    /**
     * This class for background tasks performed during startup
     * */
    private class DataInitializerTask extends AsyncTask<Void, Void, Boolean> {

        /**
         * Background method initiated data by
         * copy from asset to the external storage: database, images, texts.html
         *
         * @return true if data loaded successfully
         */
        @Override
        protected Boolean doInBackground(Void... params) {
            Log.d(TAG, ".doInBackground()");

            // check if external storage writable. Return false otherwise
            if (!Utils.isExternalStorageWritable()) {
                Toast.makeText(getApplicationContext(), R.string.no_external_card_installed, Toast.LENGTH_LONG).show();
                return false;
            }

            try {
                // Copy database from asset to local storage  if not exits
                Utils.copyAssets(getApplicationContext(), Utils.TYPE_DATABASE, false);
                // Copy images from asset to local storage if not exits
                Utils.copyAssets(getApplicationContext(), Utils.TYPE_PICTURE, false);
                // Copy texts from asset to local storage if not exits
                Utils.copyAssets(getApplicationContext(), Utils.TYPE_DOCUMENT, false);

                // Select articles from database
                // DBHelper dbHelper = new DBHelper(getApplicationContext());
                // List<Article> articles = dbHelper.loadArticlesFromDB(null);

                // get maximum timestamp value from Articles list. increase on one sec to avoid last data download
                // long max_timestamp = getMaxTimestampFromArticles(articles) + 1000;
                // long max_timestamp = dbHelper.getMaximumTimestamp() + 1000;
                // Log.d(TAG, ".doInBackground(), db max_timestamp=" + max_timestamp);

                // if there is no articles copied, try to download from web and save to database
                // Intent intent = DownloadDataStartedService.makeIntent(getApplicationContext(), max_timestamp);
                // getApplicationContext().startService(intent);

            } catch (SQLiteException ex) {
                Log.e(TAG, ".doInBackground(), SQLiteException: ", ex);
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            Log.i(TAG, ".onPostExecute()");

            progressDialog.dismiss();

            if (result == true) {
                InitialActivity.this.finish();
                startMainActivity();
            } else {
                // progressDialog.dismiss();
                showInitErrorDialog(getString(R.string.initialization_error_text));
            }
        }
    }

}
