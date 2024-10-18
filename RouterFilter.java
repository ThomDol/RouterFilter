public class RouterFilter
{
    private String chain;
    private int currentIdx = 0;

    private static XenLogger log = new XenLogger(RouterFilter.class);

    public RouterFilter(String chain) {

        this.chain = chain;
       
    }

    public static class Token
    {
        public enum Type
        {
            KEY("clé"),
            OPERATOR("opérateur"),
            VALUE("valeur"),
            SEPARATOR("séparateur"),
            BRACKET("parenthèse");
           

            String name;
           
            Type(String name)
            {
                this.name = name;
            }

            public String toString()
            {
                return name;
            }

        }

       
        private String value;
        private Type type;

        public Token(){}

        public Token(Type type,String value)
        {
            this.type=type;
            this.value=value;
        }

        public String getValue()
        {
            return this.value;
        }

        public Type getType()
        {
            return this.type;
        }

        @Override
        public String toString ()
        {
            return "Token de type "+ this.getType()+": "+this.getValue();
        }
    }

    //Condition : Key + Operator + Value
    public class Condition
    {  
        private List<String> header;
        private List<String> values;
        private Token tokenKey;
        private Token tokenOperator;
        private Token tokenValue;

        public Condition(List<String> header , List<String> values, Token key, Token operator, Token value){
            this.header = header;
            this.values = values;
            this.tokenKey = key;
            this.tokenOperator = operator;
            this.tokenValue = value;
        }

   
        public boolean isConditionTrue (){
            boolean result = false;
            if (this.header.contains(this.tokenKey.getValue())){
                int index = header.indexOf(this.tokenKey.getValue());
                if(this.tokenOperator.getValue().equals("=")){
                    if(values.get(index).equals(this.tokenValue.getValue())){
                    result=true;
                    }  
                }
                if(this.tokenOperator.getValue().equals("!=")){
                    if(!values.get(index).equals(this.tokenValue.getValue())){
                    result=true;
                    }
                }            
            }
            return result;
        }

       

    }
    //
    private Character getCharAndMoveForward() {
        if (this.currentIdx == this.chain.length()) {
            return null;
        } else {
            return this.chain.charAt(this.currentIdx++);
        }
    }

    private void back() {
        this.currentIdx--;
    }

    private char getChar() {
        return this.chain.charAt(this.currentIdx);
    }


    private void consumeSpaces() {
            while(!readingOver() && getChar() == ' ')
            {
                this.currentIdx++;
            }
    }

    private boolean isExpectedChar(Character c) {
        if (this.currentIdx < this.chain.length()) {
            return (this.chain.charAt(this.currentIdx) == c);
        }
        return false;
    }

   

    private LinkedList<Token> tokens;

   
    private void addTokenToTokens(Token token) {
        this.tokens.add(token);
    }

    //Return Token from Type key
    public Token getKeyFromFilter() {
        StringBuffer sb = new StringBuffer();
        Character c;
        while ((c = getCharAndMoveForward()) != null) {
            if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9') || (c == '-') || (c == '_')) {
                sb.append(c);
            } else {
                back();
                return new Token(Token.Type.KEY, sb.toString());
            }
        }
        return new Token(Token.Type.KEY, sb.toString());
    }

    //Return Token from Type Operator
    public Token getOperatorFromFilter() {
        Character c;
        Character d;
        Token token = new Token();
        if ((c = getCharAndMoveForward()) != null) {
            if (c == '!') {
                if ((d=getCharAndMoveForward()) != null) {
                    if (d == '=') {
                        token = new Token(Token.Type.OPERATOR, "!=");
                    } else {
                        log.errorAndException("operateur incorrect");
                    }
                }
                else{
                    log.errorAndException("operateur incorrect");
                }
            } else if (c == '=') {
                token = new Token(Token.Type.OPERATOR, "=");
            } else {
                log.errorAndException("operateur incorrect");
            }
        }
        return token;
    }

    //Return Token from Type Value
    private Token getValueFromFilter() {
        Token token = new Token();
        Character c;
        StringBuffer sb = new StringBuffer();

        if ((c=getCharAndMoveForward())!=null) {
            if (c!='\''){
                log.errorAndException("valeur du filtre doit commencer par un guillement");
            }
            else{
                while (!readingOver() && !isExpectedChar('\'')){
                    sb.append(getCharAndMoveForward());
                }
                if(readingOver()){
                    log.errorAndException("valeur du filtre doit finir par un guillement");
                }
                else{
                token = new Token (Token.Type.VALUE,sb.toString());
                this.currentIdx++;}
            }
        }
        return token;
    }

    //Return Token from Type Separator
    public Token getSeparatorFromFilter() {
        Character c;
        Character d;
        Token token = new Token();
        if ((c = getCharAndMoveForward()) != null) {
            if (c == '&') {
                if ((d = getCharAndMoveForward()) != null) {
                    if (d == '&') {
                        token = new Token(Token.Type.SEPARATOR, "&&");
                    } else {
                        log.errorAndException("séparateur incorrect");
                    }
                } else {
                    log.errorAndException("séparateur incorrect");
                }
            } else if (c == '|') {
                if ((d = getCharAndMoveForward()) != null) {
                    if (d == '|') {
                        token = new Token(Token.Type.SEPARATOR, "||");
                    } else {
                        log.errorAndException("séparateur incorrect");
                    }
                } else {
                    log.errorAndException("séparateur incorrect");
                }
            } else {
                log.errorAndException("séparateur incorrect ou manquant");
            }
        }
        return token;
    }

    //Return Token from Type Opening Bracket
    public Token getOpeningBracketFromFilter(){
        Character c;
        if ((c = getCharAndMoveForward()) != null) {
             if (c == '(') {
                return new Token (Token.Type.BRACKET,"(");
             }
        }
        return null;
    }

    //Return Token from Type Closing Bracket
    public Token getClosingBracketFromFilter(){
        Character c;
        if ((c = getCharAndMoveForward()) != null) {
             if (c == ')') {
                return new Token (Token.Type.BRACKET,")");
             }
        }
        return null;
    }

    public String displayTokens() {
        StringBuilder sb = new StringBuilder();
        for (Token token : tokens) {
            sb.append(token.toString() + System.lineSeparator());
        }
        return sb.toString();

    }

    //Return if end of chain
    public boolean readingOver() {
        if (this.currentIdx == this.chain.length()) {
            return true;
        } else {
            return false;
        }
    }

   
    public boolean isTrue(List<String> header, List<String> values)
    {
        currentIdx = 0;
        tokens = extractTokens();
        return isTrueCondition(header, values);
       
    }

    //Return if conditions filtered are true
    public boolean isTrueCondition(List<String> header, List<String> values) {
        boolean globalResult = true;
        String separator = "&&";  
        while (!tokens.isEmpty()) {
            Token token = tokens.remove(0);    
            if (token.getType() == Token.Type.BRACKET && token.getValue().equals("(")) {
                boolean subResult = isTrueCondition(header, values);
                if (separator.equals("&&")) {
                globalResult = globalResult && subResult;
                } else if (separator.equals("||")) {
                globalResult = globalResult || subResult;
                }
            } else if (token.getType() == Token.Type.BRACKET && token.getValue().equals(")")) {
                return globalResult;
            } else if (token.getType() == Token.Type.SEPARATOR) {
            separator = token.getValue();
            } else {
                Token key = token;
                Token operator = tokens.remove(0);
                Token value = tokens.remove(0);
                Condition c = new Condition(header, values, key, operator, value);

                if (separator.equals("&&")) {
                    globalResult = globalResult && c.isConditionTrue();
                } else if (separator.equals("||")) {
                globalResult = globalResult || c.isConditionTrue();
                }
            }
        }

        return globalResult;
    }

    //Return Token from chain
    public LinkedList<Token> extractTokens() {

        boolean isEnd = false;
        boolean isConditionValid=true;
        boolean isBracketOpen = false;
        tokens = new LinkedList<Token>();
        int state = 0;
loop:   while (!readingOver()) {

            switch (state) {
                case 0:
                    consumeSpaces();
                    Token openingBracket = getOpeningBracketFromFilter();
                    if (openingBracket!=null){
                        addTokenToTokens(openingBracket);
                        isBracketOpen = !isBracketOpen;
                        state = 1;
                        consumeSpaces();
                        isEnd = readingOver();
                        if(isEnd)
                        {
                            isConditionValid=false;
                            break loop;
                        }
                    }
                    else{
                        back();
                        state = 1;
                    }

                case 1:
                    Token key = getKeyFromFilter();
                    if (key.getValue() != ""){
                        addTokenToTokens(key);
                        state = 2;
                        consumeSpaces();}
                    else{
                        log.errorAndException("Une clé ne peut pas être nulle");
                    }
                    isEnd = readingOver();
                    if(isEnd)
                    {
                        isConditionValid=false;
                        break loop;
                    }
                    break;

                case 2:
                    Token operator = getOperatorFromFilter();
                    addTokenToTokens(operator);
                    state = 3;
                    consumeSpaces();

                    if(readingOver())
                    {
                        isConditionValid=false;
                        break loop;
                    }
                    break;

                case 3:
                    Token value = getValueFromFilter();
                    addTokenToTokens(value);
                    state = 4;
                    consumeSpaces();
                    break;

                case 4:
                    Token closingBracket = getClosingBracketFromFilter();
                    if (closingBracket!=null) {
                        addTokenToTokens(closingBracket);
                        isBracketOpen = !isBracketOpen;
                        state = 5;
                        consumeSpaces();
                    }
                    else{
                        back();
                        state = 5;
                    }
                    break;
                   

                case 5:
                    Token separator = getSeparatorFromFilter();
                    addTokenToTokens(separator);
                    state = 0;
                    consumeSpaces();
                    if(readingOver())
                    {
                        isConditionValid=false;
                        break loop;
                    }
                    break;
            }
        }
        if(isConditionValid && !isBracketOpen) {return tokens;}
        else{
            log.errorAndException("Condition invalide");
            return null;
            }
    }



   
}





