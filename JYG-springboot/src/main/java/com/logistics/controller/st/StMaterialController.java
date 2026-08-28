package com.logistics.controller.st;

import com.logistics.annotation.Log;
import com.logistics.common.PageResult;
import com.logistics.common.Result;
import com.logistics.dto.st.MaterialPageQuery;
import com.logistics.dto.st.MaterialSaveRequest;
import com.logistics.dto.st.MaterialVO;
import com.logistics.service.st.StMaterialService;
import com.logistics.security.RequiresRoles;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

/**
 * 食堂物资档案管理
 */
@RestController
@RequestMapping("/api/st/material")
@RequiredArgsConstructor
public class StMaterialController {

    private final StMaterialService stMaterialService;

    /**
     * 新增或编辑物资
     */
    @RequiresRoles({"BIZ_ADMIN","WAREHOUSE","DEPT_MANAGER"})
    @PostMapping("/save")
    @Log(module="ST", operation="物资建档", type="ADD")
    public Result<MaterialVO> save(@Valid @RequestBody MaterialSaveRequest request) {
        return Result.success(stMaterialService.save(request));
    }

    /**
     * 逻辑删除物资
     */
    @RequiresRoles({"BIZ_ADMIN","WAREHOUSE","DEPT_MANAGER"})
    @DeleteMapping("/{id}")
    @Log(module="ST", operation="删除物资", type="DELETE")
    public Result<Void> delete(@PathVariable Long id) {
        stMaterialService.delete(id);
        return Result.success();
    }

    /**
     * 分页查询物资
     */
    @RequiresRoles({"BIZ_ADMIN","WAREHOUSE","DEPT_MANAGER","DIRECTOR"})
    @GetMapping("/page")
    public Result<PageResult<MaterialVO>> page(MaterialPageQuery query) {
        return Result.success(stMaterialService.queryPage(query));
    }

    /**
     * 物资详情
     */
    @RequiresRoles({"BIZ_ADMIN","WAREHOUSE","DEPT_MANAGER","DIRECTOR"})
    @GetMapping("/{id}")
    public Result<MaterialVO> detail(@PathVariable Long id) {
        return Result.success(stMaterialService.getDetail(id));
    }

    /**
     * 按分类获取物资列表
     */
    @RequiresRoles({"BIZ_ADMIN","WAREHOUSE","DEPT_MANAGER","DIRECTOR"})
    @GetMapping("/category/{category}")
    public Result<List<MaterialVO>> byCategory(@PathVariable String category) {
        return Result.success(stMaterialService.getByCategory(category));
    }
}
