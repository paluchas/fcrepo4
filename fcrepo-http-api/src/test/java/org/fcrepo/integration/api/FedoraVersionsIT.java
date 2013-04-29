package org.fcrepo.integration.api;

import static java.util.regex.Pattern.DOTALL;
import static java.util.regex.Pattern.compile;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.junit.Test;

public class FedoraVersionsIT extends AbstractResourceIT {
	
	@Test
	public void testGetObjectVersionProfile() throws Exception {
		execute(postObjMethod("FedoraDatastreamsTest1"));
		final HttpGet method = new HttpGet(serverAddress +
				"objects/FedoraDatastreamsTest1/fcr:versions");
		HttpResponse resp = execute(method);
		assertEquals(200, resp.getStatusLine().getStatusCode());
	}

	@Test
	public void testGetDatastreamVersionProfile() throws Exception {
		execute(postObjMethod("FedoraDatastreamsTest1"));

		HttpPost postDs = postDSMethod("FedoraDatastreamsTest1","ds1","foo");
		execute(postDs);
		assertEquals(201, getStatus(postDs));
		
		final HttpGet getVersion = new HttpGet(serverAddress +
				"objects/FedoraDatastreamsTest1/ds1/fcr:versions");
		HttpResponse resp =execute(getVersion);
		assertEquals(200, resp.getStatusLine().getStatusCode());
	}
	
	@Test
	public void testGetDatastreamVersion() throws Exception {
		execute(postObjMethod("FedoraDatastreamsTest1"));

		HttpPost postDs = postDSMethod("FedoraDatastreamsTest1","ds1","foo");
		execute(postDs);
		assertEquals(201, getStatus(postDs));
		
		final HttpGet getVersion = new HttpGet(serverAddress +
				"objects/FedoraDatastreamsTest1/ds1/fcr:versions/lastVersion");
		HttpResponse resp =execute(getVersion);
		assertEquals(200, resp.getStatusLine().getStatusCode());
	}
}
