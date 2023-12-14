import java.util.HashMap;
import java.util.LinkedList;
import java.io.FileWriter;
import java.io.IOException;

public class TreeWalk implements CompilerVisitor {

    // ################## Variables ##################
    private int IDcounter = 0;
    private int Indent = 0;
    public String fileText = "";
    private boolean returnStmt = false;
    public HashMap<String, SymbolTableEntry> symbolTable = new HashMap<String, SymbolTableEntry>();

    // ################## Symbol Table Methods ##################
    class SymbolTableEntry {
        String ID;
        String Type;
        Integer Size;
        Integer Dimension;
        Integer LineOfDecl;
        LinkedList<Integer> LineOfUsage;
        Object Value;

        public SymbolTableEntry(
                String id, String type, Integer size, Integer dimension,
                Integer lineOfDecl, LinkedList<Integer> lineOfUsage, Object value) {
            this.ID = id;
            this.Type = type;
            this.Dimension = dimension;
            this.LineOfDecl = lineOfDecl;
            this.LineOfUsage = lineOfUsage;
            this.Value = value;
        }
    }

    void printTable() {
        System.out.printf("%n--------------------------------%n");
        System.out.printf("      Symbol Table Values       %n");
        System.out.printf("                                %n");
        System.out.printf("--------------------------------%n");
        System.out.printf("| %-10s | %-10s | %-10s | %-10s | %-10s | %-10s |%n",
                "ID", "Type", "Size", "Dimension", "LineOfDecl", "value");
        System.out.printf("--------------------------------%n");
        for (SymbolTableEntry i : symbolTable.values()) {
            System.out.printf("| %-10s | %-10s | %-10s | %-10s | %-10s | %-10s |%n",
                    i.ID, i.Type, i.Size, i.Dimension, i.LineOfDecl, i.Value);
        }
        System.out.printf("--------------------------------%n%n");
    }

    // ################## Visit Methods ##################

    private int GetID() {
        IDcounter++;
        return IDcounter;
    }

    public Object visit(SimpleNode node, Object data) {
        node.childrenAccept(this, data);
        return null;
    }

    public Object visit(ASTProgram node, Object data) {
        int ID = GetID();

        printHeader();

        // Print information about node
        System.out.println("-----");
        System.out.println("** Node " + ID + ": Program");
        System.out.println("-----");

        // Iterate through children nodes
        node.childrenAccept(this, data);

        printfooter();

        printTable();

        System.out.println(fileText);
        try {
            FileWriter myWriter = new FileWriter("Output/filename.txt");
            myWriter.write(fileText);
            myWriter.close();
        } catch (IOException e) {
            System.out.print(e.getMessage());
        } finally {

        }
        // finish walk
        return null;
    }

    public Object visit(ASTFunctionDeclaration node, Object data) {
        int ID = GetID();

        // Print information about node
        System.out.println("-----");
        System.out.println("** Node " + ID + ": FunctionDeclaration");
        System.out.println("-----");

        node.childrenAccept(this, data);

        return null;
    }

    public Object visit(ASTProcedureDeclaration node, Object data) {
        int ID = GetID();
        IndentCode();
        fileText += typeStandard((String) node.data.get("type")) + " " +
                node.data.get("funcName") + getParams(node) + "{\r\n";

        // Print information about node
        System.out.println("-----");
        System.out.println("** Node " + ID + ": Function");
        System.out.println("**** Returns " + node.data.get("type"));
        System.out.println("-----");

        // Iterate through children nodes
        Indent++;
        node.childrenAccept(this, data);
        addReturnStmt((String) node.data.get("type"));
        Indent--;
        IndentCode();
        fileText += "}";
        // Return to parent node (or move to sibling node if exists)
        return null;
    }

    public Object visit(ASTParameterList node, Object data) {
        int ID = GetID();

        // fileText = fileText + node.data.get("type") + " ";

        // Print information about node
        System.out.println("-----");
        System.out.println("** Node " + ID + ": Type");
        System.out.println("-----");

        node.childrenAccept(this, data);

        return null;
    }

