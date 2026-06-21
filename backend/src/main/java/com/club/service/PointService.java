package com.club.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.club.common.Result;
import com.club.dto.PointDTOs;
import com.club.entity.ShopItem;
import com.club.entity.UserPoints;

public interface PointService extends IService<ShopItem> {
    Result<?> getMyPoints();
    Result<?> getMyLedger(PointDTOs.LedgerQueryRequest request);
    Result<?> getShopItems(PointDTOs.ItemQueryRequest request);
    Result<?> getItemDetail(Integer id);
    Result<?> redeemItem(PointDTOs.RedeemRequest request);
    Result<?> getMyOrders(PointDTOs.OrderQueryRequest request);
    Result<?> getOrderDetail(Integer id);

    Result<?> createItem(PointDTOs.ShopItemRequest request);
    Result<?> updateItem(Integer id, PointDTOs.ShopItemRequest request);
    Result<?> deleteItem(Integer id);
    Result<?> updateItemStatus(Integer id, String status);
    Result<?> getMyClubItems(PointDTOs.ItemQueryRequest request);
    Result<?> getAllOrders(PointDTOs.OrderQueryRequest request);
    Result<?> updateOrderStatus(Integer id, String status);

    Result<?> addPoints(PointDTOs.AddPointsRequest request);
    Result<?> getPointRules();
    Result<?> createPointRule(PointDTOs.PointRuleRequest request);
    Result<?> updatePointRule(Integer id, PointDTOs.PointRuleRequest request);
    Result<?> updateRuleStatus(Integer id, String status);

    Result<?> getUserPoints(Integer userId);
    Result<?> getUserLedger(PointDTOs.LedgerQueryRequest request);
}
