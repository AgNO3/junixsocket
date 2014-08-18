/**
 * junixsocket
 *
 * Copyright (c) 2009 NewsClub, Christian Kohlschütter
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


import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;


/**
 * Describes an {@link InetSocketAddress} that actually uses AF_UNIX sockets
 * instead of AF_INET.
 * 
 * The ability to specify a port number is not specified by AF_UNIX sockets, but
 * we need it sometimes, for example for RMI-over-AF_UNIX.
 * 
 * @author Christian Kohlschütter
 */
public class AFUNIXSocketAddress extends InetSocketAddress {

    private static final long serialVersionUID = 1L;
    private final String socketFile;
    private final boolean abst;
    private final boolean dgram;


    /**
     * Creates a new {@link AFUNIXSocketAddress} that points to the AF_UNIX
     * socket specified by the given file.
     * 
     * @param socketFile
     * @throws IOException
     */
    public AFUNIXSocketAddress ( final File socketFile ) throws IOException {
        this(socketFile, 0, false, false);
    }


    /**
     * Creates a new {@link AFUNIXSocketAddress} that points to the AF_UNIX
     * socket specified by the given file.
     * 
     * @param socketFile
     * @param abst
     *            abstract socket
     * @throws IOException
     */
    public AFUNIXSocketAddress ( final File socketFile, boolean abst ) throws IOException {
        this(socketFile, 0, abst, false);
    }


    /**
     * Creates a new {@link AFUNIXSocketAddress} that points to the AF_UNIX
     * socket specified by the given file, assigning the given port to it.
     * 
     * @param socketFile
     * @param port
     * @throws IOException
     */
    public AFUNIXSocketAddress ( final File socketFile, final int port ) throws IOException {
        this(socketFile, port, false, false);
    }


    /**
     * Creates a new {@link AFUNIXSocketAddress} that points to the AF_UNIX
     * socket specified by the given file, assigning the given port to it.
     * 
     * @param socketFile
     * @param port
     * @param abst
     *            abstract socket
     * @throws IOException
     */
    public AFUNIXSocketAddress ( final File socketFile, final int port, boolean abst ) throws IOException {
    	this(socketFile, port, abst, false);
    }

    /**
     * Creates a new {@link AFUNIXSocketAddress} that points to the AF_UNIX
     * socket specified by the given file, assigning the given port to it.
     * 
     * @param socketFile
     * @param port
     * @param abst
     *            abstract socket
     * @param dgram
     *            dgram socket 
     * @throws IOException
     */
    public AFUNIXSocketAddress ( final File socketFile, final int port, boolean abst, boolean dgram ) throws IOException {
        super(0);
        if ( port != 0 ) {
            NativeUnixSocket.setPort(this, port);
        }
        this.socketFile = socketFile.getCanonicalPath();
        this.abst = abst;
	this.dgram = dgram;
    }


    /**
     * Returns the (canonical) file path for this {@link AFUNIXSocketAddress}.
     * 
     * @return
     */
    public String getSocketFile () {
        return this.socketFile;
    }


    public boolean isAbstract () {
        return this.abst;
    }

    public boolean isDgram() {
    	return this.dgram;
    }


    @Override
    public String toString () {
        return getClass().getName() + "[host=" + getHostName() + ";port=" + getPort() + ";file=" + this.socketFile + "]";
    }
}
