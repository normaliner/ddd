package microarch.delivery.adapters.out.grpc;

import clients.geo.GeoGrpc;
import clients.geo.GeoProto;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import jakarta.annotation.PreDestroy;
import libs.errs.Result;
import microarch.delivery.ApplicationProperties;
import microarch.delivery.core.domain.model.Location;
import microarch.delivery.core.ports.GeoClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import libs.errs.Error;

@Component
public class GeoClientImpl implements GeoClient {
    private final ManagedChannel channel;
    private final GeoGrpc.GeoBlockingStub stub;

    @Autowired
    public GeoClientImpl(ApplicationProperties properties) {
        var grpc = properties.getGrpc().getGeoService();
        this.channel = ManagedChannelBuilder.forAddress(
                grpc.getHost(), grpc.getPort()).usePlaintext().build();
        this.stub = GeoGrpc.newBlockingStub(channel);
    }

    @PreDestroy
    public void shutdown() {
        if (!channel.isShutdown()) {
            channel.shutdown();
        }
    }


    @Override
    public Result<Location, Error> getGeoLocationByStreet(String street) {

        var request = GeoProto.GetGeolocationRequest.newBuilder().setStreet(street).build();
        var response = stub.getGeolocation(request);

        var location = response.getLocation();
        var locationResult = Location.create(location.getX(), location.getY());

        if (locationResult.isFailure()) {
            return Result.failure(locationResult.getError());
        }

        return Result.success(locationResult.getValue());
    }

}
