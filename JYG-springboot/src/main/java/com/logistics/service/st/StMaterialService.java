package com.logistics.service.st;

import com.logistics.common.PageResult;
import com.logistics.dto.st.MaterialPageQuery;
import com.logistics.dto.st.MaterialSaveRequest;
import com.logistics.dto.st.MaterialVO;

import java.util.List;

/**
 * 食堂物资档案管理服务
 */
public interface StMaterialService {

    /**
     * 新增或编辑物资
     */
    MaterialVO save(MaterialSaveRequest request);

    /**
     * 逻辑删除物资
     */
    void delete(Long id);

    /**
     * 分页查询物资
     */
    PageResult<MaterialVO> queryPage(MaterialPageQuery query);

    /**
     * 物资详情
     */
    MaterialVO getDetail(Long id);

    /**
     * 按分类获取物资列表
     */
    List<MaterialVO> getByCategory(String category);
}
