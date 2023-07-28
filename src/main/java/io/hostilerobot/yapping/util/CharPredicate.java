package io.hostilerobot.yapping.util;

public interface CharPredicate {
    boolean test(char c);

    public static CharPredicate from(char match) {
        return c -> c == match;
    }
}
