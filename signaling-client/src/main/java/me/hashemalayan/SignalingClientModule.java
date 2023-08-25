package me.hashemalayan;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;

public class SignalingClientModule extends AbstractModule {



    @Override
    protected void configure() {

        bind(String.class)
                .annotatedWith(Names.named("host"))
                .toInstance("localhost");

        bind(Integer.class)
                .annotatedWith(Names.named("port"))
                .toInstance(8080);

        bind(SignalingClient.class).asEagerSingleton();
    }
}