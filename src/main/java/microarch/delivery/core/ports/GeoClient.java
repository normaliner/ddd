package microarch.delivery.core.ports;

import libs.errs.Result;
import microarch.delivery.core.domain.model.Location;
import libs.errs.Error;

public interface GeoClient {
    Result<Location, Error> getGeoLocationByStreet(String street);

}
