import java.util.HashMap;
import java.util.LinkedList;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.FileWriter;
import java.io.IOException;

public class SymbolTable implements CompilerVisitor {

    // ################## Variables ##################
    private int IDcounter = 0;
    static int labelCounter = 1; // start from 1 because L000 is reserved for main
    static int TableIDCounter = 0;
    public scopedSymbolTable symbolTable = new scopedSymbolTable();
    public MemoryHelper memoryHelper = new MemoryHelper();
    public ClassFileWriter fileWriter = new ClassFileWriter("Output/yourmain.h");

    // ################## Symbol Table Methods ##################
    class scopedSymbolTable {
        HashMap<String, SymbolTableEntry> symbolTable = new HashMap<String, SymbolTableEntry>();
        scopedSymbolTable parent;
        LinkedList<scopedSymbolTable> children;
        int scopeLevel = 0;
        int TableID = 0;

        public scopedSymbolTable() {
            this.parent = null;
            this.scopeLevel = 0;
            this.TableID = TableIDCounter;
            this.children = new LinkedList<scopedSymbolTable>();
        }

        public scopedSymbolTable(scopedSymbolTable parent) {
            this.parent = parent;
            this.scopeLevel = parent.scopeLevel + 1;
            this.TableID = TableIDCounter++;
            this.children = new LinkedList<scopedSymbolTable>();
            parent.children.add(this);
        }

        public void addChild(scopedSymbolTable child) {
            this.children.add(child);
        }

        public void addEntry(String id, SymbolTableEntry entry) {
            this.symbolTable.put(id, entry);
        }

        public void addLineOfUse(String id, int lineOfUse) {
            SymbolTableEntry entry = getEntry(id);
            entry.LineOfUsage.add(lineOfUse);
        }

        public String getNewLabel() {
            return String.format("L%03d", labelCounter++); // generate label
        }

        // Overloaded method to add entry with label
        public void addEntry(String ID, String Type, int Size, int LineOfDecl,
                Object Value, boolean addLabel) {
            String label = null;
            if (addLabel) {
                if (ID.equals("main")) {
                    label = "L000"; // reserve L000 for main
                } else {
                    label = String.format("L%03d", labelCounter++); // generate label
                }
            }

            SymbolTableEntry entry = new SymbolTableEntry(ID, Type, Size, LineOfDecl, null, Value,
                    memoryHelper.requestVariable(Type), label);

            symbolTable.put(ID, entry);
            addLineOfUse(ID, LineOfDecl);

        }

        public SymbolTableEntry getEntry(String id) {
            SymbolTableEntry entry = this.symbolTable.get(id);
            if (entry != null) {
                return entry;
            } else if (this.parent != null) {
                return this.parent.getEntry(id);
            } else {
                return null;
            }
        }

        public boolean containsKey(String id) {
            if (this.symbolTable.containsKey(id)) {
                return true;
            } else if (this.parent != null) {
                return this.parent.containsKey(id);
            } else {
                return false;
            }
        }

        public void printScopedTable() {
            System.out.printf("%n--------------------------------%n");
            System.out.printf("      Symbol Table Values       %n");
            System.out.printf("      TableID:  %-10s           %n", this.TableID);
            System.out.printf("      ScopeLevel:  %-10s        %n", this.scopeLevel);
            System.out.printf("                                %n");
            System.out.printf("--------------------------------%n");
            System.out.printf("| %-10s | %-10s| %-10s | %-10s | %-10s | %-10s |%n",
                    "ID", "Type", "Size", "LineOfDecl", "value", "Label");
            System.out.printf("--------------------------------%n");
            for (SymbolTableEntry i : symbolTable.values()) {
                System.out.printf("| %-10s | %-10s | %-10s | %-10s | %-10s | %-10s |%n",
                        i.ID, i.Type, i.Size, i.LineOfDecl, i.Value, i.Label);
            }
            System.out.printf("--------------------------------%n%n");

            for (scopedSymbolTable i : children) {
                i.printScopedTable();
            }
        }

    }

    class SymbolTableEntry {

        String ID;
        /** INTEGER, DOUBLE, STRING, BOOLEAN, FUNCTION, PROCEDURE */
        String Type;
        /** for storing string size*/
        Integer Size;
        /** Line that the item was declared on */
        Integer LineOfDecl;
        /** Line that the item was used on */
        LinkedList<Integer> LineOfUsage;
        /** Raw value of the object, must be cast */
        Object Value;
        /** intager version of where it is stored in memory */
        int MemoryLocation;
        /** Label of the format L###; used for go-tos */
        String Label;

        public SymbolTableEntry(
                String id, String type, Integer size,
                Integer lineOfDecl, Object value, int memoryLocation) {
            this.ID = id;
            /// INTEGER, DOUBLE, STRING, BOOLEAN, FUNCTION, PROCEDURE
            this.Type = type; // INTEGER, DOUBLE, STRING, BOOLEAN, FUNCTION, PROCEDURE
            this.LineOfDecl = lineOfDecl;
            this.LineOfUsage = new LinkedList<Integer>();
            this.Value = value;
            this.MemoryLocation = memoryLocation;
            this.Label = null;
        }

        public SymbolTableEntry(String ID, String Type, int Size, int LineOfDecl,
                LinkedList<Integer> lineOfUsage, Object Value, int memoryLocation, String Label) {
            this(ID, Type, Size, LineOfDecl, Value, memoryLocation); // call existing constructor
            this.Label = Label; // assign label
        }
    }

    class VisitReturn {
        int memoryLocation;
        String type;
        String SymbolTableEntry;

        public VisitReturn(int memoryLocation, String type) {
            this.memoryLocation = memoryLocation;
            this.type = type;
            this.SymbolTableEntry = "";
        }

        public VisitReturn() {
            this.memoryLocation = -1;
            this.type = "";
            this.SymbolTableEntry = "";
        }
    }

    class MemoryHelper {
        private LinkedList<Integer> intRegisters; // avalable int registers
        private LinkedList<Integer> floatRegisters; // available DOUBLE registers
        private LinkedList<Integer> Mem; // available int memory locations
        private LinkedList<Integer> FMem; // available DOUBLE memory locations
        private LinkedList<Integer> SMem; // available string memory locations
        private int MemCounter; // counter for int variables
        private int FMemCounter; // counter for DOUBLE variables
        private int SMemCounter; // counter for string variables
        private static final int MAX_INT_REGISTERS = 32; // Maximum number of int registers
        private static final int MAX_FLOAT_REGISTERS = 16; // Maximum number of float registers

        public MemoryHelper() {
            intRegisters = new LinkedList<>();
            floatRegisters = new LinkedList<>();
            Mem = new LinkedList<>();
            FMem = new LinkedList<>();
            SMem = new LinkedList<>();
            MemCounter = 0;
            initializeRegisters(); // Added: Initialize registers
        }

        private void initializeRegisters() {
            for (int i = 0; i < MAX_INT_REGISTERS; i++) {
                intRegisters.add(i);
            }
            for (int i = 0; i < MAX_FLOAT_REGISTERS; i++) {
                floatRegisters.add(i);
            }
        }

        public int requestIntRegister() {
            if (intRegisters.isEmpty()) {
                System.out.println("No available registers.");
                return -1; // Return -1 to indicate no available register
            }
            int register = intRegisters.removeFirst(); // Get the first available register
            return register;
        }

        public int requestFloatRegister() {
            if (floatRegisters.isEmpty()) {
                System.out.println("No available registers.");
                return -1; // Return -1 to indicate no available register
            }
            int register = floatRegisters.removeFirst(); // Get the first available register
            return register;
        }

        public void returnIntRegister(int register) {
            if (register < MAX_INT_REGISTERS) {
                intRegisters.add(register);
            }
        }

        public void returnFloatRegister(int register) {
            if (register < MAX_FLOAT_REGISTERS) {
                floatRegisters.add(register);
            }
        }

        public void returnVariable(int variable, String type) {
            if (type.equals("INTEGER")) {
                Mem.add(variable);
            } else if (type.equals("DOUBLE")) {
                FMem.add(variable);
            } else if (type.equals("STRING")) {
                SMem.add(variable);
            } else if (type.equals("BOOLEAN")) {
                Mem.add(variable);
            }
        }

        public LinkedList<Integer> getRegisters() {
            return intRegisters;
        }

        public LinkedList<Integer> getFloatingRegisters() {
            return floatRegisters;
        }

        public LinkedList<Integer> getMem() {
            return Mem;
        }

        public LinkedList<Integer> getFMem() {
            return FMem;
        }

        public LinkedList<Integer> getSMem() {
            return SMem;
        }

        public Integer requestVariable(String type) {
            if (type.equals("INTEGER")) {
                if (!Mem.isEmpty()) {
                    return Mem.removeFirst();
                } else {
                    return MemCounter++;
                }
            } else if (type.equals("DOUBLE")) {
                if (!FMem.isEmpty()) {
                    return FMem.removeFirst();
                } else {
                    return FMemCounter++;
                }
            } else if (type.equals("STRING")) {
                if (!SMem.isEmpty()) {
                    return SMem.removeFirst();
                } else {
                    return SMemCounter++;
                }
            } else if (type.equals("BOOLEAN")) {
                if (!Mem.isEmpty()) {
                    return Mem.removeFirst();
                } else {
                    return MemCounter++;
                }
            } else {
                return -1;
            }
        }
    }

    // need to implement strings in its functions
    public class ClassFileWriter {
        private String fileName;
        private StringBuilder fileText;

        public ClassFileWriter(String fileName) {
            this.fileName = fileName;
            this.fileText = new StringBuilder();
        }

        public void addComment(String comment, String functionName) {
            fileText.append("\t\t\t// Comment From: " + functionName + "\r\n");
            fileText.append("\t\t\t// " + comment + "\r\n");
        }

