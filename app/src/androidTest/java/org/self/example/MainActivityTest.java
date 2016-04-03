package org.self.example;

import android.test.ActivityInstrumentationTestCase2;


import junit.framework.Assert;

/**
 * Created by manthan on 27/3/16.
 */
public class MainActivityTest extends ActivityInstrumentationTestCase2<MainActivity> {
    public MainActivityTest(Class<MainActivity> activityClass) {
        super(activityClass);
    }


    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    public MainActivityTest(){
        super(MainActivity.class);
    }

    public void testGreeting() throws Exception {

        MainActivity activity = getActivity();
        int count = 0;

        String result = activity.getGreeting(count);


        count = 3;

        result = activity.getGreeting(count);

        Assert.assertEquals("Hallo Welt", result);

    }
    public void test2(){
        MainActivity activity = getActivity();

        int count = 0;

        String result = activity.getGreeting(count);

        Assert.assertEquals("Holamundo", result);

        count = 1;

        result = activity.getGreeting(count);

        Assert.assertEquals("Bonjour tout le monde", result);

        count = 2;

        result = activity.getGreeting(count);

        Assert.assertEquals("Ciao mondo", result);
    }

}
