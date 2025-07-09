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
    NOT("not");

    private final String displayName;

   CommandType(String displayName){
       this.displayName = displayName;
   }

    public String getDisplayName() {
        return displayName;
    }

}

