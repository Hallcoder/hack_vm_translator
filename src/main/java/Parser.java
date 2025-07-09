public class Parser {
    public ParserResponse parse(String line){
        String[] parts = line.split(" ");
        String command;
        MemorySegment memorySegment = null;
        Integer index = null;
        command = parts[0];
        if(parts.length >= 2){
            memorySegment = MemorySegment.valueOf(parts[1].toUpperCase());
        }
        if(parts.length >= 3){
            index = Integer.parseInt(parts[2]);
        }
        CommandType type = CommandType.valueOf(command.toUpperCase());
//        System.out.println("Line:"+ line + " , Command Type: " + type);
        return new ParserResponse(type,index,memorySegment);
//        if(parts.length == 1) {
//            return new ParserResponse(type,parts);
//        }
//        if(parts.length == 2) {
//            return new ParserResponse(type)
//        }
//        String memorySegment = line.split(" ")[1];
//        String value = line.split(" ")[2];
//        switch(command){
//            case "push":
//
//        }
    }
}
