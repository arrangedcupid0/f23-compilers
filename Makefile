# Variables


all: treegen gen comp run

try_te1: treegen gen comp run_te1

try_te2: treegen gen comp run_te2

try_te3: treegen gen comp run_te3

try_te4: treegen gen comp run_te4

try_del1: treegen gen comp run_del1

try_if: treegen gen comp run_if

try_do: treegen gen comp run_do

try_while: treegen gen comp run_while

treegen: BaseFiles/fresh.jjt
	jjtree BaseFiles/fresh.jjt
	copy BaseFiles\BaseNode.java Generated
	copy BaseFiles\SymbolTable.java Generated

gen: Generated/fresh.jj
	javacc Generated/fresh.jj

comp:
	javac -d Compiled Generated/*.java

run: run_te1 run_te2 run_te3 run_te4 run_mg

run_te1: Input/te1.f23
	java -cp Compiled Compiler < Input/te1.f23
#ren .\Output\filename.txt te1.h

run_te2: Input/te2.f23
	java -cp Compiled Compiler < Input/te2.f23
#ren .\Output\filename.txt te2.h

run_te3: Input/te3.f23
	java -cp Compiled Compiler < Input/te3.f23
#ren .\Output\filename.txt te1.h

run_te4: Input/te4.f23
	java -cp Compiled Compiler < Input/te4.f23
#ren .\Output\filename.txt te2.h

run_del1: Input/del1.f23
	java -cp Compiled Compiler < Input/del1.f23
#ren .\Output\filename.txt te2.h

run_if: Input/ifStatements.f23
	java -cp Compiled Compiler < Input/ifStatements.f23

run_do: Input/doLoop.f23
	java -cp Compiled Compiler < Input/doLoop.f23

run_while: Input/whileLoop.f23
	java -cp Compiled Compiler < Input/whileLoop.f23

run_mg: Input/mg.f23
	java -cp Compiled Compiler < Input/mg.f23
#ren .\Output\filename.txt mg.h

cp_output: Output/yourmain.h
	copy Output\filename.txt Output\filename.h


clean: 
	del /q .\Generated\*
	del /q .\Compiled\*
	del /q .\Output\*.h

# help and test javacc exists
javacc: 
	javacc

.phony: clean javacc