    public Object visit(ASTParameter node, Object data) {
        int ID = GetID();
        IndentCode();
        fileText += typeStandard((String) node.data.get("type")) + " " +
                node.data.get("funcName") + getParams(node) + "{\r\n";

        // Print information about node
        System.out.println("-----");
        System.out.println("** Node " + ID + ": Procedure");
        System.out.println("**** Returns " + node.data.get("type"));
        System.out.println("-----");

        // Iterate through children nodes
        Indent++;
        node.childrenAccept(this, data);
        addReturnStmt((String) node.data.get("type"));
        Indent--;
        IndentCode();
        fileText += "}";
        // Return to parent node (or move to sibling node if exists)
        return null;
    }

    public Object visit(ASTVariableDeclarator node, Object data) {
        int ID = GetID();

        // the output for this one should be taken care of in getParams

        // Print information about node
        System.out.println("-----");
        System.out.println("** Node " + ID + ": ParameterList");
        System.out.println("-----");

        node.childrenAccept(this, data);

        return null;
    }

    public Object visit(ASTVariableDeclaratorId node, Object data) {
        int ID = GetID();

        // ibid. see getParams

        // Print information about node
        System.out.println("-----");
        System.out.println("** Node " + ID + ": Parameter");
        System.out.println("-----");

        node.childrenAccept(this, data);

        return null;
    }

    public Object visit(ASTType node, Object data) {
        int ID = GetID();

        // anything in here should be taken care of by the individual procedure, etc

        // Print information about node
        System.out.println("-----");
        System.out.println("** Node " + ID + ": Body");
        System.out.println("-----");

        node.childrenAccept(this, data);

        return null;
    }

    public Object visit(ASTStatement node, Object data) {
        int ID = GetID();

        // everything here should be taken care of in the individual parts, as above

        // Print information about node
        System.out.println("-----");
        System.out.println("** Node " + ID + ": Expression");
        System.out.println("-----");

        node.childrenAccept(this, data);

        return null;
    }

    public Object visit(ASTBlock node, Object data) {
        int ID = GetID();

        // TODO: still needs the ability to parse if you give it a variable to
        // initialize
        IndentCode();
        //fileText = fileText + typeStandard((String) node.data.get("type")) + " " + node.data.get("value") + ";\r\n";

        // Print information about node
        System.out.println("-----");
        System.out.println("** Node " + ID + ": Declaration");
        System.out.println("-----");

        node.childrenAccept(this, data);

        return null;
    }

    public Object visit(ASTBlockStatement node, Object data) {
        int ID = GetID();

        // TODO: still needs the ability to parse if you give it a variable to
        // initialize
        IndentCode();
        //fileText = fileText + typeStandard((String) node.data.get("type")) + " " + node.data.get("value") + ";\r\n";

        // Print information about node
        System.out.println("-----");
        System.out.println("** Node " + ID + ": Declared Term");
        System.out.println("-----");

        node.childrenAccept(this, data);

        return null;
    }



    public Object visit(ASTStatementExpression node, Object data) {
        int ID = GetID();

        // Standardize types
        String VarID = (String) node.data.get("variable");
        // String VarType = (String) node.data.get("type");
        // Object VarValue = (String) node.data.get("value");
        // int lineOfDecl = (int) node.data.get("lineNo");

        // if (symbolTable.get(VarID) != null) {
        // lineOfDecl = (int) symbolTable.get(VarID).LineOfDecl;
        // }
        /*
         * i have a theory that this needs to be a level below here, in the actual
         * assign operator
         * IndentCode();
         * fileText += VarID + " = " + VarValue + ";\r\n";
         */

        // Print information about node
        System.out.println("-----");
        System.out.println("** Node " + ID + ": Assignment");
        // System.out.println("**** Type: " + VarType);
        // System.out.println("**** Name: " + VarID);
        // System.out.println("**** Value: " + VarValue);
        System.out.println("-----");

        // Set up data object
        // SymbolTableEntry entry = new SymbolTableEntry(VarID, VarType, null, null,
        // lineOfDecl, null, VarValue);
        // add to Symbol table
        // symbolTable.put(VarID, entry);

        if (node.data.get("more") == "no") {
            // should be no reason to check children, because there are no children
            IndentCode();
            fileText += VarID + node.data.get("assign") + ";\r\n";
        } else {
            // Iterate through children nodes
            node.childrenAccept(this, data);
        }
        // Return to parent node (or move to sibling node if exists)
        return null;
    }

