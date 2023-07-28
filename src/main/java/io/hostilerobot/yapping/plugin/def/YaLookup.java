package io.hostilerobot.yapping.plugin.def;

// a sub-definition of a given item5
public interface YaLookup extends YaLogicNode {
    boolean canLookup(YaContext context, YaData<? extends Object> parent, YaNamePath.PathItem path);
    YaData<? extends Object> lookup(YaContext context, YaData<? extends Object> parent, YaNamePath.PathItem path);
}
