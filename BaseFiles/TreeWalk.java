
import java.util.HashMap;
import java.util.LinkedList;

public class TreeWalk implements CompilerVisitor {

    private LinkedList stack = new LinkedList();
    private HashMap symbolTable = new HashMap();

    private int IDcounter = 0;

    private int GetID() {
        IDcounter++;
        return IDcounter;
    }

    public Object visit(SimpleNode node, Object data) {
        printHeader();
        node.childrenAccept(this, data);
        printfooter();
        return null;
    }

    public Object visit(ASTProgram node, Object data) {
        printHeader();
        int ID = GetID();
        System.out.println("-----");
        System.out.println("** Node " + ID + ": Program");
        System.out.println("-----");
        node.childrenAccept(this, data);
        printfooter();
        return symbolTable;
    }

    public Object visit(ASTFunction node, Object data) {
        int ID = GetID();
        System.out.println("-----");
        System.out.println("** Node " + ID + ": Function");
        System.out.println("**** Returns " + node.data.get("type"));
        System.out.println("-----");
        node.childrenAccept(this, data);
        return null;
    }

    public Object visit(ASTStatement node, Object data) {
        int ID = GetID();
        System.out.println("-----");
        System.out.println("** Node " + ID + ": Statement");
        System.out.println("**** Statement Type: " + node.data.get("type"));
        System.out.println("-----");
        node.childrenAccept(this, data);
        return null;
    }

    public Object visit(ASTVariableDeclare node, Object data) {
        int ID = GetID();
        System.out.println("-----");
        System.out.println("** Node " + ID + ": Variable Declareation");
        System.out.println("**** Type: " + node.data.get("type"));
        System.out.println("**** Variable Name: " + node.data.get("value"));
        System.out.println("-----");
        node.childrenAccept(this, data);
        return null;
    }

    public Object visit(ASTVariableAssignment node, Object data) {
        int ID = GetID();
        System.out.println("-----");
        System.out.println("** Node " + ID + ": Variable Assignment");
        System.out.println("**** Type: " + node.data.get("type"));
        System.out.println("**** Variable Name: " + node.data.get("variable"));
        System.out.println("**** Variable Value: " + node.data.get("value"));
        System.out.println("-----");
        node.childrenAccept(this, data);
        return null;
    }

    public Object visit(ASTPrintStatement node, Object data) {
        int ID = GetID();
        System.out.println("-----");
        System.out.println("** Node " + ID + ": PrintStatement");
        System.out.println("**** Print Type: " + node.data.get("PType"));
        System.out.println("**** Print Variable/Value: " + node.data.get("value"));
        System.out.println("-----");
        node.childrenAccept(this, data);
        return null;
    }

    private Integer pop() {
        return (Integer) stack.removeFirst();
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