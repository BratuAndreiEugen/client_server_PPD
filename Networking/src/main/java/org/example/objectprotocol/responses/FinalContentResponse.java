package org.example.objectprotocol.responses;

import org.example.Participant;

import java.util.HashMap;

public class FinalContentResponse implements Response{
    private HashMap<Integer, Integer> countryLeaderboardContent;
    private Iterable<Participant> participantLeaderboardContent;

    public FinalContentResponse(HashMap<Integer, Integer> countryLeaderboardContent, Iterable<Participant> participantLeaderboardContent) {
        this.countryLeaderboardContent = countryLeaderboardContent;
        this.participantLeaderboardContent = participantLeaderboardContent;
    }

    public HashMap<Integer, Integer> getCountryLeaderboardContent() {
        return countryLeaderboardContent;
    }

    public void setCountryLeaderboardContent(HashMap<Integer, Integer> countryLeaderboardContent) {
        this.countryLeaderboardContent = countryLeaderboardContent;
    }

    public Iterable<Participant> getParticipantLeaderboardContent() {
        return participantLeaderboardContent;
    }

    public void setParticipantLeaderboardContent(Iterable<Participant> participantLeaderboardContent) {
        this.participantLeaderboardContent = participantLeaderboardContent;
    }
}
