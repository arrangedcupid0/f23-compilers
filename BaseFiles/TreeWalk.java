import java.util.HashMap;
import java.util.LinkedList;

public class TreeWalk implements CompilerVisitor {

    // ################## Variables ##################
    private int IDcounter = 0;

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

        // finish walk
        return null;
    }

    public Object visit(ASTFunction node, Object data) {
        int ID = GetID();

        // Print information about node
        System.out.println("-----");
        System.out.println("** Node " + ID + ": Function");
        System.out.println("**** Returns " + node.data.get("type"));
        System.out.println("-----");

        // Iterate through children nodes
        node.childrenAccept(this, data);
        // Return to parent node (or move to sibling node if exists)
        return null;
    }

    public Object visit(ASTStatement node, Object data) {
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

    public Object visit(ASTVariableDeclare node, Object data) {

        int ID = GetID();
        // Standardize types
        String VarID = (String) node.data.get("value");
        String VarType = (String) node.data.get("type");
        int lineOfDecl = (int) node.data.get("lineNo");
        // Print information about node
        System.out.println("-----");
        System.out.println("** Node " + ID + ": Variable Declareation");
        System.out.println("**** Type: " + VarType);
        System.out.println("**** Variable Name: " + VarID);
        System.out.println("-----");

        // Set up data object
        SymbolTableEntry entry = new SymbolTableEntry(VarID, VarType, null, null, lineOfDecl, null, null);
        // add to Symbol table
        symbolTable.put(VarID, entry);

        // Iterate through children nodes
        node.childrenAccept(this, data);
        // Return to parent node (or move to sibling node if exists)
        return null;
    }

    public Object visit(ASTVariableAssignment node, Object data) {
        int ID = GetID();

        // Standardize types
        String VarID = (String) node.data.get("variable");
        String VarType = (String) node.data.get("type");
        Object VarValue = (String) node.data.get("value");
        int lineOfDecl = (int) node.data.get("lineNo");

        if (symbolTable.get(VarID) != null) {
            lineOfDecl = (int) symbolTable.get(VarID).LineOfDecl;
        }

        // Print information about node
        System.out.println("-----");
        System.out.println("** Node " + ID + ": Variable Assignment");
        System.out.println("**** Type: " + VarType);
        System.out.println("**** Variable Name: " + VarID);
        System.out.println("**** Variable Value: " + VarValue);
        System.out.println("-----");

        // Set up data object
        SymbolTableEntry entry = new SymbolTableEntry(VarID, VarType, null, null, lineOfDecl, null, VarValue);
        // add to Symbol table
        symbolTable.put(VarID, entry);

        // Iterate through children nodes
        node.childrenAccept(this, data);
        // Return to parent node (or move to sibling node if exists)
        return null;
    }

    public Object visit(ASTPrintStatement node, Object data) {
        int ID = GetID();

        // Standardize types
        String VarType = (String) node.data.get("PType");
        Object VarValue = (String) node.data.get("value");
        Object Value; // holder for either value

        // Retrieve variable information
        // Object SymValue = symbolTable.get(VarValue);

        if (symbolTable.get(VarValue) != null) {
            Value = symbolTable.get(VarValue).Value;
        } else {
            Value = VarValue;
        }

        // Test for type correctness
        switch (VarType) {
            case "INTEGER":
                // code block
                break;
            case "DOUBLE":
                // code block
                break;
            case "STRING":
                // code block
                break;
            default:
                System.out.println("ERROR: Unknown Type Passed To Print Statement");
        }

        // Print information about node
        System.out.println("-----");
        System.out.println("** Node " + ID + ": PrintStatement");
        System.out.println("**** Print Type: " + VarType);
        System.out.println("**** Print Passed Variable/Value: " + VarValue);
        System.out.println("**** Print Printed Variable/Value: " + Value);
        System.out.println("-----");

        // Iterate through children nodes
        node.childrenAccept(this, data);
        // Return to parent node (or move to sibling node if exists)
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

}