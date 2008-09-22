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
import java.util.HashMap;

/**
 *
 * @author Dave Oxley <dave@daveoxley.co.uk>
 */
public abstract class CGateObject
{
    static void handle200Response(ArrayList<String> resp_array) throws CGateException
    {
        if (resp_array.isEmpty())
            throw new CGateException();

        String response = resp_array.get(0);
        String result_code = response.substring(0, 3).trim();
        if (!result_code.equals("200"))
            throw new CGateException(response);
    }

    static HashMap<String,String> responseToMap(String cgate_response)
    {
        HashMap<String,String> map = new HashMap<String,String>();
        String resp_array[] = cgate_response.substring(4).split(" ");
        for (String resp : resp_array)
        {
            int index = resp.indexOf("=");
            if (index > -1)
                map.put(resp.substring(0, index), resp.substring(index + 1));
        }
        return map;
    }

    protected abstract String getKey();

    private final HashMap<String,HashMap<String,CGateObject>> subtree_cache = new HashMap<String,HashMap<String,CGateObject>>();

    protected void setupSubtreeCache(String cache_key)
    {
        subtree_cache.put(cache_key, new HashMap<String,CGateObject>());
    }

    protected CGateObject cacheObject(String cache_key, CGateObject cgate_obj)
    {
        return subtree_cache.get(cache_key).put(cgate_obj.getKey(), cgate_obj);
    }

    protected CGateObject uncacheObject(String cache_key, CGateObject cgate_obj)
    {
        return subtree_cache.get(cache_key).remove(cgate_obj);
    }

    protected CGateObject getCachedObject(String cache_key, String key)
    {
        return subtree_cache.get(cache_key).get(key);
    }

    protected void clearCache()
    {
        for (HashMap<String,CGateObject> map: subtree_cache.values())
            map.clear();
    }
}
