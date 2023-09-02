package me.hashemalayan.signaling;

import me.hashemalayan.managers.SignalingClientManager;
import me.hashemalayan.managers.SignalingProcessorsManager;

public interface SignalingMessageObserverFactory {
    SignalingMessageObserver create(
            SignalingClientManager signalingClientManager,
            SignalingProcessorsManager signalingProcessorsManager
    );
}
