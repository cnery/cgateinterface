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

import com.daveoxley.cbus.events.DebugEventCallback;
import com.daveoxley.cbus.events.EventCallback;
import com.daveoxley.cbus.threadpool.ThreadImpl;
import com.daveoxley.cbus.threadpool.ThreadImplPool;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.apache.commons.pool.impl.GenericObjectPool.Config;

/**
 *
 * @author Dave Oxley <dave@daveoxley.co.uk>
 */
public class CGateSession extends CGateObject
{
    private final static Log log = LogFactory.getLog(CGateSession.class);

    private final Socket command_socket;

    private final BufferedReader response_input_stream;

    private final PrintWriter output_stream;

    private final Socket event_socket;

    private final BufferedReader event_input_stream;

    private boolean connected = false;

    private final EventController event_controller = new EventController();

    private final ArrayList<EventCallback> event_callbacks = new ArrayList<EventCallback>();

    CGateSession(InetAddress cgate_server, int command_port, int event_port) throws CGateConnectException
    {
        setupSubtreeCache("project");
        try {
            command_socket = new Socket(cgate_server, command_port);
            response_input_stream = new BufferedReader(new InputStreamReader(command_socket.getInputStream()));
            output_stream = new PrintWriter(command_socket.getOutputStream(), true);
            event_socket = new Socket(cgate_server, event_port);
            event_input_stream = new BufferedReader(new InputStreamReader(event_socket.getInputStream()));
            connected = true;
            if (DebugEventCallback.isDebugEnabled())
                registerEventCallback(new DebugEventCallback());
            String response = response_input_stream.readLine();
            log.debug(response);
        }
        catch (IOException e)
        {
            try
            {
                close();
            }
            catch (Exception e2) {}
            throw new CGateConnectException(e);
        }
    }

    @Override
    protected String getKey()
    {
        return null;
    }
    
    /**
     * Issue a <code>quit</code> to the C-Gate server and close the input and output stream
     * and the command_socket.
     *
     * @see <a href="http://www.clipsal.com/cis/downloads/Toolkit/CGateServerGuide_1_0.pdf">
     *      <i>C-Gate Server Guide 4.3.99</i></a>
     * @throws com.daveoxley.cbus.CGateException
     */
    public void close() throws CGateException
    {
        try
        {
            sendCommand("quit");
            event_controller.stop();
            clearCache();
        }
        catch(Exception e) {}

        try
        {
            if (output_stream != null) output_stream.close();
            if (response_input_stream != null) response_input_stream.close();
            if (command_socket != null) command_socket.close();
        }
        catch(IOException e)
        {
            throw new CGateException(e);
        }
        finally
        {
            try
            {
                if (event_input_stream != null) event_input_stream.close();
                if (event_socket != null) event_socket.close();
            }
            catch(IOException e)
            {
                throw new CGateException(e);
            }
            finally
            {
                connected = false;
            }
        }
    }

    /**
     * 
     * @param cgate_command
     * @return ArrayList of C-Gate response lines
     * @throws com.daveoxley.cbus.CGateException
     */
    synchronized ArrayList<String> sendCommand(String cgate_command) throws CGateException
    {
        checkConnected();

        output_stream.println(cgate_command);
        output_stream.flush();

        ArrayList<String> array_response = new ArrayList<String>();
        try
        {
            boolean has_more = true;
            while (has_more)
            {
                String response = response_input_stream.readLine();
                array_response.add(response);
                has_more = response.substring(3,4).equals("-");
            }

            if (log.isDebugEnabled())
            {
                for (String response : array_response)
                    log.debug("response: " + response);
            }
        }
        catch(IOException e)
        {
            throw new CGateException(e);
        }

        return array_response;
    }

    public boolean isConnected()
    {
        return connected;
    }

    private void checkConnected() throws CGateNotConnectedException
    {
        if (!connected)
            throw new CGateNotConnectedException();
    }

    /**
     *
     * @param event_callback
     */
    public void registerEventCallback(EventCallback event_callback)
    {
        event_callbacks.add(event_callback);
        event_controller.start();
    }

    private class EventController implements Runnable
    {
        private Thread thread = null;

        private final ThreadImplPool event_callback_pool;

        private EventController()
        {
            Config config = new Config();
            config.maxActive = 10;
            config.minIdle   = 2;
            config.maxIdle   = 5;
            config.testOnBorrow = false;
            config.testOnReturn = true;
            config.whenExhaustedAction = GenericObjectPool.WHEN_EXHAUSTED_BLOCK;
            config.maxWait = -1;
            event_callback_pool = new ThreadImplPool(config);
        }

        private synchronized void start()
        {
            if (thread != null)
                return;

            thread = new Thread(this);
            thread.setDaemon(true);
            thread.start();
        }

        private synchronized void stop()
        {
            thread = null;
        }

        private synchronized boolean continueRunning()
        {
            return thread != null;
        }

        public void run()
        {
            while (continueRunning())
            {
                try
                {
                    final String event = CGateSession.this.event_input_stream.readLine();
                    if(event.length() >= 19)
                    {
                        final int event_code = Integer.parseInt(event.substring(16, 19).trim());
                        for (final EventCallback event_callback : event_callbacks)
                        {
                            try
                            {
                                if (event_callback.acceptEvent(event_code))
                                {
                                    ThreadImpl callback_thread = (ThreadImpl)event_callback_pool.borrowObject();
                                    callback_thread.execute(new Runnable() {
                                        public void run()
                                        {
                                            GregorianCalendar event_time = new GregorianCalendar(
                                                    Integer.parseInt(event.substring(0, 4)),
                                                    Integer.parseInt(event.substring(4, 6)),
                                                    Integer.parseInt(event.substring(6, 8)),
                                                    Integer.parseInt(event.substring(9, 11)),
                                                    Integer.parseInt(event.substring(11, 13)),
                                                    Integer.parseInt(event.substring(13, 15)));

                                            event_callback.processEvent(CGateSession.this, event_code,
                                                    event_time, event.length() == 19 ? null : event.substring(19));
                                        }
                                    });
                                }
                            }
                            catch (Exception e)
                            {
                                new CGateException(e);
                            }
                        }
                    }
                }
                catch (IOException e)
                {
                    new CGateException(e);
                }
            }
        }
    }
}
