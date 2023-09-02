package me.hashemalayan.signaling;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.multibindings.Multibinder;
import me.hashemalayan.Event;
import me.hashemalayan.EventHandler;
import me.hashemalayan.managers.SignalingClientManager;
import me.hashemalayan.managers.SignalingProcessorsManager;
import me.hashemalayan.processors.SignalingMessageProcessor;
import org.reflections.Reflections;

import java.util.Set;

public class SignalingServerModule extends AbstractModule {
    @Override
    protected void configure() {

        var processorMultibinder = Multibinder.newSetBinder(
                binder(),
                new TypeLiteral<SignalingMessageProcessor>() {}
        );

        var reflections = new Reflections("me.hashemalayan");
        var handlerClasses = reflections.getSubTypesOf(SignalingMessageProcessor.class);

        for (var handlerClass : handlerClasses) {
            processorMultibinder.addBinding().to(handlerClass);
        }

        bind(SignalingClientManager.class).asEagerSingleton();
        bind(SignalingProcessorsManager.class).asEagerSingleton();
        install(
                new FactoryModuleBuilder()
                        .build(SignalingMessageObserverFactory.class)
        );
    }
}
