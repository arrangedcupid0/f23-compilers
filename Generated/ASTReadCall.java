/* Generated By:JJTree: Do not edit this line. ASTReadCall.java Version 4.1 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=false,NODE_PREFIX=AST,NODE_EXTENDS=BaseNode,NODE_FACTORY= */
public class ASTReadCall extends SimpleNode {
  public ASTReadCall(int id) {
    super(id);
  }

  public ASTReadCall(Compiler p, int id) {
    super(p, id);
  }


  /** Accept the visitor. **/
  public Object jjtAccept(CompilerVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }
}
/* JavaCC - OriginalChecksum=0af0d5e907b6e914467a6645c0c1296a (do not edit this line) */