    public Object visit(ASTIfStatement node, Object data) {
        int ID = GetID();
        // String VarID = (String) node.data.get("variable");
        // String VarType = (String) node.data.get("type");
        // Object VarValue = (String) node.data.get("value");
        // int lineOfDecl = (int) node.data.get("lineNo");

        // if (symbolTable.get(VarID) != null) {
        // lineOfDecl = (int) symbolTable.get(VarID).LineOfDecl;
        // }
        // this way, we should be able to get the exact operator that we need
        IndentCode();
        // fileText += VarID + " " + node.data.get("assign") + " " + VarValue + ";\r\n";
        // Print information about node
        System.out.println("-----");
        System.out.println("** Node " + ID + ": Variable");
        System.out.println("-----");

        node.childrenAccept(this, data);

        return null;
    }

    public Object visit(ASTWhileStatement node, Object data) {
        int ID = GetID();
        // String VarID = (String) node.data.get("variable");
        // String VarType = (String) node.data.get("type");
        // Object VarValue = (String) node.data.get("value");
        // int lineOfDecl = (int) node.data.get("lineNo");

        // if (symbolTable.get(VarID) != null) {
        // lineOfDecl = (int) symbolTable.get(VarID).LineOfDecl;
        // }
        // this way, we should be able to get the exact operator that we need
        IndentCode();
        // fileText += VarID + " " + node.data.get("assign") + " " + VarValue + ";\r\n";
        // Print information about node
        System.out.println("-----");
        System.out.println("** Node " + ID + ": AssignOperator");
        System.out.println("-----");

        node.childrenAccept(this, data);

        return null;
    }

    public Object visit(ASTDoStatement node, Object data) {
        int ID = GetID();

        // Print information about node
        System.out.println("-----");
        System.out.println("** Node " + ID + ": FunctionCall");
        System.out.println("-----");

        node.childrenAccept(this, data);

        return null;
    }

    public Object visit(ASTStatementExpressionList node, Object data) {
        int ID = GetID();

        // Print information about node
        System.out.println("-----");
        System.out.println("** Node " + ID + ": PrintCall");
        System.out.println("-----");

        String varID = (String) node.data.get("variable");

        switch ((String) node.data.get("printType")) {
            case "int":
                fileText += "printf(\"%d\", " + node.data.get("printVal") + ");\r\n";
                break;
            case "double":
                fileText += "printf(\"%f\", " + node.data.get("printVal") + ");\r\n";
                break;
            case "String":
                fileText += "printf(" + node.data.get("printVal") + ");\r\n";
                break;
        }

        node.childrenAccept(this, data);

        return null;
    }

    public Object visit(ASTReturnStatement node, Object data) {
        int ID = GetID();

        // Print information about node
        System.out.println("-----");
        System.out.println("** Node " + ID + ": ReadCall");
        System.out.println("-----");

        node.childrenAccept(this, data);

        return null;
    }

    public Object visit(ASTPrintStatement node, Object data) {
        int ID = GetID();

        // same here

        // Print information about node
        System.out.println("-----");
        System.out.println("** Node " + ID + ": DefinedCall");
        System.out.println("-----");

        node.childrenAccept(this, data);

        return null;
    }

    public Object visit(ASTPrintIntStatement node, Object data) {
        int ID = GetID();

        // output for this one is likely found in the child, ASTValue

        // Print information about node
        System.out.println("-----");
        System.out.println("** Node " + ID + ": ValueList");
        System.out.println("-----");

        node.childrenAccept(this, data);

        return null;
    }

