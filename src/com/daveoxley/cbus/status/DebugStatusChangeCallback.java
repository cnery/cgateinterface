/*
 * CGateInterface - A library to allow interaction with Clipsal C-Gate.
 *
 * Copyright 2008, 2009, 2012, 2017 Dave Oxley <dave@daveoxley.co.uk>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
