/* Generated By:JJTree: Do not edit this line. ASTIfThen.java Version 4.1 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=false,NODE_PREFIX=AST,NODE_EXTENDS=BaseNode,NODE_FACTORY= */
public class ASTIfThen extends SimpleNode {
  public ASTIfThen(int id) {
    super(id);
  }

  public ASTIfThen(Compiler p, int id) {
    super(p, id);
  }


  /** Accept the visitor. **/
  public Object jjtAccept(CompilerVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }
}
/* JavaCC - OriginalChecksum=b8dab4615ed853217dc993e2fcfc8e61 (do not edit this line) */