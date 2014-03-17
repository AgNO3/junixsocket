/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 09.03.2014 by mbechler
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
