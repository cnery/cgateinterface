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
public class Unit extends CGateObject
{
    private Network network;

    private int unit_id;

    Unit(Network network, String cgate_response)
    {
        this.network = network;
        this.unit_id = getUnitID(network, cgate_response);
    }

    @Override
    protected String getKey()
    {
        return String.valueOf(unit_id);
    }

    static Unit getOrCreateUnit(CGateSession cgate_session, Network network, String response) throws CGateException
    {
        String application_type = getApplicationType(network, response);
        int unit_id = getUnitID(network, response);

        Unit return_unit;
        if (application_type.equals("p"))
        {
            Unit unit = (Unit)network.getCachedObject("unit", String.valueOf(unit_id));
            if (unit == null)
            {
                unit = new Unit(network, response);
                network.cacheObject("unit", unit);
            }
            return_unit = unit;
        }
        else
        {
            Group group = (Group)network.getCachedObject("group", String.valueOf(unit_id));
            if (group == null)
            {
                Application application = network.getApplication(cgate_session, Integer.parseInt(application_type));
                group = new Group(application, response);
                application.cacheObject("group", group);
            }
            return_unit = group;
        }
        return return_unit;
    }

    static String getApplicationType(Network network, String response)
    {
        String network_address = "//" + network.getProjectName() + "/" + network.getNetworkID() + "/";
        int index = response.indexOf(network_address);
        int application_index = response.indexOf("/", index + network_address.length());
        return response.substring(index + network_address.length(), application_index);
    }

    static int getUnitID(Network network, String response)
    {
        String application_type = getApplicationType(network, response);
        String application_address = "//" + network.getProjectName() + "/" + network.getNetworkID() + "/" + application_type + "/";
        int index = response.indexOf(application_address);
        int unit_index = response.indexOf(" ", index + 1);
        return Integer.parseInt(response.substring(index + application_address.length(), unit_index).trim());
    }

    /**
     *
     * @return
     */
    public int getUnitID()
    {
        return unit_id;
    }

    /**
     *
     * @return
     */
    protected Network getNetwork()
    {
        return network;
    }
}
