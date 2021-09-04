# boot_C
RabbitMq实现分库（数据库解耦）方案demo-rabbitmq数据库初始化

jar包
1个public queue  多个private queue (第一个注册的private queue为基础队列，其他队列的数据初始化同步此队列服务数据)
每个服务监听public 和自己的private queue

系统启动实现applicationRunner 
  public 和 private queue初始化
  设置监听容器监听public 和自己的private queue 并设置队列消息的并发消费者为1，即每个消息只能被一个消费者消费
  对private queue入库并发消息给public queue(message=private queue) signIn
实际的监听方法onMessage 对消息解析 通过反射找到具体的方法并传参执行
private queue注册消息方法 signIn（新注册的private queue）
  if(是自己的服务) 则 发消息给public queue(message=private queue) signIn 如果是自己服务监听到自己服务的队列注册消息，则再次向public发本服务的注册消息，直到指定次数
  else 不是自己的服务
    if(新注册的队列没有在本库入过库)发消息给其他队列initQueuesInfo 让其对这个队列入库，发消息给新注册的队列，让其对其他队列入库
    else （此队列以前做个入库） 发消息给其他队列initQueuesInfo 让其对这个队列做更新
    if(新注册队列status=0) 发送初始化数据给新队列
