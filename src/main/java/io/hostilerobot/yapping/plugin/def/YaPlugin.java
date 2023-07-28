package io.hostilerobot.yapping.plugin.def;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * represents a plugin, which is a list of defined environments that we may enter
 * an environment is entered by declaring a section
 *
 * section1:
 *     plugin1 = import: MyPlugin;
 *     plugin1.myenvironment: x ;
 *
 * section1:
 *      import: MyPlugin; # myplugin's environments are in the context till semicolon
 *                        # essentially equivalent to () = import: MyPlugin;
 *      myenvironment: x ; # myenvironment is not recognized, out of scope for MyPlugin
 *
 * section1:
 *     import: MyPlugin # myplu
 *     {x = myenvironment: asdf}
 *     myenvironment: x;
 *
 * import:
 *     MyPlugin
 * myenvironment: x;
 *
 * note that a plugin might have different runtime scope based on where and how it's imported.
 *
 *
 */
public class YaPlugin {
    // todo - a plugin is just an ANodeMap.
    //  todo implement maps as an extension of pairs
    //   e.g. {a = 1, b = 2} (notice the comma)
    private Map<String, YaEnvironment> definedEnvironments;
    private YaPlugin(Map<String, YaEnvironment> definedEnvironments) {
        this.definedEnvironments = definedEnvironments;
    }

    /**
     * usage:
     *    user defines
     *    YaPlugin plugin = YaPlugin.builder()
     *      .plugin("myplugin1", myplugin1)
     *      .plugin("myplugin2", myplugin2)
     *      .plugin("myplugin3", myplugin3)
     *      .build();
     *
     *
     *
     * @return
     */
    public Builder builder() {
        return new Builder();
    }
    public static final class Builder{
        private Map<String, YaEnvironment> definedEnvironments;
        private Builder() {
            definedEnvironments = new LinkedHashMap<>();
        }
        public Builder plugin(String name, YaEnvironment environment) {
            definedEnvironments.put(name, environment);
            return this;
        }
        public YaPlugin build() {
            return new YaPlugin(definedEnvironments);
        }
    }
}
