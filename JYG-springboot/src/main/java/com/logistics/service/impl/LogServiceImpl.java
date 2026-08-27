package com.logistics.service.impl;

import com.logistics.common.PageResult;
import com.logistics.dto.sys.LogPageQuery;
import com.logistics.entity.SysOperationLog;
import com.logistics.repository.SysOperationLogRepository;
import com.logistics.service.LogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.persistence.criteria.Predicate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

/**
 * 操作日志服务实现
 */
@Service
@RequiredArgsConstructor
public class LogServiceImpl implements LogService {

    private final SysOperationLogRepository logRepository;

    @Override
    public PageResult<SysOperationLog> queryPage(LogPageQuery query) {
        int page = (query.getPage() == null || query.getPage() < 1) ? 1 : query.getPage();
        int size = (query.getSize() == null || query.getSize() < 1) ? 20 : query.getSize();

        Specification<SysOperationLog> spec = (root, cq, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (StringUtils.hasText(query.getUsername())) {
                predicates.add(cb.like(root.get("username"), "%" + query.getUsername().trim() + "%"));
            }
            if (StringUtils.hasText(query.getModule())) {
                predicates.add(cb.equal(cb.upper(root.get("module")), query.getModule().toUpperCase()));
            }
            if (StringUtils.hasText(query.getOperationType())) {
                predicates.add(cb.equal(cb.upper(root.get("operationType")), query.getOperationType().toUpperCase()));
            }
            if (query.getStartTime() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createTime"),
                        query.getStartTime().atZone(ZoneId.systemDefault()).toOffsetDateTime()));
            }
            if (query.getEndTime() != null) {
                predicates.add(cb.lessThan(root.get("createTime"),
                        query.getEndTime().atZone(ZoneId.systemDefault()).toOffsetDateTime()));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<SysOperationLog> result = logRepository.findAll(spec,
                PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createTime")));
        return PageResult.of(result, result.getContent());
    }
}
