package org.frias.avalon.fabric.convertermasa.factory;

import org.frias.avalon.fabric.convertermasa.factory.strategy.ConvertStrategy;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ConvertFactoryServiceImpl implements ConvertFactoryService {


    private final Map<String, ConvertStrategy> strategies ;

    private final Set<String> unitMasaPesable = Set.of("KG","LB","GR");

    public ConvertFactoryServiceImpl(Map<String, ConvertStrategy> strategies) {

        this.strategies = strategies.entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue()));
    }

    @Override
    public BigDecimal convertTo(String quantity, String unitToConvert, Boolean exit) {

        quantity = quantity.replace(",",".");

        if( !unitMasaPesable.contains(unitToConvert))
            return new BigDecimal(quantity);

        return
                strategies.get(unitToConvert).redyConvertEntry(new BigDecimal(quantity), exit);
    }
}
