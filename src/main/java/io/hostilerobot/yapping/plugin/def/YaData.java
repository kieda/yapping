package io.hostilerobot.yapping.plugin.def;

import io.hostilerobot.yapping.ast.ANode;

// a definition essentially represents data that can be passed around in the current state while
// running a yap file. all possible data types are enumerated here
public sealed interface YaData<T> {
    T getValue();

    record Plugin(YaPlugin plugin) implements YaData<YaPlugin> {
        public YaPlugin getValue() {
            return plugin;
        }
    }

    record Environment(YaEnvironment environment) implements YaData<YaEnvironment> {
        public YaEnvironment getValue() {
            return environment;
        }

    }

    record YapNode<T>(ANode<T> node) implements YaData<ANode<T>> {
        public ANode<T> getValue() {
            return node;
        }
    }

    record Value<T>(T value) implements YaData<T> {
        public T getValue() {
            return value;
        }
    }

    record None() implements YaData<Void> {
        @Override
        public Void getValue() {
            return null;
        }
    }

    static final None NONE_INSTANCE = new None();

    static YaData<? extends Object> toDefinition(Object in) {
        switch(in) {
            case YaEnvironment environment:
                return new Environment(environment);
            case ANode<? extends Object> node:
                return new YapNode<>(node);
            case YaData<? extends Object> stateType:
                return stateType;
            case null:
                return NONE_INSTANCE;
            default:
                return new Value<>(in);
        }
    }
}
