/*
  Copyright 2023 Democracy Developers
  This is a Java re-implementation of raire-rs https://github.com/DemocracyDevelopers/raire-rs
  It attempts to copy the design, API, and naming as much as possible subject to being idiomatic and efficient Java.

  This file is part of raire-java.
  raire-java is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
  raire-java is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License for more details.
  You should have received a copy of the GNU Affero General Public License along with ConcreteSTV.  If not, see <https://www.gnu.org/licenses/>.

 */

package au.org.democracydevelopers.raire.util;


import au.org.democracydevelopers.raire.irv.Vote;

import java.util.Arrays;
import java.util.HashMap;

/**
 * A utility class for building an array of Vote[] structures
 * from provided preference lists. The main purpose is to convert
 * a large number of weight votes, possibly the same, into a
 * set of unique votes with multiplicities.
 *
 * It is also (optionally) capable of converting a preference list of
 * strings into the array of integer preferences used by Raire.
 */
public class VoteConsolidator {
    /** The map from candidate names to indices. The argument should never be null */
    private final HashMap<String,Integer> candidateNameToIndex = new HashMap<>();

    /** The thing being built up. The key is a preference list, the argument is a non-null multiplicity */
    private final HashMap<HashableIntArray,Integer> multiplicityByPreferenceList = new HashMap<>();

    /** Use this constructor if you are providing preference lists as an array of integers */
    public VoteConsolidator() {}
    /** Use this constructor if you are providing preference lists as an array of integers */
    public VoteConsolidator(String[] candidateNames) {
        for (int i=0;i<candidateNames.length;i++) candidateNameToIndex.put(candidateNames[i],i);
    }

    /** Call addVote({0,5,2}) to add a vote first for candidate 0, second for candidate 5, third for candidate 2 */
    public void addVote(int []preferences) {
        HashableIntArray key = new HashableIntArray(preferences);
        multiplicityByPreferenceList.put(key,multiplicityByPreferenceList.getOrDefault(key,0)+1);
    }

    private int candidateIndex(String candidateName) throws InvalidCandidateName {
        Integer res = candidateNameToIndex.get(candidateName);
        if (res==null) throw new InvalidCandidateName(candidateName);
        return res;
    }

    /**
     * Call addVote({"A","B","C"}) to add a vote first for candidate A, second for candidate B, third for candidate C.
     * Uses the order given in the VoteConsolidator(String[] candidateNames) constructor. */
    public void addVoteNames(String[] preferences) throws InvalidCandidateName {
        int [] intPreferences = Arrays.stream(preferences).mapToInt(this::candidateIndex).toArray();
        addVote(intPreferences);
    }

    /** Get the votes with appropriate multiplicities */
    public Vote[] getVotes() {
        return multiplicityByPreferenceList.entrySet().stream().map((entry)->new Vote(entry.getValue(),entry.getKey().array)).toArray(Vote[]::new);
    }

    /** An error indicating that the provided name was not a listed candidate */
    public static class InvalidCandidateName extends IllegalArgumentException {
        public final String candidateName;
        public InvalidCandidateName(String candidateName) {
            super("Candidate "+candidateName+" was not on the list of candidates");
            this.candidateName = candidateName;
        }
    }

    /** A wrapper around int[] that works as a key in a hash map */
    private static class HashableIntArray {
        final private int[] array;
        private HashableIntArray(int[] array) { this.array = array; }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            HashableIntArray that = (HashableIntArray) o;
            return Arrays.equals(array, that.array);
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(array);
        }
    }
}
