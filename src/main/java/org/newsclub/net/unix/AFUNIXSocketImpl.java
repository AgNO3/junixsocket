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


import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketImpl;
import java.net.SocketOptions;

import org.apache.log4j.Logger;


/**
 * The Java-part of the {@link AFUNIXSocket} implementation.
 * 
 * @author Christian Kohlschütter
 */
class AFUNIXSocketImpl extends SocketImpl {

    private static final Logger log = Logger.getLogger(AFUNIXServerSocket.class);

    private static final int SHUT_RD = 0;
    private static final int SHUT_WR = 1;

    private String socketFile;
    private boolean closed = false;
    private boolean bound = false;
    private boolean connected = false;

    boolean closedInputStream = false;
    boolean closedOutputStream = false;
    private boolean abstractSocket;


    public AFUNIXSocketImpl () {
        super();
        this.fd = new FileDescriptor();
    }


    FileDescriptor getFD () {
        return this.fd;
    }


    @Override
    protected void accept ( SocketImpl s ) throws IOException {
        AFUNIXSocketImpl si = (AFUNIXSocketImpl) s;
        NativeUnixSocket.accept(this.socketFile, this.fd, si.fd, this.abstractSocket);
        si.socketFile = this.socketFile;
        si.connected = true;
    }


    @Override
    protected int available () throws IOException {
        return NativeUnixSocket.available(this.fd);
    }

    protected void bind ( SocketAddress addr, boolean dgram ) throws IOException {
        bind(0, addr);
    }

    protected void bind ( SocketAddress addr ) throws IOException {
        bind(0, addr);
    }

    protected void bind ( int backlog, SocketAddress addr ) throws IOException {
        if ( ! ( addr instanceof AFUNIXSocketAddress ) ) {
            throw new SocketException("Cannot bind to this type of address: " + addr.getClass());
        }
        AFUNIXSocketAddress sockAddr = (AFUNIXSocketAddress) addr;
        this.socketFile = sockAddr.getSocketFile();
        NativeUnixSocket.bind(this.socketFile, this.fd, backlog, sockAddr.isAbstract(), sockAddr.isDgram());
        this.bound = true;
        this.localport = sockAddr.getPort();
        this.abstractSocket = sockAddr.isAbstract();
    }


    @Override
    protected void bind ( InetAddress host, int p ) throws IOException {
        throw new SocketException("Cannot bind to this type of address: " + InetAddress.class);
    }


    void checkClose () {
        if ( this.closedInputStream && this.closedOutputStream ) {
            // close();
        }
    }


    @Override
    protected synchronized void close () throws IOException {
        log.debug("Closing socket");
        if ( this.closed ) {
            return;
        }
        this.closed = true;
        if ( this.fd.valid() ) {
            NativeUnixSocket.close(this.fd);
        }
        if ( this.bound ) {
            NativeUnixSocket.unlink(this.socketFile);
        }
        this.connected = false;
    }


    @Override
    protected void connect ( String host, int p ) throws IOException {
        throw new SocketException("Cannot bind to this type of address: " + InetAddress.class);
    }


    @Override
    protected void connect ( InetAddress addr, int p ) throws IOException {
        throw new SocketException("Cannot bind to this type of address: " + InetAddress.class);
    }


    @Override
    protected void connect ( SocketAddress addr, int timeout ) throws IOException {
        if ( ! ( addr instanceof AFUNIXSocketAddress ) ) {
            throw new SocketException("Cannot bind to this type of address: " + addr.getClass());
        }
        AFUNIXSocketAddress sockAddr = (AFUNIXSocketAddress) addr;
        this.socketFile = sockAddr.getSocketFile();
        NativeUnixSocket.connect(this.socketFile, this.fd, sockAddr.isAbstract(), sockAddr.isDgram());
        this.address = sockAddr.getAddress();
        this.port = sockAddr.getPort();
        this.localport = 0;
        this.connected = true;
    }


    @Override
    protected void create ( boolean stream ) throws IOException {}

