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

使用的时候可以将这些语句放在一个文件中 然后进入bin目录执行如下命令：
 cat  filePath | ./neo4j-shell 

 执行成功后  去页面http://ip:7474   执行命令如下  :schema  如果下面显示一大堆的索引信息 就说说明创建成功；
 在后面无论是插入还是查找，只要根绝分类信息和响应的字段 速度有很大的提升




