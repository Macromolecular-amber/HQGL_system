package com.logistics.service.gy.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.logistics.common.BusinessException;
import com.logistics.common.PageResult;
import com.logistics.dto.gy.RoomPageQuery;
import com.logistics.dto.gy.RoomSaveRequest;
import com.logistics.dto.gy.RoomVO;
import com.logistics.entity.GyRoom;
import com.logistics.repository.GyOccupantRepository;
import com.logistics.repository.GyRoomRepository;
import com.logistics.service.gy.GyRoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.persistence.criteria.Predicate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 公寓基础信息建档服务实现
 */
@Service
@RequiredArgsConstructor
public class GyRoomServiceImpl implements GyRoomService {

    /** 房间状态：空闲 */
    private static final String STATUS_IDLE = "idle";
    /** 房间状态：已入住 */
    private static final String STATUS_OCCUPIED = "occupied";
    /** 入住状态：在住 */
    private static final String OCCUPANT_ACTIVE = "ACTIVE";
    /** 房间类型中文名 */
    private static final Map<String, String> ROOM_TYPE_LABEL_MAP = new HashMap<>();
    /** 房间状态中文名 */
    private static final Map<String, String> STATUS_LABEL_MAP = new HashMap<>();

    static {
        ROOM_TYPE_LABEL_MAP.put("expert_apartment", "专家公寓");
        ROOM_TYPE_LABEL_MAP.put("talent_apartment", "人才公寓");
        STATUS_LABEL_MAP.put("idle", "空闲");
        STATUS_LABEL_MAP.put("occupied", "已入住");
        STATUS_LABEL_MAP.put("repairing", "维修中");
        STATUS_LABEL_MAP.put("reserved", "已预留");
    }

    private final GyRoomRepository roomRepository;
    private final GyOccupantRepository occupantRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public RoomVO save(RoomSaveRequest request) {
        // 房间类型白名单校验
        String roomType = request.getRoomType() == null ? null : request.getRoomType().toLowerCase();
        if (!ROOM_TYPE_LABEL_MAP.containsKey(roomType)) {
            throw new BusinessException("房间类型无效");
        }
        // 楼栋+楼层+房间号唯一性校验（编辑时排除自身）
        List<GyRoom> exists = roomRepository
                .findByBuildingAndFloorAndRoomNoAndIsDeleted(request.getBuilding(), request.getFloor(), request.getRoomNo(), false);
        boolean duplicate = exists.stream()
                .anyMatch(r -> request.getId() == null || !request.getId().equals(r.getId()));
        if (duplicate) {
            throw new BusinessException("同一楼栋、楼层、房间号已存在");
        }

        OffsetDateTime now = OffsetDateTime.now();
        GyRoom room;
        if (request.getId() != null) {
            room = roomRepository.findById(request.getId())
                    .orElseThrow(() -> new BusinessException("房间不存在"));
        } else {
            room = new GyRoom();
            room.setRoomStatus(STATUS_IDLE); // 新增默认空闲
            room.setIsDeleted(false);
            room.setCreateTime(now);
        }
        room.setBuilding(request.getBuilding());
        room.setFloor(request.getFloor());
        room.setRoomNo(request.getRoomNo());
        room.setRoomType(roomType);
        room.setLayout(request.getLayout());
        room.setArea(request.getArea());
        room.setFacilities(serializeFacilities(request.getFacilities()));
        room.setRemark(request.getRemark());
        room.setUpdateTime(now);
        return toVO(roomRepository.save(room));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        GyRoom room = roomRepository.findById(id)
                .orElseThrow(() -> new BusinessException("房间不存在"));
        if (STATUS_OCCUPIED.equalsIgnoreCase(room.getRoomStatus())) {
            throw new BusinessException("已入住的房间不可删除");
        }
        room.setIsDeleted(true);
        room.setUpdateTime(OffsetDateTime.now());
        roomRepository.save(room);
    }

    @Override
    public PageResult<RoomVO> queryPage(RoomPageQuery query) {
        int page = (query.getPage() == null || query.getPage() < 1) ? 1 : query.getPage();
        int size = (query.getSize() == null || query.getSize() < 1) ? 20 : query.getSize();

        Specification<GyRoom> spec = (root, cq, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            // 仅查询未删除
            predicates.add(cb.isFalse(root.get("isDeleted")));
            if (StringUtils.hasText(query.getBuilding())) {
                predicates.add(cb.like(root.get("building"), "%" + query.getBuilding().trim() + "%"));
            }
            if (query.getFloor() != null) {
                predicates.add(cb.equal(root.get("floor"), query.getFloor()));
            }
            if (StringUtils.hasText(query.getRoomType())) {
                predicates.add(cb.equal(cb.lower(root.get("roomType")), query.getRoomType().toLowerCase()));
            }
            if (StringUtils.hasText(query.getRoomStatus())) {
                predicates.add(cb.equal(cb.lower(root.get("roomStatus")), query.getRoomStatus().toLowerCase()));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<GyRoom> result = roomRepository.findAll(spec,
                PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createTime")));
        List<RoomVO> vos = result.getContent().stream().map(this::toVO).collect(Collectors.toList());
        return PageResult.of(result, vos);
    }

    @Override
    public RoomVO getDetail(Long id) {
        GyRoom room = roomRepository.findById(id)
                .orElseThrow(() -> new BusinessException("房间不存在"));
        return toVO(room);
    }

    @Override
    public List<RoomVO> getAvailableRooms(String roomType) {
        // 忽略大小写匹配房间状态/类型，兼容历史预置大写枚举
        Specification<GyRoom> spec = (root, cq, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isFalse(root.get("isDeleted")));
            predicates.add(cb.equal(cb.lower(root.get("roomStatus")), "idle"));
            if (StringUtils.hasText(roomType)) {
                predicates.add(cb.equal(cb.lower(root.get("roomType")), roomType.toLowerCase()));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        List<GyRoom> rooms = roomRepository.findAll(spec,
                Sort.by("building").and(Sort.by("floor")).and(Sort.by("roomNo")));
        return rooms.stream().map(this::toVO).collect(Collectors.toList());
    }

    /**
     * 房间转 VO，补充中文名与入住信息
     */
    private RoomVO toVO(GyRoom room) {
        RoomVO vo = new RoomVO();
        BeanUtils.copyProperties(room, vo);
        vo.setRoomTypeLabel(label(ROOM_TYPE_LABEL_MAP, room.getRoomType()));
        vo.setRoomStatusLabel(label(STATUS_LABEL_MAP, room.getRoomStatus()));
        // 当前入住人数：关联 gy_occupant 统计在住人员
        vo.setOccupantCount((int) occupantRepository.countByRoomIdAndOccupantStatus(room.getId(), OCCUPANT_ACTIVE));
        return vo;
    }

    private String label(Map<String, String> map, String value) {
        if (!StringUtils.hasText(value)) {
            return value;
        }
        return map.getOrDefault(value.toLowerCase(), value);
    }

    /**
     * 配套设施 Map 序列化为 JSON 存储
     */
    private String serializeFacilities(Map<String, List<String>> facilities) {
        if (facilities == null || facilities.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(facilities);
        } catch (JsonProcessingException e) {
            throw new BusinessException("配套设施格式错误");
        }
    }
}
