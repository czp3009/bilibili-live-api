# Bilibili直播弹幕API
用于获取B站直播弹幕

# 构建需求
openjdk-8-jdk

maven 3.3.9

# 引入依赖
Maven

    <!-- https://mvnrepository.com/artifact/com.hiczp/bilibili-live-danmu-api -->
    <dependency>
        <groupId>com.hiczp</groupId>
        <artifactId>bilibili-live-danmu-api</artifactId>
        <version>1.1.0</version>
    </dependency>
    
# 使用示例
## 接收弹幕

    LiveDanMuReceiver liveDanMuReceiver =
                    new LiveDanMuReceiver("http://live.bilibili.com/545342")
                            .setPrintDebugInfo(true)
                            .addCallback(new LiveDanMuCallback());
    try {
        liveDanMuReceiver.connect();
    } catch (IOException | IllegalArgumentException e) {
        e.printStackTrace();
    }
    //Some code here
    try {
        liveDanMuReceiver.close();
    } catch (IOException e) {
        e.printStackTrace();
    }

LiveDanMuReceiver 是对外提供的 API 入口, 有三个构造器.

    public LiveDanMuReceiver(int roomId);
    public LiveDanMuReceiver(String url);
    public LiveDanMuReceiver(URL url);

.setPrintDebugInfo(Boolean printDebugInfo)

设定是否输出调试信息到控制台.

.addCallback(ILiveDanMuCallback liveDanMuCallback)

添加回调. 继承并实现 ILiveDanMuCallback 中的方法即可作为参数传入.

.connect()

连接弹幕推送服务器

IOException 在 Socket 错误时抛出.

IllegalArgumentException 在构造器中传入错误的房间号或 URL 不正确时抛出.

.close()

关闭 socket 连接.

IOException 在 Socket 错误时抛出.

# 特别说明
并不是所有主播的房间号都是 URL 末尾的数字.

假设一个直播间 URL 为 http://live.bilibili.com/1000

在 Chrome 中访问目标直播间, 查看源代码

在 \<head> 标签中找到类似如下段

    <script>
        document.domain = 'bilibili.com';

        var ROOMID = 5067;
        var DANMU_RND = 1491217361;
        var NEED_VIDEO = 1;
        var ROOMURL = 1000;
        var INITTIME = Date.now();
    </script>
    
其中的 5067 即为我们需要的 RoomID.

使用 URL 或者 String 表达的 URL 作为构造器参数时, 将自动完成以上房间号的获取.

# 直播弹幕协议
## 弹幕接收协议
弹幕的接收是通过 socket 连接至 B 站弹幕推送服务器, 由服务端主动推送实现的.

感谢 lyy 提供的协议分析 http://www.lyyyuna.com/2016/03/14/bilibili-danmu01/

抓取到的一些 json (包含一些注释) 在 ./protocol/

其中的 DANMU_MSG 存储的数据没有键名, 一些不明含义的值需要手动按下标取值, 例如:

    String s = danMuMSGEntity.info.getJSONArray(2).getString(1);

SEND_GIFT.data.medal 可能是数字也可能是数组, 具体内容和含义见 json 示例.

## 弹幕发送协议
通过对直播页面的分析发现, 弹幕的发送是一个 RESTful API.

需要三个 cookie 作为用户登录凭据(站点为 bilibili.com), 分别为 DedeUserID, DedeUserID__ckMd5, SESSDATA.

URL 为 http://live.bilibili.com/msg/send, 方法为 POST.

参数示例:

    color:16777215
    fontsize:25
    mode:1
    msg:测试
    rnd:1496669082
    roomid:1110317

color 与 fontsize 无须解释.

mode 弹幕模式, 对照表(一些模式需要 VIP):

    1 普通
    4 底端
    5 顶端
    6 逆向
    7 特殊
    9 高级

msg 为弹幕文本内容.

rnd 是一个随机数, 其值即为 HTML 页面中的 head 中的 script 部分中的 DANMU_RND 的值.见本文 "[特别说明](#特别说明)" 一节.

roomid 为房间号.

请求返回的 HttpStatus 均为 200.

Response body 为 JSON 数据, 例如:

    {"code":-400,"msg":"\u53c2\u6570\u9519\u8bef","data":[]}
    
code 表示该次请求的执行结果, 数字与提示信息对照表(可能不全):

    -101: 请先登录(cookies 错误)

    -400: 参数错误(POST 参数缺失或不在合法范围内)
    
    -500: 超出限制长度(弹幕至多 20 个 Unicode 字符)
    
    0: 发送成功

msg 为提示信息.

发送失败时, msg 为失败提示.

发送成功并状态正常时, msg 为空字符串.

发送速度过快时, msg 为 "msg in 1s".

data 不详, 未见过除空 JSONArray 以外的情况.

在本 API 中, 请求返回的 JSON 被封装到 DanMuResponseEntity 这一实体类(发送弹幕后返回), 变量名与 JSON key 一致.

# 开源协议
GPL V3
