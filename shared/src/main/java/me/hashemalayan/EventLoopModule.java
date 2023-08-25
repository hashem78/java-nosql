package me.hashemalayan;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.Multibinder;
import org.reflections.Reflections;

import java.util.Set;

@SuppressWarnings({"rawtypes", "unchecked"})
public class EventLoopModule extends AbstractModule {

    @Override
    protected void configure() {
        Multibinder<EventHandler<? extends Event>> eventHandlerBinder =
                Multibinder.newSetBinder(binder(), new TypeLiteral<>() {});

        Reflections reflections = new Reflections("me.hashemalayan");
        Set<Class<? extends EventHandler>> handlerClasses = reflections.getSubTypesOf(EventHandler.class);

        for (Class<? extends EventHandler> handlerClass : handlerClasses) {
            bindEventHandler(eventHandlerBinder, handlerClass);
        }

        bind(EventLoop.class).asEagerSingleton();
    }

    private void bindEventHandler(
            Multibinder<EventHandler<? extends Event>> binder,
            Class<? extends EventHandler> handlerClass) {
        binder.addBinding().to((Class<? extends EventHandler<? extends Event>>) handlerClass);
    }
}