        public void addCode(String code, String functionName) {
            fileText.append("\t\t\t// Generated from function: " + functionName + "\r\n");
            fileText.append(code);
        }

        public void loadRegister(int register, int memoryLocation, String type, String functionName) {
            fileText.append("\t\t\t// Generated from function: " + functionName + "\r\n");
            if (type.equals("INTEGER")) {
                fileText.append("R[" + register + "] = Mem[SR+" + memoryLocation + "];\r\n");
                fileText.append("F23_Time += (20 + 1);\r\n");
            } else if (type.equals("DOUBLE")) {
                fileText.append("F[" + register + "] = FMem[FR+" + memoryLocation + "];\r\n");
                fileText.append("F23_Time += (20 + 2);\r\n");
            } else {
                throw new IllegalArgumentException(
                        "Type: " + type + " not implemented in loadRegister(int, int, String)");
            }
        }

        public void loadRegister(int register, String type, String value, String functionName) {
            fileText.append("\t\t\t// Generated from function: " + functionName + "\r\n");
            if (type.equals("INTEGER")) {
                fileText.append("R[" + register + "] = " + value + ";\r\n");
                fileText.append("F23_Time += (1);\r\n");
            } else if (type.equals("DOUBLE")) {
                fileText.append("F[" + register + "] = " + value + ";\r\n");
                fileText.append("F23_Time += (2);\r\n");
            } else {
                throw new IllegalArgumentException(
                        "Type: " + type + " not implemented in loadRegister(int, String, String)");
            }
        }

        public void storeRegister(int register, VisitReturn vr, String functionName) {
            fileText.append("\t\t\t// Generated from function: " + functionName + "\r\n");
            if (vr.type.equals("INTEGER")) {
                fileText.append("Mem[SR+" + vr.memoryLocation + "] = R[" + register + "];\r\n");
                fileText.append("F23_Time += (20 + 1);\r\n");
            } else if (vr.type.equals("DOUBLE")) {
                fileText.append("FMem[FR+" + vr.memoryLocation + "] = F[" + register + "];\r\n");
                fileText.append("F23_Time += (20 + 2);\r\n");
            } else {
                throw new IllegalArgumentException("Type: " + vr.type + " not implemented in storeRegister");
            }
        }

        public void LoadStrToSMem(int memoryLocation, String value, String functionName) {
            fileText.append("\t\t\t// Generated from function: " + functionName + "\r\n");
            fileText.append("strcpy(SMem[" + memoryLocation + "], " + value + ");\r\n");
            fileText.append("F23_Time += (60 * " + value.length() + ");\r\n");
        }

        public void writeToFile() {
            try {
                FileWriter writer = new FileWriter(fileName);
                writer.write(fileText.toString());
                writer.close();
            } catch (Exception e) {
                System.out.print(e.getMessage());
            } finally {
            }
        }
    }

    // ################## Visit Methods ##################

    private int GetID() {
        IDcounter++;
        return IDcounter;
    }

