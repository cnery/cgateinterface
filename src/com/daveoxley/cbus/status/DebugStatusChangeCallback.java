/**
 *  CGateInterface - A library to allow interaction with Clipsal C-Gate.
 *  Copyright (C) 2008,2009,2012  Dave Oxley <dave@daveoxley.co.uk>.
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

package com.daveoxley.cbus.status;

import com.daveoxley.cbus.CGateSession;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author Dave Oxley <dave@daveoxley.co.uk>
 */
public class DebugStatusChangeCallback extends StatusChangeCallback
{
    private final static Log log = LogFactory.getLog(DebugStatusChangeCallback.class);

    public boolean isActive()
    {
        return log.isDebugEnabled();
    }

    @Override
    public void processStatusChange(CGateSession cgate_session, String status_change)
    {
        log.debug("status_change: " + status_change);
    }

}
