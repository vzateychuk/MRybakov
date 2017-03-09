package ru.vzateychuk.mr2.common;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.test.suitebuilder.annotation.SmallTest;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit test for RetainedFragmentManager
 */
@RunWith(MockitoJUnitRunner.class)
@SmallTest
public class RetainedFragmentManagerTest
{
    private final String tag = this.getClass().getName();

    @Captor
    private ArgumentCaptor captor;

    @Mock
    private FragmentTransaction fragmentTransactionMock;
    @Mock
    private FragmentManager fragmentManagerMock;

    private RetainedFragmentManager retainedFragmentManager;

    @Before
    public void setUp()
    {
        when( fragmentManagerMock.beginTransaction() ).thenReturn(fragmentTransactionMock);
        when( fragmentTransactionMock.add( any( Fragment.class), eq(tag) ) ).thenReturn(fragmentTransactionMock);
        retainedFragmentManager = new RetainedFragmentManager(fragmentManagerMock, tag);
    }

    @Test
    public void shouldCreateRetainedFragmentManager() {
        assertNotNull("retainedFragmentManager.", retainedFragmentManager);
        verify(fragmentTransactionMock, times(1)).commit();
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowExceptionIfFragmentManagerIsNull ()
    {
        retainedFragmentManager = new RetainedFragmentManager(null, tag);
    }

    @Test
    public void firstTime()
    {
        assertTrue( "Expected isFirstTime() returns true", retainedFragmentManager.isFirstTime() );
        assertFalse( "Expected isFirstTime() returns false", retainedFragmentManager.isFirstTime() );
    }


    @Test
    public void putAndGet() throws Exception
    {
        List<String> list = Arrays.asList("S1", "S2", "S3");
        retainedFragmentManager.put(tag, list);

        List<String> got = retainedFragmentManager.get(tag);
        assertEquals(got, Arrays.asList("S1", "S2", "S3"));
    }

}