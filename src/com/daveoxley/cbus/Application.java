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

        if (application_id.equals("255"))
            return null;

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

    public int getApplicationID()
    {
        return application_id;
    }

    public String getHexID()
    {
        return Integer.toHexString(application_id);
    }

    public String getName(CGateSession cgate_session) throws CGateException
    {
        String address = "//" + network.getProjectName() + "/" + network.getNetworkID() + "/" + String.valueOf(application_id) + "/TagName";
        ArrayList<String> resp_array = cgate_session.sendCommand("dbget " + address);
        return responseToMap(resp_array.get(0), true).get(address);
    }

    public String getDescription(CGateSession cgate_session) throws CGateException
    {
        String address = "//" + network.getProjectName() + "/" + network.getNetworkID() + "/" + String.valueOf(application_id) + "/Description";
        ArrayList<String> resp_array = cgate_session.sendCommand("dbget " + address);
        return responseToMap(resp_array.get(0), true).get(address);
    }

    ArrayList<String> dbget(CGateSession cgate_session, String param_name) throws CGateException
    {
        return cgate_session.sendCommand("dbget //" + network.getProjectName() + "/" +
                network.getNetworkID() + "/" + application_id + (param_name == null ? "" : ("/" + param_name)));
    }

    public ArrayList<Group> getGroups(CGateSession cgate_session) throws CGateException
    {
        network.tree(cgate_session);

        ArrayList<String> resp_array = dbget(cgate_session, null);

        int number_of_groups = -1;
        for (String response : resp_array) {
            String address = "//" + network.getProjectName() + "/" + network.getNetworkID() + "/" + application_id + "/Group[";
            int index = response.indexOf(address);
            if (index > -1) {
                int index2 = response.indexOf("]", index + address.length());
                number_of_groups = Integer.parseInt(response.substring(index + address.length(), index2));
                break;
            }
        }

        ArrayList<Group> groups = new ArrayList<Group>();
        for (int i = 1; i <= number_of_groups; i++) {
            resp_array = dbget(cgate_session, "Group[" + i + "]/Address");
            Group group = Group.getOrCreateGroup(cgate_session, this, resp_array.get(0));
            if (group != null)
                groups.add(group);
        }

        return groups;
    }
}
