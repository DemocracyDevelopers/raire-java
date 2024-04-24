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
second is the output file name. If only 1 argument is supplied, the input file name
is stripped of extension and path, and `_out.json` is added to produce the output file.

E.g.
```bash
java -jar target/raire-java-1.0-SNAPSHOT-jar-with-dependencies.jar ../raire-rs/WebContent/example_input/a_guide_to_RAIRE_eg_guide.json 
```



## Copyright

This program is Copyright 2023-2024 Democracy Developers.
It is based on software (c) Michelle Blom in C++ https://github.com/michelleblom/audit-irv-cp/tree/raire-branch

This file is part of raire-java.

raire-java is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

raire-java is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with raire-java.  If not, see <https://www.gnu.org/licenses/>.

## Other copyrights

This repository contains some files derived from data sources with their
own separate copyrights. These files are licensed as above to the extent that they are the work
of contributors to raire-rs, and maintain the original copyright to the appropriate
extent.
* Australian Examples/NSW Local Government/
  These lists are partially derived from data on the
  [NSW Electoral Commission website](https://www.elections.nsw.gov.au), which
  is © State of New South Wales through the NSW Electoral Commission
  and licensed under the [Creative Commons Attribution 4.0 License](https://creativecommons.org/licenses/by/4.0/) (CCA License).
  Thank you to the State of New South Wales for the use of such a license allowing us to use
  this real election data as test data.

This should not be taken as an endorsement of raire-java by any organisation listed here.

