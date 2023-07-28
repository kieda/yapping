package io.hostilerobot.yapping.plugin.def;

import com.romix.scala.collection.concurrent.TrieMap;

public class YaContext {
    YaNamePath currentPath;
    TrieMap<YaNamePath, YaData<? extends Object>> definitions;
    public YaContext() {
        definitions = new TrieMap<>();
    }
    public YaContext(YaNamePath.PathItem scope, YaContext other) {
        currentPath = other.currentPath.addChild(scope);
        // generate a new stackframe from the given definitions
        definitions = other.definitions.snapshot();
    }
}
