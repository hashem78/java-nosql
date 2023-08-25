package me.hashemalayan;
public class Main {
    public static void main(String[] args) throws InterruptedException {
        System.setProperty("io.grpc.netty.shaded.io.netty.log.level", "DEBUG");
        var client = new SignalingClient("localhost", 8080);
        client.shutdown();
    }
}