package io.hostilerobot.yapping.parser.advancer;

import io.hostilerobot.yapping.util.CharBiConsumer;
import io.hostilerobot.yapping.util.CharBiPredicate;

public interface CharAdvancer<T extends AdvancerState> extends CharBiPredicate<T>, CharBiConsumer<T> {
    void accept(char c, T state);
    boolean test(char c, T state);

    public static <S extends AdvancerState> void runAdvancer(CharSequence cs, S state, CharAdvancer<S> advancer) {
        for(int pos; (pos = state.getPos()) < cs.length() && !state.isStopped(); state.increasePos()) {
            char c = cs.charAt(pos);
            if(advancer.test(c, state)) {
                advancer.accept(c, state);
            }
        }
    }
}
