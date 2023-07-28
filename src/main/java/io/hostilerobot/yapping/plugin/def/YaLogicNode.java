package io.hostilerobot.yapping.plugin.def;

import java.util.Set;

public interface YaLogicNode {
    // represents an environment
    public Set<YaDataDef> acceptingTypes();
    public Set<YaDataDef> resultingTypes();
}
