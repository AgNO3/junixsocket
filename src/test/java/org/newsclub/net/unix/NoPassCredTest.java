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


import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.lang.management.ManagementFactory;

import org.apache.log4j.Logger;
import org.junit.Test;


/**
 * @author mbechler
 * 
 */
public class NoPassCredTest extends SocketTestBase {

    static final Logger log = Logger.getLogger(NoPassCredTest.class);

    static final String procName = ManagementFactory.getRuntimeMXBean().getName();
    static final int pid = Integer.parseInt(procName.substring(0, procName.indexOf('@')));


    /**
     * @throws IOException
     */
    public NoPassCredTest () throws IOException {
        super();
    }


    @Override
    protected AFUNIXServerSocket startServer () throws IOException {
        AFUNIXServerSocket server = super.startServer();
        return server;
    }


    @Test
    public void testDisabledCredentialPassing () throws Exception {

        ServerThread serverThread = new ServerThread() {

            @Override
            protected void handleConnection ( final AFUNIXSocket sock ) throws IOException {
                log.debug("Got connection, retrieving credentials");
                SocketCredentials creds = sock.recieveCredentials();

                assertEquals((byte) 0, creds.getData());
                assertEquals(-1, creds.getPid());
                assertEquals(-1, creds.getUid());
                assertEquals(-1, creds.getGid());

                sock.sendCredentials(Byte.MIN_VALUE);
            }
        };

        try ( AFUNIXSocket sock = connectToServer() ) {
            sock.sendCredentials((byte) 0);
            SocketCredentials creds = sock.recieveCredentials();
            assertEquals(Byte.MIN_VALUE, creds.getData());
            assertEquals(-1, creds.getPid());
            assertEquals(-1, creds.getUid());
            assertEquals(-1, creds.getGid());
        }

        serverThread.checkException();
    }
}
