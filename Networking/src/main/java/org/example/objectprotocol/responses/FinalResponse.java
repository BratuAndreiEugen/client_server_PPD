package org.example.objectprotocol.responses;

import java.io.File;

public class FinalResponse implements Response {
    private final File perCountryLeaderboard;
    private final File perParticipantLeaderboard;

    public FinalResponse(File perCountryLeaderboard, File perParticipantLeaderboard) {
        this.perCountryLeaderboard = perCountryLeaderboard;
        this.perParticipantLeaderboard = perParticipantLeaderboard;
    }

    public File getPerCountryLeaderboard() {
        return perCountryLeaderboard;
    }

    public File getPerParticipantLeaderboard() {
        return perParticipantLeaderboard;
    }
}
