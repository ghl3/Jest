# Introduction to Jest


## Testing

To run the Jest test suite, simply do:

    > lein test
    
To add verbose logging, do:

    > lein with-profile verbose test


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

There are two ways to declare variables in jest.

Global variables are declared using the "val" keyword, an equals sign, and an expression:

    val x = 10;
    val y = x*x;
    
Variable declaration statements must terminate with a semi-colon.  Variables cannot be reassigned, and variable names may not be re-used in the same scope, though a variable name in an inner scope is allowed to shadow a variable name in an outer scope.


## Comments

Jest uses c-style comments

    // This is a comment
    
    /* These are comments too
    comment
    comment
    */


## Scope

Variables declared in Jest are global unless they are declared in a let expression:

    let(val x=0) {
        x + 10;
    }

Let expressions are indeed expressions and they evaluate to the value of the final line of their body.  A variable declared in the heading of a let expression will be scoped and will exist only within the body of the let block.  A variable declared in the head will shadow any existing variables declared outside of the let expression.  One can declare multiple variables in the header of a let expression:

    let (val x = 10) {
        x+5;
    }

    val x = 100;
    
    val res = let (val x = 10; val y = 20) {
        x + y;
    }

    println(res);
    
    // Prints "30", not "120"


## Comparison Operators

Jest uses the usual comparison operators, and comparison expressions return boolean values of true or false

    5 < 10
    // true
    
    100 >= 300
    // false
    

Double equals asserts that two expressions are equal to each other (where equality is determined by value, not by reference):

    val x = 12;
    val y = 12;
    
    x == y
    // true    


## Strings

String literals are declared using double quotes:

    val str = "My String";

Jest strings are _Clojure_ Strings, which are _java.util.String_ Strings.  Among other things, this means that Jest strings are immutable.


## Symbols

Jest supports symbols (or "keywords" in _Clojure_ notation).  Symbols are values that evaluate to themselves and have no meaning outside of their own identity (in other words, they store no data other than their own id).  The main use case of a Symbol is a quick comparison, which makes them useful as keys to maps (see later);

    val symb = :mySymbol;


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

As discussed earlier, symbols are useful as keys to maps:

    val mp = {:a : 1, :b : 2};
    val x = mp[:a];


## Conditionals

In Jest, if statements are expressions, meaning they evaluate to a value.  The value of an if statement is the value of the final like of the block that is conditionally selected or 'nil' if no block is conditionally selected.

    val conditional = if (true) { 20 } else { 30 };

In the above, 'conditional' will be set to the value 20;

    val alwaysNil = if (false) { 100 };
    
One can always call functions for their side effects and still ignore the value of the if statement:

    if (true) {
        println("It's True!!");
    } else {
        println("Oh no, False...");
    }

And one can use "else if" statements, which in Jest use the "elif" keyword:

    if (nil) {
    	println("Not gunna happen");
    } elif (false) {
        println("Nope");
    } elif (true) {
        println("HELLO WORLD!");
    } else {
        println("That ship has sailed");
    }

Or, more elegantly

    println(if (nil) {
    	"Not gunna happen"
    } elif (false) {
        "Nope"
    } elif (true) {
       "HELLO WORLD!"
    } else {
        "That ship has sailed"
    });


## Functions

Functions in Jest are first class objects.  They can be created using the "defn" keyword:

    defn function(a, b, c) {
        a + b + c;
    }

The value of a function is the value of the last expression in the body of the function when evaluated with the input parameters.  Function bodies must include curly braces and the body between the braces must consist of one or more statements or expressions (each ending in a semi-colon).

Functions are called by passing arguments to the name bound to the function in the standard way:

    val result = function(1.0, 2.0, 3.0); 


## Lambdas

One can create anonymous functions, or Lambdas, in Jest using hash notation:

    val myFunc = #(% * %);
    myFunc(2):
    // evaluates to 4
    
