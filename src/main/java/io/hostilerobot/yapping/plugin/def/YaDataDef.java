package io.hostilerobot.yapping.plugin.def;

import io.hostilerobot.yapping.ast.ANode;

import static io.hostilerobot.yapping.plugin.def.YaDataType.*;

public class YaDataDef {
    private Class<? extends YaDataType> dataType;
    private Class<? extends Object> subType;
    private YaDataDef(Class<? extends YaDataType> definitionClass,
                      Class<? extends Object> subType) {
        this.dataType = definitionClass;
        this.subType = subType;
    }

    private static YaDataDef NONE = new YaDataDef(None.class, Void.class);
    private static YaDataDef ANY = new YaDataDef(YaDataType.class, null);

    // null can be cast to any type
    // but nothing can be cast to void.

    // e.g. if we have (abc) -> abc
    // unboxes the 1 element list
    public static YaDataDef any() {
        return ANY;
    }
    public static YaDataDef none() {
        return NONE;
    }
    public static YaDataDef value(Class<? extends Object> valueClass) {
        return new YaDataDef(Value.class, valueClass);
    }
    public static YaDataDef node(Class<? extends ANode> nodeType) {
        return new YaDataDef(YapNode.class, nodeType);
    }
    public static YaDataDef environment() {
        // todo - what other information should we keep here?
        //        should we include acceptedTypes() and resultingTypes()
        //        in this information?
        return new YaDataDef(Environment.class, null);
    }
    public static YaDataDef plugin() {
        // todo - what other information should we store here?
        //        YaDataDef is about building a type graph to improve the lookup process

        return new YaDataDef(Plugin.class, null);
    }

    public Class<? extends YaDataType> getDataType() {
        return dataType;
    }
    public Class<? extends Object> getSubType() {
        return subType;
    }

    // return true if other can be transformed into this
    public boolean canTransformFrom(YaDataDef other) {
        return other.canTransformInto(this);
    }
    // return true if this can be transformed into other
    public boolean canTransformInto(YaDataDef other) {
        if(this == other)
            return true;
        if(getDataType() == other.getDataType() && getSubType() == other.getSubType())
            return true;
        // want this instanceof other
        if(other.getDataType().isAssignableFrom(getDataType())) {
            // check the subtype too
            if(other.getSubType() == null) {
                return true;
            }
            return getSubType() != null && other.getSubType().isAssignableFrom(getSubType());
        }
        return false;
    }
}
