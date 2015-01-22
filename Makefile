
JAVA_SOURCE := $(shell find . -regex "src/.*\.java" -print)
CLOJURE_SOURCE := $(shell find . -regex "src/.*\.clj" -print)

all: src/java/grammar/JestLexer.java src/java/grammar/JestParser.java target/jest-0.1.0-SNAPSHOT-standalone.jar

jar:
	lein uberjar

src/java/grammar/JestLexer.java: grammar/jest.g
	lein antlr

src/java/grammar/JestParser.java: grammar/jest.g
	lein antlr

target/jest-0.1.0-SNAPSHOT-standalone.jar: $(JAVA_SOURCE) $(CLOJURE_SOURCE) src/clj/jest/jest.clj src/java/grammar/JestLexer.java src/java/grammar/JestParser.java
	lein uberjar
