just for fun,you can publish status on renren via the code.
The code uses the Gson and HttpClient,so you must import these jars.

针对新版做个小变动

人人网主页换了后，相关参数发生了变化
原来的隐藏参数封装到首页这个数组里面了。
nx.user = {
id : "",
ruid:"",
tinyPic	: " ",
name : "",
requestToken : '',
_rtk : ''
}

hostid -> id
requestToken和_rtk的长度减一了
