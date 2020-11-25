package org.ld.utils;

import akka.actor.ActorSystem;

public class ActorSystemHolder {
    public static final ActorSystem ACTORSYSTEM = ActorSystem.create("lucaSystem");
}