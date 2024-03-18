import java.util.HashMap;
import java.util.LinkedList;
import java.io.FileWriter;
import java.io.IOException;

public class SymbolTable implements CompilerVisitor {

    // ################## Variables ##################
    private int IDcounter = 0;
    private int Indent = 0;
    public String fileText = "";
    private boolean returnStmt = false;
    public HashMap<String, SymbolTableEntry> symbolTable = new HashMap<String, SymbolTableEntry>();
    public int MemoryLocationTracker = 0;

    // ################## Symbol Table Methods ##################
    class SymbolTableEntry {
        String ID;
        String Type;
        Integer Size;
        Integer Dimension;
        Integer LineOfDecl;
        LinkedList<Integer> LineOfUsage;
        Object Value;
        int MemoryLocation;

        public SymbolTableEntry(
                String id, String type, Integer size, Integer dimension,
                Integer lineOfDecl, LinkedList<Integer> lineOfUsage, Object value, int memoryLocation) {
            this.ID = id;
            this.Type = type;
            this.Dimension = dimension;
            this.LineOfDecl = lineOfDecl;
            this.LineOfUsage = lineOfUsage;
            this.Value = value;
            this.MemoryLocation = memoryLocation;
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

    // Holds Name Identifier under "Value"
    // can have 0 or more (functionDeclaration() | procedureDeclaration()) children

    /**
     * Description: Declares a program 
     * 
     * * Stored Data:
     * - Key                    Value
     * --------------------------------------------------------
     * - "Value"              IDENTIFIER
     * 
     * * Children:
     * - Child #    Required/Optional    Type
     * --------------------------------------------------------
     * - 1+          Optional             ASTFunctionDeclaration || ASTProcedureDeclaration
     */
    public Object visit(ASTProgram node, Object data) {
        int ID = GetID();

        printHeader();

        // Print information about node
        System.out.println("-----");
        System.out.println("** Node " + ID + ": Program");
        System.out.println("-----");

        fileText += "int yourmain()\r\n" + "{\r\n";

        // Iterate through children nodes
        node.childrenAccept(this, data);

        fileText += "}";

        printfooter();

        printTable();

        System.out.println(fileText);
        try {
            FileWriter myWriter = new FileWriter("Output/yourmain.h");
            myWriter.write(fileText);
            myWriter.close();
        } catch (IOException e) {
            System.out.print(e.getMessage());
        } finally {

        }
        // finish walk
        return null;
    }

    /**
     * Description: Declares the structure of a function
     * 
     * * Stored Data:
     * - Key                    Value
     * --------------------------------------------------------
     * - "Value"                IDENTIFIER
     * 
     * * Children:
     * - Child #    Required/Optional    Type
     * --------------------------------------------------------
     * - 1          Required             ASTType
     * - 2          Required             ASTParameterList
     * - 3          Required             ASTBlock
     */
    public Object visit(ASTFunctionDeclaration node, Object data) {
        int ID = GetID();

        // Print information about node
        System.out.println("-----");
        System.out.println("** Node " + ID + ": FunctionDeclaration");
        System.out.println("-----");

        node.childrenAccept(this, data);

        return null;
    }

    /**
     * Description: Declares the structure of a procedure
     * 
     * * Stored Data:
     * - Key                    Value
     * --------------------------------------------------------
     * - "Value"                IDENTIFIER
     * 
     * * Children:
     * - Child #    Required/Optional    Type
     * --------------------------------------------------------
     * - 1          Required             ASTParameterList
     * - 2          Required             ASTBlock
     */
    public Object visit(ASTProcedureDeclaration node, Object data) {
        int ID = GetID();
        IndentCode();
        // Print information about node
        System.out.println("-----");
        System.out.println("** Node " + ID + ": Function");
        System.out.println("**** Returns " + node.data.get("type"));
        System.out.println("-----");

        // Iterate through children nodes
        Indent++;
        node.childrenAccept(this, data);
        Indent--;
        IndentCode();
        // Return to parent node (or move to sibling node if exists)
        return null;
    }

    /**
     * Description: Declares the structure of a list of parameters
     * 
     * * Stored Data:
     * - No Stored Data
     *
     * * Children:
     * - Child #            Required/Optional    Type
     * --------------------------------------------------------
     * - 1+ (Incrementing)  Optional            ASTParameter
     */
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

    /**
     * Description: Declares the structure of a parameter
     * 
     * * Stored Data:
     *   No Stored Data
     
     * * Children:
     * - Child #    Required/Optional    Type
     * --------------------------------------------------------
     * - 1          Required             ASTType
     * - 2          Required             ASTVariableDeclaratorId
     */
    public Object visit(ASTParameter node, Object data) {
        int ID = GetID();
        IndentCode();

        // Print information about node
        System.out.println("-----");
        System.out.println("** Node " + ID + ": Procedure");
        System.out.println("**** Returns " + node.data.get("type"));
        System.out.println("-----");

        // Iterate through children nodes
        Indent++;
        node.childrenAccept(this, data);
        Indent--;
        IndentCode();
        // Return to parent node (or move to sibling node if exists)
        return null;
    }

    /**
     * Description:
     * - Declares the structure of a variable (full statement of a variable declaration)
     * -- May have an ASTExpression as second child if assigning a value to the variable
     * 
     * * Stored Data:
     * - No stored Data
     * 
     * * Children:
     * - Child #    Required/Optional    Type
     * --------------------------------------------------------
     * - 1         Required             VariableDeclaratorID
     * - 2         Optional             ASTExpression
     */
    public Object visit(ASTVariableDeclarator node, Object data) {
        int ID = GetID();
        // get the type of the variable
        // store in symbol table

        // Print information about node
        System.out.println("-----");
        System.out.println("** Node " + ID + ": ParameterList");
        System.out.println("-----");

        node.childrenAccept(this, data);

        return null;
    }

    /**
     * Description:
     * - Holds the Name Identifier
     * -- can have 0 or more ASTExpression surrounded by brackets to signify arrays
     * 
     * * Stored Data:
     * - Key                    Value
     * --------------------------------------------------------
     * - "Value"                some identifier
     * 
     * * Children:
     * - Child #    Required/Optional    Type
     * --------------------------------------------------------
     * - 1+         Optional             ASTExpression
     */
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

    /**
     * Description:
     * - Holds the Type of a variable
     * 
     * * Stored Data:
     * - Key                    Value
     * --------------------------------------------------------
     * - "Type"                 "INTEGER", "DOUBLE", "STRING"
     * 
     * * Children:
     * - No Children
     */
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

    /**
     * Description:
     * - Root for all statements (Switchpoint for all statements)
     * -- one child
     * 
     * * Stored Data:
     * - No stored Data
     * 
     * * Children:
     * - Child #    Required/Optional    Type
     * --------------------------------------------------------
     * - 1         Required                ASTBlock          || ASTStatementExpression  || ASTIfStatement 
     *                                  || ASTWhileStatement || ASTDoStatement          || ASTReturnStatement 
     *                                  || ASTPrintStatement || ASTReadStatement
     */
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

    /**
     * Description:
     * - matches a block of code surrounded by curly braces { ... }
     * -- may not have code within the braces 
     * -- so 0 or more ASTBlockStatement children
     * 
     * * Stored Data:
     * - No stored Data
     * 
     * * Children:
     * - Child #    Required/Optional    Type
     * --------------------------------------------------------
     * - 1+         Optional             ASTBlockStatement
     */
    public Object visit(ASTBlock node, Object data) {
        int ID = GetID();

        // TODO: still needs the ability to parse if you give it a variable to
        // initialize
        IndentCode();
        // fileText = fileText + typeStandard((String) node.data.get("type")) + " " +
        // node.data.get("value") + ";\r\n";

        // Print information about node
        System.out.println("-----");
        System.out.println("** Node " + ID + ": Declaration");
        System.out.println("-----");

        node.childrenAccept(this, data);

        return null;
    }

    /**
     * Description:
     * - Switch point for Block Statements
     * 
     * * Stored Data:
     * - No stored Data
     * 
     * * Children:
     * - Child #    Required/Optional    Type
     * --------------------------------------------------------
     * - 1          Required             ASTlocalVariableDeclaration || ASTStatement || ASTProcedureDeclaration || ASTFunctionDeclaration
     */
    public Object visit(ASTBlockStatement node, Object data) {
        int ID = GetID();

        // TODO: still needs the ability to parse if you give it a variable to
        // initialize
        IndentCode();
        // fileText = fileText + typeStandard((String) node.data.get("type")) + " " +
        // node.data.get("value") + ";\r\n";

        // Print information about node
        System.out.println("-----");
        System.out.println("** Node " + ID + ": Declared Term");
        System.out.println("-----");

        node.childrenAccept(this, data);

        return null;
    }

    /**
     * Description:
     * - Root for Expression Statements
     * - Can be a simple assignment, or an increment or decrement
     * - Will have 1 or 3 children
     * -- 1 child if incrementing or decrementing
     * -- 3 children if assigning a value
     * 
     * * Stored Data:
     * - No stored Data
     * 
     * * Children:
     * - Child #    Required/Optional    Type
     * --------------------------------------------------------
     * - 1         Required              ASTType
     * - 2         Required              ASTVariableDeclarator
     * - 3         Optional              ASTVariableDeclarator
     */
    /*
     * 
    
     ASTParameterList cnode = (ASTParameterList) node.jjtGetChild(1);
     int amount = cnode.jjtGetNumChildren();
     // node.data.get("PType");
     for(
     int i = 0;i<amount;i++)
     {
                Pnode = (ASTParameter) cnode.jjtGetChild(i);
                paramList = paramList + Pnode.data.get("type");
                paramList = paramList + " ";
                paramList = paramList + Pnode.data.get("value");
                if (!(i == (amount - 1))) {
                    paramList = paramList + ", ";
                }
            }
     */
    public Object visit(ASTLocalVariableDeclaration node, Object data) {
        String Type;

        int ID = GetID();
        // Print information about node
        System.out.println("-----");
        System.out.println("** Node " + ID + ": LocalVariableDeclaration");
        System.out.println("-----");

        // get data from children
        // get the type of the variable
        ASTType cnode = (ASTType) node.jjtGetChild(0);
        Type = (String) cnode.data.get("Type");

        // get the name of the variable
        ASTVariableDeclarator cnode2 = (ASTVariableDeclarator) node.jjtGetChild(1);
        ASTVariableDeclaratorId cnode3 = (ASTVariableDeclaratorId) cnode2.jjtGetChild(0);
        String VarID = (String) cnode3.data.get("Value");
        System.out.println("VarID: " + VarID);

        // add to symbol table
        switch (Type) {
            case "INTEGER":
                symbolTable.put(VarID,
                        new SymbolTableEntry(VarID, "INTEGER", null, null, null, null, null,
                                MemoryLocationTracker));
                MemoryLocationTracker += 1;
                break;
            case "DOUBLE":
                symbolTable.put(VarID,
                        new SymbolTableEntry(VarID, "DOUBLE", null, null, null, null, null,
                                MemoryLocationTracker));
                MemoryLocationTracker += 1;
                break;
            case "STRING":
                symbolTable.put(VarID,
                        new SymbolTableEntry(VarID, "STRING", null, null, null, null, null,
                                MemoryLocationTracker));
                MemoryLocationTracker += 1;
                break;
        }

        // Return to parent node (or move to sibling node if exists)
        return null;
    }

    /**
     * Description:
     * - Root for Expression Statements
     * - Can be a simple assignment, or an increment or decrement
     * - Will have 1 or 3 children
     * -- 1 child if incrementing or decrementing
     * -- 3 children if assigning a value
     * 
     * * Stored Data:
     * - Key                    Value
     * --------------------------------------------------------
     * - "CREMENT"              "INCREMENT" or "DECREMENT"
     * 
     * * Children:
     * - Child #    Required/Optional    Type
     * --------------------------------------------------------
     * - 1         Required              ASTPrimaryExpression
     * - 2         Optional              ASTAssignmentOperator
     * - 3         Optional              ASTExpression
     */
    public Object visit(ASTStatementExpression node, Object data) {
        int ID = GetID();
        String VarID;
        String VarType;
        String AssignOp = "";
        String Crement = "";
        Object VarValue = "";
        String VarValueType;
        int VarMemoryLocation;
        Boolean boolCrement = false;
        // Print information about node
        System.out.println("-----");
        System.out.println("** Node " + ID + ": Assignment");
        System.out.println("-----");

        // get data from children
        // get variable name from child 1
        ASTPrimaryExpression cnode = (ASTPrimaryExpression) node.jjtGetChild(0);
        ASTPrimaryPrefix cnode2 = (ASTPrimaryPrefix) cnode.jjtGetChild(0);
        ASTLiteral cnode3 = (ASTLiteral) cnode2.jjtGetChild(0);
        VarID = (String) cnode3.data.get("Value");
        VarType = (String) cnode3.data.get("Type");
        System.out.println("VarID: " + VarID);
        System.out.println("VarType: " + VarType);

        // get memory location of variable in Symbol Table
        VarMemoryLocation = symbolTable.get((String) VarID).MemoryLocation;

        // get the assignment operator from child 2 (if it exists)
        if (node.jjtGetNumChildren() > 1) {

            ASTAssignmentOperator cnode4 = (ASTAssignmentOperator) node.jjtGetChild(1);
            AssignOp = (String) cnode4.data.get("Assignment");

            // get the value to assign from child 3 (if it exists)
            ASTExpression cnode5 = (ASTExpression) node.jjtGetChild(2);
            ASTConditionalOrExpression cnode6 = (ASTConditionalOrExpression) cnode5.jjtGetChild(0);
            ASTConditionalAndExpression cnode7 = (ASTConditionalAndExpression) cnode6.jjtGetChild(0);
            ASTEqualityExpression cnode8 = (ASTEqualityExpression) cnode7.jjtGetChild(0);
            ASTRelationalExpression cnode9 = (ASTRelationalExpression) cnode8.jjtGetChild(0);
            ASTAdditiveExpression cnode10 = (ASTAdditiveExpression) cnode9.jjtGetChild(0);
            ASTMultiplicativeExpression cnode11 = (ASTMultiplicativeExpression) cnode10.jjtGetChild(0);
            ASTUnaryExpression cnode12 = (ASTUnaryExpression) cnode11.jjtGetChild(0);
            ASTPostfixExpression cnode13 = (ASTPostfixExpression) cnode12.jjtGetChild(0);
            ASTPrimaryExpression cnode14 = (ASTPrimaryExpression) cnode13.jjtGetChild(0);
            ASTPrimaryPrefix cnode15 = (ASTPrimaryPrefix) cnode14.jjtGetChild(0);
            ASTLiteral cnode16 = (ASTLiteral) cnode15.jjtGetChild(0);
            VarValue = cnode16.data.get("Value");
            VarValueType = (String) cnode16.data.get("Type");

        } else {
            boolCrement = true;
            Crement = (String) node.data.get("CREMENT");
        }

        fileText += "R[1] = Mem[SR+" + VarMemoryLocation + "];\r\n";
        fileText += "F23_Time += (1);\r\n";
        if (boolCrement) {
            // if incrementing or decrementing
            // write the code to file
            if (Crement == "INCREMENT") {
                fileText += "R[1] = R[1] + 1;\r\n";
            } else {
                fileText += "R[1] = R[1] - 1;\r\n";
            }
            fileText += "F23_Time += (1);\r\n";

        } else {
            // if assigning a value
            // write the code to file
            switch (AssignOp) {
                case "ASSIGN":
                    fileText += "R[1] = " + VarValue + ";\r\n";
                    break;
                case "PLUS":
                    fileText += "R[1] = R[1] + " + VarValue + ";\r\n";
                    break;
                case "MINUS":
                    fileText += "R[1] = R[1] - " + VarValue + ";\r\n";
                    break;
                case "MULTIPLY":
                    fileText += "R[1] = R[1] * " + VarValue + ";\r\n";
                    break;
                case "DIVIDE":
                    fileText += "R[1] = R[1] / " + VarValue + ";\r\n";
                    break;
                case "MOD":
                    fileText += "R[1] = R[1] % " + VarValue + ";\r\n";
                    break;
            }
            fileText += "F23_Time += (1);\r\n";
        }
        fileText += "Mem[SR+" + VarMemoryLocation + "] = R[1];\r\n";
        fileText += "F23_Time += (20+1);\r\n";

        // Return to parent node (or move to sibling node if exists)
        return null;
    }

    /**
     * Description:
     * - If statement (while (x < 10) { ... })
     * -- ASTExpression to evaluate the boolean
     * -- ASTStatement to execute if the boolean is true
     * -- ASTStatement to execute if the boolean is false (optional)
     * 
     * * Stored Data:
     * - No stored Data
     * 
     * * Children:
     * - Child #    Required/Optional    Type
     * --------------------------------------------------------
     * - 1         Required             ASTExpression
     * - 2         Required             ASTStatement
     * - 3         Optional             ASTStatement
     */
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

    /**
     * Description:
     * - While statement (while (x < 10) { ... })
     * -- ASTExpression to evaluate the boolean
     * -- ASTStatement to execute if the boolean is true
     * 
     * * Stored Data:
     * - No stored Data
     * 
     * * Children:
     * - Child #    Required/Optional    Type
     * --------------------------------------------------------
     * - 1         Required             ASTExpression
     * - 2         Required             ASTStatement
     */
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

    /**
     * Description:
     * - For loop statements (int i = 0; i < 10; i++) { ... }
     * -- 
     * * Stored Data:
     * - No stored Data
     * 
     * * Children:
     * - Child #    Required/Optional    Type
     * --------------------------------------------------------
     * - 1          Optional             ASTDoInit
     * - 2          Optional             ASTExpression
     * - 3          Optional             ASTStatementExpressionList
     * - 4          Required             ASTStatement
     */
    public Object visit(ASTDoStatement node, Object data) {
        int ID = GetID();

        // Print information about node
        System.out.println("-----");
        System.out.println("** Node " + ID + ": FunctionCall");
        System.out.println("-----");

        node.childrenAccept(this, data);

        return null;
    }

    /**
     * Description:
     * - Initializer options for the ASTDoStatement (for-loop) ((int i = 0; ...))
     * -- 
     * * Stored Data:
     * - No stored Data
     * 
     * * Children:
     * - Child #    Required/Optional    Type
     * --------------------------------------------------------
     * - 1          Required             ASTLocalVariableDeclaration || ASTStatementExpressionList
     */
    public Object visit(ASTDoInit node, Object data) {
        int ID = GetID();

        // Print information about node
        System.out.println("-----");
        System.out.println("** Node " + ID + ": FunctionCall");
        System.out.println("-----");

        node.childrenAccept(this, data);

        return null;
    }

    /**
     * Description:
     * - seperates a list of ASTStatementExpressions with commas
     * 
     * * Stored Data:
     * - No stored Data
     * 
     * * Children:
     * - Child #    Required/Optional    Type
     * --------------------------------------------------------
     * - 1          Required             ASTStatementExpression
     * - 2+         Optional             ASTStatementExpression
     */
    public Object visit(ASTStatementExpressionList node, Object data) {
        int ID = GetID();

        // Print information about node
        System.out.println("-----");
        System.out.println("** Node " + ID + ": ASTStatementExpressionList");
        System.out.println("-----");

        node.childrenAccept(this, data);

        return null;
    }

    /**
     * Description:
     * - marks a return statement
     * 
     * * Stored Data:
     * - No stored Data
     * 
     * * Children:
     * - Child #    Required/Optional    Type
     * --------------------------------------------------------
     * - 1          Optional             ASTExpression
     */
    public Object visit(ASTReturnStatement node, Object data) {
        int ID = GetID();

        // Print information about node
        System.out.println("-----");
        System.out.println("** Node " + ID + ": ASTReturnStatement");
        System.out.println("-----");

        node.childrenAccept(this, data);

        return null;
    }

    /**
     * Description:
     * - Switch point for Print Statments
     * 
     * * Stored Data:
     * - No stored Data
     * 
     * * Children:
     * - Child #    Required/Optional    Type
     * --------------------------------------------------------
     * - 1          Required             ASTPrintIntStatement || ASTPrintDoubleStatement || ASTPrintStringStatement
     */
    public Object visit(ASTPrintStatement node, Object data) {
        int ID = GetID();

        // Print information about node
        System.out.println("-----");
        System.out.println("** Node " + ID + ": ASTPrintStatement");
        System.out.println("-----");

        node.childrenAccept(this, data);

        return null;
    }

    /**
     * Description:
     * - Print a intager value to the console
     * 
     * * Stored Data:
     * - No stored Data
     * 
     * * Children:
     * - Child #    Required/Optional    Type
     * --------------------------------------------------------
     * - 1          Required             ASTExpression
     */
    public Object visit(ASTPrintIntStatement node, Object data) {
        String VarValue;
        int VarMemoryLocation;
        int ID = GetID();

        // Print information about node
        System.out.println("-----");
        System.out.println("** Node " + ID + ": ASTPrintIntStatement");
        System.out.println("-----");

        // get the value to print
        ASTExpression cnode = (ASTExpression) node.jjtGetChild(0);
        ASTConditionalOrExpression cnode2 = (ASTConditionalOrExpression) cnode.jjtGetChild(0);
        ASTConditionalAndExpression cnode3 = (ASTConditionalAndExpression) cnode2.jjtGetChild(0);
        ASTEqualityExpression cnode4 = (ASTEqualityExpression) cnode3.jjtGetChild(0);
        ASTRelationalExpression cnode5 = (ASTRelationalExpression) cnode4.jjtGetChild(0);
        ASTAdditiveExpression cnode6 = (ASTAdditiveExpression) cnode5.jjtGetChild(0);
        ASTMultiplicativeExpression cnode7 = (ASTMultiplicativeExpression) cnode6.jjtGetChild(0);
        ASTUnaryExpression cnode8 = (ASTUnaryExpression) cnode7.jjtGetChild(0);
        ASTPostfixExpression cnode9 = (ASTPostfixExpression) cnode8.jjtGetChild(0);
        ASTPrimaryExpression cnode10 = (ASTPrimaryExpression) cnode9.jjtGetChild(0);
        ASTPrimaryPrefix cnode11 = (ASTPrimaryPrefix) cnode10.jjtGetChild(0);
        ASTLiteral cnode12 = (ASTLiteral) cnode11.jjtGetChild(0);
        VarValue = (String) cnode12.data.get("Value");
        System.out.println("VarValue: " + VarValue);

        // get memory location of variable in Symbol Table
        VarMemoryLocation = symbolTable.get(VarValue).MemoryLocation;

        fileText += "print_int( Mem[SR+" + VarMemoryLocation + "] );\r\n";

        node.childrenAccept(this, data);

        return null;
    }

    /**
     * Description:
     * - Print a double value to the console
     * 
     * * Stored Data:
     * - No stored Data
     * 
     * * Children:
     * - Child #    Required/Optional    Type
     * --------------------------------------------------------
     * - 1          Required             ASTExpression
     */
    public Object visit(ASTPrintDoubleStatement node, Object data) {
        int ID = GetID();

        // Print information about node
        System.out.println("-----");
        System.out.println("** Node " + ID + ": Value");
        System.out.println("-----");

        node.childrenAccept(this, data);

        return null;
    }

    /**
     * Description:
     * - Print a string to the console
     * 
     * * Stored Data:
     * - No stored Data
     * 
     * * Children:
     * - Child #    Required/Optional    Type
     * --------------------------------------------------------
     * - 1          Required             ASTExpression
     */
    public Object visit(ASTPrintStringStatement node, Object data) {
        int ID = GetID();

        // Print information about node
        System.out.println("-----");
        System.out.println("** Node " + ID + ": AtomicValue");
        System.out.println("-----");

        node.childrenAccept(this, data);

        return null;
    }

    /**
     * Description:
     * - Switch point for Read Statments
     * 
     * * Stored Data:
     * - No stored Data
     * 
     * * Children:
     * - Child #    Required/Optional    Type
     * --------------------------------------------------------
     * - 1          Required             ASTReadIntStatement || ASTReadDoubleStatement || ASTReadStringStatement
     */
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

    /**
     * Description:
     * - Get a user input for a int
     * 
     * * Stored Data:
     * - Key                    Value
     * --------------------------------------------------------
     * - "value"                Some Identifier to store the information
     * 
     * * Children:
     * - No Children
     */
    public Object visit(ASTReadIntStatement node, Object data) {
        int ID = GetID();

        // Print information about node
        System.out.println("-----");
        System.out.println("** Node " + ID + ": Predicate");
        System.out.println("-----");

        node.childrenAccept(this, data);

        return null;
    }

    /**
     * Description:
     * - Get a user input for a double
     * 
     * * Stored Data:
     * - Key                    Value
     * --------------------------------------------------------
     * - "value"                Some Identifier to store the information
     * 
     * * Children:
     * - No Children
     */
    public Object visit(ASTReadDoubleStatement node, Object data) {
        int ID = GetID();

        // Print information about node
        System.out.println("-----");
        System.out.println("** Node " + ID + ": Predicate");
        System.out.println("-----");

        node.childrenAccept(this, data);

        return null;
    }

    /**
     * Description:
     * - Get a user input for a string
     * 
     * * Stored Data:
     * - Key                    Value
     * --------------------------------------------------------
     * - "value"                Some Identifier to store the information
     * 
     * * Children:
     * - No Children
     */
    public Object visit(ASTReadStringStatement node, Object data) {
        int ID = GetID();

        // Print information about node
        System.out.println("-----");
        System.out.println("** Node " + ID + ": ConnectiveOperator");
        System.out.println("-----");

        node.childrenAccept(this, data);

        return null;
    }

    /**
     * Description:
     * - Starting point for evaluating an expression
     * - Will have 1 or 3 children
     * 
     * * Stored Data:
     * - No stored Data
     * 
     * * Children:
     * - Child #    Required/Optional    Type
     * --------------------------------------------------------
     * - 1          Required             ASTConditionalOrExpression
     * - 2          Optional             ASTAssignmentOperator
     * - 3          Optional             ASTExpression
     */
    public Object visit(ASTExpression node, Object data) {
        int ID = GetID();

        // Print information about node
        System.out.println("-----");
        System.out.println("** Node " + ID + ": CompareOperator");
        System.out.println("-----");

        node.childrenAccept(this, data);

        return null;
    }

    /**
     * Description:
     * - Stores the Assignment Operator
     * 
     * * Stored Data:
     * - Key                    Value
     * --------------------------------------------------------
     * - "Assignment"      "ASSIGN", "DIVIDE", "MINUS", "MOD", "MULTIPLY", or "PLUS"
     * 
     * * Children:
     * - No Children
     */
    public Object visit(ASTAssignmentOperator node, Object data) {
        int ID = GetID();

        // Print information about node
        System.out.println("-----");
        System.out.println("** Node " + ID + ": MathOperator");
        System.out.println("-----");

        node.childrenAccept(this, data);

        return null;
    }

    /**
     * Description:
     * - Compares "OR" logic relations between two children.
     * - If there is one child there is no "OR" logic,
     * - Otherwise there is "OR" logic between each child
     * 
     * Stored Data:
     * - No stored Data
     * 
     * * Children:
     * - Child #    Required/Optional    Type
     * --------------------------------------------------------
     * - 1          Required             ASTConditionalAndExpression
     * - 2+         Optional             ASTConditionalAndExpression
     */
    public Object visit(ASTConditionalOrExpression node, Object data) {
        int ID = GetID();

        // Print information about node
        System.out.println("-----");
        System.out.println("** Node " + ID + ": DoLoop");
        System.out.println("-----");

        node.childrenAccept(this, data);

        return null;
    }

    /**
     * Description:
     * - Compares "AND" logic relations between two children.
     * - If there is one child there is no "AND" logic,
     * - Otherwise there is "AND" logic between each child
     * 
     * Stored Data:
     * - No stored Data
     * 
     * * Children:
     * - Child #    Required/Optional    Type
     * --------------------------------------------------------
     * - 1          Required             ASTEqualityExpression
     * - 2+         Optional             ASTEqualityExpression
     */
    public Object visit(ASTConditionalAndExpression node, Object data) {
        int ID = GetID();

        // Print information about node
        System.out.println("-----");
        System.out.println("** Node " + ID + ": WhileLoop");
        System.out.println("-----");

        node.childrenAccept(this, data);

        return null;
    }

    /**
     * Description: Compares equality relations between two children
     * 
     * * Stored Data:
     * - Key                    Value
     * --------------------------------------------------------
     * - 1+ (incrementing)      "DEQ" (==), "NE" (!=)
     * 
     * * Children:
     * - Child #    Required/Optional    Type
     * --------------------------------------------------------
     * - 1          Required             ASTRelationalExpression
     * - 2+         Optional             ASTRelationalExpression
     */
    public Object visit(ASTEqualityExpression node, Object data) {
        int ID = GetID();

        // Print information about node
        System.out.println("-----");
        System.out.println("** Node " + ID + ": IfThen");
        System.out.println("-----");

        node.childrenAccept(this, data);

        return null;
    }

    /**
     * Description:
     * - Compares relational relations between two children
     * - If there is one child there is no relational logic,
     * - Otherwise there is relational logic between each child
     * 
     * * Stored Data:
     * - Key                    Value
     * --------------------------------------------------------
     * - 1+ (incrementing)      "LT" (<), "GT" (>), "LEQ" (<=), "GEQ" (>=)
     * 
     * * Children:
     * - Child #    Required/Optional    Type
     * --------------------------------------------------------
     * - 1          Required             ASTAdditiveExpression
     * - 2+         Optional             ASTAdditiveExpression
     */
    public Object visit(ASTRelationalExpression node, Object data) {
        int ID = GetID();

        // Print information about node
        System.out.println("-----");
        System.out.println("** Node " + ID + ": ");
        System.out.println("-----");

        node.childrenAccept(this, data);

        return null;
    }

    /**
     * Description:
     * - Compares additive relations between two children
     * - If there is one child there is no additive logic,
     * - Otherwise there is additive logic between each child
     * 
     * * Stored Data:
     * - Key                    Value
     * --------------------------------------------------------
     * - 1+ (incrementing)      "PLUS", "MINUS"
     * 
     * * Children:
     * - Child #    Required/Optional    Type
     * --------------------------------------------------------
     * - 1          Required             ASTMultiplicativeExpression
     * - 2+         Optional             ASTMultiplicativeExpression
     * 
     */
    public Object visit(ASTAdditiveExpression node, Object data) {
        int ID = GetID();

        // Print information about node
        System.out.println("-----");
        System.out.println("** Node " + ID + ": ");
        System.out.println("-----");

        node.childrenAccept(this, data);

        return null;
    }

    /**
     * Description:
     * - Compares multiplicative relations between two children
     * - If there is one child there is no multiplicative logic,
     * - Otherwise there is multiplicative logic between each child
     * 
     * * Stored Data:
     * - Key                    Value
     * --------------------------------------------------------
     * - 1+ (incrementing)      "MULTIPLY", "DIVIDE" or "MOD"
     * 
     * * Children:
     * - Child #    Required/Optional    Type
     * --------------------------------------------------------
     * - 1          Required             ASTUnaryExpression
     * - 2+         Optional             ASTUnaryExpression
     */
    public Object visit(ASTMultiplicativeExpression node, Object data) {
        int ID = GetID();

        // Print information about node
        System.out.println("-----");
        System.out.println("** Node " + ID + ": ");
        System.out.println("-----");

        node.childrenAccept(this, data);

        return null;
    }

    /**
     * Description:
     * - gives a unary expression for an expression (+5 or -3 or !true)
     * - if the child is a UnaryExpression, the there is stored data
     * - if the child is a PostfixExpression, then no data is stored
     * 
     * Stored Data:
     * - Key: "Unary"
     * - Value: String, can be:
     * -- "PLUS"
     * -- "MINUS"
     * -- "NOT"
     * 
     * Children:
     * - Child #    Required/Optional    Type
     * --------------------------------------------------------
     * - 1          Required             ASTUnaryExpression || ASTPostfixExpression
     */
    public Object visit(ASTUnaryExpression node, Object data) {
        int ID = GetID();

        // Print information about node
        System.out.println("-----");
        System.out.println("** Node " + ID + ": ");
        System.out.println("-----");

        node.childrenAccept(this, data);

        return null;
    }

    /**
     * Description:
     * - gives a postfix expression for an expression (5++ or 3--)
     * 
     * Stored Data:
     * - Key: "Postfix"
     * - Value: String, can be:
     * -- "INCREMENT"
     * -- "DECREMENT"
     * 
     * Children:
     * - Child #    Type                Required or Optional
     * --------------------------------------------------------
     * - 1          ASTPrimaryExpression    Required
     */
    public Object visit(ASTPostfixExpression node, Object data) {
        int ID = GetID();

        // Print information about node
        System.out.println("-----");
        System.out.println("** Node " + ID + ": ");
        System.out.println("-----");

        node.childrenAccept(this, data);

        return null;
    }

    /**
     * Description:
     * - Seperates a Expression into a prefix and 0 or more suffixes
     * 
     * Stored Data:
     * - No stored Data
     * 
     * Children:
     * - Child #    Type                Required or Optional
     * --------------------------------------------------------
     * - 1          ASTPrimaryPrefix    Required
     * - 2+         ASTPrimarySuffix    Optional
     */
    public Object visit(ASTPrimaryExpression node, Object data) {
        int ID = GetID();

        // Print information about node
        System.out.println("-----");
        System.out.println("** Node " + ID + ": ");
        System.out.println("-----");

        node.childrenAccept(this, data);

        return null;
    }

    /**
     * Description:
     * - Either holds a value or an expression in parentheses
     * 
     * Stored Data:
     * - No stored Data
     * 
     * Children:
     * - Child #    Type                                Required or Optional
     * --------------------------------------------------------
     * - 1          ASTLiteral || ASTExpression         Required
     */
    public Object visit(ASTPrimaryPrefix node, Object data) {
        int ID = GetID();

        // Print information about node
        System.out.println("-----");
        System.out.println("** Node " + ID + ": ");
        System.out.println("-----");

        node.childrenAccept(this, data);

        return null;
    }

    /**
     * Description:
     * - either holds a expression in brackets or arguments
     * 
     * Stored Data:
     * - No stored Data
     * 
     * Children:
    * -  Child #        Type                                Required or Optional
     * --------------------------------------------------------
     * - 1              ASTPrimaryPrefix || ASTExpression   Required
     */
    public Object visit(ASTPrimarySuffix node, Object data) {
        int ID = GetID();

        // Print information about node
        System.out.println("-----");
        System.out.println("** Node " + ID + ": ");
        System.out.println("-----");

        node.childrenAccept(this, data);

        return null;
    }

    /**
     * Description:
     * - holds the value of a variable name, a constant, or a function call name
     * 
     * Stored Data:
     * Key            | Value
     * -------------------------
     * "Type"         | "IDENTIFIER", "ICONSTANT", "DCONSTANT", "SCONSTANT"
     * "Value"        | raw value
     * 
     * Children:
     * - No Children
     */
    public Object visit(ASTLiteral node, Object data) {
        int ID = GetID();

        // Print information about node
        System.out.println("-----");
        System.out.println("** Node " + ID + ": ");
        System.out.println("-----");

        node.childrenAccept(this, data);

        return null;
    }

    /**
     * Description:
     * - matches parentheses around a list of arguments
     * 
     * Stored Data:
     * - no stored data
     * 
     * Children:
     * Child #    Type                Required or Optional
     * --------------------------------------------------------
     * 1          ASTArgumentList     Optional
     */
    public Object visit(ASTArguments node, Object data) {
        int ID = GetID();

        // Print information about node
        System.out.println("-----");
        System.out.println("** Node " + ID + ": ");
        System.out.println("-----");

        node.childrenAccept(this, data);

        return null;
    }

    /**
     * Description:
     * - matches a list of arguments
     * 
     * Stored Data:
     * - no stored data
     * 
     * Children:
     * - Child #    Type                Required or Optional
     * --------------------------------------------------------
     * - 1          ASTExpression       Required
     * - 2+         ASTExpression       Optional
     */
    public Object visit(ASTArgumentList node, Object data) {
        int ID = GetID();

        // Print information about node
        System.out.println("-----");
        System.out.println("** Node " + ID + ": ");
        System.out.println("-----");

        node.childrenAccept(this, data);

        return null;
    }

    //************ Helper Functions **********************************************/
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

    private String getParams(ASTFunctionDeclaration node) {
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

    private String getParams(ASTProcedureDeclaration node) {
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