package microarch.delivery.adapters.out.grpc;

import java.util.concurrent.TimeUnit;

import clients.geo.GeoGrpc;
import clients.geo.GeoProto;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
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
        this.channel = ManagedChannelBuilder.forAddress(grpc.getHost(), grpc.getPort()).usePlaintext().build();
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
        try {
            var response = stub.withDeadlineAfter(5, TimeUnit.SECONDS).getGeolocation(request);
            var location = response.getLocation();
            return Location.create(location.getX(), location.getY());
        } catch (StatusRuntimeException exception) {
            return Result.failure(Error.of("geo.rpc.failed", "Geo request failed: " + exception.getStatus().getCode()));
        }
    }

}
