# raire-java

This is a Java implementation of the RAIRE algorithm as a library. 

It is very closely based on the Rust implementation in [raire-rs](https://github.com/DemocracyDevelopers/raire-rs).
See [ComparisonToRaireRS.md](ComparisonToRaireRS.md) for details. The documentation for raire-rs is generally
valid for raire-java.

## Entry point

The main entry point is the constructor for `au.org.democracydevelopers.raire.algorithm.RaireResult`
which will either return a valid result or throw a RaireException.

