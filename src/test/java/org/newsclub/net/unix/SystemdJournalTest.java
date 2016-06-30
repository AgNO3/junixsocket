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


import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.management.ManagementFactory;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.newsclub.net.unix.AFUNIXSocketImpl.AFUNIXOutputStream;


/**
 * @author mbechler
 * 
 */
public class SystemdJournalTest extends SocketTestBase {

    static final Logger log = Logger.getLogger(SystemdJournalTest.class);

    static final String procName = ManagementFactory.getRuntimeMXBean().getName();
    static final int pid = Integer.parseInt(procName.substring(0, procName.indexOf('@')));

    private AFUNIXSocketAddress socketAddress;


    /**
     * @throws IOException
     */
    public SystemdJournalTest () throws IOException {
        super();
        File socketFile = new File("/run/systemd/journal/socket");
        if ( !socketFile.exists() ) {
            return;
        }
        this.socketAddress = new AFUNIXSocketAddress(socketFile, 0, false, true);
    }


    @Override
    protected AFUNIXSocket connectToServer () throws IOException {
        return AFUNIXSocket.connectTo(this.socketAddress);
    }


    @SuppressWarnings ( {
        "unused", "resource"
    } )
    @Test
    public void testWriteJournal () throws Exception {

        if ( this.socketAddress == null ) {
            return;
        }

        try ( AFUNIXSocket sock = connectToServer() ) {
            System.err.println("Client running");
            InputStream is = sock.getInputStream();
            AFUNIXOutputStream os = sock.getOutputStream();

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(bos);
            dos.write("PRIORITY=2\n".getBytes("UTF-8"));
            dos.write("MESSAGE=TEST\n".getBytes("UTF-8"));
            // os.sendmsg(bos.toByteArray(), true);
            System.err.println("Wrote from client");
        }
        catch ( Exception e ) {
            e.printStackTrace();
        }

    }


    @SuppressWarnings ( {
        "unused", "resource"
    } )
    @Test
    public void testWriteJournalLarge () throws Exception {

        if ( this.socketAddress == null ) {
            return;
        }

        try ( AFUNIXSocket sock = connectToServer() ) {
            System.err.println("Client running");
            InputStream is = sock.getInputStream();
            AFUNIXOutputStream os = sock.getOutputStream();

            Path tmpFile = Files.createTempFile(Paths.get("/dev/shm/"), "journal", ".tmp");
            FileChannel open = FileChannel.open(tmpFile, StandardOpenOption.READ, StandardOpenOption.WRITE);
            try ( FileOutputStream fos = new FileOutputStream(tmpFile.toFile());
                  FileInputStream fis = new FileInputStream(tmpFile.toFile()) ) {
                DataOutputStream dos = new DataOutputStream(fos);
                dos.write("PRIORITY=2\n".getBytes("UTF-8"));
                dos.write("MESSAGE=TEST2\n".getBytes("UTF-8"));
                dos.write("TEST2\n".getBytes("UTF-8"));
                int len = 68000;
                writeLongLE(dos, len - 1);
                byte data[] = new byte[len];
                for ( int i = 0; i < len; i++ ) {
                    data[ i ] = (byte) ( ( i % 26 ) + 'A' );
                }
                data[ len - 1 ] = '\n';
                dos.write(data);
                fos.flush();
                os.sendfd(fis.getFD());
            }
            finally {
                Files.deleteIfExists(tmpFile);
            }

            System.err.println("Wrote from client");
        }
        catch ( Exception e ) {
            e.printStackTrace();
        }

    }


    private static void writeLongLE ( DataOutputStream out, long value ) throws IOException {
        out.writeByte((int) ( value & 0xFF ));
        out.writeByte((int) ( ( value >> 8 ) & 0xFF ));
        out.writeByte((int) ( ( value >> 16 ) & 0xFF ));
        out.writeByte((int) ( ( value >> 24 ) & 0xFF ));
        out.writeByte((int) ( ( value >> 32 ) & 0xFF ));
        out.writeByte((int) ( ( value >> 49 ) & 0xFF ));
        out.writeByte((int) ( ( value >> 48 ) & 0xFF ));
        out.writeByte((int) ( ( value >> 56 ) & 0xFF ));
    }
}
