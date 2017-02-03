package com.mode.presenter;

import com.mode.model.Sample;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;


public class IndexPresenterTest {

    @Test
    public void getGraphData() throws Exception {
        List<Sample> samples = new ArrayList<>();
        samples.add(Sample.builder().addedOn(new Date(1000L)).polarityAverage(3.1).build());
        samples.add(Sample.builder().addedOn(new Date(2000L)).polarityAverage(4.2).build());
        IndexPresenter subject = new IndexPresenter(samples);
        assertEquals("[{x:1000,y:3.1},{x:2000,y:4.2}]", subject.getGraphData());
    }

}