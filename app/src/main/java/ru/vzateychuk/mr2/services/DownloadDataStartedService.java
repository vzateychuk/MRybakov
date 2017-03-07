package ru.vzateychuk.mr2.services;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.squareup.okhttp.OkHttpClient;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import retrofit.Call;
import retrofit.GsonConverterFactory;
import retrofit.Retrofit;
import ru.vzateychuk.mr2.R;
import ru.vzateychuk.mr2.common.Utils;
import ru.vzateychuk.mr2.model.Article;
import ru.vzateychuk.mr2.model.DBHelper;
import ru.vzateychuk.mr2.ops.AlarmNotificationReceiver;
import ru.vzateychuk.mr2.retrofit.IWebServiceAPI;
import ru.vzateychuk.mr2.view.MainActivity;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * Used this for downloading data from web and save data to local storage
 * Created by vez on 05.11.15.
 */
public class DownloadDataStartedService extends IntentService {

    private final static String TAG = DownloadDataStartedService.class.getSimpleName();

    // IntentService can perform, e.g. ACTION_DOWNLOAD
    private static final String ACTION_DOWNLOAD = "ru.vzateychuk.mr2.services.action.downloadandsave";

    // HttpClient download timeout
    private static final int HTTP_CLIENT_READ_TIMEOUT = 5 * 60 * 1000;

    // name of income timestamp parameter
    private static final String EXTRA_TIMESTAMP = "ru.vzateychuk.mr2.services.timestamp";

    // Notification ID to allow for future notification updates
    private static final int MY_NOTIFICATION_ID = 1001;
    // Vibrate pattern used when notification arrived
    private final long[] mVibratePattern = { 0, 200, 200, 300 };

    //  default epoch timestamp 1357002061 = 01/01/2013
    public static final long TIMESTAMP_DEFAULT = 1357002061000l;


    /**
     * A constructor is required, and must call the super IntentService(String)
     * constructor with a name for the worker thread.
     */
    public DownloadDataStartedService() {
        super(TAG);
        Log.d(TAG, "service created");
    }

