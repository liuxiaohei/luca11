package org.ld.utils;

import akka.japi.Option;

import java.util.Optional;

public class SaOptionalAdapter {
    public static <T> Optional<T> asOptional(Option<T> option) {
        return Optional.ofNullable(option.getOrElse(null));
    }
}
