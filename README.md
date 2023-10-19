# raire-java

This is a Java implementation of the RAIRE algorithm as a library. 

It is very closely based on the Rust implementation in [raire-rs](https://github.com/DemocracyDevelopers/raire-rs).
See [ComparisonToRaireRS.md](ComparisonToRaireRS.md) for details. The documentation for raire-rs is generally
valid for raire-java.

## Entry point

The main entry point is the constructor for `au.org.democracydevelopers.raire.algorithm.RaireResult`
which will either return a valid result or throw a RaireException. Alternatively, one can make
a `au.org.democracydevelopers.raire.RaireProblem` object, and call the `solve` method on it.

## Command line

You can make a command line program that takes a RaireProblem JSON and produces a RaireSolution JSON.

To build, run
```bash
mvn package
```

To run, it takes either 1 or 2 arguments. The first is the input file name. The
second is the output file name. If only 1 arguement is supplied, the input file name
is stripped of extension and path, and `_out.json` is added to produce the output file.

E.g.
```bash
java -jar target/raire-java-1.0-SNAPSHOT-jar-with-dependencies.jar ../raire-rs/WebContent/example_input/a_guide_to_RAIRE_eg_guide.json 
```
