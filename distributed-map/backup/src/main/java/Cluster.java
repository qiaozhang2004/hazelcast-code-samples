import static com.hazelcast.examples.helper.CommonUtils.sleepSeconds;

import java.lang.management.ManagementFactory;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

public class Cluster {

    public static void main(String[] args) {
        System.setProperty("hazelcast.jmx", "true");

        Hazelcast.newHazelcastInstance();
        Hazelcast.newHazelcastInstance();
        Hazelcast.newHazelcastInstance();
        HazelcastInstance hz = Hazelcast.newHazelcastInstance();
        System.out.println("Hazelcast cluster was started. Number of members: " + hz.getCluster().getMembers().size());

        IMap<String, String> map = hz.getMap("cities");
        map.put("where to live", "Brno");
        map.put("where to have a joy", "Paris");
        map.put("where to go next", "London");
        System.out.println("Entries were added to 'cities' IMap, current size is: " + map.size());

        // time-to-live is set on the 'cities' map, let's do some statistics over the time
        for (int i = 0; i < 15; i++) {
            sleepSeconds(2);
            printMapStatistics();
        }

        Hazelcast.shutdownAll();
    }

    private static void printMapStatistics() {
        try {
            MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
            long ownedEntrieSum = 0;
            long backupEntrieSum = 0;
            for (ObjectName obj : mBeanServer.queryNames(null,
                    ObjectName.getInstance("com.hazelcast:type=IMap,name=cities,*"))) {
                ownedEntrieSum += (Long) mBeanServer.getAttribute(obj, "localOwnedEntryCount");
                backupEntrieSum += (Long) mBeanServer.getAttribute(obj, "localBackupEntryCount");
            }
            System.out.println(String.format("Count of owned IMap entries across cluster: %d, backup entries: %d",
                    ownedEntrieSum, backupEntrieSum));
        } catch (Exception e) {
            System.out.println("Getting Map statistics over JMX failed: " + e.getMessage());
        }
    }

}
