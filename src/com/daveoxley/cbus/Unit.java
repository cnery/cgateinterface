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

import java.util.ArrayList;

/**
 *
 * @author Dave Oxley <dave@daveoxley.co.uk>
 */
public class Unit extends CGateObject
{
    private Network network;

    private int unit_id;

    private boolean on_network;

    Unit(Network network, String cgate_response, boolean tree_resp)
    {
        this.network = network;
        this.on_network = tree_resp;
        if (tree_resp)
            this.unit_id = getUnitID(network, cgate_response);
        else
        {
            int index = cgate_response.indexOf("=");
            this.unit_id = Integer.parseInt(cgate_response.substring(index + 1));
        }
    }

    @Override
    protected String getKey()
    {
        return String.valueOf(unit_id);
    }

    static Unit getOrCreateUnit(CGateSession cgate_session, Network network, String response, boolean tree_resp) throws CGateException
    {
        if (tree_resp)
        {
            String application_type = Network.getApplicationType(network, response);
            int unit_id = getUnitID(network, response);

            if (application_type.equals("p"))
            {
                Unit unit = (Unit)network.getCachedObject("unit", String.valueOf(unit_id));
                if (unit == null)
                {
                    unit = new Unit(network, response, true);
                    network.cacheObject("unit", unit);
                }
                return unit;
            }
            return null;
        }
        else
        {
            int index = response.indexOf("=");
            String unit_id = response.substring(index + 1);

            Unit unit = (Unit)network.getCachedObject("unit", unit_id);
            if (unit == null)
            {
                unit = new Unit(network, response, false);
                network.cacheObject("unit", unit);
            }
            return unit;
        }
    }

    static int getUnitID(Network network, String response)
    {
        String application_type = Network.getApplicationType(network, response);
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
    private Network getNetwork()
    {
        return network;
    }

    public boolean onNetwork()
    {
        return on_network;
    }

    public String getName(CGateSession cgate_session) throws CGateException
    {
        String address = "//" + network.getProjectName() + "/" + network.getNetworkID() + "/p/" + unit_id + "/TagName";
        ArrayList<String> resp_array = cgate_session.sendCommand("dbget " + address).toArray();
        return responseToMap(resp_array.get(0), true).get(address);
    }

    public String getUnitType(CGateSession cgate_session) throws CGateException
    {
        String address = "//" + network.getProjectName() + "/" + network.getNetworkID() + "/p/" + unit_id + "/UnitType";
        ArrayList<String> resp_array = cgate_session.sendCommand("dbget " + address).toArray();
        return responseToMap(resp_array.get(0), true).get(address);
    }

    public String getSerialNumber(CGateSession cgate_session) throws CGateException
    {
        String address = "//" + network.getProjectName() + "/" + network.getNetworkID() + "/p/" + unit_id + "/SerialNumber";
        ArrayList<String> resp_array = cgate_session.sendCommand("dbget " + address).toArray();
        return responseToMap(resp_array.get(0), true).get(address);
    }

    public String getUnitName(CGateSession cgate_session) throws CGateException
    {
        String address = "//" + network.getProjectName() + "/" + network.getNetworkID() + "/p/" + unit_id + "/UnitName";
        ArrayList<String> resp_array = cgate_session.sendCommand("dbget " + address).toArray();
        return responseToMap(resp_array.get(0), true).get(address);
    }

    public String getFirmware(CGateSession cgate_session) throws CGateException
    {
        String address = "//" + network.getProjectName() + "/" + network.getNetworkID() + "/p/" + unit_id + "/FirmwareVersion";
        ArrayList<String> resp_array = cgate_session.sendCommand("dbget " + address).toArray();
        return responseToMap(resp_array.get(0), true).get(address);
    }
}
