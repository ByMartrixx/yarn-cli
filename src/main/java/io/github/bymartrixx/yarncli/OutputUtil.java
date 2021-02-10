package io.github.bymartrixx.yarncli;

public class OutputUtil {
    public static void println(String x) {
        System.out.println(x);
    }

    public static void println(int x) {
        System.out.println(x);
    }

    public static void println(Object x) {
        System.out.println(x);
    }

    public static void printf(String format, Object ... args) {
        System.out.printf(format, args);
    }

    public static void print(String x) {
        System.out.print(x);
    }

    public static void print(int x) {
        System.out.print(x);
    }

    public static void print(Object x) {
        System.out.print(x);
    }

    /**
     * <a href="https://en.wikipedia.org/wiki/ANSI_escape_code#CSI_(Control_Sequence_Introducer)_sequences">Control Sequence Introducer</a>
     */
    private static void CSI(String code) {
        print("\033[" + code);
    }

    public static void showCursor() {
        CSI("?25h");
    }

    public static void hideCursor() {
        CSI("?25l");
    }

    public static void cursorUp() {
        cursorUp(1);
    }

    public static void cursorUp(int n) {
        CSI(n + "A");
    }

    public static void cursorDown() {
        cursorDown(1);
    }

    public static void cursorDown(int n) {
        CSI(n + "B");
    }

    public static void cursorRight() {
        cursorRight(1);
    }

    public static void cursorRight(int n) {
        CSI(n + "C");
    }

    public static void cursorLeft() {
        cursorLeft(1);
    }

    public static void cursorLeft(int n) {
        CSI(n + "D");
    }

    public static void cursorPreviousLine() {
        cursorPreviousLine(1);
    }

    public static void cursorPreviousLine(int n) {
        CSI(n + "E");
    }

    public static void cursorNextLine() {
        cursorNextLine(1);
    }

    public static void cursorNextLine(int n) {
        CSI(n + "F");
    }

    public static void cursorHorizontalAbsolute(int n) {
        CSI(n + "G");
    }

    public static void eraseScreenAfterCursor() {
        CSI("0J");
    }

    public static void eraseScreenBeforeCursor() {
        CSI("1J");
    }

    public static void eraseScreen() {
        CSI("2J");
    }

    public static void eraseLineFromCursor() {
        CSI("0K");
    }

    public static void eraseLineToCursor() {
        CSI("1K");
    }

    public static void eraseCursorLine() {
        CSI("2K");
    }

    public static void reset() {
        CSI("0m");
    }

    public static void bold() {
        CSI("1m");
    }

    public static void faint() {
        CSI("2m");
    }

    public static void italic() {
        CSI("3m");
    }

    public static void underline() {
        CSI("4m");
    }

    public static void reverse() {
        CSI("7m");
    }

    public static void strike() {
        CSI("9m");
    }

    public static void doubleUnderline() {
        CSI("21m");
    }

    public static void normal() {
        CSI("22m");
    }

    public static void notUnderlined() {
        CSI("24m");
    }

    public static void notReversed() {
        CSI("27m");
    }

    public static void notStrike() {
        CSI("29m");
    }

    public static void black() {
        CSI("30m");
    }

    public static void red() {
        CSI("31m");
    }

    public static void green() {
        CSI("32m");
    }

    public static void yellow() {
        CSI("33m");
    }

    public static void blue() {
        CSI("34m");
    }

    public static void magenta() {
        CSI("35m");
    }

    public static void cyan() {
        CSI("36m");
    }

    public static void white() {
        CSI("37m");
    }

    public static void gray() {
        CSI("90m");
    }
}
