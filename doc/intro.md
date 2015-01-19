# Introduction to Jest


## Executable

The main jest executable lives in a pre-compiled java Jar file and is run using a bash script that uses whatever version of Java is in the environment to run the jar file.  This is the equivalent of doing:

    > java -jar jest-0.1.0-SNAPSHOT-standalone.jar <args>

The jar itself is compiled using Leiningen as an _uberjar_ and the main function is ahead-of-time compiled.  

To run a Jest program, simply use the primary jest script located in the bin directory:

    >jest myProgram.jst
    
By convention, jest programs use the ".jst" suffix.

One can also convert a Jest program to the equivalent Clojure source by using the "--clojure" flag of the executable:

    >jest --clojure myProgram.jst
    
This will print the Clojure source to standard out.  Use the "-h" or "--help" flags for other options.

## Variables

Variables are declared using the "val" keyword, an equals sign, and an expression:

    val x = 10;
    val y = x*x;
    
Variable declaration statements must terminate with a semi-colon.  Variable names may not be re-used (currently the language implementation allows this, but future iterations will remove this, so programs should not depend on it as a property).

## Vectors

Jest has vector literals that can be created using bracket notation:

    val vec = [1, 2, 3];
    
Items in a vector must be separated by a comma.  Jest vectors are _Clojure_ vectors, which means they are persistent and immutable but have nearly O(1) random read access.  One can get the ith element of a vector using the get function.  There are also a number of other functions that act on vectors:

    val zeroth = vec.get(0);
	val fst = vec.first();
	val lst = vec.last();


## Maps

Jest has map literals that can be created using curly-bracket notation:

    val mp = {"a": 1, "b": 2};
    
Items in a map are related using a colon and pairs of items must be separated by a comma.  Jest maps are _Clojure_ maps, which means they are persistent and immutable but have nearly O(1) read access by key.  One can get a key from a map using the get function:

    val myVal = mp.get("a"); 

## Functions

Functions in Jest are first class objects.  They can be created using the "defn" keyword:

    defn function(a, b, c) {
        a + b + c;
    };

The value of a function is the value of the last expression in the body of the function when evaluated with the input parameters.  Function bodies must include braces and, when ending a statement, the braces must terminate in a semi-colon.  

Functions are called by passing arguments to the name bound to the function in the standard way:

    val result = function(1.0, 2.0, 3.0); 

## For Loop

For loops allow for looping over and mapping over one or more iterables:

    iterA = [1, 2, 3];
    iterB = [4, 5, 6];

    for (a: iterA) {
        println(a);
    };
    
    ;; 1
    ;; 2
    ;; 3
    
    for (a, b: iterA, iterB) {
        println(a + b);
    };
    
    ;; 5
    ;; 7
    ;; 9

For loops in Jest are expressions, meaning that they evaluate to values.  This means that one can set a variable to a for loop.  The value of a for loop is the value of the final expression of the body of the loop evaluated over every item in the supplied iterables.

    val x = for(a, b: iterA, iterB) {
        a*b;
    }
    
    println(x);
    
    ;; (4, 10, 18);
    
The value of a loop is an eager sequence, but one may return a lazy sequence by adding the "lazy" keyword:

    val x = for lazy(a, b: iterA, iterB) {
        a*b;
    }
    
Note, of course, that any side effects executed in a lazy for loop (such as printing) won't happen until the lazy sequence is realized (which may never happen).