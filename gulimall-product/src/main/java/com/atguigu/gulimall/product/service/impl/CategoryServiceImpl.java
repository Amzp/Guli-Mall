package com.atguigu.gulimall.product.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.gulimall.product.dao.CategoryDao;
import com.atguigu.gulimall.product.entity.CategoryEntity;
import com.atguigu.gulimall.product.service.CategoryBrandRelationService;
import com.atguigu.gulimall.product.service.CategoryService;
import com.atguigu.gulimall.product.vo.Catalog2Vo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service("categoryService")
@Slf4j
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {
    /*，CategoryServiceImpl 类继承自 MyBatis-Plus 提供的 ServiceImpl 抽象类，并通过泛型指定了 CategoryDao 和 CategoryEntity 类型，从而建立了对应的服务与 DAO 和实体类之间的关系。
     *   使用 MyBatis-Plus 的 ServiceImpl 抽象类提供了对数据访问对象（CategoryDao）和实体类（CategoryEntity）的通用 CRUD（增删改查）操作的支持。
     *   ServiceImpl自动注入了CategoryDao 类型的字段
     * */
//    @Autowired
//    CategoryDao categoryDao;

    @Resource
    private CategoryBrandRelationService categoryBrandRelationService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Resource
    private RedissonClient redissonClient;


    /**
     * 查询分类信息的分页数据。
     *
     * @param params 包含查询参数的Map对象，可以用来指定分页信息和查询条件。
     * @return 返回分类信息的分页工具对象，包含当前页的数据、总页数等信息。
     */
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        // 使用PageHelper进行分页查询，设置查询条件
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<CategoryEntity>()
        );

        // 封装分页查询结果到PageUtils工具类中并返回
        return new PageUtils(page);
    }

    /**
     * 列出所有分类，并以树形结构进行组织。
     * 这个方法首先查询出所有分类，然后将它们组织成树形结构，其中根节点是一级分类。
     *
     * @return 返回包含所有一级分类的列表，每个一级分类都包含其子分类。
     */
    @Override
    public List<CategoryEntity> listWithTree() {
        // 1、查询出所有分类
        List<CategoryEntity> entities = baseMapper.selectList(null);

        // 检查是否有数据
        if (entities.isEmpty()) {
            // 返回空列表
            return new ArrayList<>();
        }

        // 2、将查询结果组装成父子结构的树形列表
        //    2.1、首先筛选出所有一级分类
        List<CategoryEntity> level1Menus = entities.stream()
                .filter(categoryEntity -> categoryEntity.getParentCid() == 0)
                .map((menu) -> {
                    // 为每个一级分类查找并设置其子分类
                    menu.setChildren(getChildren(menu, entities));
                    return menu;
                })
                // 按照排序值对一级分类进行排序
                .sorted((menu1, menu2) -> {
                    // 安全获取排序值，如果sort为null，则默认为0
                    Integer sort1 = menu1.getSort() != null ? menu1.getSort() : 0;
                    Integer sort2 = menu2.getSort() != null ? menu2.getSort() : 0;
                    return sort1.compareTo(sort2);
                })
                .collect(Collectors.toList());

        return level1Menus;
    }


    /**
     * 根据菜单ID列表删除菜单。
     * 本方法采用逻辑删除方式，即在数据库中标记删除，而非真正物理删除。
     * 在执行删除前，应检查待删除的菜单是否被其他地方引用，确保删除操作的安全性。
     *
     * @param asList 要删除的菜单ID列表，类型为List<Long>。
     *               需要删除的菜单的ID将以此列表中的元素为依据进行删除操作。
     *               列表为空或null时，方法不执行任何操作。
     */
    @Override
    public void removeMenuByIds(List<Long> asList) {
        // TODO 检查当前删除的菜单，是否被别的地方引用

        // 执行逻辑删除，通过baseMapper的deleteBatchIds方法，根据ID列表批量删除记录
        baseMapper.deleteBatchIds(asList);
    }

    /**
     * 查找给定目录ID的目录路径。
     *
     * @param catelogId 目录的ID，表示要查找的目录。
     * @return 返回一个Long类型的数组，表示从根目录到指定目录的路径。
     */
    @Override
    public Long[] findCatelogPath(Long catelogId) {
        // 初始化存储路径的列表
        List<Long> paths = new ArrayList<>();

        // 查找父目录路径，并将路径中的每个目录ID添加到paths列表中
        List<Long> parentPath = findParentPath(catelogId, paths);

        // 将父目录路径反转，使其从根目录开始
        Collections.reverse(parentPath);

        // 将列表转换为数组并返回
        return parentPath.toArray(new Long[parentPath.size()]);
    }


    /**
     * 级联更新所有关联的数据
     * <p>
     * 本方法用于当更新一个分类信息时，不仅更新分类本身，同时也会更新与该分类关联的品牌信息。
     *
     * @param category 分类实体对象，包含需要更新的分类信息。
     */
