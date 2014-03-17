package org.newsclub.net.unix;


import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;


/**
 * This test ensures that the test cases written for the junixsocket also pass
 * for the standard Java Internet sockets. If they do not, the tests may be
 * incorrect. As much as possible, junixsocket's AFUnixSocket should emulate the regular
 * Java Internet sockets so they can be used with all existing tools.
 * 
 * See http://code.google.com/p/junixsocket/issues/detail?id=9
 * 
 * @author Derrick Rice (April, 2010)
 */
public class EndOfFileJavaTest extends EndOfFileTest {

    int port;


    @Override
    @Before
    public void setup () throws IOException {
        String explicitPort = System.getProperty("org.newsclub.net.unix.testport");
        if ( explicitPort != null )
            this.port = new Integer(explicitPort);
        else
            this.port = 14842;

        this.server = new ServerSocket(this.port, 1, InetAddress.getByName("127.0.0.1"));
        this.executor = Executors.newFixedThreadPool(2);
    }


    @Override
    @After
    public void teardown () {
        super.teardown();
    }


    @SuppressWarnings ( "resource" )
    @Override
    Socket[] connectToServer () throws Exception {
        Socket clientSocket = new Socket();
        Future<Socket> serverAcceptFuture = this.executor.submit(new Callable<Socket>() {

            @Override
            public Socket call () throws Exception {
                return EndOfFileJavaTest.this.server.accept();
            }
        });

        Thread.sleep(100);

        clientSocket.connect(new InetSocketAddress(InetAddress.getByName("127.0.0.1"), this.port));

        Socket serverSocket = serverAcceptFuture.get(100, TimeUnit.MILLISECONDS);

        return new Socket[] {
            serverSocket, clientSocket
        };
    }


    @Override
    @Test ( timeout = 2000 )
    public void bidirectionalSanity () throws Exception {
        super.bidirectionalSanity();
    }


    @Override
    @Test ( timeout = 2000 )
    public void clientReadEof () throws Exception {
        super.clientReadEof();
    }


    @Override
    @Test ( timeout = 2000 )
    public void clientWriteToSocketClosedByClient () throws Exception {
        super.clientWriteToSocketClosedByClient();
    }


    @Override
    @Test ( timeout = 2000 )
    public void clientWriteToSocketClosedByServer () throws Exception {
        super.clientWriteToSocketClosedByServer();
    }


    @Override
    @Test ( timeout = 2000 )
    public void serverReadEof () throws Exception {
        super.serverReadEof();
    }


    @Override
    @Test ( timeout = 2000 )
    public void serverWriteToSocketClosedByClient () throws Exception {
        super.serverWriteToSocketClosedByClient();
    }


    @Override
    @Test ( timeout = 2000 )
    public void serverWriteToSocketClosedByServer () throws Exception {
        super.serverWriteToSocketClosedByServer();
    }
}
