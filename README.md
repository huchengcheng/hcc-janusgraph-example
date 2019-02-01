####环境要求：
 JanusGraph version:0.3.1  
 cassandra version:2.1.20  
 elasticsearch version:5.6.5

####启动cassandra
cd /home/janusgraph3  
su test  
bin/cassandra  

####启动elasticsearch
elasticsearch/bin/elasticsearch  
不行就RPM包安装一个  

在实际生产环境cassandra和elasticsearch可以分布式部署

####下面演示嵌入式使用方法
1. 打开图，如果图不存在，系统会自动创建一个图实例
2. 删除图
3. 创建Schema
4. 使用ES索引属性
5. 插入数据
6. 遍历所有顶点、边
7. 查询两个人物之间的的N度以内的关系