//    @Caching(evict = {
//            @CacheEvict(value = "category", key = "'getLevel1Categorys'"),
//            @CacheEvict(value = "category", key = "'getCatalogJson'")
//    })
    @CacheEvict(value = "category", allEntries = true)
    @Transactional
    @Override
    public void updateCascade(CategoryEntity category) {
        // 更新分类本身的信息
        log.info("更新分类本身的信息");
        // this指向CategoryServiceImpl实例，调用的是ServiceImpl基类提供的或本类重写的updateById方法，用于更新分类本身的信息。
        this.updateById(category);

        // 更新与该分类关联的品牌信息
        log.info("更新与该分类关联的品牌信息");
        categoryBrandRelationService.updateCategory(category.getCatId(), category.getName());
    }


    /**
     * 查找给定分类ID的父级路径
     *
     * @param catelogId 当前分类的ID
     * @param paths     存储已经遍历过的分类ID路径
     * @return 返回包括当前分类ID及其所有父级分类ID的路径列表
     */
    private List<Long> findParentPath(Long catelogId, List<Long> paths) {
        // 将当前分类ID加入路径列表
        paths.add(catelogId);
        // 通过当前分类ID获取分类实体
        CategoryEntity byId = this.getById(catelogId);
        // 如果当前分类还有父级分类，则递归查找父级分类的路径
        if (byId.getParentCid() != 0) {
            findParentPath(byId.getParentCid(), paths);
        }
        return paths;
    }


    /**
     * 递归查找指定菜单的所有子菜单
     *
     * @param root 当前遍历的菜单项
     * @param all  所有菜单项的列表
     * @return 返回当前菜单项的所有子菜单项列表
     */
    private List<CategoryEntity> getChildren(CategoryEntity root, List<CategoryEntity> all) {

        // 1. 使用流处理来查找当前菜单的所有子菜单
        List<CategoryEntity> children = all.stream()
                // 2. 筛选出父菜单ID与当前菜单ID相同的菜单项
                .filter(categoryEntity -> Objects.equals(categoryEntity.getParentCid(), root.getCatId()))
                // 3. 对筛选后的菜单项进行处理，递归找到它们的子菜单
                .map(categoryEntity -> {
                    // 为每个菜单项设置其子菜单
                    categoryEntity.setChildren(getChildren(categoryEntity, all));
                    return categoryEntity;
                })
                // 4. 对菜单项按照排序字段进行排序
                .sorted((menu1, menu2) -> {
                    // 安全获取排序值，如果sort为null，则默认为0
                    Integer sort1 = menu1.getSort() != null ? menu1.getSort() : 0;
                    Integer sort2 = menu2.getSort() != null ? menu2.getSort() : 0;
                    return sort1.compareTo(sort2);
                })
                // 5. 集合化处理后的菜单项列表
                .collect(Collectors.toList());

        return children;
    }

    /**
     * 查询所有的一级分类
     * 该方法不接受任何参数，查询数据库中父分类ID为0的所有分类实体，并返回其列表。
     *
     * @return 返回包含所有一级分类的CategoryEntity列表
     */
    @Cacheable(value = {"category"}, key = "#root.method.name",sync = true)
    // 代表当前方法的结果需要缓存，注解参数{"category"}指定了缓存名称为"category"，表明缓存结果将存储在名为"category"的缓存区域中。
    @Override
    public List<CategoryEntity> getLevel1Categorys() {
        long l = System.currentTimeMillis();
        log.info("从数据库中查询所有的一级分类");
        // 使用LambdaQueryWrapper构造查询条件，查询父分类ID为0的所有分类
        List<CategoryEntity> categoryEntities = baseMapper.selectList(new LambdaQueryWrapper<CategoryEntity>().eq(CategoryEntity::getParentCid, 0));

        // 打印查询一级分类所花费的时间
        log.info("查询一级分类时间：{} ms", System.currentTimeMillis() - l);

        return categoryEntities;
    }

    @Cacheable(value = {"category"}, key = "#root.methodName")
    @Override
    public Map<String, List<Catalog2Vo>> getCatalogJson() {
        log.info("线程 {}：缓存未命中，查询数据库", Thread.currentThread().getId());

        List<CategoryEntity> selectList = baseMapper.selectList(null);

        // 1. 查询所有的一级分类
        List<CategoryEntity> level1Categorys = getParentCid(selectList, 0L);

        // 2. 使用Stream API将查询结果封装为指定的JSON格式
        Map<String, List<Catalog2Vo>> parentCid = level1Categorys.stream()
                .collect(Collectors
                        .toMap(
                                key -> key.getCatId().toString(), // 将一级分类的ID作为键
                                value -> {
                                    // 查询指定一级分类下的所有二级分类
                                    List<CategoryEntity> categoryEntities = getParentCid(selectList, value.getCatId());
                                    // 将查询到的二级分类封装为Catalog2Vo对象
                                    List<Catalog2Vo> catalog2Vos = null;
                                    if (categoryEntities != null && !categoryEntities.isEmpty()) {
                                        catalog2Vos = categoryEntities.stream()
                                                .map(l2 -> {
                                                            Catalog2Vo catalog2Vo = new Catalog2Vo(value.getCatId().toString(), null, l2.getCatId().toString(), l2.getName());
                                                            // 查询并封装每个二级分类下的三级分类
                                                            List<CategoryEntity> level3Catalog = getParentCid(selectList, l2.getCatId());
                                                            if (level3Catalog != null && !level3Catalog.isEmpty()) {
                                                                // 将三级分类信息封装为Catalog2Vo的内部类Catalog3Vo
                                                                List<Catalog2Vo.Catalog3Vo> collect = level3Catalog.stream()
                                                                        .map(l3 -> new Catalog2Vo.Catalog3Vo(l2.getCatId().toString(), l3.getCatId().toString(), l3.getName()))
                                                                        .collect(Collectors.toList());
                                                                catalog2Vo.setCatalog3List(collect);
                                                            }
                                                            return catalog2Vo;
                                                        }
                                                )
                                                .collect(Collectors.toList());
                                    }
                                    return catalog2Vos;
                                }
                        )
                );

        return parentCid;
    }


    /**
     * 获取分类的JSON数据，首先尝试从缓存中获取，如果缓存中不存在，则从数据库中查询，并将查询结果存入缓存。
     * 使用Redis作为缓存工具，以提升数据的访问速度，并确保数据在不同平台间的兼容性。
     *
     * @return 返回一个Map，其中键是分类的名称，值是该分类下的商品列表。
     */
