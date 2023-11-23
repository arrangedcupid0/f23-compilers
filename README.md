Authors:
Samuel Riggs,
Kellen Mentock,
Michael Stoll

# Simple compiler

### Setup

    The user will need to add the lib folder to their PATH for this project to generate, compile and run.

    Additionally, if the user is not running the program from a windows machine, the copy and delete file commands inside the MAkefile may faile, and will need to be adjusted to run for their operating system. see the *clean* and *treegen* commands inside the makefile

### Makefile

`make (Make all)`: Generates, compiles and runs tiny example 1 and 2

`make treegen`: Generates the java.jj file and appropriate files for the parse tree. Adds these files to the /Generated folder.
Additionally it copies the TreeWalk.java and SymbolTable.java files to the "Generated" folder

`make gen`: Generates the files from the lex and parser section of the .jj file. Adds these files to the "Generated" folder

`make comp`: Compiles the contents of the "Generated" folder to the "Compiled" Folder

`make run`: runs both Tiny Example 1 and Tiny example 2 on the compiled files

`make run_te1`: runs Tiny Example 1 on the compiled files

`make run_te2`: runs Tiny Example 2 on the compiled files

`make clean`: Deletes the contents of the Generated and Compiled folders

### Notes

- The target folder contains the .jar file needing to be distributed with the compiler.

- The lib folder contains the executable files to run JavaCC and its functions. This folder will need to be added to the PATH variables.

- The Input folder contains the examples to run, as well as two sub folders, which is our nots on tokenization of the two tiny examples and the expected output of the Tree Walk.

- The BaseFiles folder contains the core files that JavaCC will Generate the Lexer and Parser from.

- Inside the BaseFiles, the BaseNode.java file gives the Non-terminal nodes a common inheritace to implement.

- Inside the BaseFiles, the TreeWalk.java file is a Visitor implementation to the nodes after the Parser is finished generating the tree. TreeWalk.java will handle printing out information when it walks through the tree.

- Inside the BaseFiles, the SymbolTable.java file is not yet implemented, please ignore this file for now.

- Inside the BaseFiles, the te1.jjt file is the JavaCC tree file on which everything else will generate. please see https://github.com/javacc/javacc/tree/master for documentation.
