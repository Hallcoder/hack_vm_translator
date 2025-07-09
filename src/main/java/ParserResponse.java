public class ParserResponse {
    CommandType commandType;
    Integer index;
    MemorySegment memorySegment;
    ParserResponse(CommandType commandType, Integer index, MemorySegment memorySegment) {
        this.commandType = commandType;
        this.index = index;
        this.memorySegment = memorySegment;
    }
}
