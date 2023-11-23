# Variables


all: treegen gen comp run

treegen: BaseFiles/te1.jjt
	jjtree BaseFiles/te1.jjt
	copy BaseFiles\BaseNode.java Generated
	copy BaseFiles\TreeWalk.java Generated

gen: Generated/te1.jj
	javacc Generated/te1.jj

comp:
	javac -d Compiled Generated/*.java

run: run_te1 run_te2

run_te1: Input/te1.f23
	java -cp Compiled Compiler < Input/te1.f23

run_te2: Input/te2.f23
	java -cp Compiled Compiler < Input/te2.f23

clean: 
	del /q .\Generated\*
	del /q .\Compiled\*

# help and test javacc exists
javacc: 
	javacc

.phony: clean javacc