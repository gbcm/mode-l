package com.mode.presenter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mode.model.Sample;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Data
public class IndexPresenter {
    private List<Sample> samples;

    private String graphData;

    public IndexPresenter(Collection<Sample> samples) {
        this.samples = new ArrayList<>(samples);
        List<DataPoint> dataPoints = new ArrayList<>();
        for (Sample s : samples) {
            dataPoints.add(new DataPoint(s.getAddedOn().getTime(),s.getPolarityAverage()));
        }
        Gson gson = new GsonBuilder().create();
        DataPoint[] dataPointsArray = dataPoints.toArray(new DataPoint[]{});
        graphData = gson.toJson(dataPointsArray);
    }

    @Data
    @AllArgsConstructor
    private class DataPoint{
        private long x;
        private double y;
    }
}
