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
public class Group extends CGateObject
{
    private Application application;

    private int group_id;

    Group(Application application, String cgate_response, boolean tree_resp)
    {
        this.application = application;
        if (tree_resp)
            this.group_id = getGroupID(application.getNetwork(), cgate_response);
        else
        {
            int index = cgate_response.indexOf("=");
            this.group_id = Integer.parseInt(cgate_response.substring(index + 1));
        }
    }

    @Override
    protected String getKey()
    {
        return String.valueOf(group_id);
    }

    static Group getOrCreateGroup(CGateSession cgate_session, Network network, String response) throws CGateException
    {
        String application_type = Network.getApplicationType(network, response);
        int group_id = getGroupID(network, response);

        if (!application_type.equals("p"))
        {
            Application application = network.getApplication(cgate_session, Integer.parseInt(application_type));
            Group group = (Group)application.getCachedObject("group", String.valueOf(group_id));
            if (group == null)
            {
                group = new Group(application, response, true);
                application.cacheObject("group", group);
            }
            return group;
        }
        return null;
    }

    static Group getOrCreateGroup(CGateSession cgate_session, Application application, String response) throws CGateException
    {
        int index = response.indexOf("=");
        String group_id = response.substring(index + 1);

        if (group_id.equals("255"))
            return null;

        Group group = (Group)application.getCachedObject("group", group_id);
        if (group == null)
        {
            group = new Group(application, response, false);
            application.cacheObject("group", group);
        }
        return group;
    }

    static int getGroupID(Network network, String response)
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
    public int getGroupID()
    {
        return group_id;
    }

    /**
     *
     * @return
     */
    private Network getNetwork()
    {
        return application.getNetwork();
    }

    public String getName(CGateSession cgate_session) throws CGateException
    {
        String address = "//" + getNetwork().getProjectName() + "/" + getNetwork().getNetworkID() + "/" +
                String.valueOf(application.getApplicationID()) + "/" + group_id + "/TagName";
        ArrayList<String> resp_array = cgate_session.sendCommand("dbget " + address);
        return responseToMap(resp_array.get(0), true).get(address);
    }

    /**
     * Issue a <code>on //PROJECT/NET_ID/GROUP_ID</code> to the C-Gate server.
     * 
     * @see <a href="http://www.clipsal.com/cis/downloads/Toolkit/CGateServerGuide_1_0.pdf">
     *      <i>C-Gate Server Guide 4.3.79</i></a>
     * @param cgate_session The C-Gate session
     * @throws CGateException
     */
    public void on(CGateSession cgate_session) throws CGateException
    {
        String address = "//" + getNetwork().getProjectName() + "/" + getNetwork().getNetworkID() +
                "/" + application.getApplicationID() + "/" + getGroupID();
        handle200Response(cgate_session.sendCommand("on " + address));
    }

    /**
     * Issue a <code>off //PROJECT/NET_ID/GROUP_ID</code> to the C-Gate server.
     * 
     * @see <a href="http://www.clipsal.com/cis/downloads/Toolkit/CGateServerGuide_1_0.pdf">
     *      <i>C-Gate Server Guide 4.3.77</i></a>
     * @param cgate_session The C-Gate session
     * @throws CGateException
     */
    public void off(CGateSession cgate_session) throws CGateException
    {
        String address = "//" + getNetwork().getProjectName() + "/" + getNetwork().getNetworkID() +
                "/" + application.getApplicationID() + "/" + getGroupID();
        handle200Response(cgate_session.sendCommand("off " + address));
    }

    /**
     * Issue a <code>ramp //PROJECT/NET_ID/GROUP_ID</code> to the C-Gate server.
     * 
     * @see <a href="http://www.clipsal.com/cis/downloads/Toolkit/CGateServerGuide_1_0.pdf">
     *      <i>C-Gate Server Guide 4.3.100</i></a>
     * @param cgate_session The C-Gate session
     * @param level
     * @param seconds
     * @throws CGateException
     */
    public void ramp(CGateSession cgate_session, int level, int seconds) throws CGateException
    {
        String address = "//" + getNetwork().getProjectName() + "/" + getNetwork().getNetworkID() +
                "/" + application.getApplicationID() + "/" + getGroupID();
        handle200Response(cgate_session.sendCommand("ramp " + address + " " + level + " " + seconds + "s"));
    }
}
