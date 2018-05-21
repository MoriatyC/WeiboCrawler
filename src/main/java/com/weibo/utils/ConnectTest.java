package com.weibo.utils;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import org.apache.http.HttpHost;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import com.weibo.dao.RedisDao;

import us.codecraft.webmagic.proxy.Proxy;

/**
*@author: Menghui Chen
*@version: 2018年5月19日上午10:29:14
**/
public class ConnectTest {
    public static Map<String, String> init(RedisDao redis) {
        Map<String, String> proxy = redis.hgetall("proxy:hash");
        System.out.println(proxy.size());
        Iterator<Map.Entry<String, String>> it = proxy.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, String> item = it.next();
            if (!connectTest(item.getKey(), Integer.valueOf(item.getValue()))) {
                it.remove();
            }
        }
        redis.del("proxy:hash");
        redis.hsetWithExpired("proxy:hash", proxy);
        System.out.println(proxy.size());
        return proxy;
    }
    public static Proxy[] mapToProxy(Map<String, String> proxys) {
        Proxy[] tmp = new Proxy[proxys.size()];
        int i = 0;
        for (String ip : proxys.keySet()) {
            tmp[i++] = new Proxy(ip, Integer.valueOf(proxys.get(ip)));
        }
        return tmp;
    }
    public static boolean connectTest(String ip, int port) {
        CloseableHttpClient client = HttpClients.createDefault();
        boolean judge = false;
        try{
            HttpHost proxy = new HttpHost(ip, port, "http");
            RequestConfig config = RequestConfig.custom()
                    .setConnectTimeout(6000)//设置连接超时时间  
                    .setSocketTimeout(6000)//设置读取超时时间  
                    .setProxy(proxy)
                    .build();
            HttpGet request = new HttpGet("http://gs.dlut.edu.cn/");
            request.setConfig(config);
            CloseableHttpResponse response = null;
            try {             
                response = client.execute(request);
                System.out.println("___________________________________________");
                System.out.println(response.getStatusLine());
                System.out.println("___________________________________________");
            } catch (ClientProtocolException e) {
//                e.printStackTrace();
                System.out.println("connect time out");
            } catch (IOException e) {
                System.out.println("connect time out");
//                e.printStackTrace();
            } finally {
                if (response != null && response.getStatusLine().getStatusCode() == 200) {
                    try {
                        judge = true;
                        response.close();
                    } catch (IOException e) {
                        System.out.println("reponse close failed");
//                        e.printStackTrace();
                    }                    
                }
            }
        } finally {
            if (client != null) {
                try {
                    client.close();
                } catch (IOException e) {
                    System.out.println("client close failed");
//                    e.printStackTrace();
                }
            }
        }
        return judge;
        
    }
}
