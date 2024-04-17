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
			// Comment From: ASTLocalVariableDeclaration
			// Variable Declaration: a at mem location 0
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
			// Generated from function: ASTLiteral
R[3] = 3;
F23_Time += (1);
			// Generated from function: ASTLiteral
Mem[SR+1] = R[3];
F23_Time += (20 + 1);
			// Comment From: ASTMultiplicativeExpression
			// register1: 4
			// Comment From: ASTMultiplicativeExpression
			// register2: 5
			// Generated from function: ASTMultiplicativeExpression
R[4] = Mem[SR+1];
F23_Time += (20 + 1);
			// Generated from function: ASTMultiplicativeExpression
R[5] = Mem[SR+0];
F23_Time += (20 + 1);
			// Generated from function: ASTMultiplicativeExpression
R[4] = R[4] * R[5];
			// Generated from function: ASTMultiplicativeExpression
Mem[SR+2] = R[4];
F23_Time += (20 + 1);
			// Generated from function: ASTLiteral
F[0] = 200.0;
F23_Time += (2);
			// Generated from function: ASTLiteral
FMem[FR+1] = F[0];
F23_Time += (20 + 2);
			// Generated from function: ASTMultiplicativeExpression
R[6] = Mem[SR+2];
F23_Time += (20 + 1);
			// Generated from function: ASTMultiplicativeExpression
F[1] = (double) R[6];
			// Generated from function: ASTMultiplicativeExpression
FMem[FR+3] = F[1];
F23_Time += (20 + 2);
			// Generated from function: ASTMultiplicativeExpression
F[2] = FMem[FR+3];
F23_Time += (20 + 2);
			// Generated from function: ASTMultiplicativeExpression
F[3] = FMem[FR+1];
F23_Time += (20 + 2);
			// Generated from function: ASTMultiplicativeExpression
F[2] = F[2] / F[3];
			// Generated from function: ASTMultiplicativeExpression
FMem[FR+2] = F[2];
F23_Time += (20 + 2);
			// Generated from function: ASTStatementExpression
F[4] = FMem[FR+0];
F23_Time += (20 + 2);
			// Generated from function: ASTStatementExpression
F[5] = FMem[FR+2];
F23_Time += (20 + 2);
			// Generated from function: ASTStatementExpression
F[4]=F[5];
			// Generated from function: ASTStatementExpression
FMem[FR+0] = F[4];
F23_Time += (20 + 2);
			// Generated from function: ASTLiteral
strcpy(SMem[0], "i = ");
F23_Time += (60 * 6);
			// Generated from function: ASTPrintStringStatement
print_string( SMem[0] );
			// Generated from function: ASTPrintIntStatement
print_int( Mem[SR+0] );
			// Generated from function: ASTLiteral
strcpy(SMem[1], ", a = ");
F23_Time += (60 * 8);
			// Generated from function: ASTPrintStringStatement
print_string( SMem[1] );
			// Generated from function: ASTPrintDoubleStatement
print_double( FMem[FR+0] );
			// Generated from function: ASTLiteral
strcpy(SMem[2], "\n");
F23_Time += (60 * 4);
			// Generated from function: ASTPrintStringStatement
print_string( SMem[2] );
			// Generated from function: ASTFunctionDeclaration
return 0;
			// Generated from function: ASTProgram
}