    public Object visit(ASTPrintDoubleStatement node, Object data) {
        int ID = GetID();

        // Print information about node
        System.out.println("-----");
        System.out.println("** Node " + ID + ": Value");
        System.out.println("-----");

        node.childrenAccept(this, data);

        return null;
    }

    public Object visit(ASTPrintStringStatement node, Object data) {
        int ID = GetID();

        // Print information about node
        System.out.println("-----");
        System.out.println("** Node " + ID + ": AtomicValue");
        System.out.println("-----");

        node.childrenAccept(this, data);

        return null;
    }

    public Object visit(ASTReadStatement node, Object data) {
        int ID = GetID();

        System.out.println("-----");
        System.out.println("** Node " + ID + ": Statement");
        System.out.println("**** Statement Type: " + node.data.get("type"));
        System.out.println("-----");
        // Iterate through children nodes
        node.childrenAccept(this, data);
        // Return to parent node (or move to sibling node if exists)
        return null;
    }

    public Object visit(ASTReadIntStatement node, Object data) {
        int ID = GetID();

        // Print information about node
        System.out.println("-----");
        System.out.println("** Node " + ID + ": Predicate");
        System.out.println("-----");

        node.childrenAccept(this, data);

        return null;
    }

    public Object visit(ASTReadDoubleStatement node, Object data) {
        int ID = GetID();

        // Print information about node
        System.out.println("-----");
        System.out.println("** Node " + ID + ": Predicate");
        System.out.println("-----");

        node.childrenAccept(this, data);

        return null;
    }

    public Object visit(ASTReadStringStatement node, Object data) {
        int ID = GetID();

        // Print information about node
        System.out.println("-----");
        System.out.println("** Node " + ID + ": ConnectiveOperator");
        System.out.println("-----");

        node.childrenAccept(this, data);

        return null;
    }

    public Object visit(ASTExpression node, Object data) {
        int ID = GetID();

        // Print information about node
        System.out.println("-----");
        System.out.println("** Node " + ID + ": CompareOperator");
        System.out.println("-----");

        node.childrenAccept(this, data);

        return null;
    }

    public Object visit(ASTAssignmentOperator node, Object data) {
        int ID = GetID();

        // Print information about node
        System.out.println("-----");
        System.out.println("** Node " + ID + ": MathOperator");
        System.out.println("-----");

        node.childrenAccept(this, data);

        return null;
    }

    public Object visit(ASTConditionalOrExpression node, Object data) {
        int ID = GetID();

        // Print information about node
        System.out.println("-----");
        System.out.println("** Node " + ID + ": DoLoop");
        System.out.println("-----");

        node.childrenAccept(this, data);

        return null;
    }

    public Object visit(ASTConditionalAndExpression node, Object data) {
        int ID = GetID();

        // Print information about node
        System.out.println("-----");
        System.out.println("** Node " + ID + ": WhileLoop");
        System.out.println("-----");

        node.childrenAccept(this, data);

        return null;
    }

    public Object visit(ASTEqualityExpression node, Object data) {
        int ID = GetID();

        // Print information about node
        System.out.println("-----");
        System.out.println("** Node " + ID + ": IfThen");
        System.out.println("-----");

        node.childrenAccept(this, data);

        return null;
    }

