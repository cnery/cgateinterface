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

import java.util.ArrayList;
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
public class NetworkTest {

    public NetworkTest() {
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
     * Test of listAll method, of class Network.
     */
    @Test
    public void testListAll() throws Exception {
        System.out.println("listAll");

        ArrayList<Network> result = Network.listAll(session, false);

        assertNotNull(result);
    }

    /**
     * Test of getNetworkID method, of class Network.
     */
    @Test
    public void testGetNetworkID() throws CGateException {
        System.out.println("getNetworkID");

        ArrayList<Network> result = Network.listAll(session, false);
        assertEquals(254, result.get(0).getNetworkID());
    }

    /**
     * Test of getNetworkID method, of class Network.
     */
    @Test
    public void testListApplications() throws CGateException {
        System.out.println("listApplications");

        Network network = Project.getProject(session, "HOME").getNetwork(254);
        network.getApplications(false);
    }
}