/*
  Copyright 2023 Democracy Developers
  This is a Java re-implementation of raire-rs https://github.com/DemocracyDevelopers/raire-rs
  It attempts to copy the design, API, and naming as much as possible subject to being idiomatic and efficient Java.

  This file is part of raire-java.
  raire-java is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
  raire-java is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License for more details.
  You should have received a copy of the GNU Affero General Public License along with ConcreteSTV.  If not, see <https://www.gnu.org/licenses/>.

 */

package au.org.democracydevelopers.raire;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * A simple program to execute the RAIRE algorithm on a JSON file producing a JSON file.
 */
public class CommandLine {
    public static void main(String[] args) throws Exception {
        if (args.length<1 || args.length>2) {
            System.err.println("Should have 1 or 2 arguments, the first the input file name, the optional second the output file name");
            return;
        }

        Path inputPath = Paths.get(args[0]);
        byte[] jsonInput = Files.readAllBytes(inputPath);
        ObjectMapper mapper = new ObjectMapper();
        RaireProblem problem = mapper.readValue(jsonInput,RaireProblem.class);
        RaireSolution solution = problem.solve();
        String outName;
        if (args.length==2) outName=args[1];
        else {
            outName=inputPath.getFileName().toString();
            int pos = outName.lastIndexOf('.');
            if (pos>=0) outName=outName.substring(0,pos);
            outName+="_out.json";
        }
        mapper.writeValue(new File(outName),solution);
    }
}
