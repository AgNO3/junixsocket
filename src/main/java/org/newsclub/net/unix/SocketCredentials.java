/**
 * junixsocket
 *
 * Copyright (c) 2014 AgNO3 Gmbh &amp; Co. KG
 *
 * The author licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.newsclub.net.unix;


/**
 * @author mbechler
 * 
 */
public final class SocketCredentials {

    private final byte data;
    private final int pid;
    private final int uid;
    private final int gid;


    SocketCredentials ( int[] data ) {

        this.data = (byte) data[ 0 ];

        if ( data.length == 4 ) {
            this.pid = data[ 1 ];
            this.uid = data[ 2 ];
            this.gid = data[ 3 ];
        }
        else {
            this.pid = -1;
            this.uid = -1;
            this.gid = -1;
        }
    }


    /**
     * @return the data
     */
    public byte getData () {
        return this.data;
    }


    /**
     * @return the pid
     */
    public int getPid () {
        return this.pid;
    }


    /**
     * @return the uid
     */
    public int getUid () {
        return this.uid;
    }


    /**
     * @return the gid
     */
    public int getGid () {
        return this.gid;
    }

}
