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

    private Network(Project project, String cgate_response) throws CGateException
    {
        this.project = project;
        this.net_id = getNetworkID(project, cgate_response);
        setupSubtreeCache("application");
        setupSubtreeCache("unit");
    }

    @Override
    protected String getKey()
    {
        return String.valueOf(net_id);
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
    public Application getApplication(CGateSession cgate_session, int application_id) throws CGateException
    {
        getApplications(cgate_session);

        return (Application)getCachedObject("application", String.valueOf(application_id));
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
        getUnits(cgate_session);

        return (Unit)getCachedObject("unit", String.valueOf(unit_id));
    }

    private static Network getOrCreateNetwork(CGateSession cgate_session, String cgate_response) throws CGateException
    {
        HashMap<String,String> resp_map = responseToMap(cgate_response);

        Project.dir(cgate_session);
        Project project = Project.getProject(cgate_session, resp_map.get("project"));

        int net_id = getNetworkID(project, cgate_response);

        Network network = (Network)project.getCachedObject("network", String.valueOf(net_id));
        if (network == null)
        {
            network = new Network(project, cgate_response);
            project.cacheObject("network", network);
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

    public String getName(CGateSession cgate_session) throws CGateException
    {
        String address = "//" + project.getName() + "/" + net_id + "/TagName";
        ArrayList<String> resp_array = cgate_session.sendCommand("dbget " + address);
        return responseToMap(resp_array.get(0), true).get(address);
    }

    public String getType(CGateSession cgate_session) throws CGateException
    {
        ArrayList<String> resp_array = cgate_session.sendCommand("show //" + project.getName() + "/" + net_id + " Type");
        return responseToMap(resp_array.get(0)).get("Type");
    }

    public String getInterfaceAddress(CGateSession cgate_session) throws CGateException
    {
        ArrayList<String> resp_array = cgate_session.sendCommand("show //" + project.getName() + "/" + net_id + " InterfaceAddress");
        return responseToMap(resp_array.get(0)).get("InterfaceAddress");
    }

    public String getState(CGateSession cgate_session) throws CGateException
    {
        ArrayList<String> resp_array = cgate_session.sendCommand("show //" + project.getName() + "/" + net_id + " State");
        return responseToMap(resp_array.get(0)).get("State");
    }

    static String getApplicationType(Network network, String response)
    {
        String network_address = "//" + network.getProjectName() + "/" + network.getNetworkID() + "/";
        int index = response.indexOf(network_address);
        int application_index = response.indexOf("/", index + network_address.length());
        return response.substring(index + network_address.length(), application_index);
    }

    public ArrayList<Unit> getUnits(CGateSession cgate_session) throws CGateException
    {
        tree(cgate_session);

        ArrayList<String> resp_array = dbget(cgate_session, null);

        int number_of_units = -1;
        for (String response : resp_array) {
            String address = "//" + project.getName() + "/" + net_id + "/Unit[";
            int index = response.indexOf(address);
            if (index > -1) {
                int index2 = response.indexOf("]", index + address.length());
                number_of_units = Integer.parseInt(response.substring(index + address.length(), index2));
                break;
            }
        }

        ArrayList<Unit> units = new ArrayList<Unit>();
        for (int i = 1; i <= number_of_units; i++) {
            resp_array = dbget(cgate_session, "Unit[" + i + "]/Address");
            Unit unit = Unit.getOrCreateUnit(cgate_session, this, resp_array.get(0), false);
            if (unit != null)
                units.add(unit);
        }

        return units;
    }

    /**
     * Issue a <code>tree //PROJECT/NET_ID</code> to the C-Gate server.
     *
     * @see <a href="http://www.clipsal.com/cis/downloads/Toolkit/CGateServerGuide_1_0.pdf">
     *      <i>C-Gate Server Guide 4.3.124</i></a>
     * @param cgate_session The C-Gate session
     * @throws CGateException
     */
    void tree(CGateSession cgate_session) throws CGateException
    {
        getApplications(cgate_session);
        ArrayList<String> resp_array = cgate_session.sendCommand("tree //" + project.getName() + "/" + net_id);

        for (String response : resp_array)
        {
            if (response.indexOf("//" + project.getName() + "/" + net_id + "/") > -1)
            {
                if (getApplicationType(this, response).equals("p"))
                    Unit.getOrCreateUnit(cgate_session, this, response, true);
                else
                    Group.getOrCreateGroup(cgate_session, this, response);
            }
        }
    }

    /**
     * Issue a <code>net open //PROJECT/NET_ID</code> to the C-Gate server.
     *
     * @see <a href="http://www.clipsal.com/cis/downloads/Toolkit/CGateServerGuide_1_0.pdf">
     *      <i>C-Gate Server Guide 4.3.65</i></a>
     * @param cgate_session The C-Gate session
     * @throws CGateException
     */
    public void open(CGateSession cgate_session) throws CGateException
    {
        handle200Response(cgate_session.sendCommand("net open //" + project.getName() + "/" + net_id));
    }

    ArrayList<String> dbget(CGateSession cgate_session, String param_name) throws CGateException
    {
        return cgate_session.sendCommand("dbget //" + project.getName() + "/" + net_id + (param_name == null ? "" : ("/" + param_name)));
    }

    /**
     * Get all Application objects for this Network.
     * 
     * @param cgate_session The CGateSession
     * @return ArrayList of Applications
     * @throws CGateException
     */
    public ArrayList<Application> getApplications(CGateSession cgate_session) throws CGateException
    {
        ArrayList<String> resp_array = dbget(cgate_session, null);

        int number_of_applications = -1;
        for (String response : resp_array)
        {
            String address = "//" + project.getName() + "/" + net_id + "/Application[";
            int index = response.indexOf(address);
            if (index > -1)
            {
                int index2 = response.indexOf("]", index + address.length());
                number_of_applications = Integer.parseInt(response.substring(index + address.length(), index2));
                break;
            }
        }

        ArrayList<Application> applications = new ArrayList<Application>();
        for (int i = 1; i <= number_of_applications; i++)
        {
            resp_array = dbget(cgate_session, "Application[" + i + "]/Address");
            Application application = Application.getOrCreateApplication(cgate_session, this, resp_array.get(0));
            if (application != null)
                applications.add(application);
        }

        return applications;
    }
}
