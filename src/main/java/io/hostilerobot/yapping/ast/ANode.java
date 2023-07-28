package io.hostilerobot.yapping.ast;

public interface ANode<T> {
    public T getValue();
    public int size();
    default boolean ignore() {
        return false;
    }
}