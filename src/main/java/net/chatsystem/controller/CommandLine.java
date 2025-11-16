package net.chatsystem.controller;

import java.util.Scanner;

public class CommandLine {

    /*
     * 
     *  we understand that we will make a graphical interface later but we thought
     *  we'd make the app nice with some command line interface
     * 
     */
    
    // ANSI escape codes
    public static final String RESET = "\u001B[0m";
    public static final String BOLD = "\u001B[1m";
    public static final String FG_GREEN = "\u001B[32m";
    public static final String FG_CYAN = "\u001B[36m";
    public static final String FG_YELLOW = "\u001B[33m";
    public static final String FG_RED = "\u001B[31m";
    public static final String FG_WHITE = "\033[97m";

    // helper method to format messages using a parameters
    private static void printFormatted(String color, String template, Object... params) {
        String out = template;

        for (Object p : params) {
            int id = out.indexOf("{}");
            if (id == -1)
                break; // no more placeholders

            String replacement = FG_WHITE + String.valueOf(p) + color;

            out = out.substring(0, id)
                + replacement
                + out.substring(id + 2);
        }

        System.out.print("\r");
        System.out.flush();
        System.out.printf(color + out + RESET);
        if (inPrompt) {
            System.out.print(prompt);
        }
    }

    public static void info(String msg, Object... params) {
        printFormatted(FG_CYAN, msg, params);
    }

    public static void success(String msg, Object... params) {
        printFormatted(BOLD + FG_GREEN, msg, params);
    }

    public static void error(String msg, Object... params) {
        printFormatted(BOLD + FG_RED, msg, params);
    }

    public static void clearLine() {
        System.out.print("\033[1F\33[K");
        System.out.flush();
    }

    public static void clearAll() {
        System.out.print("\033[2J\033[H");
        System.out.flush();
    }

    private static boolean inPrompt = false;
    private static String prompt;

    public static String prompt(String prompt, Scanner scanner) {
        
        inPrompt = true;
        prompt = FG_CYAN + prompt + BOLD + " > " + RESET;
        System.out.print(prompt);
        try {
            String answer = scanner.nextLine().trim();
            inPrompt = false;
            return answer;
        } catch(Exception e) {
            clearAll();
            return "";
        }
    }

}
