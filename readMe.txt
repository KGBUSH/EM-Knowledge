*******************************************************1.Neo4j 使用：
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



************************************************************2.neo4j sqls 导入：
第一种方法(适合入库的sql数量比较少的时候)：
使用的时候可以将这些语句放在一个文件中 然后进入bin目录执行如下命令：
 cat  filePath | ./neo4j-shell
 
第二种方法(适合入库反日sql语句较多的时候，一般正式线上目前用这种方法入库)：
多线程导入，从线上拉取日志（日志假设为EALLLast） 
插入节点sql
[root@host73 Shell]# cat EALLLast | grep "QUERY=create" | sort | uniq > node
[root@host73 Shell]# cat node | awk -F "UERY=" '{print $2}'
通过以上命令 得到插入节点的sql
 
 
插入关系：
cat EALLLast | grep "QUERY=" | grep "merge (p)"  | sort | uniq  > a
cat a | awk -F "###" '{print $5}'
通过上述命令得到插入关系的sql语句 


============neo4j 数据删除
必须先删除关系再删除节点：
START n=node(*) match n-[r]->() delete r;
START n=node(*)  delete n;





================
 执行成功后  去页面http://ip:7474   执行命令如下  :schema  如果下面显示一大堆的索引信息 就说说明创建成功；
 在后面无论是插入还是查找，只要根绝分类信息和响应的字段 速度有很大的提升










************************************************************3.后台脚本
后台脚本
-------------------------------
hbase基本命令
hbase shell      
list 
describe 'scrapy_baike_words_first'

--------------------------------
getData.sh  host
这是拉取hadoop job 日志的脚本程序 ；其中host中记录的是hadoop集群的节点机器（包括主节点）
后续的同义词和当前页面下的页面内部的百科链接地址，一级所有的节点喝关系的sql语句，全部从日志中分析得出

SQL语句的分析：
多线程导入，从线上拉取日志（日志假设为EALLLast） 
插入节点sql
[root@host73 Shell]# cat EALLLast | grep "QUERY=create" | sort | uniq > node
[root@host73 Shell]# cat node | awk -F "UERY=" '{print $2}'
通过以上命令 得到插入节点的sql
 
插入关系：
cat EALLLast | grep "QUERY=" | grep "merge (p)"  | sort | uniq  > a
cat a | awk -F "###" '{print $5}'
通过上述命令得到插入关系的sql语句


当前所有的抓去页面的url分析：
cat EALLLast | grep "url=" | awk -F "url=" '{print $2}' | sort | uniq > url
上述命令得出当前所有抓去的url的去重后的地址信息

当前所有页面的页面内部的百科页面分析：
cat EALLLast | grep "===" | grep ">>>" | grep "http" |awk -F "===" '{print $1}' | sort | uniq > url
上述命令得出所有的当前抓去页面内部的百科链接  可以作为下一阶段二级节点扩充数据的来源

----------------------------------

每台机器的userlog目录
ls /tmp3/linux_src/hadoop-2.6.0/logs/userlogs/


--------------------------------
hball.sh    
该脚本用于扫描hbase表  获取当前抓去的表里面的url,也可以使用getData.sh取分析日志获取地址。

hbase shell hball.sh  > url
cat url | awk -F "value=" '{print $2}' | sort | uniq

步骤如上  可以得到扫描表里面的url信息
 
================================

neo4j.sh  
pom:              <mainClass>com.emotibot.MR.ExtractorJob</mainClass>


/tmp3/linux_src/hadoop-2.6.0/bin/hadoop jar KnowledgeGraph-0.0.1.jar t11 liu Neo4j 1


节点插入语句示例：
QUERY=create (result:anime{key:"00f6ec6f0b53146d4fc9975392f25079"}) set result.urlkey="dfb43cd9373d385d0dd867f2257e3764",result.tag="",result.Pic="http://b.hiphotos.baidu.com/baike/w%3D268%3Bg%3D0/sign=97bad5b8cc1349547e1eef626e75f565/63d9f2d3572c11df6089d21a642762d0f703c2ab.jpg",result.key="00f6ec6f0b53146d4fc9975392f25079",result.Name="新世纪福音战士",result.ParamInfo="《新世纪福音战士》EVA官方手游是一款由上海黑桃互动发行的，以经典动漫作品《EVA》改编的3D动作卡牌类手游，游戏将原作中宏大的世界观背景设定完整平移，融合动漫新剧场版的时尚元素，采用Unity3D技术和PHP游戏引擎还原出丰富的原作风貌和精致机战场景。 在游戏中，玩家将驾驶着属于自己的机甲，以第一视角驾驶巨大机甲，强化武器，亲身参与对抗使徒守护全人类的战斗，在感受原著魅力的同时，体验酣畅的激战和新奇的游戏乐趣。"  return result;

关系插入语句示例：
match (p:anime {urlkey:"0056836c76087b3067252b7dcb526b56"} ) match (q:figure {key:"d242d13e0439db7f9612f421656750d4"} ) merge (p)-[r:主要配音]->(q) 
match (p:anime {urlkey:"0056836c76087b3067252b7dcb526b56"} ) match (q:other {key:"380a3a9e81cb79b59bdc505a4d05af3d"} ) merge (p)-[r:首播电视台]->(q) 

-----------------------------------------------------------------------------------

echo "flushall" | /home/taoliu/redis/redis-3.0.0/src/redis-cli 
目前host73 清空redis的语句如上。

-----------------------------------------------------------
后台数据处理MapReduce部分的使用：
1.修改pom.xml,修改mainClass:<mainClass>com.emotibot.MR.ExtractorJob</mainClass>
2.mvn package 打包 生成KnowledgeGraph-0.0.1.jar
KnowledgeGraph-0.0.1.jar使用示例如下：


/tmp3/linux_src/hadoop-2.6.0/bin/hadoop jar KnowledgeGraph-0.0.1.jar t11 liu Neo4j 1

/tmp3/linux_src/hadoop-2.6.0/bin/hadoop jar KnowledgeGraph-0.0.1.jar t22 liu Neo4j 3

/tmp3/linux_src/hadoop-2.6.0/bin/hadoop jar KnowledgeGraph-0.0.1.jar t11 liu Neo4j 2

参数一：文件名  里面是表名
参数二：不要关注 没用
参数三：Neo4j Solr
参数四：1 一级节点  3 二级节点  2 关系   如果参数三是Solr就没有参数四


-------------------------------------
solr.sh  

--------------------------
t11  t22
现在线上一级表和二级表的名字


===========================================
代码：
 hadoop dfs -ls /domain/
 
 
 
 
 
 
************************************************************4.代码逻辑介绍
页面信息提取：
代码包的位置：com.emotibot.extractor
Extractor是一个接口类，目前主要针对百科数据进行抽取，所以主要是在BaikeExtractor中实现页面页面基本信息的抽取；
抽取以后的信息全部封装成一个PageExtractInfo的类对象，里面封装了一些基本信息：
包括页面词条名，是否是多义词，有无同义词信息，图片，基本属性信息，实体关系信息，前两段内容，图片信息，tag信息，以及百科页面的短句信息


solr建立索引：
封装了百科页面入solr的基本的操作，基本思路就是对于PageExtractInfo选取几个字段构建一个solrDocument 然后入库
实际入库的时候基本都是批量插入solr

mapreduce部分：