    private void printNode(int ID, String nodeType) {
        System.out.println("-----");
        System.out.println("** Node " + ID + ": " + nodeType);
        System.out.println("-----");
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

    // ToDo: add a jump to the Label for main()
    public Object visit(ASTProgram node, Object data) {
        int ID = GetID();

        printHeader();

        // Print information about node
        printNode(ID, "ASTProgram");

        fileWriter.addCode("int yourmain()\r\n" + "{\r\n", "ASTProgram");
        fileWriter.addCode("goto L000;\r\n", "ASTProgram");
        // Iterate through children nodes
        node.childrenAccept(this, data);

        fileWriter.addCode("}", "ASTProgram");
        printfooter();
        symbolTable.printScopedTable();
        // finish walk
        fileWriter.writeToFile();
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
        // get unique ID
        int ID = GetID();
        String functionName = (String) node.data.get("Value");
        System.out.println("Function Name: " + functionName);
        int lineOfDecl = (int) node.data.get("LineNo");
        // Print information about node
        printNode(ID, "ASTFunctionDeclaration");
        // add function and label to symbol table
        symbolTable.addEntry(functionName, "FUNCTION", 0, lineOfDecl, functionName, true);

        // Insert label for procedure
        fileWriter.addCode(symbolTable.getEntry(functionName).Label + ":\r\n", "ASTFunctionDeclaration");
        // increase scope
        symbolTable = new scopedSymbolTable(symbolTable);
        // Iterate through children nodes
        node.childrenAccept(this, data);
        if (!functionName.equals("main")) {
            fileWriter.addCode("goto *P[0];\r\n", "ASTFunctionDeclaration");
        } else {
            fileWriter.addCode("return 0;\r\n", "ASTFunctionDeclaration");
        }
        // decrease scope
        symbolTable = symbolTable.parent;
        // Return to parent node (or move to sibling node if exists)
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
        // get unique ID
        int ID = GetID();
        String procedureName = (String) node.data.get("Value");
        int lineOfDecl = (int) node.data.get("LineNo");
        // Print information about node jjtThis.data.put("Value", t.image);
        printNode(ID, "ASTProcedureDeclaration");
        // add function and label to symbol table
        symbolTable.addEntry(procedureName, "PROCEDURE", 0, lineOfDecl, procedureName, true);

        // Insert label for procedure
        fileWriter.addCode(symbolTable.getEntry(procedureName).Label + ":\r\n", "ASTProcedureDeclaration");
        // increase scope
        symbolTable = new scopedSymbolTable(symbolTable);
        // Iterate through children nodes
        node.childrenAccept(this, data);
        fileWriter.addCode("goto *P[0];\r\n", "ASTProcedureDeclaration");
        // decrease scope
        symbolTable = symbolTable.parent;
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
        // get unique ID
        int ID = GetID();
        ASTParameter cnode;
        // Print information about node
        printNode(ID, "ASTParameterList");
        for (int i = 0; i < node.jjtGetNumChildren(); i++) {
            cnode = (ASTParameter) node.jjtGetChild(i);
            visitParameter(cnode, i + 1);
        }
        // Iterate through children nodes
        node.childrenAccept(this, data);
        // Return to parent node (or move to sibling node if exists)
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
        // get unique ID
        int ID = GetID();
        // Print information about nodes
        printNode(ID, "ASTParameter");
        // Iterate through children nodes
        node.childrenAccept(this, data);
        // Return to parent node (or move to sibling node if exists)
        return null;
    }

    public VisitReturn visitParameter(ASTParameter node, int parameterNumber) {
        ASTType cnode = (ASTType) node.jjtGetChild(0);
        ASTVariableDeclaratorId cnode2 = (ASTVariableDeclaratorId) node.jjtGetChild(1);

        // get the name of the variable
        String Type = (String) cnode.data.get("Type");
        String VarID = (String) cnode2.data.get("Value");
        int lineOfDecl = (int) cnode2.data.get("LineNo");
        System.out.println("VarID: " + VarID);

        // add to symbol table
        symbolTable.addEntry(VarID, Type, lineOfDecl, lineOfDecl, VarID, false);
        fileWriter.addComment(
                "Variable Declaration: " + VarID + " at mem location " + symbolTable.getEntry(VarID).MemoryLocation,
                "ASTLocalVariableDeclaration");
        if (Type.equals("INTEGER")) {
            fileWriter.addCode("Mem[" + symbolTable.getEntry(VarID).MemoryLocation + "] = P[" + parameterNumber + "];",
                    "ASTParameter");
        } else if (Type.equals("DOUBLE")) {
            fileWriter.addCode("FMem[" + symbolTable.getEntry(VarID).MemoryLocation + "] = P[" + parameterNumber + "];",
                    "ASTParameter");
        } else {
            throw new RuntimeException("Error: Type " + Type + " is not supported for parameters");
        }
        fileWriter.addCode("", "ASTParameter");
        return new VisitReturn(symbolTable.getEntry(VarID).MemoryLocation, Type);
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
        // get unique ID
        int ID = GetID();
        // Print information about node
        printNode(ID, "ASTVariableDeclarator");
        // Iterate through children nodes
        node.childrenAccept(this, data);
        // Return to parent node (or move to sibling node if exists)
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
        // get unique ID
        int ID = GetID();
        // Print information about node
        printNode(ID, "ASTVariableDeclaratorId");
        // Iterate through children nodes
        node.childrenAccept(this, data);
        // Return to parent node (or move to sibling node if exists)
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
        // get unique ID
        int ID = GetID();
        // Print information about node
        printNode(ID, "ASTType");
        // Iterate through children nodes
        node.childrenAccept(this, data);
        // Return to parent node (or move to sibling node if exists)
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
        // get unique ID
        int ID = GetID();
        // Print information about node
        printNode(ID, "ASTStatement");
        // Iterate through children nodes
        node.childrenAccept(this, data);
        // Return to parent node (or move to sibling node if exists)
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
        // get unique ID
        int ID = GetID();
        // Print information about node
        printNode(ID, "ASTBlock");
        // Iterate through children nodes
        node.childrenAccept(this, data);
        // Return to parent node (or move to sibling node if exists)
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
        // get unique ID
        int ID = GetID();
        // Print information about node
        printNode(ID, "ASTBlockStatement");
        // Iterate through children nodes
        node.childrenAccept(this, data);
        // Return to parent node (or move to sibling node if exists)
        return null;
    }

    /**
     * Description:
     * - Root for Variable Declaration
     * 
     * * Stored Data:
     * - No stored Data
     * 
     * * Children:
     * - Child #    Required/Optional    Type
     * --------------------------------------------------------
     * - 1         Required              ASTType
     * - 2         Required              ASTVariableDeclaratorId
     * - 3         Optional              ASTVariableDeclarator
     */
    public Object visit(ASTLocalVariableDeclaration node, Object data) {
        int ID = GetID();
        // Print information about node
        printNode(ID, "ASTLocalVariableDeclaration");
        visitLocalVariableDeclaration(node);
        node.childrenAccept(this, data);
        // Return to parent node (or move to sibling node if exists)
        return null;
    }

    public VisitReturn visitLocalVariableDeclaration(ASTLocalVariableDeclaration node) {
        String Type;
        ASTType cnode = (ASTType) node.jjtGetChild(0);
        Type = (String) cnode.data.get("Type");

        // get the name of the variable
        ASTVariableDeclarator cnode2 = (ASTVariableDeclarator) node.jjtGetChild(1);
        ASTVariableDeclaratorId cnode3 = (ASTVariableDeclaratorId) cnode2.jjtGetChild(0);
        String VarID = (String) cnode3.data.get("Value");
        int lineOfDecl = (int) cnode3.data.get("LineNo");
        System.out.println("VarID: " + VarID);

        // add to symbol table
        symbolTable.addEntry(VarID, Type, lineOfDecl, lineOfDecl, VarID, false);
        fileWriter.addComment(
                "Variable Declaration: " + VarID + " at mem location " + symbolTable.getEntry(VarID).MemoryLocation,
                "ASTLocalVariableDeclaration");
        return new VisitReturn(symbolTable.getEntry(VarID).MemoryLocation, Type);
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
        // get unique ID
        int ID = GetID();
        // Print information about node
        printNode(ID, "ASTStatementExpression");
        // Iterate through children nodes
        visitStatementExpression(node);
        node.childrenAccept(this, data);
        // Return to parent node (or move to sibling node if exists)
        return null;
    }

    public VisitReturn visitStatementExpression(ASTStatementExpression node) {
        ASTPrimaryExpression cnode;
        ASTAssignmentOperator cnode1;
        ASTExpression cnode2;
        VisitReturn returnData = new VisitReturn(), returnData1 = new VisitReturn();
        String assign = "", registerString1 = "", registerString2 = "";
        int register1 = -1, register2 = -1;

        cnode = (ASTPrimaryExpression) node.jjtGetChild(0);
        returnData = visitPrimaryExpression(cnode);

        if (node.jjtGetNumChildren() == 1 && node.data.get("CREMENT") == null) {
            return returnData;
        } else if (node.jjtGetNumChildren() == 1 && node.data.get("CREMENT") != null) {
            if (returnData1.type == "INTEGER") {
                register1 = memoryHelper.requestIntRegister();
                registerString1 = "R[" + register1 + "]";
            } else {
                throw new RuntimeException(
                        "Error: Type " + returnData.type + " is not supported for assignment expression");
            }
            fileWriter.loadRegister(register1, returnData.memoryLocation, returnData.type, "ASTStatementExpression");
            if (node.data.get("CREMENT").toString() == "INCREMENT") {
                fileWriter.addCode(registerString1 + "++;", "ASTStatementExpression");
            } else {
                fileWriter.addCode(registerString1 + "--;", "ASTStatementExpression");
            }
            fileWriter.storeRegister(register1, returnData, "ASTStatementExpression");
            memoryHelper.returnIntRegister(register1);
            return returnData;

        } else {
            cnode1 = (ASTAssignmentOperator) node.jjtGetChild(1);
            cnode2 = (ASTExpression) node.jjtGetChild(2);
            assign = visitAssignmentOperator(cnode1);
            returnData1 = visitExpression(cnode2);

            if (returnData.type == "DOUBLE") {
                register1 = memoryHelper.requestFloatRegister();
                registerString1 = "F[" + register1 + "]";
            } else if (returnData.type == "INTEGER") {
                register1 = memoryHelper.requestIntRegister();
                registerString1 = "R[" + register1 + "]";
            } else if (returnData.type == "STRING") {
                if (assign.equals("=")) {
                    register1 = memoryHelper.requestVariable(returnData.type);
                    String MemLoc1 = "SMem[" + returnData1.memoryLocation + "]";
                    fileWriter.LoadStrToSMem(returnData.memoryLocation, MemLoc1, "ASTStatementExpression");
                    memoryHelper.returnVariable(returnData1.memoryLocation, returnData1.type);
                    return returnData;
                } else {
                    throw new RuntimeException(
                            "Error: Type " + returnData.type + " is not supported for the " + assign
                                    + " assignment expression");
                }
            } else {
                throw new RuntimeException(
                        "Error: Type " + returnData.type + " is not supported for assignment expression");
            }
            if (returnData1.type == "DOUBLE") {
                register2 = memoryHelper.requestFloatRegister();
                registerString2 = "F[" + register2 + "]";
            } else if (returnData1.type == "INTEGER") {
                register2 = memoryHelper.requestIntRegister();
                registerString2 = "R[" + register2 + "]";
            } else {
                throw new RuntimeException(
                        "Error: Type " + returnData1.type + " is not supported for assignment expression");
            }
            fileWriter.loadRegister(register1, returnData.memoryLocation, returnData.type, "ASTStatementExpression");
            fileWriter.loadRegister(register2, returnData1.memoryLocation, returnData1.type, "ASTStatementExpression");
            fileWriter.addCode(registerString1 + assign + registerString2 + ";\r\n", "ASTStatementExpression");
            fileWriter.storeRegister(register1, returnData, "ASTStatementExpression");
            if (returnData.type == "DOUBLE") {
                memoryHelper.returnFloatRegister(register1);
            } else if (returnData.type == "INTEGER") {
                memoryHelper.returnIntRegister(register1);
            }
            if (returnData1.type == "DOUBLE") {
                memoryHelper.returnFloatRegister(register2);
            } else if (returnData1.type == "INTEGER") {
                memoryHelper.returnIntRegister(register2);
            }

            memoryHelper.returnVariable(returnData1.memoryLocation, returnData1.type);
            return returnData;

        }
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
        // get unique ID
        int ID = GetID();
        // Print information about node
        printNode(ID, "ASTIfStatement");
        // Iterate through children nodes
        node.childrenAccept(this, data);
        // Return to parent node (or move to sibling node if exists)
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
        // get unique ID
        int ID = GetID();
        // Print information about node
        printNode(ID, "ASTWhileStatement");
        // Iterate through children nodes
        node.childrenAccept(this, data);
        // Return to parent node (or move to sibling node if exists)
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
        // get unique ID
        int ID = GetID();
        // Print information about node
        printNode(ID, "ASTDoStatement");
        // Iterate through children nodes
        node.childrenAccept(this, data);
        // Return to parent node (or move to sibling node if exists)
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
        // get unique ID
        int ID = GetID();
        // Print information about node
        printNode(ID, "ASTDoInit");
        // Iterate through children nodes
        node.childrenAccept(this, data);
        // Return to parent node (or move to sibling node if exists)
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
        // get unique ID
        int ID = GetID();
        // Print information about node
        printNode(ID, "ASTStatementExpressionList");
        // Iterate through children nodes
        node.childrenAccept(this, data);
        // Return to parent node (or move to sibling node if exists)
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
        // get unique ID
        int ID = GetID();
        // Print information about node
        printNode(ID, "ASTReturnStatement");
        // Iterate through children nodes
        node.childrenAccept(this, data);
        // Return to parent node (or move to sibling node if exists)
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
        // get unique ID
        int ID = GetID();
        // Print information about node
        printNode(ID, "ASTReturnStatement");
        // Iterate through children nodes
        node.childrenAccept(this, data);
        // Return to parent node (or move to sibling node if exists)
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
        // get unique ID
        int ID = GetID();
        // Print information about node
        printNode(ID, "ASTPrintIntStatement");
        // get the value to print
        ASTExpression cnode = (ASTExpression) node.jjtGetChild(0);
        VisitReturn returnData = visitExpression(cnode);
        fileWriter.addCode("print_int( Mem[SR+" + returnData.memoryLocation + "] );\r\n", "ASTPrintIntStatement");
        // Iterate through children nodes
        node.childrenAccept(this, data);
        // Return to parent node (or move to sibling node if exists)
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
        // get unique ID
        int ID = GetID();
        // Print information about node
        printNode(ID, "ASTPrintDoubleStatement");
        // get the value to print
        ASTExpression cnode = (ASTExpression) node.jjtGetChild(0);
        VisitReturn returnData = visitExpression(cnode);
        fileWriter.addCode("print_double( FMem[FR+" + returnData.memoryLocation + "] );\r\n",
                "ASTPrintDoubleStatement");
        // Iterate through children nodes
        node.childrenAccept(this, data);
        // Return to parent node (or move to sibling node if exists)
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
        // get unique ID
        int ID = GetID();
        // Print information about node
        printNode(ID, "ASTPrintStringStatement");

        ASTExpression cnode = (ASTExpression) node.jjtGetChild(0);
        VisitReturn returnData = visitExpression(cnode);
        fileWriter.addCode("print_string( SMem[" + returnData.memoryLocation + "] );\r\n", "ASTPrintStringStatement");

        // Iterate through children nodes
        node.childrenAccept(this, data);
        // Return to parent node (or move to sibling node if exists)
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
        // get unique ID
        int ID = GetID();
        // Print information about node
        printNode(ID, "ASTReadStatement");
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
        // get unique ID
        int ID = GetID();
        // Print information about node
        printNode(ID, "ASTReadIntStatement");
        // Iterate through children nodes
        node.childrenAccept(this, data);
        // Return to parent node (or move to sibling node if exists)
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
        // get unique ID
        int ID = GetID();
        // Print information about node
        printNode(ID, "ASTReadDoubleStatement");
        // Iterate through children nodes
        node.childrenAccept(this, data);
        // Return to parent node (or move to sibling node if exists)
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
        // get unique ID
        int ID = GetID();
        // Print information about node
        printNode(ID, "ASTReadStringStatement");
        // Iterate through children nodes
        node.childrenAccept(this, data);
        // Return to parent node (or move to sibling node if exists)
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
        // get unique ID
        int ID = GetID();
        // Print information about node
        printNode(ID, "ASTExpression");
        // Iterate through children nodes
        node.childrenAccept(this, data);
        // Return to parent node (or move to sibling node if exists)
        return null;
    }

    public VisitReturn visitExpression(ASTExpression node) {
        ASTConditionalOrExpression cnode;
        ASTAssignmentOperator cnode1;
        ASTExpression cnode2;
        String assign, registerString1, registerString2;
        VisitReturn returnData1, returnData2;
        int register1, register2;

        if (node.jjtGetNumChildren() == 1) {
            cnode = (ASTConditionalOrExpression) node.jjtGetChild(0);
            return visitConditionalOrExpression(cnode);
        } else {
            cnode = (ASTConditionalOrExpression) node.jjtGetChild(0);
            cnode1 = (ASTAssignmentOperator) node.jjtGetChild(1);
            cnode2 = (ASTExpression) node.jjtGetChild(2);
            returnData1 = visitConditionalOrExpression(cnode);
            returnData2 = visitExpression(cnode2);
            assign = visitAssignmentOperator(cnode1);

            if (returnData1.type == "DOUBLE") {
                register1 = memoryHelper.requestFloatRegister();
                registerString1 = "F[" + register1 + "]";
            } else if (returnData1.type == "INTEGER") {
                register1 = memoryHelper.requestIntRegister();
                registerString1 = "R[" + register1 + "]";
            } else {
                throw new RuntimeException(
                        "Error: Type " + returnData1.type + " is not supported for assignment expression");
            }
            if (returnData2.type == "DOUBLE") {
                register2 = memoryHelper.requestFloatRegister();
                registerString2 = "F[" + register2 + "]";
            } else if (returnData2.type == "INTEGER") {
                register2 = memoryHelper.requestIntRegister();
                registerString2 = "R[" + register2 + "]";
            } else {
                throw new RuntimeException(
                        "Error: Type " + returnData2.type + " is not supported for assignment expression");
            }

            fileWriter.addCode(registerString1 + assign + registerString2 + ";", "ASTExpression");
            fileWriter.storeRegister(register1, returnData1, "ASTExpression");
            if (returnData1.type == "DOUBLE") {
                memoryHelper.returnFloatRegister(register1);
            } else if (returnData1.type == "INTEGER") {
                memoryHelper.returnIntRegister(register1);
            }
            if (returnData2.type == "DOUBLE") {
                memoryHelper.returnFloatRegister(register2);
            } else if (returnData2.type == "INTEGER") {
                memoryHelper.returnIntRegister(register2);
            }
            memoryHelper.returnVariable(returnData2.memoryLocation, returnData2.type);
            return returnData1;
        }

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
        // get unique ID
        int ID = GetID();
        // Print information about node
        printNode(ID, "ASTAssignmentOperator");
        // Iterate through children nodes
        node.childrenAccept(this, data);
        // Return to parent node (or move to sibling node if exists)
        return null;
    }

    public String visitAssignmentOperator(ASTAssignmentOperator node) {
        String assign = "";
        switch (node.data.get("Assignment").toString()) {
            case "ASSIGN":
                assign = "=";
                break;
            case "DIVIDE":
                assign = "/=";
                break;
            case "MINUS":
                assign = "-=";
                break;
            case "MOD":
                assign = "%=";
                break;
            case "MULTIPLY":
                assign = "*=";
                break;
            case "PLUS":
                assign = "+=";
                break;
        }
        return assign;
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
        // get unique ID
        int ID = GetID();
        // Print information about node
        printNode(ID, "ASTConditionalOrExpression");
        // Iterate through children nodes
        node.childrenAccept(this, data);
        // Return to parent node (or move to sibling node if exists)
        return null;
    }

    public VisitReturn visitConditionalOrExpression(ASTConditionalOrExpression node) {
        // declare holder variables
        ASTConditionalAndExpression cnode1, cnode2;
        VisitReturn returnData = new VisitReturn(), returnData1 = new VisitReturn(), returnData2 = new VisitReturn();
        int register1 = -1, register2 = -1, register3 = -1;
        String registerString1 = "", registerString2 = "";

        if (node.jjtGetNumChildren() == 1) {
            ASTConditionalAndExpression cnode = (ASTConditionalAndExpression) node.jjtGetChild(0);
            return visitConditionalAndExpression(cnode);
        } else {
            returnData.type = "BOOLEAN";
            returnData.memoryLocation = memoryHelper.requestVariable("BOOLEAN");
            // iterate through pairs of children
            for (int i = 0; i < node.jjtGetNumChildren() - 1; i++) {
                if (i == 0) {
                    // first pass get both children
                    cnode1 = (ASTConditionalAndExpression) node.jjtGetChild(i);
                    cnode2 = (ASTConditionalAndExpression) node.jjtGetChild(i + 1);
                    // load variables
                    returnData1 = visitConditionalAndExpression(cnode1);
                    returnData2 = visitConditionalAndExpression(cnode2);
                } else {
                    // prep next node for multiplication
                    cnode2 = (ASTConditionalAndExpression) node.jjtGetChild(i + 1);
                    returnData1 = returnData;
                    returnData2 = visitConditionalAndExpression(cnode2);
                }

                if (returnData1.type == "DOUBLE") {
                    register1 = memoryHelper.requestFloatRegister();
                    registerString1 = "F[" + register1 + "]";
                } else if (returnData1.type == "INTEGER") {
                    register1 = memoryHelper.requestIntRegister();
                    registerString1 = "R[" + register1 + "]";
                } else if (returnData1.type == "BOOLEAN" && returnData2.type == "BOOLEAN") {
                    register1 = memoryHelper.requestIntRegister();
                    registerString1 = "R[" + register1 + "]";
                } else {
                    throw new RuntimeException(
                            "Error: Type " + returnData1.type + " is not supported for equality expression");
                }
                if (returnData2.type == "DOUBLE") {
                    register2 = memoryHelper.requestFloatRegister();
                    registerString2 = "F[" + register2 + "]";
                } else if (returnData2.type == "INTEGER") {
                    register2 = memoryHelper.requestIntRegister();
                    registerString2 = "R[" + register2 + "]";
                } else if (returnData1.type == "BOOLEAN" && returnData2.type == "BOOLEAN") {
                    register2 = memoryHelper.requestIntRegister();
                    registerString2 = "R[" + register2 + "]";
                } else {
                    throw new RuntimeException(
                            "Error: Type " + returnData2.type + " is not supported for equality expression");
                }

                register3 = memoryHelper.requestIntRegister();
                fileWriter.loadRegister(register1, returnData1.memoryLocation, returnData1.type,
                        "ASTConditionalOrExpression");
                fileWriter.loadRegister(register2, returnData2.memoryLocation, returnData2.type,
                        "ASTConditionalOrExpression");
                fileWriter.addCode("R[" + register3 + "] = " + registerString1 + " | " + registerString2 + ";\r\n",
                        "ASTConditionalOrExpression");
                fileWriter.storeRegister(register3, returnData, "ASTConditionalOrExpression");
                if (returnData1.type == "DOUBLE") {
                    memoryHelper.returnFloatRegister(register2);
                } else if (returnData1.type == "INTEGER") {
                    memoryHelper.returnIntRegister(register2);
                }
                if (returnData2.type == "DOUBLE") {
                    memoryHelper.returnFloatRegister(register2);
                } else if (returnData2.type == "INTEGER") {
                    memoryHelper.returnIntRegister(register2);
                }
                memoryHelper.returnIntRegister(register3);
                if (i == 0) {
                    memoryHelper.returnVariable(returnData1.memoryLocation, returnData1.type);
                    memoryHelper.returnVariable(returnData2.memoryLocation, returnData2.type);
                } else {
                    memoryHelper.returnVariable(returnData2.memoryLocation, returnData2.type);
                }
            }
            return returnData;
        }
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
        // get unique ID
        int ID = GetID();
        // Print information about node
        printNode(ID, "ASTConditionalAndExpression");
        // Iterate through children nodes
        node.childrenAccept(this, data);
        // Return to parent node (or move to sibling node if exists)
        return null;
    }

    public VisitReturn visitConditionalAndExpression(ASTConditionalAndExpression node) {
        // declare holder variables
        ASTEqualityExpression cnode1, cnode2;
        VisitReturn returnData = new VisitReturn(), returnData1 = new VisitReturn(), returnData2 = new VisitReturn();
        int register1 = -1, register2 = -1, register3 = -1;
        String registerString1 = "", registerString2 = "";

        if (node.jjtGetNumChildren() == 1) {
            ASTEqualityExpression cnode = (ASTEqualityExpression) node.jjtGetChild(0);
            return visitEqualityExpression(cnode);
        } else {
            returnData.type = "BOOLEAN";
            returnData.memoryLocation = memoryHelper.requestVariable("BOOLEAN");
            // iterate through pairs of children
            for (int i = 0; i < node.jjtGetNumChildren() - 1; i++) {
                if (i == 0) {
                    // first pass get both children
                    cnode1 = (ASTEqualityExpression) node.jjtGetChild(i);
                    cnode2 = (ASTEqualityExpression) node.jjtGetChild(i + 1);
                    // load variables
                    returnData1 = visitEqualityExpression(cnode1);
                    returnData2 = visitEqualityExpression(cnode2);
                } else {
                    // prep next node for multiplication
                    cnode2 = (ASTEqualityExpression) node.jjtGetChild(i + 1);
                    returnData1 = returnData;
                    returnData2 = visitEqualityExpression(cnode2);
                }

                if (returnData1.type == "DOUBLE") {
                    register1 = memoryHelper.requestFloatRegister();
                    registerString1 = "F[" + register1 + "]";
                } else if (returnData1.type == "INTEGER") {
                    register1 = memoryHelper.requestIntRegister();
                    registerString1 = "R[" + register1 + "]";
                } else if (returnData1.type == "BOOLEAN" && returnData2.type == "BOOLEAN") {
                    register1 = memoryHelper.requestIntRegister();
                    registerString1 = "R[" + register1 + "]";
                } else {
                    throw new RuntimeException(
                            "Error: Type " + returnData1.type + " is not supported for equality expression");
                }
                if (returnData2.type == "DOUBLE") {
                    register2 = memoryHelper.requestFloatRegister();
                    registerString2 = "F[" + register2 + "]";
                } else if (returnData2.type == "INTEGER") {
                    register2 = memoryHelper.requestIntRegister();
                    registerString2 = "R[" + register2 + "]";
                } else if (returnData1.type == "BOOLEAN" && returnData2.type == "BOOLEAN") {
                    register2 = memoryHelper.requestIntRegister();
                    registerString2 = "R[" + register2 + "]";
                } else {
                    throw new RuntimeException(
                            "Error: Type " + returnData2.type + " is not supported for equality expression");
                }

                register3 = memoryHelper.requestIntRegister();
                fileWriter.loadRegister(register1, returnData1.memoryLocation, returnData1.type,
                        "ASTConditionalAndExpression");
                fileWriter.loadRegister(register2, returnData2.memoryLocation, returnData2.type,
                        "ASTConditionalAndExpression");
                fileWriter.addCode("R[" + register3 + "] = " + registerString1 + " & " + registerString2 + ";\r\n",
                        "ASTConditionalAndExpression");
                fileWriter.storeRegister(register3, returnData, "ASTConditionalAndExpression");
                if (returnData1.type == "DOUBLE") {
                    memoryHelper.returnFloatRegister(register1);
                } else if (returnData1.type == "INTEGER") {
                    memoryHelper.returnIntRegister(register1);
                }
                if (returnData2.type == "DOUBLE") {
                    memoryHelper.returnFloatRegister(register2);
                } else if (returnData2.type == "INTEGER") {
                    memoryHelper.returnIntRegister(register2);
                }
                memoryHelper.returnIntRegister(register3);
                if (i == 0) {
                    memoryHelper.returnVariable(returnData1.memoryLocation, returnData1.type);
                    memoryHelper.returnVariable(returnData2.memoryLocation, returnData2.type);
                } else {
                    memoryHelper.returnVariable(returnData2.memoryLocation, returnData2.type);
                }
            }
            return returnData;
        }
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
     * 
    
     * - 2+         Optional             ASTRelationalExpression
     */
    public Object visit(ASTEqualityExpression node, Object data) {
        // get unique ID
        int ID = GetID();
        // Print information about node
        printNode(ID, "ASTEqualityExpression");
        // Iterate through children nodes
        node.childrenAccept(this, data);
        // Return to parent node (or move to sibling node if exists)
        return null;
    }

    public VisitReturn visitEqualityExpression(ASTEqualityExpression node) {
        // declare holder variables
        VisitReturn returnData = new VisitReturn();
        VisitReturn returnData1 = new VisitReturn();
        VisitReturn returnData2 = new VisitReturn();
        int register1 = -1, register2 = -1, register3 = -1;
        String equality = "", registerString1 = "", registerString2 = "";

        if (node.jjtGetNumChildren() == 1) {
            ASTRelationalExpression cnode = (ASTRelationalExpression) node.jjtGetChild(0);
            return visitRelationalExpression(cnode);
        } else {
            returnData.type = "BOOLEAN";
            returnData.memoryLocation = memoryHelper.requestVariable("BOOLEAN");
            // iterate through pairs of children
            for (int i = 0; i < node.jjtGetNumChildren() - 1; i++) {
                equality = node.data.get(i + 1).toString();
                if (i == 0) {
                    // first pass get both children
                    ASTRelationalExpression cnode1 = (ASTRelationalExpression) node.jjtGetChild(i);
                    ASTRelationalExpression cnode2 = (ASTRelationalExpression) node.jjtGetChild(i + 1);
                    // load variables
                    returnData1 = visitRelationalExpression((ASTRelationalExpression) cnode1.jjtGetChild(i));
                    returnData2 = visitRelationalExpression((ASTRelationalExpression) cnode2.jjtGetChild(i + 1));
                } else {
                    // prep next node for comparison
                    ASTRelationalExpression cnode2 = (ASTRelationalExpression) node.jjtGetChild(i + 1);
                    returnData1 = returnData;
                    returnData2 = visitRelationalExpression(cnode2);
                }

                if (returnData1.type == "DOUBLE") {
                    register1 = memoryHelper.requestFloatRegister();
                    registerString1 = "F[" + register1 + "]";
                } else if (returnData1.type == "INTEGER") {
                    register1 = memoryHelper.requestIntRegister();
                    registerString1 = "R[" + register1 + "]";
                } else if (returnData1.type == "BOOLEAN" && returnData2.type == "BOOLEAN") {
                    register1 = memoryHelper.requestIntRegister();
                    registerString1 = "R[" + register1 + "]";
                } else {
                    throw new RuntimeException(
                            "Error: Type " + returnData1.type + " is not supported for equality expression");
                }
                if (returnData2.type == "DOUBLE") {
                    register2 = memoryHelper.requestFloatRegister();
                    registerString2 = "F[" + register2 + "]";
                } else if (returnData2.type == "INTEGER") {
                    register2 = memoryHelper.requestIntRegister();
                    registerString2 = "R[" + register2 + "]";
                } else if (returnData1.type == "BOOLEAN" && returnData2.type == "BOOLEAN") {
                    register2 = memoryHelper.requestIntRegister();
                    registerString2 = "R[" + register2 + "]";
                } else {
                    throw new RuntimeException(
                            "Error: Type " + returnData2.type + " is not supported for equality expression");
                }

                register3 = memoryHelper.requestIntRegister();
                fileWriter.loadRegister(register1, returnData1.memoryLocation, returnData1.type,
                        "ASTEqualityExpression");
                fileWriter.loadRegister(register2, returnData2.memoryLocation, returnData2.type,
                        "ASTEqualityExpression");
                if (equality.equals("DEQ")) {
                    fileWriter
                            .addCode("R[" + register3 + "] = " + registerString1 + " == " + registerString2 + ";\r\n",
                                    "ASTEqualityExpression");
                } else if (equality.equals("NE")) {
                    fileWriter
                            .addCode("R[" + register3 + "] = " + registerString1 + " != " + registerString2 + ";\r\n",
                                    "ASTEqualityExpression");
                } else {
                    throw new RuntimeException("Error: Equality operator " + equality + " is not supported");
                }
                fileWriter.storeRegister(register3, returnData, "ASTEqualityExpression");
                if (returnData1.type == "DOUBLE") {
                    memoryHelper.returnFloatRegister(register1);
                } else if (returnData1.type == "INTEGER") {
                    memoryHelper.returnIntRegister(register1);
                }
                if (returnData2.type == "DOUBLE") {
                    memoryHelper.returnFloatRegister(register2);
                } else if (returnData2.type == "INTEGER") {
                    memoryHelper.returnIntRegister(register2);
                }
                memoryHelper.returnIntRegister(register3);
                if (i == 0) {
                    memoryHelper.returnVariable(returnData1.memoryLocation, returnData1.type);
                    memoryHelper.returnVariable(returnData2.memoryLocation, returnData2.type);
                } else {
                    memoryHelper.returnVariable(returnData2.memoryLocation, returnData2.type);
                }
            }
            return returnData;
        }
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
        // get unique ID
        int ID = GetID();
        // Print information about node
        printNode(ID, "ASTRelationalExpression");
        // Iterate through children nodes
        node.childrenAccept(this, data);
        // Return to parent node (or move to sibling node if exists)
        return null;
    }

    public VisitReturn visitRelationalExpression(ASTRelationalExpression node) {
        // declare holder variables
        VisitReturn returnData = new VisitReturn();
        VisitReturn returnData1 = new VisitReturn();
        VisitReturn returnData2 = new VisitReturn();
        int register1 = -1, register2 = -1, register3 = -1;
        String relational = "", registerString1 = "", registerString2 = "";

        if (node.jjtGetNumChildren() == 1) {
            return visitAdditiveExpression((ASTAdditiveExpression) node.jjtGetChild(0));
        } else {
            returnData.type = "BOOLEAN";
            returnData.memoryLocation = memoryHelper.requestVariable("BOOLEAN");
            // iterate through pairs of children
            for (int i = 0; i < node.jjtGetNumChildren() - 1; i++) {
                relational = node.data.get(i + 1).toString();
                if (i == 0) {
                    // load variables
                    returnData1 = visitAdditiveExpression((ASTAdditiveExpression) node.jjtGetChild(i));
                    returnData2 = visitAdditiveExpression((ASTAdditiveExpression) node.jjtGetChild(i + 1));
                } else {
                    // prep next node for multiplication
                    returnData1 = returnData;
                    returnData2 = visitAdditiveExpression((ASTAdditiveExpression) node.jjtGetChild(i + 1));
                }

                if (returnData1.type == "DOUBLE") {
                    register1 = memoryHelper.requestFloatRegister();
                    registerString1 = "F[" + register1 + "]";
                } else if (returnData1.type == "INTEGER") {
                    register1 = memoryHelper.requestIntRegister();
                    registerString1 = "R[" + register1 + "]";
                } else {
                    throw new RuntimeException(
                            "Error: Type " + returnData1.type + " is not supported for relational expression");
                }
                if (returnData2.type == "DOUBLE") {
                    register2 = memoryHelper.requestFloatRegister();
                    registerString2 = "F[" + register2 + "]";
                } else if (returnData2.type == "INTEGER") {
                    register2 = memoryHelper.requestIntRegister();
                    registerString2 = "R[" + register2 + "]";
                } else {
                    throw new RuntimeException(
                            "Error: Type " + returnData2.type + " is not supported for relational expression");
                }

                register3 = memoryHelper.requestIntRegister();
                fileWriter.loadRegister(register1, returnData1.memoryLocation, returnData1.type,
                        "ASTRelationalExpression");
                fileWriter.loadRegister(register2, returnData2.memoryLocation, returnData2.type,
                        "ASTRelationalExpression");
                if (relational.equals("LT")) {
                    fileWriter.addCode("R[" + register3 + "] = " + registerString1 + " < " + registerString2 + ";\r\n",
                            "ASTRelationalExpression");
                } else if (relational.equals("GT")) {
                    fileWriter.addCode("R[" + register3 + "] = " + registerString1 + " > " + registerString2 + ";\r\n",
                            "ASTRelationalExpression");
                } else if (relational.equals("LEQ")) {
                    fileWriter
                            .addCode("R[" + register3 + "] = " + registerString1 + " <= " + registerString2 + ";\r\n",
                                    "ASTRelationalExpression");
                } else if (relational.equals("GEQ")) {
                    fileWriter
                            .addCode("R[" + register3 + "] = " + registerString1 + " >= " + registerString2 + ";\r\n",
                                    "ASTRelationalExpression");
                }
                fileWriter.storeRegister(register3, returnData, "ASTRelationalExpression");
                if (returnData1.type == "DOUBLE") {
                    memoryHelper.returnFloatRegister(register1);
                } else if (returnData1.type == "INTEGER") {
                    memoryHelper.returnIntRegister(register1);
                }
                if (returnData2.type == "DOUBLE") {
                    memoryHelper.returnFloatRegister(register2);
                } else if (returnData2.type == "INTEGER") {
                    memoryHelper.returnIntRegister(register2);
                }
                memoryHelper.returnIntRegister(register3);
                if (i == 0) {
                    memoryHelper.returnVariable(returnData1.memoryLocation, returnData1.type);
                    memoryHelper.returnVariable(returnData2.memoryLocation, returnData2.type);
                } else {
                    memoryHelper.returnVariable(returnData2.memoryLocation, returnData2.type);
                }
            }
        }
        return returnData;
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
        // get unique ID
        int ID = GetID();
        // Print information about node
        printNode(ID, "ASTAdditiveExpression");
        // Iterate through children nodes
        node.childrenAccept(this, data);
        // Return to parent node (or move to sibling node if exists)
        return null;
    }

    public VisitReturn visitAdditiveExpression(ASTAdditiveExpression node) {
        // declare holder variables
        VisitReturn returnData = new VisitReturn();
        VisitReturn returnData1 = new VisitReturn();
        VisitReturn returnData2 = new VisitReturn();
        int register1 = -1, register2 = -1;
        String additive = "";

        if (node.jjtGetNumChildren() == 1) {
            ASTMultiplicativeExpression cnode = (ASTMultiplicativeExpression) node.jjtGetChild(0);
            return visitMultiplicativeExpression(cnode);
        } else {

            // iterate through pairs of children
            for (int i = 0; i < node.jjtGetNumChildren() - 1; i++) {
                additive = node.data.get(i + 1).toString();
                if (i == 0) {
                    // load variables
                    returnData1 = visitMultiplicativeExpression((ASTMultiplicativeExpression) node.jjtGetChild(i));
                    returnData2 = visitMultiplicativeExpression((ASTMultiplicativeExpression) node.jjtGetChild(i + 1));
                } else {
                    // prep next node for multiplication
                    returnData1 = returnData;
                    returnData2 = visitMultiplicativeExpression((ASTMultiplicativeExpression) node.jjtGetChild(i + 1));
                }
                if (returnData1.type == "INTEGER" && returnData2.type == "INTEGER") {
                    if (i == 0) {
                        returnData.memoryLocation = memoryHelper.requestVariable("INTEGER");
                        returnData.type = "INTEGER";
                    }
                    register1 = memoryHelper.requestIntRegister();
                    register2 = memoryHelper.requestIntRegister();
                    fileWriter.loadRegister(register1, returnData1.memoryLocation, returnData1.type,
                            "ASTAdditiveExpression");
                    fileWriter.loadRegister(register2, returnData2.memoryLocation, returnData2.type,
                            "ASTAdditiveExpression");
                    if (additive.equals("PLUS")) {
                        fileWriter.addCode("R[" + register1 + "] = R[" + register1 + "] + R[" + register2 + "];\r\n",
                                "ASTAdditiveExpression");
                    } else if (additive.equals("MINUS")) {
                        fileWriter.addCode("R[" + register1 + "] = R[" + register1 + "] - R[" + register2 + "];\r\n",
                                "ASTAdditiveExpression");
                    }
                    fileWriter.storeRegister(register1, returnData, "ASTAdditiveExpression");
                    memoryHelper.returnIntRegister(register1);
                    memoryHelper.returnIntRegister(register2);
                } else if ((returnData1.type == "DOUBLE" || returnData1.type == "INTEGER") &&
                        (returnData2.type == "DOUBLE" || returnData2.type == "INTEGER")) {
                    if (i == 0) {
                        returnData.memoryLocation = memoryHelper.requestVariable("DOUBLE");
                        returnData.type = "DOUBLE";
                    } else if (returnData.type == "INTEGER") {
                        // convert to double
                        register1 = memoryHelper.requestIntRegister();
                        register2 = memoryHelper.requestFloatRegister();
                        fileWriter.loadRegister(register1, returnData.memoryLocation, returnData.type,
                                "ASTAdditiveExpression");
                        fileWriter.addCode("F[" + register2 + "] = (double) R[" + register1 + "];\r\n",
                                "ASTAdditiveExpression");
                        memoryHelper.returnVariable(returnData.memoryLocation, returnData.type);
                        returnData.type = "DOUBLE";
                        returnData.memoryLocation = memoryHelper.requestVariable("DOUBLE");
                        fileWriter.storeRegister(register2, returnData, "ASTAdditiveExpression");
                        memoryHelper.returnIntRegister(register1);
                        memoryHelper.returnFloatRegister(register2);
                    }
                    if (returnData1.type == "INTEGER") {
                        // convert to double
                        register1 = memoryHelper.requestIntRegister();
                        register2 = memoryHelper.requestFloatRegister();
                        fileWriter.loadRegister(register1, returnData1.memoryLocation, returnData1.type,
                                "ASTAdditiveExpression");
                        fileWriter.addCode("F[" + register2 + "] = (double) R[" + register1 + "];\r\n",
                                "ASTAdditiveExpression");
                        memoryHelper.returnVariable(returnData1.memoryLocation, returnData1.type);
                        returnData1.type = "DOUBLE";
                        returnData1.memoryLocation = memoryHelper.requestVariable("DOUBLE");
                        fileWriter.storeRegister(register2, returnData1, "ASTAdditiveExpression");
                        memoryHelper.returnIntRegister(register1);
                        memoryHelper.returnFloatRegister(register2);
                    }
                    if (returnData2.type == "INTEGER") {
                        // convert to double
                        register1 = memoryHelper.requestIntRegister();
                        register2 = memoryHelper.requestFloatRegister();
                        fileWriter.loadRegister(register1, returnData2.memoryLocation, returnData2.type,
                                "ASTAdditiveExpression");
                        fileWriter.addCode("F[" + register2 + "] = (double) R[" + register1 + "];\r\n",
                                "ASTAdditiveExpression");
                        memoryHelper.returnVariable(returnData2.memoryLocation, returnData2.type);
                        returnData2.type = "DOUBLE";
                        returnData2.memoryLocation = memoryHelper.requestVariable("DOUBLE");
                        fileWriter.storeRegister(register2, returnData2, "ASTAdditiveExpression");
                        memoryHelper.returnIntRegister(register1);
                        memoryHelper.returnFloatRegister(register2);
                    }
                    register1 = memoryHelper.requestFloatRegister();
                    register2 = memoryHelper.requestFloatRegister();
                    fileWriter.loadRegister(register1, returnData1.memoryLocation, returnData1.type,
                            "ASTAdditiveExpression");
                    fileWriter.loadRegister(register2, returnData2.memoryLocation, returnData2.type,
                            "ASTAdditiveExpression");
                    if (additive.equals("PLUS")) {
                        fileWriter.addCode("F[" + register1 + "] = F[" + register1 + "] + F[" + register2 + "];\r\n",
                                "ASTAdditiveExpression");
                    } else if (additive.equals("MINUS")) {
                        fileWriter.addCode("F[" + register1 + "] = F[" + register1 + "] - F[" + register2 + "];\r\n",
                                "ASTAdditiveExpression");
                    }
                    fileWriter.storeRegister(register1, returnData, "ASTAdditiveExpression");
                    memoryHelper.returnFloatRegister(register1);
                    memoryHelper.returnFloatRegister(register2);
                } else {
                    throw new RuntimeException("Error: Illegal use of additive operator on non-integer type.");
                }

                // release temp variables
                if (i == 0) {
                    memoryHelper.returnVariable(returnData1.memoryLocation, returnData1.type);
                    memoryHelper.returnVariable(returnData2.memoryLocation, returnData2.type);
                } else {
                    // second pass returnData1 == returnData
                    memoryHelper.returnVariable(returnData2.memoryLocation, returnData2.type);
                }
            }
        }
        return returnData;
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
        // get unique ID
        int ID = GetID();
        // Print information about node
        printNode(ID, "ASTMultiplicativeExpression");
        // Iterate through children nodes
        node.childrenAccept(this, data);
        // Return to parent node (or move to sibling node if exists)
        return null;
    }

    public VisitReturn visitMultiplicativeExpression(ASTMultiplicativeExpression node) {
        // declare holder variables
        VisitReturn returnData = new VisitReturn();
        VisitReturn returnData1 = new VisitReturn();
        VisitReturn returnData2 = new VisitReturn();
        int register1 = -1, register2 = -1;
        String multiplicative = "";

        if (node.jjtGetNumChildren() == 1) {
            ASTUnaryExpression cnode = (ASTUnaryExpression) node.jjtGetChild(0);
            return visitUnaryExpression(cnode);
        } else {

            // iterate through pairs of children
            for (int i = 0; i < node.jjtGetNumChildren() - 1; i++) {
                multiplicative = node.data.get(i + 1).toString();
                if (i == 0) {
                    // first pass get both children
                    ASTUnaryExpression cnode1 = (ASTUnaryExpression) node.jjtGetChild(i);
                    ASTUnaryExpression cnode2 = (ASTUnaryExpression) node.jjtGetChild(i + 1);
                    // load variables
                    returnData1 = visitUnaryExpression(cnode1);
                    returnData2 = visitUnaryExpression(cnode2);
                } else {
                    // prep next node for multiplication
                    returnData1 = returnData;
                    returnData2 = visitUnaryExpression((ASTUnaryExpression) node.jjtGetChild(i + 1));
                }
                if (returnData1.type == "INTEGER" && returnData2.type == "INTEGER") {
                    if (i == 0) {
                        returnData.memoryLocation = memoryHelper.requestVariable("INTEGER");
                        returnData.type = "INTEGER";
                    }
                    register1 = memoryHelper.requestIntRegister();
                    fileWriter.addComment("register1: " + register1, "ASTMultiplicativeExpression");
                    register2 = memoryHelper.requestIntRegister();
                    fileWriter.addComment("register2: " + register2, "ASTMultiplicativeExpression");

                    fileWriter.loadRegister(register1, returnData1.memoryLocation, returnData1.type,
                            "ASTMultiplicativeExpression");
                    fileWriter.loadRegister(register2, returnData2.memoryLocation, returnData2.type,
                            "ASTMultiplicativeExpression");
                    if (multiplicative.equals("MULTIPLY")) {
                        fileWriter.addCode("R[" + register1 + "] = R[" + register1 + "] * R[" + register2 + "];\r\n",
                                "ASTMultiplicativeExpression");
                    } else if (multiplicative.equals("DIVIDE")) {
                        fileWriter.addCode("R[" + register1 + "] = R[" + register1 + "] / R[" + register2 + "];\r\n",
                                "ASTMultiplicativeExpression");
                    } else if (multiplicative.equals("MOD")) {
                        fileWriter.addCode("R[" + register1 + "] = R[" + register1 + "] % R[" + register2 + "];\r\n",
                                "ASTMultiplicativeExpression");
                    }
                    fileWriter.storeRegister(register1, returnData, "ASTMultiplicativeExpression");
                    memoryHelper.returnIntRegister(register1);
                    memoryHelper.returnIntRegister(register2);
                } else if ((returnData1.type == "DOUBLE" || returnData1.type == "INTEGER") &&
                        (returnData2.type == "DOUBLE" || returnData2.type == "INTEGER")) {
                    if (i == 0) {
                        returnData.memoryLocation = memoryHelper.requestVariable("DOUBLE");
                        returnData.type = "DOUBLE";
                    } else if (returnData.type == "INTEGER") {
                        // convert to double
                        register1 = memoryHelper.requestIntRegister();
                        register2 = memoryHelper.requestFloatRegister();
                        fileWriter.loadRegister(register1, returnData.memoryLocation, returnData.type,
                                "ASTMultiplicativeExpression");
                        fileWriter.addCode("F[" + register2 + "] = (double) R[" + register1 + "];\r\n",
                                "ASTMultiplicativeExpression");
                        memoryHelper.returnVariable(returnData.memoryLocation, returnData.type);
                        returnData.type = "DOUBLE";
                        returnData.memoryLocation = memoryHelper.requestVariable("DOUBLE");
                        fileWriter.storeRegister(register2, returnData, "ASTMultiplicativeExpression");
                        memoryHelper.returnIntRegister(register1);
                        memoryHelper.returnFloatRegister(register2);
                    }
                    if (returnData1.type == "INTEGER") {
                        // convert to double
                        register1 = memoryHelper.requestIntRegister();
                        register2 = memoryHelper.requestFloatRegister();
                        fileWriter.loadRegister(register1, returnData1.memoryLocation, returnData1.type,
                                "ASTMultiplicativeExpression");
                        fileWriter.addCode("F[" + register2 + "] = (double) R[" + register1 + "];\r\n",
                                "ASTMultiplicativeExpression");
                        memoryHelper.returnVariable(returnData1.memoryLocation, returnData1.type);
                        returnData1.type = "DOUBLE";
                        returnData1.memoryLocation = memoryHelper.requestVariable("DOUBLE");
                        fileWriter.storeRegister(register2, returnData1, "ASTMultiplicativeExpression");
                        memoryHelper.returnIntRegister(register1);
                        memoryHelper.returnFloatRegister(register2);
                    }
                    if (returnData2.type == "INTEGER") {
                        // convert to double
                        register1 = memoryHelper.requestIntRegister();
                        register2 = memoryHelper.requestFloatRegister();
                        fileWriter.loadRegister(register1, returnData2.memoryLocation, returnData2.type,
                                "ASTMultiplicativeExpression");
                        fileWriter.addCode("F[" + register2 + "] = (double) R[" + register1 + "];\r\n",
                                "ASTMultiplicativeExpression");
                        memoryHelper.returnVariable(returnData2.memoryLocation, returnData2.type);
                        returnData2.type = "DOUBLE";
                        returnData2.memoryLocation = memoryHelper.requestVariable("DOUBLE");
                        fileWriter.storeRegister(register2, returnData2, "ASTMultiplicativeExpression");
                        memoryHelper.returnIntRegister(register1);
                        memoryHelper.returnFloatRegister(register2);
                    }
                    register1 = memoryHelper.requestFloatRegister();
                    register2 = memoryHelper.requestFloatRegister();
                    fileWriter.loadRegister(register1, returnData1.memoryLocation, returnData1.type,
                            "ASTMultiplicativeExpression");
                    fileWriter.loadRegister(register2, returnData2.memoryLocation, returnData2.type,
                            "ASTMultiplicativeExpression");
                    if (multiplicative.equals("MULTIPLY")) {
                        fileWriter.addCode("F[" + register1 + "] = F[" + register1 + "] * F[" + register2 + "];\r\n",
                                "ASTMultiplicativeExpression");
                    } else if (multiplicative.equals("DIVIDE")) {
                        fileWriter.addCode("F[" + register1 + "] = F[" + register1 + "] / F[" + register2 + "];\r\n",
                                "ASTMultiplicativeExpression");
                    } else if (multiplicative.equals("MOD")) {
                        fileWriter.addCode("F[" + register1 + "] = F[" + register1 + "] % F[" + register2 + "];\r\n",
                                "ASTMultiplicativeExpression");
                    }
                    fileWriter.storeRegister(register1, returnData, "ASTMultiplicativeExpression");
                    memoryHelper.returnFloatRegister(register1);
                    memoryHelper.returnFloatRegister(register2);
                } else {
                    throw new RuntimeException("Error: Illegal use of multiplicative operator on non-integer type.");
                }

                // release temp variables
                if (i == 0) {
                    memoryHelper.returnVariable(returnData1.memoryLocation, returnData1.type);
                    memoryHelper.returnVariable(returnData2.memoryLocation, returnData2.type);
                } else {
                    // second pass returnData1 == returnData
                    memoryHelper.returnVariable(returnData2.memoryLocation, returnData2.type);
                }
            }
        }
        return returnData;
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
        // get unique ID
        int ID = GetID();
        // Print information about node
        printNode(ID, "ASTUnaryExpression");
        // Iterate through children nodes
        node.childrenAccept(this, data);
        // Return to parent node (or move to sibling node if exists)
        return null;
    }

    public VisitReturn visitUnaryExpression(ASTUnaryExpression node) {
        VisitReturn returnData = new VisitReturn();

        if (node.jjtGetChild(0) instanceof ASTUnaryExpression) {
            ASTUnaryExpression cnode = (ASTUnaryExpression) node.jjtGetChild(0);
            returnData = visitUnaryExpression(cnode);
            if (node.data.get("Unary") != null) {
                String unary = node.data.get("Unary").toString();
                if (unary.equals("PLUS")) {
                    if (returnData.type == "INTEGER") {
                        /* To-Do: implement unary operation */
                        throw new RuntimeException(
                                "To-Do: Unary plus Not implemented. Please implement this feature.");
                    } else {
                        throw new RuntimeException(
                                "Error: Illegal use of unary plus operator on non-integer type.");
                    }
                } else if (unary.equals("MINUS")) {

                    if (returnData.type == "INTEGER") {
                        /* To-Do: implement unary operation */
                        throw new RuntimeException(
                                "To-Do: Unary minus Not implemented. Please implement this feature.");
                    } else {
                        throw new RuntimeException(
                                "Error: Illegal use of unary minus operator on non-integer type.");
                    }
                } else if (unary.equals("NOT")) {
                    /* To-Do: implement unary operation */
                    if (returnData.type == "BOOLEAN") {
                        throw new RuntimeException(
                                "To-Do: Unary not Not implemented. Please implement this feature.");
                    } else {
                        throw new RuntimeException("Error: Illegal use of unary not operator on non-boolean type.");
                    }
                }
            }
        } else if (node.jjtGetChild(0) instanceof ASTPostfixExpression) {
            ASTPostfixExpression cnode = (ASTPostfixExpression) node.jjtGetChild(0);
            returnData = visitPostfixExpression(cnode);
        }
        return returnData;
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
        // get unique ID
        int ID = GetID();
        // Print information about node
        printNode(ID, "ASTPrimaryExpression");
        // Iterate through children nodes
        node.childrenAccept(this, data);
        // Return to parent node (or move to sibling node if exists)
        return null;
    }

    public VisitReturn visitPostfixExpression(ASTPostfixExpression node) {
        ASTPrimaryExpression cnode = (ASTPrimaryExpression) node.jjtGetChild(0);
        // Get child evaluated
        VisitReturn visitReturn = visitPrimaryExpression(cnode);
        // if the child is an integer, then we can increment or decrement
        if (visitReturn.type == "INTEGER" && node.data.get("Postfix") != null) {
            int register = memoryHelper.requestIntRegister();
            if (node.data.get("Postfix") != null) {
                String postfix = node.data.get("Postfix").toString();
                fileWriter.loadRegister(register, visitReturn.memoryLocation, visitReturn.type, "ASTPostfixExpression");
                if (postfix.equals("INCREMENT")) {
                    fileWriter.addCode("R[" + register + "] = R[" + register + "] + 1;\r\n", "ASTPostfixExpression");
                } else {
                    fileWriter.addCode("R[" + register + "] = R[" + register + "] - 1;\r\n", "ASTPostfixExpression");
                }
                fileWriter.addCode("F23_Time += 1;\r\n", "ASTPostfixExpression");
                fileWriter.storeRegister(register, visitReturn, "ASTPostfixExpression");
            }
            memoryHelper.returnIntRegister(register);
        } else if (visitReturn.type != "INTEGER" && node.data.get("Postfix") != null) {
            throw new RuntimeException("Error: Illegal use of increment or decrement operator on non-integer type.");
        }

        return visitReturn;
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
        // get unique ID
        int ID = GetID();
        // Print information about node
        printNode(ID, "ASTPrimaryExpression");
        // Iterate through children nodes
        node.childrenAccept(this, data);
        // Return to parent node (or move to sibling node if exists)
        return null;
    }

    //A[I][J][K] or funtion calls
    public VisitReturn visitPrimaryExpression(ASTPrimaryExpression node) {
        VisitReturn visitReturn = new VisitReturn();
        // if one child then only a prefix
        if (node.jjtGetNumChildren() == 1) {
            if (node.jjtGetChild(0) instanceof ASTPrimaryPrefix) {
                ASTPrimaryPrefix cnode = (ASTPrimaryPrefix) node.jjtGetChild(0);
                visitReturn = visitPrimaryPrefix(cnode);
            }
        }
        // Suffixed expression exists
        else {
            // get the prefix
            ASTPrimaryPrefix cnode = (ASTPrimaryPrefix) node.jjtGetChild(0);
            visitReturn = visitPrimaryPrefix(cnode);
            // iterate through the suffixes
            for (int i = 1; i < node.jjtGetNumChildren(); i++) {
                ASTPrimarySuffix cnode1 = (ASTPrimarySuffix) node.jjtGetChild(i);
                visitReturn = visitPrimarySuffix(cnode1, visitReturn);
            }
        }
        return visitReturn;
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
        // get unique ID
        int ID = GetID();
        // Print information about node
        printNode(ID, "ASTPrimaryPrefix");
        // Iterate through children nodes
        node.childrenAccept(this, data);
        // Return to parent node (or move to sibling node if exists)
        return null;
    }

    public VisitReturn visitPrimaryPrefix(ASTPrimaryPrefix node) {
        VisitReturn visitReturn;
        if (node.jjtGetChild(0) instanceof ASTLiteral) {
            ASTLiteral cnode = (ASTLiteral) node.jjtGetChild(0);
            visitReturn = visitLiteral(cnode);
        } else {
            ASTExpression cnode = (ASTExpression) node.jjtGetChild(0);
            visitReturn = visitExpression(cnode);
        }
        return visitReturn;
    }

    /**
     * Description:
     * - either holds a expression in brackets or arguments
     * - Expression in brackets is an array
     * - Arguments are for function calls
     * Stored Data:
     * - No stored Data
     * 
     * Children:
    * -  Child #        Type                                Required or Optional
     * --------------------------------------------------------
     * - 1              ASTArguments || ASTExpression   Required
     */
    public Object visit(ASTPrimarySuffix node, Object data) {
        // get unique ID
        int ID = GetID();
        // Print information about node
        printNode(ID, "ASTPrimarySuffix");
        // Iterate through children nodes
        node.childrenAccept(this, data);
        // Return to parent node (or move to sibling node if exists)
        return null;
    }

    public VisitReturn visitPrimarySuffix(ASTPrimarySuffix node, VisitReturn vr) {
        VisitReturn visitReturn = new VisitReturn();
        if (node.jjtGetChild(0) instanceof ASTArguments) {
            ASTArguments cnode = (ASTArguments) node.jjtGetChild(0);
            visitReturn = visitArguments(cnode);
            String returnLabel = symbolTable.getNewLabel();
            fileWriter.addCode("P[0] = &" + returnLabel + ";\r\n",
                    "ASTArguments");
            fileWriter.addCode(
                    "goto " + symbolTable.getEntry(vr.SymbolTableEntry).Label + ";", "ASTPrimarySuffix");
            fileWriter.addCode(returnLabel + ":\r\n", "ASTArguments");
        } else {
            ASTExpression cnode = (ASTExpression) node.jjtGetChild(0);
            visitReturn = visitExpression(cnode);
        }
        return visitReturn;
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
        // get unique ID
        int ID = GetID();
        // Print information about node
        printNode(ID, "ASTLiteral");
        // Iterate through children nodes
        node.childrenAccept(this, data);
        // Return to parent node (or move to sibling node if exists)
        return null;
    }

    public VisitReturn visitLiteral(ASTLiteral node) {
        String type = (String) node.data.get("Type");
        String value = (String) node.data.get("Value");
        int register = -1;
        VisitReturn visitReturn = new VisitReturn();

        switch (type) {
            case "IDENTIFIER":
                visitReturn.type = symbolTable.getEntry(value).Type;
                visitReturn.memoryLocation = symbolTable.getEntry(value).MemoryLocation;
                visitReturn.SymbolTableEntry = value;
                System.out.println("Type: " + visitReturn.type + " Memory Location: " + visitReturn.memoryLocation
                        + " Symbol Table Entry: " + visitReturn.SymbolTableEntry);
                break;
            case "ICONSTANT":
                visitReturn.type = "INTEGER";
                visitReturn.memoryLocation = memoryHelper.requestVariable("INTEGER");
                break;
            case "DCONSTANT":
                visitReturn.type = "DOUBLE";
                visitReturn.memoryLocation = memoryHelper.requestVariable("DOUBLE");
                if (value.contains("d")) {
                    value = String.valueOf(formatDPE(value));
                }
                break;
            case "SCONSTANT":
                visitReturn.type = "STRING";
                /** Dont think this is right either */
                visitReturn.memoryLocation = memoryHelper.requestVariable("STRING");
                break;
            default:
                // Handle unknown type
                break;
        }

        if (type == "ICONSTANT") {
            register = memoryHelper.requestIntRegister();
            fileWriter.loadRegister(register, visitReturn.type, value, "ASTLiteral");
            fileWriter.storeRegister(register, visitReturn, "ASTLiteral");
            memoryHelper.returnIntRegister(register);
        } else if (type == "DCONSTANT") {
            register = memoryHelper.requestFloatRegister();
            fileWriter.loadRegister(register, visitReturn.type, value, "ASTLiteral");
            fileWriter.storeRegister(register, visitReturn, "ASTLiteral");
            memoryHelper.returnFloatRegister(register);
        } else if (type == "SCONSTANT") {
            fileWriter.LoadStrToSMem(visitReturn.memoryLocation, value, "ASTLiteral");
        }

        return visitReturn;
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
        // get unique ID
        int ID = GetID();
        // Print information about node
        printNode(ID, "ASTArguments");
        // Iterate through children nodes
        node.childrenAccept(this, data);
        // Return to parent node (or move to sibling node if exists)
        return null;
    }

    public VisitReturn visitArguments(ASTArguments node) {
        VisitReturn visitReturn = new VisitReturn();
        if (node.jjtGetNumChildren() == 1) {
            ASTArgumentList cnode = (ASTArgumentList) node.jjtGetChild(0);
            visitReturn = visitArgumentList(cnode);
        }

        return visitReturn;
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
        // get unique ID
        int ID = GetID();
        // Print information about node
        printNode(ID, "ASTArgumentList");
        // Iterate through children nodes
        node.childrenAccept(this, data);
        // Return to parent node (or move to sibling node if exists)
        return null;
    }

    public VisitReturn visitArgumentList(ASTArgumentList node) {
        VisitReturn visitReturn = new VisitReturn();
        fileWriter.addComment("/* Arguments */\r\n", "ASTArgumentList");
        for (int i = 0; i < node.jjtGetNumChildren(); i++) {
            ASTExpression cnode = (ASTExpression) node.jjtGetChild(i);
            visitReturn = visitExpression(cnode);
            if (visitReturn.type == "INTEGER") {
                fileWriter.addCode("P[" + (i + 1) + "] = &Mem[" + visitReturn.memoryLocation + "];\r\n",
                        "ASTArgumentList");
            } else if (visitReturn.type == "DOUBLE") {
                fileWriter.addCode("P[" + (i + 1) + "] = &FMem[" + visitReturn.memoryLocation + "];\r\n",
                        "ASTArgumentList");
            } else if (visitReturn.type == "STRING") {
                fileWriter.addCode("P[" + (i + 1) + "] = &SMem[" + visitReturn.memoryLocation + "];\r\n",
                        "ASTArgumentList");
            } else {
                throw new RuntimeException("Error: Type " + visitReturn.type + " is not supported for arguments.");
            }

        }
        return visitReturn;
    }

    //************ Helper Functions **********************************************/
    // format a double precision exponent into a double
    // convert the format from 2.0d5 to 200000.0
    public double formatDPE(String doublePE) {
        String[] parts = doublePE.split("d");
        double mantissa = Double.parseDouble(parts[0]);
        int exponent = Integer.parseInt(parts[1]);
        double result = mantissa * Math.pow(10, exponent);
        return result;
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

}