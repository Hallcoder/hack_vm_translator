package com.zesta;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Set;
import java.util.stream.Stream;

public class CodeGenerator {
    //push, pop, add, sub, neg, eq, gt, lt, and, or, not
   private int SP;
   private int LCL;
   private int STATIC;
   private int THIS;
   private int THAT;
   private HashMap<String, String> symbols = new HashMap<>();
   CodeGenerator(){
       symbols.put(MemorySegment.LOCAL.name(), "LCL");
       symbols.put(MemorySegment.ARGUMENT.name(), "ARG");
       symbols.put(MemorySegment.CONSTANT.name(), "CONST");
       symbols.put(MemorySegment.STATIC.name(), "STATIC");
       symbols.put(MemorySegment.THIS.name(), "THIS");
       symbols.put(MemorySegment.THAT.name(), "THAT");
       symbols.put(MemorySegment.POINTER.name(), "POINTER");
       symbols.put(MemorySegment.TEMP.name(), "TEMP");
   }
   public String[] generateCode(ParserResponse response){
       switch (response.commandType){
           case PUSH:
               return codeForStackPush(response);
           case POP:
               return codeForStackPop(response);
       }
       return new String[0];
   }
   public String[] codeForStackPush(ParserResponse response){
       Set<MemorySegment> firstClassSegments = EnumSet.of(
               MemorySegment.LOCAL,
               MemorySegment.THIS,
               MemorySegment.THAT,
               MemorySegment.ARGUMENT
       );
      if(firstClassSegments.contains(response.memorySegment)){
          String[] result = Stream.concat(Stream.of(getIndexCode(response.index)),Stream.of(getSegmentCode(response.memorySegment))).toArray(String[]::new);
          result = Stream.concat(Stream.of(result),Stream.of(getStackPushAndIncreasePointerCode())).toArray(String[]::new);
          return result;
       }
      return new String[]{};
   }

   public String[] codeForStackPop(ParserResponse response){
    return new String[]{};
   }
   public String[] getIndexCode(int index){
       return new String[]{"@"+index,"D=A"};
   }
    public String[] getSegmentCode(MemorySegment segment){
        return new String[]{"@"+symbols.get(segment),"A=D+M","D=M"};
    }

//    public String codeForAdd(){}
//    public String codeForSub(){}
//    public String codeForNeg(){}
//    public String codeForOr(){}
//    public String codeForLt(){}
//    public String codeForGt(){}
//    public String codeForAnd(){}
//    public String codeForNot(){}

    public String[] getStackPushAndIncreasePointerCode(){
       return new String[]{"@SP","A=M","M=D","@SP","M=M+1"};
    }

    public String[] getStackPopAndDecreasePointerCode(){
       return new String[]{"@SP","AM=M-1","D=M"};
    }





}
