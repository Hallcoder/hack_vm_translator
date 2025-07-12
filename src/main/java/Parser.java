public class Parser {
    public ParserResponse parse(String line){
        String[] parts = line.split(" ");
        String command;
        String functionName = "";
        MemorySegment memorySegment = null;
        Integer index = null;
        command = parts[0];
        CommandType type = CommandType.valueOf(command.replace("-","_").toUpperCase());
        System.out.println("line :"+line+" , Command: " + command);

        if(parts.length >= 2 && !isBranchingFunction(type)){
            memorySegment = MemorySegment.valueOf(parts[1].toUpperCase());
        }else if(parts.length >= 2 && isBranchingFunction(type)){
            functionName = parts[1];
        }
        if(parts.length >= 3){
            index = Integer.parseInt(parts[2]);
        }
        System.out.println("Line:"+ line + " , Command Type: " + type);
        return isBranchingFunction(type) ? new ParserResponse(type,index,functionName): new ParserResponse(type,index,memorySegment);
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

    boolean isBranchingFunction(CommandType type){
        switch (type) {
            case FUNCTION:
                return true;
            case GOTO:
                return true;
            case IF_GOTO:
                return true;
            case LABEL:
                return true;
            case CALL:
                return true;
            default:
                return false;
        }
    }
}
