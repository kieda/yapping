package io.hostilerobot.yapping.parser;
import org.junit.platform.suite.api.*;

import static org.junit.jupiter.api.Assertions.*;

@Suite
@SelectClasses({ACommentParserTest.class, ADecimalParserTest.class, AQuotientParserTest.class, ANameParserTest.class,
        ASectionNameParserTest.class, AWhitespaceParserTest.class,
        AListParserTest.class, APairParserTest.class, APropertiesParserTest.class})
// todo SelectModule to automatically get tests?
public class ParserTests {

}
