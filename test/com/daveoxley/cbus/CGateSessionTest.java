/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.daveoxley.cbus;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author dave
 */
public class CGateSessionTest {

    public CGateSessionTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of getCGateObject method, of class CGateSession.
     */
    @Test
    public void testGetCGateObject() throws Exception {
        System.out.println("getCGateObject");

        CGateSession session = CGateInterface.connect(CGateConfig.SERVER, CGateConfig.COMMAND_PORT,
                CGateConfig.EVENT_PORT, CGateConfig.STATUS_CHANGE_PORT);
        session.getCGateObject("//OXLEY/254/56/0");
        session.close();
    }
}