# Bilibili直播弹幕API
用于获取B站直播弹幕

# 构建需求
openjdk-8-jdk

maven 3.3.9
    
# 使用示例

    try {
        new LiveDanMuAPI("http://live.bilibili.com/244")
                .setPrintDebugInfo(true)
                .addCallback(new LiveDanMuCallback())
                .connect();
    } catch (IOException | IllegalArgumentException e) {
        e.printStackTrace();
    }

LiveDanMuAPI 是对外提供的 API 入口, 有三个构造器.

    public LiveDanMuAPI(int roomId);
    public LiveDanMuAPI(String url) throws IOException, IllegalArgumentException;
    public LiveDanMuAPI(URL url) throws IOException, IllegalArgumentException;

.setPrintDebugInfo(Boolean printDebugInfo)

设定是否输出调试信息到控制台.

.addCallback(ILiveDanMuCallback liveDanMuCallback)

添加回调. 继承并实现 ILiveDanMuCallback 中的方法即可作为参数传入.

.connect()

连接弹幕推送服务器

IOException 在 Socket 错误时抛出.

IllegalArgumentException 在构造器中传入错误的房间号或 URL 不正确时抛出.

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
感谢 lyy 提供的协议分析 http://www.lyyyuna.com/2016/03/14/bilibili-danmu01/

抓取到的一些 json (包含一些注释) 在 ./protocol/

其中的 DANMU_MSG 存储的数据没有键名, 一些不明含义的值需要手动按下标取值, 例如:

    String s = danMuMSGEntity.info.getJSONArray(2).getString(1);

SEND_GIFT.data.medal 可能是数字也可能是数组, 具体内容和含义见 json 示例.

# 开源协议
GPL V3