    /**
     * Make intent to starts this service to perform action downloadArticlesFromWeb with the given parameter timestamp. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static Intent makeIntent(Context context,
                                    long timestamp) {
        Log.d(TAG, "makeIntent(): with timestamp=" + timestamp);

        Intent intent = new Intent(context, DownloadDataStartedService.class);
        intent.setAction(ACTION_DOWNLOAD);
        intent.putExtra(EXTRA_TIMESTAMP, timestamp);
        return intent;
    }


    /**
     * The IntentService calls this method from the default worker thread with
     * the intent that started the service. When this method returns, IntentService
     * stops the service, as appropriate.
     */
    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "onHandleIntent()");
        // check if intent with appropriate action
        if (intent != null) {
            final String action = intent.getAction();

            if (ACTION_DOWNLOAD.equals(action)) {
                // get long_timestamp from Intent
                Long ltimestamp = intent.getLongExtra(EXTRA_TIMESTAMP, TIMESTAMP_DEFAULT);
                // convert to string representation
                String stimestamp = convertFromLongToString(ltimestamp);
                // download articles from web
                List<Article> articles = downloadArticlesFromWeb(stimestamp);
                // if there is any new articles downloaded, save to the database and sent broadcast message: "Hey, new data just arrived!"
                if (articles.size() > 0) {
                    DBHelper dbHelper = new DBHelper(getApplicationContext());
                    // update article list in database
                    dbHelper.insertOrUpdateData(articles);

                    // download images and text data from files
                    String url;
                    for (Article article: articles) {
                        String id = article.getID();

                        // download image by url
                        url = article.getDrawing();
                        if (url.trim() != "")
                            Utils.downloadUsingStream(
                                    this.getApplicationContext(),
                                    url,
                                    Utils.TYPE_PICTURE,
                                    article.getID());

                        // download file by url
                        url = article.getContent();
                        if (url.trim() != "")
                            Utils.downloadUsingStream(
                                    this.getApplicationContext(),
                                    url,
                                    Utils.TYPE_DOCUMENT,
                                    article.getID());
                    }


                    /**
                     * Create and send broadcast message with flag that new data just arrived.
                     * This notifies the user that the new Articles is arrived by sends
                     * an ordered broadcast to the BroadcastReceiver in ActicleOps.class
                     */
                    broadcastDownloadCompeted(articles);

                }
            }
        }
    }

    /**
     * Convert timestamp from long to string with divide on 1,000
     *
     * @param ltimestamp - timestamp to be converted
     * @return string representation
     */
    private String convertFromLongToString(long ltimestamp) {
        String result = Long.toString(ltimestamp / 1000);
        return result;
    }


    /**
     * Handle action downloadArticlesFromWeb in the provided background thread with the provided parameter timestamp.
     */
    private List<Article> downloadArticlesFromWeb(String timestamp) {
        Log.d(TAG, "downloadArticlesFromWeb(); timestamp=" + timestamp);

        // create client with big timeout
        OkHttpClient okHttpClient = new OkHttpClient();
        okHttpClient.setReadTimeout(HTTP_CLIENT_READ_TIMEOUT, TimeUnit.MILLISECONDS);  // timeout = 300 sec


        /**
         * This is how we turn the IWebServiceAPI into an object that
         * will translate method calls on the IWebServiceAPI's interface
         * methods into HTTP requests on the server. Parameters / return
         * values are being marshalled to/from JSON.
         * @returns List<Article> or null if download aborted
         */
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(IWebServiceAPI.base_WebAPI_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        // The Retrofit class generates WebService (an implementation of the IWebServiceAPI interface).
        IWebServiceAPI webService = retrofit.create(IWebServiceAPI.class);

        /**
         * Send the GET request to the web service using Retrofit to add the article.
         * Notice how Retrofit provides a nice strongly-typed interface to our
         * HTTP-accessible service that is much cleaner than muddling around
         * with URL query parameters, etc.
         */
        Call<List<Article>> call = webService.listArticles(timestamp);

        List<Article> articles = null;
        try {
            // get downloaded data from retrofit call
            articles = call.execute().body();

            // log result to Android logger
            if (articles != null)
                Log.d(TAG, "articles loaded=" + articles.size());
            else
                Log.d(TAG, "NO articles loaded");

        } catch (IOException e) {
            Log.e(TAG, "IOException during call.execute().body(); e=", e);
        }
        return articles;
    }


    /** notifies the user that the additional downloads are complete by sends
     * an ordered broadcast back to the BroadcastReceiver in
     * ArticlesActivity to determine whether the notification is necessary.
     */

    private void broadcastDownloadCompeted(List<Article> articles) {
        Log.d(TAG, ".broadcastDownloadCompeted()");

        Context context = this.getApplicationContext();

        // Sends an ordered broadcast indicates that data loaded to determine whether MainActivity is active and in the foreground.
        // In same time creates a new BroadcastReceiver to receive a result indicating the state of MainActivity (IS_LIVE?)
        // The Action for this broadcast Intent is ALARM_TO_CHECK_NEW_DATA

        // first create intent with recieved acticles id
        Intent intent = new Intent(AlarmNotificationReceiver.ALARM_TO_CHECK_NEW_DATA);
        intent.putExtra("NEW_ARTICLES", "asdfasdf");
        context.sendOrderedBroadcast(
                            intent,
                            null,
                            new BroadcastReceiver() {
                                @Override
                                public void onReceive(Context context, Intent intent) {
                                    Log.d(TAG, "OrderedBroadcast.onReceive(); result_code=" + getResultCode());

                                    // Check whether or not the MainActivity  received the broadcast
                                    // if not, create Notification
                                    if (getResultCode() != MainActivity.IS_ALIVE)
                                        DownloadDataStartedService.this.createNotification(context);
                                }
                            },
                null,
                0,
                null,
                null);
    }

    /**
     * Creates and send new System Notification
     * Uses when new data arrived and MainActivity is not started
     * */
    private void createNotification(Context context) {
        // Log occurrence of notify() call
        Log.d(TAG, ".createNotification()");

        // setup the notification text
        final String notifTitle = context.getResources().getString(R.string.notification_title);
        final String notifTicker = context.getResources().getString(R.string.notification_title);
        final String notifText = context.getResources().getString(R.string.notification_text)
                + " "
                + context.getResources().getString(R.string.app_name);

        // Notification Action Elements
        Intent mNotificationIntent;
        PendingIntent mPendingIntent;

        // The Intent to be used when the user clicks on the Notification View
        mNotificationIntent = new Intent(context, MainActivity.class);

        // The PendingIntent that wraps the underlying Intent
        mPendingIntent = PendingIntent.getActivity(
                context,
                0,
                mNotificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        // Build the Notification with pending intent to start MainActivity.class
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context)
                .setTicker(notifTicker)
                .setSmallIcon(R.mipmap.ic_rybakov)
                .setAutoCancel(true)
                .setContentTitle(notifTitle)
                .setVibrate(mVibratePattern)
                .setContentText(notifText)
                .setContentIntent(mPendingIntent);

        // Get the NotificationManager
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        Notification notification = notificationBuilder.build();

        // Pass the Notification to the NotificationManager:
        mNotificationManager.notify(MY_NOTIFICATION_ID, notification);
    }

}
