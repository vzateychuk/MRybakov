package ru.vzateychuk.mr2.view;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.List;

import ru.vzateychuk.mr2.R;
import ru.vzateychuk.mr2.common.GenericActivity;
import ru.vzateychuk.mr2.common.OnFragmentInteractionListener;
import ru.vzateychuk.mr2.model.Article;
import ru.vzateychuk.mr2.model.MyFilter;
import ru.vzateychuk.mr2.ops.AlarmNotificationReceiver;
import ru.vzateychuk.mr2.ops.ArticlesOps;

/**
* This class extends GenericActivity that provides a framework for automatically
* handling runtime configuration changes of a ArticleOps object, which plays the role
* of the "Presenter" in the MVP pattern.
* NavigationView.OnNavigationItemSelectedListener used for interaction with item in NavigationView
* OnFragmentInteractionListener uses for callback when user press the item in HeadlinesFragment.ListView
 * View.OnClickListener  used when user clicks on NavigationDrawer.headerView
 * */
public class MainActivity extends GenericActivity<ArticlesOps>
        implements NavigationView.OnNavigationItemSelectedListener,
            DialogOptionSelection.NoticeDialogListener,
            OnFragmentInteractionListener,
            View.OnClickListener
{

    /**
    * reference for fragments used:
    * list of Articles - HeadlinesFragment
    */
    private HeadlinesFragment mHeadlinesFragment;

    // current applyFilter for displayed articles. initiated with empty values
    private MyFilter mFilter;

    // position indicates when user selected the article
    private int mPosition;
    
    private final String TAG_SELECTED_POSITION = "ru.vzateychuk.mr2.view.SELECTED_POSITION";

    // status indicates that ArticleActivity is shown on the front
    public static final int IS_ALIVE = 1;

    // this for broadcast message when the data will be downloaded by AlarmNotificationRecieiver
    public final static String TAG_ALARM_TIME_TO_CHECK_ACTION = "ru.vazateychuk.mr2.ALARM_TIME_TO_CHECK_ACTION";

    // Receives broadcast and setResult indicates that Activity is alive
    private BroadcastReceiver mRefreshReceiver;

    @SuppressLint("MissingSuperCall")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        /**
         * Invoke the special onCreate() method in GenericActivity,
         * passing in the ArticleOps class to instantiate/manage.
         * And  "this" to provide ArticleOps MainActivity instance.
         * super.onCreate(savedInstanceState);
         */
        super.onCreate(savedInstanceState,
                ArticlesOps.class);

        // Get references to the UI components.
        setContentView(R.layout.activity_main);

        // Store the reference on all UI components that holds visual
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();


        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // thereis some trouble to find the HeaderView from Navigation
        // I use the solution: http://stackoverflow.com/questions/32246360/how-to-get-view-from-drawer-header-layout-with-binding-in-activity
        View headerView = navigationView.inflateHeaderView(R.layout.nav_header_main);
        ImageView ivLogo = (ImageView) headerView.findViewById(R.id.ivLogo);


        // try open link when user click on header view
        headerView.setOnClickListener(this);


        // Setup headlines fragment if the Activity created at the first time
        if (savedInstanceState == null) {

            // setup fragments
            setupFragments();

            // open drawer when the application starts
            drawer.openDrawer(GravityCompat.START);
        } else {
            Log.d(TAG, "onCreate(), looking for a mHeadlinesFragment");

            // Restore saved instance state (on reconfiguration find current mHeadlinesFragment by Tag)
            mHeadlinesFragment = (HeadlinesFragment) getSupportFragmentManager().findFragmentByTag(HeadlinesFragment.class.getName());

            // restore selected position from savedInstance
            mPosition = savedInstanceState.getInt(TAG_SELECTED_POSITION);
        }
        // if in two_pane_mode, select previously selected article (0 by default)
        if (isInTwoPaneMode()) onSelectedListener(mPosition);

        // create new mRefreshReceiver for orderedBroadcast
        setBroadcastReciever();

    }

    // hide drawer on back pressed
    @Override
    public void onBackPressed() {
        Log.d(TAG, "onBackPressed()");

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    /**
    * This method called when user clicks on item on Navigation bar
    * */
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        Log.d(TAG, "onNavigationItemSelected()");

        // Handle navigation view item clicks here.
        int id = item.getItemId();

        String title;

        switch (id) {
            // display articles
            case R.id.nav_articles:
                // toast message to be displayed
                title = getString(R.string.action_article);
                // refresh data on headlines
                refreshData(id, "", title);
                break;

            // display schedules
            case R.id.nav_schedule:
                // toast message to be displayed
                title = getString(R.string.action_schedule);
                // refresh data on headlines
                refreshData(id, "", title);
                break;

            // display our team page
            case R.id.nav_about:
                // toast message to be displayed
                title = getString(R.string.action_about);
                // refresh data on headlines
                refreshData(id, "", title);
                break;

            // display with filter by author
            case R.id.nav_by_author:
                // toast message to be displayed
                title = getString(R.string.action_article)
                        + " "
                        + getString(R.string.action_by_author).toLowerCase();
                // display dialog for selection appropriate filter value
                showDialog(title,
                        getResources().getStringArray(R.array.array_authors_options),
                        getResources().getStringArray(R.array.array_authors_options));
                break;

            // display with filter by category
            case R.id.nav_by_category:
                title = getString(R.string.action_article)
                        + " "
                        + getString(R.string.action_by_category).toLowerCase();
                // display dialog for selection appropriate filter value
                showDialog(title,
                        getResources().getStringArray(R.array.array_category_options),
                        getResources().getStringArray(R.array.array_category_tags));
                break;

            // start facebook page when user clicks
            case R.id.nav_facebook:
                String sUrl = this.getString(R.string.http_facebook);
                Uri address = Uri.parse(sUrl);
                Intent openlink = new Intent(Intent.ACTION_VIEW, address);
                startActivity(openlink);
                break;

            // create and send new message (email/sms)
            case R.id.nav_send:
                makeMessage();
                break;
        }


        // hiding the drawer
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

        // replace fragment by headlines in one_pane_mode if already filled by ArticleView
        if (!isInTwoPaneMode()) {

            // check if the current displayed fragment is instance of ArticleFramgent, just replace it
            Fragment fragment =  getSupportFragmentManager().findFragmentById(R.id.fragment_container_headline);
            if (fragment instanceof ArticleFragment) {
                Log.d(TAG, "onNavigationItemSelected(): fragment instanceof ArticleFragment, => onBackPressed()");
                // this.onBackPressed();
                getSupportFragmentManager().beginTransaction().replace(
                        R.id.fragment_container_headline,
                        mHeadlinesFragment,
                        mHeadlinesFragment.getClass().getName()).commit();
            }
        }

        return true;
    }

    /**
    // The user selected the headline of an article from the HeadlinesFragment
    // Do something here to display that article
    // method implements OnFragmentInteractionListener.onSelectedListener interface
     */
    @Override
    public void onSelectedListener(int position) {
        Log.d(TAG, ".onSelectedListener(): position=" + position);

        // get article from Ops
        Article article = getOps().getArticle(position);

        // if there is no articles, just exit
        if (article == null) return;

        // store the position for future use
        mPosition = position;

        // try to find the fragement_container for article (valid for two pane mode)
        ArticleFragment articleFrag = (ArticleFragment)
                getSupportFragmentManager().findFragmentById(R.id.fragment_container_article);

        if (articleFrag != null) {
            // If article frag is available, we're in two-pane layout...
            // Call a method in the ArticleFragment to update its content
            articleFrag.updateArticleView(article);
        } else {
            // Otherwise, we're in the one-pane layout and must swap frags...
            // Create fragment and give it an argument for the selected article
            articleFrag = ArticleFragment.newInstance(article);

            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container_headline,
                    articleFrag,
                    articleFrag.getClass().getName());

            // add fragment to back stack
            transaction.addToBackStack(articleFrag.getClass().getName());
            transaction.commit();
        }
    }


    /**
     * Display the ArticleList to the user.
     * @param results List of Articles to display.
     */
    public void updateHeadlinesView(List<Article> results,
                                    String errorMessage) {
        Log.d(TAG, "updateHeadlinesView()");

        if (results == null || results.size() == 0)
            Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
        else {
            Log.d(TAG, "updateHeadlinesView() with number of Articles = " + results.size());

            // if headlines not null, display articles on headlines fragment
            if (mHeadlinesFragment!=null) {
                mPosition = 0;
                mHeadlinesFragment.displayArticles(results);
                // if in two_pane_mode, select top article
                if (isInTwoPaneMode()) onSelectedListener(mPosition);
            }
        }
    }

    /**
     * The dialog fragment receives a reference to this Activity through the
     * Fragment.onAttach() callback, which it uses to call the following methods
     * defined by the DialogOptionSelection.NoticeDialogFragment.NoticeDialogListener interface
     * (invoked from  when user click on item)
     * @param tag - tag selected for filter
     * */
    @Override
    public void onDialogItemClick(String tag) {
        Log.d(TAG, "onDialogItemClick(), tag=" + tag);

        // build message to be displayed
        String message = getString(R.string.warning_call_in_progress)
                + " "
                + getString(R.string.action_article).toLowerCase()
                + " "
                + tag;

        // refresh headlines according tag provided
        refreshData(R.id.nav_articles, tag, message);
    }

    // Register the BroadcastReceiver
    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, ".onResume(); Register the mRefreshReceiver with filter: ");
        // Register the BroadcastReceiver to receive a DATA_REFRESHED_ACTION broadcast
        IntentFilter filter = new IntentFilter(AlarmNotificationReceiver.ALARM_TO_CHECK_NEW_DATA);
        if (null != mRefreshReceiver) registerReceiver(mRefreshReceiver, filter);
    }

    // Unregister the BroadcastReceiver if it has been registered
    @Override
    protected void onPause() {
        Log.d(TAG, ".onPause(); Unregister the mRefreshReceiver");

        // Note: check that mRefreshReceiver is not null before attempting to
        if (null != mRefreshReceiver) unregisterReceiver(mRefreshReceiver);

        super.onPause();
    }

    @Override
    protected void onDestroy() {
        mRefreshReceiver = null;
        super.onDestroy();
    }


    /**************************************************************************************
     * ***********************        Private area            *****************************
     **************************************************************************************/
    // Creating new email message to request
    private void makeMessage() {
        Log.d(TAG, ".makeMessage(): making new message");
        getOps().makeMessage(this);
    }

    // If there is no fragment_container_headline (one pane), then the application is in two-pane mode
    private boolean isInTwoPaneMode() {
        // return findViewById(R.id.fragment_container_headline) == null;
        return getResources().getBoolean(R.bool.has_two_panes);
    }

    // Save instance state for reconfiguration
    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        // save the position of item selected by user
        savedInstanceState.putInt(TAG_SELECTED_POSITION, mPosition);

        Log.d(TAG, ".onSaveInstanceState()");
    }


    private void setupFragments() {
        Log.d(TAG, ".setupFragments()");
        // Make new HeadlineFragment which displays List of Articles
        mHeadlinesFragment = HeadlinesFragment.newInstance();
        getSupportFragmentManager().beginTransaction().add(R.id.fragment_container_headline,
                mHeadlinesFragment,
                mHeadlinesFragment.getClass().getName()).commit();

        // Make new ArticleFragment in two pane mode
        if (isInTwoPaneMode()) {
            ArticleFragment articleFrag = ArticleFragment.newInstance(null);
            getSupportFragmentManager().beginTransaction().add(R.id.fragment_container_article,
                    articleFrag,
                    ArticleFragment.class.getName()).commit();

        }
    }


    /**
     * showDialog() take care of adding the fragment in a transaction.
     * We also want to remove any currently showing dialog,
     * so make our own transaction and take care of that here.
     * example depicted: http://developer.android.com/intl/ru/reference/android/app/DialogFragment.html
     *
     * @param title   - dialog title
     * @param options - dialog option to be selected
     * @param values  - values to be returned by dialog
     *
      */
    private void showDialog(
            String title,
            String[] options,
            String[] values) {

        Log.d(TAG, "showDialog(); title="+title);

        // Create and show the dialog.
        DialogOptionSelection newFragment = DialogOptionSelection.newInstance(
                this.getApplicationContext(),
                title,
                options,
                values);

        newFragment.show(getSupportFragmentManager(), TAG_SELECTED_POSITION);
    }

    /**
     * Make new Filter and send "refresh data" request to underlined presentation layer
     *
     * @param id        - type_id for data selection
     * @param tag       - string_tag to be used for selection by tag
     *
     * */
    private void refreshData(int id, String tag, String message){
        Log.d(TAG, "refreshData(), id=" + id + ", tag=" + tag);
        // Create and show toast message first
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        // make new applyFilter and display data with new applyFilter
        mFilter = MyFilter.newInstance(id, tag);
        // start async reload data (will be displayed by completion)
        getOps().refreshData(mFilter);
    }

    // creates and initializes Receiver. Receiver lets sender know that the Intent was received by setting result code to IS_ALIVE
    // the Receiver will be registered in onResume()
    private void setBroadcastReciever() {
        Log.d(TAG, ".setBroadcastReciever()");

        // Set up a BroadcastReceiver to receive an Intent when download finishes.
        mRefreshReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d(TAG, ".onReceive(), intent.action=" + intent.getAction());

                // Check to make sure this is an ordered broadcast of our intent
                if (isOrderedBroadcast()) {
                    // by setting result code to IS_ALIVE will let sender know that the Intent was received
                    setResultCode(IS_ALIVE);
                    Log.d(TAG, ".onReceive(); intent.setResultCode(" + IS_ALIVE + ")");
                }
            }
        };
    }

    /***
     * implements View.OnClickListener()
     * used when user clicks on NavigationDrawer
     * */
    @Override
    public void onClick(View v) {
        String strUrl = "http://" + this.getString(R.string.web_site);
        Uri address = Uri.parse(strUrl);
        Intent openlink = new Intent(Intent.ACTION_VIEW, address);
        startActivity(openlink);
    }
}
