
package org.fcrepo.services.functions;

import static com.google.common.base.Preconditions.checkArgument;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.jcr.Repository;

import org.infinispan.Cache;
import org.infinispan.manager.DefaultCacheManager;
import org.modeshape.jcr.JcrRepository;
import org.modeshape.jcr.value.binary.BinaryStore;
import org.modeshape.jcr.value.binary.infinispan.InfinispanBinaryStore;
import org.slf4j.Logger;

import com.google.common.base.Function;

public class GetClusterConfiguration implements
		Function<Repository, Map<String, String>> {

	private static final Logger logger =
			getLogger(GetClusterConfiguration.class);

	public static final String CLUSTER_NAME = "clusterName";
	public static final String CACHE_MODE = "clusterCacheMode";
	public static final String NODE_ADDRESS = "clusterNodeAddress";
	public static final String PHYSICAL_ADDRESS = "clusterPhysicalAddress";
	public static final String NODE_VIEW = "clusterNodeView";
	public static final String CLUSTER_SIZE = "clusterSize";
	public static final String CLUSTER_MEMBERS = "clusterMembers";

	/**
	 * Extract the BinaryStore out of Modeshape (infinspan, jdbc, file, transient, etc)
	 * @return
	 */
	@Override
	public Map<String, String> apply(final Repository input) {
		checkArgument(input != null, "null cannot have a BinaryStore!");

		LinkedHashMap<String, String> result =
				new LinkedHashMap<String, String>();
		try {
			BinaryStore store =
					((JcrRepository) input).getConfiguration()
							.getBinaryStorage().getBinaryStore();
			InfinispanBinaryStore ispnStore = (InfinispanBinaryStore) store;

			//seems like we have to start it, not sure why.
			ispnStore.start();

			List<Cache<?, ?>> caches = ispnStore.getCaches();
			DefaultCacheManager cm =
					(DefaultCacheManager) caches.get(0).getCacheManager();

			if (cm == null) {
				logger.debug("Could not get cluster configuration information");
				return null;
			}

			int nodeView = -1;
			if(cm.getTransport() != null) {
				nodeView = cm.getTransport().getViewId() + 1;
			}

			result.put(CLUSTER_NAME, cm.getClusterName());
			result.put(CACHE_MODE, cm.getCache().getCacheConfiguration()
					.clustering().cacheMode().toString());
			result.put(NODE_ADDRESS, cm.getNodeAddress());
			result.put(PHYSICAL_ADDRESS, cm.getPhysicalAddresses());
			result.put(NODE_VIEW, nodeView == -1 ? "Unknown" : String.valueOf(nodeView));
			result.put(CLUSTER_SIZE, String.valueOf(cm.getClusterSize()));
			result.put(CLUSTER_MEMBERS, cm.getClusterMembers());
			ispnStore.shutdown();
			return result;
		} catch (Exception e) {
			logger.debug("Could not get cluster configuration information");
			return null;
		}
	}

}
