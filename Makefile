# Variables

all: 
	javacc

# Tiny Example 1
te1: te1_treegen te1_gen te1_comp te1_run

te1_treegen: BaseFiles/te1.jjt
	jjtree BaseFiles/te1.jjt
	copy BaseFiles\BaseNode.java Generated
	copy BaseFiles\TreeWalk.java Generated

te1_gen: Generated/te1.jj
	javacc Generated/te1.jj


te1_comp:
	javac -d Compiled Generated/*.java

te1_run: Input/te2.f23
	java -cp Compiled Compiler < Input/te1.f23

te1_clean: 
	del /q .\Generated\*
	del /q .\Compiled\*

clean: te1_clean

.phony: clean te1_clean
#	cp BaseFiles\BaseNode.java Generated
#	cp BaseFiles\ExpressionVisitor.java Generated\ExpressionVisitor.java