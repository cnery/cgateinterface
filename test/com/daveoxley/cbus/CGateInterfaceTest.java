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
public class CGateInterfaceTest {

    public CGateInterfaceTest() {
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
     * Test of connect method, of class CGateInterface.
     */
    @Test
    public void testConnect() throws Exception {
        System.out.println("connect");

        try
        {
            CGateSession result = CGateInterface.connect(null, 0, 0, 0);
            fail("NullPointerException should have been thrown");
        }
        catch (NullPointerException npe) {}

        CGateSession session = CGateInterface.connect(CGateConfig.SERVER, CGateConfig.COMMAND_PORT,
                CGateConfig.EVENT_PORT, CGateConfig.STATUS_CHANGE_PORT);
        assertNotNull(session);
        session.connect();
        session.close();
    }

    /**
     * Test of noop method, of class CGateInterface.
     */
    @Test
    public void testNoop() throws Exception {
        System.out.println("noop");

        try
        {
            CGateInterface.noop(null);
            fail("NullPointerException should have been thrown");
        }
        catch (NullPointerException npe) {}

        CGateSession session = CGateInterface.connect(CGateConfig.SERVER, CGateConfig.COMMAND_PORT,
                CGateConfig.EVENT_PORT, CGateConfig.STATUS_CHANGE_PORT);
        session.connect();
        CGateInterface.noop(session);
        session.close();
    }

}