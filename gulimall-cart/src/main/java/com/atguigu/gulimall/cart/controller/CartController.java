package com.atguigu.gulimall.cart.controller;

import com.atguigu.common.constant.AuthServerConstant;
import com.atguigu.gulimall.cart.interceptor.CartInterceptor;
import com.atguigu.gulimall.cart.service.CartService;
import com.atguigu.gulimall.cart.to.UserInfoTo;
import com.atguigu.gulimall.cart.vo.CartItemVo;
import com.atguigu.gulimall.cart.vo.CartVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * ClassName: CartController
 * Package: com.atguigu.gulimall.cart.controller
 * Description:
 *
 * @Author Rainbow
 * @Create 2024/4/26 下午9:36
 * @Version 1.0
 */
@Controller
@Slf4j
public class CartController {

    @Resource
    private CartService cartService;


    /**
     * 获取当前用户的购物车商品项
     *
     * @return
     */
    @GetMapping(value = "/currentUserCartItems")
    @ResponseBody
    public List<CartItemVo> getCurrentCartItems() {
        log.debug("获取当前用户的购物车商品项...");

        List<CartItemVo> cartItemVoList = cartService.getUserCartItems();

        return cartItemVoList;
    }

    /**
     * 去购物车页面的请求
     * 浏览器有一个cookie:user-key 标识用户的身份，一个月过期
     * 如果第一次使用jd的购物车功能，都会给一个临时的用户身份:
     * 浏览器以后保存，每次访问都会带上这个cookie；
     * <p>
     * 登录：session有
     * 没登录：按照cookie里面带来user-key来做
     * 第一次，如果没有临时用户，自动创建一个临时用户
     *
     * @return
     */
    @GetMapping(value = "/cart.html")
    public String cartListPage(Model model) throws ExecutionException, InterruptedException {
        log.debug("获取购物车中的商品项...");

        CartVo cartVo = cartService.getCart();
        model.addAttribute("cart", cartVo);

        return "cartList";
    }


    /**
     * 添加商品到购物车
     * 本方法处理客户端发起的添加购物车请求，将指定的商品数量添加到购物车中。
     * 通过RedirectAttributes添加信息到URL或Session中，用于页面重定向后使用。
     *
     * @param skuId      商品ID，用于标识要添加到购物车的具体商品。
     * @param num        要添加的商品数量。
     * @param attributes 用于在请求重定向时携带信息，可以将数据暂时存储在session中或添加到URL参数中。
     * @return 返回重定向的URL，将用户重定向到添加购物车成功的页面。
     * @throws ExecutionException   当异步操作完成时抛出的异常。
     * @throws InterruptedException 当线程被中断时抛出的异常。
     */
    @GetMapping(value = "/addCartItem")
    public String addCartItem(@RequestParam("skuId") Long skuId, @RequestParam("num") Integer num, RedirectAttributes attributes) throws ExecutionException, InterruptedException {
        log.info("添加商品到购物车：skuId = {}，num = {}", skuId, num);

        // 将指定的商品ID和数量添加到购物车
        cartService.addToCart(skuId, num);

        // 向重定向的URL添加参数，用于在目标页面显示具体添加的商品ID
        attributes.addAttribute("skuId", skuId);

        // 重定向到添加购物车成功的页面
        return "redirect:http://cart.gulimall.com/addToCartSuccessPage.html";
    }


    /**
     * 跳转到添加购物车成功页面
     * <p>
     * 该方法处理客户端将商品添加到购物车后请求成功页面的逻辑。通过商品ID查询购物车中对应的商品信息，
     * 并将该信息传递给成功页面进行展示。
     *
     * @param skuId 商品ID，用于查询购物车中对应的商品信息
     * @param model Model对象，用于在视图和控制器之间传递数据
     * @return 返回页面视图名，此处为"success"，即跳转到成功页面
     */
    @GetMapping(value = "/addToCartSuccessPage.html")
    public String addToCartSuccessPage(@RequestParam("skuId") Long skuId, Model model) {
        log.debug("添加购物车成功：addToCartSuccessPage = {}", skuId);

        // 查询购物车中指定商品ID的购物项信息
        CartItemVo cartItemVo = cartService.getCartItem(skuId);

        // 将购物项信息添加到模型中，以便在成功页面中展示
        model.addAttribute("cartItem", cartItemVo);

        // 返回成功页面的视图名
        return "success";
    }


    /**
     * 商品是否选中
     *
     * @param skuId
     * @param checked
     * @return
     */
    @GetMapping(value = "/checkItem")
    public String checkItem(@RequestParam(value = "skuId") Long skuId, @RequestParam(value = "checked") Integer checked) {
        log.debug("修改商品状态：skuId = {}，checked = {}", skuId, checked);
        cartService.checkItem(skuId, checked);

        return "redirect:http://cart.gulimall.com/cart.html";

    }


    /**
     * 改变商品数量
     *
     * @param skuId
     * @param num
     * @return
     */
    @GetMapping(value = "/countItem")
    public String countItem(@RequestParam(value = "skuId") Long skuId, @RequestParam(value = "num") Integer num) {
        log.debug("修改商品数量：skuId = {}，num = {}", skuId, num);

        cartService.changeItemCount(skuId, num);

        return "redirect:http://cart.gulimall.com/cart.html";
    }


    /**
     * 删除商品信息
     *
     * @param skuId
     * @return
     */
    @GetMapping(value = "/deleteItem")
    public String deleteItem(@RequestParam("skuId") Integer skuId) {
        log.debug("删除商品信息：skuId = {}", skuId);

        cartService.deleteIdCartInfo(skuId);

        return "redirect:http://cart.gulimall.com/cart.html";
    }
}
