package ru.vzateychuk.mr2.view;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import ru.vzateychuk.mr2.R;

/**
 *
 * This class represents the dialog for user selection when user try to select apropriate article
 * Created by 123 on 16.12.2015.
 */
public class DialogOptionSelection extends DialogFragment implements AdapterView.OnItemClickListener {
    /**
     * Debugging tag used by the Android logger.
     */
    final static String TAG = DialogOptionSelection.class.getSimpleName();

    /**
     *  The activity that creates an instance of this dialog fragment must
     * implement this interface in order to receive event callbacks.
     * Each method passes the DialogFragment in case the host needs to query it. */
    public interface NoticeDialogListener {
        void onDialogItemClick(String result);
    }

    // Use this instance of the interface to deliver action events
    private NoticeDialogListener mListener;

    // internal reference to the listView / adapter
    private ArrayAdapter<String> mAdapter;

    // values to be returned as a result of selection
    private String[] mValues;
    private String mTitle;

    /**
     * Create a new instance of MyDialogFragment, providing title and options
     * as an argument.
     */
    static DialogOptionSelection newInstance(
            Context context,
            String title,
            String[] options,
            String[] values) {

        Log.d(TAG, "newInstance(); title=" + title + ", options=" + options.toString());

        // create new instance of the dialog
        DialogOptionSelection dialog = new DialogOptionSelection();

        // copy data to new array
        dialog.mValues = new String[values.length];
        System.arraycopy(values, 0, dialog.mValues, 0, values.length);

        // create new ArrayAdapter to be used in listView
        if (options.length > 0 )
            dialog.mAdapter = new ArrayAdapter<String>(
                        context,
                        android.R.layout.select_dialog_item,
                        options);

        // change dialog style
        dialog.setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_DeviceDefault_Dialog);
        dialog.mTitle = title;

        // Supply num input as an argument.
        return dialog;
    }


    // Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            mListener = (NoticeDialogListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement NoticeDialogListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView()");

        View view = inflater.inflate(R.layout.fragment_dialog, container, false);

        // find reference to the ListView
        ListView listView = (ListView) view.findViewById(R.id.listViewDialog);

        // Setup adapter and set OnItemClickListener so we can be notified on item clicks
        if (null!=mAdapter) {
            listView.setAdapter(mAdapter);
            listView.setOnItemClickListener(this);
        }

        // Setup dialog title
        if (null!=mTitle) this.getDialog().setTitle(mTitle);

        return view;
    }

    @Override
    public void onDestroy(){
        Log.d(TAG, "onCreateView()");
        super.onDestroy();
    }

    /**
     * Inherited implements AdapterView.OnItemClickListener
     * */
    @Override
    public void onItemClick(AdapterView<?> parent,
                            View view,
                            int position,
                            long id) {
        Log.d(TAG, "onItemClick(), postion=" + position);

        if (null != mListener)
            mListener.onDialogItemClick(mValues[position]);

        // finally dismiss the dialog
        dismiss();
    }

}
