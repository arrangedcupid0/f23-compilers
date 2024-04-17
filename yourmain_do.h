int yourmain()
{
    int *P[16];
    SR = SR + 1;
    FR = 4;
    FR = FR >> 1;
    goto L000;
    // Generated from function: Header
    // Generated from function: ASTFunctionDeclaration
L000:
    // Comment From: ASTLocalVariableDeclaration
    // Variable Declaration: i at mem location 0
    // Comment From: ASTDoStatement
    // Start of Do-Statement

    // Generated from function: ASTLiteral
    R[0] = 0;
    F23_Time += (1);
    // Generated from function: ASTLiteral
    Mem[SR + 1] = R[0];
    F23_Time += (20 + 1);
    // Generated from function: ASTStatementExpression
    R[1] = Mem[SR + 0];
    F23_Time += (20 + 1);
    // Generated from function: ASTStatementExpression
    R[2] = Mem[SR + 1];
    F23_Time += (20 + 1);
    // Generated from function: ASTStatementExpression
    R[1] = R[2];
    // Generated from function: ASTStatementExpression
    Mem[SR + 0] = R[1];
    F23_Time += (20 + 1);
    // Generated from function: ASTDoStatement
L001:
    // Generated from function: ASTLiteral
    R[3] = 25;
    F23_Time += (1);
    // Generated from function: ASTLiteral
    Mem[SR + 2] = R[3];
    F23_Time += (20 + 1);
    // Generated from function: ASTRelationalExpression
    R[4] = Mem[SR + 0];
    F23_Time += (20 + 1);
    // Generated from function: ASTRelationalExpression
    R[5] = Mem[SR + 2];
    F23_Time += (20 + 1);
    // Generated from function: ASTRelationalExpression
    R[6] = R[4] < R[5];
    // Generated from function: ASTRelationalExpression
    Mem[SR + 1] = R[6];
    F23_Time += (20 + 1);
    // Generated from function: ASTDoStatement
    R[7] = Mem[SR + 1];
    F23_Time += (20 + 1);
    // Generated from function: ASTDoStatement
    if (R[7])
        goto L002; // Generated from function: ASTDoStatement
    goto L003;
    // Generated from function: ASTDoStatement
L002:
    // Generated from function: ASTStatementExpression
    R[8] = Mem[SR + 0];
    F23_Time += (20 + 1);
    // Generated from function: ASTStatementExpression
    R[8]++; // Generated from function: ASTStatementExpression
    Mem[SR + 0] = R[8];
    F23_Time += (20 + 1);
    // Generated from function: ASTPrintIntStatement
    print_int(Mem[SR + 0]);
    // Generated from function: ASTDoStatement
    goto L001;
    // Generated from function: ASTDoStatement
L003:
    // Generated from function: ASTLiteral
    strcpy(&SMem[0], "Finished\n");
    F23_Time += (60 * 12);
    // Generated from function: ASTPrintStringStatement
    print_string(&SMem[0]);
    // Generated from function: ASTFunctionDeclaration
    SR = SR + 1;
    return 0;
    // Generated from function: ASTProgram
}