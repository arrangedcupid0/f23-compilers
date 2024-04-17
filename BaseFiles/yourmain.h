			// Generated from function: ASTProgram
int yourmain()
{
			// Generated from function: ASTProgram
int*    P[16];
int offset;			// Generated from function: ASTProgram
goto L000;
			// Generated from function: ASTFunctionDeclaration
L000:
			// Comment From: ASTLocalVariableDeclaration
			// Variable Declaration: arr at mem location 0
			// Generated from function: ASTLiteral
R[0] = 0;
F23_Time += (1);
		// Generated from function: ASTLiteral
R[3] = 9;
F23_Time += (1);
			// Generated from function: ASTLiteral
			// Generated from function: ASTStatementExpression
R[4] = &Mem[SR]; //R[4] holds address of top of array
F23_Time += (20 + 1);
			// Generated from function: ASTStatementExpression
R[5] = R[(3 - 3)%32]; //first 3 is dynamic and is generated iteratively. second three is hardcoded because of the structure
			//holds the index we're referencing
F23_Time += (20 + 1);
			// Generated from function: ASTStatementExpression
R[4] = R[4] + R[5]; //now holds the address of the index we want
			// Generated from function: ASTStatementExpression
Mem[R[4]] = R[3];
F23_Time += (20 + 1);
			// Generated from function: ASTLiteral
R[6] = 0;
F23_Time += (1);
			// Generated from function: ASTLiteral
			// Generated from function: ASTPrimarySuffix
R[7] = &Mem[SR + 0]; //the address of the head of the array
F23_Time += (20 + 1);
			// Generated from function: ASTPrimarySuffix
R[8] = R[6];
			// Generated from function: ASTPrimarySuffix
R[7] = R[7] + R[8]; //now holds address of index we desire
			// Generated from function: ASTPrimarySuffix
R[7] = R[7];	//don't think we need this		// Generated from function: ASTPrimarySuffix
F23_Time += (20 + 1);
			// Generated from function: ASTPrintIntStatement
print_int( R[7] );
			// Generated from function: ASTFunctionDeclaration
return 0;
			// Generated from function: ASTProgram
}
