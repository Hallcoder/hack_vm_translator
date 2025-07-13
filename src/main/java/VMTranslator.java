import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class VMTranslator {
    private static Parser parser = new Parser(); // Parser instance

    public static void main(String[] args) throws IOException {

        String basePath = "C:\\Users\\Admin\\Downloads\\Compressed\\nand2tetris\\nand2tetris\\projects\\8\\FunctionCalls\\FibonacciElement";

//        Path inputPath = Paths.get(basePath );
        Path inputPath = Paths.get(args[0]);

        String outputFileName;
        Path outputFile;
        CodeGenerator codeGenerator;

        if (Files.isDirectory(inputPath)) {
            outputFileName = inputPath.getFileName().toString();
            outputFile = inputPath.resolve(outputFileName + ".asm");

            codeGenerator = new CodeGenerator(outputFileName);

            if (Files.exists(outputFile)) {
                Files.newBufferedWriter(outputFile, StandardOpenOption.TRUNCATE_EXISTING).close();
            } else {
                Files.createFile(outputFile);
            }

            // Write the bootstrap code once at the very beginning
            Files.write(outputFile, codeGenerator.generateBootstrapCode(), StandardOpenOption.APPEND);
            Files.writeString(outputFile, "// End of Bootstrap Code\n", StandardOpenOption.APPEND);

            // Process Sys.vm first if it exists
            Path sysVmPath = inputPath.resolve("Sys.vm");
            if (Files.exists(sysVmPath)) {
                processSingleFile(sysVmPath, codeGenerator, outputFile);
            }
            // Process all other .vm files in alphabetical order
            try (Stream<Path> stream = Files.list(inputPath)) {
                stream.filter(path -> path.toString().endsWith(".vm"))
                        .filter(path -> !path.equals(sysVmPath))
                        .sorted(Comparator.comparing(Path::getFileName))
                        .forEach(path -> {
                            try {
                                codeGenerator.setFilename(path.getFileName().toString());
                                processSingleFile(path, codeGenerator, outputFile);
                            } catch (IOException e) {
                                throw new RuntimeException("Error processing VM file: " + path, e);
                            }
                        });
            }

        } else {
            outputFileName = inputPath.getFileName().toString().replaceFirst("\\.vm$", "");

            if (inputPath.getParent() != null) {
                outputFile = inputPath.getParent().resolve(outputFileName + ".asm");
            } else {
                outputFile = Paths.get(outputFileName + ".asm");
            }

            codeGenerator = new CodeGenerator(outputFileName);

            if (Files.exists(outputFile)) {
                Files.newBufferedWriter(outputFile, StandardOpenOption.TRUNCATE_EXISTING).close();
            } else {
                Files.createFile(outputFile);
            }

            processSingleFile(inputPath, codeGenerator, outputFile);
        }
        System.out.println("Translation complete: " + outputFile.toAbsolutePath());
    }

    public static void processSingleFile(Path vmFile, CodeGenerator codeGenerator, Path outputFile) throws IOException {
        System.out.println("Processing file: " + vmFile.getFileName());
        Files.readAllLines(vmFile).forEach(line -> {
            String cleanedLine = cleanLine(line);
            if (cleanedLine.isEmpty()) {
                return;
            }

            ParserResponse res = parser.parse(cleanedLine);
            List<String> generatedAssembly = codeGenerator.generateCode(res);

            try {
                Files.writeString(outputFile, "// " + cleanedLine + "\n", StandardOpenOption.APPEND);
                Files.write(outputFile, generatedAssembly, StandardOpenOption.APPEND);
            } catch (IOException e) {
                throw new RuntimeException("Error writing to output file: " + outputFile, e);
            }
        });
    }

    public static String cleanLine(String line) {
        return line.replaceAll("//.*", "").trim();
    }
}