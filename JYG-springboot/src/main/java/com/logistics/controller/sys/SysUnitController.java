package com.logistics.controller.sys;

import com.logistics.common.Result;
import com.logistics.dto.sys.UnitVO;
import com.logistics.entity.SysUnit;
import com.logistics.repository.SysUnitRepository;
import com.logistics.security.RequiresRoles;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 单位管理（基础下拉数据）
 */
@RestController
@RequestMapping("/api/sys/unit")
@RequiredArgsConstructor
@RequiresRoles({"ADMIN"})
public class SysUnitController {

    private final SysUnitRepository sysUnitRepository;

    /**
     * 单位下拉列表
     */
    @GetMapping("/list")
    public Result<List<UnitVO>> list() {
        List<UnitVO> list = sysUnitRepository.findAll(Sort.by(Sort.Direction.ASC, "sortOrder"))
                .stream()
                .filter(u -> u.getIsDeleted() == null || !u.getIsDeleted())
                .map(u -> new UnitVO(u.getId(), u.getUnitName()))
                .collect(Collectors.toList());
        return Result.success(list);
    }
}
