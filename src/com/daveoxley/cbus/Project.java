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

import com.daveoxley.cbus.Project.ProjectState;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author Dave Oxley <dave@daveoxley.co.uk>
 */
public final class Project
{
    /**
     * 
     */
    public enum ProjectState {

        /**
         * Project not yet loaded.
         */
        not_loaded,
        /**
         * Project is starting.
         */
        starting,
        /**
         * Project is stopping.
         */
        stopping,
        /**
         * Project is started.
         */
        started,
        /**
         * Project is stopped.
         */
        stopped
    };

    private String project_name;

    private ProjectState state;

    private final HashMap<Integer,Network> cached_networks = new HashMap<Integer,Network>();

    private Project(String cgate_response)
    {
        state = ProjectState.not_loaded;
        updateFromResponse(cgate_response);
    }

    /**
     * Issue a <code>project dir</code> and <code>project list</code> to the
     * C-Gate server. The <code>project list</code> is run to update the state
     * of the projects that are loaded already.
     * 
     * @see <a href="http://www.clipsal.com/cis/downloads/Toolkit/CGateServerGuide_1_0.pdf">
     *      <i>C-Gate Server Guide 4.3.89 and 4.3.90</i></a>
     * @param cgate_session The C-Gate session
     * @return ArrayList of Projects
     * @throws CGateException
     */
    public static ArrayList<Project> dir(CGateSession cgate_session) throws CGateException
    {
        ArrayList<String> resp_array = cgate_session.sendCommand("project dir");

        ArrayList<Project> projects = new ArrayList<Project>();
        for (String response : resp_array)
            projects.add(getOrCreateProject(cgate_session, response));

        // Do a list so that the status of the projects are up to date
        list(cgate_session);

        return projects;
    }

    /**
     * Issue a <code>project dir</code> to the C-Gate server.
     * 
     * @see <a href="http://www.clipsal.com/cis/downloads/Toolkit/CGateServerGuide_1_0.pdf">
     *      <i>C-Gate Server Guide 4.3.90</i></a>
     * @param cgate_session The C-Gate session
     * @return ArrayList of Projects
     * @throws CGateException
     */
    public static ArrayList<Project> list(CGateSession cgate_session) throws CGateException
    {
        ArrayList<String> resp_array = cgate_session.sendCommand("project list");

        ArrayList<Project> projects = new ArrayList<Project>();
        for (String response : resp_array)
            projects.add(getOrCreateProject(cgate_session, response));

        return projects;
    }

    /**
     * Retrieve the Project Object for the specified project name.
     * 
     * @param cgate_session The CGateSession
     * @param project_name The project name to retrieve
     * @return The Project
     * @throws CGateException
     */
    public static Project getProject(CGateSession cgate_session, String project_name) throws CGateException
    {
        dir(cgate_session);

        return cgate_session.getCachedProject(project_name);
    }

    /**
     * Retrieve the Network Object for the specified network id.
     * 
     * @param cgate_session The CGateSession
     * @param network_id The network to retrieve
     * @return The Network
     * @throws CGateException
     */
    public Network getNetwork(CGateSession cgate_session, int network_id) throws CGateException
    {
        Network.listAll(cgate_session);

        return getCachedNetwork(network_id);
    }

    static Project getOrCreateProject(CGateSession cgate_session, String cgate_response) throws CGateException
    {
        String project_name = null;

        HashMap<String,String> resp_map = Utils.responseToMap(cgate_response);
        project_name = resp_map.get("project");

        if (project_name == null)
            throw new CGateException();

        Project project = cgate_session.getCachedProject(project_name);
        if (project == null)
        {
            project = new Project(cgate_response);
            cgate_session.cacheProject(project);
        }
        project.updateFromResponse(cgate_response);
        return project;
    }

    private void updateFromResponse(String cgate_response)
    {
        HashMap<String,String> resp_map = Utils.responseToMap(cgate_response);
        String _project_name = resp_map.get("project");
        if (_project_name != null)
            project_name = _project_name;
        String _state = resp_map.get("state");
        if (_state != null)
            state = ProjectState.valueOf(_state);
    }

    /**
     * Get the project name.
     *
     * @return The project name
     */
    public String getName()
    {
        return project_name;
    }

    /**
     * Get the project state.
     *
     * @return The ProjectState
     */
    public ProjectState getState()
    {
        return state;
    }

    Network getCachedNetwork(int net_id)
    {
        return cached_networks.get(net_id);
    }

    void cacheNetwork(Network network)
    {
        cached_networks.put(network.getNetworkID(), network);
    }

    /**
     * Issue a <code>project close <i>project_name</i></code> to the C-Gate server.
     * 
     * @see <a href="http://www.clipsal.com/cis/downloads/Toolkit/CGateServerGuide_1_0.pdf">
     *      <i>C-Gate Server Guide 4.3.86</i></a>
     * @param cgate_session The C-Gate session
     * @throws CGateException
     */
    public void close(CGateSession cgate_session) throws CGateException
    {
        ArrayList<String> resp_array = cgate_session.sendCommand("project close " + project_name);

        if (resp_array.isEmpty())
            throw new CGateException();

        String response = resp_array.get(0);
        String result_code = response.substring(0, 3).trim();
        if (!result_code.equals("200"))
            throw new CGateException(response);

        state = ProjectState.not_loaded;
    }

    /**
     * Issue a <code>project load <i>project_name</i></code> to the C-Gate server.
     * 
     * @see <a href="http://www.clipsal.com/cis/downloads/Toolkit/CGateServerGuide_1_0.pdf">
     *      <i>C-Gate Server Guide 4.3.91</i></a>
     * @param cgate_session The C-Gate session
     * @throws CGateException
     */
    public void load(CGateSession cgate_session) throws CGateException
    {
        ArrayList<String> resp_array = cgate_session.sendCommand("project load " + project_name);

        if (resp_array.isEmpty())
            throw new CGateException();

        String response = resp_array.get(0);
        String result_code = response.substring(0, 3).trim();
        if (!result_code.equals("200"))
            throw new CGateException(response);

        if (state == ProjectState.not_loaded)
            state = ProjectState.stopped;
    }

    /**
     * Issue a <code>project start <i>project_name</i></code> to the C-Gate server.
     * 
     * @see <a href="http://www.clipsal.com/cis/downloads/Toolkit/CGateServerGuide_1_0.pdf">
     *      <i>C-Gate Server Guide 4.3.96</i></a>
     * @param cgate_session The C-Gate session
     * @throws CGateException
     */
    public void start(CGateSession cgate_session) throws CGateException
    {
        ArrayList<String> resp_array = cgate_session.sendCommand("project start " + project_name);

        if (resp_array.isEmpty())
            throw new CGateException();

        String response = resp_array.get(0);
        String result_code = response.substring(0, 3).trim();
        if (!result_code.equals("200"))
            throw new CGateException(response);

        // TODO: Probably better to call list in a loop until state==STARTED
        //       than assuming that state is STARTED immediately after a start.
        state = ProjectState.started;
    }
}
