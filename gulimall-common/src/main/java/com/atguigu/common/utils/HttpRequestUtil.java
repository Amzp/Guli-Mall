package com.atguigu.common.utils;

import javax.servlet.http.HttpServletRequest;

public class HttpRequestUtil {

    /**
     * 获取客户端的IP地址
     * 此方法首先尝试从HTTP头中获取IP地址，依次检查"x-forwarded-for"、"Proxy-Client-IP"、"WL-Proxy-Client-IP"，
     * 如果这些头都不存在或为空，则退回到获取请求端的IP地址。
     * 注意：此方法适用于通过代理或负载均衡服务器转发请求的场景。
     *
     * @param request 客户端请求对象，用于获取客户端IP地址。
     * @return 客户端的IP地址。如果无法获取到有效的IP地址，则返回本地回环地址"127.0.0.1"。
     */
    public String getIpAddress(HttpServletRequest request) {
        String ip = null;
        
        // 尝试从"x-forwarded-for"头获取IP地址，这是常见的代理传递客户端IP的方式
        ip = request.getHeader("x-forwarded-for");
        if (isValidIp(ip)) {
            // 处理多级代理情况，只返回最后一个有效的IP地址
            ip = ip.split(",")[ip.split(",").length - 1].trim();
        } else {
            // 如果"x-forwarded-for"头不存在或值为空或为"unknown"，则尝试从"Proxy-Client-IP"头获取IP地址
            ip = request.getHeader("Proxy-Client-IP");
            if (!isValidIp(ip)) {
                // 如果"Proxy-Client-IP"头也不存在或值为空或为"unknown"，则尝试从"WL-Proxy-Client-IP"头获取IP地址
                ip = request.getHeader("WL-Proxy-Client-IP");
                if (!isValidIp(ip)) {
                    // 如果所有尝试都失败，退回到获取请求对象本身的远程地址
                    ip = request.getRemoteAddr();
                } else {
                    // 由于WL-Proxy-Client-IP通常不会包含多个IP，故此处不进行分割处理
                }
            } else {
                // 由于Proxy-Client-IP通常不会包含多个IP，故此处不进行分割处理
            }
        }

        // 特殊处理IP为本地回环地址的情况，返回字符串"127.0.0.1"
        return "0:0:0:0:0:0:0:1".equals(ip) ? "127.0.0.1" : ip;
    }

    /**
     * 验证IP地址是否有效
     * 
     * @param ip 待验证的IP地址字符串
     * @return 如果IP地址有效则返回true，否则返回false
     */
    private boolean isValidIp(String ip) {
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            return false;
        }
        // 使用正则表达式验证IP地址格式，这里同时考虑了IPv4和IPv6
        String ipv4Regex = "^((25[0-5]|2[0-4]\\d|[01]?\\d\\d?)\\.){3}(25[0-5]|2[0-4]\\d|[01]?\\d\\d?)$";
        String ipv6Regex = "^([0-9a-fA-F]{0,4}:){2,7}[0-9a-fA-F]{0,4}$";
        return ip.matches(ipv4Regex) || ip.matches(ipv6Regex);
    }
}
