package ru.vzateychuk.mr2.ops;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ru.vzateychuk.mr2.R;
import ru.vzateychuk.mr2.common.GenericAsyncTask;
import ru.vzateychuk.mr2.common.IConfigurableOps;
import ru.vzateychuk.mr2.common.IGenericAsyncTaskOps;
import ru.vzateychuk.mr2.common.Utils;
import ru.vzateychuk.mr2.model.Article;
import ru.vzateychuk.mr2.model.DBHelper;
import ru.vzateychuk.mr2.model.MyFilter;
import ru.vzateychuk.mr2.services.DownloadDataStartedService;
import ru.vzateychuk.mr2.view.HeadlinesFragment;
import ru.vzateychuk.mr2.view.MainActivity;

/**
 * This class implements all the Articles-related operations defined in
 * the ArticlesOps interface.
 * Created by vez on 3.11.15.
 */
public class ArticlesOps implements IConfigurableOps,
        IGenericAsyncTaskOps<MyFilter, Void, List<Article>> {

    /**
     * Debugging tag used by the Android logger.
     */
    private final String TAG = getClass().getSimpleName();

    /**
     * Used to store reference to MainActivity and Fragments
     * WeakReference to enable garbage collection.
     */
    private WeakReference<MainActivity> mActivity;
    private WeakReference<HeadlinesFragment> mHeadlinesFragment;

    /**
     * Сache consists List<Articles> to display. used to
     * re-populate the UI after a runtime configuration change
     */
     private List<Article> mList = new ArrayList<Article>();

    /**
     * Keeps track of whether a call is already in progress and
     * ignores subsequent calls until the first call is done.
     */
    // private boolean mCallInProgress;

    /**
     * The GenericAsyncTask used to expand an acronym in a background
     * thread via the Acronym web service.
     */
    private GenericAsyncTask<MyFilter,
                            Void,
                            List<Article>,
                            ArticlesOps> mAsyncTask;


    /**
     * display progress dialog during applyFilter data.
     */
    // private ProgressDialog mProgressDialog;

    /**
     * Default constructor that's needed by the GenericActivity framework.
     */
    public ArticlesOps() {
    }

    /**
     * Called after a runtime configuration change occurs to finish
     * the initialisation steps.
     */
    public void onConfiguration(Activity activity,
                                boolean firstTimeIn) {
        final String time = firstTimeIn ? "first time" : "second+ time";
        Log.d(TAG, "onConfiguration() called the "
                + time
                + " with activity class "
                + activity.getClass());

        // (Re)set the mActivity WeakReference.
        mActivity = new WeakReference<>((MainActivity) activity);

        // refresh data first time with default filter
         if (firstTimeIn) refreshData(MyFilter.newInstance(R.id.nav_articles, ""));
    }

    /**
     * Method invoked from MainActivity when user change filter
     * */
    public void refreshData(MyFilter my_filter) {
        Log.d(TAG, "refreshData() called, filter=" + my_filter);
            // create and display progress dialog
            // mProgressDialog = Utils.showProgressDialog(mActivity.get());

            // TODO: Don't allow concurrent calls run simultaneously. не запускать загрузку данных, если предыдущая по каким то причинам еще не закончена
            // Execute the AsyncTask to read data from DB without blocking the caller.
            mAsyncTask = new GenericAsyncTask<>(this);
            mAsyncTask.execute(my_filter);
    }

    /**
     * implements IGenericAsyncTaskOps.doInBackground.
     * used for applyFilter Article dataset in background tread
     * return new List of Articles
     */
    @Override
    public List<Article> doInBackground(MyFilter my_filter) {
        Log.d(TAG, ".doInBackground()");
        return loadDataFromDBAsync(my_filter);
    }

    // this IGenericAsyncTaskOps.onPostExecute runs in main thread
    @Override
    public void onPostExecute(List<Article> records, MyFilter my_filter) {
        Log.d(TAG, "onPostExecute(): Articles=" + records.size());

        mList.clear();
        mList.addAll(records);

        // Send list of Articles to the main activity
        displayDataOnActivity();
    }

    /******************* PRIVATE PRIVATE PRIVATE AREA ************************************/
    /**
     * Initiate the asynchronous articles lookup from DB
     * Открывает БД на чтение и читает данные для отображения на главной форме. Данные сразу фильтруются согласно фильтру my_filter
     * Важное примечание. Поскольку операция может выполняться длительное время (в случае если БД пересоздается), вызыв работает в фоновом потоке
     */
    private List<Article> loadDataFromDBAsync(MyFilter my_filter) {
        Log.d(TAG, ".loadDataFromDBAsync()");

        // Create dbHelper first
        DBHelper dbHelper = new DBHelper(mActivity.get().getApplicationContext());
        // load data from DB by using DBHelper
        List<Article> records = dbHelper.loadArticlesFromDB(my_filter);
        // finally close dbHelper and exit;
        dbHelper.close();

        // finally sort the collection
        Collections.sort(records);

        return records;
    }

    // Populate the display (Update UI) if a Articles is stored in the Ops instance.
    private void displayDataOnActivity() {
        Log.d(TAG, "displayDataOnActivity()");
        mActivity.get().updateHeadlinesView(mList, "articles displayed=" + mList.size());
    }


    // Creating new email message to request
    public void makeMessage(Context context) {
        Log.d(TAG, ".makeMessage(): making new message");

        String email_contacts = context.getResources().getString(R.string.email_contacts);
        String email_subject = context.getResources().getString(R.string.email_subject);
        String email_body = context.getResources().getString(R.string.email_body);
        Intent intent = Utils.makeEmailIntent(email_contacts, email_subject, email_body);

        // start next email Activity if email application exists on device. Notification otherwise
        if (intent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(intent);
        } else {
            // create warning if there is no messager installed
            Toast.makeText(context, context.getResources().getString(R.string.email_warning), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Initiate the asynchronous articles lookup from web service
     */
    public void loadArticlesFromWebAsync(final long timestamp) {
        Log.d(TAG, "updateArticlesAsync()");
        Context context = mActivity.get().getApplicationContext();
        // make intent from factory
        Intent intent = DownloadDataStartedService.makeIntent(context, timestamp);
        context.startService(intent);
    }

    /**
     * get article by position from internal list of Articles
     * */
    public @Nullable Article getArticle(int position) {
        Article article = null;
        if (mList.size()>0) article = mList.get(position);
        return article;
    }

    /**
     *  get reference to HeadlinesFragment
     *  */
    public HeadlinesFragment getHeadlinesFragment() {
        return mHeadlinesFragment.get();
    }

    public void setHeadlinesFragment(HeadlinesFragment headlines) {
        if (headlines != null)
            mHeadlinesFragment = new WeakReference<HeadlinesFragment>(headlines);
    }

}