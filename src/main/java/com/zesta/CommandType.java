package com.zesta;

public enum CommandType {
    ADD("add"),
    POP("pop"),
    NEG("NEGATE"),
    PUSH("push"),
    ;

    private final String displayName;

   CommandType(String displayName){
       this.displayName = displayName;
   }

    public String getDisplayName() {
        return displayName;
    }

}

