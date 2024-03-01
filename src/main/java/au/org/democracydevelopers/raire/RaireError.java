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

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;
import java.util.stream.StreamSupport;

/**
 * Everything that could go wrong in Raire. Typically this will be returned as a thrown RaireException with this as its argument.
 *
 * It is implemented as a class rather than an Exception hierarchy to facilitate detailed error serialization.
 **/
@JsonSerialize(using= RaireError.RaireErrorSerializer.class)
@JsonDeserialize(using = RaireError.RaireErrorDeserializer.class)
public abstract class RaireError {
    /** The RAIRE algorithm is given an integer number of candidates. This must be at least one.
     * If a negative or 0 value is provided as the number of candidates, then the InvalidNumberOfCandidates
     * error is generated. Don't get this confused with the InvalidCandidateNumber error below, which is a
     * vote for a candidate who doesn't exist. */
    public static class InvalidNumberOfCandidates extends RaireError {}
    /** The RAIRE algorithm is given a time limit in seconds to limit its runtime.
     * If a negative or NaN value is provided as the time limit, then the InvalidTimout
     * error is generated. */
    public static class InvalidTimeout extends RaireError {}

    /** RAIRE treats votes as a list of integers between 0 (inclusive) and the number of
     * candidates (exclusive). If any vote provided to raire-java, has some other (invalid)
     * integer an InvalidCandidateNumber error is generated. */
    public static class InvalidCandidateNumber extends RaireError {}

    /** There are three stages of computation involved in assertion generation, the
     * first of which is finding out who won (usually very fast). However, if
     * this step exceeds the provided time limit then the TimeoutCheckingWinner error
     * will be generated. All three stages must be completed within the specified time limit
     * or a relevant timeout error will be generated. */
    public static class TimeoutCheckingWinner extends RaireError {}

    /** If assertion generation (usually the slowest of the three stages of computation)
     * does not complete within the specified time limit, the TimeoutFindingAssertions error
     * will be generated. All three stages must be completed within the specified time limit
     * or a relevant timeout error will be generated. */
    public static class TimeoutFindingAssertions extends RaireError { public final double difficultyAtTimeOfStopping;
        public TimeoutFindingAssertions(double difficultyAtTimeOfStopping) {
            this.difficultyAtTimeOfStopping = difficultyAtTimeOfStopping;
        }
    }

    /** After generating assertions, a filtering stage will occur in which redundant
     * assertions are removed from the final set. This stage is usually reasonably fast.
     * However, if this stage does not complete within the specified time limit, the
     * TimeoutTrimmingAssertions error will be generated. All three stages must be completed
     * within the specified time limit or a relevant timeout error will be generated.*/
    public static class TimeoutTrimmingAssertions extends RaireError {}

    /** If RAIRE determines that the contest has multiple possible winners consistent with
     * the rules of IRV (i.e. there is a tie) then the TiedWinners error will be generated.
     * While the particular legislation governing the contest may have unambiguous tie
     * resolution rules, there is no way that an RLA could be helpful if the contest comes
     * down to a tie resolution. */
    public static class TiedWinners extends RaireError { public final int[] expected;
        public TiedWinners(int[] expected) {
            this.expected = expected;
        }
    }

    /** If RAIRE is called with a specified winner, and upon checking RAIRE determines
     * that the provided winner does not match the votes (according to its own tabulation),
     * the WrongWinner error will be generated. */
    public static class WrongWinner extends RaireError { public final int[] expected;
        public WrongWinner(int[] expected) {
            this.expected = expected;
        }
    }

    /** If RAIRE determines that it is not possible to compute a set of assertions because
     * there are no assertions that would rule out a particular elimination order, then a
     * CouldNotRuleOut error is generated. */
    public static class CouldNotRuleOut extends RaireError { public final int[] eliminationOrder;
        public CouldNotRuleOut(int[] eliminationOrder) {
            this.eliminationOrder = eliminationOrder;
        }
    }

    /** Sanity checks are conducted in various locations in raire-java to ensure that
     * the code is operating as intended. An InternalErrorRuledOutWinner error will be
     * generated if the set of generated assertions actually rule out the reported winner
     * (i.e. the assertions are invalid). */
    public static class InternalErrorRuledOutWinner extends RaireError {}

    /** Sanity checks are conducted in various locations in raire-java to ensure that
     * the code is operating as intended. An InternalErrorDidntRuleOutLoser error is
     * generated if the set of assertions formed does not rule out at least one
     * reported loser (i.e. the assertions are invalid). */
    public static class InternalErrorDidntRuleOutLoser extends RaireError {}

    /** Sanity checks are conducted in various locations in raire-java to ensure that
     * the code is operating as intended. An InternalErrorTrimming error is generated
     * if a problem has arisen during the filtering of redundant assertions. */
    public static class InternalErrorTrimming extends RaireError {}


