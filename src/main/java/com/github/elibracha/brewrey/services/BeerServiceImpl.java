package com.github.elibracha.brewrey.services;

import com.github.elibracha.brewrey.web.dtos.BeerDto;
import com.github.elibracha.brewrey.web.dtos.CustomerDto;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class BeerServiceImpl implements BeerService {
    @Override
    public BeerDto getBeerById(UUID beerId) {
        return BeerDto.builder().id(beerId).build();
    }

    @Override
    public UUID createBeer(BeerDto beer) {
        return UUID.randomUUID();
    }

    @Override
    public UUID updateBeer(UUID id, CustomerDto beer) {
        return UUID.randomUUID();
    }

    @Override
    public void deleteBeer(UUID id) {

    }
}