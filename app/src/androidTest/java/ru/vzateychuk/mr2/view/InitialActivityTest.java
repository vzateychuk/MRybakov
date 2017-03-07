package ru.vzateychuk.mr2.view;


import android.test.ActivityInstrumentationTestCase2;
import android.test.ViewAsserts;
import android.widget.TextView;

import ru.vzateychuk.mr2.R;

/**
 * This Activity test case doesn't do any useful checks since current logic tightly coupled with Activity
 */
public class InitialActivityTest extends ActivityInstrumentationTestCase2<InitialActivity>
{
    private InitialActivity mActivity;
    private TextView tvWebSite;
    private TextView tvContactPhone;

    public InitialActivityTest()
    {
        super(InitialActivity.class);
    }

    @Override
    public void setUp() throws Exception
    {
        super.setUp();
        // Получим активность
        mActivity = getActivity();
        tvWebSite = (TextView) mActivity.findViewById(R.id.tvWebSite);
        tvContactPhone = (TextView) mActivity.findViewById(R.id.tvContactPhone);
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }

    public void testActivityAndControlsCreated()
    {
        assertNotNull("Activity", mActivity);
        assertNotNull("WebSite.", tvWebSite);
        assertNotNull("ContactPhone.", tvContactPhone);
    }

    public void testControlsVisible() throws Exception
    {
        ViewAsserts.assertOnScreen(tvWebSite.getRootView(), tvWebSite);
        ViewAsserts.assertOnScreen(tvContactPhone.getRootView(), tvContactPhone);
    }

    public void testWebSiteNotEmpty()
    {
        assertEquals( "WebSite.",
                mActivity.getString(R.string.web_site),
                tvWebSite.getText().toString() );
    }
}