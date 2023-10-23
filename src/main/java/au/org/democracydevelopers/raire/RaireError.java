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

import com.fasterxml.jackson.core.JacksonException;
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
    public static class InvalidTimeout extends RaireError {}
    public static class InvalidCandidateNumber extends RaireError {}
    public static class TimeoutCheckingWinner extends RaireError {}
    public static class TimeoutFindingAssertions extends RaireError { final double difficultyAtTimeOfStopping;
        public TimeoutFindingAssertions(double difficultyAtTimeOfStopping) {
            this.difficultyAtTimeOfStopping = difficultyAtTimeOfStopping;
        }
    }
    public static class TimeoutTrimmingAssertions extends RaireError {}
    public static class TiedWinners extends RaireError { final int[] expected;
        public TiedWinners(int[] expected) {
            this.expected = expected;
        }
    }
    public static class WrongWinner extends RaireError { final int[] expected;
        public WrongWinner(int[] expected) {
            this.expected = expected;
        }
    }
    public static class CouldNotRuleOut extends RaireError { final int[] eliminationOrder;
        public CouldNotRuleOut(int[] eliminationOrder) {
            this.eliminationOrder = eliminationOrder;
        }
    }
    public static class InternalErrorRuledOutWinner extends RaireError {}
    public static class InternalErrorDidntRuleOutLoser extends RaireError {}
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