Inside the hash, a '%' represents the (single) argument to the lambda function and the result will be a callable function of one argument.  One can create functions of more than one argument by indexing the '%' keywords:

    val myFunc = #(%1 + (%2*%3));
	myFunc(10, 2, 5);
	// evaluates to 15 + (2*5) = 25


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

    val x = for(a, b: iterA, iterB) lazy {
        a*b;
    }
    
Note, of course, that any side effects executed in a lazy for loop (such as printing) won't happen until the lazy sequence is realized (which may never happen).

## Records

In Jest, one can create structures of data using the "record" keyword:

    record Student{ name; class; }

  These are not classes as they have no methods and their data is immutable.  One can create an instance of a record using the "new" keyword:
  
    val bob = new Student("Bob", "History");
   
One can alternatively create a record by naming the fields:

    val bob = new Student(name: "Bob", class: "History");
   
Fields of a record can be accessed using a "dot" accessor:

    val class = bob.class;


## Methods

Jest doesn't have classes.  However, Jest does have syntax that allows one to call a function on an object in a familiar way.  One can call a "method" on an object in Jest by using the dot operator:

    val myVec = [1, 2, 3, 4, 5];
    myVec.get(2);
    
There, "myVec" is a vector, which is not a class, but one can use the dot notation to apply a function to it that mimics a method call in other languages.  Specifically, doing:

    obj.func(arg, arg)
    
is completely equivalent to doing:

    func(obj, arg, arg)

This is nice because it means that any function that takes a particular object as its first parameter can be interpreted as a method on that object.  This means that all Jest objects are extendable; anyone can create any new methods on that object just by defining new functions.

    defn bookend(vec) {
    	vec.first() + vec.last();
    }
    
    val myVec = [1, 2, 3, 4, 5];
    myVec.bookend();
    
    // Evaluates to 1+5 = 6
    

## Pipeing

An elegant way to process data in Jest is to leverage Jest's piping functionality.  Chaining together multiple function calls is a common procedure in a data processing pipeline and Jest makes this easy.  

Piping is enabled using the piping operator '->' which causes function calls to be chained together.  This is easiest to explain with an example:

    range(0, 100)
        ->filter(even?)
        ->map( #(%+%) )
        ->take(10);
         
    // Evaluates to: (0 4 8 12 16 20 24 28 32 36)

To explain the above, when an expression is piped into a function, it is placed as an argument to that function (at the END of the argument list).  So, for example:

    range(0, 100)
        ->filter(even?)
        
is equivalent to:

    filter(even?, range(0, 100))

which takes the range of all numbers from 0 to 100 and filters it to retain only the even ones.  Using multiple pipes in succession puts the result of the previous part of the chain into the next function.

Note that this can also be combined with method calls to produce even more possibilities:

     range(0, 100)
        ->filter(even?)
        ->map( #(%+%) )
        .get(5);

The above returns the 5th item in the list.


## Type Checking (Experimental!)

Jest currently supports an experimental and optional type-checking system.  To run a Jest script with type checking, just add the "-t" flag:

    >jest myProgram.jst -t
    
Jest type checking leverages Typed Clojure (core.typed) to validate type annotations.  In order to take advantage of this, one can optionally annotate their variables and functions:

    val x: Integer = 5;
    val y: Integer = 10;

    val myList: Vec[Integer] = [1, 2, 3, 4];

    defn func(a, b): Integer Integer -> #AnyInteger {
        a + b;
    };

    println(func(x, y));
    
 
The above function is correctly typed, as we are passing two integers into a function that expects integers, and our list of integers indeed consists of integers.  (Note that we put a hash '#' in front of AnyInteger to denote generic types).

If we had instead written:

    val x: Double = 5.0;
    val y: Integer = 10;

    defn func(a, b): Integer Integer -> #AnyInteger {
        a + b;
    };

    println(func(x, y));
    
Our type checking would have failed, as we're passing a double ('x') into a function that expects only integers.