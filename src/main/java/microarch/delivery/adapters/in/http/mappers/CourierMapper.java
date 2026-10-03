package microarch.delivery.adapters.in.http.mappers;

import microarch.delivery.adapters.in.http.model.Courier;
import microarch.delivery.core.application.queries.dto.CourierDto;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, uses = LocationMapper.class, injectionStrategy = InjectionStrategy.CONSTRUCTOR, unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface CourierMapper {

    Courier toHttp(CourierDto dto);

}
