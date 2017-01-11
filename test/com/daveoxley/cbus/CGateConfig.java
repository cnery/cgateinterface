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

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 *
 * @author Dave Oxley <dave@daveoxley.co.uk>
 */
class CGateConfig
{
    /** Configure these for specific environment */
    static final String SERVER_STR = "localhost";
    static final int COMMAND_PORT = 20023;
    static final int EVENT_PORT = 20024;
    static final int STATUS_CHANGE_PORT = 20025;
    /** ---------------------------------------- */


    static final InetAddress SERVER;

    static
    {
        InetAddress cgate_server = null;
        try
        {
            cgate_server = InetAddress.getByName(SERVER_STR);
        }
        catch (UnknownHostException e) {}

        SERVER = cgate_server;
    }
}
