package ru.vzateychuk.mr2.ops;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import ru.vzateychuk.mr2.model.DBHelper;
import ru.vzateychuk.mr2.services.DownloadDataStartedService;

/**
 * AlarmNotificationReciever recieve alarm from Android to trigger data load
 * New Alarm is registered in InitialActivity
  */
public class AlarmNotificationReceiver extends BroadcastReceiver {
    /**
     * Debugging tag used by the Android logger.
     */
    final static String TAG = AlarmNotificationReceiver.class.getSimpleName();

    // used when create broadcast
    public static final String ALARM_TO_CHECK_NEW_DATA = " ru.vzateychuk.ALARM_TO_CHECK_NEW_DATA";

    /**
     * This method is called when the AlarmNotificationReciever got an Alarm_Intent.
      */
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, ".onReceive(), intent_action=" + intent.getAction());

        // check if there is not appropriate alarm, just return
        if (intent.getAction() != ALARM_TO_CHECK_NEW_DATA) return;

        // get maximum timestamp value from Articles list. increase on one sec to avoid last data download
        DBHelper dbHelper = new DBHelper(context);
        long max_timestamp = dbHelper.getMaximumTimestamp() + 1000;

        // run download_service in order to download datat from web service and save to database
        Intent intent_service = DownloadDataStartedService.makeIntent(context, max_timestamp);
        context.startService(intent_service);
    }
}
