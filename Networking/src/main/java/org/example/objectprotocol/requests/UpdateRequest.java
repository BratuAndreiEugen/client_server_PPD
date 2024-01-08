package org.example.objectprotocol.requests;

import org.example.Participant;

public class UpdateRequest implements Request {
    private Iterable<Participant> entries;

    private Integer countryId;

    public UpdateRequest(Iterable<Participant> entries, Integer countryId) {
        this.entries = entries;
        this.countryId = countryId;
    }

    public Integer getCountryId() {
        return countryId;
    }

    public void setCountryId(Integer countryId) {
        this.countryId = countryId;
    }

    public Iterable<Participant> getEntries() {
        return entries;
    }

    public void setEntries(Iterable<Participant> entries) {
        this.entries = entries;
    }
}
