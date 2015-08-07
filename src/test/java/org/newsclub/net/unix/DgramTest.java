/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 09.03.2014 by mbechler
 */
package org.newsclub.net.unix;


import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.management.ManagementFactory;

import org.apache.log4j.Logger;
import org.junit.Test;


/**
 * @author mbechler
 * 
 */
public class DgramTest extends SocketTestBase {

    static final Logger log = Logger.getLogger(DgramTest.class);

    static final String procName = ManagementFactory.getRuntimeMXBean().getName();
    static final int pid = Integer.parseInt(procName.substring(0, procName.indexOf('@')));

    private AFUNIXSocketAddress socketAddress;


    /**
     * @throws IOException
     */
    public DgramTest () throws IOException {
        super();
        this.socketAddress = new AFUNIXSocketAddress(new File("/tmp/test-dgram.sock"), 0, false, true);
    }


    @Override
    protected AFUNIXServerSocket startServer () throws IOException {
        final AFUNIXServerSocket server = AFUNIXServerSocket.newInstance();
        server.bind(this.socketAddress);
        return server;
    }


    @Override
    protected AFUNIXSocket connectToServer () throws IOException {
        return AFUNIXSocket.connectTo(this.socketAddress);
    }


    @SuppressWarnings ( {
        "unused", "resource"
    } )
    @Test
    public void testDgramSocket () throws Exception {

        ServerThread serverThread = new ServerThread() {

            @Override
            protected void handleConnection ( final AFUNIXSocket sock ) throws IOException {
                System.err.println("Server running");
                InputStream is = sock.getInputStream();
                OutputStream os = sock.getOutputStream();
                assertEquals(1, is.read());
                assertEquals(2, is.read());
                assertEquals(3, is.read());
                assertEquals(4, is.read());
                System.err.println("Read on server");
            }
        };

        try ( AFUNIXSocket sock = connectToServer() ) {
            System.err.println("Client running");
            InputStream is = sock.getInputStream();
            OutputStream os = sock.getOutputStream();
            os.write(1);
            os.write(2);
            os.write(3);
            os.write(4);
            System.err.println("Wrote from client");
        }
        catch ( Exception e ) {
            e.printStackTrace();
        }

        serverThread.checkException();
    }
}
