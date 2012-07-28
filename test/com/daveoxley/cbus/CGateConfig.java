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
