package com.zesta;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class VMTranslator {
    public static void main(String[] args) throws IOException {
        //TIP Press <shortcut actionId="ShowIntentionActions"/> with your caret at the highlighted text
        // to see how IntelliJ IDEA suggests fixing it.
        System.out.printf("Hello and welcome!");
        String basePath = "C:\\Users\\Admin\\Downloads\\Compressed\\nand2tetris\\nand2tetris\\projects\\7\\MemoryAccess\\BasicTest\\";
        Path inputFile = Paths.get(basePath + args[0]);
        Files.readAllLines(inputFile).forEach(line -> {
            String cleanedLine = cleanLine(line);
            if(cleanedLine.isEmpty()){
                return;
            }

            System.out.println(line);
        });
    }

    public static String cleanLine(String line) {
        // remove comments
        // remove all whitespace
        String cleanedLine = line.replaceAll("\\s+", "")   // remove comments
                .replaceAll("//.*", "");
        System.out.println("Original line: " + line + " cleaned line: " + cleanedLine);
        // remove all whitespace
        return cleanedLine;
    }
}