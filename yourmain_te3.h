			// Generated from function: ASTProgram
int yourmain()
{
			// Generated from function: ASTProgram
int*    P[16];
			// Generated from function: ASTProgram
goto L000;
			// Generated from function: ASTProcedureDeclaration
L001:
			// Comment From: ASTLocalVariableDeclaration
			// Variable Declaration: i at mem location 0
			// Generated from function: ASTParameter
Mem[0] = P[1];			// Generated from function: ASTParameter
			// Comment From: ASTLocalVariableDeclaration
			// Variable Declaration: title at mem location 0
			// Generated from function: ASTLiteral
strcpy(&SMem[1], "printing integer ");
F23_Time += (60 * 19);
			// Generated from function: ASTStatementExpression
strcpy(&SMem[0], SMem[1]);
F23_Time += (60 * 7);
			// Generated from function: ASTPrintStringStatement
print_string( &SMem[0] );
			// Generated from function: ASTPrintIntStatement
print_int( Mem[SR+0] );
			// Generated from function: ASTLiteral
strcpy(&SMem[1], "\n");
F23_Time += (60 * 4);
			// Generated from function: ASTPrintStringStatement
print_string( &SMem[1] );
			// Generated from function: ASTProcedureDeclaration
goto *P[0];
			// Generated from function: ASTFunctionDeclaration
L000:
			// Comment From: ASTLocalVariableDeclaration
			// Variable Declaration: i at mem location 1
			// Generated from function: ASTLiteral
R[0] = 441;
F23_Time += (1);
			// Generated from function: ASTLiteral
Mem[SR+2] = R[0];
F23_Time += (20 + 1);
			// Generated from function: ASTStatementExpression
R[1] = Mem[SR+1];
F23_Time += (20 + 1);
			// Generated from function: ASTStatementExpression
R[2] = Mem[SR+2];
F23_Time += (20 + 1);
			// Generated from function: ASTStatementExpression
R[1]=R[2];
			// Generated from function: ASTStatementExpression
Mem[SR+1] = R[1];
F23_Time += (20 + 1);
			// Comment From: ASTArgumentList
			// /* Arguments */

			// Generated from function: ASTArgumentList
P[1] = &Mem[1];
			// Generated from function: ASTArguments
P[0] = &L002;
			// Generated from function: ASTPrimarySuffix
goto L001;			// Generated from function: ASTArguments
L002:
			// Generated from function: ASTFunctionDeclaration
return 0;
			// Generated from function: ASTProgram
}