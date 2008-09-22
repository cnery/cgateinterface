/**
 *  CGateInterface - A library to allow interaction with Clipsal C-Gate.
 *  Copyright (C) 2008  Dave Oxley <dave@daveoxley.co.uk>.
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

/**
 *
 * @author Dave Oxley <dave@daveoxley.co.uk>
 */
public class Application extends CGateObject
{
    private Network network;

    private int application_id;

    private Application(Network network, int application_id)
    {
        this.network = network;
        this.application_id = application_id;
        setupSubtreeCache("group");
    }

    @Override
    protected String getKey()
    {
        return String.valueOf(application_id);
    }

    static Application getOrCreateApplication(CGateSession cgate_session, Network network, String response)
    {
        int index = response.indexOf("=");
        String application_id = response.substring(index + 1);

        Application application = (Application)network.getCachedObject("application", application_id);
        if (application == null)
        {
            application = new Application(network, Integer.parseInt(application_id));
            network.cacheObject("application", application);
        }
        return application;
    }

    Network getNetwork()
    {
        return network;
    }
}
