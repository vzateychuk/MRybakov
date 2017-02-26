package ru.vzateychuk.mr2.common;

import android.app.Activity;

/**
 * The base interface that an operations ("Ops") class must implement
 * so that it can be notified automatically by the GenericActivity
 * framework when runtime configuration changes occur.
 * Created by vez on 3.11.15.
 */
public interface IConfigurableOps {
    /**
     * Hook method dispatched by the GenericOps framework to
     * initialize an operations ("Ops") object after it's been created.
     *
     * @param activity    The currently active Activity.
     * @param firstTimeIn Set to "true" if this is the first time the
     *                    Ops class is initialized, else set to
     *                    "false" if called after a runtime
     *                    configuration change.
     */
    void onConfiguration(Activity activity,
                         boolean firstTimeIn);
}
