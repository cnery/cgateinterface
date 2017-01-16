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
        session.connect();
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
        assertEquals("HOME", result.get(0).getName());

        result = Project.list(session, false);
        assertEquals("HOME", result.get(0).getName());
   }

}