    /** Custom JSON serializer for Jackson */
    public static class RaireErrorSerializer extends StdSerializer<RaireError> {

        public RaireErrorSerializer() { this(null); }
        public RaireErrorSerializer(Class<RaireError> t) { super(t); }

        private void writeIntArray(JsonGenerator jsonGenerator,String fieldName,int[]array) throws IOException {
            jsonGenerator.writeFieldName(fieldName);
            jsonGenerator.writeArray(array,0,array.length);
        }

        @Override
        public void serialize(RaireError raireError, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
            // first consider the errors that are serialized as a simple string
            if (raireError instanceof InvalidTimeout) jsonGenerator.writeString("InvalidTimeout");
            else if (raireError instanceof InvalidNumberOfCandidates) jsonGenerator.writeString("InvalidNumberOfCandidates");
            else if (raireError instanceof InvalidCandidateNumber) jsonGenerator.writeString("InvalidCandidateNumber");
            else if (raireError instanceof TimeoutCheckingWinner) jsonGenerator.writeString("TimeoutCheckingWinner");
            else if (raireError instanceof TimeoutTrimmingAssertions) jsonGenerator.writeString("TimeoutTrimmingAssertions");
            else if (raireError instanceof InternalErrorRuledOutWinner) jsonGenerator.writeString("InternalErrorRuledOutWinner");
            else if (raireError instanceof InternalErrorDidntRuleOutLoser) jsonGenerator.writeString("InternalErrorDidntRuleOutLoser");
            else if (raireError instanceof InternalErrorTrimming) jsonGenerator.writeString("InternalErrorTrimming");
            else {
                // the remaining errors are serialized as an object with one field (the name of the error) and one value (the parameter in it).
                jsonGenerator.writeStartObject();
                if (raireError instanceof TimeoutFindingAssertions) jsonGenerator.writeNumberField("TimeoutFindingAssertions",((TimeoutFindingAssertions) raireError).difficultyAtTimeOfStopping);
                else if (raireError instanceof TiedWinners) writeIntArray(jsonGenerator,"TiedWinners", ((TiedWinners) raireError).expected);
                else if (raireError instanceof WrongWinner) writeIntArray(jsonGenerator,"WrongWinner", ((WrongWinner) raireError).expected);
                else if (raireError instanceof CouldNotRuleOut) writeIntArray(jsonGenerator,"CouldNotRuleOut", ((CouldNotRuleOut) raireError).eliminationOrder);
                else throw new IOException("Do not understand RaireError "+raireError);
                jsonGenerator.writeEndObject();
            }
        }
    }

    public static class RaireErrorDeserializer extends StdDeserializer<RaireError> {
        public RaireErrorDeserializer() { this(null); }
        public RaireErrorDeserializer(Class<?> vc) { super(vc); }

        private int[] getIntArray(JsonNode node) {
            return StreamSupport.stream(node.spliterator(), false).mapToInt(JsonNode::asInt).toArray();
        }
        @Override
        public RaireError deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
            JsonNode node = jsonParser.getCodec().readTree(jsonParser);
            if (node.isTextual()) {
                String text = node.asText();
                switch (text) {
                    case "InvalidTimeout" : return new RaireError.InvalidTimeout();
                    case "InvalidNumberOfCandidates" : return new RaireError.InvalidNumberOfCandidates();
                    case "InvalidCandidateNumber" : return new RaireError.InvalidCandidateNumber();
                    case "TimeoutCheckingWinner" : return new RaireError.TimeoutCheckingWinner();
                    case "TimeoutTrimmingAssertions" : return new RaireError.TimeoutTrimmingAssertions();
                    case "InternalErrorRuledOutWinner" : return new RaireError.InternalErrorRuledOutWinner();
                    case "InternalErrorDidntRuleOutLoser" : return new RaireError.InternalErrorDidntRuleOutLoser();
                    case "InternalErrorTrimming" : return new RaireError.InternalErrorTrimming();
                }
            } else if (node.isObject()) {
                if (node.has("TimeoutFindingAssertions")) return new RaireError.TimeoutFindingAssertions(node.get("TimeoutFindingAssertions").doubleValue());
                else if (node.has("TiedWinners")) return new RaireError.TiedWinners(getIntArray(node.get("TiedWinners")));
                else if (node.has("WrongWinner")) return new RaireError.WrongWinner(getIntArray(node.get("WrongWinner")));
                else if (node.has("CouldNotRuleOut")) return new RaireError.CouldNotRuleOut(getIntArray(node.get("CouldNotRuleOut")));
            }
            throw new IOException("Could not understand "+node);
        }
    }
}
