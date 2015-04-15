import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Kevin on 2015/4/13.
 * @version 1.0
 * @email linxiaobai@live.com
 * 最近浏览租房吗网站时发现其一个小小的漏洞，就是在访问每个租房信息的时候，查看商家电话
 * 号码时如果检测你未登陆，需要你登陆之后才能获取手机号码相关信息。但是实际上，通过观看
 * 前台代码发现这只是一个div的显隐，即手机号码已经在html代码之中了，所以我们可以直接通过
 * 不断地请求url去获取html内容，然后从里面抓取所需的手机号码即可。
 */
public class ZFMSpider {
    private static final int EXECUTE_NUM = 500;
    private static final Set<String> telSet = new HashSet<String>();
    private static final ExecutorService executorService = Executors
            .newFixedThreadPool(12);
    private static final AtomicInteger count = new AtomicInteger(0);

    private static HttpGet getHttpGet(String getUrl) {
        return new HttpGet(getUrl);
    }

    private static String getRetEntityStr(HttpResponse response) throws IOException {
        HttpEntity httpEntity = response.getEntity();
        return EntityUtils.toString(httpEntity, "UTF-8");
    }

    private static void putTel(String htmlContent) {
        try {
            int telIndex = htmlContent.indexOf("tel:");
            if (telIndex > 0) {
                String tel = htmlContent.substring(telIndex, telIndex+15);
                telSet.add(tel);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private static void executeReq(final int i) throws IOException {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                count.incrementAndGet();
                String url = "http://zufangma.cn/house_detail?id="+i;
                HttpClient httpClient = HttpClients.custom()
                        .setDefaultRequestConfig(getRequestConfig()).build();
                HttpGet httpGet = getHttpGet(url);
                HttpResponse httpResponse = null;
                try {
                    httpResponse = httpClient.execute(httpGet);
                } catch (IOException e) {
                    System.out.println(e.getMessage());
                }
                if (httpResponse != null && httpResponse.getStatusLine().getStatusCode() == 200) {
                    try {
                        putTel(getRetEntityStr(httpResponse));
                    } catch (IOException e) {
                        System.out.println(e.getMessage());
                    }
                }

                if (count.get() == EXECUTE_NUM) {
                    for (String tel : telSet) {
                        System.out.println(tel);
                    }
                    System.out.println("All tel count is :" + telSet.size());
                }
            }
        });
    }

    /**
     * httpClient配置相关参数
     * 超时时间设置为5秒
     * @return
     */
    private static RequestConfig getRequestConfig() {
        RequestConfig defaultRequestConfig = RequestConfig.custom()
                .setSocketTimeout(10000)
                .setConnectTimeout(10000)
                .setConnectionRequestTimeout(5000)
                .setStaleConnectionCheckEnabled(true)
                .build();
        return defaultRequestConfig;
    }

    public static void main(String[] args) throws IOException {
        for (int i = 1; i <= EXECUTE_NUM; i++) {
            executeReq(i);
        }

    }


}
