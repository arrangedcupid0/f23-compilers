# Variables

all: 
	javacc

# Tiny Example 1
te1: te1_treegen te1_gen te1_comp te1_run

te1_treegen: BaseFiles/Expr.jjt
	jjtree BaseFiles/Expr.jjt

te1_gen: Generated/Expr.jj
	javacc Generated/Expr.jj
	cp BaseFiles/BaseNode.java Generated/BaseNode.java
	cp BaseFiles/ExpressionVisitor.java Generated/ExpressionVisitor.java

te1_comp:
	javac -d Compiled Generated/*.java

te1_run: Input/te1.f23
	java -cp Compiled Compiler < Input/te1.f23

te1_clean: 
	rm -f CalcInterpreter/Generated/*
	rm -f CalcInterpreter/Compiled/*

clean: te1_clean

.phony: clean te1_clean