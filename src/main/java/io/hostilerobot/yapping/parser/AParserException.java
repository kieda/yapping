package io.hostilerobot.yapping.parser;

public class AParserException extends RuntimeException{
    // todo have CharSequence and int position
    public AParserException(String message) {
        super(message);
    }
    public AParserException() {

    }
}
