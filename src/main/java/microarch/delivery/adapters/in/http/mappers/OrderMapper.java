package microarch.delivery.adapters.in.http.mappers;

import microarch.delivery.core.application.queries.dto.OrderDto;
import microarch.delivery.adapters.in.http.model.Order;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, uses = LocationMapper.class, injectionStrategy = InjectionStrategy.CONSTRUCTOR, unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface OrderMapper {

    Order toHttp(OrderDto dto);
}
