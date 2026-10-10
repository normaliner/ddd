package microarch.delivery.adapters.in.http.mappers;

import microarch.delivery.adapters.in.http.model.Location;
import microarch.delivery.core.application.queries.dto.LocationDto;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface LocationMapper {

    Location toHttp(LocationDto dto);

}
