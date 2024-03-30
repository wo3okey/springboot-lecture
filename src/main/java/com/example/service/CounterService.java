package com.example.service;

import com.example.domain.entity.Counter;
import com.example.repository.CounterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CounterService {
    private final CounterRepository counterRepository;

    @Transactional
    public void init(int count) {
        Counter counter = counterRepository.findFirstElseThrow();
        counter.initCount(count);
    }

    @Transactional
    public void decrease() {
        Counter counter = counterRepository.findFirstElseThrow();
        counter.decCount();
    }

    @Transactional
    public void decreasePessimistic() {
        Counter counter = counterRepository.findFirstElseThrowPessimistic();
        counter.decCount();
    }

    @Transactional
    public synchronized void decreaseSync() {
        Counter counter = counterRepository.findFirstElseThrow();
        counter.decCount();
    }

    public int count() {
        return counterRepository.findFirstElseThrow().getCount();
    }
}
