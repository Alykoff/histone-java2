package ru.histone.v2.support;

class ExpectedException {
    private int line;
    private String expected;
    private String found;
    private String message;

    public int getLine() {
        return line;
    }

    public String getExpected() {
        return expected;
    }

    public String getFound() {
        return found;
    }

    public String getMessage() {
        return message;
    }
}