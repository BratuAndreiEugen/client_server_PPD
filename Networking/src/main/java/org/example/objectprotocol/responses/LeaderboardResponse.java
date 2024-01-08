package org.example.objectprotocol.responses;


import org.example.Country;

import java.util.HashMap;

public class LeaderboardResponse implements Response {
    private final HashMap<Integer, Integer> scores;

    public LeaderboardResponse(HashMap<Integer, Integer> scores) {
        this.scores = scores;
    }

    public HashMap<Integer, Integer> getScores() {
        return scores;
    }
}
