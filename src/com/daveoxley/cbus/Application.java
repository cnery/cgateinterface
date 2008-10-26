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
public class Application extends CGateObject
{
    private Network network;

    private int application_id;

    private Application(CGateSession cgate_session, Network network, int application_id)
    {
        super(cgate_session);
        this.network = network;
        this.application_id = application_id;
        setupSubtreeCache("group");
    }

    @Override
    protected String getKey()
    {
        return String.valueOf(application_id);
    }

    @Override
    public CGateObject getCGateObject(String address) throws CGateException
    {
        if (address.startsWith("//"))
            throw new IllegalArgumentException("Address must be a relative address. i.e. Not starting with //");

        boolean return_next = false;
        int next_part_index = address.indexOf("/");
        if (next_part_index == -1)
        {
            next_part_index = address.length();
            return_next = true;
        }

        int group_id = Integer.parseInt(address.substring(0, next_part_index));
        Group group = getGroup(group_id);
        if (return_next)
            return group;

        return group.getCGateObject(address.substring(next_part_index + 1));
    }

    @Override
    public String getAddress()
    {
        return "//" + getNetwork().getProjectName() + "/" + getNetwork().getNetworkID() + "/" + getApplicationID();
    }

    static Application getOrCreateApplication(CGateSession cgate_session, Network network, String response)
    {
        int index = response.indexOf("=");
        String application_id = response.substring(index + 1);

        if (application_id.equals("255"))
            return null;

        Application application = (Application)network.getCachedObject("application", application_id);
        if (application == null)
        {
            application = new Application(cgate_session, network, Integer.parseInt(application_id));
            network.cacheObject("application", application);
        }
        return application;
    }

    Network getNetwork()
    {
        return network;
    }

    public int getApplicationID()
    {
        return application_id;
    }

    public String getHexID()
    {
        return Integer.toHexString(application_id);
    }

    public String getName() throws CGateException
    {
        String address = getAddress() + "/TagName";
        ArrayList<String> resp_array = getCGateSession().sendCommand("dbget " + address).toArray();
        return responseToMap(resp_array.get(0), true).get(address);
    }

    public String getDescription() throws CGateException
    {
        String address = getAddress() + "/Description";
        ArrayList<String> resp_array = getCGateSession().sendCommand("dbget " + address).toArray();
        return responseToMap(resp_array.get(0), true).get(address);
    }

    Response dbget(String param_name) throws CGateException
    {
        return getCGateSession().sendCommand("dbget " + getAddress() + (param_name == null ? "" : ("/" + param_name)));
    }

    public ArrayList<Group> getGroups() throws CGateException
    {
        network.tree();

        Response resp = dbget(null);

        int number_of_groups = -1;
        for (String response : resp) {
            String address = getAddress() + "/Group[";
            int index = response.indexOf(address);
            if (index > -1) {
                int index2 = response.indexOf("]", index + address.length());
                number_of_groups = Integer.parseInt(response.substring(index + address.length(), index2));
                break;
            }
        }

        ArrayList<Group> groups = new ArrayList<Group>();
        for (int i = 1; i <= number_of_groups; i++) {
            ArrayList<String> resp_array = dbget("Group[" + i + "]/Address").toArray();
            Group group = Group.getOrCreateGroup(getCGateSession(), this, resp_array.get(0));
            if (group != null)
                groups.add(group);
        }

        return groups;
    }

    /**
     * Retrieve the Group Object for the specified group id.
     *
     * @param group_id The group to retrieve
     * @return The Group
     * @throws CGateException
     */
    public Group getGroup(int group_id) throws CGateException
    {
        getGroups();

        return (Group)getCachedObject("group", String.valueOf(group_id));
    }
}
