# 一种双Buffer缓冲池的实现方式：TwinsBufferPool

缓冲区（Buffer）和缓冲池（BufferPool）是两个重要的概念，很明显，两者构成了一个包含与被包含的关系，一个缓冲池内可以有一个或者多个缓冲区协同工作，缓冲池中的所有缓冲区被组织成了一个环形队列，一前一后的两个缓冲区可以互相替换角色。

我们先从配置说起。

每个参数都会提供默认值，所以不做任何配置也是允许的。如下是目前TwinsBufferPool能提供的配置参数（yml）：

```yaml
buffer:
  capacity: 2000
  threshold: 0.5
  allow-duplicate: true
  pool:
    enable-temporary-storage: true
    buffer-time-in-seconds: 120
```
下面附上参数说明表：

![TwinsBufferPool参数表](https://img-blog.csdnimg.cn/20190328104559501.jpg)

以上参数比较浅显易懂，这里重点解释enable-temporary-storage和buffer-time-in-seconds这两个参数。

根据参数说明，很明显可以感受到，这两个参数是为了预防突发情况，导致数据丢失。因为缓冲区都是基于内存的设计的，这就意味着缓冲的数据随时处于一种服务重启，或者服务宕机的高风险环境中，因此，才会有这两个参数的诞生。

因为TwinsBufferPool良好的接口设计，对于以上两个参数的实现机制也是高度可扩展的。TwinsBufferPool默认的是基于Redis的实现，用户也可以用MongoDB，MySQL，FileSystem等方式实现。由此又会衍生出另外一个问题，由于各种异常情况，导致临时存储层遗留了一定量的数据，需要在下次启动的时候，恢复这一部分的数据。

总而言之，数据都是通过flush动作最终持久化到磁盘上。

![在这里插入图片描述](https://img-blog.csdnimg.cn/20190328103002725.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_d3hfcHViOiBqaXNodWh1aV8yMDE1,size_15,color_FFFFFF,t_70)

因为大多数实际业务场景对于缓冲池的并发量是有一定要求的，所以默认就采用了线程安全的实现策略，受到JDK中ThreadPool的启发，缓冲池也具备了自身状态管理的机制。如下列出了缓冲池所有可能存在的状态，以及各个状态的流转。

```java
/**
 * 缓冲池暂未就绪
 */
private static final int ST_NOT_READY = 1;

/**
 * 缓冲池初始化完毕，处于启动状态
 */
private static final int ST_STARTED = 2;

/**
 * 如果安全关闭缓冲池，会立即进入此状态
 */
private static final int ST_SHUTTING_DOWN = 3;

/**
 * 缓冲池已关闭
 */
private static final int ST_SHUTDOWN = 4;

/**
 * 正在进行数据恢复
 */
private static final int ST_RECOVERING = 5;
```
![TwinsBufferPool状态机](https://img-blog.csdnimg.cn/20190328115336664.jpg?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_d3hfcHViOiBqaXNodWh1aV8yMDE1,size_30,color_CDCDCD,t_70)

通过上述的一番分析，设计的方案也呼之欲出了，下面给出主要的接口设计与实现。

![BufferPool接口定义](https://img-blog.csdnimg.cn/20190328103123456.jpg?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_d3hfcHViOiBqaXNodWh1aV8yMDE1,size_30,color_CDCDCD,t_70)

通过以上的讲解，也不难理解BufferPool定义的接口。缓冲池的整个生命周期，以及内部的一些运作机制都得以体现。值得注意的是，在设计上，将缓冲池和存储层做了逻辑分离，使得扩展性进一步得到增强。

存储相关的接口包含了一些简单的CURD，目前默认是用Redis作为临时存储层，MongoDB作为永久存储层，用户可以根据需要实现其他的存储方式。

下图展现的是TwinsBufferPool的实现方式，DataBuffer是缓冲区，必须依赖的基础元素。因为设计的是环形队列，所以依赖了CycleQueue，这个环形队列的interface也是自定义的，在JDK中没有找到比较合适的实现。

![BufferPool接口实现](https://img-blog.csdnimg.cn/2019032810314446.jpg?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_d3hfcHViOiBqaXNodWh1aV8yMDE1,size_30,color_CDCDCD,t_70)

值得注意的是，BufferPool接口定义是灵活可扩展的，TwinsBufferPool只是提供了一种基于环形队列的实现方式，用户也可以自行设计，使用另外一种数据结构来支撑缓冲池的运作。

更多精彩内容，请移步：[细说双Buffer缓冲池](https://zhuanlan.zhihu.com/p/60226758)
