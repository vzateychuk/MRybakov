package ru.vzateychuk.mr2.view;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.ref.WeakReference;

import ru.vzateychuk.mr2.R;
import ru.vzateychuk.mr2.common.LifecycleLoggingFragment;
import ru.vzateychuk.mr2.common.Utils;
import ru.vzateychuk.mr2.model.Article;

/**
 * A fragment with a Google +1 button.
 * Use the {@link ArticleFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ArticleFragment extends LifecycleLoggingFragment {

    // store data for displayed article
    private Article mArticle;

    // store reference to the hosted activity (MainActivity). initiated in onAttach
    private WeakReference<Activity> mActivity;

    // reference to the TextView AritcleDate
    private TextView tvArticleDate;
    private TextView tvId;
    // private ImageView ivArticle;
    private TextView tvArticleTitle;
    private TextView tvArticleTags;

    private WebView vwActicle;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment ArticleFragment.
     */
    public static ArticleFragment newInstance(@Nullable Article article) {
        Log.d(ArticleFragment.class.getSimpleName(), "newInstance()");

        ArticleFragment fragment = new ArticleFragment();
        if (article != null) fragment.mArticle = article;

        return fragment;
    }

    public ArticleFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity =  new WeakReference(activity);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Fragment won't be re-created in reconfiguration
        setRetainInstance(true);

        // fragment has options menu
        setHasOptionsMenu(true);
    }

    // создание меню
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.article_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    // обработка нажатий
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d(TAG, "onOptionsItemSelected(); item=" + item.getTitle());
        makeMessage(mActivity.get());
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.article_view, container, false);

        // find by Id all visual components
        tvArticleDate = (TextView) view.findViewById(R.id.tvArticleDate);
        tvId = (TextView) view.findViewById(R.id.tvId);
        tvArticleTitle = (TextView) view.findViewById(R.id.tvArticleTitle);
        // ivArticle = (ImageView) view.findViewById(R.id.ivArticle);
        vwActicle = (WebView) view.findViewById(R.id.wvArticle);
        tvArticleTags =  (TextView) view.findViewById(R.id.tvArticleTags);

        // fill visual components by values from mArticle
        if (mArticle != null) updateArticleView(mArticle);

        return view;
    }

    /**
     * The user selected the headline of an article from the HeadlinesFragment
     * Do something here to display that article
     * */
    public void updateArticleView(@Nullable Article article) {
        Log.d(TAG, "updateArticleView(); trying to fill visual components");

        if (article != null) {
            // display timestamp in human readable format
            // tvArticleDate.setText(Utils.getDateFromTimestamp(article.getTimestamp() * 1000));
            // display Id
            // tvId.setText(article.getID());
            // tvArticleTags.setText(article.getTags());

            // display article title
            tvArticleTitle.setText(article.getTitle());
            // display article text
            String url = Utils.getUrlFileFromStorage(mActivity.get().getApplicationContext(), article.getID());
            vwActicle.loadUrl(url);

            mArticle = article;
        }
    }

    /**
     * run on send email message click
     * */
    // Creating new email message to request
    private void makeMessage(Context context) {
        Log.d(TAG, ".makeMessage(): making new message");

        // fill in the email message subject and body from resources
        String email_contacts = context.getResources().getString(R.string.email_contacts);
        String email_subject = context.getResources().getString(R.string.email_subject);
        String email_body =  context.getResources().getString(R.string.email_body);

        Intent intent = Utils.makeEmailIntent(
                            email_contacts,
                            email_subject,
                            email_body);

        // start next email Activity if email application exists on device. Notification otherwise
        if (intent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(intent);
        } else {
            // create warning if there is no messager installed
            Toast.makeText(context, context.getResources().getString(R.string.email_warning), Toast.LENGTH_SHORT).show();
        }
    }
}
