
GRAMMAR_SOURCES := $(shell find . -regex "./src/java/jest/grammar/.*\.java" -print)
JAVA_SOURCE := $(shell find . -regex "./src/.*\.java" -print)
CLOJURE_SOURCE := $(shell find . -regex "./src/.*\.clj" -print)

DEPENDENCIES := $(JAVA_SOURCE) $(CLOJURE_SOURCE) src/clj/jest/jest.clj src/java/jest/grammar/JestLexer.java src/java/jest/grammar/JestParser.java

all: src/java/jest/grammar/JestLexer.java src/java/jest/grammar/JestParser.java target/jest-0.1.0-SNAPSHOT-standalone.jar

test: $(DEPENDENCIES)
	lein test

jar:
	lein uberjar

antlr:
	java -jar antlr4-4.5.jar -o src/java/jest grammar/Jest.g -visitor


src/java/jest/grammar/JestLexer.java: grammar/Jest.g
	make antlr

src/java/jest/grammar/JestParser.java: grammar/Jest.g
	make antlr

target/jest-0.1.0-SNAPSHOT-standalone.jar: $(DEPENDENCIES)
	lein uberjar


.PHONY: clean

clean:
	rm -f $(GRAMMAR_SOURCES)
	lein clean
