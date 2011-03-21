package edu.berkeley.bps.services.common.time;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for simple Time.
 */
public class TimeTest 
    extends TestCase
{
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public TimeTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( TimeTest.class );
    }

    public void testApp()
    {
        long time = TimeUtils.getTimeInMillisForYear(2001);
        String timeS = TimeUtils.millisToSimpleYearString(time);
        assert("2001 CE".equals(timeS));
        time = TimeUtils.getTimeInMillisForYear(200);
        timeS = TimeUtils.millisToSimpleYearString(time);
        assert("200 CE".equals(timeS));
        time = TimeUtils.getTimeInMillisForYear(0);
        timeS = TimeUtils.millisToSimpleYearString(time);
        assert("1 BCE".equals(timeS));
        time = TimeUtils.getTimeInMillisForYear(-200);
        timeS = TimeUtils.millisToSimpleYearString(time);
        assert("201 BCE".equals(timeS));
    }
}
