/* Generated By:JJTree: Do not edit this line. ASTAtomicValue.java Version 4.1 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=false,NODE_PREFIX=AST,NODE_EXTENDS=BaseNode,NODE_FACTORY= */
public class ASTAtomicValue extends SimpleNode {
  public ASTAtomicValue(int id) {
    super(id);
  }

  public ASTAtomicValue(Compiler p, int id) {
    super(p, id);
  }


  /** Accept the visitor. **/
  public Object jjtAccept(CompilerVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }
}
/* JavaCC - OriginalChecksum=6e69dfaa8a804858270e15d62a4b6bda (do not edit this line) */
