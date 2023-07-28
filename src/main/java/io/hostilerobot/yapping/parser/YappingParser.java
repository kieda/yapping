package io.hostilerobot.yapping.parser;

import io.hostilerobot.yapping.ast.AProperties;
import io.hostilerobot.yapping.ast.ASection;

import java.util.ArrayList;
import java.util.List;

/**
 * the top level of yap files will be in the Properties format.
 * we could have this just be a list of values if we wanted, but it's ok to enforce at least a little bit of structure, no?
 */
public class YappingParser implements AParser<List<ASection<? extends Object>>> {
    /*
     * name :
     * asdf = asdf1
     * (key, val) = (1, 2)
     * ==
     *
     * asdf = name : 123 key = val name2: key = val
     * {asdf = name : [123, key = val] name2: key = val }
     * name: {asdf 123 = 123}
     * name: asdf {123 = 123}
     *
     * {name: asdf 123 = 123} -- if we're in GROUP Properties may be a key
     *                        -- if we're in RAW Properties may not be a key.
     * (name: asdf, name2: asdf = 123)
     * (name:[asdf], name2:[asdf = 123])
     * (name:[asdf], name2:[{asdf = name3:[asdf hijk]}])
     *
     * (yes. we can resolve this too)
     * name2:
     *    {asdf = name3: asdf}
     *    hijk
     * properties may not be a value in properties.
     *
     * Base = Pairs, Lists, Quotients, Decimals, Names, Comments, Whitespace
     * List = (Base)
     * Pairs = (keyGroup: Base, valGroup: Base + Properties, keyRaw: Base, valRaw: Base + Properties)
     * Properties = Base
     */
    AQuotientParser QUOTIENTS = new AQuotientParser();
    ADecimalParser DECIMALS = new ADecimalParser();
    ANameParser NAMES = new ANameParser();

    List<AParser<? extends Object>> BASE_PARSE_TYPES = new ArrayList<>();
    {
        // (123 , 456) = (123, 456) - parse pairs first
        BASE_PARSE_TYPES.add(null); // will be Pairs
        BASE_PARSE_TYPES.add(null); // will be lists
        BASE_PARSE_TYPES.add(QUOTIENTS); // parse quotients first 123 12/3
        BASE_PARSE_TYPES.add(DECIMALS);  // decimals next
        BASE_PARSE_TYPES.add(NAMES);     // then names
    }
    List<AParser<? extends Object>> ALL_PARSE_TYPES = new ArrayList<>();
    {
        ALL_PARSE_TYPES.add(null); // very first is properties
        ALL_PARSE_TYPES.addAll(BASE_PARSE_TYPES); // then parse the rest
    }
    AListParser<? extends Object> LISTS = new AListParser<>(ALL_PARSE_TYPES); // lists = (QUOTIENTS, DECIMALS, NAMES, LISTS, PAIRS, PROPERTIES)
    {
        BASE_PARSE_TYPES.set(1, LISTS);
        ALL_PARSE_TYPES.set(2, LISTS);
    }
    APairParser<? extends Object, ? extends Object> PAIRS = new APairParser<>(
            // group key,     group val
            ALL_PARSE_TYPES,  ALL_PARSE_TYPES,
            // raw key,       raw val
            BASE_PARSE_TYPES, ALL_PARSE_TYPES);
    {
        BASE_PARSE_TYPES.set(0, PAIRS);
        ALL_PARSE_TYPES.set(1, PAIRS);
    }

    List<AParser<? extends Object>> PROPERTY_PARSE_TYPES = new ArrayList<>(BASE_PARSE_TYPES);
    {
        PROPERTY_PARSE_TYPES.add(ACommentParser.getInstance());
        PROPERTY_PARSE_TYPES.add(AWhitespaceParser.getInstance());
    }
    ASectionNameParser SECTION_NAMES = new ASectionNameParser();
    APropertiesParser<? extends Object> PROPERTIES = new APropertiesParser<>(SECTION_NAMES, PROPERTY_PARSE_TYPES);
    {
        ALL_PARSE_TYPES.set(0, PROPERTIES);
    }
    // lists = (QUOTIENTS, DECIMALS, NAMES, LISTS, PAIRS, PROPERTIES)
    // properties = (QUOTIENTS, DECIMALS, NAMES, LISTS, PAIRS, WHITESPACE)

    // base = (PAIRS, LISTS, QUOTIENTS, DECIMALS, NAMES) : PAIRS (raw key)
    // all = PROPERTIES + base : LISTS, PAIRS
    // properties = base + COMMENTS + WHITESPACE : PROPERTIES

    // pairs = (QUOTIENTS, DECIMALS, NAMES, LISTS, PAIRS, PROPERTIES (not on raw key))

    // properties = (QUOTIENTS, DECIMALS, NAMES, LISTS, PAIRS, WHITESPACE)

    @Override
    public AProperties parse(CharSequence cs) {
        return PROPERTIES.parse(cs);
    }

    @Override
    public int match(CharSequence cs) {
        return PROPERTIES.match(cs);
    }
}
