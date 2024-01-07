package org.example;

import java.io.Serializable;
import java.util.Objects;

import static java.lang.Integer.signum;

public class Country implements Serializable, Comparable<Country> {
    private Integer id;

    private Integer score;

    public Country(Integer id, Integer score) {
        this.id = id;
        this.score = score;
    }

    @Override
    public int compareTo(Country o) {
        int res = signum(this.score.compareTo(o.getScore()));
        if (res == 0)
            return signum(this.id.compareTo(o.getId()));

        return res;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Country that = (Country) o;
        return Objects.equals(this.id, that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }
}