    private final AFUNIXInputStream in = new AFUNIXInputStream();
    private final AFUNIXOutputStream out = new AFUNIXOutputStream();


    @Override
    protected InputStream getInputStream () throws IOException {
        if ( !this.connected && !this.bound ) {
            throw new IOException("Not connected/not bound");
        }
        return this.in;
    }


    @Override
    protected OutputStream getOutputStream () throws IOException {
        if ( !this.connected && !this.bound ) {
            throw new IOException("Not connected/not bound");
        }
        return this.out;
    }


    @Override
    protected void listen ( int backlog ) throws IOException {
        NativeUnixSocket.listen(this.fd, backlog);
    }


    @Override
    protected void sendUrgentData ( int data ) throws IOException {
        NativeUnixSocket.write(this.fd, new byte[] {
            (byte) ( data & 0xFF )
        }, 0, 1);
    }


    protected void sendCredentials ( byte data ) throws IOException {
        NativeUnixSocket.sendCredentials(this.fd, data);
    }


    protected SocketCredentials receiveCredentials () throws IOException {
        return new SocketCredentials(NativeUnixSocket.receiveCredentials(this.fd));
    }

    private final class AFUNIXInputStream extends InputStream {

        private boolean streamClosed = false;


        /**
         * 
         */
        public AFUNIXInputStream () {}


        @Override
        public int read ( byte[] b, int off, int len ) throws IOException {
            if ( this.streamClosed ) {
                throw new IOException("This InputStream has already been closed.");
            }
            if ( len == 0 ) {
                return 0;
            }
            try {
                return NativeUnixSocket.read(getFD(), b, off, len);
            }
            catch ( IOException e ) {
                throw (IOException) new IOException(e.getMessage() + " at " + AFUNIXSocketImpl.this.toString()).initCause(e);
            }
        }

        private byte[] buf1 = new byte[1];


        @Override
        public int read () throws IOException {
            synchronized ( this.buf1 ) {
                int numRead = read(this.buf1, 0, 1);
                if ( numRead <= 0 ) {
                    return -1;
                }
                return this.buf1[ 0 ] & 0xFF;
            }
        }


        @Override
        public void close () throws IOException {
            if ( this.streamClosed ) {
                return;
            }
            this.streamClosed = true;
            if ( getFD().valid() ) {
                NativeUnixSocket.shutdown(getFD(), SHUT_RD);
            }

            AFUNIXSocketImpl.this.closedInputStream = true;
            checkClose();
        }


        @Override
        public int available () throws IOException {
            int av = NativeUnixSocket.available(getFD());
            return av;
        }
    }

    private final class AFUNIXOutputStream extends OutputStream {

        private boolean streamClosed = false;

        private byte[] buf1 = new byte[1];


        /**
         * 
         */
        public AFUNIXOutputStream () {}


        @Override
        public void write ( int b ) throws IOException {
            synchronized ( this.buf1 ) {
                this.buf1[ 0 ] = (byte) b;
                write(this.buf1, 0, 1);
            }
        }


        @Override
        public void write ( byte b[], int off, int len ) throws IOException {
            if ( this.streamClosed ) {
                throw new AFUNIXSocketException("This OutputStream has already been closed.");
            }
            int tmpOff = off;
            int tmpLen = len;
            try {
                while ( tmpLen > 0 && !Thread.interrupted() ) {
                    int written = NativeUnixSocket.write(getFD(), b, tmpOff, tmpLen);
                    if ( written == -1 ) {
                        throw new IOException("Unspecific error while writing");
                    }
                    tmpLen -= written;
                    tmpOff += written;
                }
            }
            catch ( IOException e ) {
                throw (IOException) new IOException(e.getMessage() + " at " + AFUNIXSocketImpl.this.toString()).initCause(e);
            }
        }


        @Override
        public void close () throws IOException {
            if ( this.streamClosed ) {
                return;
            }
            this.streamClosed = true;
            if ( getFD().valid() ) {
                NativeUnixSocket.shutdown(getFD(), SHUT_WR);
            }
            AFUNIXSocketImpl.this.closedOutputStream = true;
            checkClose();
        }
    }


