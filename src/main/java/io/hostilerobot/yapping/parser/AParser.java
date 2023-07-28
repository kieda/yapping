package io.hostilerobot.yapping.parser;

import io.hostilerobot.yapping.ast.ANode;
import io.hostilerobot.yapping.util.CharArrays;

public interface AParser<T> {
    public ANode<T> parse(CharSequence cs);
    public int match(CharSequence cs); // -1 if no match, otherwise length of sequence match

    public default boolean ignore() {
        return false;
    }

    public static final int RADIX = 10;
    // these will separate a name or an item if found in the middle
    public static final char[] RESERVED_SEPARATORS = CharArrays.sort(
            ',', '=', '/', '\\', '#', '(', ')', '<', '>', '[', ']', ':'
    );
    // these are all reserved chars, and cannot be found at the beginning of a name
    public static final char[] RESERVED_CHARS = CharArrays.sort(
        ',', '=', '/', '\\', '#', '(', ')', '<', '>', '[', ']', ':', '.', '-'
    );
}
