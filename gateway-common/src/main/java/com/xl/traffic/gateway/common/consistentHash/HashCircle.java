package com.xl.traffic.gateway.common.consistentHash;

import com.xl.traffic.gateway.common.utils.MD5;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 一致性hash
 *
 * @author xuliang
 */
public class HashCircle {

    private static final int numberOfReplicas = 64;

    private static final int circleNodes = 10240;

    /**
     * 用于存储不同类型的集群对应的哈希环 例如 : /chat/cluster_tob -> circle    /room/cluster_tob -> circle
     */
    private Map<String, Character[]> maps = new ConcurrentHashMap<>(5);

    /**
     * 用于存储不同类型的集群对应的 serverIP 和 key 映射关系  例如 : /chat/cluster_tob ->  1-192.168.1.100  , 2-192.168.1.200
     */
    private final Map<String, Map<Character, String>> keyMaps = new ConcurrentHashMap<>(5);


    private HashCircle() {

    }

    private static class InstanceHolder {
        public static final HashCircle instance = new HashCircle();
    }

    public static HashCircle getInstance() {
        return InstanceHolder.instance;
    }



    /**
     * 集群节点变动需要调用此方法重置哈希环
     *
     * @param clusterPath 集群zk路径
     * @param serverList  集群机器列表 使用,分割
     */
    public synchronized void init(String clusterPath, String serverList) {

        if (StringUtils.isEmpty(serverList)) {
            return;
        }


        Character[] circle = new Character[circleNodes];

        String[] servers = serverList.split(",");

        ArrayList<String> totalNodes = new ArrayList<>();


        for (String server : servers) {
            if (StringUtils.isEmpty(server)) {
                continue;
            }
            /**
             * 每个服务节点生成 numberOfReplicas 个虚拟节点
             */
            List<String> virtualNodes = generateVirtualNodes(server);
            totalNodes.addAll(virtualNodes);
        }

        Map<Character, String> keyServerIpMap = hashNodesToCircle(totalNodes, circle);


        // 更新之前的哈希环
        maps.put(clusterPath, circle);
        // 更新keyMap
        keyMaps.put(clusterPath, keyServerIpMap);

    }


    /**
     * 根据clusterPath 和 key 获取映射后的结点
     *
     * @param clusterPath 集群zk路径
     * @param key         hashkey
     * @return
     */
    public String get(String clusterPath, String key) {
        Character[] circle = maps.get(clusterPath);
        if (circle == null || circle.length == 0) {
            return null;
        }

        String crypt = MD5.crypt(key);
        int index = Math.abs(crypt.hashCode()) % circleNodes;

        for (int i = index; i < circle.length; i++) {
            Character selectNode = circle[i];
            if (selectNode != null) {
                //keyMaps.get()
                return keyMaps.get(clusterPath).get(selectNode);
            }
        }
        for (int j = 0; j < index; j++) {
            Character selectNode = circle[j];
            if (selectNode != null) {
                return keyMaps.get(clusterPath).get(selectNode);
            }
        }
        return null;
    }


    private static Map<Character, String> hashNodesToCircle(ArrayList<String> totalNodes, Character[] circle) {

        HashMap<String, Character> map = new HashMap<>();// serverIp - num

        char num = 0;
        for (String node : totalNodes) {
            String crypt = MD5.crypt(node);
            int abs = Math.abs(crypt.hashCode());
            int index = (int) (abs % circleNodes);
            String[] split = node.split("#");
            Character character = map.get(split[0]);
            if (character == null) {
                character = num++;
                map.put(split[0], character);
            }
            circle[index] = character;
        }

        HashMap<Character, String> map1 = new HashMap<>();
        for (String serverIp : map.keySet()) {
            map1.put(map.get(serverIp), serverIp);
        }
        return map1;
    }

    private static List<String> generateVirtualNodes(String server) {
        ArrayList<String> objects = new ArrayList<>(numberOfReplicas);
        for (int i = 0; i < numberOfReplicas; i++) {
            objects.add(server + "#" + i);
        }
        return objects;
    }

//
//    public static void main(String[] args) {
//
//
//        HashCircle instance = HashCircle.getInstance();
//
//        instance.init("/chat/cluster","127.0.0.1,127.0.0.2,127.0.0.3,127.0.0.4");
//
//
//        HashMap<String, String> map = new HashMap<>();
//
//        for(int i=0;i<50000;i++){
//            String node = "test_" + i + "node";
//            String result = instance.get("/chat/cluster", node);
//            map.put(node,result);
//        }
//
//        Map<String, List<String>> collect = map.values().stream().collect(Collectors.groupingBy(String::intern));
//
//        // 移除节点
//        instance.init("/chat/cluster","127.0.0.2,127.0.0.3,127.0.0.4");
//
//        int num = 0;
//        HashMap<String, String> map3 = new HashMap<>();
//        for(int i=0;i<50000;i++){
//            String node = "test_" + i + "node";
//            String result = instance.get("/chat/cluster", node);
//            map3.put(node,result);
//            if(!map.get(node).equals(result)){
//                num ++;
//            }
//        }
//
//        Map<String, List<String>> collect3 = map3.values().stream().collect(Collectors.groupingBy(String::intern));
//        System.out.println(num);
//
//        // 添加节点
//        instance.init("/chat/cluster","127.0.0.1,127.0.0.2,127.0.0.3,127.0.0.4,127.0.0.5");
//
//        num = 0;
//        HashMap<String, String> map5 = new HashMap<>();
//        for(int i=0;i<50000;i++){
//            String node = "test_" + i + "node";
//            String result = instance.get("/chat/cluster", node);
//            map5.put(node,result);
//            if(!map.get(node).equals(result)){
//                num ++;
//            }
//        }
//        Map<String, List<String>> collect5 = map5.values().stream().collect(Collectors.groupingBy(String::intern));
//        System.out.println(num);
//
//
//    }


}
