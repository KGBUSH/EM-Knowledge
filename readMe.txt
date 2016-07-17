1.Neo4j 使用：
 1，下载neo4j-community-2.3.2-unix.tar.gz  解压
 2，conf下neo4j-server.properties文件中，把org.neo4j.server.webserver.address=0.0.0.0这行前面的注释#号去了。（插入节点数据量加大的时候，修改neo4j-wrapper.conf中wrapper.java.initmemory=2000，wrapper.java.maxmemory=3000，加大这两个配置项的值，能够加快查询速度）
 3，cd bin; 然后运行./neo4j start  这时候通过http://ip:7474就能访问neo4j页面了（ 常用命令1：查询关系数量match (n)-[r]->(m) return count(r); 2：查询节点数量：
  match(n)  return count(n);）

其他说明：
为了提高查找性能和插入性能，我们需要对数据库里面的节点，对不同标签的一些具体字段构建索引，目前主要对不同的类别的Name,urlkey,key这几个字段构建索引；
Name，实体名字；urlkry:url的md5,key:百科页面前两段内容的md5
构建索引的语句如下：
CREATE INDEX ON :college(Name);
CREATE INDEX ON :college(key);
CREATE INDEX ON :college(urlkey);
CREATE INDEX ON :tv(Name);
CREATE INDEX ON :tv(key);
CREATE INDEX ON :tv(urlkey);
CREATE INDEX ON :figure(Name);
CREATE INDEX ON :figure(key);
CREATE INDEX ON :figure(urlkey);
CREATE INDEX ON :movie(Name);
CREATE INDEX ON :movie(key);
CREATE INDEX ON :movie(urlkey);
CREATE INDEX ON :sports(Name);
CREATE INDEX ON :sports(key);
CREATE INDEX ON :sports(urlkey);
CREATE INDEX ON :festival(Name);
CREATE INDEX ON :festival(key);
CREATE INDEX ON :festival(urlkey);
CREATE INDEX ON :tourism(Name);
CREATE INDEX ON :tourism(key);
CREATE INDEX ON :tourism(urlkey);
CREATE INDEX ON :technology(Name);
CREATE INDEX ON :technology(key);
CREATE INDEX ON :technology(urlkey);
CREATE INDEX ON :economy(Name);
CREATE INDEX ON :economy(key);
CREATE INDEX ON :economy(urlkey);
CREATE INDEX ON :cosmetics(Name);
CREATE INDEX ON :cosmetics(key);
CREATE INDEX ON :cosmetics(urlkey);
CREATE INDEX ON :music(Name);
CREATE INDEX ON :music(key);
CREATE INDEX ON :music(urlkey);
CREATE INDEX ON :major(Name);
CREATE INDEX ON :major(key);
CREATE INDEX ON :major(urlkey);
CREATE INDEX ON :medical_treatment(Name);
CREATE INDEX ON :medical_treatment(key);
CREATE INDEX ON :medical_treatment(urlkey);
CREATE INDEX ON :car(Name);
CREATE INDEX ON :car(key);
CREATE INDEX ON :car(urlkey);
CREATE INDEX ON :science(Name);
CREATE INDEX ON :science(key);
CREATE INDEX ON :science(urlkey);
CREATE INDEX ON :plant(Name);
CREATE INDEX ON :plant(key);
CREATE INDEX ON :plant(urlkey);
CREATE INDEX ON :catchword(Name);
CREATE INDEX ON :catchword(key);
CREATE INDEX ON :catchword(urlkey);
CREATE INDEX ON :event(Name);
CREATE INDEX ON :event(key);
CREATE INDEX ON :event(urlkey);
CREATE INDEX ON :computer_game(Name);
CREATE INDEX ON :computer_game(key);
CREATE INDEX ON :computer_game(urlkey);
CREATE INDEX ON :anime(Name);
CREATE INDEX ON :anime(key);
CREATE INDEX ON :anime(urlkey);
CREATE INDEX ON :delicacy(Name);
CREATE INDEX ON :delicacy(key);
CREATE INDEX ON :delicacy(urlkey);
CREATE INDEX ON :novel(Name);
CREATE INDEX ON :novel(key);
CREATE INDEX ON :novel(urlkey);
CREATE INDEX ON :pet(Name);
CREATE INDEX ON :pet(key);
CREATE INDEX ON :pet(urlkey);
CREATE INDEX ON :other(Name);
CREATE INDEX ON :other(key);
CREATE INDEX ON :other(urlkey);



