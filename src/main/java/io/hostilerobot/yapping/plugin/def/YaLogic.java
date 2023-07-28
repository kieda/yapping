package io.hostilerobot.yapping.plugin.def;

public interface YaLogic extends YaLogicNode{
    // something to match against another logic node
    // something to transform definition after we've matched
    public boolean match(YaContext context, YaData<? extends Object> definition);
    // todo - how do we produce lookups here?
    YaData<? extends Object> transform(YaContext context, YaData<? extends Object> definition);
}