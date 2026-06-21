package com.club.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.club.common.Result;
import com.club.common.RoleConstants;
import com.club.dto.PointDTOs;
import com.club.entity.*;
import com.club.mapper.*;
import com.club.service.PointService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class PointServiceImpl extends ServiceImpl<ShopItemMapper, ShopItem> implements PointService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ClubMapper clubMapper;

    @Autowired
    private PointRuleMapper pointRuleMapper;

    @Autowired
    private PointLedgerMapper pointLedgerMapper;

    @Autowired
    private UserPointsMapper userPointsMapper;

    @Autowired
    private RedeemOrderMapper redeemOrderMapper;

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) return null;
        return userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, auth.getName()));
    }

    @Override
    public Result<?> getMyPoints() {
        User user = getCurrentUser();
        if (user == null) return Result.error("未认证");

        UserPoints userPoints = userPointsMapper.selectOne(
                new LambdaQueryWrapper<UserPoints>().eq(UserPoints::getUserId, user.getId()));

        Map<String, Object> result = new HashMap<>();
        result.put("userId", user.getId());
        result.put("userName", user.getRealName());
        if (userPoints != null) {
            result.put("balance", userPoints.getBalance());
            result.put("totalEarned", userPoints.getTotalEarned());
            result.put("totalSpent", userPoints.getTotalSpent());
        } else {
            result.put("balance", 0);
            result.put("totalEarned", 0);
            result.put("totalSpent", 0);
        }

        return Result.success(result);
    }

    @Override
    public Result<?> getMyLedger(PointDTOs.LedgerQueryRequest request) {
        User user = getCurrentUser();
        if (user == null) return Result.error("未认证");

        LambdaQueryWrapper<PointLedger> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PointLedger::getUserId, user.getId());

        if (request.getRefType() != null && !request.getRefType().isEmpty()) {
            wrapper.eq(PointLedger::getRefType, request.getRefType());
        }
        if (request.getStartTime() != null) {
            wrapper.ge(PointLedger::getCreateTime, request.getStartTime());
        }
        if (request.getEndTime() != null) {
            wrapper.le(PointLedger::getCreateTime, request.getEndTime());
        }
        wrapper.orderByDesc(PointLedger::getCreateTime);

        Page<PointLedger> page = new Page<>(request.getPageNum(), request.getPageSize());
        IPage<PointLedger> pageResult = pointLedgerMapper.selectPage(page, wrapper);

        return Result.success(pageResult);
    }

    @Override
    public Result<?> getShopItems(PointDTOs.ItemQueryRequest request) {
        LambdaQueryWrapper<ShopItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShopItem::getStatus, "ON_SALE");

        if (request.getKeyword() != null && !request.getKeyword().isEmpty()) {
            wrapper.like(ShopItem::getName, request.getKeyword());
        }
        if (request.getClubId() != null) {
            wrapper.and(w -> w.isNull(ShopItem::getClubId).or().eq(ShopItem::getClubId, request.getClubId()));
        }

        wrapper.orderByAsc(ShopItem::getSortOrder).orderByDesc(ShopItem::getCreateTime);

        Page<ShopItem> page = new Page<>(request.getPageNum(), request.getPageSize());
        IPage<ShopItem> pageResult = this.page(page, wrapper);

        List<ShopItem> records = pageResult.getRecords();
        for (ShopItem item : records) {
            if (item.getClubId() != null) {
                Club club = clubMapper.selectById(item.getClubId());
                item.setClubName(club != null ? club.getName() : "");
            } else {
                item.setClubName("全校");
            }
        }

        return Result.success(pageResult);
    }

    @Override
    public Result<?> getItemDetail(Integer id) {
        ShopItem item = this.getById(id);
        if (item == null) return Result.error("商品不存在");

        if (item.getClubId() != null) {
            Club club = clubMapper.selectById(item.getClubId());
            item.setClubName(club != null ? club.getName() : "");
        } else {
            item.setClubName("全校");
        }

        if (item.getCreatorId() != null) {
            User creator = userMapper.selectById(item.getCreatorId());
            item.setCreatorName(creator != null ? creator.getRealName() : "");
        }

        return Result.success(item);
    }

    @Override
    @Transactional
    public Result<?> redeemItem(PointDTOs.RedeemRequest request) {
        User user = getCurrentUser();
        if (user == null) return Result.error("未认证");

        ShopItem item = this.getById(request.getItemId());
        if (item == null) return Result.error("商品不存在");
        if (!"ON_SALE".equals(item.getStatus())) return Result.error("商品已下架");

        int totalCost = item.getCostPoints() * request.getQuantity();

        if (item.getStock() < request.getQuantity()) {
            return Result.error("库存不足");
        }

        UserPoints userPoints = userPointsMapper.selectOne(
                new LambdaQueryWrapper<UserPoints>().eq(UserPoints::getUserId, user.getId()));

        int currentBalance = userPoints != null ? userPoints.getBalance() : 0;
        if (currentBalance < totalCost) {
            return Result.error("积分不足");
        }

        int updatedRows = baseMapper.update(null,
                new LambdaQueryWrapper<ShopItem>()
                        .eq(ShopItem::getId, item.getId())
                        .ge(ShopItem::getStock, request.getQuantity())
                        .setSql("stock = stock - " + request.getQuantity()));

        if (updatedRows == 0) {
            return Result.error("库存不足，请稍后重试");
        }

        int pointsUpdatedRows;
        if (userPoints == null) {
            UserPoints newPoints = new UserPoints();
            newPoints.setUserId(user.getId());
            newPoints.setBalance(0);
            newPoints.setTotalEarned(0);
            newPoints.setTotalSpent(0);
            userPointsMapper.insert(newPoints);
            userPoints = newPoints;
        }

        pointsUpdatedRows = userPointsMapper.update(null,
                new LambdaQueryWrapper<UserPoints>()
                        .eq(UserPoints::getUserId, user.getId())
                        .ge(UserPoints::getBalance, totalCost)
                        .setSql("balance = balance - " + totalCost)
                        .setSql("total_spent = total_spent + " + totalCost));

        if (pointsUpdatedRows == 0) {
            baseMapper.update(null,
                    new LambdaQueryWrapper<ShopItem>()
                            .eq(ShopItem::getId, item.getId())
                            .setSql("stock = stock + " + request.getQuantity()));
            return Result.error("积分不足，请稍后重试");
        }

        UserPoints updatedPoints = userPointsMapper.selectOne(
                new LambdaQueryWrapper<UserPoints>().eq(UserPoints::getUserId, user.getId()));

        String orderNo = generateOrderNo();
        RedeemOrder order = new RedeemOrder();
        order.setOrderNo(orderNo);
        order.setUserId(user.getId());
        order.setItemId(item.getId());
        order.setItemName(item.getName());
        order.setItemImage(item.getImageUrl());
        order.setCostPoints(totalCost);
        order.setQuantity(request.getQuantity());
        order.setStatus("COMPLETED");
        redeemOrderMapper.insert(order);

        PointLedger ledger = new PointLedger();
        ledger.setUserId(user.getId());
        ledger.setDelta(-totalCost);
        ledger.setReason("兑换商品-" + item.getName());
        ledger.setRefType("REDEEM");
        ledger.setRefId(order.getId());
        ledger.setBalanceSnapshot(updatedPoints.getBalance());
        pointLedgerMapper.insert(ledger);

        Map<String, Object> result = new HashMap<>();
        result.put("orderId", order.getId());
        result.put("orderNo", orderNo);
        result.put("totalCost", totalCost);
        result.put("remainingPoints", updatedPoints.getBalance());
        result.put("itemName", item.getName());
        result.put("quantity", request.getQuantity());

        return Result.success(result);
    }

    private String generateOrderNo() {
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        int random = ThreadLocalRandom.current().nextInt(1000, 10000);
        return "PO" + dateStr + random;
    }

    @Override
    public Result<?> getMyOrders(PointDTOs.OrderQueryRequest request) {
        User user = getCurrentUser();
        if (user == null) return Result.error("未认证");

        LambdaQueryWrapper<RedeemOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RedeemOrder::getUserId, user.getId());

        if (request.getItemId() != null) {
            wrapper.eq(RedeemOrder::getItemId, request.getItemId());
        }
        if (request.getStatus() != null && !request.getStatus().isEmpty()) {
            wrapper.eq(RedeemOrder::getStatus, request.getStatus());
        }
        if (request.getStartTime() != null) {
            wrapper.ge(RedeemOrder::getCreateTime, request.getStartTime());
        }
        if (request.getEndTime() != null) {
            wrapper.le(RedeemOrder::getCreateTime, request.getEndTime());
        }
        wrapper.orderByDesc(RedeemOrder::getCreateTime);

        Page<RedeemOrder> page = new Page<>(request.getPageNum(), request.getPageSize());
        IPage<RedeemOrder> pageResult = redeemOrderMapper.selectPage(page, wrapper);

        return Result.success(pageResult);
    }

    @Override
    public Result<?> getOrderDetail(Integer id) {
        User user = getCurrentUser();
        if (user == null) return Result.error("未认证");

        RedeemOrder order = redeemOrderMapper.selectById(id);
        if (order == null) return Result.error("订单不存在");

        if (!order.getUserId().equals(user.getId())
                && !RoleConstants.ADMIN.equals(user.getRole())
                && !RoleConstants.UNION_ADMIN.equals(user.getRole())) {
            return Result.error("无权限查看此订单");
        }

        User orderUser = userMapper.selectById(order.getUserId());
        order.setUserName(orderUser != null ? orderUser.getRealName() : "");
        order.setStudentNo(orderUser != null ? orderUser.getStudentId() : "");

        return Result.success(order);
    }

    @Override
    @Transactional
    public Result<?> createItem(PointDTOs.ShopItemRequest request) {
        User user = getCurrentUser();
        if (user == null) return Result.error("未认证");

        if (!canManageItems(user, request.getClubId())) {
            return Result.error("无权限创建此社团的商品");
        }

        ShopItem item = new ShopItem();
        item.setName(request.getName());
        item.setDescription(request.getDescription());
        item.setImageUrl(request.getImageUrl());
        item.setCostPoints(request.getCostPoints());
        item.setStock(request.getStock());
        item.setClubId(request.getClubId());
        item.setSortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0);
        item.setStatus("ON_SALE");
        item.setCreatorId(user.getId());

        this.save(item);
        return Result.success(item);
    }

    @Override
    @Transactional
    public Result<?> updateItem(Integer id, PointDTOs.ShopItemRequest request) {
        User user = getCurrentUser();
        if (user == null) return Result.error("未认证");

        ShopItem item = this.getById(id);
        if (item == null) return Result.error("商品不存在");

        if (!canManageItems(user, item.getClubId())) {
            return Result.error("无权限修改此商品");
        }

        item.setName(request.getName());
        item.setDescription(request.getDescription());
        item.setImageUrl(request.getImageUrl());
        item.setCostPoints(request.getCostPoints());
        item.setStock(request.getStock());
        item.setSortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0);

        this.updateById(item);
        return Result.success(item);
    }

    @Override
    @Transactional
    public Result<?> deleteItem(Integer id) {
        User user = getCurrentUser();
        if (user == null) return Result.error("未认证");

        ShopItem item = this.getById(id);
        if (item == null) return Result.error("商品不存在");

        if (!canManageItems(user, item.getClubId())) {
            return Result.error("无权限删除此商品");
        }

        this.removeById(id);
        return Result.success();
    }

    @Override
    @Transactional
    public Result<?> updateItemStatus(Integer id, String status) {
        User user = getCurrentUser();
        if (user == null) return Result.error("未认证");

        ShopItem item = this.getById(id);
        if (item == null) return Result.error("商品不存在");

        if (!canManageItems(user, item.getClubId())) {
            return Result.error("无权限修改此商品状态");
        }

        item.setStatus(status);
        this.updateById(item);
        return Result.success(item);
    }

    private boolean canManageItems(User user, Integer clubId) {
        if (RoleConstants.ADMIN.equals(user.getRole()) || RoleConstants.UNION_ADMIN.equals(user.getRole())) {
            return true;
        }
        if (RoleConstants.CLUB_LEADER.equals(user.getRole())) {
            return clubId != null && clubId.equals(user.getClubId());
        }
        return false;
    }

    @Override
    public Result<?> getMyClubItems(PointDTOs.ItemQueryRequest request) {
        User user = getCurrentUser();
        if (user == null) return Result.error("未认证");

        LambdaQueryWrapper<ShopItem> wrapper = new LambdaQueryWrapper<>();

        if (RoleConstants.CLUB_LEADER.equals(user.getRole())) {
            wrapper.eq(ShopItem::getClubId, user.getClubId());
        }

        if (request.getKeyword() != null && !request.getKeyword().isEmpty()) {
            wrapper.like(ShopItem::getName, request.getKeyword());
        }
        if (request.getStatus() != null && !request.getStatus().isEmpty()) {
            wrapper.eq(ShopItem::getStatus, request.getStatus());
        }

        wrapper.orderByDesc(ShopItem::getCreateTime);

        Page<ShopItem> page = new Page<>(request.getPageNum(), request.getPageSize());
        IPage<ShopItem> pageResult = this.page(page, wrapper);

        List<ShopItem> records = pageResult.getRecords();
        for (ShopItem item : records) {
            if (item.getClubId() != null) {
                Club club = clubMapper.selectById(item.getClubId());
                item.setClubName(club != null ? club.getName() : "");
            } else {
                item.setClubName("全校");
            }
        }

        return Result.success(pageResult);
    }

    @Override
    public Result<?> getAllOrders(PointDTOs.OrderQueryRequest request) {
        User user = getCurrentUser();
        if (user == null) return Result.error("未认证");

        LambdaQueryWrapper<RedeemOrder> wrapper = new LambdaQueryWrapper<>();

        if (request.getUserId() != null) {
            wrapper.eq(RedeemOrder::getUserId, request.getUserId());
        }
        if (request.getItemId() != null) {
            wrapper.eq(RedeemOrder::getItemId, request.getItemId());
        }
        if (request.getStatus() != null && !request.getStatus().isEmpty()) {
            wrapper.eq(RedeemOrder::getStatus, request.getStatus());
        }
        if (request.getStartTime() != null) {
            wrapper.ge(RedeemOrder::getCreateTime, request.getStartTime());
        }
        if (request.getEndTime() != null) {
            wrapper.le(RedeemOrder::getCreateTime, request.getEndTime());
        }
        wrapper.orderByDesc(RedeemOrder::getCreateTime);

        Page<RedeemOrder> page = new Page<>(request.getPageNum(), request.getPageSize());
        IPage<RedeemOrder> pageResult = redeemOrderMapper.selectPage(page, wrapper);

        List<RedeemOrder> records = pageResult.getRecords();
        for (RedeemOrder order : records) {
            User orderUser = userMapper.selectById(order.getUserId());
            order.setUserName(orderUser != null ? orderUser.getRealName() : "");
            order.setStudentNo(orderUser != null ? orderUser.getStudentId() : "");
        }

        return Result.success(pageResult);
    }

    @Override
    @Transactional
    public Result<?> updateOrderStatus(Integer id, String status) {
        User user = getCurrentUser();
        if (user == null) return Result.error("未认证");

        RedeemOrder order = redeemOrderMapper.selectById(id);
        if (order == null) return Result.error("订单不存在");

        if (!"CANCELLED".equals(status) && !"COMPLETED".equals(status)) {
            return Result.error("无效的状态值");
        }

        if ("CANCELLED".equals(status) && "COMPLETED".equals(order.getStatus())) {
            ShopItem item = this.getById(order.getItemId());
            if (item != null) {
                baseMapper.update(null,
                        new LambdaQueryWrapper<ShopItem>()
                                .eq(ShopItem::getId, item.getId())
                                .setSql("stock = stock + " + order.getQuantity()));
            }

            userPointsMapper.update(null,
                    new LambdaQueryWrapper<UserPoints>()
                            .eq(UserPoints::getUserId, order.getUserId())
                            .setSql("balance = balance + " + order.getCostPoints())
                            .setSql("total_spent = total_spent - " + order.getCostPoints()));

            UserPoints updatedPoints = userPointsMapper.selectOne(
                    new LambdaQueryWrapper<UserPoints>().eq(UserPoints::getUserId, order.getUserId()));

            PointLedger ledger = new PointLedger();
            ledger.setUserId(order.getUserId());
            ledger.setDelta(order.getCostPoints());
            ledger.setReason("订单取消返还-" + order.getItemName());
            ledger.setRefType("REDEEM_CANCEL");
            ledger.setRefId(order.getId());
            ledger.setBalanceSnapshot(updatedPoints != null ? updatedPoints.getBalance() : order.getCostPoints());
            pointLedgerMapper.insert(ledger);
        }

        order.setStatus(status);
        redeemOrderMapper.updateById(order);

        return Result.success(order);
    }

    @Override
    @Transactional
    public Result<?> addPoints(PointDTOs.AddPointsRequest request) {
        User currentUser = getCurrentUser();
        if (currentUser == null) return Result.error("未认证");

        if (!RoleConstants.ADMIN.equals(currentUser.getRole())
                && !RoleConstants.UNION_ADMIN.equals(currentUser.getRole())) {
            return Result.error("无权限操作");
        }

        User targetUser = userMapper.selectById(request.getUserId());
        if (targetUser == null) return Result.error("用户不存在");

        UserPoints userPoints = userPointsMapper.selectOne(
                new LambdaQueryWrapper<UserPoints>().eq(UserPoints::getUserId, request.getUserId()));

        if (userPoints == null) {
            userPoints = new UserPoints();
            userPoints.setUserId(request.getUserId());
            userPoints.setBalance(request.getPoints());
            userPoints.setTotalEarned(request.getPoints() > 0 ? request.getPoints() : 0);
            userPoints.setTotalSpent(request.getPoints() < 0 ? -request.getPoints() : 0);
            userPointsMapper.insert(userPoints);
        } else {
            if (request.getPoints() > 0) {
                userPointsMapper.update(null,
                        new LambdaQueryWrapper<UserPoints>()
                                .eq(UserPoints::getUserId, request.getUserId())
                                .setSql("balance = balance + " + request.getPoints())
                                .setSql("total_earned = total_earned + " + request.getPoints()));
            } else {
                if (userPoints.getBalance() < -request.getPoints()) {
                    return Result.error("积分不足");
                }
                userPointsMapper.update(null,
                        new LambdaQueryWrapper<UserPoints>()
                                .eq(UserPoints::getUserId, request.getUserId())
                                .ge(UserPoints::getBalance, -request.getPoints())
                                .setSql("balance = balance + " + request.getPoints())
                                .setSql("total_spent = total_spent + " + (-request.getPoints())));
            }
            userPoints = userPointsMapper.selectOne(
                    new LambdaQueryWrapper<UserPoints>().eq(UserPoints::getUserId, request.getUserId()));
        }

        PointLedger ledger = new PointLedger();
        ledger.setUserId(request.getUserId());
        ledger.setDelta(request.getPoints());
        ledger.setReason(request.getReason());
        ledger.setRefType(request.getRefType());
        ledger.setRefId(request.getRefId());
        ledger.setBalanceSnapshot(userPoints.getBalance());
        pointLedgerMapper.insert(ledger);

        return Result.success(userPoints);
    }

    @Override
    public Result<?> getPointRules() {
        List<PointRule> rules = pointRuleMapper.selectList(
                new LambdaQueryWrapper<PointRule>()
                        .eq(PointRule::getStatus, "ACTIVE")
                        .orderByAsc(PointRule::getId));
        return Result.success(rules);
    }

    @Override
    @Transactional
    public Result<?> createPointRule(PointDTOs.PointRuleRequest request) {
        User user = getCurrentUser();
        if (user == null) return Result.error("未认证");

        if (!RoleConstants.ADMIN.equals(user.getRole()) && !RoleConstants.UNION_ADMIN.equals(user.getRole())) {
            return Result.error("无权限操作");
        }

        PointRule existing = pointRuleMapper.selectOne(
                new LambdaQueryWrapper<PointRule>().eq(PointRule::getRuleCode, request.getRuleCode()));
        if (existing != null) {
            return Result.error("规则编码已存在");
        }

        PointRule rule = new PointRule();
        rule.setRuleCode(request.getRuleCode());
        rule.setRuleName(request.getRuleName());
        rule.setRuleJson(request.getRuleJson());
        rule.setDescription(request.getDescription());
        rule.setStatus("ACTIVE");
        rule.setCreatorId(user.getId());

        pointRuleMapper.insert(rule);
        return Result.success(rule);
    }

    @Override
    @Transactional
    public Result<?> updatePointRule(Integer id, PointDTOs.PointRuleRequest request) {
        User user = getCurrentUser();
        if (user == null) return Result.error("未认证");

        if (!RoleConstants.ADMIN.equals(user.getRole()) && !RoleConstants.UNION_ADMIN.equals(user.getRole())) {
            return Result.error("无权限操作");
        }

        PointRule rule = pointRuleMapper.selectById(id);
        if (rule == null) return Result.error("规则不存在");

        rule.setRuleName(request.getRuleName());
        rule.setRuleJson(request.getRuleJson());
        rule.setDescription(request.getDescription());

        pointRuleMapper.updateById(rule);
        return Result.success(rule);
    }

    @Override
    @Transactional
    public Result<?> updateRuleStatus(Integer id, String status) {
        User user = getCurrentUser();
        if (user == null) return Result.error("未认证");

        if (!RoleConstants.ADMIN.equals(user.getRole()) && !RoleConstants.UNION_ADMIN.equals(user.getRole())) {
            return Result.error("无权限操作");
        }

        PointRule rule = pointRuleMapper.selectById(id);
        if (rule == null) return Result.error("规则不存在");

        rule.setStatus(status);
        pointRuleMapper.updateById(rule);
        return Result.success(rule);
    }

    @Override
    public Result<?> getUserPoints(Integer userId) {
        User currentUser = getCurrentUser();
        if (currentUser == null) return Result.error("未认证");

        if (!RoleConstants.ADMIN.equals(currentUser.getRole())
                && !RoleConstants.UNION_ADMIN.equals(currentUser.getRole())) {
            return Result.error("无权限操作");
        }

        User targetUser = userMapper.selectById(userId);
        if (targetUser == null) return Result.error("用户不存在");

        UserPoints userPoints = userPointsMapper.selectOne(
                new LambdaQueryWrapper<UserPoints>().eq(UserPoints::getUserId, userId));

        Map<String, Object> result = new HashMap<>();
        result.put("userId", userId);
        result.put("userName", targetUser.getRealName());
        result.put("studentNo", targetUser.getStudentId());
        if (userPoints != null) {
            result.put("balance", userPoints.getBalance());
            result.put("totalEarned", userPoints.getTotalEarned());
            result.put("totalSpent", userPoints.getTotalSpent());
        } else {
            result.put("balance", 0);
            result.put("totalEarned", 0);
            result.put("totalSpent", 0);
        }

        return Result.success(result);
    }

    @Override
    public Result<?> getUserLedger(PointDTOs.LedgerQueryRequest request) {
        User currentUser = getCurrentUser();
        if (currentUser == null) return Result.error("未认证");

        if (!RoleConstants.ADMIN.equals(currentUser.getRole())
                && !RoleConstants.UNION_ADMIN.equals(currentUser.getRole())) {
            return Result.error("无权限操作");
        }

        LambdaQueryWrapper<PointLedger> wrapper = new LambdaQueryWrapper<>();

        if (request.getUserId() != null) {
            wrapper.eq(PointLedger::getUserId, request.getUserId());
        }
        if (request.getRefType() != null && !request.getRefType().isEmpty()) {
            wrapper.eq(PointLedger::getRefType, request.getRefType());
        }
        if (request.getStartTime() != null) {
            wrapper.ge(PointLedger::getCreateTime, request.getStartTime());
        }
        if (request.getEndTime() != null) {
            wrapper.le(PointLedger::getCreateTime, request.getEndTime());
        }
        wrapper.orderByDesc(PointLedger::getCreateTime);

        Page<PointLedger> page = new Page<>(request.getPageNum(), request.getPageSize());
        IPage<PointLedger> pageResult = pointLedgerMapper.selectPage(page, wrapper);

        List<PointLedger> records = pageResult.getRecords();
        for (PointLedger ledger : records) {
            User u = userMapper.selectById(ledger.getUserId());
            ledger.setUserName(u != null ? u.getRealName() : "");
            ledger.setStudentNo(u != null ? u.getStudentId() : "");
        }

        return Result.success(pageResult);
    }
}
