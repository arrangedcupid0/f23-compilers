# Variables

all: 
	javacc

# Tiny Example 1
te1: te1_treegen te1_gen te1_comp te1_run

te1_treegen: BaseFiles/te1.jjt
	jjtree BaseFiles/te1.jjt

te1_gen: Generated/te1.jj
	javacc Generated/te1.jj


te1_comp:
	javac -d Compiled Generated/*.java

te1_run: Input/te2.f23
	java -cp Compiled Compiler < Input/te1.f23

te1_clean: 
	rm -Force .\Generated\*
	rm -Force .\Compiled\*

clean: te1_clean

.phony: clean te1_clean
#	cp BaseFiles\BaseNode.java Generated\BaseNode.java
#	cp BaseFiles\ExpressionVisitor.java Generated\ExpressionVisitor.java