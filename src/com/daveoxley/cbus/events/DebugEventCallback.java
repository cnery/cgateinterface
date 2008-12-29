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

package com.daveoxley.cbus.events;

import com.daveoxley.cbus.CGateSession;
import java.util.GregorianCalendar;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author Dave Oxley <dave@daveoxley.co.uk>
 */
public class DebugEventCallback extends EventCallback
{
    private final static Log log = LogFactory.getLog(DebugEventCallback.class);

    @Override
    public boolean acceptEvent(int event_code)
    {
        return log.isDebugEnabled(); // Accept all events if debug enabled
    }

    @Override
    public void processEvent(CGateSession cgate_session, int event_code, GregorianCalendar event_time, String event)
    {
        log.debug("event_code: " + event_code + ", event_time: " + event_time.toString() + ", event: " + event);
    }

}
