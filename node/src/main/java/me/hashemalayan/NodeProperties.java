package me.hashemalayan;

import lombok.Getter;

@Getter
public class NodeProperties {
    private final int signalingPort = Integer.parseInt(System.getenv("SIGNALING_PORT"));
    private final int port = Integer.parseInt(System.getenv("PORT"));
    private final String name = System.getenv("NAME");
}
