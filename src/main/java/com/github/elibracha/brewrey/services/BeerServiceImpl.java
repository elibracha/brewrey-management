package com.github.elibracha.brewrey.services;

import com.github.elibracha.brewrey.config.JmsConfig;
import com.github.elibracha.brewrey.providers.ExceptionProvider;
import com.github.elibracha.brewrey.repositories.BeerRepository;
import com.github.elibracha.brewrey.web.dtos.BeerDto;
import com.github.elibracha.brewrey.web.mappers.BeerMapper;
import lombok.AllArgsConstructor;
import lombok.val;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class BeerServiceImpl implements BeerService {

    private BeerRepository beerRepository;
    private JmsTemplate jmsTemplate;
    private BeerMapper mapper;

    @Override
    @Cacheable(cacheNames = "BeerListCache")
    public List<BeerDto> getBeers(int page, int size) {
        return beerRepository.findAll(PageRequest.of(page, size, Sort.by("beerName").descending()))
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Cacheable(cacheNames = "BeerCache", key = "#beerId")
    public BeerDto getBeerById(UUID beerId) {
        val beer = beerRepository.findById(beerId).orElseThrow(ExceptionProvider.ENTITY_NOT_FOUND_ERROR_SUPPLER);
        return mapper.toDto(beer);
    }

    @Override
    public UUID createBeer(BeerDto beerDto) {
        beerRepository.findByUpc(beerDto.getUpc()).ifPresent(ExceptionProvider.UPC_FOUND_ERROR_CONSUMER);
        val beer = beerRepository.save(mapper.fromDto(beerDto));

        val dto = mapper.toDto(beer);

        jmsTemplate.convertAndSend(JmsConfig.QUEUE, mapper.fromDtoToMessage(dto));
        return beer.getId();
    }

    @Override
    public UUID updateBeer(UUID beerId, BeerDto beerDto) {
        val beer = beerRepository.findById(beerId).orElseThrow(ExceptionProvider.ENTITY_NOT_FOUND_ERROR_SUPPLER);

        mapper.merge(beer, beerDto);
        beerRepository.save(beer);

        val dto = mapper.toDto(beer);

        jmsTemplate.convertAndSend(JmsConfig.QUEUE, mapper.fromDtoToMessage(dto));
        return beer.getId();
    }

    @Override
    public void deleteBeer(UUID beerId) {
        val beer = beerRepository.findById(beerId).orElseThrow(ExceptionProvider.ENTITY_NOT_FOUND_ERROR_SUPPLER);
        beerRepository.delete(beer);
    }
}
