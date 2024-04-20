package com.atguigu.gulimall.search.controller;

import com.atguigu.common.exception.BizCodeEnume;
import com.atguigu.common.to.es.SkuEsModel;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.search.service.ProductSaveService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;

/**
 * ClassName: ElasticSaveController
 * Package: com.atguigu.gulimall.search.app
 * Description:
 *
 * @Author Rainbow
 * @Create 2024/4/19 上午9:32
 * @Version 1.0
 */
@RequestMapping("/search/save")
@RestController
@Slf4j
public class ElasticSaveController {
    @Resource
    private ProductSaveService productSaveService;
    /**
     * 上架商品
     *
     * @param skuEsModels 商品信息列表，需要上架的商品以ES模型格式提供
     * @return 返回操作结果，成功返回R.ok()，失败返回包含错误信息的R.error()
     */
    @PostMapping("/product")
    public R productStatusUp(@RequestBody List<SkuEsModel> skuEsModels) {
        log.info("ElasticSaveController开始上架商品...");
        Boolean status = false;
        try {
            // 尝试上架商品，调用产品保存服务
            status = productSaveService.productStatusUp(skuEsModels);
        } catch (IOException e) {
            // 捕获并记录IO异常，返回商品上架异常错误信息
            log.error("ElasticSaveController商品上架错误", e);
            return R.error(BizCodeEnume.PRODUCT_UP_EXCEPTION.getCode(), BizCodeEnume.PRODUCT_UP_EXCEPTION.getMsg());
        }
        // 如果上架失败状态为true，即上架失败，则返回商品上架异常错误信息
        if (status) {
            return R.error(BizCodeEnume.PRODUCT_UP_EXCEPTION.getCode(), BizCodeEnume.PRODUCT_UP_EXCEPTION.getMsg());
        }
        // 上架成功，返回成功信息
        return R.ok();
    }

}
