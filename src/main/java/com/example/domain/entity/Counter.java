package com.example.domain.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Version;

@Entity
@Table(name = "counter")
@Getter
@NoArgsConstructor()
public class Counter {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "count")
    protected int count;

    @Version
    @Column(name = "version")
    private int version;

    public void decCount() {
        count--;
    }

    public void initCount(int count) {
        this.count = count;
    }
}
