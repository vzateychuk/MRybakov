package ru.vzateychuk.mr2.common;

import android.app.FragmentManager;

import java.lang.ref.WeakReference;

/**
 * Retains and manages state information between runtime configuration
 * changes to an Activity.
 */
public class RetainedFragmentManager
{
    // Name used to identify the RetainedFragment.
    private String tag;
    // WeakReference to the FragmentManager.
    private WeakReference<FragmentManager> fragManagerWeakRef;
    // Reference to the RetainedFragment.
    private RetainedFragment retainedFragment;
    private boolean firstTime = true;

    public RetainedFragmentManager(FragmentManager fragmentManager, String tag)
    {
        if (null==fragmentManager) {
            throw new NullPointerException("Expected fragManagerWeakRef not null");
        }

        this.fragManagerWeakRef = new WeakReference<>(fragmentManager);
        this.tag = tag;

        this.retainedFragment = new RetainedFragment();
        fragmentManager.beginTransaction().add( retainedFragment, tag ).commit();
    }

    /**
     * Initializes the RetainedFragment the first time it's called.
     *
     * @returns true if it's first time the method's been called, else false.
     */
    public boolean isFirstTime()
    {
        return firstTime ? !(firstTime=false) : firstTime;
    }

    /**
     * Store the @obj object with the key.
     */
    public void put(String key, Object obj)
    {
        retainedFragment.put(key, obj);
    }

    /**
     * Get the object with @a key.
     */
    public <T> T get(String key)
    {
        return (T) retainedFragment.get(key);
    }
}
