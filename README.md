# Jest

>I knew him, Horatio; a fellow of infinite jest, of most excellent fancy

- Hamlet, V.i

## About

Jest is an dynamic language that runs on the JVM.  Jest is a (mostly) functional language with an emphasis on immutability, simplicity, and readability.

Jest programs are internally represented as Clojure data structures and are interpreted using Clojure's runtime.

## Installing

Jest requires a recent version of <a href="http://leiningen.org/">leiningen</a> to be installed.

To install Jest:

* Download the jest source code.
* Navigate inside the source directory and type "make" to run the makefile.
* Add "jest/bin" to your path.

## Usage

Run a jest program:
>jest program.jst

## Example Programs

```
val myList = range(0, 100, 10);

val incremented = map(inc, myList);

defn square(x) {
     x*x;
};

val squared = map(square, incremented);

println(squared);

val halfRange = (squared.first() + squared.last()) / 2;

println(halfRange);
```

Which prints

    (1 121 441 961 1681 2601 3721 5041 6561 8281)
    4141


```
val scores = [90, 85, 95, 92];

println("Scores: ", scores);

val studentAges = {"Jane" :14, "Bob": 16, "Tom": 15};

val answers = {"Jane": ["A", "C", "D", "A"],
    "Bob": ["B", "C", "D", "B"],
    "Tom": ["A", "C", "B", "A"]};

for (a, b : studentAges, answers) {
    println(a, b);
};
```

Which prints

    [90 85 95 92]
    [Tom 15] [Tom [A C B A]]
    [Jane 14] [Jane [A C D A]]
    [Bob 16] [Bob [B C D B]]

## License

Copyright © 2015

Distributed under the MIT License.