//    @Override
    public Map<String, List<Catalog2Vo>> getCatalogJson2() {
        /**
         * 压测时会出现堆外内存溢出：OutOfDirectMemoryError
         * 原因： 1. springboot2.0 默认使用lettuce作为redis的客户端，使用netty通信
         *       2. lettuce的bug导致netty堆外内存溢出，-Xmx300m，netty如果没有指定堆外内存，则默认使用-Xmx300m的配置
         *       3. 可以通过修改netty堆外内存，-Dio.netty.maxDirectMemory
         * 解决方案： 1. 不能使用-Dio.netty.maxDirectMemory参数
         *          2. 升级lettuce客户端
         *          3. 使用jedis
         */

        /**
         * 1. 缓存穿透：空结果缓存
         * 2. 缓存雪崩：设置随机过期时间
         * 3. 缓存击穿：加锁
         */
        // 尝试从缓存中获取分类的JSON数据
        String catalogJson = redisTemplate.opsForValue().get("catalogJSON");
        if (StringUtils.isEmpty(catalogJson)) {
            // 缓存中未找到，从数据库查询并存入缓存
            log.info("查询三级分类数据，Redis缓存未命中，查询数据库");

            return getCatalogJsonFromDbWithRedisLock();
        }
        // 将缓存中的JSON数据转换为对象
        log.info("查询三级分类数据，Redis缓存命中，直接返回");
        return JSON.parseObject(catalogJson, new TypeReference<Map<String, List<Catalog2Vo>>>() {
        });
    }


    /**
     * 从数据库中获取分类信息，并使用Redisson实现分布式锁以保证数据一致性。
     *
     * @return 返回一个Map，其中键是分类的名称，值是该分类下的商品列表。
     */
    public Map<String, List<Catalog2Vo>> getCatalogJsonFromDbWithRedissonLock() {
        // 获取分布式锁
        RLock lock = redissonClient.getLock("CatalogJson-lock");
        lock.lock();

        Map<String, List<Catalog2Vo>> dataFromDb;
        try {
            // 尝试从数据库中获取分类信息，确保在持有锁的情况下进行，以避免数据竞争
            dataFromDb = getDataFromDb();
        } finally {
            // 无论如何都释放锁，确保锁的正确释放，避免死锁
            lock.unlock();
        }
        return dataFromDb;

    }

    /**
     * 从数据库中获取分类信息，并使用Redis分布式锁保证数据一致性。
     * 使用UUID作为锁的标识，加锁失败会进行自旋重试。
     *
     * @return 返回一个Map，键是分类的名称，值是该分类下的商品信息列表。
     */
    public Map<String, List<Catalog2Vo>> getCatalogJsonFromDbWithRedisLock() {
        // 尝试使用UUID作为锁标识加锁
        String uuid = UUID.randomUUID().toString();
        Boolean lock = redisTemplate.opsForValue().setIfAbsent("lock", uuid, 300, TimeUnit.SECONDS);
        if (Boolean.TRUE.equals(lock)) {
            // 加锁成功，尝试从数据库获取数据
            log.debug("获取分布式锁成功，线程id = {}", Thread.currentThread().getId());
            Map<String, List<Catalog2Vo>> dataFromDb;
            try {
                dataFromDb = getDataFromDb();
            } finally {
                // 使用Lua脚本确保解锁操作的原子性
                String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
                Long lock1 = redisTemplate
                        .execute(new DefaultRedisScript<Long>(script, Long.class),
                                Arrays.asList("lock"), uuid);
            }
            return dataFromDb;
        } else {
            // 加锁失败，等待一段时间后重试
            log.debug("获取分布式锁失败，等待重试，线程id = {}", Thread.currentThread().getId());
            try {
                Thread.sleep(200);
            } catch (Exception e) {

            }
            return getCatalogJsonFromDbWithRedisLock();
        }
    }

    /**
     * 获取分类的JSON格式数据
     * 该方法用于查询并封装所有的分类信息，包括一级分类、二级分类以及三级分类，并以特定的JSON格式返回。
     * 一级分类对应父级分类ID，二级分类和三级分类则分别属于一级分类的子级。
     *
     * @return Map<String, List < Catalog2Vo>> 分类信息的映射表，键为一级分类的ID，值为该一级分类下所有二级分类及其三级子分类的列表。
     */
    public Map<String, List<Catalog2Vo>> getCatalogJsonFromDbWithLocalLock() {
        // springboot所有组件在容器中默认是单例的
        synchronized (this) {
            // 得到锁以后，应该再去缓存中确定一次，如果没有，才需要继续查询
            // 本地锁，不适用于分布式，必须使用分布式锁
            return getDataFromDb();
        }
    }

    private Map<String, List<Catalog2Vo>> getDataFromDb() {
        String catalogJson = redisTemplate.opsForValue().get("catalogJSON");
        if (!StringUtils.isEmpty(catalogJson)) {
            // 缓存不为空，直接返回
            log.info("线程 {}：拿到了锁，但缓存命中，直接返回", Thread.currentThread().getId());
            return JSON.parseObject(catalogJson, new TypeReference<Map<String, List<Catalog2Vo>>>() {
            });
        }
        log.info("线程 {}：拿到了锁，缓存未命中，查询数据库", Thread.currentThread().getId());

        List<CategoryEntity> selectList = baseMapper.selectList(null);

        // 1. 查询所有的一级分类
        List<CategoryEntity> level1Categorys = getParentCid(selectList, 0L);

        // 2. 使用Stream API将查询结果封装为指定的JSON格式
        Map<String, List<Catalog2Vo>> parentCid = level1Categorys.stream()
                .collect(Collectors
                        .toMap(
                                key -> key.getCatId().toString(), // 将一级分类的ID作为键
                                value -> {
                                    // 查询指定一级分类下的所有二级分类
                                    List<CategoryEntity> categoryEntities = getParentCid(selectList, value.getCatId());
                                    // 将查询到的二级分类封装为Catalog2Vo对象
                                    List<Catalog2Vo> catalog2Vos = null;
                                    if (categoryEntities != null && !categoryEntities.isEmpty()) {
                                        catalog2Vos = categoryEntities.stream()
                                                .map(l2 -> {
                                                            Catalog2Vo catalog2Vo = new Catalog2Vo(value.getCatId().toString(), null, l2.getCatId().toString(), l2.getName());
                                                            // 查询并封装每个二级分类下的三级分类
                                                            List<CategoryEntity> level3Catalog = getParentCid(selectList, l2.getCatId());
                                                            if (level3Catalog != null && !level3Catalog.isEmpty()) {
                                                                // 将三级分类信息封装为Catalog2Vo的内部类Catalog3Vo
                                                                List<Catalog2Vo.Catalog3Vo> collect = level3Catalog.stream()
                                                                        .map(l3 -> new Catalog2Vo.Catalog3Vo(l2.getCatId().toString(), l3.getCatId().toString(), l3.getName()))
                                                                        .collect(Collectors.toList());
                                                                catalog2Vo.setCatalog3List(collect);
                                                            }
                                                            return catalog2Vo;
                                                        }
                                                )
                                                .collect(Collectors.toList());
                                    }
                                    return catalog2Vos;
                                }
                        )
                );
        // 在释放锁之前，要将查到的数据先放入缓存
        redisTemplate.opsForValue().set("catalogJSON", JSON.toJSONString(parentCid), 1, TimeUnit.DAYS);
        return parentCid;
    }


    /**
     * 根据父级分类ID获取所有子分类实体列表。
     *
     * @param selectList 待筛选的分类实体列表。
     * @param parentCid  父级分类的ID。
     * @return 过滤后的包含指定父级ID的分类实体列表。
     */
    private List<CategoryEntity> getParentCid(List<CategoryEntity> selectList, Long parentCid) {
        // 使用Stream API过滤出父级分类ID为指定值的分类实体，并收集到List中
        return selectList.stream()
                .filter(item -> Objects.equals(item.getParentCid(), parentCid))
                .collect(Collectors.toList());
    }
}