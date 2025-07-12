import java.awt.*;
import java.util.*;
import java.util.List;

public class CodeGenerator {
    //push, pop, add, sub, neg, eq, gt, lt, and, or, not
   private int eq=0;
   private int gt=0;
   private int lt=0;
   private int LCL = 1;
   private int ARG = 2;
   private int THIS =3 ;
   private int THAT = 4;
   private int returnLabelCounter = 0;
   private final String filename;

    private final HashMap<String, String> symbols = new HashMap<>();
   private static final Set<MemorySegment> firstClassSegments = EnumSet.of(
            MemorySegment.LOCAL,
            MemorySegment.THIS,
            MemorySegment.THAT,
            MemorySegment.ARGUMENT,
            MemorySegment.CONSTANT
    );
   public CodeGenerator(String filename){
       this.filename = filename;
       symbols.put(MemorySegment.LOCAL.name(), "LCL");
       symbols.put(MemorySegment.ARGUMENT.name(), "ARG");
       symbols.put(MemorySegment.CONSTANT.name(), "CONST");
       symbols.put(MemorySegment.STATIC.name(), "STATIC");
       symbols.put(MemorySegment.THIS.name(), "THIS");
       symbols.put(MemorySegment.THAT.name(), "THAT");
       symbols.put(MemorySegment.POINTER.name(), "POINTER");
       symbols.put(MemorySegment.TEMP.name(), "TEMP");
   }
   public List<String> generateCode(ParserResponse response){
       switch(response.commandType){
           case PUSH:
               return codeForStackPush(response);
           case POP:
               return codeForStackPop(response);
           case ADD:
               return codeForAddOrSub(response,true);
           case SUB:
               return codeForAddOrSub(response,false);
           case NOT:
               return codeForLogicalOperator(response);
           case AND:
               return codeForLogicalOperator(response);
           case LT:
               return codeForLogicalOperator(response);
           case GT:
               return codeForLogicalOperator(response);
           case EQ:
               return codeForLogicalOperator(response);
           case OR:
               return codeForLogicalOperator(response);
           case NEG:
               return codeForLogicalOperator(response);
           case FUNCTION:
               return codeForFunctionDeclaration(response);
           case CALL:
               return codeForFunctionCalling(response);
           case RETURN:
               return codeForFunctionReturning(getReturnLabel(response.functionOrLabelName));
           case LABEL:
               return codeForLabelGeneration(response);
           case GOTO:
               return List.of(new String[]{"@"+response.functionOrLabelName,"0;JMP"});
           case IF_GOTO:
               List<String> result = new ArrayList<>();
               result.addAll(getStackPushAndIncreasePointerCode());
               result.addAll(List.of(new String[]{"@1", "D=D-A", "@" + response.functionOrLabelName, "D;JEQ"}));
               return result;
           default:
                   return List.of();
       }
   }
   public List<String> codeForStackPush(ParserResponse response){
       List<String> result = new ArrayList<>();
      if(firstClassSegments.contains(response.memorySegment)){
          result.addAll(getIndexCode(response.index));
        if(response.memorySegment != MemorySegment.CONSTANT) result.addAll(getSegmentCode(response.memorySegment,true));
        result.addAll(getStackPushAndIncreasePointerCode());
       }
       if(response.memorySegment == MemorySegment.STATIC){
           result.addAll(List.of(new String[]{"@"+filename+"."+response.index, "D=M"}));
           result.addAll(getStackPushAndIncreasePointerCode());
       }
       if(response.memorySegment == MemorySegment.TEMP){
           result.add("@"+(response.index+5));
           result.add("D=M");
           result.addAll(getStackPushAndIncreasePointerCode());
       }
       if(response.memorySegment == MemorySegment.POINTER){
           int thisorthat = response.index == 0 ? 3:4;
           result.add("@"+thisorthat);
           result.add("D=M");
           result.addAll(getStackPushAndIncreasePointerCode());
       }
      return result;
   }

