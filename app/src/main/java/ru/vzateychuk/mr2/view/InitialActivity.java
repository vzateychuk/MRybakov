package ru.vzateychuk.mr2.view;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.database.sqlite.SQLiteException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
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

    private static final long INITIAL_ALARM_DELAY = 5 * 60 * 1000;
    private static final long INTERVAL_ALARM_DELAY = AlarmManager.INTERVAL_HOUR;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_initial);
        progressDialog = Utils.showProgressDialog( this, getString(R.string.warning_call_in_progress) );
        new DataInitializerTask().execute((Void) null);
        setRepeatingAlarm();
    }

    /**
     * Starts main activity when application is ready
     */
    private void startMainActivity() {
        Log.d(TAG, ".startMainActivity()");
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
                .setNegativeButton(
                        getString(R.string.initial_dialog_bb_cancel),
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

        AlarmManager mAlarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        // Create an Intent to broadcast to the AlarmNotificationReceiver
        Intent mNotificationReceiverIntent = new Intent(this, AlarmNotificationReceiver.class);
        mNotificationReceiverIntent.setAction( AlarmNotificationReceiver.ALARM_TO_CHECK_NEW_DATA );

        // Create an PendingIntent that holds the NotificationReceiverIntent
        PendingIntent mNotifReceiverPendingIntent = PendingIntent.getBroadcast(
                this,
                0,
                mNotificationReceiverIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        // Set repeating alarm for data load (received by Alarm Notification manager)
        mAlarmManager.setRepeating(
                AlarmManager.ELAPSED_REALTIME,
                ( SystemClock.elapsedRealtime() + INITIAL_ALARM_DELAY ),
                INTERVAL_ALARM_DELAY,
                mNotifReceiverPendingIntent);
    }

    /**
     * This class for background tasks performed during startup (data copy from assets)
     * */
    private class DataInitializerTask extends AsyncTask<Void, Void, Boolean>
    {
        /**
         * Background method initiated data by
         * copy from asset to the external storage: database, images, texts.html
         *
         * @return true if data loaded successfully
         */
        @Override
        protected Boolean doInBackground(Void... params)
        {
            Log.d(TAG, ".doInBackground()");
            // check if external storage writable. Return false otherwise
            if ( !Utils.isExternalStorageWritable() ) {
                Toast.makeText(getApplicationContext(), R.string.no_external_card_installed, Toast.LENGTH_LONG).show();
                return false;
            }

            try {
                // Copy database, images, text from asset to local storage  if not exits
                Utils.copyAssets(getApplicationContext(), Utils.TYPE_DATABASE, false);
                Utils.copyAssets(getApplicationContext(), Utils.TYPE_PICTURE, false);
                Utils.copyAssets(getApplicationContext(), Utils.TYPE_DOCUMENT, false);
            } catch (SQLiteException ex) {
                Log.e(TAG, ".doInBackground(), SQLiteException: ", ex);
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean result)
        {
            super.onPostExecute(result);
            Log.d(TAG, ".onPostExecute()");

            progressDialog.dismiss();
            if (result) {
                InitialActivity.this.finish();
                startMainActivity();
            } else {
                showInitErrorDialog(getString(R.string.initialization_error_text));
            }
        }
    }

}
