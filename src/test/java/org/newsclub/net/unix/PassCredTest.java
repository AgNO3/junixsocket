/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 09.03.2014 by mbechler
 */
package org.newsclub.net.unix;


import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.lang.management.ManagementFactory;

import org.apache.log4j.Logger;
import org.junit.Test;

import com.sun.security.auth.module.UnixSystem;


/**
 * @author mbechler
 * 
 */
@SuppressWarnings ( "restriction" )
public class PassCredTest extends SocketTestBase {

    static final Logger log = Logger.getLogger(PassCredTest.class);

    static final String procName = ManagementFactory.getRuntimeMXBean().getName();
    static final int pid = Integer.parseInt(procName.substring(0, procName.indexOf('@')));


    /**
     * @throws IOException
     */
    public PassCredTest () throws IOException {
        super();
    }


    @Override
    protected AFUNIXServerSocket startServer () throws IOException {
        AFUNIXServerSocket server = super.startServer();
        server.setPassCred(true);
        return server;
    }


    @Test
    public void testCredentialPassing () throws Exception {
        final UnixSystem system = new UnixSystem();

        ServerThread serverThread = new ServerThread() {

            @Override
            protected void handleConnection ( final AFUNIXSocket sock ) throws IOException {
                log.debug("Got connection, retrieving credentials");
                SocketCredentials creds = sock.recieveCredentials();

                assertEquals((byte) 0, creds.getData());
                assertEquals(pid, creds.getPid());
                assertEquals(system.getUid(), creds.getUid());
                assertEquals(system.getGid(), creds.getGid());

                sock.sendCredentials(Byte.MIN_VALUE);
            }
        };

        try ( AFUNIXSocket sock = connectToServer() ) {
            sock.setPassCred(true);
            sock.sendCredentials((byte) 0);
            SocketCredentials creds = sock.recieveCredentials();
            assertEquals(Byte.MIN_VALUE, creds.getData());
            assertEquals(pid, creds.getPid());
            assertEquals(system.getUid(), creds.getUid());
            assertEquals(system.getGid(), creds.getGid());
        }

        serverThread.checkException();
    }
}
