/**
 *  CGateInterface - A library to allow interaction with Clipsal C-Gate.
 *  Copyright (C) 2008,2009,2012  Dave Oxley <dave@daveoxley.co.uk>.
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
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

        Network network = Project.getProject(session, "OXLEY").getNetwork(254);
        network.getApplications(false);
    }
}