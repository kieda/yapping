package io.hostilerobot.yapping.parser.advancer;

import java.util.List;

public class CompositeAdvancer<T extends AdvancerState> implements CharAdvancer<T> {
    private final List<? extends CharAdvancer<T>> advancers;
    public CompositeAdvancer(CharAdvancer<T>[] advancers) {
        this(List.of(advancers));
    }
    public CompositeAdvancer(List<? extends CharAdvancer<T>> advancers) {
        this.advancers = advancers;
    }


    @Override
    public void accept(char c, T state) {
        for(CharAdvancer<T> a : advancers) {
            if(a.test(c, state)) {
                a.accept(c, state);
                return;
            }
        }
    }

    @Override
    public boolean test(char c, T state) {
        for(CharAdvancer<T> a : advancers) {
            if(a.test(c, state)) {
                return true;
            }
        }
        return false;
    }
}
