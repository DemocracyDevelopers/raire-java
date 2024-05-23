# Comparison To raire-rs

This is a Java re-implementation of raire-rs https://github.com/DemocracyDevelopers/raire-rs
It attempts to copy the design, API, and naming as much as possible subject to being idiomatic and efficient Java.

## Namespace

The namespace for everything starts with `au.org.democracydevelopers.raire`

Many of the Rust files in raire-rs contain a set of thematically related
items that require separate files in Java. These are generally
all in a single folder in the `au.org.democracydevelopers.raire` namespace,
with name being determined by the Rust file.

For instance, the `audit_type.rs` file has become several files in the
`au.org.democracydevelopers.raire.audittype` namespace. The `AuditType` 
Rust trait has become the `au.org.democracydevelopers.raire.audittype.AuditType`
interface, and the four different audit types implemented as structs in
Rust have become equivalently named classes in Java. The enum `Audit` in
Rust has not been translated to Java as the dynamic dispatch in Java
handles its functionality automatically. 

## Utility type aliases

raire-rs has several utility type aliases and wrappers, such as
`AssertionDifficulty` which is really just a double precision floating
point argument renamed for documentation purposes at zero runtime cost.

This is not straight forward to do in Java without significant runtime
cost, so the java type `double` is used instead of `AssertionDifficulty`

Similarly, the Java type `int` is used instead of the Rust
`BallotPaperCount` and `CandidateIndex` types. Note that this gives somewhat
lower restrictions on number of ballots and candidates than Rust, using
the Java signed int rather than Rust usize or u32. But two billion is still
large enough for any current human electorate I am aware of.

## Exceptions

A Rust function returning a type `Result<R,E>` will be
converted to a Java function returning R and possibly throwing
exception E.

## Execution speed and memory usage

The Java version is a little slower and uses more memory than the Rust version,
by an amount that is problem specific but on the order of 30%.
This is primarily due to the language overhead of Java.
