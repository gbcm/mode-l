package com.mode.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SentimentResponse {
    private SentimentalTweet[] data;

    @Override
    public String toString() {
        String ret = "<html><body>";
        for (SentimentalTweet st : data) {
            switch (st.getPolarity()) {
                case 0:
                    ret += "<p style=\"color: red;\">";
                    break;
                case 2:
                    ret += "<p style=\"color: blue;\">";
                    break;
                case 4:
                    ret += "<p style=\"color: green;\">";
                    break;
            }
            ret += st.getText() + "</p>";
        }
        ret += "</body></html>";
        return ret;
    }

    public double getAdjustedPolarityAverage() {
        double ret = 0;
        for (SentimentalTweet st : data) {
            ret += (st.getPolarity() - 2);
        }
        return ret / data.length;
    }
}