   public List<String> codeForStackPop(ParserResponse response){
       List<String> result = new ArrayList<>();
    if(firstClassSegments.contains(response.memorySegment)){
        result.addAll(getIndexCode(response.index));
        result.addAll(getSegmentCode(response.memorySegment,false));
        result.add("@R13");
        result.add("M=D");
        result.addAll(getStackPopAndDecreasePointerCode());
        result.add("@R13");
        result.add("A=M");
        result.add("M=D");
    }

       if(response.memorySegment == MemorySegment.STATIC){
           result.addAll(getStackPopAndDecreasePointerCode());
           result.add("@"+filename+"."+response.index);
           result.add("M=D");
       }
       if(response.memorySegment == MemorySegment.TEMP){
           result.addAll(getStackPopAndDecreasePointerCode());
           result.add("@"+(response.index+5));
           result.add("M=D");
       }
       if(response.memorySegment == MemorySegment.POINTER){
           int thisorthat = response.index == 0 ? 3:4;
           result.addAll(getStackPopAndDecreasePointerCode());
           result.add("@"+thisorthat);
           result.add("M=D");
       }
    return result;
   }
   public List<String> getIndexCode(int index){
       return List.of(new String[]{"@" + index, "D=A"});
   }
    public List<String> getSegmentCode(MemorySegment segment, boolean forPush){
//        System.out.println("Symbol for " + segment.name() + " : " + symbols.get(segment.name()));
        return List.of(new String[]{"@" + symbols.get(segment.name()), "A=D+M", forPush ? "D=M" : "D=A"}); //check here
    }

    public List<String> codeForAddOrSub(ParserResponse response,boolean add){
        List<String> result = new ArrayList<>(getStackPopAndDecreasePointerCode());
       result.add("A=A-1");
       result.add(add ? "M=D+M": "M=M-D");
       return result;
    }
    public List<String> codeForLogicalOperator(ParserResponse response){
       List<String> result = new ArrayList<>();
         switch(response.commandType){
             case EQ :
                 result.addAll(getStackPopAndDecreasePointerCode());
                 result.add("@SP");
                 result.add("AM=M-1");
                 result.add("D=M-D");
                 result.add("@PUSH_EQUAL_"+eq);
                 result.add("D;JEQ");
                 result.addAll(getStackAddTrueOrFalse(false));
                 result.add("@END_EQ_"+ eq);
                 result.add("0;JMP");
                 result.add("(PUSH_EQUAL_"+eq+")");
                 result.addAll(getStackAddTrueOrFalse(true));
                 result.add("@END_EQ_"+ eq);
                 result.add("0;JMP");
                 result.add("(END_EQ_"+eq+")");
                 result.addAll(getStackNextCode());
                 eq++;
                 return result;
             case LT :
                 result.addAll(getStackPopAndDecreasePointerCode());
                 result.add("@SP");
                 result.add("AM=M-1");
                 result.add("D=M-D");
                 result.add("@PUSH_LESS_THAN_"+lt);
                 result.add("D;JLT");
                 result.addAll(getStackAddTrueOrFalse(false));
                 result.add("@END_LESS_THAN_"+ lt);
                 result.add("0;JMP");
                 result.add("(PUSH_LESS_THAN_"+lt+")");
                 result.addAll(getStackAddTrueOrFalse(true));
                 result.add("@END_LESS_THAN_"+ lt);
                 result.add("0;JMP");
                 result.add("(END_LESS_THAN_"+lt+")");
                 result.addAll(getStackNextCode());
                 lt++;
                 return result;
             case GT :
                 result.addAll(getStackPopAndDecreasePointerCode());
                 result.add("@SP");
                 result.add("AM=M-1");
                 result.add("D=M-D");
                 result.add("@PUSH_GREATER_THAN_"+gt);
                 result.add("D;JGT");
                 result.addAll(getStackAddTrueOrFalse(false));
                 result.add("@END_GREATER_THAN_"+ gt);
                 result.add("0;JMP");
                 result.add("(PUSH_GREATER_THAN_"+gt+")");
                 result.addAll(getStackAddTrueOrFalse(true));
                 result.add("@END_GREATER_THAN_"+ gt);
                 result.add("0;JMP");
                 result.add("(END_GREATER_THAN_"+gt+")");
                 result.addAll(getStackNextCode());
                 gt++;
                 return result;
             case OR:
                 result.addAll(getStackPopAndDecreasePointerCode());
                 result.add("@SP");
                 result.add("AM=M-1");
                 result.add("D=D|M");
                 result.add("@SP");
                 result.add("AM=M+1");
                 result.add("A=A-1");
                 result.add("M=D");
                 return result;
             case AND:
                 result.addAll(getStackPopAndDecreasePointerCode());
                 result.add("@SP");
                 result.add("AM=M-1");
                 result.add("D=D&M");
                 result.add("@SP");
                 result.add("AM=M+1");
                 result.add("A=A-1");
                 result.add("M=D");
                 return result;
             case NOT:
                 result.addAll(getStackPopAndDecreasePointerCode());
                 result.add("@SP");
                 result.add("AM=M+1");
                 result.add("A=A-1");
                 result.add("M=!D");
                 return result;
             case NEG:
                 result.addAll(getStackPopAndDecreasePointerCode());
                 result.add("@SP");
                 result.add("AM=M+1");
                 result.add("A=A-1");
                 result.add("M=-D");
                 return result;
             default:
                 return List.of(new String[]{});
         }
    }
    //We need code to jump to an address that we kept
//    public String codeForNeg(){}
//    public String codeForOr(){}
//    public String codeForLt(){}
//    public String codeForGt(){}
//    public String codeForAnd(){}
//    public String codeForNot(){}
    public List<String> getStackAddTrueOrFalse(boolean addTrue){
       return List.of(new String[]{"@SP","AM=M+1","A=A-1","M=" + (addTrue ? -1:0)});
    }
    public List<String> getStackNextCode(){
        return List.of(new String[]{"@SP","M=M"});
    }
    public List<String> getStackPushAndIncreasePointerCode(){
       return List.of(new String[]{"@SP", "AM=M+1","A=A-1" ,"M=D"});
    }

