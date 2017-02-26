package ru.vzateychuk.mr2.common;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * This abstract class extends the Fragment class and overrides lifecycle
 * callbacks for logging various lifecycle events.
 * Created by vez on 31.10.15.
 */

public class LifecycleLoggingFragment extends android.support.v4.app.Fragment {

    // debug purpose only
    protected final String TAG = this.getClass().getSimpleName();

    public LifecycleLoggingFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate(): fragment re-created");
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView(): fragment is about to create view");
        return new View(getActivity());
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Log.d(TAG, ".onAttach()");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.d(TAG, ".onDetach()");
    }

    // run downloading when user resumed the Fragment
    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, ".onResume(): fragment is going to be visible");
    }

    // Called when the fragment is no longer in use.  This is called to destroy used objects
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, ".onDestroy(): fragment is going to be destroyed");
    }

}
