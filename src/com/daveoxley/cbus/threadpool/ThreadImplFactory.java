/**
 *  CGateInterface - A library to allow interaction with Clipsal C-Gate.
 *  Copyright (C) 2008,2009  Dave Oxley <dave@daveoxley.co.uk>.
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

package com.daveoxley.cbus.threadpool;

import org.apache.commons.pool.PoolableObjectFactory;

/**
 *
 * @author Dave Oxley <dave@daveoxley.co.uk>
 */
public class ThreadImplFactory implements PoolableObjectFactory
{
    /**
     *
     * @return
     * @throws java.lang.Exception
     */
    public Object makeObject() throws Exception
    {
        ThreadImpl thread = new ThreadImpl();
        thread.start();
        return thread;
    }

    /**
     *
     * @param obj
     * @throws java.lang.Exception
     */
    public void destroyObject(Object obj) throws Exception
    {
        ThreadImpl thread = (ThreadImpl)obj;
        thread.finish();
    }

    /**
     *
     * @param obj
     * @return
     */
    public boolean validateObject(Object obj)
    {
        ThreadImpl thread = (ThreadImpl)obj;
        synchronized (thread)
        {
            return !thread.isBorrowed() &&
                thread.getThreadGroup() != null &&
                thread.isRunning();
        }
    }

    /**
     *
     * @param obj
     * @throws java.lang.Exception
     */
    public void activateObject(Object obj) throws Exception {}

    /**
     *
     * @param obj
     * @throws java.lang.Exception
     */
    public void passivateObject(Object obj) throws Exception {}
}
