public enum CommandType {
    ADD("add"),
    POP("pop"),
    NEG("NEGATE"),
    PUSH("push"),
    SUB("sub"),
    OR("or"),
    AND("and"),
    LT("lt"),
    GT("gt"),
    EQ("eq"),
    NOT("not"),
    FUNCTION("function"),
    CALL("call"),
    RETURN("return"),
    GOTO("goto"),
    IF_GOTO("if-goto"),
    LABEL("label");

    private final String displayName;

   CommandType(String displayName){
       this.displayName = displayName;
   }

    public String getDisplayName() {
        return displayName;
    }

}

// function foo 3
// call foo 2
//return