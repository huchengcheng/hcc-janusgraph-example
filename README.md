JanusGraph是基于TinkerPop的基础实现，本身是一组没有执行线程的JAR包。
服务部署和嵌入在应用程序中使用，多个JanusGraph实例之间都不互相通信，这和cassandra、elasticsearch这种分布式有点不同。
JanusGraph集成hadoop spark，主要应用于离线关系分析（OLAP）。
JanusGraph集成cassandra、elasticsearch，主要应用于实时关系查询（OLTP）。
具体使用哪种场景根据自身需要确定。

环境要求：
JanusGraph version:0.3.1
cassandra version:2.1.20
elasticsearch version:5.6.5

启动cassandra
cd /home/janusgraph3
su hcc
bin/cassandra

启动elasticsearch
elasticsearch/bin/elasticsearch
不行就RPM包安装一个

在实际生产环境cassandra和elasticsearch可以分布式部署。

下面使用三国演义中的人物关系数据演示嵌入式使用方法
示例包括：
1.打开图，如果图不存在，系统会自动创建一个图实例
2.删除图
3.创建Schema
4.使用ES索引属性
5.插入数据
6.遍历所有顶点、边
7.查询两个人物之间的的N度以内的关系
