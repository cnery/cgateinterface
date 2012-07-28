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