package ru.vzateychuk.mr2.common;

/**
 * Created by vez on 3.11.15.
 */

/**
 * The base interface that an operations ("Ops") class must implement
 * so that it can be notified automatically by the GenericAsyncTask
 * framework during the AsyncTask processing.
 */
public interface IGenericAsyncTaskOps<Params, Progress, Result> {
    /**
     * Process the @a param in a background thread.
     */
    Result doInBackground(Params param);

    /**
     * Process the @a result in the UI Thread.
     */
    void onPostExecute(Result result,
                       Params param);
}
