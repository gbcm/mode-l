package com.mode.model;

import lombok.Builder;
import lombok.Data;
import org.hibernate.annotations.Generated;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.util.Date;

@Data
@Builder
@Entity
public class Sample {
    @Id
    @GeneratedValue
    private Long id;

    private Date addedOn;

    private int polarityAverage;
}
