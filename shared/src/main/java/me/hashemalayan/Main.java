package me.hashemalayan;

import com.google.inject.Guice;

public class Main {
    public static void main(String[] args) {
        var injector = Guice.createInjector(new EventLoopModule());

        var eventLoop = injector.getInstance(EventLoop.class);

        eventLoop.dispatch(new EventTypeA());
        eventLoop.process();
    }
}