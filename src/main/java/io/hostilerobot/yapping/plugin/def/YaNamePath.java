package io.hostilerobot.yapping.plugin.def;

import io.hostilerobot.yapping.ast.AName;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Consumer;

public class YaNamePath {

    public void forEachl(Consumer<PathItem> visitor) {
        if(parent != null) {
            parent.forEachl(visitor);
        }
        visitor.accept(item);
    }
    public void forEachr(Consumer<PathItem> visitor) {
        YaNamePath node = this;
        do{
            visitor.accept(node.item);
        } while((node = node.parent) != null);
    }
    public <A> A foldl(A initial, BiFunction<PathItem, A, A> fold) {
        if(parent == null) {
            return fold.apply(item, initial);
        } else {
            return fold.apply(item, parent.foldl(initial, fold));
        }
    }
    public <A> A foldr(A initial, BiFunction<PathItem, A, A> fold) {
        YaNamePath node = this;
        A current = initial;
        do {
            current = fold.apply(node.item, current);
        } while((node = node.parent) != null);
        return current;
    }

    // todo lookups. Java class might have getV1()
    // build a path that looks like mymesh.triangles.5.v1.x
    public interface PathItem{}
    public record NameItem(AName name) implements PathItem{}
    public record IndexItem(int index) implements PathItem{}

    private final YaNamePath parent;
    private final PathItem item;
    private final int hashCode;
    private YaNamePath(PathItem item) {
        this.parent = null;
        this.item = item;
        hashCode = item.hashCode();
    }
    private YaNamePath(YaNamePath parent, PathItem item) {
        this.parent = parent;
        this.item = item;
        hashCode = parent.hashCode() * 31 + item.hashCode();
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public boolean equals(Object obj) {
        if(this == obj)
            return true;
        if(obj != null && obj.getClass() == this.getClass()) {
            YaNamePath other = (YaNamePath) obj;
            return Objects.equals(this.item, other.item)
                && Objects.equals(this.parent, other.parent);
        }
        return false;
    }

    public static YaNamePath from(PathItem item){
        return new YaNamePath(item);
    }
    public static YaNamePath from(PathItem... items) {
        if(items.length == 0)
            return null;
        YaNamePath path = from(items[0]);

        for(int i = 1; i < items.length; i++) {
            path = new YaNamePath(path, items[i]);
        }
        return path;
    }

    public YaNamePath addChild(PathItem item) {
        YaNamePath path = new YaNamePath(this, item);
        return path;
    }
}
