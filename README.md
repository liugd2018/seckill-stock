# 库存系统



## 系统设计

TODO 架构图



通过LoadingCache+Redis的方式拦截一部分流量，防止一些不必要流量打入数据库。通过redis自增、redisson分布式锁扣减库存，通过MQ方式同步扣减成功订单数据到数据库。



### 技术

| 框架                                                         | 版本          |
| ------------------------------------------------------------ | ------------- |
| [Spring Boot](https://spring.io/projects/spring-boot)        | 2.3.2.RELEASE |
| [Spring Cloud](https://spring.io/projects/spring-cloud)      | Hoxton.SR8    |
| [SpringCloud Alibaba](https://spring.io/projects/spring-cloud-alibaba) | 2.2.3.RELEASE |
| [caffeine](https://github.com/ben-manes/caffeine)            | 2.6.2         |
| [mapstruct](https://mapstruct.org/)                          | 1.3.1.Final   |
| [MyBatis-Plus](https://mp.baomidou.com/)                     | 3.4.0         |
| [Redisson](https://github.com/redisson/redisson)             | 3.13.4        |
| [Rabbitmq](https://www.rabbitmq.com/documentation.html)      |               |
| [MySQL](https://www.mysql.com/cn/)                           |               |
| [Redis](https://redis.io/documentation)                      |               |



### 业务流程图

![业务流程图](https://raw.githubusercontent.com/liugd2018/img-folder/main/stock/%E5%BA%93%E5%AD%98%E6%B5%81%E7%A8%8B%E5%9B%BE.jpg)



如图所示上面是扣减库存大概业务流程，我稍微解释一下。

1. LocdingCache中存放已经售罄库存、不存在库存信息。（LocdingCache过期时间）
2. Redis存放库存信息是数据库同步，在Redis中扣减。添加库存数据库添加Redis信息。（Redis过期时间）
3. Redis扣减成功之后，去执行数据库扣减。
4. 通过Rabbitmq异步数据库订单信息。





