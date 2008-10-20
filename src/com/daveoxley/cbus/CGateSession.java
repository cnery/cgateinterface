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
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PipedReader;
import java.io.PipedWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
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

    private final Map<String,BufferedWriter> response_writers = Collections.synchronizedMap(new HashMap<String,BufferedWriter>());

    private final CommandConnection command_connection;

    private final EventConnection event_connection;

    private final PingConnections ping_connections;

    private boolean connected = false;

    CGateSession(InetAddress cgate_server, int command_port, int event_port) throws CGateConnectException
    {
        setupSubtreeCache("project");
        try {
            command_connection = new CommandConnection(cgate_server, command_port);
            event_connection = new EventConnection(cgate_server, event_port);
            if (DebugEventCallback.isDebugEnabled())
                registerEventCallback(new DebugEventCallback());
            connected = true;
            ping_connections = new PingConnections();
        }
        catch (CGateConnectException e)
        {
            try
            {
                close();
            }
            catch (Exception e2) {}
            throw e;
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
        synchronized (ping_connections)
        {
            try
            {
                sendCommand("quit");
            }
            catch (Exception e) {}

            try
            {
                command_connection.stop();
                event_connection.stop();
            }
            catch (Exception e)
            {
                throw new CGateException(e);
            }
            finally
            {
                clearCache();
                connected = false;
                ping_connections.notify();
            }
        }
    }

    /**
     * 
     * @param cgate_command
     * @return ArrayList of C-Gate response lines
     * @throws com.daveoxley.cbus.CGateException
     */
    ArrayList<String> sendCommand(String cgate_command) throws CGateException
    {
        checkConnected();

        return command_connection.sendCommand(cgate_command);
    }

    public boolean isConnected()
    {
        return connected;
    }

    private void checkConnected() throws CGateNotConnectedException
    {
        if (!connected)
            throw new CGateNotConnectedException();
        try
        {
            command_connection.start();
            event_connection.start();
        }
        catch (CGateConnectException e)
        {
            throw new CGateNotConnectedException();
        }
    }

    private static boolean responseHasMore(String response)
    {
        return response.substring(3,4).equals("-");
    }

    private abstract class CGateConnection implements Runnable
    {
        private final InetAddress server;

        private final int port;

        private final boolean create_output;

        private Thread thread = null;

        private Socket socket;

        private volatile BufferedReader input_reader;

        private PrintWriter output_stream;

        protected CGateConnection(InetAddress server, int port, boolean create_output) throws CGateConnectException
        {
            this.server = server;
            this.port = port;
            this.create_output = create_output;
            start();
        }

        protected synchronized void start() throws CGateConnectException
        {
            if (thread != null)
                return;

            try
            {
                socket = new Socket(server, port);
                input_reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                if (create_output)
                    output_stream = new PrintWriter(socket.getOutputStream(), true);
                logConnected();

                thread = new Thread(this);
                thread.setDaemon(true);
                thread.start();
            }
            catch (IOException e)
            {
                throw new CGateConnectException(e);
            }
        }

        protected synchronized void stop()
        {
            try
            {
                thread = null;

                // Only close the Socket as trying to close the BufferedReader results
                // in a deadlock (http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4859836).
                try
                {
                    if (socket != null)
                        socket.close();
                }
                catch (IOException e)
                {
                    new CGateException(e);
                }
            }
            catch (Exception e)
            {
                new CGateException(e);
            }
            finally
            {
                input_reader = null;
                output_stream = null;
                socket = null;
            }
        }

        public void println(String str) throws CGateException
        {
            if (!create_output)
                throw new CGateException();

            output_stream.println(str);
            output_stream.flush();
        }

        protected final BufferedReader getInputReader()
        {
            return input_reader;
        }

        protected void logConnected() throws IOException {}

        protected synchronized boolean continueRunning()
        {
            return thread != null;
        }

        public final void run()
        {
            try
            {
                while (continueRunning())
                {
                    try
                    {
                        doRun();
                    }
                    catch (IOException ioe)
                    {
                        if (thread != null)
                            new CGateException(ioe);
                    }
                    catch (Exception e)
                    {
                        new CGateException(e);
                    }
                }
            }
            finally
            {
                boolean restart = thread != null;
                stop();
                if (restart)
                {
                    try
                    {
                        start();
                    }
                    catch (CGateConnectException e) {}
                }
            }
        }

        protected abstract void doRun() throws IOException;
    }

    private class PingConnections implements Runnable
    {
        private final Thread thread;

        private PingConnections()
        {
            thread = new Thread(this);
            thread.setDaemon(true);
            thread.start();
        }

        public synchronized void run()
        {
            while (connected)
            {
                try
                {
                    try
                    {
                        wait(10000l);
                    }
                    catch (InterruptedException e) {}

                    if (connected)
                        CGateInterface.noop(CGateSession.this);
                }
                catch (Exception e) {}
            }
        }
    }

    private BufferedReader getReader(String id) throws CGateException
    {
        try
        {
            PipedWriter piped_writer = new PipedWriter();
            BufferedWriter out = new BufferedWriter(piped_writer);
            response_writers.put(id, out);

            PipedReader piped_reader = new PipedReader(piped_writer);
            return new BufferedReader(piped_reader);
        }
        catch(IOException e)
        {
            throw new CGateException(e);
        }
    }

    private class CommandConnection extends CGateConnection
    {
        private int next_id = 0;

        private CommandConnection(InetAddress server, int port) throws CGateConnectException
        {
            super(server, port, true);
        }

        private ArrayList<String> sendCommand(String cgate_command) throws CGateException
        {
            String id = getID();
            BufferedReader response_reader = getReader(id);

            command_connection.println("[" + id + "] " + cgate_command);

            ArrayList<String> array_response = new ArrayList<String>();
            try
            {
                boolean has_more = true;
                while (has_more)
                {
                    String response = response_reader.readLine();
                    array_response.add(response);
                    has_more = responseHasMore(response);
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

        private synchronized String getID()
        {
            return String.valueOf(next_id++);
        }

        @Override
        protected void logConnected() throws IOException
        {
            log.debug(getInputReader().readLine());
        }

        @Override
        public void doRun() throws IOException
        {
            final String response = getInputReader().readLine();
            if (response != null)
            {
                int id_end = response.indexOf("]");
                String id = response.substring(1, id_end);
                String actual_response = response.substring(id_end + 2);

                BufferedWriter writer = response_writers.get(id);
                writer.write(actual_response);
                writer.newLine();

                if (!responseHasMore(actual_response))
                {
                    writer.flush();
                    writer.close();
                    response_writers.remove(id);
                }
            }
        }
    }

    /**
     *
     * @param event_callback
     */
    public void registerEventCallback(EventCallback event_callback) throws CGateConnectException
    {
        event_connection.registerEventCallback(event_callback);
    }

    private class EventConnection extends CGateConnection
    {
        private final ThreadImplPool event_callback_pool;

        private final List<EventCallback> event_callbacks = Collections.synchronizedList(new ArrayList<EventCallback>());

        private EventConnection(InetAddress server, int port) throws CGateConnectException
        {
            super(server, port, false);
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

        private void registerEventCallback(EventCallback event_callback) throws CGateConnectException
        {
            event_callbacks.add(event_callback);
            start();
        }

        @Override
        protected void doRun() throws IOException
        {
            final String event = getInputReader().readLine();
            if(event.length() >= 19)
            {
                final int event_code = Integer.parseInt(event.substring(16, 19).trim());
                for (final EventCallback event_callback : event_callbacks)
                {
                    if (!continueRunning())
                        return;

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
    }
}
