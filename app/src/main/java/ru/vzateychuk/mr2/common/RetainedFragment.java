package ru.vzateychuk.mr2.common;

import android.app.Fragment;
import android.os.Bundle;

import java.util.HashMap;
import java.util.Map;

/**
 * RetainedFragment
 * This internal class represent "headless" Fragment that retains state information between
 * configuration changes.
 */
public class RetainedFragment extends Fragment
{
    private Map<String, Object> dataMap = new HashMap<>();

    /**
     * Hook method called when a new instance of Fragment is created.
     *
     * @param savedInstanceState object that contains saved state information.
     */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        // Ensure the data survives runtime configuration changes.
        setRetainInstance(true);
    }

    /**
     * Add the @a object with the @a key.
     */
    public void put(String key, Object object)
    {
        dataMap.put(key, object);
    }

    /**
     * Get the object with @a key.
     */
    public <T> T get(String key)
    {
        return (T) dataMap.get(key);
    }

}
