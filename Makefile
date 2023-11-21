# Variables

all: 
	javacc

# Example1
ex1: ex1_gen ex1_comp ex1_run

ex1_gen: Example1/Example1.jj
	javacc Example1/Example1.jj

ex1_comp:
	javac -d Example1/Compiled Example1/Generated/*.java

ex1_run: Example1/Input/input.fakeExt
	java -cp Example1/Compiled Example1 < Example1/Input/input.fakeExt

ex1_clean: 
	rm -f Example1/Generated/*
	rm -f Example1/Compiled/*

# CalcInterpreter
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