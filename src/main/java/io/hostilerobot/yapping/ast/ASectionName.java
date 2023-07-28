package io.hostilerobot.yapping.ast;

public class ASectionName extends AName{
    public ASectionName(CharSequence name) {
        super(name);
    }

    @Override
    public String toString() {
        return super.toString() + ":";
    }
}
