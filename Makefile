
GRAMMAR_SOURCES := $(shell find . -regex "./src/java/grammar/.*\.java" -print)
JAVA_SOURCE := $(shell find . -regex "./src/.*\.java" -print)
CLOJURE_SOURCE := $(shell find . -regex "./src/.*\.clj" -print)

DEPENDENCIES := $(JAVA_SOURCE) $(CLOJURE_SOURCE) src/clj/jest/jest.clj src/java/grammar/JestLexer.java src/java/grammar/JestParser.java

all: src/java/grammar/JestLexer.java src/java/grammar/JestParser.java target/jest-0.1.0-SNAPSHOT-standalone.jar

test: $(DEPENDENCIES)
	lein test

jar:
	lein uberjar

antlr:
	java -jar antlr4-4.5.jar -o src/java grammar/Jest.g


src/java/grammar/JestLexer.java: antlr

src/java/grammar/JestParser.java: antlr

target/jest-0.1.0-SNAPSHOT-standalone.jar: $(DEPENDENCIES)
	lein uberjar


.PHONY: clean

clean:
	rm -f $(GRAMMAR_SOURCES)
