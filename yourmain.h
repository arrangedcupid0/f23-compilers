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
			// Variable Declaration: arr at mem location 0
			// Generated from function: ASTLiteral
R[0] = 0;
F23_Time += (1);
			// Generated from function: ASTLiteral
Mem[SR+1] = R[0];
F23_Time += (20 + 1);
			// Generated from function: ASTPrimarySuffix
R[1] = &Mem[SR + 0];
			// Generated from function: ASTPrimarySuffix
R[2] = Mem[SR+1];
F23_Time += (20 + 1);
			// Generated from function: ASTPrimarySuffix
R[1] = R[1] + R[2];
			// Generated from function: ASTPrimarySuffix
R[1] = *R[1];			// Generated from function: ASTPrimarySuffix
Mem[SR+1] = R[1];
F23_Time += (20 + 1);
			// Generated from function: ASTLiteral
R[3] = 9;
F23_Time += (1);
			// Generated from function: ASTLiteral
Mem[SR+1] = R[3];
F23_Time += (20 + 1);
			// Generated from function: ASTStatementExpression
R[4] = Mem[SR+1];
F23_Time += (20 + 1);
			// Generated from function: ASTStatementExpression
R[5] = Mem[SR+1];
F23_Time += (20 + 1);
			// Generated from function: ASTStatementExpression
R[4]=R[5];
			// Generated from function: ASTStatementExpression
Mem[SR+1] = R[4];
F23_Time += (20 + 1);
			// Generated from function: ASTLiteral
R[6] = 0;
F23_Time += (1);
			// Generated from function: ASTLiteral
Mem[SR+1] = R[6];
F23_Time += (20 + 1);
			// Generated from function: ASTPrimarySuffix
R[7] = &Mem[SR + 0];
			// Generated from function: ASTPrimarySuffix
R[8] = Mem[SR+1];
F23_Time += (20 + 1);
			// Generated from function: ASTPrimarySuffix
R[7] = R[7] + R[8];
			// Generated from function: ASTPrimarySuffix
R[7] = *R[7];			// Generated from function: ASTPrimarySuffix
Mem[SR+1] = R[7];
F23_Time += (20 + 1);
			// Generated from function: ASTPrintIntStatement
print_int( Mem[SR+1] );
			// Generated from function: ASTFunctionDeclaration
return 0;
			// Generated from function: ASTProgram
}