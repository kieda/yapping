package io.hostilerobot.yapping.parser;

public sealed interface ParserTestCase {
    record ValidTestCase<T>(String input, T expectedParse, int expectedMatch) implements ParserTestCase {}
    record ExceptionTestCase<E>(String input, Class<? extends Throwable> throwable, String errorMessage, E additionalInfo) implements ParserTestCase {}
}
