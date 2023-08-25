package me.hashemalayan.server.events;

import me.hashemalayan.Event;

public record InitializeLocalServerEvent(String port) implements Event {

}
