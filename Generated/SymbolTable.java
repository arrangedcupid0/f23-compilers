import java.util.HashMap;
import java.util.LinkedList;

public class SymbolTable implements CompilerVisitor {

    private LinkedList stack = new LinkedList();
    private HashMap symbolTable = new HashMap();

    public Object visit(SimpleNode node, Object data) {
        node.childrenAccept(this, data);
        return null;
    }

    public Object visit(ASTprogram node, Object data) {
        node.childrenAccept(this, data);
        return symbolTable;
    }

    public Object visit(ASTfunction node, Object data) {
        node.childrenAccept(this, data);
        Integer arg1 = pop();
        Integer arg2 = pop();
        stack.addFirst(new Integer(arg2.intValue() + arg1.intValue()));
        return null;
    }

    private Integer pop() {
        return (Integer) stack.removeFirst();
    }

}