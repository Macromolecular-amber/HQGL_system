package com.logistics.service.gc;

import com.logistics.common.PageResult;
import com.logistics.dto.gc.AssetApplyRequest;
import com.logistics.dto.gc.AssetAuditRequest;
import com.logistics.dto.gc.AssetDetailVO;
import com.logistics.dto.gc.AssetListVO;
import com.logistics.dto.gc.AssetPageQuery;
import com.logistics.dto.gc.AssetQuery;
import com.logistics.dto.gc.AssetVO;

import java.util.List;

/**
 * 公物仓资产服务
 */
public interface GcAssetService {

    /**
     * 提交入仓申请
     */
    AssetVO apply(AssetApplyRequest request);

    /**
     * 审核入仓申请
     */
    void audit(AssetAuditRequest request);

    /**
     * 分页查询资产
     */
    PageResult<AssetVO> queryPage(AssetPageQuery query);

    /**
     * 查询资产详情
     */
    AssetVO getDetail(Long id);

    /**
     * 分页查询资产列表（多条件筛选）
     */
    PageResult<AssetListVO> queryAssetList(AssetQuery query);

    /**
     * 查询资产详情（完整字段）
     */
    AssetDetailVO getAssetDetail(Long id);

    /**
     * 查询所有在仓资产（可带分类筛选），供借用/调剂选择
     */
    List<AssetListVO> listAvailableAssets(String categoryCode);
}