    @Override
    public String toString () {
        return super.toString() + "[fd=" + this.fd + "; file=" + this.socketFile + "; connected=" + this.connected + "; bound=" + this.bound + "]";
    }


    private static int expectInteger ( Object value ) throws SocketException {
        int v;
        try {
            v = (Integer) value;
        }
        catch ( ClassCastException e ) {
            throw new AFUNIXSocketException("Unsupport value: " + value, e);
        }
        catch ( NullPointerException e ) {
            throw new AFUNIXSocketException("Value must not be null", e);
        }
        return v;
    }


    private static int expectBoolean ( Object value ) throws SocketException {
        int v;
        try {
            v = ( (Boolean) value ).booleanValue() ? 1 : 0;
        }
        catch ( ClassCastException e ) {
            throw new AFUNIXSocketException("Unsupport value: " + value, e);
        }
        catch ( NullPointerException e ) {
            throw new AFUNIXSocketException("Value must not be null", e);
        }
        return v;
    }


    @Override
    public Object getOption ( int optID ) throws SocketException {
        try {
            switch ( optID ) {
            case SocketOptions.SO_KEEPALIVE:
            case SocketOptions.TCP_NODELAY:
                return NativeUnixSocket.getSocketOptionInt(this.fd, optID) != 0 ? true : false;
            case SocketOptions.SO_LINGER:
            case SocketOptions.SO_TIMEOUT:
            case SocketOptions.SO_RCVBUF:
            case SocketOptions.SO_SNDBUF:
                return NativeUnixSocket.getSocketOptionInt(this.fd, optID);
            case AFUNIXSocket.SO_PASSCRED:
                return NativeUnixSocket.getSocketOptionInt(this.fd, optID) != 0 ? true : false;
            }
        }
        catch ( AFUNIXSocketException e ) {
            throw e;
        }
        catch ( Exception e ) {
            throw new AFUNIXSocketException("Error while getting option", e);
        }
        throw new AFUNIXSocketException("Unsupported option: " + optID);
    }


    @Override
    public void setOption ( int optID, Object value ) throws SocketException {
        try {
            switch ( optID ) {
            case SocketOptions.SO_LINGER:

                if ( value instanceof Boolean ) {
                    boolean b = (Boolean) value;
                    if ( b ) {
                        throw new SocketException("Only accepting Boolean.FALSE here");
                    }
                    NativeUnixSocket.setSocketOptionInt(this.fd, optID, -1);
                    return;
                }
                NativeUnixSocket.setSocketOptionInt(this.fd, optID, expectInteger(value));
                return;
            case SocketOptions.SO_TIMEOUT:
                NativeUnixSocket.setSocketOptionInt(this.fd, optID, expectInteger(value));
                return;
            case SocketOptions.SO_KEEPALIVE:
            case SocketOptions.TCP_NODELAY:
            case SocketOptions.SO_RCVBUF:
            case SocketOptions.SO_SNDBUF:
                NativeUnixSocket.setSocketOptionInt(this.fd, optID, expectBoolean(value));
                return;
            case AFUNIXSocket.SO_PASSCRED:
                NativeUnixSocket.setSocketOptionInt(this.fd, optID, expectBoolean(value));
                return;
            }
        }
        catch ( AFUNIXSocketException e ) {
            throw e;
        }
        catch ( Exception e ) {
            throw new AFUNIXSocketException("Error while setting option", e);
        }
        throw new AFUNIXSocketException("Unsupported option: " + optID);
    }


    @Override
    protected void shutdownInput () throws IOException {
        if ( !this.closed && this.fd.valid() ) {
            NativeUnixSocket.shutdown(this.fd, SHUT_RD);
        }
    }


    @Override
    protected void shutdownOutput () throws IOException {
        if ( !this.closed && this.fd.valid() ) {
            NativeUnixSocket.shutdown(this.fd, SHUT_WR);
        }
    }
}
