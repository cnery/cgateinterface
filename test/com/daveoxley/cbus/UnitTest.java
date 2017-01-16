/*
 * CGateInterface - A library to allow interaction with Clipsal C-Gate.
 *
 * Copyright 2008, 2009, 2012, 2017 Dave Oxley <dave@daveoxley.co.uk>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
 * @author Dave Oxley <dave@daveoxley.co.uk>
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
        session = CGateInterface.connect(CGateConfig.SERVER, CGateConfig.COMMAND_PORT,
                CGateConfig.EVENT_PORT, CGateConfig.STATUS_CHANGE_PORT);
        session.connect();
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

        String example_tree_resp1 = "320-//HOME/254/p/14 ($e) type=KEY2 app=56($38),255($ff) state=ok groups=22,23";
        String example_tree_resp2 = "320-  //HOME/254/56/10 ($a) level=0 state=sync units=1,29,31";

        Project project = Project.getProject(session, "HOME");
        Network network = project.getNetwork(254);

        assertEquals("p", Network.getApplicationType(network, example_tree_resp1));
        assertEquals("56", Network.getApplicationType(network, example_tree_resp2));
    }

    /**
     * Test of getUnitID method, of class Unit.
     */
    @Test
    public void testGetUnitID() throws CGateException {
        System.out.println("getUnitID");

        String example_tree_resp1 = "320-//HOME/254/p/14 ($e) type=KEY2 app=56($38),255($ff) state=ok groups=22,23";
        String example_tree_resp2 = "320-  //HOME/254/56/10 ($a) level=0 state=sync units=1,29,31";

        Project project = Project.getProject(session, "HOME");
        Network network = project.getNetwork(254);

        assertEquals(14, Unit.getUnitID(network, example_tree_resp1));
        assertEquals(10, Unit.getUnitID(network, example_tree_resp2));
    }
}