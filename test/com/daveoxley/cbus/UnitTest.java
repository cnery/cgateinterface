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
import static org.junit.Assert.*;

/**
 *
 * @author dave
 */
public class UnitTest {

    public UnitTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    private CGateSession session;

    @Before
    public void setUp() throws CGateConnectException {
        session = CGateInterface.connect(CGateConfig.SERVER, CGateConfig.COMMAND_PORT, CGateConfig.EVENT_PORT);
    }

    @After
    public void tearDown() throws CGateException {
        session.close();
    }

    /**
     * Test of getApplicationType method, of class Unit.
     */
    @Test
    public void testGetApplicationType() throws CGateException {
        System.out.println("getApplicationType");

        String example_tree_resp1 = "320-//OXLEY/254/p/14 ($e) type=KEYBL5 app=56($38),136($88) state=ok groups=1,44,10,2";
        String example_tree_resp2 = "320- //OXLEY/254/56/10 ($a) level=0 state=ok units=6,14";

        Project project = Project.getProject(session, "OXLEY");
        Network network = project.getNetwork(session, 254);

        assertEquals("p", Unit.getApplicationType(network, example_tree_resp1));
        assertEquals("56", Unit.getApplicationType(network, example_tree_resp2));
    }

    /**
     * Test of getUnitID method, of class Unit.
     */
    @Test
    public void testGetUnitID() throws CGateException {
        System.out.println("getUnitID");

        String example_tree_resp1 = "320-//OXLEY/254/p/14 ($e) type=KEYBL5 app=56($38),136($88) state=ok groups=1,44,10,2";
        String example_tree_resp2 = "320- //OXLEY/254/56/10 ($a) level=0 state=ok units=6,14";

        Project project = Project.getProject(session, "OXLEY");
        Network network = project.getNetwork(session, 254);

        assertEquals(14, Unit.getUnitID(network, example_tree_resp1));
        assertEquals(10, Unit.getUnitID(network, example_tree_resp2));
    }
}