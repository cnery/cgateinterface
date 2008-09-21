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
import java.util.HashMap;

/**
 *
 * @author Dave Oxley <dave@daveoxley.co.uk>
 */
public final class Network extends CGateObject
{
    private Project project;

    private int net_id;

    private final HashMap<Integer,Unit> cached_units = new HashMap<Integer,Unit>();

    private Network(Project project, String cgate_response) throws CGateException
    {
        this.project = project;
        this.net_id = getNetworkID(project, cgate_response);
    }

    /**
     * Issue a <code>net list_all</code> to the C-Gate server.
     * 
     * @see <a href="http://www.clipsal.com/cis/downloads/Toolkit/CGateServerGuide_1_0.pdf">
     *      <i>C-Gate Server Guide 4.3.63</i></a>
     * @param cgate_session The C-Gate session
     * @return ArrayList of Networks
     * @throws CGateException
     */
    public static ArrayList<Network> listAll(CGateSession cgate_session) throws CGateException
    {
        ArrayList<String> resp_array = cgate_session.sendCommand("net list_all");

        ArrayList<Network> networks = new ArrayList<Network>();
        for (String response : resp_array)
            networks.add(getOrCreateNetwork(cgate_session, response));

        return networks;
    }

    /**
     * Retrieve the Unit Object for the specified unit id.
     * 
     * @param cgate_session The CGateSession
     * @param unit_id The unit to retrieve
     * @return The Unit
     * @throws CGateException
     */
    public Unit getUnit(CGateSession cgate_session, int unit_id) throws CGateException
    {
        tree(cgate_session);

        return getCachedUnit(unit_id);
    }

    private static Network getOrCreateNetwork(CGateSession cgate_session, String cgate_response) throws CGateException
    {
        HashMap<String,String> resp_map = responseToMap(cgate_response);

        Project.dir(cgate_session);
        Project project = Project.getProject(cgate_session, resp_map.get("project"));

        int net_id = getNetworkID(project, cgate_response);

        Network network = project.getCachedNetwork(net_id);
        if (network == null)
        {
            network = new Network(project, cgate_response);
            project.cacheNetwork(network);
        }
        return network;
    }

    static int getNetworkID(Project project, String cgate_response) throws CGateException
    {
        HashMap<String,String> resp_map = responseToMap(cgate_response);
        int net_id = -1;
        String value = resp_map.get("network");
        if (value != null)
            net_id = Integer.parseInt(value.trim());
        else
        {
            value = resp_map.get("address");
            if (value != null)
            {
                String net_str = value.substring(project.getName().length() + 3);
                net_id = Integer.parseInt(net_str.trim());
            }
        }

        if (net_id < 0)
            throw new CGateException();

        return net_id;
    }

    /**
     *
     * @return
     */
    public int getNetworkID()
    {
        return net_id;
    }

    String getProjectName()
    {
        return project.getName();
    }

    Unit getCachedUnit(int unit_id)
    {
        return cached_units.get(unit_id);
    }

    void cacheUnit(Unit unit)
    {
        cached_units.put(unit.getUnitID(), unit);
    }

    /**
     * Issue a <code>tree //PROJECT/NET_ID</code> to the C-Gate server.
     * 
     * @see <a href="http://www.clipsal.com/cis/downloads/Toolkit/CGateServerGuide_1_0.pdf">
     *      <i>C-Gate Server Guide 4.3.124</i></a>
     * @param cgate_session The C-Gate session
     * @return ArrayList of Units
     * @throws CGateException
     */
    public ArrayList<Unit> tree(CGateSession cgate_session) throws CGateException
    {
        ArrayList<String> resp_array = cgate_session.sendCommand("tree //" + project.getName() + "/" + net_id);

        ArrayList<Unit> untis = new ArrayList<Unit>();
        for (String response : resp_array)
        {
            if (response.indexOf("//" + project.getName() + "/" + net_id + "/") > -1)
                untis.add(Unit.getOrCreateUnit(cgate_session, this, response));
        }

        return untis;
    }
}
