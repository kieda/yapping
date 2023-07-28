package io.hostilerobot.yapping.plugin.def;

import io.hostilerobot.yapping.ast.ANode;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class YaEnvironment {
    // provides logic that will transform given nodes
    private List<YaLogic> logic = new ArrayList<>();
    // provides lookups for nodes
    private List<YaLookup> lookups = new ArrayList<>();

    // other environment variables that are defined with respect to the context
    private Map<YaNamePath, YaData> environmentVars = new LinkedHashMap<>();

    private YaEnvironment(List<YaLogic> logic, List<YaLookup> lookups,
                          Map<YaNamePath, YaData> environmentVars) {
        this.logic = logic;
        this.lookups = lookups;
        this.environmentVars = environmentVars;
    }
    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder{
        private Builder() {}
        private List<YaLogic> logic = new ArrayList<>();
        private List<YaLookup> lookups = new ArrayList<>();
        private Map<YaNamePath, YaData> environmentVars = new LinkedHashMap<>();
        public Builder addLogic(YaLogic logic) {
            this.logic.add(logic);
            return this;
        }
        public Builder addLookup(YaLookup lookup) {
            this.lookups.add(lookup);
            return this;
        }
        public Builder addVar(YaNamePath path, YaData data) {
            this.environmentVars.put(path, data);
            return this;
        }
        public YaEnvironment build() {
            return new YaEnvironment(logic, lookups, environmentVars);
        }
    }
}

/**
 * plugin = {string -> environment} -- we use section: to match on an environment that we want to utilize
 * environment = yalogic list -- logic can match on existing nodes and transform them
 *   yalogic
 *      boolean match(YaDefinition def, YaContext context)
 *      YaDefinition logic(YaDefinition def, YaContext context) -- change context, output new vars, etc. Things like @yapping/abc.yap
 *         we want to match nodes that begin with @
 *         todo - will we also want to add lookups to the state?
 *
 *
 *
 * probably needs to be a collection of environments
 *
 *
 * for example, in the import plugin
 */
