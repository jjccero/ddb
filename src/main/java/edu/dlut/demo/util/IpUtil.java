package edu.dlut.demo.util;

import javax.servlet.http.HttpServletRequest;

public class IpUtil {
    private IpUtil() {
    }

    private static boolean isUnknown(String ipAddr) {
        return ipAddr == null || ipAddr.length() == 0 || "unknown".equalsIgnoreCase(ipAddr);
    }

    public static String getIpAddress(HttpServletRequest request) {
        String ip = null;
        //X-Forwarded-For：Squid 服务代理
        String ipAddr = request.getHeader("X-Forwarded-For");
        if (isUnknown(ipAddr)) {
            //Proxy-Client-IP：apache 服务代理
            ipAddr = request.getHeader("Proxy-Client-IP");
        }
        if (isUnknown(ipAddr)) {
            //WL-Proxy-Client-IP：weblogic 服务代理
            ipAddr = request.getHeader("WL-Proxy-Client-IP");
        }
        if (isUnknown(ipAddr)) {
            //HTTP_CLIENT_IP：有些代理服务器
            ipAddr = request.getHeader("HTTP_CLIENT_IP");
        }
        if (isUnknown(ipAddr)) {
            //X-Real-IP：nginx服务代理
            ipAddr = request.getHeader("X-Real-IP");
        }
        //有些网络通过多层代理，那么获取到的ip就会有多个，一般都是通过逗号（,）分割开来，并且第一个ip为客户端的真实IP
        if (ipAddr != null && ipAddr.length() != 0) {
            ip = ipAddr.split(",")[0];
        }
        //还是不能获取到，最后再通过request.getRemoteAddr();获取
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ipAddr)) {
            ip = request.getRemoteAddr();
        }
        return ip.equals("0:0:0:0:0:0:0:1") ? "127.0.0.1" : ip;
    }
}
