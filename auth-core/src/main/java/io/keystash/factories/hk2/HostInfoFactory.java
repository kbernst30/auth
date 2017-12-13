package io.keystash.factories.hk2;

import io.keystash.models.web.HostInfo;
import org.glassfish.hk2.api.Factory;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.ws.rs.core.UriInfo;

public class HostInfoFactory implements Factory<HostInfo> {

    private final UriInfo uriInfo;

    @Inject
    public HostInfoFactory(Provider<UriInfo> uriInfoProvider) {
        this.uriInfo = uriInfoProvider.get();
    }

    @Override
    public HostInfo provide() {
        HostInfo hostInfo = new HostInfo();
        hostInfo.setBaseUrl(this.uriInfo.getBaseUri().toString());
        hostInfo.setHostName(this.uriInfo.getBaseUri().getHost());
        return hostInfo;
    }

    @Override
    public void dispose(HostInfo hostInfo) {}
}
