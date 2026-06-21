package com.club.controller;

import com.club.common.Result;
import com.club.common.annotation.Log;
import com.club.dto.PointDTOs;
import com.club.service.PointService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "积分商城管理", description = "积分查询、流水、商品兑换与管理")
@RestController
@RequestMapping("/api/points")
public class PointController {

    @Autowired
    private PointService pointService;

    @Operation(summary = "获取当前用户积分")
    @GetMapping("/my")
    public Result<?> getMyPoints() {
        return pointService.getMyPoints();
    }

    @Operation(summary = "获取我的积分流水")
    @GetMapping("/my/ledger")
    public Result<?> getMyLedger(@ModelAttribute PointDTOs.LedgerQueryRequest request) {
        return pointService.getMyLedger(request);
    }

    @Operation(summary = "获取商城商品列表")
    @GetMapping("/shop/items")
    public Result<?> getShopItems(@ModelAttribute PointDTOs.ItemQueryRequest request) {
        return pointService.getShopItems(request);
    }

    @Operation(summary = "获取商品详情")
    @GetMapping("/shop/items/{id}")
    public Result<?> getItemDetail(@PathVariable Integer id) {
        return pointService.getItemDetail(id);
    }

    @Operation(summary = "兑换商品")
    @Log("兑换商品")
    @PostMapping("/shop/redeem")
    public Result<?> redeemItem(@Valid @RequestBody PointDTOs.RedeemRequest request) {
        return pointService.redeemItem(request);
    }

    @Operation(summary = "获取我的兑换订单")
    @GetMapping("/my/orders")
    public Result<?> getMyOrders(@ModelAttribute PointDTOs.OrderQueryRequest request) {
        return pointService.getMyOrders(request);
    }

    @Operation(summary = "获取订单详情")
    @GetMapping("/orders/{id}")
    public Result<?> getOrderDetail(@PathVariable Integer id) {
        return pointService.getOrderDetail(id);
    }

    @Operation(summary = "管理员增加/扣减用户积分")
    @Log("调整用户积分")
    @PostMapping("/admin/add-points")
    @PreAuthorize("hasAnyRole('ADMIN', 'UNION_ADMIN')")
    public Result<?> addPoints(@Valid @RequestBody PointDTOs.AddPointsRequest request) {
        return pointService.addPoints(request);
    }

    @Operation(summary = "获取积分规则列表")
    @GetMapping("/rules")
    public Result<?> getPointRules() {
        return pointService.getPointRules();
    }

    @Operation(summary = "创建积分规则")
    @Log("创建积分规则")
    @PostMapping("/rules")
    @PreAuthorize("hasAnyRole('ADMIN', 'UNION_ADMIN')")
    public Result<?> createPointRule(@Valid @RequestBody PointDTOs.PointRuleRequest request) {
        return pointService.createPointRule(request);
    }

    @Operation(summary = "更新积分规则")
    @Log("更新积分规则")
    @PutMapping("/rules/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'UNION_ADMIN')")
    public Result<?> updatePointRule(@PathVariable Integer id, @Valid @RequestBody PointDTOs.PointRuleRequest request) {
        return pointService.updatePointRule(id, request);
    }

    @Operation(summary = "更新规则状态")
    @Log("更新积分规则状态")
    @PutMapping("/rules/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'UNION_ADMIN')")
    public Result<?> updateRuleStatus(@PathVariable Integer id, @RequestParam String status) {
        return pointService.updateRuleStatus(id, status);
    }

    @Operation(summary = "创建商品")
    @Log("创建商城商品")
    @PostMapping("/admin/items")
    @PreAuthorize("hasAnyRole('ADMIN', 'UNION_ADMIN', 'CLUB_LEADER')")
    public Result<?> createItem(@Valid @RequestBody PointDTOs.ShopItemRequest request) {
        return pointService.createItem(request);
    }

    @Operation(summary = "更新商品")
    @Log("更新商城商品")
    @PutMapping("/admin/items/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'UNION_ADMIN', 'CLUB_LEADER')")
    public Result<?> updateItem(@PathVariable Integer id, @Valid @RequestBody PointDTOs.ShopItemRequest request) {
        return pointService.updateItem(id, request);
    }

    @Operation(summary = "删除商品")
    @Log("删除商城商品")
    @DeleteMapping("/admin/items/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'UNION_ADMIN', 'CLUB_LEADER')")
    public Result<?> deleteItem(@PathVariable Integer id) {
        return pointService.deleteItem(id);
    }

    @Operation(summary = "更新商品状态")
    @Log("更新商品状态")
    @PutMapping("/admin/items/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'UNION_ADMIN', 'CLUB_LEADER')")
    public Result<?> updateItemStatus(@PathVariable Integer id, @RequestParam String status) {
        return pointService.updateItemStatus(id, status);
    }

    @Operation(summary = "获取我的社团商品列表")
    @GetMapping("/admin/items/my-club")
    @PreAuthorize("hasAnyRole('ADMIN', 'UNION_ADMIN', 'CLUB_LEADER')")
    public Result<?> getMyClubItems(@ModelAttribute PointDTOs.ItemQueryRequest request) {
        return pointService.getMyClubItems(request);
    }

    @Operation(summary = "获取所有兑换订单（管理员）")
    @GetMapping("/admin/orders")
    @PreAuthorize("hasAnyRole('ADMIN', 'UNION_ADMIN')")
    public Result<?> getAllOrders(@ModelAttribute PointDTOs.OrderQueryRequest request) {
        return pointService.getAllOrders(request);
    }

    @Operation(summary = "更新订单状态")
    @Log("更新兑换订单状态")
    @PutMapping("/admin/orders/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'UNION_ADMIN')")
    public Result<?> updateOrderStatus(@PathVariable Integer id, @RequestParam String status) {
        return pointService.updateOrderStatus(id, status);
    }

    @Operation(summary = "获取指定用户积分")
    @GetMapping("/admin/user/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'UNION_ADMIN')")
    public Result<?> getUserPoints(@PathVariable Integer userId) {
        return pointService.getUserPoints(userId);
    }

    @Operation(summary = "获取用户积分流水（管理员）")
    @GetMapping("/admin/ledger")
    @PreAuthorize("hasAnyRole('ADMIN', 'UNION_ADMIN')")
    public Result<?> getUserLedger(@ModelAttribute PointDTOs.LedgerQueryRequest request) {
        return pointService.getUserLedger(request);
    }
}