    public List<String> getStackPopAndDecreasePointerCode(){
       return List.of(new String[]{"@SP", "AM=M-1", "D=M"});
    }

  //function Foo 2 => 2 local variables
   private List<String> codeForFunctionDeclaration(ParserResponse response){
       List<String> result = new ArrayList<>();
       result.addAll(List.of(new String[]{"("+response.functionOrLabelName+")",}));
       result.addAll(getLocalInitializationCode(response.index));
       return result;
   }
   // call Foo 4
   private List<String> codeForFunctionCalling(ParserResponse response){
       String returnLabel = getReturnLabel(response.functionOrLabelName);
       List<String> result = new ArrayList<>();
       result.addAll(getPreFunctionCallCode(returnLabel,response.index));
       result.add("goto "+response.functionOrLabelName);
       result.add("("+returnLabel+")");
       returnLabelCounter++;
       return result;
   }
   private List<String> codeForFunctionReturning(String returnLabel){
       List<String> result = new ArrayList<>();
       result.add("@LCL");
       result.add("D=M");
       result.add("@endframe");
       result.add("M=D");
       result.add("@"+5);
       result.add("D=D-M");
       result.add("@R13");
       result.add("M=D");
       result.add("A=M");
       result.add("D=M");
       result.add("@retaddr");
       result.add("M=D");
       result.addAll(getStackPopAndDecreasePointerCode());
       result.add("@ARG");
       result.add("M=D");
       result.add("D=M+1");
       result.addAll(getStackPushAndIncreasePointerCode());
       result.addAll(getCallerFrameRestore());
       result.add("goto " + returnLabel);
       return result;
   }

   private List<String> codeForLabelGeneration(ParserResponse response){
       return new ArrayList<>(List.of(new String[]{"(" + response.functionOrLabelName + ")",}));
   }

   private List<String> getCallerFrameRestore(){
       List<String> result = new ArrayList<>();
       result.addAll(List.of(new String[]{"@endframe","AD=M-1","@THAT","M=D"}));
       result.addAll(List.of(new String[]{"@endframe","AD=M-1","@THIS","M=D"}));
       result.addAll(List.of(new String[]{"@endframe","AD=M-1","@ARG","M=D"}));
       result.addAll(List.of(new String[]{"@endframe","AD=M-1","@LCL","M=D"}));
       return result;
   }

   private List<String> getLocalInitializationCode(int count){
       List<String> result = new ArrayList<>();
       result.add("D=0");
       for(int i = 0; i < count; i++){
           result.addAll(getStackPushAndIncreasePointerCode());
       }
       return result;
   }

   private List<String> getPreFunctionCallCode(String returnAddressLabel,int argN){
       List<String> result = new ArrayList<>();
       result.addAll(List.of(new String[]{"@LCL","D=A"}));
       result.addAll(getStackPushAndIncreasePointerCode());
       result.addAll(List.of(new String[]{"@ARG","D=A"}));
       result.addAll(getStackPushAndIncreasePointerCode());
       result.addAll(List.of(new String[]{"@THIS","D=A"}));
       result.addAll(getStackPushAndIncreasePointerCode());
       result.addAll(List.of(new String[]{"@THAT","D=A"}));
       result.addAll(getStackPushAndIncreasePointerCode());
       result.addAll(getArgRepositionCode(argN));
       result.addAll(List.of(new String[]{"@SP","D=M","@LCL","M=D"}));
       return result;
   }
   //ARG=SP-5-ArgN  240-(5+10)
   private List<String> getArgRepositionCode(int argN){
       List<String> result = new ArrayList<>();
       int argDeductionValue = 5 + argN;
       result.add("@"+argDeductionValue);
       result.add("D=A");
       result.add("SP");
       result.add("D=A-D");
       result.add("@ARG");
       result.add("M=D");
       return result;
   }
   private String getReturnLabel(String functionName){
       return functionName+"_"+returnLabelCounter;
   }


}
