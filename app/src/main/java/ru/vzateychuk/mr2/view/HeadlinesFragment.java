package ru.vzateychuk.mr2.view;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;

import java.util.List;

import ru.vzateychuk.mr2.R;
import ru.vzateychuk.mr2.common.LifecycleLoggingFragment;
import ru.vzateychuk.mr2.common.OnFragmentInteractionListener;
import ru.vzateychuk.mr2.model.Article;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Large screen devices (such as tablets) are supported by replacing the ListView
 * with a GridView.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnFragmentInteractionListener}
 * interface.
 */
public class HeadlinesFragment extends LifecycleLoggingFragment implements AdapterView.OnItemClickListener, View.OnClickListener {

    private OnFragmentInteractionListener mListener;

    /**
     * The fragment's ListView/GridView.
     */
    private AbsListView mListView;

    /**
     * Filter pane
     * */
    private EditText mFilterEdit;
    private Button mFilterButton;

    /**
     * The Adapter which will be used to populate the ListView/GridView with
     * Views.
     */
    private ListAdapter mAdapter;

    // factory method creates the fragment with parameters (in bundle)
    public static HeadlinesFragment newInstance() {
        Log.d("HeadlinesFragment", "newInstance()");
        HeadlinesFragment fragment = new HeadlinesFragment();
        return fragment;
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public HeadlinesFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Fragment won't be re-created in reconfiguration
        setRetainInstance(true);

        // turn on options menu
        // setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        // just for logging
        super.onCreateView(inflater, container, savedInstanceState);

        View view = inflater.inflate(R.layout.headlines, container, false);

        // Set the adapter
        mListView = (AbsListView) view.findViewById(android.R.id.list);

        // set reference to filter pane
        mFilterEdit = (EditText) view.findViewById(R.id.filter_edit);
        mFilterButton = (Button) view.findViewById(R.id.filter_button);
        mFilterButton.setOnClickListener(this);

        // setup adapter
        if (null!=mAdapter)
            mListView.setAdapter(mAdapter);

        // Set OnItemClickListener so we can be notified on item clicks
        mListView.setOnItemClickListener(this);

        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    // click on article, callback listener on hosted activity
    @Override
    public void onItemClick(AdapterView<?> parent,
                            View view,
                            int position,
                            long id) {
        Log.d(TAG, "onItemClick(), position=" + position + ", id=" + id);

        if (null != mListener) {
            // Notify the active callbacks interface (the activity, if the fragment is attached to one) that an item has been selected.
            mListener.onSelectedListener(position);
        }
    }

    /**
     *  Change Adapter to display new content
     **/
    public void displayArticles(List<Article> articles) {
        Log.d(TAG, "displayArticles()");
        // create new adapter
        mAdapter = ArticleViewAdapter.newInstance(getActivity().getApplicationContext(), articles);
        // set adapter to the ListView
        mListView.setAdapter(mAdapter);
    }

    /**
     * click on Filter button
     * not used (as filter doesn't used)
      */
    @Override
    public void onClick(View v) {
        Log.d(TAG, "onClick()");

    }


}
