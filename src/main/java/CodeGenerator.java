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
   private String filename;

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
    public void setFilename(String newFilename) {
        this.filename = newFilename;
    }
    public List<String> generateCode(ParserResponse response){
        List<String> combinedCode = new ArrayList<>();

        switch(response.commandType){
            case PUSH:
                combinedCode.addAll(codeForStackPush(response));
                break;
            case POP:
                combinedCode.addAll(codeForStackPop(response));
                break;
            case ADD:
                combinedCode.addAll(codeForAddOrSub(response,true));
                break;
            case SUB:
                combinedCode.addAll(codeForAddOrSub(response,false));
                break;
            case NOT:
                combinedCode.addAll(codeForLogicalOperator(response));

                break;
            case AND:
                combinedCode.addAll(codeForLogicalOperator(response));
                break;
            case LT:
                combinedCode.addAll(codeForLogicalOperator(response));
                break;
            case GT:
                combinedCode.addAll(codeForLogicalOperator(response));
                break;
            case EQ:
                combinedCode.addAll(codeForLogicalOperator(response));
                break;
            case OR:
                combinedCode.addAll(codeForLogicalOperator(response));
                break;
            case NEG:
                combinedCode.addAll(codeForLogicalOperator(response));
                break;
            case FUNCTION:
                combinedCode.addAll(codeForFunctionDeclaration(response));
                break;
            case CALL:
                combinedCode.addAll(codeForFunctionCalling(response));
                break;
            case RETURN:
                combinedCode.addAll(codeForFunctionReturning());
                break;
            case LABEL:
                combinedCode.addAll(codeForLabelGeneration(response));
                break;
            case GOTO:
                combinedCode.addAll(List.of(
                        "@" + response.functionOrLabelName,
                        "0;JMP"
                ));
                break;
            case IF_GOTO:
                combinedCode.addAll(getStackPopAndDecreasePointerCode());
                combinedCode.addAll(List.of(
                        "@" + response.functionOrLabelName,
                        "D;JNE"
                ));
                break;
            default:
                break;
        }
        return combinedCode;
    }
    public static List<String> generateBootstrapCode() {
        List<String> bootstrapCode = new ArrayList<>();

        // Set SP to 256
        bootstrapCode.add("@256");
        bootstrapCode.add("D=A");
        bootstrapCode.add("@SP");
        bootstrapCode.add("M=D");

        // Call Sys.init 0
        // Push return address
        bootstrapCode.add("@Sys.init$ret.0");
        bootstrapCode.add("D=A");
        bootstrapCode.add("@SP");
        bootstrapCode.add("AM=M+1");
        bootstrapCode.add("A=A-1");
        bootstrapCode.add("M=D");

        // Push LCL
        bootstrapCode.add("@LCL");
        bootstrapCode.add("D=M");
        bootstrapCode.add("@SP");
        bootstrapCode.add("AM=M+1");
        bootstrapCode.add("A=A-1");
        bootstrapCode.add("M=D");

        // Push ARG
        bootstrapCode.add("@ARG");
        bootstrapCode.add("D=M");
        bootstrapCode.add("@SP");
        bootstrapCode.add("AM=M+1");
        bootstrapCode.add("A=A-1");
        bootstrapCode.add("M=D");

        // Push THIS
        bootstrapCode.add("@THIS");
        bootstrapCode.add("D=M");
        bootstrapCode.add("@SP");
        bootstrapCode.add("AM=M+1");
        bootstrapCode.add("A=A-1");
        bootstrapCode.add("M=D");

        // Push THAT
        bootstrapCode.add("@THAT");
        bootstrapCode.add("D=M");
        bootstrapCode.add("@SP");
        bootstrapCode.add("AM=M+1");
        bootstrapCode.add("A=A-1");
        bootstrapCode.add("M=D");

        // Reposition ARG for Sys.init
        bootstrapCode.add("@SP");
        bootstrapCode.add("D=M");
        bootstrapCode.add("@5");
        bootstrapCode.add("D=D-A");
        bootstrapCode.add("@ARG");
        bootstrapCode.add("M=D");

        // Set LCL for Sys.init
        bootstrapCode.add("@SP");
        bootstrapCode.add("D=M");
        bootstrapCode.add("@LCL");
        bootstrapCode.add("M=D");

        // Go to Sys.init
        bootstrapCode.add("@Sys.init");
        bootstrapCode.add("0;JMP");

        // Return address label for Sys.init
        bootstrapCode.add("(Sys.init$ret.0)");

        return bootstrapCode;
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
       result.add("@" + response.functionOrLabelName);
       result.add("0;JMP");
       result.add("("+returnLabel+")");
       return result;
   }
   private List<String> codeForFunctionReturning(){
       List<String> result = new ArrayList<>();
       result.add("@LCL");
       result.add("D=M");
       result.add("@endframe");
       result.add("M=D");
       result.add("@"+5);
       result.add("D=D-A");
       result.add("@R13");
       result.add("M=D");
       result.add("A=M");
       result.add("D=M");
       result.add("@retaddr");
       result.add("M=D");
       result.addAll(getStackPopAndDecreasePointerCode());
       result.add("@ARG");
       result.add("A=M");
       result.add("M=D");
       result.add("@ARG");
       result.add("D=M+1");
       result.add("@SP");
       result.add("M=D");
       result.addAll(getCallerFrameRestore());
       result.add("@retaddr");
       result.add("A=M");
       result.add("0;JMP");
       return result;
   }

   private List<String> codeForLabelGeneration(ParserResponse response){
       return new ArrayList<>(List.of(new String[]{"(" + response.functionOrLabelName + ")"}));
   }

   private List<String> getCallerFrameRestore(){
       List<String> result = new ArrayList<>();
       result.addAll(List.of(new String[]{"@endframe","AM=M-1","D=M","@THAT","M=D"}));
       result.addAll(List.of(new String[]{"@endframe","AM=M-1","D=M","@THIS","M=D"}));
       result.addAll(List.of(new String[]{"@endframe","AM=M-1","D=M","@ARG","M=D"}));
       result.addAll(List.of(new String[]{"@endframe","AM=M-1","D=M","@LCL","M=D"}));
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

   private List<String> getPreFunctionCallCode(String returnAddressLabel, int argN){
       List<String> result = new ArrayList<>();
       result.add("@"+returnAddressLabel);
       result.add("D=A");
       result.addAll(getStackPushAndIncreasePointerCode());
       result.addAll(List.of(new String[]{"@LCL","D=M"}));
       result.addAll(getStackPushAndIncreasePointerCode());
       result.addAll(List.of(new String[]{"@ARG","D=M"}));
       result.addAll(getStackPushAndIncreasePointerCode());
       result.addAll(List.of(new String[]{"@THIS","D=M"}));
       result.addAll(getStackPushAndIncreasePointerCode());
       result.addAll(List.of(new String[]{"@THAT","D=M"}));
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
       result.add("@SP");
       result.add("D=M-D");
       result.add("@ARG");
       result.add("M=D");
       return result;
   }
   private String getReturnLabel(String functionName){
       String label  =  functionName+"_"+returnLabelCounter;
       returnLabelCounter++;
       return label;
   }


}
