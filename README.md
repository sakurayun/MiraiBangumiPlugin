# MiraiBangumiPlugin

用于番剧更新播报的mirai插件（目前仅支持bilibili ~~，等找到A站时间表的api就加上去~~）

支持：
- 番剧更新播报
- 番剧时间表定时播报
- 查询番剧时间表

## 使用方法

### 初次使用

1. 把插件放入mirai的plugins目录后运行mirai-console（废话）
2. 使用`/bangumi-notify add <bot> <targetType> <targetId>`指令添加播报的对象（好友/群组）
3. 按需更改配置文件

### 可用指令

指令可在控制台和聊天环境发送，请自行使用Mirai Console内置的 [权限管理服务](https://github.com/mamoe/mirai-console/blob/master/docs/Permissions.md#使用内置权限服务指令) 进行权限许可。

#### 查询番剧时间表
>
>  `(/)bangumi` 或 `(/)[配置文件中设置的命令别名]`

#### 管理番剧更新的提醒

>  `(/)bangumi-notify add <bot> <targetType> <targetId>`   为指定Bot添加提醒目标
>
>  `(/)bangumi-notify list <bot>`   查看指定Bot的提醒目标
>
>  `(/)bangumi-notify remove <bot> <targetType> <targetId>`   为指定Bot移除提醒目标

#### 番剧更新提醒插件调试命令

>  `(/)bangumi-debug all`    查看完整番剧时间表（13天）
>
>  `(/)bangumi-debug deliver`    立即推送下一部更新的番剧（即使未到时间）
>
>  `(/)bangumi-debug delivertimeline`    立即推送番剧时间表（即使未到时间）
>
>  `(/)bangumi-debug fetch`    立即从远程拉取番剧数据
>
>  `(/)bangumi-debug peek`    查看下一部更新的番剧
>
>  `(/)bangumi-debug pending`    查看所有即将更新的番剧

### 配置文件（general.yml）

```yaml
dateFormat: yyyy-MM-dd  # 日期格式化模板
timeFormat: 'HH:mm'  # 时间格式化模板

fetchIntervalMills: 7200000  # 从远端拉取数据的时间间隔（单位：毫秒）

commandSecondaryNames:  # 查询番剧时间表命令的别名
  - 看看番

notifies:  # 分bot配置要播报的对象（好友/群组），也可以用命令更改
  12345678: 
    notifyFriendList: 
      - 114514
    notifyGroupList: 
      - 1919810

seasonMessageType: card  # 番剧更新播报的消息类型（card为Json消息，plain为文本消息）
seasonNotifyTag: 【番剧更新】  # 消息开头添加的tag
seasonCardTitle: 番剧更新提醒  # 消息类型为card时，title的内容

timelineNotifyEnabled: true  # 是否开启番剧时间表定时播报
timelineNotifyTime: '00:00'  # 番剧时间表播报时间
```