package com.atguigu.gulimall.thirdparty.controller;

import com.aliyun.oss.OSS;
import com.aliyun.oss.common.utils.BinaryUtil;
import com.aliyun.oss.model.MatchMode;
import com.aliyun.oss.model.PolicyConditions;
import com.atguigu.common.utils.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@Slf4j
public class OssController {
    // OSS客户端
    @Resource
    OSS ossClient;

    // OSS的Endpoint
    @Value("${spring.cloud.alicloud.oss.endpoint}")
    private String endpoint;
    // OSS的存储桶名称
    @Value("${spring.cloud.alicloud.oss.bucket}")
    private String bucket;

    // 阿里云访问密钥ID
    @Value("${spring.cloud.alicloud.access-key}")
    private String accessId;

    /**
     * 获取上传策略
     * 该接口用于生成并返回上传文件所需的政策信息，包括访问ID、签名、策略等，以便客户端能够直接进行文件上传。
     *
     * @return 返回包含上传策略信息的Map对象。其中，数据包括访问ID、编码后的策略、签名、上传目录、主机地址和过期时间等。
     */
    @GetMapping("/oss/policy")
    public R policy() {
        log.info("开始尝试获取上传策略...");
        // 构建OSS文件访问URL
        String host = "https://" + bucket + "." + endpoint;

        // 生成以日期为前缀的文件上传目录
        String format = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        String dir = format + "/";

        Map<String, String> respMap = null;
        try {
            // 设置策略过期时间
            long expireTime = 30;
            long expireEndTime = System.currentTimeMillis() + expireTime * 1000;
            Date expiration = new Date(expireEndTime);

            // 设置上传策略条件
            PolicyConditions policyConds = new PolicyConditions();
            policyConds.addConditionItem(PolicyConditions.COND_CONTENT_LENGTH_RANGE, 0, 1048576000);
            policyConds.addConditionItem(MatchMode.StartWith, PolicyConditions.COND_KEY, dir);

            // 生成上传策略
            String postPolicy = ossClient.generatePostPolicy(expiration, policyConds);
            byte[] binaryData = postPolicy.getBytes("utf-8");
            String encodedPolicy = BinaryUtil.toBase64String(binaryData);

            // 计算上传签名
            String postSignature = ossClient.calculatePostSignature(postPolicy);

            // 准备返回给客户端的信息
            respMap = new LinkedHashMap<String, String>();
            respMap.put("accessid", accessId);
            respMap.put("policy", encodedPolicy);
            respMap.put("signature", postSignature);
            respMap.put("dir", dir);
            respMap.put("host", host);
            respMap.put("expire", String.valueOf(expireEndTime / 1000));

            log.info("获取上传策略成功，返回给客户端的信息：{}", respMap);
        } catch (Exception e) {
            // 异常处理，打印错误信息
            log.error("获取上传策略失败", e);
            // 返回具体的错误信息给客户端
            return R.error("获取上传策略失败，请稍后重试");
        }

        // 返回包含上传策略信息的结果
        return R.ok().put("data", respMap);
    }
}

