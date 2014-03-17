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


import java.io.IOException;
import java.net.ServerSocket;
import java.net.SocketAddress;
import java.net.SocketException;


/**
 * The server part of an AF_UNIX domain socket.
 * 
 * @author Christian Kohlschütter
 */
public class AFUNIXServerSocket extends ServerSocket implements UNIXSocket {

    private final AFUNIXSocketImpl impl;
    private AFUNIXSocketAddress boundEndpoint = null;


    /**
     * @return the boundEndpoint
     */
    AFUNIXSocketAddress getBoundEndpoint () {
        return this.boundEndpoint;
    }

    private final Thread shutdownThread = new Thread() {

        @Override
        public void run () {
            try {
                if ( getBoundEndpoint() != null ) {
                    NativeUnixSocket.unlink(getBoundEndpoint().getSocketFile());
                }
            }
            catch ( IOException e ) {}
        }
    };


    protected AFUNIXServerSocket () throws IOException {
        super();
        this.impl = new AFUNIXSocketImpl();
        NativeUnixSocket.initServerImpl(this, this.impl);

        Runtime.getRuntime().addShutdownHook(this.shutdownThread);
        NativeUnixSocket.setCreatedServer(this);
    }


    /**
     * Returns a new, unbound AF_UNIX {@link ServerSocket}.
     * 
     * @return The new, unbound {@link AFUNIXServerSocket}.
     * @throws IOException
     */
    public static AFUNIXServerSocket newInstance () throws IOException {
        AFUNIXServerSocket instance = new AFUNIXServerSocket();
        return instance;
    }


    /**
     * Returns a new AF_UNIX {@link ServerSocket} that is bound to the given {@link AFUNIXSocketAddress}.
     * 
     * @return The new, unbound {@link AFUNIXServerSocket}.
     * @throws IOException
     */
    public static AFUNIXServerSocket bindOn ( final AFUNIXSocketAddress addr ) throws IOException {
        AFUNIXServerSocket socket = newInstance();
        socket.bind(addr);
        return socket;
    }


    @Override
    public void setPassCred ( boolean passCred ) throws SocketException {
        this.impl.setOption(AFUNIXSocket.SO_PASSCRED, passCred);
    }


    /** {@inheritDoc} */
    @Override
    public void bind ( SocketAddress endpoint, int backlog ) throws IOException {
        if ( isClosed() )
            throw new SocketException("Socket is closed");
        if ( isBound() )
            throw new SocketException("Already bound");
        if ( ! ( endpoint instanceof AFUNIXSocketAddress ) ) {
            throw new IOException("Can only bind to endpoints of type " + AFUNIXSocketAddress.class.getName());
        }
        this.impl.bind(backlog, endpoint);
        this.boundEndpoint = (AFUNIXSocketAddress) endpoint;
    }


    /** {@inheritDoc} */
    @Override
    public boolean isBound () {
        return this.boundEndpoint != null;
    }


    /** {@inheritDoc} */
    @Override
    public AFUNIXSocket accept () throws IOException {
        if ( isClosed() )
            throw new SocketException("Socket is closed");
        AFUNIXSocket as = AFUNIXSocket.newInstance();
        this.impl.accept(as.impl);
        as.addr = this.boundEndpoint;
        NativeUnixSocket.setConnected(as);
        return as;
    }


    @Override
    public String toString () {
        if ( !isBound() )
            return "AFUNIXServerSocket[unbound]";
        return "AFUNIXServerSocket[" + this.boundEndpoint.getSocketFile() + "]";
    }


    @Override
    public void close () throws IOException {
        if ( isClosed() ) {
            return;
        }

        super.close();
        this.impl.close();
        if ( this.boundEndpoint != null ) {
            NativeUnixSocket.unlink(this.boundEndpoint.getSocketFile());
        }
        try {
            Runtime.getRuntime().removeShutdownHook(this.shutdownThread);
        }
        catch ( IllegalStateException e ) {}
    }


    public static boolean isSupported () {
        return NativeUnixSocket.isSupported();
    }
}
