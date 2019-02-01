package hcc.janusgraph.example;

import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.Path;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.janusgraph.core.JanusGraph;
import org.janusgraph.core.JanusGraphFactory;
import org.janusgraph.core.schema.JanusGraphManagement;
import org.janusgraph.core.schema.Mapping;
import org.janusgraph.graphdb.database.management.ManagementSystem;
import org.janusgraph.graphdb.relations.CacheEdge;
import org.janusgraph.graphdb.vertices.CacheVertex;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

/**
 * JanusGraph嵌入式示例
 * 作者：HCC（奋斗）
 * 创建时间：2019.02.01
 */
public class JanusGraphExample {

    //图实例
    static JanusGraph graph;

    //管理对象
    static JanusGraphManagement mgmt;

    //遍历源
    static GraphTraversalSource g;

    public static void main(String[] args) throws Exception {

        String path=getFilePath("janusgraph-cql-es.properties").getPath();
        graph= JanusGraphFactory.open(path);
        if (graph != null) {
            JanusGraphFactory.drop(graph);
        }
        graph= JanusGraphFactory.open(path);
        mgmt = graph.openManagement();
        g=graph.traversal();
        screateSchema();//创建Schema
        insert();//插入数据
        query_all();//查询所有
        queryPath("周瑜","孙策",5);//查询周瑜<-->孙策深度5以内的所有关系，忽略重复路径
        g.close();
        graph.close();
    }

    /**
     * 获取文件完整路径
     * @param filePath 相对路径
     * @return 完整路径
     */
    public static URL getFilePath(String filePath){
        return ClassLoader.getSystemResource(filePath);
    }

    /**
     * 创建Schema
     *
     */
    public static void screateSchema() throws InterruptedException {
        mgmt.makePropertyKey("name").dataType(String.class).make();
        mgmt.makeVertexLabel("人物").make();
        mgmt.makeEdgeLabel("关系").make();
        //会在es中创建名为janusgraph_vertex_name_index的索引，索引mapping为下面指定的结构
        mgmt.buildIndex("vertex_name_index", Vertex.class).addKey(mgmt.getPropertyKey("name"), Mapping.STRING.asParameter()).buildMixedIndex("search");
        //会在es中创建名为janusgraph_edge_name_index的索引，索引mapping为下面指定的结构
        mgmt.buildIndex("edge_name_index", Edge.class).addKey(mgmt.getPropertyKey("name"), Mapping.STRING.asParameter()).buildMixedIndex("search");
        mgmt.commit();

        //等待索引状态，阻塞，直到SchemaStatus从INSTALLED转换为REGISTERED
        ManagementSystem.awaitGraphIndexStatus(graph, "vertex_name_index").call();
        //等待索引状态，阻塞，直到SchemaStatus从INSTALLED转换为REGISTERED
        ManagementSystem.awaitGraphIndexStatus(graph, "edge_name_index").call();
    }

    /**
     * 插入数据
     * @throws Exception
     */
    public static void insert() throws Exception{

        List<String> vertexList = Files.readAllLines(Paths.get(getFilePath("data/vertex.csv").toURI()));
        List<String> edgeList = Files.readAllLines(Paths.get(getFilePath("data/edge.csv").toURI()));
        for (String item:vertexList){
            g.addV("人物").property("name",item).next();
        }
        for (String item:edgeList){
            String[] split = item.split(",");
            String start=split[0];
            String edgeName=split[1];
            String end=split[2];
            g.V().has("name",start).addE("关系").property("name",edgeName).to(g.V().has("name",end)).next();
        }
        g.tx().commit();
    }

    /**
     * 查询所有顶点和边
     */
    public static  void query_all(){

        List<Map<Object, Object>> vmaps = g.V().valueMap(true).toList();
        for(Map<Object, Object> item:vmaps){
            System.out.println("label:"+item.get(T.label)+",id:"+item.get(T.id)+",name:"+item.get("name"));
        }

        List<Map<Object, Object>> emaps = g.E().valueMap(true).toList();
        for(Map<Object, Object> item:emaps){
            System.out.println("label:"+item.get(T.label)+",id:"+item.get(T.id)+",name:"+item.get("name"));
        }
    }


    /**
     * 删除所有顶点和边
     */
    public static void remove_all(){
        g.V().drop().iterate();
        g.E().drop().iterate();
        g.tx().commit();
    }


    /**
     * 查询两个顶点之间的关系路径
     * @param startName 起始点
     * @param endName 结束点
     * @param maxDepth 最大深度
     */
    public static void queryPath(String startName,String endName,int maxDepth){
        List<Path> list=g.V().has("name",startName).repeat(__.bothE().otherV().simplePath()).until(__.has("name",endName).and().loops().is(P.lte(maxDepth))).path().toList();
        System.out.println(list.size());
        for (Path p:list){
            p.forEach(item->{
                if(item instanceof CacheVertex){
                    CacheVertex v=(CacheVertex)item;
                    System.out.println("id:"+v.id()+",name:"+v.property("name").value()+",label:"+v.label());
                }
                else if(item instanceof CacheEdge){
                    CacheEdge e=(CacheEdge)item;
                    System.out.println("id:"+e.id()+",name:"+e.property("name").value()+",label:"+e.label()+",start:"+e.outVertex().id()+",end:"+e.inVertex().id());
                }
            });
        }
    }
}
