public class ParserResponse {
    CommandType commandType;
    Integer index;
    String functionOrLabelName;
    MemorySegment memorySegment;
    ParserResponse(CommandType commandType, Integer index, MemorySegment memorySegment) {
        this.commandType = commandType;
        this.index = index;
        this.memorySegment = memorySegment;
    }
    ParserResponse(CommandType commandType, Integer index, String functionOrLabelName) {
        this.commandType = commandType;
        this.index = index;
        this.functionOrLabelName = functionOrLabelName;
    }
}
