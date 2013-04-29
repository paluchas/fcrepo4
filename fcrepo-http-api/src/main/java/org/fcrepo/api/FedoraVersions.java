package org.fcrepo.api;

import static org.fcrepo.jaxb.responses.management.DatastreamProfile.DatastreamStates.A;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.jcr.ItemNotFoundException;
import javax.jcr.RepositoryException;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.fcrepo.AbstractResource;
import org.fcrepo.Datastream;
import org.fcrepo.FedoraObject;
import org.fcrepo.jaxb.responses.management.DatastreamProfile;
import org.fcrepo.services.DatastreamService;
import org.fcrepo.services.LowLevelStorageService;
import org.fcrepo.services.ObjectService;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Path("/rest/{path: .*}/fcr:versions")
public class FedoraVersions extends AbstractResource{
    private final Logger logger = getLogger(FedoraDatastreams.class);

    @Autowired
    private DatastreamService datastreamService;
    
    @Autowired
    private ObjectService objectService;
    
    @GET
    @Produces({MediaType.TEXT_XML, MediaType.APPLICATION_XML})
    public List<Version> getVersionProfile(@PathParam("path") final List<PathSegment> segments) throws RepositoryException{
    	final String path = toPath(segments);
    	if (path.contains("/fcr:datastreams/")){
    		Datastream ds = datastreamService.getDatastream(path);
  			Version v = new Version(path, ds.getDsId(), ds.getCreatedDate().getTime());
   			return Arrays.asList(v);
    	}
    	FedoraObject obj = objectService.getObject(path);
    	Version v = new Version(path,obj.getName(),new Date().getTime());
    	return Arrays.asList(v);
    }

    @Path("/{id}")
    @GET
    @Produces({MediaType.TEXT_XML, MediaType.APPLICATION_XML})
    public Response getVersion(@PathParam("path") final List<PathSegment> segments, @PathParam("id") final String id) throws RepositoryException,IOException{
    	final String path = toPath(segments);
    	if (path.contains("/fcr:datastreams/")){
    		Datastream ds = datastreamService.getDatastream(path);
    		return Response.ok(getDSProfile(ds)).build();
    	}
    	return Response.ok(objectService.getObjectNames(path, null).toString()).build();
    }
    
    /* duplicate of FedoraDatatstreams.getDSProfile */
    private DatastreamProfile getDSProfile(final Datastream ds)
            throws RepositoryException, IOException {
        logger.trace("Executing getDSProfile() with node: " + ds.getDsId());
        final DatastreamProfile dsProfile = new DatastreamProfile();
        dsProfile.dsID = ds.getDsId();
        dsProfile.pid = ds.getObject().getName();
        logger.trace("Retrieved datastream " + ds.getDsId() + "'s parent: " +
                dsProfile.pid);
        dsProfile.dsLabel = ds.getLabel();
        logger.trace("Retrieved datastream " + ds.getDsId() + "'s label: " +
                ds.getLabel());
        dsProfile.dsOwnerId = ds.getOwnerId();
        dsProfile.dsChecksumType = ds.getContentDigestType();
        dsProfile.dsChecksum = ds.getContentDigest();
        dsProfile.dsState = A;
        dsProfile.dsMIME = ds.getMimeType();
        dsProfile.dsSize = ds.getSize();
        dsProfile.dsCreateDate = ds.getCreatedDate().toString();
        return dsProfile;
    }

    
    /* this is just a inner static to mock the yet non existant versioning model */
    /* TODO: use a real versioning mechanism */
    @XmlRootElement(name="version",namespace="http://example.com/test/version")
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Version {
    	
    	@XmlAttribute(name="path")
    	private String path;
    	@XmlAttribute(name="name")
    	private String name;
    	@XmlAttribute(name="timestamp")
    	private long timestamp;
    	
    	private Version() {
			super();
		}

    	public Version(String path, String versionId, long timestamp) {
			this.name = versionId;
			this.timestamp = timestamp;
			this.path = path;
		}
		
		public String getName() {
			return name;
		}
		
		public long getTimestamp() {
			return timestamp;
		}
    }
}
