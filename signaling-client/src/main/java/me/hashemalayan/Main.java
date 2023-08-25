package me.hashemalayan;
public class Main {
    public static void main(String[] args) throws InterruptedException {

        var client = new SignalingClient("localhost", 8080);
        client.shutdown();
    }
}