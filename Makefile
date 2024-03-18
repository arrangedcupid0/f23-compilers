# Variables


all: treegen gen comp run

try_te1: treegen gen comp run_te1

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
	java -cp Compiled Compiler < Input/te1.f23
#ren .\Output\filename.txt te1.h

run_te4: Input/te4.f23
	java -cp Compiled Compiler < Input/te2.f23
#ren .\Output\filename.txt te2.h

run_mg: Input/mg.f23
	java -cp Compiled Compiler < Input/mg.f23
#ren .\Output\filename.txt mg.h


clean: 
	del /q .\Generated\*
	del /q .\Compiled\*
	del /q .\Output\*.h

# help and test javacc exists
javacc: 
	javacc

.phony: clean javacc