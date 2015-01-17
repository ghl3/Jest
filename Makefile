
all: src/java/grammar/JestLexer.java target/jest-0.1.0-SNAPSHOT-standalone.jar

src/java/grammar/JestLexer.java: grammar/jest.g
	lein antlr


target/jest-0.1.0-SNAPSHOT-standalone.jar: src/java/grammar/JestLexer.java src/java/grammar/JestParser.java
	lein uberjar



