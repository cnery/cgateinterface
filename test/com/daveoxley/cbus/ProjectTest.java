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
public class ProjectTest {

    public ProjectTest() {
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
     * Test of dir method, of class Project.
     */
    @Test
    public void testDir() throws Exception {
        System.out.println("dir");

        ArrayList<Project> result = Project.dir(session, false);

        assertNotNull(result);
    }

    /**
     * Test of getName method, of class Project.
     */
    @Test
    public void testGetName() throws Exception {
        System.out.println("getName");

        ArrayList<Project> result = Project.dir(session, false);
        assertEquals("OXLEY", result.get(0).getName());

        result = Project.list(session, false);
        assertEquals("OXLEY", result.get(0).getName());
   }

}