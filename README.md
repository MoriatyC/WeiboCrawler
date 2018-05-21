## 功能
WeiboCrawler是一个基于[webmagic](http://webmagic.io/)和redis的Java爬虫应用，爬虫还有3个功能模块

### 1. 微博搜索爬取
通过[微博cn接口](https://weibo.cn/search/)输入搜索关键字，并点击下一页，得到通用url，比如搜索**微博**，选择下页，复制网址中前段部分，**https://weibo.cn/search/mblog?hideSearchFrame=&keyword=%E5%BE%AE%E5%8D%9A&page=** ， 作为**WeiboSearch.java**中target的起始部分，该爬虫会爬去以下字段：

* 评论数
* 转发数
* 点赞数
* 微博正文
* 发文时间
* 作者
* 微博id

### 2. 微博用户信息爬取
该爬虫会扫描当前用户数据库，更新所有用户信息，包括：

* 生日
* 微博数
* 粉丝数
* 关注数
* 主页网址
* 博主所在地
* 姓名
* 组织/个人
* 数字id
* 性别
* 标签
* 是否大V

### 3. 代理爬取
由于微博对于爬虫的限制，所以需要通过代理的支持，该爬虫通过爬取免费的代理网站并进行简单验证，将有效的代理加入redis中进行缓存，过期时间为1天，但是由于微博封锁力度较大，免费代理中很多测试有效的ip很有可能已被微博永封，出现了访问400的错误，但是却可以访问其他网址
 
 ## 兼容性
该工具在windows7上开发并测试有效
基础依赖：

* JAVA8
* Maven3.3.9
* spring-boot 2.0.2
* mysql 8.0.11
* redis 3.0.504
## 如何获取cookie

1. 用Chrome打开<https://passport.weibo.cn/signin/login>；<br>
2. 按F12键打开Chrome开发者工具；<br>
3. 点开“Network”，将“Preserve log”选中，输入微博的用户名、密码，登录，如图所示：
![](https://picture.cognize.me/cognize/github/weibospider/cookie1.png)
4. 点击Chrome开发者工具“Name"列表中的"m.weibo.cn",点击"Headers"，其中"Request Headers"下，"Cookie"后的值即为我们要找的cookie值，复制即可，如图所示：
![](https://picture.cognize.me/cognize/github/weibospider/cookie2.png)

## 使用
在本地mysql创建**weibo**数据库，根据需要依次执行**WeiBoApplication.java**中的相关爬虫即可，可能会由于爬取速度过快导致爬虫被封，所以有条件，还是使用个人代理进行爬取。