neo4j sqls 导入：
第一种方法：
使用的时候可以将这些语句放在一个文件中 然后进入bin目录执行如下命令：
 cat  filePath | ./neo4j-shell
 
第二种方法：
 多线程导入，从线上拉取日志（日志假设为EALLLast） 
插入节点sql
[root@host73 Shell]# cat EALLLast | grep "QUERY=create" | sort | uniq > node
[root@host73 Shell]# cat node | awk -F "UERY=" '{print $2}'
 
插入关系：
cat EALLLast | grep "QUERY=" | grep "merge (p)"  | sort | uniq  > a
cat a | awk -F "###" '{print $5}'
  

 执行成功后  去页面http://ip:7474   执行命令如下  :schema  如果下面显示一大堆的索引信息 就说说明创建成功；
 在后面无论是插入还是查找，只要根绝分类信息和响应的字段 速度有很大的提升










=====================================================
后台脚本
-------------------------------
hbase基本命令
hbase shell      
list 
describe 'scrapy_baike_words_first'

--------------------------------
getData.sh  host

每台机器的userlog目录
ls /tmp3/linux_src/hadoop-2.6.0/logs/userlogs/


--------------------------------
hball.sh    
hbase shell hball.sh  > url
cat url | awk -F "value=" '{print $2}' | sort | uniq 
================================

neo4j.sh  
pom:              <mainClass>com.emotibot.MR.ExtractorJob</mainClass>


/tmp3/linux_src/hadoop-2.6.0/bin/hadoop jar KnowledgeGraph-0.0.1.jar t11 liu Neo4j 1


节点语句：
QUERY=create (result:anime{key:"00f6ec6f0b53146d4fc9975392f25079"}) set result.urlkey="dfb43cd9373d385d0dd867f2257e3764",result.tag="",result.Pic="http://b.hiphotos.baidu.com/baike/w%3D268%3Bg%3D0/sign=97bad5b8cc1349547e1eef626e75f565/63d9f2d3572c11df6089d21a642762d0f703c2ab.jpg",result.key="00f6ec6f0b53146d4fc9975392f25079",result.Name="新世纪福音战士",result.ParamInfo="《新世纪福音战士》EVA官方手游是一款由上海黑桃互动发行的，以经典动漫作品《EVA》改编的3D动作卡牌类手游，游戏将原作中宏大的世界观背景设定完整平移，融合动漫新剧场版的时尚元素，采用Unity3D技术和PHP游戏引擎还原出丰富的原作风貌和精致机战场景。 在游戏中，玩家将驾驶着属于自己的机甲，以第一视角驾驶巨大机甲，强化武器，亲身参与对抗使徒守护全人类的战斗，在感受原著魅力的同时，体验酣畅的激战和新奇的游戏乐趣。"  return result;

关系
match (p:anime {urlkey:"0056836c76087b3067252b7dcb526b56"} ) match (q:figure {key:"d242d13e0439db7f9612f421656750d4"} ) merge (p)-[r:主要配音]->(q) 
match (p:anime {urlkey:"0056836c76087b3067252b7dcb526b56"} ) match (q:other {key:"380a3a9e81cb79b59bdc505a4d05af3d"} ) merge (p)-[r:首播电视台]->(q) 


echo "flushall" | /home/taoliu/redis/redis-3.0.0/src/redis-cli 
清空redis

/tmp3/linux_src/hadoop-2.6.0/bin/hadoop jar KnowledgeGraph-0.0.1.jar t11 liu Neo4j 1

/tmp3/linux_src/hadoop-2.6.0/bin/hadoop jar KnowledgeGraph-0.0.1.jar t22 liu Neo4j 3

/tmp3/linux_src/hadoop-2.6.0/bin/hadoop jar KnowledgeGraph-0.0.1.jar t11 liu Neo4j 2

参数一：文件名  里面是表名
参数二：不要关注 没用
参数三：Neo4j Solr
参数四：1 一级节点  3 二级节点  2 关系   如果是solr就没有


-------------------------------------
solr.sh  

--------------------------
t11  t22
现在线上一级表和二级表的名字


===========================================
代码：
 hadoop dfs -ls /domain/