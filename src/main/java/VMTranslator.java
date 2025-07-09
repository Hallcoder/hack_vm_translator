import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class VMTranslator {
    private static Parser parser = new Parser();
    public static void main(String[] args) throws IOException {
        System.out.printf("Hello and welcome!" + args[0]);
        String basePath = "C:\\Users\\Admin\\Downloads\\Compressed\\nand2tetris\\nand2tetris\\projects\\7\\StackArithmetic\\StackTest\\";
        Path inputFile = Paths.get( args[0]);
        String filename = args[0].split("\\.")[0].split("/")[0];
        System.out.println("\nFilename: " + filename);
        Path outPutFile = Files.createFile(Paths.get(filename + "/" + filename+".asm"));
//        Path outPutFile = Files.createFile(Paths.get(filename+".asm"));

        CodeGenerator codeGenerator = new CodeGenerator(filename);
        Files.readAllLines(inputFile).forEach(line -> {
            String cleanedLine = cleanLine(line);
            if(cleanedLine.isEmpty()){
                return;
            }
            ParserResponse res = parser.parse(cleanedLine);
            List<String> generatedCode = new ArrayList<>(codeGenerator.generateCode(res));
            try {
                Files.writeString(outPutFile,"//"+cleanedLine+"\n",StandardOpenOption.APPEND);
                Files.write(outPutFile, generatedCode, StandardOpenOption.APPEND);
                System.out.println("Generated code: " + generatedCode);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
//            System.out.println(codeGenerator.generateCode(parser.parse(cleanedLine)));
        });
    }

    public static String cleanLine(String line) {
        // remove comments
        // remove all whitespace
        String cleanedLine = line   // remove comments
                .replaceAll("//.*", "").trim();
        System.out.println("Original line: " + line + " cleaned line: " + cleanedLine);
        // remove all whitespace
        return cleanedLine;
    }
}