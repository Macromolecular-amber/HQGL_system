-- ============================================================
-- 测试账号创建脚本：为 8 个角色各创建一个专属测试账号
-- 数据库：JYGDataBase
-- 密码：123456（BCrypt）
-- 执行方式：
--   psql -U postgres -d JYGDataBase -f create_test_accounts.sql
--   （或 Windows: set PGPASSWORD=root 后执行）
-- 可重复执行（幂等）：账号存在则更新，角色关联存在则跳过
-- ============================================================

BEGIN;

-- 1. 插入 8 个测试用户（username 唯一，存在则更新）
INSERT INTO sys_user (username, password, real_name, phone, email, unit_id, user_type, user_status, create_by, is_deleted)
VALUES
  ('test_admin',     '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '测试管理员',     '13900000001', 'test_admin@test.com',     1, 'ADMIN',        'ACTIVE', 1, false),
  ('test_biz',       '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '测试业务管理员', '13900000002', 'test_biz@test.com',       1, 'BIZ_ADMIN',    'ACTIVE', 1, false),
  ('test_director',  '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '测试局领导',     '13900000003', 'test_director@test.com',  1, 'DIRECTOR',     'ACTIVE', 1, false),
  ('test_dept',      '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '测试部门管理员', '13900000004', 'test_dept@test.com',      1, 'DEPT_MANAGER', 'ACTIVE', 1, false),
  ('test_warehouse', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '测试仓库管理员', '13900000005', 'test_warehouse@test.com', 1, 'WAREHOUSE',    'ACTIVE', 1, false),
  ('test_user',      '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '测试普通用户',   '13900000006', 'test_user@test.com',      1, 'USER',         'ACTIVE', 1, false),
  ('test_driver',    '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '测试驾驶员',     '13900000007', 'test_driver@test.com',    1, 'DRIVER',       'ACTIVE', 1, false),
  ('test_cleaner',   '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '测试保洁员',     '13900000008', 'test_cleaner@test.com',   1, 'CLEANER',      'ACTIVE', 1, false)
ON CONFLICT (username) WHERE is_deleted = false
DO UPDATE SET
  password = EXCLUDED.password,
  real_name = EXCLUDED.real_name,
  phone = EXCLUDED.phone,
  email = EXCLUDED.email,
  unit_id = EXCLUDED.unit_id,
  user_type = EXCLUDED.user_type,
  user_status = EXCLUDED.user_status,
  is_deleted = false;

-- 2. 插入用户-角色关联（user_id + role_id 已存在则跳过）
INSERT INTO sys_user_role (user_id, role_id, create_by)
SELECT u.id, r.id, 1 FROM sys_user u, sys_role r
WHERE u.username = 'test_admin' AND r.role_code = 'ADMIN'
ON CONFLICT (user_id, role_id) DO NOTHING;

INSERT INTO sys_user_role (user_id, role_id, create_by)
SELECT u.id, r.id, 1 FROM sys_user u, sys_role r
WHERE u.username = 'test_biz' AND r.role_code = 'BIZ_ADMIN'
ON CONFLICT (user_id, role_id) DO NOTHING;

INSERT INTO sys_user_role (user_id, role_id, create_by)
SELECT u.id, r.id, 1 FROM sys_user u, sys_role r
WHERE u.username = 'test_director' AND r.role_code = 'DIRECTOR'
ON CONFLICT (user_id, role_id) DO NOTHING;

INSERT INTO sys_user_role (user_id, role_id, create_by)
SELECT u.id, r.id, 1 FROM sys_user u, sys_role r
WHERE u.username = 'test_dept' AND r.role_code = 'DEPT_MANAGER'
ON CONFLICT (user_id, role_id) DO NOTHING;

INSERT INTO sys_user_role (user_id, role_id, create_by)
SELECT u.id, r.id, 1 FROM sys_user u, sys_role r
WHERE u.username = 'test_warehouse' AND r.role_code = 'WAREHOUSE'
ON CONFLICT (user_id, role_id) DO NOTHING;

INSERT INTO sys_user_role (user_id, role_id, create_by)
SELECT u.id, r.id, 1 FROM sys_user u, sys_role r
WHERE u.username = 'test_user' AND r.role_code = 'USER'
ON CONFLICT (user_id, role_id) DO NOTHING;

INSERT INTO sys_user_role (user_id, role_id, create_by)
SELECT u.id, r.id, 1 FROM sys_user u, sys_role r
WHERE u.username = 'test_driver' AND r.role_code = 'DRIVER'
ON CONFLICT (user_id, role_id) DO NOTHING;

INSERT INTO sys_user_role (user_id, role_id, create_by)
SELECT u.id, r.id, 1 FROM sys_user u, sys_role r
WHERE u.username = 'test_cleaner' AND r.role_code = 'CLEANER'
ON CONFLICT (user_id, role_id) DO NOTHING;

COMMIT;
