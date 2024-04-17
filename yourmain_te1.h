			// Generated from function: ASTProgram
int yourmain()
{
			// Generated from function: ASTProgram
int*    P[16];
			// Generated from function: ASTProgram
goto L000;
			// Generated from function: ASTFunctionDeclaration
L000:
			// Comment From: ASTLocalVariableDeclaration
			// Variable Declaration: i at mem location 0
			// Generated from function: ASTLiteral
R[0] = 441;
F23_Time += (1);
			// Generated from function: ASTLiteral
Mem[SR+1] = R[0];
F23_Time += (20 + 1);
			// Generated from function: ASTStatementExpression
R[1] = Mem[SR+0];
F23_Time += (20 + 1);
			// Generated from function: ASTStatementExpression
R[2] = Mem[SR+1];
F23_Time += (20 + 1);
			// Generated from function: ASTStatementExpression
R[1]=R[2];
			// Generated from function: ASTStatementExpression
Mem[SR+0] = R[1];
F23_Time += (20 + 1);
			// Generated from function: ASTPrintIntStatement
print_int( Mem[SR+0] );
			// Generated from function: ASTFunctionDeclaration
return 0;
			// Generated from function: ASTProgram
}