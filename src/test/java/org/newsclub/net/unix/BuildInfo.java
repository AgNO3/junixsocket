package org.newsclub.net.unix;


import org.junit.Test;


public class BuildInfo {

    @Test
    public void junixInfo () {
        System.out.println("NativeUnixSocket#isSupported: " + NativeUnixSocket.isSupported());
    }
}
