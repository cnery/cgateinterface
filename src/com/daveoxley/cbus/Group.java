/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.daveoxley.cbus;

import java.util.ArrayList;

/**
 *
 * @author dave
 */
public class Group extends Unit
{
    Group(Network network, String cgate_response)
    {
        super(network, cgate_response);
    }

    /**
     * Issue a <code>on //PROJECT/NET_ID/GROUP_ID</code> to the C-Gate server.
     * 
     * @see <a href="http://www.clipsal.com/cis/downloads/Toolkit/CGateServerGuide_1_0.pdf">
     *      <i>C-Gate Server Guide 4.3.79</i></a>
     * @param cgate_session The C-Gate session
     */
    public void on(CGateSession cgate_session) throws CGateException
    {
        String address = "//" + getNetwork().getProjectName() + "/" + getNetwork().getNetworkID() + "/" + getUnitID();
        ArrayList<String> resp_array = cgate_session.sendCommand("on " + address);

        if (resp_array.isEmpty())
            throw new CGateException();

        String response = resp_array.get(0);
        String result_code = response.substring(0, 3).trim();
        if (!result_code.equals("200"))
            throw new CGateException(response);
    }

    /**
     * Issue a <code>off //PROJECT/NET_ID/GROUP_ID</code> to the C-Gate server.
     * 
     * @see <a href="http://www.clipsal.com/cis/downloads/Toolkit/CGateServerGuide_1_0.pdf">
     *      <i>C-Gate Server Guide 4.3.77</i></a>
     * @param cgate_session The C-Gate session
     */
    public void off(CGateSession cgate_session) throws CGateException
    {
        String address = "//" + getNetwork().getProjectName() + "/" + getNetwork().getNetworkID() + "/" + getUnitID();
        ArrayList<String> resp_array = cgate_session.sendCommand("off " + address);

        if (resp_array.isEmpty())
            throw new CGateException();

        String response = resp_array.get(0);
        String result_code = response.substring(0, 3).trim();
        if (!result_code.equals("200"))
            throw new CGateException(response);
    }

    /**
     * Issue a <code>ramp //PROJECT/NET_ID/GROUP_ID</code> to the C-Gate server.
     * 
     * @see <a href="http://www.clipsal.com/cis/downloads/Toolkit/CGateServerGuide_1_0.pdf">
     *      <i>C-Gate Server Guide 4.3.100</i></a>
     * @param cgate_session The C-Gate session
     */
    public void ramp(CGateSession cgate_session, int level, int seconds) throws CGateException
    {
        String address = "//" + getNetwork().getProjectName() + "/" + getNetwork().getNetworkID() + "/" + getUnitID();
        ArrayList<String> resp_array = cgate_session.sendCommand("ramp " + address + " " + level + " " + seconds + "s");

        if (resp_array.isEmpty())
            throw new CGateException();

        String response = resp_array.get(0);
        String result_code = response.substring(0, 3).trim();
        if (!result_code.equals("200"))
            throw new CGateException(response);
    }
}