    public Object visit(ASTRelationalExpression node, Object data) {
        int ID = GetID();

        // Print information about node
        System.out.println("-----");
        System.out.println("** Node " + ID + ": ");
        System.out.println("-----");

        node.childrenAccept(this, data);

        return null;
    }
    public Object visit(ASTAdditiveExpression node, Object data) {
        int ID = GetID();

        // Print information about node
        System.out.println("-----");
        System.out.println("** Node " + ID + ": ");
        System.out.println("-----");

        node.childrenAccept(this, data);

        return null;
    }
    public Object visit(ASTMultiplicativeExpression node, Object data) {
        int ID = GetID();

        // Print information about node
        System.out.println("-----");
        System.out.println("** Node " + ID + ": ");
        System.out.println("-----");

        node.childrenAccept(this, data);

        return null;
    }
    public Object visit(ASTUnaryExpression node, Object data) {
        int ID = GetID();

        // Print information about node
        System.out.println("-----");
        System.out.println("** Node " + ID + ": ");
        System.out.println("-----");

        node.childrenAccept(this, data);

        return null;
    }
    public Object visit(ASTPostfixExpression node, Object data) {
        int ID = GetID();

        // Print information about node
        System.out.println("-----");
        System.out.println("** Node " + ID + ": ");
        System.out.println("-----");

        node.childrenAccept(this, data);

        return null;
    }
    public Object visit(ASTPrimaryExpression node, Object data) {
        int ID = GetID();

        // Print information about node
        System.out.println("-----");
        System.out.println("** Node " + ID + ": ");
        System.out.println("-----");

        node.childrenAccept(this, data);

        return null;
    }
    public Object visit(ASTPrimaryPrefix node, Object data) {
        int ID = GetID();

        // Print information about node
        System.out.println("-----");
        System.out.println("** Node " + ID + ": ");
        System.out.println("-----");

        node.childrenAccept(this, data);

        return null;
    }
    public Object visit(ASTPrimarySuffix node, Object data) {
        int ID = GetID();

        // Print information about node
        System.out.println("-----");
        System.out.println("** Node " + ID + ": ");
        System.out.println("-----");

        node.childrenAccept(this, data);

        return null;
    }
    public Object visit(ASTLiteral node, Object data) {
        int ID = GetID();

        // Print information about node
        System.out.println("-----");
        System.out.println("** Node " + ID + ": ");
        System.out.println("-----");

        node.childrenAccept(this, data);

        return null;
    }
    public Object visit(ASTArguments node, Object data) {
        int ID = GetID();

        // Print information about node
        System.out.println("-----");
        System.out.println("** Node " + ID + ": ");
        System.out.println("-----");

        node.childrenAccept(this, data);

        return null;
    }
    public Object visit(ASTArgumentList node, Object data) {
        int ID = GetID();

        // Print information about node
        System.out.println("-----");
        System.out.println("** Node " + ID + ": ");
        System.out.println("-----");

        node.childrenAccept(this, data);

        return null;
    }

    private void printHeader() {
        System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++");
        System.out.println("Walking Through the Parse Tree");
        System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++");
    }

    private void printfooter() {
        System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++");
        System.out.println("Parse Tree Walk Complete");
        System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++");
    }

    private String typeStandard(String typeTest) {
        switch (typeTest) {
            case "INTEGER":
                return "int";

            case "INTEGERARRAY":
                return "int[]";

            case "integer":
                return "int";

            case "DOUBLE":
                return "double";

            case "DOUBLEARRAY":
                return "double[]";

            case "STRING":
                return "String";

            case "STRINGARRAY":
                return "String[]";

            default:
                return "ERROR: Unknown Type";
        }
    }

    private void IndentCode() {
        for (int i = 0; i < Indent; i++) {
            fileText += "    ";
        }
    }

    private String getParams(ASTFunction node) {
        String paramList = "(";
        ASTParameter Pnode;

        if (node.data.get("Param") == "true") // if there is a paramlist
        {
            ASTParameterList cnode = (ASTParameterList) node.jjtGetChild(1); // this is the location it will be at if it
                                                                             // exists
            int amount = cnode.jjtGetNumChildren();
            // node.data.get("PType");
            for (int i = 0; i < amount; i++) {
                Pnode = (ASTParameter) cnode.jjtGetChild(i);
                paramList = paramList + Pnode.data.get("type");
                paramList = paramList + " ";
                paramList = paramList + Pnode.data.get("value");
                if (!(i == (amount - 1))) {
                    paramList = paramList + ", ";
                }
            }
        }
        paramList = paramList + ")";
        return paramList;
    }

