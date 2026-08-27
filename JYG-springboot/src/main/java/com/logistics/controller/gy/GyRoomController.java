package com.logistics.controller.gy;

import com.logistics.annotation.Log;
import com.logistics.common.PageResult;
import com.logistics.common.Result;
import com.logistics.dto.gy.RoomPageQuery;
import com.logistics.dto.gy.RoomSaveRequest;
import com.logistics.dto.gy.RoomVO;
import com.logistics.service.gy.GyRoomService;
import com.logistics.security.RequiresRoles;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

/**
 * 公寓基础信息建档
 */
@RestController
@RequestMapping("/api/gy/room")
@RequiredArgsConstructor
public class GyRoomController {

    private final GyRoomService gyRoomService;

    /**
     * 新增或编辑房间
     */
    @RequiresRoles({"BIZ_ADMIN","WAREHOUSE"})
    @PostMapping("/save")
    @Log(module="GY", operation="房间建档", type="ADD")
    public Result<RoomVO> save(@Valid @RequestBody RoomSaveRequest request) {
        return Result.success(gyRoomService.save(request));
    }

    /**
     * 逻辑删除房间
     */
    @RequiresRoles({"BIZ_ADMIN","WAREHOUSE"})
    @DeleteMapping("/{id}")
    @Log(module="GY", operation="删除房间", type="DELETE")
    public Result<Void> delete(@PathVariable Long id) {
        gyRoomService.delete(id);
        return Result.success();
    }

    /**
     * 分页查询房间
     */
    @RequiresRoles({"USER","BIZ_ADMIN","WAREHOUSE","DIRECTOR"})
    @GetMapping("/page")
    public Result<PageResult<RoomVO>> page(RoomPageQuery query) {
        return Result.success(gyRoomService.queryPage(query));
    }

    /**
     * 房间详情
     */
    @RequiresRoles({"USER","BIZ_ADMIN","WAREHOUSE","DIRECTOR"})
    @GetMapping("/{id}")
    public Result<RoomVO> detail(@PathVariable Long id) {
        return Result.success(gyRoomService.getDetail(id));
    }

    /**
     * 获取空闲房间（供分配选择）
     */
    @RequiresRoles({"USER","BIZ_ADMIN","WAREHOUSE","DIRECTOR"})
    @GetMapping("/available")
    public Result<List<RoomVO>> available(@RequestParam(required = false) String roomType) {
        return Result.success(gyRoomService.getAvailableRooms(roomType));
    }
}
