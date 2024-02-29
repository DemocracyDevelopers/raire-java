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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TestSerialization {
    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void testSerialization() throws JsonProcessingException {
        String demoJson = "{\n" +
                "  \"metadata\": {\n" +
                "    \"candidates\": [\"Alice\", \"Bob\", \"Chuan\",\"Diego\" ],\n" +
                "    \"note\" : \"Anything can go in the metadata section. Candidates names are used below if present. \"\n" +
                "  },\n" +
                "  \"num_candidates\": 4,\n" +
                "  \"votes\": [\n" +
                "    { \"n\": 5000, \"prefs\": [ 2, 1, 0 ] },\n" +
                "    { \"n\": 1000, \"prefs\": [ 1, 2, 3 ] },\n" +
                "    { \"n\": 1500, \"prefs\": [ 3, 0 ] },\n" +
                "    { \"n\": 4000, \"prefs\": [ 0, 3 ] },\n" +
                "    { \"n\": 2000, \"prefs\": [ 3 ]  }\n" +
                "  ],\n" +
                "  \"winner\": 2,\n" +
                "  \"trim_algorithm\": \"MinimizeTree\",\n" +
                "  \"audit\": { \"type\": \"OneOnMargin\", \"total_auditable_ballots\": 13500  }\n" +
                "}\n";
        RaireProblem problem = mapper.readValue(demoJson,RaireProblem.class);
        // String serialized = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(problem);
        // System.out.println(serialized);
        RaireSolution solution = problem.solve();
        assertNotNull(solution.solution.Ok);
        assertEquals(27.0,solution.solution.Ok.difficulty,1e-6);
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(solution));
        RaireSolution solution2 = mapper.readValue(mapper.writeValueAsString(solution),RaireSolution.class);
        assertNotNull(solution2.solution.Ok);
        assertEquals(27.0,solution2.solution.Ok.difficulty,1e-6);
        assertEquals(solution.solution.Ok.assertions.length,solution2.solution.Ok.assertions.length);
    }

    @Test
    void testErrorSerialization() throws JsonProcessingException {
        assertEquals("{\"Err\":\"InvalidTimeout\"}",mapper.writeValueAsString(new RaireSolution.RaireResultOrError(new RaireError.InvalidTimeout())));
        assertEquals("{\"Err\":\"InvalidNumberOfCandidates\"}",mapper.writeValueAsString(new RaireSolution.RaireResultOrError(new RaireError.InvalidNumberOfCandidates())));
        assertEquals("{\"Err\":\"InvalidCandidateNumber\"}",mapper.writeValueAsString(new RaireSolution.RaireResultOrError(new RaireError.InvalidCandidateNumber())));
        assertEquals("{\"Err\":\"TimeoutCheckingWinner\"}",mapper.writeValueAsString(new RaireSolution.RaireResultOrError(new RaireError.TimeoutCheckingWinner())));
        assertEquals("{\"Err\":{\"TimeoutFindingAssertions\":3.0}}",mapper.writeValueAsString(new RaireSolution.RaireResultOrError(new RaireError.TimeoutFindingAssertions(3.0))));
        assertEquals("{\"Err\":\"TimeoutTrimmingAssertions\"}",mapper.writeValueAsString(new RaireSolution.RaireResultOrError(new RaireError.TimeoutTrimmingAssertions())));
        assertEquals("{\"Err\":{\"TiedWinners\":[2,3]}}",mapper.writeValueAsString(new RaireSolution.RaireResultOrError(new RaireError.TiedWinners(new int[]{2,3}))));
        assertEquals("{\"Err\":{\"WrongWinner\":[2,3]}}",mapper.writeValueAsString(new RaireSolution.RaireResultOrError(new RaireError.WrongWinner(new int[]{2,3}))));
        assertEquals("{\"Err\":{\"CouldNotRuleOut\":[2,3]}}",mapper.writeValueAsString(new RaireSolution.RaireResultOrError(new RaireError.CouldNotRuleOut(new int[]{2,3}))));
        assertEquals("{\"Err\":\"InternalErrorRuledOutWinner\"}",mapper.writeValueAsString(new RaireSolution.RaireResultOrError(new RaireError.InternalErrorRuledOutWinner())));
        assertEquals("{\"Err\":\"InternalErrorDidntRuleOutLoser\"}",mapper.writeValueAsString(new RaireSolution.RaireResultOrError(new RaireError.InternalErrorDidntRuleOutLoser())));
        assertEquals("{\"Err\":\"InternalErrorTrimming\"}",mapper.writeValueAsString(new RaireSolution.RaireResultOrError(new RaireError.InternalErrorTrimming())));
    }

    void checkIdempotentDeserializeAndSerializeRaireResultOrError(String json) throws JsonProcessingException {
        RaireSolution.RaireResultOrError deserialized = mapper.readValue(json,RaireSolution.RaireResultOrError.class);
        assertEquals(json,mapper.writeValueAsString(deserialized));
    }
    @Test
    void testErrorDeserialization() throws JsonProcessingException {
        checkIdempotentDeserializeAndSerializeRaireResultOrError("{\"Err\":\"InvalidTimeout\"}");
        checkIdempotentDeserializeAndSerializeRaireResultOrError("{\"Err\":\"InvalidCandidateNumber\"}");
        checkIdempotentDeserializeAndSerializeRaireResultOrError("{\"Err\":\"TimeoutCheckingWinner\"}");
        checkIdempotentDeserializeAndSerializeRaireResultOrError("{\"Err\":{\"TimeoutFindingAssertions\":3.0}}");
        checkIdempotentDeserializeAndSerializeRaireResultOrError("{\"Err\":\"TimeoutTrimmingAssertions\"}");
        checkIdempotentDeserializeAndSerializeRaireResultOrError("{\"Err\":{\"TiedWinners\":[2,3]}}");
        checkIdempotentDeserializeAndSerializeRaireResultOrError("{\"Err\":{\"WrongWinner\":[2,3]}}");
        checkIdempotentDeserializeAndSerializeRaireResultOrError("{\"Err\":{\"CouldNotRuleOut\":[2,3]}}");
        checkIdempotentDeserializeAndSerializeRaireResultOrError("{\"Err\":\"InternalErrorRuledOutWinner\"}");
        checkIdempotentDeserializeAndSerializeRaireResultOrError("{\"Err\":\"InternalErrorDidntRuleOutLoser\"}");
        checkIdempotentDeserializeAndSerializeRaireResultOrError("{\"Err\":\"InternalErrorTrimming\"}");
    }


}