    private String getParams(ASTProcedure node) {
        String paramList = "(";
        ASTParameter Pnode;

        if (node.data.get("Param") == "true") // if there is a paramlist
        {
            ASTParameterList cnode = (ASTParameterList) node.jjtGetChild(1);
            int amount = cnode.jjtGetNumChildren();
            // node.data.get("PType");
            for (int i = 0; i < amount; i++) {
                Pnode = (ASTParameter) cnode.jjtGetChild(i);
                paramList = paramList + Pnode.data.get("type");
                paramList = paramList + " ";
                paramList = paramList + Pnode.data.get("value");
                if (!(i == (amount - 1))) {
                    paramList = paramList + ", ";
                }
            }
        }
        paramList = paramList + ")";
        return paramList;
    }

    private void addReturnStmt(String typeTest) {
        if (returnStmt) {
            returnStmt = false;
            return;
        } else {
            switch (typeTest) {
                case "INTEGER":
                    IndentCode();
                    fileText += "return 0;\r\n";
                    break;
                case "integer":
                    IndentCode();
                    fileText += "return 0;\r\n";
                    break;

                case "DOUBLE":
                    IndentCode();
                    fileText += "return 0.0;\r\n";
                    break;

                case "STRING":
                    IndentCode();
                    fileText += "ERROR: No Implementation for the type String";
                    break;
                default:
                    IndentCode();
                    fileText += "ERROR: Unknown Type";
                    break;
            }
            return;
        }
    }
}

/*
 * 
 * 
 * 
 * 
 * 
 * public Object visit(ASTVariableDeclare node, Object data) {
 * 
 * int ID = GetID();
 * // Standardize types
 * String VarID = (String) node.data.get("value");
 * String VarType = (String) node.data.get("type");
 * int lineOfDecl = (int) node.data.get("lineNo");
 * IndentCode();
 * fileText += typeStandard(VarType) + " " + VarID + ";\r\n";
 * 
 * // Print information about node
 * System.out.println("-----");
 * System.out.println("** Node " + ID + ": Variable Declareation");
 * System.out.println("**** Type: " + VarType);
 * System.out.println("**** Variable Name: " + VarID);
 * System.out.println("-----");
 * 
 * // Set up data object
 * SymbolTableEntry entry = new SymbolTableEntry(VarID, VarType, null, null,
 * lineOfDecl, null, null);
 * // add to Symbol table
 * symbolTable.put(VarID, entry);
 * 
 * // Iterate through children nodes
 * Indent++;
 * node.childrenAccept(this, data);
 * Indent--;
 * // Return to parent node (or move to sibling node if exists)
 * return null;
 * }
 */

// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
/*
 * public Object visit(ASTPrintStatement node, Object data) {
 * int ID = GetID();
 * // printf("%d", i);
 * // Standardize types
 * String VarType = (String) node.data.get("PType");
 * Object VarValue = (String) node.data.get("value");
 * Object Value; // holder for either value
 * 
 * // Retrieve variable information
 * // Object SymValue = symbolTable.get(VarValue);
 * IndentCode();
 * 
 * fileText += "printf(" + VarValue + ");\r\n";
 * 
 * if (symbolTable.get(VarValue) != null) {
 * Value = symbolTable.get(VarValue).Value;
 * } else {
 * Value = VarValue;
 * }
 * 
 * // Print information about node
 * System.out.println("-----");
 * System.out.println("** Node " + ID + ": PrintStatement");
 * System.out.println("**** Print Type: " + VarType);
 * System.out.println("**** Print Passed Variable/Value: " + VarValue);
 * System.out.println("**** Print Printed Variable/Value: " + Value);
 * System.out.println("-----");
 * 
 * // Iterate through children nodes
 * Indent++;
 * node.childrenAccept(this, data);
 * Indent--;
 * // Return to parent node (or move to sibling node if exists)
 * return null;
 * }
 */
// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!