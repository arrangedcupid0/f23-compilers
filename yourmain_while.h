int yourmain()
{
    int *P[16];
    FR = 4;
    FR = FR >> 1;
    goto L000;
    // Generated from function: Header
    // Generated from function: ASTFunctionDeclaration
L000:
    // Generated from function: ASTLiteral
    R[0] = 25;
    F23_Time += (1);
    // Generated from function: ASTLiteral
    Mem[SR + 0] = R[0];
    F23_Time += (20 + 1);
    // Generated from function: ASTLocalVariableDeclaration
    R[1] = Mem[SR + 0];
    F23_Time += (20 + 1);
    // Comment From: ASTLocalVariableDeclaration
    // Variable Declaration: i at mem location 1
    // Generated from function: ASTLocalVariableDeclaration
    Mem[SR + 1] = R[1];
    F23_Time += (20 + 1);
    // Comment From: ASTWhileStatement
    // Start of While-Statement

    // Generated from function: ASTWhileStatement
L001:
    // Comment From: ASTWhileStatement
    // Evaluate if condidtion

    // Generated from function: ASTLiteral
    R[2] = 0;
    F23_Time += (1);
    // Generated from function: ASTLiteral
    Mem[SR + 3] = R[2];
    F23_Time += (20 + 1);
    // Generated from function: ASTRelationalExpression
    R[3] = Mem[SR + 1];
    F23_Time += (20 + 1);
    // Generated from function: ASTRelationalExpression
    R[4] = Mem[SR + 3];
    F23_Time += (20 + 1);
    // Generated from function: ASTRelationalExpression
    R[5] = R[3] > R[4];
    // Generated from function: ASTRelationalExpression
    Mem[SR + 2] = R[5];
    F23_Time += (20 + 1);
    // Generated from function: ASTWhileStatement
    R[6] = Mem[SR + 2];
    F23_Time += (20 + 1);
    // Comment From: ASTWhileStatement
    // Test If-Condition

    // Generated from function: ASTWhileStatement
    if (R[6])
        goto L002; // Generated from function: ASTWhileStatement
    goto L003;
    // Comment From: ASTWhileStatement
    // Start of While-Logic

    // Generated from function: ASTWhileStatement
L002:
    // Generated from function: ASTPrintIntStatement
    print_int(Mem[SR + 1]);
    // Generated from function: ASTStatementExpression
    R[7] = Mem[SR + 1];
    F23_Time += (20 + 1);
    // Generated from function: ASTStatementExpression
    R[7]--; // Generated from function: ASTStatementExpression
    Mem[SR + 1] = R[7];
    F23_Time += (20 + 1);
    // Comment From: ASTWhileStatement
    // Go back to start of While

    // Generated from function: ASTWhileStatement
    goto L001;
    // Comment From: ASTWhileStatement
    // End of While-Statement

    // Generated from function: ASTWhileStatement
L003:
    // Generated from function: ASTLiteral
    strcpy(&SMem[0], "Finished\n");
    F23_Time += (60 * 12);
    // Generated from function: ASTPrintStringStatement
    print_string(&SMem[0]);
    // Generated from function: ASTFunctionDeclaration
    return 0;
    // Generated from function: ASTProgram
}