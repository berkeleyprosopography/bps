package edu.berkeley.bps.services.common;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import edu.berkeley.bps.services.common.Version;

@Path("/version")
public class VersionResource {
    @GET
    @Produces("application/xml")
    public Version getXML() {
        return Version.getSingleton();
    }

    @GET
    @Produces("application/json")
    public Version getJSON() {
        return Version.getSingleton();
    }
} 

