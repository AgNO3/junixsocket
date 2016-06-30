/**
 * junixsocket
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
import java.nio.file.Files;
import java.nio.file.Path;


/**
 * Some base functionality for socket tests.
 * 
 * @author Christian Kohlschuetter
 */
abstract class SocketTestBase {

    private final AFUNIXSocketAddress serverAddress;


    public SocketTestBase () throws IOException {
        this.serverAddress = new AFUNIXSocketAddress(getSocketFile());
    }


    private static File getSocketFile () throws IOException {
        String explicitFile = System.getProperty("org.newsclub.net.unix.testsocket");
        if ( explicitFile != null ) {
            return new File(explicitFile);
        }

        Path f = Files.createTempFile("junixsocket-test", ".sock");
        Files.delete(f);
        return f.toFile();
    }


    protected AFUNIXServerSocket startServer () throws IOException {
        final AFUNIXServerSocket server = AFUNIXServerSocket.newInstance();
        server.bind(this.serverAddress);
        return server;
    }


    protected AFUNIXSocket connectToServer () throws IOException {
        return AFUNIXSocket.connectTo(this.serverAddress);
    }

    protected abstract class ServerThread extends Thread {

        private final AFUNIXServerSocket serverSocket;
        private Exception exception = null;


        protected ServerThread () throws IOException {
            this.serverSocket = startServer();
            setDaemon(true);
            start();
        }


        protected abstract void handleConnection ( final AFUNIXSocket sock ) throws IOException;


        @Override
        public final void run () {
            try {
                try ( AFUNIXSocket sock = this.serverSocket.accept() ) {
                    handleConnection(sock);
                }
                finally {
                    this.serverSocket.close();
                }
            }
            catch ( Exception e ) {
                this.exception = e;
            }
        }


        public void checkException () throws Exception {
            if ( this.exception != null ) {
                throw this.exception;
            }
        }
    }


    protected void sleepFor ( final int ms ) throws IOException {
        try {
            Thread.sleep(ms);
        }
        catch ( InterruptedException e ) {
            throw (IOException) new IOException(e.getMessage()).initCause(e);
        }
    }
}
