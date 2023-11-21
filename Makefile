# Variables

all: 
	javacc

# Simple Example 1
ci: ci_treegen ci_gen ci_comp ci_run

ci_treegen: CalcInterpreter/Expr.jjt
	jjtree CalcInterpreter/Expr.jjt

ci_gen: CalcInterpreter/Generated/Expr.jj
	javacc CalcInterpreter/Generated/Expr.jj
	cp CalcInterpreter/BaseNode.java CalcInterpreter/Generated/BaseNode.java
	cp CalcInterpreter/ExpressionVisitor.java CalcInterpreter/Generated/ExpressionVisitor.java

ci_comp:
	javac -d CalcInterpreter/Compiled CalcInterpreter/Generated/*.java

ci_run: CalcInterpreter/Input/input.fakeExt
	java -cp CalcInterpreter/Compiled ExpressionParser < CalcInterpreter/Input/input.fakeExt

ci_clean: 
	rm -f CalcInterpreter/Generated/*
	rm -f CalcInterpreter/Compiled/*

clean: ex1_clean ci_clean

.phony: clean ex1_clean ci_clean