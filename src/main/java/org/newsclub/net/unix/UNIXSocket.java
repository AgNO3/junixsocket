/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 07.03.2014 by mbechler
 */
package org.newsclub.net.unix;


import java.net.SocketException;


/**
 * @author mbechler
 * 
 */
public interface UNIXSocket {

    /**
     * @param passCred
     * @throws SocketException
     */
    void setPassCred ( boolean passCred ) throws SocketException;

}
