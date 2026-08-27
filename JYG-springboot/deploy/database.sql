--
-- PostgreSQL database dump
--

\restrict qcKPAO2fdZqqi31xACapWZe1fNrnXLnxKflZsm9o0xXnI0sVaDZ15DjrafJcTSX

-- Dumped from database version 17.11
-- Dumped by pg_dump version 17.11

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

ALTER TABLE IF EXISTS ONLY public.gc_transfer_detail DROP CONSTRAINT IF EXISTS gc_transfer_detail_transfer_order_id_fkey;
ALTER TABLE IF EXISTS ONLY public.gc_transfer_detail DROP CONSTRAINT IF EXISTS gc_transfer_detail_asset_id_fkey;
ALTER TABLE IF EXISTS ONLY public.sys_user_role DROP CONSTRAINT IF EXISTS fk_user_role_user;
ALTER TABLE IF EXISTS ONLY public.sys_user_role DROP CONSTRAINT IF EXISTS fk_user_role_role;
ALTER TABLE IF EXISTS ONLY public.st_purchase_detail DROP CONSTRAINT IF EXISTS fk_st_purchase_detail_order;
ALTER TABLE IF EXISTS ONLY public.st_purchase_detail DROP CONSTRAINT IF EXISTS fk_st_purchase_detail_material;
ALTER TABLE IF EXISTS ONLY public.st_inventory_record DROP CONSTRAINT IF EXISTS fk_st_inventory_material;
ALTER TABLE IF EXISTS ONLY public.gc_return_order DROP CONSTRAINT IF EXISTS fk_return_borrow;
ALTER TABLE IF EXISTS ONLY public.cl_repair_order DROP CONSTRAINT IF EXISTS fk_repair_vehicle;
ALTER TABLE IF EXISTS ONLY public.gy_occupant DROP CONSTRAINT IF EXISTS fk_occupant_room;
ALTER TABLE IF EXISTS ONLY public.gy_repair_order DROP CONSTRAINT IF EXISTS fk_gy_repair_room;
ALTER TABLE IF EXISTS ONLY public.cl_dispatch_order DROP CONSTRAINT IF EXISTS fk_dispatch_vehicle;
ALTER TABLE IF EXISTS ONLY public.cl_dispatch_order DROP CONSTRAINT IF EXISTS fk_dispatch_apply;
ALTER TABLE IF EXISTS ONLY public.cl_cost_detail DROP CONSTRAINT IF EXISTS fk_cost_vehicle;
ALTER TABLE IF EXISTS ONLY public.gy_cleaning_order DROP CONSTRAINT IF EXISTS fk_cleaning_room;
ALTER TABLE IF EXISTS ONLY public.gc_borrow_detail DROP CONSTRAINT IF EXISTS fk_borrow_detail_order;
ALTER TABLE IF EXISTS ONLY public.gc_borrow_detail DROP CONSTRAINT IF EXISTS fk_borrow_detail_asset;
ALTER TABLE IF EXISTS ONLY public.cl_track_point DROP CONSTRAINT IF EXISTS cl_track_point_vehicle_id_fkey;
ALTER TABLE IF EXISTS ONLY public.cl_track_point DROP CONSTRAINT IF EXISTS cl_track_point_dispatch_id_fkey;
DROP TRIGGER IF EXISTS trg_sys_user_update_time ON public.sys_user;
DROP TRIGGER IF EXISTS trg_sys_unit_update_time ON public.sys_unit;
DROP TRIGGER IF EXISTS trg_sys_role_update_time ON public.sys_role;
DROP TRIGGER IF EXISTS trg_sys_config_update_time ON public.sys_config;
DROP TRIGGER IF EXISTS trg_st_purchase_order_update_time ON public.st_purchase_order;
DROP TRIGGER IF EXISTS trg_st_purchase_detail_update_time ON public.st_purchase_detail;
DROP TRIGGER IF EXISTS trg_st_meal_reservation_update_time ON public.st_meal_reservation;
DROP TRIGGER IF EXISTS trg_st_material_update_time ON public.st_material;
DROP TRIGGER IF EXISTS trg_pay_account_update_time ON public.pay_account;
DROP TRIGGER IF EXISTS trg_gy_room_update_time ON public.gy_room;
DROP TRIGGER IF EXISTS trg_gy_repair_order_update_time ON public.gy_repair_order;
DROP TRIGGER IF EXISTS trg_gy_occupant_update_time ON public.gy_occupant;
DROP TRIGGER IF EXISTS trg_gy_cleaning_order_update_time ON public.gy_cleaning_order;
DROP TRIGGER IF EXISTS trg_gc_transfer_order_update_time ON public.gc_transfer_order;
DROP TRIGGER IF EXISTS trg_gc_return_order_update_time ON public.gc_return_order;
DROP TRIGGER IF EXISTS trg_gc_logistics_order_update_time ON public.gc_logistics_order;
DROP TRIGGER IF EXISTS trg_gc_borrow_order_update_time ON public.gc_borrow_order;
DROP TRIGGER IF EXISTS trg_gc_borrow_detail_update_time ON public.gc_borrow_detail;
DROP TRIGGER IF EXISTS trg_gc_asset_card_update_time ON public.gc_asset_card;
DROP TRIGGER IF EXISTS trg_cl_vehicle_archive_update_time ON public.cl_vehicle_archive;
DROP TRIGGER IF EXISTS trg_cl_repair_order_update_time ON public.cl_repair_order;
DROP TRIGGER IF EXISTS trg_cl_dispatch_order_update_time ON public.cl_dispatch_order;
DROP TRIGGER IF EXISTS trg_cl_cost_detail_update_time ON public.cl_cost_detail;
DROP TRIGGER IF EXISTS trg_cl_apply_order_update_time ON public.cl_apply_order;
DROP INDEX IF EXISTS public.uk_sys_username;
DROP INDEX IF EXISTS public.uk_sys_user_role;
DROP INDEX IF EXISTS public.uk_sys_unit_code;
DROP INDEX IF EXISTS public.uk_sys_role_code;
DROP INDEX IF EXISTS public.uk_sys_phone;
DROP INDEX IF EXISTS public.uk_sys_config_key;
DROP INDEX IF EXISTS public.uk_st_purchase_no;
DROP INDEX IF EXISTS public.uk_st_material_code;
DROP INDEX IF EXISTS public.uk_pay_user_type;
DROP INDEX IF EXISTS public.uk_pay_trans_no;
DROP INDEX IF EXISTS public.uk_pay_account_no;
DROP INDEX IF EXISTS public.uk_gy_repair_no;
DROP INDEX IF EXISTS public.uk_gy_cleaning_no;
DROP INDEX IF EXISTS public.uk_gy_building_room;
DROP INDEX IF EXISTS public.uk_gc_transfer_no;
DROP INDEX IF EXISTS public.uk_gc_return_no;
DROP INDEX IF EXISTS public.uk_gc_logistics_no;
DROP INDEX IF EXISTS public.uk_gc_borrow_no;
DROP INDEX IF EXISTS public.uk_gc_asset_rfid;
DROP INDEX IF EXISTS public.uk_gc_asset_code;
DROP INDEX IF EXISTS public.uk_cl_repair_no;
DROP INDEX IF EXISTS public.uk_cl_plate_number;
DROP INDEX IF EXISTS public.uk_cl_frame_no;
DROP INDEX IF EXISTS public.uk_cl_dispatch_no;
DROP INDEX IF EXISTS public.uk_cl_apply_no;
DROP INDEX IF EXISTS public.idx_sys_user_unit;
DROP INDEX IF EXISTS public.idx_sys_user_type;
DROP INDEX IF EXISTS public.idx_sys_user_status;
DROP INDEX IF EXISTS public.idx_sys_user_role_user;
DROP INDEX IF EXISTS public.idx_sys_unit_parent;
DROP INDEX IF EXISTS public.idx_sys_message_receiver;
DROP INDEX IF EXISTS public.idx_sys_message_read;
DROP INDEX IF EXISTS public.idx_sys_message_create_time;
DROP INDEX IF EXISTS public.idx_sys_log_user;
DROP INDEX IF EXISTS public.idx_sys_log_type;
DROP INDEX IF EXISTS public.idx_sys_log_time;
DROP INDEX IF EXISTS public.idx_sys_log_module;
DROP INDEX IF EXISTS public.idx_sys_config_group;
DROP INDEX IF EXISTS public.idx_st_waste_date;
DROP INDEX IF EXISTS public.idx_st_purchase_supplier;
DROP INDEX IF EXISTS public.idx_st_purchase_status;
DROP INDEX IF EXISTS public.idx_st_purchase_effective_end;
DROP INDEX IF EXISTS public.idx_st_purchase_detail_order;
DROP INDEX IF EXISTS public.idx_st_purchase_detail_material;
DROP INDEX IF EXISTS public.idx_st_purchase_create_time;
DROP INDEX IF EXISTS public.idx_st_meal_user;
DROP INDEX IF EXISTS public.idx_st_meal_unit;
DROP INDEX IF EXISTS public.idx_st_meal_type;
DROP INDEX IF EXISTS public.idx_st_meal_date;
DROP INDEX IF EXISTS public.idx_st_material_name;
DROP INDEX IF EXISTS public.idx_st_material_category;
DROP INDEX IF EXISTS public.idx_st_inventory_type;
DROP INDEX IF EXISTS public.idx_st_inventory_material;
DROP INDEX IF EXISTS public.idx_st_inventory_create_time;
DROP INDEX IF EXISTS public.idx_st_inventory_business;
DROP INDEX IF EXISTS public.idx_pay_trans_user;
DROP INDEX IF EXISTS public.idx_pay_trans_status;
DROP INDEX IF EXISTS public.idx_pay_trans_pay_time;
DROP INDEX IF EXISTS public.idx_pay_trans_card;
DROP INDEX IF EXISTS public.idx_pay_trans_biz;
DROP INDEX IF EXISTS public.idx_pay_trans_account;
DROP INDEX IF EXISTS public.idx_pay_account_user;
DROP INDEX IF EXISTS public.idx_pay_account_card;
DROP INDEX IF EXISTS public.idx_gy_room_type;
DROP INDEX IF EXISTS public.idx_gy_room_status;
DROP INDEX IF EXISTS public.idx_gy_room_occupant;
DROP INDEX IF EXISTS public.idx_gy_repair_status;
DROP INDEX IF EXISTS public.idx_gy_repair_room;
DROP INDEX IF EXISTS public.idx_gy_repair_applicant;
DROP INDEX IF EXISTS public.idx_gy_occupant_unit;
DROP INDEX IF EXISTS public.idx_gy_occupant_status;
DROP INDEX IF EXISTS public.idx_gy_occupant_room;
DROP INDEX IF EXISTS public.idx_gy_occupant_name;
DROP INDEX IF EXISTS public.idx_gy_occupant_expected_leave;
DROP INDEX IF EXISTS public.idx_gy_occupant_checkin;
DROP INDEX IF EXISTS public.idx_gy_cleaning_time;
DROP INDEX IF EXISTS public.idx_gy_cleaning_status;
DROP INDEX IF EXISTS public.idx_gy_cleaning_room;
DROP INDEX IF EXISTS public.idx_gc_transfer_type;
DROP INDEX IF EXISTS public.idx_gc_transfer_status;
DROP INDEX IF EXISTS public.idx_gc_transfer_applicant;
DROP INDEX IF EXISTS public.idx_gc_return_status;
DROP INDEX IF EXISTS public.idx_gc_return_borrow;
DROP INDEX IF EXISTS public.idx_gc_logistics_status;
DROP INDEX IF EXISTS public.idx_gc_logistics_sign_time;
DROP INDEX IF EXISTS public.idx_gc_logistics_business;
DROP INDEX IF EXISTS public.idx_gc_borrow_status;
DROP INDEX IF EXISTS public.idx_gc_borrow_end;
DROP INDEX IF EXISTS public.idx_gc_borrow_detail_status;
DROP INDEX IF EXISTS public.idx_gc_borrow_detail_order;
DROP INDEX IF EXISTS public.idx_gc_borrow_detail_asset;
DROP INDEX IF EXISTS public.idx_gc_borrow_create_time;
DROP INDEX IF EXISTS public.idx_gc_borrow_applicant_unit;
DROP INDEX IF EXISTS public.idx_gc_borrow_applicant;
DROP INDEX IF EXISTS public.idx_gc_asset_warehouse;
DROP INDEX IF EXISTS public.idx_gc_asset_status;
DROP INDEX IF EXISTS public.idx_gc_asset_purchase_date;
DROP INDEX IF EXISTS public.idx_gc_asset_owner_unit;
DROP INDEX IF EXISTS public.idx_gc_asset_current_use_unit;
DROP INDEX IF EXISTS public.idx_gc_asset_create_time;
DROP INDEX IF EXISTS public.idx_gc_asset_category;
DROP INDEX IF EXISTS public.idx_cl_vehicle_unit;
DROP INDEX IF EXISTS public.idx_cl_vehicle_type;
DROP INDEX IF EXISTS public.idx_cl_vehicle_status;
DROP INDEX IF EXISTS public.idx_cl_vehicle_next_maintenance;
DROP INDEX IF EXISTS public.idx_cl_vehicle_insurance_end;
DROP INDEX IF EXISTS public.idx_cl_track_vehicle_time;
DROP INDEX IF EXISTS public.idx_cl_track_dispatch;
DROP INDEX IF EXISTS public.idx_cl_repair_vehicle;
DROP INDEX IF EXISTS public.idx_cl_repair_type;
DROP INDEX IF EXISTS public.idx_cl_repair_status;
DROP INDEX IF EXISTS public.idx_cl_dispatch_vehicle;
DROP INDEX IF EXISTS public.idx_cl_dispatch_status;
DROP INDEX IF EXISTS public.idx_cl_dispatch_driver;
DROP INDEX IF EXISTS public.idx_cl_dispatch_apply;
DROP INDEX IF EXISTS public.idx_cl_dispatch_actual_start;
DROP INDEX IF EXISTS public.idx_cl_cost_vehicle;
DROP INDEX IF EXISTS public.idx_cl_cost_type;
DROP INDEX IF EXISTS public.idx_cl_cost_time;
DROP INDEX IF EXISTS public.idx_cl_cost_status;
DROP INDEX IF EXISTS public.idx_cl_apply_unit;
DROP INDEX IF EXISTS public.idx_cl_apply_status;
DROP INDEX IF EXISTS public.idx_cl_apply_start_time;
DROP INDEX IF EXISTS public.idx_cl_apply_applicant;
ALTER TABLE IF EXISTS ONLY public.sys_user_role DROP CONSTRAINT IF EXISTS sys_user_role_pkey;
ALTER TABLE IF EXISTS ONLY public.sys_user DROP CONSTRAINT IF EXISTS sys_user_pkey;
ALTER TABLE IF EXISTS ONLY public.sys_unit DROP CONSTRAINT IF EXISTS sys_unit_pkey;
ALTER TABLE IF EXISTS ONLY public.sys_role DROP CONSTRAINT IF EXISTS sys_role_pkey;
ALTER TABLE IF EXISTS ONLY public.sys_operation_log DROP CONSTRAINT IF EXISTS sys_operation_log_pkey;
ALTER TABLE IF EXISTS ONLY public.sys_message DROP CONSTRAINT IF EXISTS sys_message_pkey;
ALTER TABLE IF EXISTS ONLY public.sys_config DROP CONSTRAINT IF EXISTS sys_config_pkey;
ALTER TABLE IF EXISTS ONLY public.st_purchase_order DROP CONSTRAINT IF EXISTS st_purchase_order_pkey;
ALTER TABLE IF EXISTS ONLY public.st_purchase_detail DROP CONSTRAINT IF EXISTS st_purchase_detail_pkey;
ALTER TABLE IF EXISTS ONLY public.st_meal_reservation DROP CONSTRAINT IF EXISTS st_meal_reservation_pkey;
ALTER TABLE IF EXISTS ONLY public.st_material DROP CONSTRAINT IF EXISTS st_material_pkey;
ALTER TABLE IF EXISTS ONLY public.st_kitchen_waste DROP CONSTRAINT IF EXISTS st_kitchen_waste_pkey;
ALTER TABLE IF EXISTS ONLY public.st_inventory_record DROP CONSTRAINT IF EXISTS st_inventory_record_pkey;
ALTER TABLE IF EXISTS ONLY public.pay_transaction DROP CONSTRAINT IF EXISTS pay_transaction_pkey;
ALTER TABLE IF EXISTS ONLY public.pay_account DROP CONSTRAINT IF EXISTS pay_account_pkey;
ALTER TABLE IF EXISTS ONLY public.gy_room DROP CONSTRAINT IF EXISTS gy_room_pkey;
ALTER TABLE IF EXISTS ONLY public.gy_repair_order DROP CONSTRAINT IF EXISTS gy_repair_order_pkey;
ALTER TABLE IF EXISTS ONLY public.gy_occupant DROP CONSTRAINT IF EXISTS gy_occupant_pkey;
ALTER TABLE IF EXISTS ONLY public.gy_cleaning_order DROP CONSTRAINT IF EXISTS gy_cleaning_order_pkey;
ALTER TABLE IF EXISTS ONLY public.gc_transfer_order DROP CONSTRAINT IF EXISTS gc_transfer_order_pkey;
ALTER TABLE IF EXISTS ONLY public.gc_transfer_detail DROP CONSTRAINT IF EXISTS gc_transfer_detail_pkey;
ALTER TABLE IF EXISTS ONLY public.gc_return_order DROP CONSTRAINT IF EXISTS gc_return_order_pkey;
ALTER TABLE IF EXISTS ONLY public.gc_logistics_order DROP CONSTRAINT IF EXISTS gc_logistics_order_pkey;
ALTER TABLE IF EXISTS ONLY public.gc_borrow_order DROP CONSTRAINT IF EXISTS gc_borrow_order_pkey;
ALTER TABLE IF EXISTS ONLY public.gc_borrow_detail DROP CONSTRAINT IF EXISTS gc_borrow_detail_pkey;
ALTER TABLE IF EXISTS ONLY public.gc_asset_card DROP CONSTRAINT IF EXISTS gc_asset_card_pkey;
ALTER TABLE IF EXISTS ONLY public.cl_vehicle_archive DROP CONSTRAINT IF EXISTS cl_vehicle_archive_pkey;
ALTER TABLE IF EXISTS ONLY public.cl_track_point DROP CONSTRAINT IF EXISTS cl_track_point_pkey;
ALTER TABLE IF EXISTS ONLY public.cl_repair_order DROP CONSTRAINT IF EXISTS cl_repair_order_pkey;
ALTER TABLE IF EXISTS ONLY public.cl_dispatch_order DROP CONSTRAINT IF EXISTS cl_dispatch_order_pkey;
ALTER TABLE IF EXISTS ONLY public.cl_cost_detail DROP CONSTRAINT IF EXISTS cl_cost_detail_pkey;
ALTER TABLE IF EXISTS ONLY public.cl_apply_order DROP CONSTRAINT IF EXISTS cl_apply_order_pkey;
ALTER TABLE IF EXISTS public.sys_user_role ALTER COLUMN id DROP DEFAULT;
ALTER TABLE IF EXISTS public.sys_user ALTER COLUMN id DROP DEFAULT;
ALTER TABLE IF EXISTS public.sys_unit ALTER COLUMN id DROP DEFAULT;
ALTER TABLE IF EXISTS public.sys_role ALTER COLUMN id DROP DEFAULT;
ALTER TABLE IF EXISTS public.sys_operation_log ALTER COLUMN id DROP DEFAULT;
ALTER TABLE IF EXISTS public.sys_message ALTER COLUMN id DROP DEFAULT;
ALTER TABLE IF EXISTS public.sys_config ALTER COLUMN id DROP DEFAULT;
ALTER TABLE IF EXISTS public.st_purchase_order ALTER COLUMN id DROP DEFAULT;
ALTER TABLE IF EXISTS public.st_purchase_detail ALTER COLUMN id DROP DEFAULT;
ALTER TABLE IF EXISTS public.st_meal_reservation ALTER COLUMN id DROP DEFAULT;
ALTER TABLE IF EXISTS public.st_material ALTER COLUMN id DROP DEFAULT;
ALTER TABLE IF EXISTS public.st_kitchen_waste ALTER COLUMN id DROP DEFAULT;
ALTER TABLE IF EXISTS public.st_inventory_record ALTER COLUMN id DROP DEFAULT;
ALTER TABLE IF EXISTS public.pay_transaction ALTER COLUMN id DROP DEFAULT;
ALTER TABLE IF EXISTS public.pay_account ALTER COLUMN id DROP DEFAULT;
ALTER TABLE IF EXISTS public.gy_room ALTER COLUMN id DROP DEFAULT;
ALTER TABLE IF EXISTS public.gy_repair_order ALTER COLUMN id DROP DEFAULT;
ALTER TABLE IF EXISTS public.gy_occupant ALTER COLUMN id DROP DEFAULT;
ALTER TABLE IF EXISTS public.gy_cleaning_order ALTER COLUMN id DROP DEFAULT;
ALTER TABLE IF EXISTS public.gc_transfer_order ALTER COLUMN id DROP DEFAULT;
ALTER TABLE IF EXISTS public.gc_transfer_detail ALTER COLUMN id DROP DEFAULT;
ALTER TABLE IF EXISTS public.gc_return_order ALTER COLUMN id DROP DEFAULT;
ALTER TABLE IF EXISTS public.gc_logistics_order ALTER COLUMN id DROP DEFAULT;
ALTER TABLE IF EXISTS public.gc_borrow_order ALTER COLUMN id DROP DEFAULT;
ALTER TABLE IF EXISTS public.gc_borrow_detail ALTER COLUMN id DROP DEFAULT;
ALTER TABLE IF EXISTS public.gc_asset_card ALTER COLUMN id DROP DEFAULT;
ALTER TABLE IF EXISTS public.cl_vehicle_archive ALTER COLUMN id DROP DEFAULT;
ALTER TABLE IF EXISTS public.cl_track_point ALTER COLUMN id DROP DEFAULT;
ALTER TABLE IF EXISTS public.cl_repair_order ALTER COLUMN id DROP DEFAULT;
ALTER TABLE IF EXISTS public.cl_dispatch_order ALTER COLUMN id DROP DEFAULT;
ALTER TABLE IF EXISTS public.cl_cost_detail ALTER COLUMN id DROP DEFAULT;
ALTER TABLE IF EXISTS public.cl_apply_order ALTER COLUMN id DROP DEFAULT;
DROP VIEW IF EXISTS public.v_gc_dashboard_stats;
DROP VIEW IF EXISTS public.v_gc_asset_available;
DROP VIEW IF EXISTS public.v_cl_pending_approvals;
DROP SEQUENCE IF EXISTS public.sys_user_role_id_seq;
DROP TABLE IF EXISTS public.sys_user_role;
DROP SEQUENCE IF EXISTS public.sys_user_id_seq;
DROP TABLE IF EXISTS public.sys_user;
DROP SEQUENCE IF EXISTS public.sys_unit_id_seq;
DROP TABLE IF EXISTS public.sys_unit;
DROP SEQUENCE IF EXISTS public.sys_role_id_seq;
DROP TABLE IF EXISTS public.sys_role;
DROP SEQUENCE IF EXISTS public.sys_operation_log_id_seq;
DROP TABLE IF EXISTS public.sys_operation_log;
DROP SEQUENCE IF EXISTS public.sys_message_id_seq;
DROP TABLE IF EXISTS public.sys_message;
DROP SEQUENCE IF EXISTS public.sys_config_id_seq;
DROP TABLE IF EXISTS public.sys_config;
DROP SEQUENCE IF EXISTS public.st_purchase_order_id_seq;
DROP TABLE IF EXISTS public.st_purchase_order;
DROP SEQUENCE IF EXISTS public.st_purchase_detail_id_seq;
DROP TABLE IF EXISTS public.st_purchase_detail;
DROP SEQUENCE IF EXISTS public.st_meal_reservation_id_seq;
DROP TABLE IF EXISTS public.st_meal_reservation;
DROP SEQUENCE IF EXISTS public.st_material_id_seq;
DROP TABLE IF EXISTS public.st_material;
DROP SEQUENCE IF EXISTS public.st_kitchen_waste_id_seq;
DROP TABLE IF EXISTS public.st_kitchen_waste;
DROP SEQUENCE IF EXISTS public.st_inventory_record_id_seq;
DROP TABLE IF EXISTS public.st_inventory_record;
DROP SEQUENCE IF EXISTS public.pay_transaction_id_seq;
DROP TABLE IF EXISTS public.pay_transaction;
DROP SEQUENCE IF EXISTS public.pay_account_id_seq;
DROP TABLE IF EXISTS public.pay_account;
DROP SEQUENCE IF EXISTS public.gy_room_id_seq;
DROP TABLE IF EXISTS public.gy_room;
DROP SEQUENCE IF EXISTS public.gy_repair_order_id_seq;
DROP TABLE IF EXISTS public.gy_repair_order;
DROP SEQUENCE IF EXISTS public.gy_occupant_id_seq;
DROP TABLE IF EXISTS public.gy_occupant;
DROP SEQUENCE IF EXISTS public.gy_cleaning_order_id_seq;
DROP TABLE IF EXISTS public.gy_cleaning_order;
DROP SEQUENCE IF EXISTS public.gc_transfer_order_id_seq;
DROP TABLE IF EXISTS public.gc_transfer_order;
DROP SEQUENCE IF EXISTS public.gc_transfer_detail_id_seq;
DROP TABLE IF EXISTS public.gc_transfer_detail;
DROP SEQUENCE IF EXISTS public.gc_return_order_id_seq;
DROP TABLE IF EXISTS public.gc_return_order;
DROP SEQUENCE IF EXISTS public.gc_logistics_order_id_seq;
DROP TABLE IF EXISTS public.gc_logistics_order;
DROP SEQUENCE IF EXISTS public.gc_borrow_order_id_seq;
DROP TABLE IF EXISTS public.gc_borrow_order;
DROP SEQUENCE IF EXISTS public.gc_borrow_detail_id_seq;
DROP TABLE IF EXISTS public.gc_borrow_detail;
DROP SEQUENCE IF EXISTS public.gc_asset_card_id_seq;
DROP TABLE IF EXISTS public.gc_asset_card;
DROP SEQUENCE IF EXISTS public.cl_vehicle_archive_id_seq;
DROP TABLE IF EXISTS public.cl_vehicle_archive;
DROP SEQUENCE IF EXISTS public.cl_track_point_id_seq;
DROP TABLE IF EXISTS public.cl_track_point;
DROP SEQUENCE IF EXISTS public.cl_repair_order_id_seq;
DROP TABLE IF EXISTS public.cl_repair_order;
DROP SEQUENCE IF EXISTS public.cl_dispatch_order_id_seq;
DROP TABLE IF EXISTS public.cl_dispatch_order;
DROP SEQUENCE IF EXISTS public.cl_cost_detail_id_seq;
DROP TABLE IF EXISTS public.cl_cost_detail;
DROP SEQUENCE IF EXISTS public.cl_apply_order_id_seq;
DROP TABLE IF EXISTS public.cl_apply_order;
DROP FUNCTION IF EXISTS public.update_modified_column();
--
-- Name: update_modified_column(); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.update_modified_column() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
    NEW.update_time = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$;


SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: cl_apply_order; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.cl_apply_order (
    id bigint NOT NULL,
    apply_no character varying(50) NOT NULL,
    applicant_id bigint NOT NULL,
    applicant_name character varying(50),
    applicant_unit_id bigint NOT NULL,
    applicant_phone character varying(20),
    purpose character varying(200) NOT NULL,
    destination character varying(200) NOT NULL,
    start_time timestamp with time zone NOT NULL,
    end_time timestamp with time zone NOT NULL,
    passenger_count integer NOT NULL,
    required_vehicle_type character varying(20),
    planned_route text,
    planned_mileage numeric(10,2),
    apply_status character varying(20) DEFAULT 'PENDING'::character varying,
    auto_approve boolean DEFAULT false,
    reject_reason text,
    dispatch_order_id bigint,
    process_instance_id character varying(50),
    create_by bigint,
    create_time timestamp with time zone DEFAULT CURRENT_TIMESTAMP,
    update_by bigint,
    update_time timestamp with time zone DEFAULT CURRENT_TIMESTAMP,
    is_deleted boolean DEFAULT false,
    audit_user_id bigint,
    audit_user_name character varying(50),
    audit_time timestamp with time zone,
    audit_remark text,
    remark text,
    CONSTRAINT chk_apply_time CHECK ((start_time < end_time)),
    CONSTRAINT chk_passenger_count CHECK ((passenger_count > 0))
);


--
-- Name: TABLE cl_apply_order; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.cl_apply_order IS '用车申请（PRD 4.2.4）';


--
-- Name: cl_apply_order_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.cl_apply_order_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: cl_apply_order_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.cl_apply_order_id_seq OWNED BY public.cl_apply_order.id;


--
-- Name: cl_cost_detail; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.cl_cost_detail (
    id bigint NOT NULL,
    vehicle_id bigint NOT NULL,
    cost_type character varying(30) NOT NULL,
    cost_amount numeric(12,2) NOT NULL,
    cost_time timestamp with time zone NOT NULL,
    cost_desc text,
    biz_order_no character varying(50),
    biz_type character varying(30),
    approval_status character varying(20) DEFAULT 'PENDING'::character varying,
    process_instance_id character varying(50),
    attachment_urls text,
    create_by bigint,
    create_time timestamp with time zone DEFAULT CURRENT_TIMESTAMP,
    update_by bigint,
    update_time timestamp with time zone DEFAULT CURRENT_TIMESTAMP,
    is_deleted boolean DEFAULT false,
    current_mileage numeric(10,2),
    fuel_quantity numeric(10,2),
    approval_user_id bigint,
    approval_time timestamp with time zone,
    approval_remark text,
    CONSTRAINT chk_cost_amount CHECK ((cost_amount >= (0)::numeric))
);


--
-- Name: TABLE cl_cost_detail; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.cl_cost_detail IS '费用明细（PRD 4.2.7）';


--
-- Name: COLUMN cl_cost_detail.cost_type; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.cl_cost_detail.cost_type IS 'FUEL/REPAIR/INSURANCE/TOLL/PARKING/ETC/OTHER（PRD 4.2.7.1）';


--
-- Name: cl_cost_detail_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.cl_cost_detail_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: cl_cost_detail_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.cl_cost_detail_id_seq OWNED BY public.cl_cost_detail.id;


--
-- Name: cl_dispatch_order; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.cl_dispatch_order (
    id bigint NOT NULL,
    dispatch_no character varying(50) NOT NULL,
    apply_id bigint NOT NULL,
    vehicle_id bigint NOT NULL,
    plate_number character varying(20),
    driver_id bigint NOT NULL,
    driver_name character varying(50),
    driver_phone character varying(20),
    scheduled_start timestamp with time zone NOT NULL,
    scheduled_end timestamp with time zone NOT NULL,
    actual_start timestamp with time zone,
    actual_end timestamp with time zone,
    actual_mileage numeric(10,2),
    dispatch_status character varying(20) DEFAULT 'WAITING'::character varying,
    is_emergency boolean DEFAULT false,
    emergency_reason character varying(200),
    remark character varying(500),
    create_by bigint,
    create_time timestamp with time zone DEFAULT CURRENT_TIMESTAMP,
    update_by bigint,
    update_time timestamp with time zone DEFAULT CURRENT_TIMESTAMP,
    is_deleted boolean DEFAULT false
);


--
-- Name: TABLE cl_dispatch_order; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.cl_dispatch_order IS '调度派单（PRD 4.2.5）';


--
-- Name: COLUMN cl_dispatch_order.dispatch_status; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.cl_dispatch_order.dispatch_status IS 'WAITING/ONGOING/RETURNED/CANCELLED';


--
-- Name: cl_dispatch_order_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.cl_dispatch_order_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: cl_dispatch_order_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.cl_dispatch_order_id_seq OWNED BY public.cl_dispatch_order.id;


--
-- Name: cl_repair_order; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.cl_repair_order (
    id bigint NOT NULL,
    repair_no character varying(50) NOT NULL,
    vehicle_id bigint NOT NULL,
    plate_number character varying(20),
    repair_type character varying(20) NOT NULL,
    fault_desc text NOT NULL,
    fault_photos text,
    urgency_level character varying(20),
    repair_shop_id bigint,
    repair_shop_name character varying(100),
    estimated_cost numeric(12,2),
    actual_cost numeric(12,2),
    parts_detail jsonb,
    labor_cost numeric(12,2),
    order_status character varying(20) DEFAULT 'PENDING'::character varying,
    process_instance_id character varying(50),
    repair_start timestamp with time zone,
    repair_end timestamp with time zone,
    repair_photos text,
    accept_user_id bigint,
    accept_time timestamp with time zone,
    accept_result character varying(20),
    accept_remark text,
    repair_mileage numeric(10,2),
    create_by bigint,
    create_time timestamp with time zone DEFAULT CURRENT_TIMESTAMP,
    update_by bigint,
    update_time timestamp with time zone DEFAULT CURRENT_TIMESTAMP,
    is_deleted boolean DEFAULT false,
    audit_user_id bigint,
    audit_user_name character varying(50),
    audit_time timestamp with time zone,
    audit_remark text
);


--
-- Name: TABLE cl_repair_order; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.cl_repair_order IS '维修保养记录（PRD 4.2.8）';


--
-- Name: COLUMN cl_repair_order.repair_type; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.cl_repair_order.repair_type IS 'MAINTENANCE/REPAIR';


--
-- Name: COLUMN cl_repair_order.parts_detail; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.cl_repair_order.parts_detail IS '配件明细（JSONB）';


--
-- Name: cl_repair_order_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.cl_repair_order_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: cl_repair_order_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.cl_repair_order_id_seq OWNED BY public.cl_repair_order.id;


--
-- Name: cl_track_point; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.cl_track_point (
    id bigint NOT NULL,
    dispatch_id bigint NOT NULL,
    vehicle_id bigint NOT NULL,
    lng numeric(10,7) NOT NULL,
    lat numeric(10,7) NOT NULL,
    speed numeric(8,2),
    direction integer,
    track_time timestamp with time zone NOT NULL,
    create_time timestamp with time zone DEFAULT CURRENT_TIMESTAMP
);


--
-- Name: cl_track_point_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.cl_track_point_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: cl_track_point_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.cl_track_point_id_seq OWNED BY public.cl_track_point.id;


--
-- Name: cl_vehicle_archive; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.cl_vehicle_archive (
    id bigint NOT NULL,
    plate_number character varying(20) NOT NULL,
    brand_model character varying(100) NOT NULL,
    vehicle_type character varying(20) NOT NULL,
    vehicle_type_name character varying(20),
    engine_no character varying(50),
    frame_no character varying(50),
    seat_count integer NOT NULL,
    displacement numeric(4,2),
    color character varying(20),
    purchase_date date,
    purchase_price numeric(15,2),
    supplier character varying(100),
    unit_id bigint NOT NULL,
    unit_name character varying(100),
    dept_id bigint,
    establishment_id bigint,
    is_establishment boolean DEFAULT true,
    vehicle_status character varying(20) DEFAULT 'AVAILABLE'::character varying NOT NULL,
    current_mileage numeric(10,2) DEFAULT 0,
    last_maintenance_mileage numeric(10,2),
    next_maintenance_mileage numeric(10,2),
    insurance_company character varying(100),
    insurance_policy_no character varying(50),
    insurance_start date,
    insurance_end date,
    photo_urls text,
    remark character varying(500),
    create_by bigint,
    create_time timestamp with time zone DEFAULT CURRENT_TIMESTAMP,
    update_by bigint,
    update_time timestamp with time zone DEFAULT CURRENT_TIMESTAMP,
    is_deleted boolean DEFAULT false,
    CONSTRAINT chk_mileage CHECK ((current_mileage >= (0)::numeric)),
    CONSTRAINT chk_seat_count CHECK ((seat_count > 0))
);


--
-- Name: TABLE cl_vehicle_archive; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.cl_vehicle_archive IS '车辆档案（PRD 4.2.2）';


--
-- Name: COLUMN cl_vehicle_archive.vehicle_status; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.cl_vehicle_archive.vehicle_status IS 'AVAILABLE/ON_DUTY/REPAIRING/MAINTAINING/WAIT_SCRAP/SCRAPPED（PRD 4.2.2.2）';


--
-- Name: cl_vehicle_archive_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.cl_vehicle_archive_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: cl_vehicle_archive_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.cl_vehicle_archive_id_seq OWNED BY public.cl_vehicle_archive.id;


--
-- Name: gc_asset_card; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.gc_asset_card (
    id bigint NOT NULL,
    asset_code character varying(50) NOT NULL,
    asset_name character varying(200) NOT NULL,
    category_code character varying(20) NOT NULL,
    category_name character varying(50),
    spec_model character varying(100),
    brand character varying(50),
    quantity integer DEFAULT 1,
    original_value numeric(15,2) NOT NULL,
    residual_rate numeric(5,2) DEFAULT 5.00,
    current_value numeric(15,2),
    accumulated_depreciation numeric(15,2) DEFAULT 0.00,
    purchase_date date,
    useful_life integer,
    depreciation_method character varying(20) DEFAULT 'STRAIGHT_LINE'::character varying,
    asset_status character varying(20) NOT NULL,
    location character varying(200),
    warehouse_id bigint,
    owner_unit_id bigint NOT NULL,
    owner_unit_name character varying(100),
    current_use_unit_id bigint,
    qr_code_url character varying(255),
    rfid_tag character varying(50),
    photo_urls text,
    attachment_urls text,
    description text,
    remark character varying(500),
    create_by bigint,
    create_time timestamp with time zone DEFAULT CURRENT_TIMESTAMP,
    update_by bigint,
    update_time timestamp with time zone DEFAULT CURRENT_TIMESTAMP,
    is_deleted boolean DEFAULT false,
    audit_user_id bigint,
    audit_user_name character varying(50),
    audit_time timestamp with time zone,
    audit_remark text,
    in_stock_time timestamp with time zone,
    CONSTRAINT chk_asset_quantity CHECK ((quantity > 0)),
    CONSTRAINT chk_asset_value CHECK ((original_value >= (0)::numeric)),
    CONSTRAINT chk_residual_rate CHECK (((residual_rate >= (0)::numeric) AND (residual_rate <= (100)::numeric))),
    CONSTRAINT chk_useful_life CHECK (((useful_life IS NULL) OR (useful_life > 0)))
);


--
-- Name: TABLE gc_asset_card; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.gc_asset_card IS '资产卡片（PRD 4.1.3.1一卡一物一码）';


--
-- Name: COLUMN gc_asset_card.asset_code; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.gc_asset_card.asset_code IS '资产编号：GLZ+分类代码+年份+序号（PRD 4.1.2.2业务规则1）';


--
-- Name: COLUMN gc_asset_card.asset_status; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.gc_asset_card.asset_status IS '资产状态：IN_STOCK/已借用/已调剂/已处置/REPAIRING（PRD 4.1.3.2）';


--
-- Name: gc_asset_card_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.gc_asset_card_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: gc_asset_card_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.gc_asset_card_id_seq OWNED BY public.gc_asset_card.id;


--
-- Name: gc_borrow_detail; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.gc_borrow_detail (
    id bigint NOT NULL,
    borrow_order_id bigint NOT NULL,
    asset_id bigint NOT NULL,
    asset_code character varying(50),
    asset_name character varying(200),
    borrow_quantity integer DEFAULT 1 NOT NULL,
    actual_quantity integer,
    return_quantity integer,
    detail_status character varying(20) DEFAULT 'PENDING'::character varying,
    return_time timestamp with time zone,
    accept_status character varying(20),
    accept_remark text,
    damage_description text,
    create_time timestamp with time zone DEFAULT CURRENT_TIMESTAMP,
    update_time timestamp with time zone DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_borrow_qty CHECK ((borrow_quantity > 0))
);


--
-- Name: TABLE gc_borrow_detail; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.gc_borrow_detail IS '借用明细（PRD 4.1.5一单多资产）';


--
-- Name: COLUMN gc_borrow_detail.detail_status; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.gc_borrow_detail.detail_status IS '明细状态：PENDING/OUT/RETURNED/DAMAGED';


--
-- Name: gc_borrow_detail_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.gc_borrow_detail_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: gc_borrow_detail_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.gc_borrow_detail_id_seq OWNED BY public.gc_borrow_detail.id;


--
-- Name: gc_borrow_order; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.gc_borrow_order (
    id bigint NOT NULL,
    order_no character varying(50) NOT NULL,
    applicant_id bigint NOT NULL,
    applicant_name character varying(50),
    applicant_unit_id bigint NOT NULL,
    applicant_unit_name character varying(100),
    applicant_phone character varying(20),
    borrow_start timestamp with time zone NOT NULL,
    borrow_end timestamp with time zone NOT NULL,
    borrow_reason text NOT NULL,
    borrow_purpose character varying(50),
    order_status character varying(20) DEFAULT 'DRAFT'::character varying NOT NULL,
    approval_status character varying(20),
    current_approver_id bigint,
    current_approver_node character varying(100),
    warehouse_out_time timestamp with time zone,
    warehouse_in_time timestamp with time zone,
    logistics_order_no character varying(50),
    logistics_status character varying(20),
    extension_count integer DEFAULT 0,
    max_extension integer DEFAULT 2,
    process_instance_id character varying(50),
    create_by bigint,
    create_time timestamp with time zone DEFAULT CURRENT_TIMESTAMP,
    update_by bigint,
    update_time timestamp with time zone DEFAULT CURRENT_TIMESTAMP,
    is_deleted boolean DEFAULT false,
    audit_user_id bigint,
    audit_user_name character varying(50),
    audit_time timestamp with time zone,
    audit_remark text,
    remark text,
    CONSTRAINT chk_borrow_time CHECK ((borrow_start < borrow_end)),
    CONSTRAINT chk_extension_count CHECK (((extension_count >= 0) AND (extension_count <= max_extension)))
);


--
-- Name: TABLE gc_borrow_order; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.gc_borrow_order IS '借用单主表（PRD 4.1.5）';


--
-- Name: COLUMN gc_borrow_order.order_no; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.gc_borrow_order.order_no IS '借用单编号：JY+年份+月份+序号（PRD 4.1.5.4）';


--
-- Name: COLUMN gc_borrow_order.order_status; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.gc_borrow_order.order_status IS '状态：DRAFT/PENDING/APPROVED/REJECTED/WAIT_OUT/BORROWING/RETURNING/RETURNED/DONE（PRD 4.1.5.7）';


--
-- Name: gc_borrow_order_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.gc_borrow_order_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: gc_borrow_order_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.gc_borrow_order_id_seq OWNED BY public.gc_borrow_order.id;


--
-- Name: gc_logistics_order; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.gc_logistics_order (
    id bigint NOT NULL,
    logistics_no character varying(50) NOT NULL,
    business_type character varying(20) NOT NULL,
    business_order_no character varying(50) NOT NULL,
    delivery_method character varying(20) NOT NULL,
    sender_name character varying(50),
    sender_phone character varying(20),
    sender_address character varying(200),
    receiver_name character varying(50) NOT NULL,
    receiver_phone character varying(20),
    receiver_address character varying(200) NOT NULL,
    cargo_desc text,
    cargo_weight numeric(10,2),
    cargo_volume numeric(10,2),
    logistics_status character varying(20) DEFAULT 'PENDING'::character varying,
    pickup_time timestamp with time zone,
    delivery_time timestamp with time zone,
    sign_time timestamp with time zone,
    sign_person character varying(50),
    sign_photo_url character varying(255),
    third_party_tracking_no character varying(50),
    third_party_platform character varying(50),
    freight_amount numeric(12,2),
    create_by bigint,
    create_time timestamp with time zone DEFAULT CURRENT_TIMESTAMP,
    update_by bigint,
    update_time timestamp with time zone DEFAULT CURRENT_TIMESTAMP,
    is_deleted boolean DEFAULT false
);


--
-- Name: TABLE gc_logistics_order; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.gc_logistics_order IS '物流单（PRD 4.1.11）';


--
-- Name: gc_logistics_order_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.gc_logistics_order_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: gc_logistics_order_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.gc_logistics_order_id_seq OWNED BY public.gc_logistics_order.id;


--
-- Name: gc_return_order; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.gc_return_order (
    id bigint NOT NULL,
    return_no character varying(50) NOT NULL,
    borrow_order_id bigint NOT NULL,
    return_applicant_id bigint NOT NULL,
    return_applicant_unit_id bigint NOT NULL,
    plan_return_time timestamp with time zone,
    actual_return_time timestamp with time zone,
    accept_user_ids text,
    accept_time timestamp with time zone,
    accept_result character varying(20),
    accept_remark text,
    accept_photos text,
    damage_info text,
    damage_responsibility character varying(50),
    repair_cost numeric(15,2),
    compensation_amount numeric(15,2),
    return_status character varying(20) DEFAULT 'PENDING'::character varying NOT NULL,
    create_by bigint,
    create_time timestamp with time zone DEFAULT CURRENT_TIMESTAMP,
    update_by bigint,
    update_time timestamp with time zone DEFAULT CURRENT_TIMESTAMP,
    is_deleted boolean DEFAULT false
);


--
-- Name: TABLE gc_return_order; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.gc_return_order IS '归还验收单（PRD 4.1.6）';


--
-- Name: gc_return_order_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.gc_return_order_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: gc_return_order_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.gc_return_order_id_seq OWNED BY public.gc_return_order.id;


--
-- Name: gc_transfer_detail; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.gc_transfer_detail (
    id bigint NOT NULL,
    transfer_order_id bigint NOT NULL,
    asset_id bigint NOT NULL,
    asset_code character varying(50),
    asset_name character varying(200),
    create_time timestamp with time zone DEFAULT CURRENT_TIMESTAMP
);


--
-- Name: gc_transfer_detail_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.gc_transfer_detail_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: gc_transfer_detail_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.gc_transfer_detail_id_seq OWNED BY public.gc_transfer_detail.id;


--
-- Name: gc_transfer_order; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.gc_transfer_order (
    id bigint NOT NULL,
    order_no character varying(50) NOT NULL,
    transfer_type character varying(20) NOT NULL,
    asset_count integer NOT NULL,
    total_value numeric(15,2),
    applicant_unit_id bigint NOT NULL,
    receive_unit_id bigint,
    apply_reason text NOT NULL,
    dispose_method character varying(30),
    appraisal_org character varying(100),
    appraisal_value numeric(15,2),
    appraisal_report_url character varying(255),
    appraisal_time timestamp with time zone,
    order_status character varying(20) DEFAULT 'PENDING'::character varying,
    exec_time timestamp with time zone,
    exec_result text,
    income_amount numeric(15,2) DEFAULT 0.00,
    expense_amount numeric(15,2) DEFAULT 0.00,
    process_instance_id character varying(50),
    create_by bigint,
    create_time timestamp with time zone DEFAULT CURRENT_TIMESTAMP,
    update_by bigint,
    update_time timestamp with time zone DEFAULT CURRENT_TIMESTAMP,
    is_deleted boolean DEFAULT false,
    audit_user_id bigint,
    audit_user_name character varying(50),
    audit_time timestamp with time zone,
    audit_remark text,
    remark text,
    net_profit numeric(15,2) GENERATED ALWAYS AS ((income_amount - expense_amount)) STORED,
    CONSTRAINT chk_transfer_type CHECK (((transfer_type)::text = ANY ((ARRAY['TRANSFER'::character varying, 'DISPOSE'::character varying])::text[])))
);


--
-- Name: TABLE gc_transfer_order; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.gc_transfer_order IS '调剂/处置申请表（PRD 4.1.7/4.1.10）';


--
-- Name: COLUMN gc_transfer_order.transfer_type; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.gc_transfer_order.transfer_type IS 'TRANSFER调剂 / DISPOSE处置';


--
-- Name: COLUMN gc_transfer_order.dispose_method; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.gc_transfer_order.dispose_method IS 'PUBLIC_AUCTION/SCRAP/DONATE（PRD 4.1.10.1）';


--
-- Name: gc_transfer_order_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.gc_transfer_order_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: gc_transfer_order_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.gc_transfer_order_id_seq OWNED BY public.gc_transfer_order.id;


--
-- Name: gy_cleaning_order; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.gy_cleaning_order (
    id bigint NOT NULL,
    cleaning_no character varying(50) NOT NULL,
    room_id bigint NOT NULL,
    room_no character varying(20),
    applicant_id bigint NOT NULL,
    applicant_name character varying(50),
    cleaning_time timestamp with time zone NOT NULL,
    cleaning_scope text,
    cleaning_requirement text,
    order_status character varying(20) DEFAULT 'PENDING'::character varying,
    assignee_id bigint,
    assignee_name character varying(50),
    assignee_company character varying(100),
    assign_time timestamp with time zone,
    execute_time timestamp with time zone,
    execute_photos text,
    accept_user_id bigint,
    accept_time timestamp with time zone,
    accept_result character varying(20),
    accept_remark text,
    accept_score integer,
    settle_amount numeric(10,2),
    settle_status character varying(20),
    create_by bigint,
    create_time timestamp with time zone DEFAULT CURRENT_TIMESTAMP,
    update_by bigint,
    update_time timestamp with time zone DEFAULT CURRENT_TIMESTAMP,
    is_deleted boolean DEFAULT false,
    cleaning_type character varying(20) DEFAULT 'REGULAR'::character varying,
    audit_user_id bigint,
    audit_user_name character varying(50),
    audit_time timestamp with time zone,
    audit_remark text,
    CONSTRAINT chk_accept_score CHECK (((accept_score IS NULL) OR ((accept_score >= 1) AND (accept_score <= 5))))
);


--
-- Name: TABLE gy_cleaning_order; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.gy_cleaning_order IS '保洁服务（PRD 4.3.6）';


--
-- Name: gy_cleaning_order_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.gy_cleaning_order_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: gy_cleaning_order_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.gy_cleaning_order_id_seq OWNED BY public.gy_cleaning_order.id;


--
-- Name: gy_occupant; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.gy_occupant (
    id bigint NOT NULL,
    occupant_name character varying(50) NOT NULL,
    occupant_type character varying(20),
    id_card character varying(18),
    phone character varying(20),
    unit_id bigint,
    unit_name character varying(100),
    "position" character varying(50),
    attendant_info jsonb,
    room_id bigint,
    room_no character varying(20),
    checkin_time timestamp with time zone,
    expected_leave_time timestamp with time zone,
    actual_leave_time timestamp with time zone,
    rent_amount numeric(10,2) DEFAULT 0,
    rent_paid_status character varying(20) DEFAULT 'PAID'::character varying,
    assign_method character varying(20) NOT NULL,
    approval_file_url character varying(255),
    occupant_status character varying(20) DEFAULT 'ACTIVE'::character varying NOT NULL,
    apply_id bigint,
    process_instance_id character varying(50),
    create_by bigint,
    create_time timestamp with time zone DEFAULT CURRENT_TIMESTAMP,
    update_by bigint,
    update_time timestamp with time zone DEFAULT CURRENT_TIMESTAMP,
    is_deleted boolean DEFAULT false,
    apply_reason text,
    audit_user_id bigint,
    audit_user_name character varying(50),
    audit_time timestamp with time zone,
    audit_remark text,
    remark text,
    checkout_time timestamp with time zone,
    room_condition text,
    facility_check_result text,
    settlement_amount numeric(12,2),
    settlement_detail text,
    checkout_photos text,
    accept_user_id bigint,
    accept_user_name character varying(50),
    accept_time timestamp with time zone,
    CONSTRAINT chk_rent_amount CHECK ((rent_amount >= (0)::numeric))
);


--
-- Name: TABLE gy_occupant; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.gy_occupant IS '入住记录（PRD 4.3.2.2）';


--
-- Name: COLUMN gy_occupant.occupant_type; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.gy_occupant.occupant_type IS 'EXPERT/TALENT/STAFF（PRD 4.3.1）';


--
-- Name: COLUMN gy_occupant.assign_method; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.gy_occupant.assign_method IS 'DIRECT直接分配（专家公寓）/APPROVAL审批分配（人才公寓），见PRD 4.3.1对比表';


--
-- Name: gy_occupant_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.gy_occupant_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: gy_occupant_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.gy_occupant_id_seq OWNED BY public.gy_occupant.id;


--
-- Name: gy_repair_order; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.gy_repair_order (
    id bigint NOT NULL,
    repair_no character varying(50) NOT NULL,
    room_id bigint NOT NULL,
    room_no character varying(20),
    applicant_id bigint NOT NULL,
    applicant_name character varying(50),
    applicant_phone character varying(20),
    fault_location character varying(100),
    fault_desc text NOT NULL,
    fault_photos text,
    urgency_level character varying(20),
    cost_type character varying(20) NOT NULL,
    estimated_cost numeric(12,2),
    actual_cost numeric(12,2),
    quote_detail jsonb,
    order_status character varying(20) DEFAULT 'PENDING'::character varying,
    process_instance_id character varying(50),
    repair_shop_id bigint,
    repair_shop_name character varying(100),
    repair_start timestamp with time zone,
    repair_end timestamp with time zone,
    repair_photos text,
    accept_user_id bigint,
    accept_time timestamp with time zone,
    accept_result character varying(20),
    accept_remark text,
    accept_photos text,
    create_by bigint,
    create_time timestamp with time zone DEFAULT CURRENT_TIMESTAMP,
    update_by bigint,
    update_time timestamp with time zone DEFAULT CURRENT_TIMESTAMP,
    is_deleted boolean DEFAULT false,
    repair_type character varying(20),
    parts_detail jsonb,
    labor_cost numeric(12,2),
    audit_user_id bigint,
    audit_user_name character varying(50),
    audit_time timestamp with time zone,
    audit_remark text,
    CONSTRAINT chk_cost_type CHECK (((cost_type)::text = ANY ((ARRAY['UNIT'::character varying, 'PERSONAL'::character varying])::text[])))
);


--
-- Name: TABLE gy_repair_order; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.gy_repair_order IS '维修申请（PRD 4.3.5）';


--
-- Name: COLUMN gy_repair_order.cost_type; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.gy_repair_order.cost_type IS 'UNIT单位承担/PERSONAL个人自费（PRD 4.3.5.1）';


--
-- Name: gy_repair_order_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.gy_repair_order_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: gy_repair_order_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.gy_repair_order_id_seq OWNED BY public.gy_repair_order.id;


--
-- Name: gy_room; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.gy_room (
    id bigint NOT NULL,
    building character varying(20) NOT NULL,
    floor integer NOT NULL,
    room_no character varying(20) NOT NULL,
    room_type character varying(20) NOT NULL,
    layout character varying(20),
    area numeric(10,2),
    orientation character varying(10),
    facilities jsonb,
    room_status character varying(20) DEFAULT 'IDLE'::character varying NOT NULL,
    current_occupant_id bigint,
    current_occupant_name character varying(50),
    current_unit_id bigint,
    asset_ids text,
    remark character varying(500),
    create_by bigint,
    create_time timestamp with time zone DEFAULT CURRENT_TIMESTAMP,
    update_by bigint,
    update_time timestamp with time zone DEFAULT CURRENT_TIMESTAMP,
    is_deleted boolean DEFAULT false,
    CONSTRAINT chk_floor CHECK ((floor >= 0))
);


--
-- Name: TABLE gy_room; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.gy_room IS '公寓房间（PRD 4.3.2.1）';


--
-- Name: COLUMN gy_room.room_type; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.gy_room.room_type IS 'EXPERT_APARTMENT/TALENT_APARTMENT（PRD 4.3.1）';


--
-- Name: COLUMN gy_room.room_status; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.gy_room.room_status IS 'IDLE/OCCUPIED/REPAIRING/RESERVED（PRD公寓房间状态变迁表）';


--
-- Name: gy_room_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.gy_room_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: gy_room_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.gy_room_id_seq OWNED BY public.gy_room.id;


--
-- Name: pay_account; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.pay_account (
    id bigint NOT NULL,
    user_id bigint NOT NULL,
    account_type character varying(20) NOT NULL,
    account_no character varying(50) NOT NULL,
    balance numeric(12,2) DEFAULT 0.00,
    frozen_amount numeric(12,2) DEFAULT 0.00,
    card_no character varying(50),
    card_status character varying(20) DEFAULT 'ACTIVE'::character varying,
    account_status character varying(20) DEFAULT 'ACTIVE'::character varying,
    last_transaction_time timestamp with time zone,
    create_by bigint,
    create_time timestamp with time zone DEFAULT CURRENT_TIMESTAMP,
    update_by bigint,
    update_time timestamp with time zone DEFAULT CURRENT_TIMESTAMP,
    is_deleted boolean DEFAULT false,
    CONSTRAINT chk_account_type CHECK (((account_type)::text = ANY ((ARRAY['MEAL_CARD'::character varying, 'VIRTUAL'::character varying])::text[]))),
    CONSTRAINT chk_balance CHECK (((balance >= (0)::numeric) AND (frozen_amount >= (0)::numeric)))
);


--
-- Name: TABLE pay_account; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.pay_account IS '用户账户（PRD 4.4.3餐卡管理）';


--
-- Name: COLUMN pay_account.account_type; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.pay_account.account_type IS 'MEAL_CARD/VIRTUAL';


--
-- Name: pay_account_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.pay_account_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: pay_account_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.pay_account_id_seq OWNED BY public.pay_account.id;


--
-- Name: pay_transaction; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.pay_transaction (
    id bigint NOT NULL,
    transaction_no character varying(50) NOT NULL,
    external_transaction_no character varying(50),
    user_id bigint NOT NULL,
    account_type character varying(20) NOT NULL,
    account_no character varying(50),
    card_no character varying(50),
    transaction_type character varying(20) NOT NULL,
    amount numeric(12,2) NOT NULL,
    balance_before numeric(12,2),
    balance_after numeric(12,2),
    pay_method character varying(20) NOT NULL,
    pay_status character varying(20) DEFAULT 'PENDING'::character varying,
    biz_module character varying(30),
    biz_order_no character varying(50),
    scene character varying(50),
    remark character varying(255),
    pay_time timestamp with time zone,
    create_time timestamp with time zone DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_pay_amount CHECK ((amount <> (0)::numeric))
);


--
-- Name: TABLE pay_transaction; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.pay_transaction IS '交易流水（PRD 4.4.3.4）';


--
-- Name: COLUMN pay_transaction.transaction_type; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.pay_transaction.transaction_type IS 'RECHARGE/CONSUME/REFUND/DEDUCT';


--
-- Name: COLUMN pay_transaction.pay_method; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.pay_transaction.pay_method IS 'FACE/CARD/WECHAT/ALIPAY（PRD 4.4.3.1刷脸支付）';


--
-- Name: pay_transaction_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.pay_transaction_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: pay_transaction_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.pay_transaction_id_seq OWNED BY public.pay_transaction.id;


--
-- Name: st_inventory_record; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.st_inventory_record (
    id bigint NOT NULL,
    material_id bigint NOT NULL,
    material_code character varying(50),
    material_name character varying(100),
    record_type character varying(20) NOT NULL,
    quantity numeric(10,2) NOT NULL,
    unit_price numeric(10,2),
    total_amount numeric(15,2),
    business_order_no character varying(50),
    business_type character varying(30),
    stock_before numeric(10,2),
    stock_after numeric(10,2),
    operator_id bigint,
    operator_name character varying(50),
    remark character varying(500),
    create_time timestamp with time zone DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_inventory_qty CHECK ((quantity <> (0)::numeric))
);


--
-- Name: TABLE st_inventory_record; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.st_inventory_record IS '进销存流水（PRD 4.4.5）';


--
-- Name: COLUMN st_inventory_record.record_type; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.st_inventory_record.record_type IS 'IN入库/OUT出库/ADJUST调整/LOSS报损（PRD 4.4.5.2/4.4.5.3）';


--
-- Name: COLUMN st_inventory_record.quantity; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.st_inventory_record.quantity IS '正数入库/增加，负数出库/减少';


--
-- Name: st_inventory_record_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.st_inventory_record_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: st_inventory_record_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.st_inventory_record_id_seq OWNED BY public.st_inventory_record.id;


--
-- Name: st_kitchen_waste; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.st_kitchen_waste (
    id bigint NOT NULL,
    record_date date NOT NULL,
    meal_type character varying(20),
    waste_weight numeric(10,2) NOT NULL,
    waste_type character varying(20),
    disposal_method character varying(30),
    disposal_person character varying(50),
    remark character varying(500),
    create_by bigint,
    create_time timestamp with time zone DEFAULT CURRENT_TIMESTAMP,
    update_by bigint,
    update_time timestamp with time zone DEFAULT CURRENT_TIMESTAMP,
    is_deleted boolean DEFAULT false
);


--
-- Name: st_kitchen_waste_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.st_kitchen_waste_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: st_kitchen_waste_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.st_kitchen_waste_id_seq OWNED BY public.st_kitchen_waste.id;


--
-- Name: st_material; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.st_material (
    id bigint NOT NULL,
    material_code character varying(50) NOT NULL,
    material_name character varying(100) NOT NULL,
    category character varying(50) NOT NULL,
    spec character varying(50),
    unit character varying(10) NOT NULL,
    shelf_life integer,
    safety_stock numeric(10,2) DEFAULT 0,
    max_stock numeric(10,2) DEFAULT 1000,
    current_price numeric(10,2),
    last_price numeric(10,2),
    current_stock numeric(10,2) DEFAULT 0,
    occupied_stock numeric(10,2) DEFAULT 0,
    remark character varying(500),
    create_by bigint,
    create_time timestamp with time zone DEFAULT CURRENT_TIMESTAMP,
    update_by bigint,
    update_time timestamp with time zone DEFAULT CURRENT_TIMESTAMP,
    is_deleted boolean DEFAULT false,
    CONSTRAINT chk_stock CHECK (((current_stock >= (0)::numeric) AND (occupied_stock >= (0)::numeric)))
);


--
-- Name: TABLE st_material; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.st_material IS '物资档案（PRD 4.4.5.1）';


--
-- Name: COLUMN st_material.category; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.st_material.category IS 'FRESH_INGREDIENTS/CONDIMENT/DAILY_GOODS（PRD 4.4.5.1）';


--
-- Name: st_material_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.st_material_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: st_material_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.st_material_id_seq OWNED BY public.st_material.id;


--
-- Name: st_meal_reservation; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.st_meal_reservation (
    id bigint NOT NULL,
    user_id bigint NOT NULL,
    user_name character varying(50),
    unit_id bigint,
    unit_name character varying(100),
    meal_date date NOT NULL,
    meal_type character varying(20) NOT NULL,
    meal_count integer DEFAULT 1,
    reservation_time timestamp with time zone DEFAULT CURRENT_TIMESTAMP,
    cancel_time timestamp with time zone,
    is_cancelled boolean DEFAULT false,
    create_time timestamp with time zone DEFAULT CURRENT_TIMESTAMP,
    update_time timestamp with time zone DEFAULT CURRENT_TIMESTAMP,
    remark character varying(500)
);


--
-- Name: TABLE st_meal_reservation; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.st_meal_reservation IS '预约订餐（PRD 4.4.2）';


--
-- Name: COLUMN st_meal_reservation.meal_type; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.st_meal_reservation.meal_type IS 'BREAKFAST/LUNCH/DINNER（PRD 4.4.2.1）';


--
-- Name: st_meal_reservation_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.st_meal_reservation_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: st_meal_reservation_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.st_meal_reservation_id_seq OWNED BY public.st_meal_reservation.id;


--
-- Name: st_purchase_detail; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.st_purchase_detail (
    id bigint NOT NULL,
    purchase_order_id bigint NOT NULL,
    material_id bigint NOT NULL,
    material_code character varying(50),
    material_name character varying(100),
    quantity numeric(10,2) NOT NULL,
    unit_price numeric(10,2) NOT NULL,
    subtotal numeric(15,2),
    received_quantity numeric(10,2),
    receive_time timestamp with time zone,
    create_time timestamp with time zone DEFAULT CURRENT_TIMESTAMP,
    update_time timestamp with time zone DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_quantity CHECK (((quantity > (0)::numeric) AND (unit_price >= (0)::numeric)))
);


--
-- Name: TABLE st_purchase_detail; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.st_purchase_detail IS '采购明细（PRD 4.4.4）';


--
-- Name: st_purchase_detail_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.st_purchase_detail_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: st_purchase_detail_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.st_purchase_detail_id_seq OWNED BY public.st_purchase_detail.id;


--
-- Name: st_purchase_order; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.st_purchase_order (
    id bigint NOT NULL,
    order_no character varying(50) NOT NULL,
    purchase_reason text,
    total_amount numeric(15,2),
    material_count integer,
    effective_start timestamp with time zone DEFAULT CURRENT_TIMESTAMP,
    effective_end timestamp with time zone,
    is_expired boolean DEFAULT false,
    supplier_id bigint,
    supplier_name character varying(100),
    order_status character varying(20) DEFAULT 'DRAFT'::character varying,
    process_instance_id character varying(50),
    accept_users text,
    accept_time timestamp with time zone,
    accept_status character varying(20),
    accept_remark text,
    create_by bigint,
    create_time timestamp with time zone DEFAULT CURRENT_TIMESTAMP,
    update_by bigint,
    update_time timestamp with time zone DEFAULT CURRENT_TIMESTAMP,
    is_deleted boolean DEFAULT false,
    audit_user_id bigint,
    audit_user_name character varying(50),
    audit_time timestamp with time zone,
    audit_remark text
);


--
-- Name: TABLE st_purchase_order; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.st_purchase_order IS '采购申请（PRD 4.4.4）';


--
-- Name: COLUMN st_purchase_order.effective_end; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.st_purchase_order.effective_end IS '有效期2天（PRD 4.4.4.1）';


--
-- Name: COLUMN st_purchase_order.accept_users; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.st_purchase_order.accept_users IS '3人电子签名验收（PRD 4.4.4.5）';


--
-- Name: st_purchase_order_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.st_purchase_order_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: st_purchase_order_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.st_purchase_order_id_seq OWNED BY public.st_purchase_order.id;


--
-- Name: sys_config; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.sys_config (
    id bigint NOT NULL,
    config_key character varying(100) NOT NULL,
    config_value text,
    config_type character varying(20) DEFAULT 'STRING'::character varying,
    config_group character varying(50),
    config_desc character varying(200),
    is_public boolean DEFAULT true,
    sort_order integer DEFAULT 0,
    create_by bigint,
    create_time timestamp with time zone DEFAULT CURRENT_TIMESTAMP,
    update_by bigint,
    update_time timestamp with time zone DEFAULT CURRENT_TIMESTAMP,
    is_deleted boolean DEFAULT false
);


--
-- Name: TABLE sys_config; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.sys_config IS '系统配置表（PRD 4.5.2.2可配置化权限）';


--
-- Name: sys_config_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.sys_config_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: sys_config_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.sys_config_id_seq OWNED BY public.sys_config.id;


--
-- Name: sys_message; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.sys_message (
    id bigint NOT NULL,
    title character varying(200) NOT NULL,
    content text,
    message_type character varying(20) NOT NULL,
    sender_id bigint,
    receiver_id bigint NOT NULL,
    is_read boolean DEFAULT false,
    read_time timestamp with time zone,
    biz_module character varying(30),
    biz_order_no character varying(50),
    create_by bigint,
    create_time timestamp with time zone DEFAULT CURRENT_TIMESTAMP,
    update_by bigint,
    update_time timestamp with time zone DEFAULT CURRENT_TIMESTAMP,
    is_deleted boolean DEFAULT false
);


--
-- Name: sys_message_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.sys_message_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: sys_message_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.sys_message_id_seq OWNED BY public.sys_message.id;


--
-- Name: sys_operation_log; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.sys_operation_log (
    id bigint NOT NULL,
    user_id bigint,
    username character varying(50),
    real_name character varying(50),
    module character varying(50) NOT NULL,
    operation_type character varying(30) NOT NULL,
    operation_desc character varying(500),
    request_url character varying(255),
    request_method character varying(10),
    request_params text,
    response_code integer,
    response_msg character varying(500),
    cost_time integer,
    client_ip character varying(50),
    user_agent character varying(255),
    exception_msg text,
    create_time timestamp with time zone DEFAULT CURRENT_TIMESTAMP
);


--
-- Name: TABLE sys_operation_log; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.sys_operation_log IS '系统操作日志表（PRD 4.5.6）';


--
-- Name: COLUMN sys_operation_log.module; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.sys_operation_log.module IS '模块：GC/CL/GY/ST/SYS/PAY';


--
-- Name: COLUMN sys_operation_log.operation_type; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.sys_operation_log.operation_type IS '操作类型：LOGIN/QUERY/ADD/UPDATE/DELETE/APPROVE/EXPORT';


--
-- Name: sys_operation_log_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.sys_operation_log_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: sys_operation_log_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.sys_operation_log_id_seq OWNED BY public.sys_operation_log.id;


--
-- Name: sys_role; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.sys_role (
    id bigint NOT NULL,
    role_code character varying(50) NOT NULL,
    role_name character varying(50) NOT NULL,
    role_desc character varying(200),
    role_type character varying(20),
    is_system boolean DEFAULT false,
    sort_order integer DEFAULT 0,
    create_by bigint,
    create_time timestamp with time zone DEFAULT CURRENT_TIMESTAMP,
    update_by bigint,
    update_time timestamp with time zone DEFAULT CURRENT_TIMESTAMP,
    is_deleted boolean DEFAULT false
);


--
-- Name: TABLE sys_role; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.sys_role IS '系统角色表（PRD 4.5.2.1角色管理）';


--
-- Name: sys_role_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.sys_role_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: sys_role_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.sys_role_id_seq OWNED BY public.sys_role.id;


--
-- Name: sys_unit; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.sys_unit (
    id bigint NOT NULL,
    unit_code character varying(50) NOT NULL,
    unit_name character varying(100) NOT NULL,
    unit_type character varying(20),
    parent_id bigint,
    contact_person character varying(50),
    contact_phone character varying(20),
    sort_order integer DEFAULT 0,
    remark character varying(500),
    create_by bigint,
    create_time timestamp with time zone DEFAULT CURRENT_TIMESTAMP,
    update_by bigint,
    update_time timestamp with time zone DEFAULT CURRENT_TIMESTAMP,
    is_deleted boolean DEFAULT false
);


--
-- Name: TABLE sys_unit; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.sys_unit IS '单位组织表（对应PRD 3.1角色矩阵中的单位）';


--
-- Name: COLUMN sys_unit.unit_type; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.sys_unit.unit_type IS '单位类型：DEPT/COMPANY/BRANCH';


--
-- Name: sys_unit_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.sys_unit_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: sys_unit_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.sys_unit_id_seq OWNED BY public.sys_unit.id;


--
-- Name: sys_user; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.sys_user (
    id bigint NOT NULL,
    username character varying(50) NOT NULL,
    password character varying(255) NOT NULL,
    real_name character varying(50),
    phone character varying(20),
    email character varying(100),
    face_id character varying(100),
    face_image_url character varying(255),
    unit_id bigint,
    unit_name character varying(100),
    dept_id bigint,
    "position" character varying(50),
    user_type character varying(20) NOT NULL,
    user_status character varying(20) DEFAULT 'ACTIVE'::character varying,
    ext_json jsonb,
    create_by bigint,
    create_time timestamp with time zone DEFAULT CURRENT_TIMESTAMP,
    update_by bigint,
    update_time timestamp with time zone DEFAULT CURRENT_TIMESTAMP,
    is_deleted boolean DEFAULT false
);


--
-- Name: TABLE sys_user; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.sys_user IS '系统用户表（PRD 4.5.1统一用户认证）';


--
-- Name: COLUMN sys_user.user_type; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.sys_user.user_type IS '用户类型：ADMIN/DIRECTOR/DEPT_HEAD/BIZ_ADMIN/USER/DRIVER/SERVICE_PROVIDER/WAREHOUSE（PRD 3.1）';


--
-- Name: sys_user_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.sys_user_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: sys_user_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.sys_user_id_seq OWNED BY public.sys_user.id;


--
-- Name: sys_user_role; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.sys_user_role (
    id bigint NOT NULL,
    user_id bigint NOT NULL,
    role_id bigint NOT NULL,
    create_by bigint,
    create_time timestamp with time zone DEFAULT CURRENT_TIMESTAMP
);


--
-- Name: TABLE sys_user_role; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.sys_user_role IS '用户角色关联表（PRD 4.5.2）';


--
-- Name: sys_user_role_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.sys_user_role_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: sys_user_role_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.sys_user_role_id_seq OWNED BY public.sys_user_role.id;


--
-- Name: v_cl_pending_approvals; Type: VIEW; Schema: public; Owner: -
--

CREATE VIEW public.v_cl_pending_approvals AS
 SELECT a.id,
    a.apply_no,
    a.applicant_id,
    a.applicant_name,
    a.applicant_unit_id,
    a.applicant_phone,
    a.purpose,
    a.destination,
    a.start_time,
    a.end_time,
    a.passenger_count,
    a.required_vehicle_type,
    a.planned_route,
    a.planned_mileage,
    a.apply_status,
    a.auto_approve,
    a.reject_reason,
    a.dispatch_order_id,
    a.process_instance_id,
    a.create_by,
    a.create_time,
    a.update_by,
    a.update_time,
    a.is_deleted,
    v.plate_number,
    v.vehicle_type,
    u.real_name AS applicant_real_name
   FROM ((public.cl_apply_order a
     LEFT JOIN public.cl_vehicle_archive v ON ((a.dispatch_order_id = v.id)))
     LEFT JOIN public.sys_user u ON ((a.applicant_id = u.id)))
  WHERE (((a.apply_status)::text = 'PENDING'::text) AND (a.is_deleted = false))
  ORDER BY a.create_time;


--
-- Name: v_gc_asset_available; Type: VIEW; Schema: public; Owner: -
--

CREATE VIEW public.v_gc_asset_available AS
 SELECT a.id,
    a.asset_code,
    a.asset_name,
    a.category_code,
    a.category_name,
    a.spec_model,
    a.brand,
    a.quantity,
    a.original_value,
    a.residual_rate,
    a.current_value,
    a.accumulated_depreciation,
    a.purchase_date,
    a.useful_life,
    a.depreciation_method,
    a.asset_status,
    a.location,
    a.warehouse_id,
    a.owner_unit_id,
    a.owner_unit_name,
    a.current_use_unit_id,
    a.qr_code_url,
    a.rfid_tag,
    a.photo_urls,
    a.attachment_urls,
    a.description,
    a.remark,
    a.create_by,
    a.create_time,
    a.update_by,
    a.update_time,
    a.is_deleted,
    u.unit_name AS owner_unit_name_full
   FROM (public.gc_asset_card a
     LEFT JOIN public.sys_unit u ON ((a.owner_unit_id = u.id)))
  WHERE (((a.asset_status)::text = 'IN_STOCK'::text) AND (a.is_deleted = false) AND (NOT (EXISTS ( SELECT 1
           FROM (public.gc_borrow_detail d
             JOIN public.gc_borrow_order o ON ((d.borrow_order_id = o.id)))
          WHERE ((d.asset_id = a.id) AND ((d.detail_status)::text = ANY ((ARRAY['PENDING'::character varying, 'OUT'::character varying])::text[])) AND (o.is_deleted = false))))));


--
-- Name: v_gc_dashboard_stats; Type: VIEW; Schema: public; Owner: -
--

CREATE VIEW public.v_gc_dashboard_stats AS
 SELECT count(*) AS total_assets,
    sum(
        CASE
            WHEN ((asset_status)::text = 'IN_STOCK'::text) THEN 1
            ELSE 0
        END) AS in_stock_count,
    sum(
        CASE
            WHEN ((asset_status)::text = '已借用'::text) THEN 1
            ELSE 0
        END) AS borrowed_count,
    sum(
        CASE
            WHEN ((asset_status)::text = '已调剂'::text) THEN 1
            ELSE 0
        END) AS transferred_count,
    sum(
        CASE
            WHEN ((asset_status)::text = '已处置'::text) THEN 1
            ELSE 0
        END) AS disposed_count,
    sum(original_value) AS total_original_value,
    sum(current_value) AS total_current_value,
    count(DISTINCT owner_unit_id) AS involved_units
   FROM public.gc_asset_card
  WHERE (is_deleted = false);


--
-- Name: cl_apply_order id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.cl_apply_order ALTER COLUMN id SET DEFAULT nextval('public.cl_apply_order_id_seq'::regclass);


--
-- Name: cl_cost_detail id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.cl_cost_detail ALTER COLUMN id SET DEFAULT nextval('public.cl_cost_detail_id_seq'::regclass);


--
-- Name: cl_dispatch_order id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.cl_dispatch_order ALTER COLUMN id SET DEFAULT nextval('public.cl_dispatch_order_id_seq'::regclass);


--
-- Name: cl_repair_order id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.cl_repair_order ALTER COLUMN id SET DEFAULT nextval('public.cl_repair_order_id_seq'::regclass);


--
-- Name: cl_track_point id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.cl_track_point ALTER COLUMN id SET DEFAULT nextval('public.cl_track_point_id_seq'::regclass);


--
-- Name: cl_vehicle_archive id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.cl_vehicle_archive ALTER COLUMN id SET DEFAULT nextval('public.cl_vehicle_archive_id_seq'::regclass);


--
-- Name: gc_asset_card id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.gc_asset_card ALTER COLUMN id SET DEFAULT nextval('public.gc_asset_card_id_seq'::regclass);


--
-- Name: gc_borrow_detail id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.gc_borrow_detail ALTER COLUMN id SET DEFAULT nextval('public.gc_borrow_detail_id_seq'::regclass);


--
-- Name: gc_borrow_order id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.gc_borrow_order ALTER COLUMN id SET DEFAULT nextval('public.gc_borrow_order_id_seq'::regclass);


--
-- Name: gc_logistics_order id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.gc_logistics_order ALTER COLUMN id SET DEFAULT nextval('public.gc_logistics_order_id_seq'::regclass);


--
-- Name: gc_return_order id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.gc_return_order ALTER COLUMN id SET DEFAULT nextval('public.gc_return_order_id_seq'::regclass);


--
-- Name: gc_transfer_detail id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.gc_transfer_detail ALTER COLUMN id SET DEFAULT nextval('public.gc_transfer_detail_id_seq'::regclass);


--
-- Name: gc_transfer_order id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.gc_transfer_order ALTER COLUMN id SET DEFAULT nextval('public.gc_transfer_order_id_seq'::regclass);


--
-- Name: gy_cleaning_order id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.gy_cleaning_order ALTER COLUMN id SET DEFAULT nextval('public.gy_cleaning_order_id_seq'::regclass);


--
-- Name: gy_occupant id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.gy_occupant ALTER COLUMN id SET DEFAULT nextval('public.gy_occupant_id_seq'::regclass);


--
-- Name: gy_repair_order id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.gy_repair_order ALTER COLUMN id SET DEFAULT nextval('public.gy_repair_order_id_seq'::regclass);


--
-- Name: gy_room id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.gy_room ALTER COLUMN id SET DEFAULT nextval('public.gy_room_id_seq'::regclass);


--
-- Name: pay_account id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.pay_account ALTER COLUMN id SET DEFAULT nextval('public.pay_account_id_seq'::regclass);


--
-- Name: pay_transaction id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.pay_transaction ALTER COLUMN id SET DEFAULT nextval('public.pay_transaction_id_seq'::regclass);


--
-- Name: st_inventory_record id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.st_inventory_record ALTER COLUMN id SET DEFAULT nextval('public.st_inventory_record_id_seq'::regclass);


--
-- Name: st_kitchen_waste id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.st_kitchen_waste ALTER COLUMN id SET DEFAULT nextval('public.st_kitchen_waste_id_seq'::regclass);


--
-- Name: st_material id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.st_material ALTER COLUMN id SET DEFAULT nextval('public.st_material_id_seq'::regclass);


--
-- Name: st_meal_reservation id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.st_meal_reservation ALTER COLUMN id SET DEFAULT nextval('public.st_meal_reservation_id_seq'::regclass);


--
-- Name: st_purchase_detail id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.st_purchase_detail ALTER COLUMN id SET DEFAULT nextval('public.st_purchase_detail_id_seq'::regclass);


--
-- Name: st_purchase_order id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.st_purchase_order ALTER COLUMN id SET DEFAULT nextval('public.st_purchase_order_id_seq'::regclass);


--
-- Name: sys_config id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.sys_config ALTER COLUMN id SET DEFAULT nextval('public.sys_config_id_seq'::regclass);


--
-- Name: sys_message id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.sys_message ALTER COLUMN id SET DEFAULT nextval('public.sys_message_id_seq'::regclass);


--
-- Name: sys_operation_log id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.sys_operation_log ALTER COLUMN id SET DEFAULT nextval('public.sys_operation_log_id_seq'::regclass);


--
-- Name: sys_role id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.sys_role ALTER COLUMN id SET DEFAULT nextval('public.sys_role_id_seq'::regclass);


--
-- Name: sys_unit id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.sys_unit ALTER COLUMN id SET DEFAULT nextval('public.sys_unit_id_seq'::regclass);


--
-- Name: sys_user id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.sys_user ALTER COLUMN id SET DEFAULT nextval('public.sys_user_id_seq'::regclass);


--
-- Name: sys_user_role id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.sys_user_role ALTER COLUMN id SET DEFAULT nextval('public.sys_user_role_id_seq'::regclass);


--
-- Data for Name: cl_apply_order; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.cl_apply_order (id, apply_no, applicant_id, applicant_name, applicant_unit_id, applicant_phone, purpose, destination, start_time, end_time, passenger_count, required_vehicle_type, planned_route, planned_mileage, apply_status, auto_approve, reject_reason, dispatch_order_id, process_instance_id, create_by, create_time, update_by, update_time, is_deleted, audit_user_id, audit_user_name, audit_time, audit_remark, remark) FROM stdin;
2	CL20260819002	2	李娜	2	\N	参加全省财政工作会议	兰州富力万达文华酒店	2026-08-26 06:30:00+08	2026-08-26 22:00:00+08	3	SEDAN	\N	150.00	PENDING	f	\N	\N	\N	\N	2026-08-19 16:57:10.533092+08	\N	2026-08-19 16:57:10.533092+08	f	\N	\N	\N	\N	\N
3	CL20260819003	3	王强	3	\N	赴酒泉市考察工业园区	酒泉工业园区	2026-08-27 08:00:00+08	2026-08-27 18:00:00+08	6	MPV	\N	80.00	DONE	f	\N	\N	\N	\N	2026-08-19 16:57:10.533092+08	\N	2026-08-19 16:57:10.533092+08	f	\N	\N	\N	\N	\N
4	CL20260819004	4	赵磊	4	\N	接送专家来访	嘉峪关机场	2026-08-28 09:00:00+08	2026-08-28 11:00:00+08	2	SEDAN	\N	30.00	REJECTED	f	\N	\N	\N	\N	2026-08-19 16:57:10.533092+08	\N	2026-08-19 16:57:10.533092+08	f	\N	\N	\N	\N	\N
5	CL20260819005	5	陈静	5	\N	下乡检查保障性住房	新城镇	2026-08-29 07:30:00+08	2026-08-29 19:00:00+08	8	BUS	\N	120.00	PENDING	f	\N	\N	\N	\N	2026-08-19 16:57:10.533092+08	\N	2026-08-19 16:57:10.533092+08	f	\N	\N	\N	\N	\N
9	CL2026080006	1	张伟	1	13800000001	公务接待	兰州市政府	2026-08-23 00:00:00+08	2026-08-24 00:00:00+08	4	SEDAN	\N	\N	DISPATCHED	f	\N	\N	\N	1	2026-08-21 11:40:44.20036+08	\N	2026-08-21 11:41:42.339286+08	f	\N	admin	2026-08-21 11:41:09.770275+08	允许调用	\N
1	CL20260819001	1	张伟	1	\N	赴省机关事务管理局汇报工作	兰州宁卧庄宾馆	2026-08-25 07:00:00+08	2026-08-25 20:00:00+08	5	BUS	\N	150.00	DISPATCHED	f	\N	\N	\N	\N	2026-08-19 16:57:10.533092+08	\N	2026-08-21 11:42:10.742523+08	f	\N	\N	\N	\N	\N
\.


--
-- Data for Name: cl_cost_detail; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.cl_cost_detail (id, vehicle_id, cost_type, cost_amount, cost_time, cost_desc, biz_order_no, biz_type, approval_status, process_instance_id, attachment_urls, create_by, create_time, update_by, update_time, is_deleted, current_mileage, fuel_quantity, approval_user_id, approval_time, approval_remark) FROM stdin;
1	1	FUEL	480.00	2026-08-15 10:00:00+08	92号汽油 60升	\N	\N	APPROVED	\N	\N	\N	2026-08-19 16:57:10.533092+08	\N	2026-08-19 16:57:10.533092+08	f	\N	\N	\N	\N	\N
2	2	TOLL	125.00	2026-08-16 09:30:00+08	嘉峪关-酒泉 过路费	\N	\N	APPROVED	\N	\N	\N	2026-08-19 16:57:10.533092+08	\N	2026-08-19 16:57:10.533092+08	f	\N	\N	\N	\N	\N
3	3	PARKING	30.00	2026-08-17 14:00:00+08	嘉峪关机场停车费	\N	\N	PENDING	\N	\N	\N	2026-08-19 16:57:10.533092+08	\N	2026-08-19 16:57:10.533092+08	f	\N	\N	\N	\N	\N
4	4	REPAIR	2800.00	2026-08-18 11:00:00+08	发动机维修	\N	\N	PENDING	\N	\N	\N	2026-08-19 16:57:10.533092+08	\N	2026-08-19 16:57:10.533092+08	f	\N	\N	\N	\N	\N
5	5	INSURANCE	8500.00	2026-08-01 08:00:00+08	2026年度交强险+商业险	\N	\N	APPROVED	\N	\N	\N	2026-08-19 16:57:10.533092+08	\N	2026-08-19 16:57:10.533092+08	f	\N	\N	\N	\N	\N
15	4	ETC	200.00	2026-08-22 10:30:00+08	途经兰成高速	\N	\N	REJECTED	\N		1	2026-08-21 11:37:16.127534+08	\N	2026-08-21 11:37:27.831717+08	f	\N	\N	1	2026-08-21 11:37:27.837683+08	可以走国道
16	4	INSURANCE	5000.00	2026-08-22 17:42:20+08	包含交强险，500w额度的车辆碰撞保险，300w额度的人身医疗保险。	DJ-20260822	\N	PENDING	\N		1	2026-08-22 17:43:15.305277+08	\N	2026-08-22 17:43:15.305277+08	f	\N	\N	\N	\N	\N
\.


--
-- Data for Name: cl_dispatch_order; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.cl_dispatch_order (id, dispatch_no, apply_id, vehicle_id, plate_number, driver_id, driver_name, driver_phone, scheduled_start, scheduled_end, actual_start, actual_end, actual_mileage, dispatch_status, is_emergency, emergency_reason, remark, create_by, create_time, update_by, update_time, is_deleted) FROM stdin;
2	DP20260819002	2	2	甘B-00002	4	赵磊	13800000004	2026-08-26 06:00:00+08	2026-08-26 23:00:00+08	\N	\N	\N	WAITING	f	\N	\N	\N	2026-08-19 16:57:10.533092+08	\N	2026-08-19 16:57:10.533092+08	f
3	DP20260819003	3	3	甘B-00003	1	张伟	13800000001	2026-08-27 07:30:00+08	2026-08-27 19:00:00+08	\N	\N	\N	RETURNED	f	\N	\N	\N	2026-08-19 16:57:10.533092+08	\N	2026-08-19 16:57:10.533092+08	f
4	DP20260819004	4	2	甘B-00002	2	李娜	13800000002	2026-08-28 08:30:00+08	2026-08-28 12:00:00+08	\N	\N	\N	CANCELLED	f	\N	\N	\N	2026-08-19 16:57:10.533092+08	\N	2026-08-19 16:57:10.533092+08	f
5	DP20260819005	5	5	甘B-00005	4	赵磊	13800000004	2026-08-29 07:00:00+08	2026-08-29 20:00:00+08	\N	\N	\N	ONGOING	f	\N	\N	\N	2026-08-19 16:57:10.533092+08	\N	2026-08-19 16:57:10.533092+08	f
1	DP20260819001	1	1	甘B-00001	4	赵磊	13800000004	2026-08-25 06:30:00+08	2026-08-25 21:00:00+08	\N	\N	\N	WAITING	f	\N	\N	\N	2026-08-19 16:57:10.533092+08	\N	2026-08-20 14:17:30.607399+08	f
7	DP2026080006	9	7	甘A-00001	4	赵磊	13800000004	2026-08-23 00:00:00+08	2026-08-24 00:00:00+08	\N	\N	\N	WAITING	\N	\N	\N	\N	2026-08-21 11:41:42.356679+08	\N	2026-08-21 11:41:42.356679+08	f
8	DP2026080007	1	1	甘B-00001	4	赵磊	13800000004	2026-08-21 11:42:07+08	2026-08-22 00:00:00+08	\N	2026-08-21 11:43:17+08	200.00	RETURNED	\N	\N	\N	\N	2026-08-21 11:42:10.745203+08	\N	2026-08-21 11:43:20.750267+08	f
\.


--
-- Data for Name: cl_repair_order; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.cl_repair_order (id, repair_no, vehicle_id, plate_number, repair_type, fault_desc, fault_photos, urgency_level, repair_shop_id, repair_shop_name, estimated_cost, actual_cost, parts_detail, labor_cost, order_status, process_instance_id, repair_start, repair_end, repair_photos, accept_user_id, accept_time, accept_result, accept_remark, repair_mileage, create_by, create_time, update_by, update_time, is_deleted, audit_user_id, audit_user_name, audit_time, audit_remark) FROM stdin;
1	REP20260819001	1	甘B-00001	MAINTENANCE	行驶里程达30000公里，常规保养	\N	NORMAL	\N	\N	680.00	\N	{"机油": "美孚5W-30", "机滤": "原厂"}	\N	DONE	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	2026-08-19 16:57:10.533092+08	\N	2026-08-19 16:57:10.533092+08	f	\N	\N	\N	\N
2	REP20260819002	2	甘B-00002	REPAIR	右前轮异响，需检查轴承	\N	URGENT	\N	\N	450.00	\N	{"轴承": "SKF"}	\N	PENDING	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	2026-08-19 16:57:10.533092+08	\N	2026-08-19 16:57:10.533092+08	f	\N	\N	\N	\N
3	REP20260819003	4	甘B-00004	REPAIR	发动机故障灯亮，加速无力	\N	URGENT	\N	\N	2800.00	\N	{"火花塞": "NGK", "点火线圈": "博世"}	\N	PENDING	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	2026-08-19 16:57:10.533092+08	\N	2026-08-19 16:57:10.533092+08	f	\N	\N	\N	\N
4	REP20260819004	5	甘B-00005	MAINTENANCE	刹车片磨损至警戒线	\N	NORMAL	\N	\N	1200.00	\N	{"刹车片": "金麒麟"}	\N	APPROVED	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	2026-08-19 16:57:10.533092+08	\N	2026-08-19 16:57:10.533092+08	f	\N	\N	\N	\N
5	REP20260819005	3	甘B-00003	REPAIR	车机屏幕黑屏，无法导航	\N	LOW	\N	\N	500.00	\N	{"屏幕总成": "比亚迪原厂"}	\N	DONE	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	2026-08-19 16:57:10.533092+08	\N	2026-08-19 16:57:10.533092+08	f	\N	\N	\N	\N
8	WX2026080001	7	甘A-00001	REPAIR	车辆从出厂已行驶超过20000公里，此为第一次大修保养（包含发动机气缸更换，发动机机油更换，包含轮胎，空气悬挂，		HIGH	\N	\N	\N	\N	\N	\N	PENDING	\N	\N	\N	\N	\N	\N	\N	\N	20000.00	1	2026-08-22 17:48:18.929969+08	\N	2026-08-22 17:48:18.929969+08	f	\N	\N	\N	\N
\.


--
-- Data for Name: cl_track_point; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.cl_track_point (id, dispatch_id, vehicle_id, lng, lat, speed, direction, track_time, create_time) FROM stdin;
107	5	5	98.3062273	39.7859611	37.00	14	2026-08-29 07:02:00+08	2026-08-20 15:44:27.929225+08
108	5	5	98.3271412	39.7953397	61.00	106	2026-08-29 07:04:00+08	2026-08-20 15:44:27.929225+08
109	5	5	98.3468242	39.8099900	54.00	160	2026-08-29 07:06:00+08	2026-08-20 15:44:27.929225+08
110	5	5	98.3676042	39.8241616	34.00	128	2026-08-29 07:12:00+08	2026-08-20 15:44:27.929225+08
111	5	5	98.3854847	39.8354868	41.00	190	2026-08-29 07:10:00+08	2026-08-20 15:44:27.929225+08
112	5	5	98.4044016	39.8452055	70.00	160	2026-08-29 07:12:00+08	2026-08-20 15:44:27.929225+08
113	5	5	98.4217318	39.8578806	32.00	192	2026-08-29 07:14:00+08	2026-08-20 15:44:27.929225+08
114	5	5	98.4410690	39.8702865	39.00	261	2026-08-29 07:24:00+08	2026-08-20 15:44:27.929225+08
115	5	5	98.4612080	39.8819047	80.00	30	2026-08-29 07:18:00+08	2026-08-20 15:44:27.929225+08
116	5	5	98.4802225	39.8962315	65.00	338	2026-08-29 07:20:00+08	2026-08-20 15:44:27.929225+08
117	5	5	98.5011533	39.9102283	38.00	33	2026-08-29 07:22:00+08	2026-08-20 15:44:27.929225+08
118	5	5	98.5197828	39.9210387	38.00	266	2026-08-29 07:24:00+08	2026-08-20 15:44:27.929225+08
119	5	5	98.5380069	39.9322097	37.00	261	2026-08-29 07:26:00+08	2026-08-20 15:44:27.929225+08
120	5	5	98.5557632	39.9458130	66.00	100	2026-08-29 07:42:00+08	2026-08-20 15:44:27.929225+08
121	5	5	98.5756705	39.9590873	31.00	39	2026-08-29 07:45:00+08	2026-08-20 15:44:27.929225+08
122	5	5	98.5918713	39.9670445	30.00	276	2026-08-29 07:48:00+08	2026-08-20 15:44:27.929225+08
123	5	5	98.6154447	39.9819224	42.00	269	2026-08-29 07:51:00+08	2026-08-20 15:44:27.929225+08
124	5	5	98.6341886	39.9915151	63.00	292	2026-08-29 07:36:00+08	2026-08-20 15:44:27.929225+08
125	5	5	98.6497505	40.0079354	60.00	144	2026-08-29 07:57:00+08	2026-08-20 15:44:27.929225+08
126	5	5	98.6724377	40.0161393	36.00	267	2026-08-29 07:40:00+08	2026-08-20 15:44:27.929225+08
127	5	5	98.6881243	40.0298225	33.00	262	2026-08-29 08:03:00+08	2026-08-20 15:44:27.929225+08
128	5	5	98.7079937	40.0417216	50.00	159	2026-08-29 08:06:00+08	2026-08-20 15:44:27.929225+08
129	5	5	98.7252960	40.0536759	39.00	359	2026-08-29 08:09:00+08	2026-08-20 15:44:27.929225+08
130	5	5	98.7458821	40.0671218	60.00	325	2026-08-29 07:48:00+08	2026-08-20 15:44:27.929225+08
131	5	5	98.7675683	40.0787519	49.00	13	2026-08-29 07:50:00+08	2026-08-20 15:44:27.929225+08
132	5	5	98.7865285	40.0939142	67.00	147	2026-08-29 07:52:00+08	2026-08-20 15:44:27.929225+08
\.


--
-- Data for Name: cl_vehicle_archive; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.cl_vehicle_archive (id, plate_number, brand_model, vehicle_type, vehicle_type_name, engine_no, frame_no, seat_count, displacement, color, purchase_date, purchase_price, supplier, unit_id, unit_name, dept_id, establishment_id, is_establishment, vehicle_status, current_mileage, last_maintenance_mileage, next_maintenance_mileage, insurance_company, insurance_policy_no, insurance_start, insurance_end, photo_urls, remark, create_by, create_time, update_by, update_time, is_deleted) FROM stdin;
2	甘B-00002	大众帕萨特 2024款	SEDAN	\N	\N	\N	5	1.80	\N	2024-03-15	220000.00	\N	2	\N	\N	\N	t	ON_DUTY	32560.00	\N	\N	\N	\N	\N	2027-03-15	\N	\N	\N	2026-08-19 16:57:10.533092+08	\N	2026-08-19 16:57:10.533092+08	f
3	甘B-00003	比亚迪汉 2025款	SEDAN	\N	\N	\N	5	0.00	\N	2025-01-10	250000.00	\N	3	\N	\N	\N	t	AVAILABLE	12500.00	\N	\N	\N	\N	\N	2028-01-10	\N	\N	\N	2026-08-19 16:57:10.533092+08	\N	2026-08-19 16:57:10.533092+08	f
4	甘B-00004	别克GL8 2024款	MPV	\N	\N	\N	7	2.00	\N	2024-09-01	350000.00	\N	4	\N	\N	\N	t	REPAIRING	48600.00	\N	\N	\N	\N	\N	2027-09-01	\N	\N	\N	2026-08-19 16:57:10.533092+08	\N	2026-08-19 16:57:10.533092+08	f
5	甘B-00005	金龙客车 2022款	BUS	\N	\N	\N	35	5.00	\N	2022-12-01	800000.00	\N	5	\N	\N	\N	t	MAINTAINING	125600.00	\N	\N	\N	\N	\N	2026-12-01	\N	\N	\N	2026-08-19 16:57:10.533092+08	\N	2026-08-19 16:57:10.533092+08	f
7	甘A-00001	上汽奥迪A6L	SEDAN	轿车	EA888	1	5	3.00	黑色	2025-09-01	300000.00	\N	2	嘉峪关市财政局	\N	\N	t	ON_DUTY	0.00	\N	\N	\N	\N	\N	\N	\N	公务接待用车	\N	2026-08-21 11:39:16.073825+08	\N	2026-08-21 11:41:42.339286+08	f
1	甘B-00001	丰田柯斯达 2023款	BUS	\N	\N	\N	19	4.00	\N	2023-06-01	450000.00	\N	1	\N	\N	\N	t	AVAILABLE	200.00	\N	\N	\N	\N	\N	2027-06-01	\N	\N	\N	2026-08-19 16:57:10.533092+08	\N	2026-08-21 11:43:20.750267+08	f
\.


--
-- Data for Name: gc_asset_card; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.gc_asset_card (id, asset_code, asset_name, category_code, category_name, spec_model, brand, quantity, original_value, residual_rate, current_value, accumulated_depreciation, purchase_date, useful_life, depreciation_method, asset_status, location, warehouse_id, owner_unit_id, owner_unit_name, current_use_unit_id, qr_code_url, rfid_tag, photo_urls, attachment_urls, description, remark, create_by, create_time, update_by, update_time, is_deleted, audit_user_id, audit_user_name, audit_time, audit_remark, in_stock_time) FROM stdin;
3	GLZ-JJ-2026-0003	红木办公桌椅套装	JJ_01	办公家具	1.6米	\N	1	3200.00	5.00	2800.00	0.00	2024-10-10	\N	STRAIGHT_LINE	已借用	\N	\N	2	\N	\N	\N	\N	\N	\N	\N	\N	\N	2026-08-19 16:56:45.886401+08	\N	2026-08-20 10:37:22.594091+08	f	\N	\N	\N	\N	\N
5	GLZ-DQ-2026-0005	海尔2匹空调	DQ_01	电器设备	壁挂式冷暖	\N	3	2800.00	5.00	2200.00	0.00	2025-06-01	\N	STRAIGHT_LINE	IN_STOCK	\N	\N	4	\N	\N	\N	\N	\N	\N	\N	\N	\N	2026-08-19 16:56:45.886401+08	\N	2026-08-20 10:42:50.10696+08	f	\N	\N	\N	\N	\N
1	GLZ-BG-2026-0001	联想ThinkPad X1 笔记本	IT_01	办公设备	i7/16G/512G	\N	1	8999.00	5.00	7500.00	0.00	2025-01-15	\N	STRAIGHT_LINE	IN_STOCK	\N	\N	1	\N	\N	\N	\N	\N	\N	\N	\N	\N	2026-08-19 16:56:45.886401+08	\N	2026-08-20 11:11:32.178242+08	f	\N	\N	\N	\N	\N
2	GLZ-BG-2026-0002	佳能MF746 打印机	IT_02	办公设备	A3黑白激光	\N	2	4500.00	5.00	3800.00	0.00	2025-03-20	\N	STRAIGHT_LINE	IN_STOCK	\N	\N	1	\N	\N	\N	\N	\N	\N	\N	\N	\N	2026-08-19 16:56:45.886401+08	\N	2026-08-20 11:11:32.178242+08	f	\N	\N	\N	\N	\N
4	GLZ-JJ-2026-0004	三人大会议桌	JJ_02	办公家具	3.2米*1.2米	\N	1	5800.00	5.00	5000.00	0.00	2024-12-05	\N	STRAIGHT_LINE	IN_STOCK	\N	\N	3	\N	\N	\N	\N	\N	\N	\N	\N	\N	2026-08-19 16:56:45.886401+08	\N	2026-08-20 12:48:11.98891+08	f	\N	\N	\N	\N	\N
\.


--
-- Data for Name: gc_borrow_detail; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.gc_borrow_detail (id, borrow_order_id, asset_id, asset_code, asset_name, borrow_quantity, actual_quantity, return_quantity, detail_status, return_time, accept_status, accept_remark, damage_description, create_time, update_time) FROM stdin;
1	1	1	GLZ-BG-2026-0001	联想ThinkPad X1 笔记本	1	1	\N	OUT	\N	\N	\N	\N	2026-08-19 16:56:45.886401+08	2026-08-19 16:56:45.886401+08
2	1	2	GLZ-BG-2026-0002	佳能MF746 打印机	1	1	\N	OUT	\N	\N	\N	\N	2026-08-19 16:56:45.886401+08	2026-08-19 16:56:45.886401+08
3	2	3	GLZ-JJ-2026-0003	红木办公桌椅套装	1	1	\N	RETURNED	\N	\N	\N	\N	2026-08-19 16:56:45.886401+08	2026-08-19 16:56:45.886401+08
4	3	4	GLZ-JJ-2026-0004	三人大会议桌	1	\N	\N	PENDING	\N	\N	\N	\N	2026-08-19 16:56:45.886401+08	2026-08-19 16:56:45.886401+08
5	5	5	GLZ-DQ-2026-0005	海尔2匹空调	2	2	\N	RETURNED	\N	\N	\N	\N	2026-08-19 16:56:45.886401+08	2026-08-19 16:56:45.886401+08
\.


--
-- Data for Name: gc_borrow_order; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.gc_borrow_order (id, order_no, applicant_id, applicant_name, applicant_unit_id, applicant_unit_name, applicant_phone, borrow_start, borrow_end, borrow_reason, borrow_purpose, order_status, approval_status, current_approver_id, current_approver_node, warehouse_out_time, warehouse_in_time, logistics_order_no, logistics_status, extension_count, max_extension, process_instance_id, create_by, create_time, update_by, update_time, is_deleted, audit_user_id, audit_user_name, audit_time, audit_remark, remark) FROM stdin;
3	JY20260819003	3	王强	3	\N	\N	2026-08-22 08:00:00+08	2026-08-29 20:00:00+08	项目评审会，借用投影设备	\N	DRAFT	\N	\N	\N	\N	\N	\N	\N	0	2	\N	\N	2026-08-19 16:56:45.886401+08	\N	2026-08-19 16:56:45.886401+08	f	\N	\N	\N	\N	\N
4	JY20260819004	4	赵磊	4	\N	\N	2026-08-23 14:00:00+08	2026-09-23 14:00:00+08	干部培训，借用桌椅30套	\N	PENDING	\N	\N	\N	\N	\N	WL20260819004	\N	0	2	\N	\N	2026-08-19 16:56:45.886401+08	\N	2026-08-19 16:56:45.886401+08	f	\N	\N	\N	\N	\N
5	JY20260819005	5	陈静	5	\N	\N	2026-08-24 09:00:00+08	2026-09-01 17:00:00+08	仓库盘点需要临时借用办公桌	\N	DONE	\N	\N	\N	\N	\N	\N	\N	0	2	\N	\N	2026-08-19 16:56:45.886401+08	\N	2026-08-19 16:56:45.886401+08	f	\N	\N	\N	\N	\N
2	JY20260819002	2	李娜	2	\N	\N	2026-08-21 10:00:00+08	2026-08-28 17:00:00+08	财务年终决算会议，借用会议桌	\N	BORROWING	\N	\N	\N	\N	\N	WL20260819003	\N	0	2	\N	\N	2026-08-19 16:56:45.886401+08	\N	2026-08-20 10:37:22.592154+08	f	\N	\N	\N	\N	\N
1	JY20260819001	1	张伟	1	\N	\N	2026-08-20 09:00:00+08	2026-09-20 18:00:00+08	召开全市节能会议，需借用笔记本和打印机	\N	APPROVED	\N	\N	\N	\N	\N	WL20260819001	\N	0	2	\N	\N	2026-08-19 16:56:45.886401+08	\N	2026-08-20 10:42:50.105133+08	f	\N	\N	\N	\N	\N
\.


--
-- Data for Name: gc_logistics_order; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.gc_logistics_order (id, logistics_no, business_type, business_order_no, delivery_method, sender_name, sender_phone, sender_address, receiver_name, receiver_phone, receiver_address, cargo_desc, cargo_weight, cargo_volume, logistics_status, pickup_time, delivery_time, sign_time, sign_person, sign_photo_url, third_party_tracking_no, third_party_platform, freight_amount, create_by, create_time, update_by, update_time, is_deleted) FROM stdin;
1	WL20260819001	BORROW	JY20260819001	同城配送	机关事务管理局	\N	\N	工信局办公室	\N	嘉峪关市新华路88号	\N	\N	\N	DELIVERED	\N	\N	\N	\N	\N	\N	\N	\N	\N	2026-08-19 16:56:45.886401+08	\N	2026-08-19 16:56:45.886401+08	f
2	WL20260819002	TRANSFER	TJ20260819001	物流专线	财政局	\N	\N	人才公寓	\N	嘉峪关市雄关路66号	\N	\N	\N	PENDING	\N	\N	\N	\N	\N	\N	\N	\N	\N	2026-08-19 16:56:45.886401+08	\N	2026-08-19 16:56:45.886401+08	f
3	WL20260819003	RETURN	JY20260819002	同城配送	工信局	\N	\N	机关事务管理局	\N	嘉峪关市体育大道1号	\N	\N	\N	SIGNED	\N	\N	\N	\N	\N	\N	\N	\N	\N	2026-08-19 16:56:45.886401+08	\N	2026-08-19 16:56:45.886401+08	f
4	WL20260819004	BORROW	JY20260819003	快递	人社局	\N	\N	科技局	\N	嘉峪关市科技路99号	\N	\N	\N	PICKED_UP	\N	\N	\N	\N	\N	\N	\N	\N	\N	2026-08-19 16:56:45.886401+08	\N	2026-08-19 16:56:45.886401+08	f
5	WL20260819005	DISPOSE	CZ20260819001	货运	住建局	\N	\N	废旧物资回收站	\N	嘉峪关市工业园区	\N	\N	\N	PENDING	\N	\N	\N	\N	\N	\N	\N	\N	\N	2026-08-19 16:56:45.886401+08	\N	2026-08-19 16:56:45.886401+08	f
\.


--
-- Data for Name: gc_return_order; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.gc_return_order (id, return_no, borrow_order_id, return_applicant_id, return_applicant_unit_id, plan_return_time, actual_return_time, accept_user_ids, accept_time, accept_result, accept_remark, accept_photos, damage_info, damage_responsibility, repair_cost, compensation_amount, return_status, create_by, create_time, update_by, update_time, is_deleted) FROM stdin;
1	GH20260819001	2	2	2	\N	2026-08-28 16:00:00+08	\N	\N	ACCEPTED	完好无损，验收合格	\N	\N	\N	\N	\N	DONE	\N	2026-08-19 16:56:45.886401+08	\N	2026-08-19 16:56:45.886401+08	f
2	GH20260819002	5	5	5	\N	2026-09-01 16:30:00+08	\N	\N	ACCEPTED	桌椅完好	\N	\N	\N	\N	\N	DONE	\N	2026-08-19 16:56:45.886401+08	\N	2026-08-19 16:56:45.886401+08	f
3	GH20260819003	1	1	1	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	PENDING	\N	2026-08-19 16:56:45.886401+08	\N	2026-08-19 16:56:45.886401+08	f
4	GH20260819004	3	3	3	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	PENDING	\N	2026-08-19 16:56:45.886401+08	\N	2026-08-19 16:56:45.886401+08	f
5	GH20260819005	4	4	4	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	PENDING	\N	2026-08-19 16:56:45.886401+08	\N	2026-08-19 16:56:45.886401+08	f
\.


--
-- Data for Name: gc_transfer_detail; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.gc_transfer_detail (id, transfer_order_id, asset_id, asset_code, asset_name, create_time) FROM stdin;
\.


--
-- Data for Name: gc_transfer_order; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.gc_transfer_order (id, order_no, transfer_type, asset_count, total_value, applicant_unit_id, receive_unit_id, apply_reason, dispose_method, appraisal_org, appraisal_value, appraisal_report_url, appraisal_time, order_status, exec_time, exec_result, income_amount, expense_amount, process_instance_id, create_by, create_time, update_by, update_time, is_deleted, audit_user_id, audit_user_name, audit_time, audit_remark, remark) FROM stdin;
1	TJ20260819001	TRANSFER	1	2800.00	2	4	财政局闲置桌椅调剂至人社局使用	PUBLIC_AUCTION	\N	\N	\N	\N	PENDING	\N	\N	0.00	0.00	\N	\N	2026-08-19 16:56:45.886401+08	\N	2026-08-19 16:56:45.886401+08	f	\N	\N	\N	\N	\N
2	TJ20260819002	TRANSFER	1	5000.00	3	5	会议桌调剂至住建局新会议室	PUBLIC_AUCTION	\N	\N	\N	\N	APPROVED	\N	\N	0.00	0.00	\N	\N	2026-08-19 16:56:45.886401+08	\N	2026-08-19 16:56:45.886401+08	f	\N	\N	\N	\N	\N
3	TJ20260819003	DISPOSE	3	6600.00	1	\N	空调老旧能耗高，申请报废处置	SCRAP	\N	\N	\N	\N	PENDING	\N	\N	0.00	0.00	\N	\N	2026-08-19 16:56:45.886401+08	\N	2026-08-19 16:56:45.886401+08	f	\N	\N	\N	\N	\N
4	TJ20260819004	TRANSFER	2	7600.00	4	2	培训桌椅调剂至财政局使用	PUBLIC_AUCTION	\N	\N	\N	\N	DONE	\N	\N	0.00	0.00	\N	\N	2026-08-19 16:56:45.886401+08	\N	2026-08-19 16:56:45.886401+08	f	\N	\N	\N	\N	\N
5	TJ20260819005	DISPOSE	1	8999.00	5	\N	笔记本损坏无法修复	SCRAP	\N	\N	\N	\N	APPROVED	\N	\N	0.00	0.00	\N	\N	2026-08-19 16:56:45.886401+08	\N	2026-08-19 16:56:45.886401+08	f	\N	\N	\N	\N	\N
\.


--
-- Data for Name: gy_cleaning_order; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.gy_cleaning_order (id, cleaning_no, room_id, room_no, applicant_id, applicant_name, cleaning_time, cleaning_scope, cleaning_requirement, order_status, assignee_id, assignee_name, assignee_company, assign_time, execute_time, execute_photos, accept_user_id, accept_time, accept_result, accept_remark, accept_score, settle_amount, settle_status, create_by, create_time, update_by, update_time, is_deleted, cleaning_type, audit_user_id, audit_user_name, audit_time, audit_remark) FROM stdin;
1	BJ20260819001	1	A-0501	1	赵青山	2026-08-20 09:00:00+08	全屋深度保洁	\N	DONE	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	2026-08-19 16:57:10.533092+08	\N	2026-08-19 16:57:10.533092+08	f	REGULAR	\N	\N	\N	\N
2	BJ20260819002	4	B-0302	2	孙丽萍	2026-08-21 10:00:00+08	客厅+卧室	\N	ONGOING	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	2026-08-19 16:57:10.533092+08	\N	2026-08-19 16:57:10.533092+08	f	REGULAR	\N	\N	\N	\N
3	BJ20260819003	3	B-0301	3	周建国	2026-08-22 14:00:00+08	全屋深度保洁	\N	PENDING	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	2026-08-19 16:57:10.533092+08	\N	2026-08-19 16:57:10.533092+08	f	REGULAR	\N	\N	\N	\N
4	BJ20260819004	2	A-0502	4	吴秀英	2026-08-23 09:30:00+08	厨房+卫生间	\N	PENDING	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	2026-08-19 16:57:10.533092+08	\N	2026-08-19 16:57:10.533092+08	f	REGULAR	\N	\N	\N	\N
5	BJ20260819005	5	B-0401	5	郑晓东	2026-08-24 15:00:00+08	全屋玻璃清洁	\N	PENDING	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	2026-08-19 16:57:10.533092+08	\N	2026-08-19 16:57:10.533092+08	f	REGULAR	\N	\N	\N	\N
\.


--
-- Data for Name: gy_occupant; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.gy_occupant (id, occupant_name, occupant_type, id_card, phone, unit_id, unit_name, "position", attendant_info, room_id, room_no, checkin_time, expected_leave_time, actual_leave_time, rent_amount, rent_paid_status, assign_method, approval_file_url, occupant_status, apply_id, process_instance_id, create_by, create_time, update_by, update_time, is_deleted, apply_reason, audit_user_id, audit_user_name, audit_time, audit_remark, remark, checkout_time, room_condition, facility_check_result, settlement_amount, settlement_detail, checkout_photos, accept_user_id, accept_user_name, accept_time) FROM stdin;
1	赵青山	EXPERT	620123198001011234	13600001111	1	机关事务管理局	高级工程师	\N	1	A-0501	2026-06-01 10:00:00+08	2027-06-01 10:00:00+08	\N	0.00	PAID	DIRECT	\N	ACTIVE	\N	\N	\N	2026-08-19 16:57:10.533092+08	\N	2026-08-19 16:57:10.533092+08	f	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N
2	孙丽萍	TALENT	620123199003156789	13600002222	2	财政局	注册会计师	\N	4	B-0302	2026-07-15 09:00:00+08	2027-07-15 09:00:00+08	\N	0.00	PAID	APPROVAL	\N	ACTIVE	\N	\N	\N	2026-08-19 16:57:10.533092+08	\N	2026-08-19 16:57:10.533092+08	f	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N
3	周建国	TALENT	620123198812015555	13600003333	3	工信局	高级项目经理	\N	3	B-0301	2026-08-01 14:00:00+08	2027-08-01 14:00:00+08	\N	0.00	PAID	APPROVAL	\N	ACTIVE	\N	\N	\N	2026-08-19 16:57:10.533092+08	\N	2026-08-19 16:57:10.533092+08	f	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N
4	吴秀英	EXPERT	620123197505101234	13600004444	4	人社局	研究员	\N	2	A-0502	2026-05-20 11:00:00+08	2026-11-20 11:00:00+08	\N	0.00	PAID	DIRECT	\N	ACTIVE	\N	\N	\N	2026-08-19 16:57:10.533092+08	\N	2026-08-19 16:57:10.533092+08	f	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N
5	郑晓东	TALENT	620123199606188888	13600005555	5	住建局	建筑设计师	\N	5	B-0401	2026-08-18 16:00:00+08	2027-08-18 16:00:00+08	\N	0.00	PAID	APPROVAL	\N	ACTIVE	\N	\N	\N	2026-08-19 16:57:10.533092+08	\N	2026-08-19 16:57:10.533092+08	f	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N
13	退住验收测试	EXPERT	\N	13500006666	1	嘉峪关市机关事务管理局	\N	\N	\N	A-0502	2026-08-01 09:00:00+08	2026-08-20 18:00:00+08	2026-08-20 23:27:20.833489+08	0.00	\N	DIRECT	\N	RESIGNED	\N	\N	1	2026-08-20 23:25:24.870813+08	\N	2026-08-20 23:33:36.026083+08	t	\N	\N	\N	\N	\N	已清点	2026-08-20 23:29:39+08	整体状况良好,空调有异响	{"冰箱":"完好","电视":"完好","空调":"损坏","洗衣机":"完好"}	1280.00	水费180元 电费300元 物业费800元	退房照片1.png	1	admin	2026-08-20 23:32:06.422081+08
\.


--
-- Data for Name: gy_repair_order; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.gy_repair_order (id, repair_no, room_id, room_no, applicant_id, applicant_name, applicant_phone, fault_location, fault_desc, fault_photos, urgency_level, cost_type, estimated_cost, actual_cost, quote_detail, order_status, process_instance_id, repair_shop_id, repair_shop_name, repair_start, repair_end, repair_photos, accept_user_id, accept_time, accept_result, accept_remark, accept_photos, create_by, create_time, update_by, update_time, is_deleted, repair_type, parts_detail, labor_cost, audit_user_id, audit_user_name, audit_time, audit_remark) FROM stdin;
1	WX20260819001	1	A-0501	1	赵青山	\N	\N	卫生间水龙头漏水严重	\N	URGENT	UNIT	\N	\N	\N	DONE	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	2026-08-19 16:57:10.533092+08	\N	2026-08-19 16:57:10.533092+08	f	\N	\N	\N	\N	\N	\N	\N
2	WX20260819002	4	B-0302	2	孙丽萍	\N	\N	客厅空调不制冷	\N	URGENT	PERSONAL	\N	\N	\N	PENDING	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	2026-08-19 16:57:10.533092+08	\N	2026-08-19 16:57:10.533092+08	f	\N	\N	\N	\N	\N	\N	\N
3	WX20260819003	3	B-0301	3	周建国	\N	\N	厨房插座无电	\N	NORMAL	UNIT	\N	\N	\N	PENDING	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	2026-08-19 16:57:10.533092+08	\N	2026-08-19 16:57:10.533092+08	f	\N	\N	\N	\N	\N	\N	\N
4	WX20260819004	2	A-0502	4	吴秀英	\N	\N	卧室门锁损坏	\N	NORMAL	UNIT	\N	\N	\N	APPROVED	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	2026-08-19 16:57:10.533092+08	\N	2026-08-19 16:57:10.533092+08	f	\N	\N	\N	\N	\N	\N	\N
5	WX20260819005	5	B-0401	5	郑晓东	\N	\N	热水器加热慢	\N	LOW	PERSONAL	\N	\N	\N	PENDING	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	\N	2026-08-19 16:57:10.533092+08	\N	2026-08-19 16:57:10.533092+08	f	\N	\N	\N	\N	\N	\N	\N
\.


--
-- Data for Name: gy_room; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.gy_room (id, building, floor, room_no, room_type, layout, area, orientation, facilities, room_status, current_occupant_id, current_occupant_name, current_unit_id, asset_ids, remark, create_by, create_time, update_by, update_time, is_deleted) FROM stdin;
1	专家公寓A栋	5	A-0501	EXPERT_APARTMENT	三室两厅	120.50	\N	{"冰箱": 1, "电视": 1, "空调": 3, "洗衣机": 1, "热水器": 1}	OCCUPIED	\N	\N	\N	\N	\N	\N	2026-08-19 16:57:10.533092+08	\N	2026-08-19 16:57:10.533092+08	f
3	人才公寓B栋	3	B-0301	TALENT_APARTMENT	两室一厅	85.00	\N	{"电视": 1, "空调": 2, "洗衣机": 1}	RESERVED	\N	\N	\N	\N	\N	\N	2026-08-19 16:57:10.533092+08	\N	2026-08-19 16:57:10.533092+08	f
4	人才公寓B栋	3	B-0302	TALENT_APARTMENT	两室一厅	85.00	\N	{"电视": 1, "空调": 2}	OCCUPIED	\N	\N	\N	\N	\N	\N	2026-08-19 16:57:10.533092+08	\N	2026-08-19 16:57:10.533092+08	f
5	人才公寓B栋	4	B-0401	TALENT_APARTMENT	两室一厅	85.00	\N	{"电视": 1, "空调": 2, "洗衣机": 1}	IDLE	\N	\N	\N	\N	\N	\N	2026-08-19 16:57:10.533092+08	\N	2026-08-20 22:13:00.511447+08	f
2	专家公寓A栋	5	A-0502	EXPERT_APARTMENT	三室两厅	120.50	\N	{"冰箱": 1, "电视": 1, "空调": 3, "洗衣机": 1}	IDLE	\N	\N	\N	\N	\N	\N	2026-08-19 16:57:10.533092+08	\N	2026-08-20 23:33:36.036398+08	f
\.


--
-- Data for Name: pay_account; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.pay_account (id, user_id, account_type, account_no, balance, frozen_amount, card_no, card_status, account_status, last_transaction_time, create_by, create_time, update_by, update_time, is_deleted) FROM stdin;
2	2	MEAL_CARD	ACC20260819002	320.50	0.00	CARD_6689	ACTIVE	ACTIVE	\N	\N	2026-08-19 16:57:25.836837+08	\N	2026-08-19 16:57:25.836837+08	f
3	3	VIRTUAL	ACC20260819003	1500.00	200.00	\N	ACTIVE	ACTIVE	\N	\N	2026-08-19 16:57:25.836837+08	\N	2026-08-19 16:57:25.836837+08	f
4	4	MEAL_CARD	ACC20260819004	80.00	0.00	CARD_6690	EXPIRED	INACTIVE	\N	\N	2026-08-19 16:57:25.836837+08	\N	2026-08-19 16:57:25.836837+08	f
5	5	MEAL_CARD	ACC20260819005	250.00	50.00	CARD_6691	ACTIVE	ACTIVE	\N	\N	2026-08-19 16:57:25.836837+08	\N	2026-08-19 16:57:25.836837+08	f
1	1	MEAL_CARD	ACC20260819001	675.00	0.00	CARD_6688	ACTIVE	ACTIVE	2026-08-23 14:43:04.382505+08	\N	2026-08-19 16:57:25.836837+08	\N	2026-08-23 14:46:31.681149+08	f
\.


--
-- Data for Name: pay_transaction; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.pay_transaction (id, transaction_no, external_transaction_no, user_id, account_type, account_no, card_no, transaction_type, amount, balance_before, balance_after, pay_method, pay_status, biz_module, biz_order_no, scene, remark, pay_time, create_time) FROM stdin;
1	TRX20260819001	\N	1	MEAL_CARD	ACC20260819001	\N	RECHARGE	200.00	500.00	700.00	WECHAT	SUCCESS	PAY_CENTER	\N	微信充值	\N	\N	2026-08-19 16:57:25.836837+08
2	TRX20260819002	\N	1	MEAL_CARD	ACC20260819001	\N	CONSUME	-25.00	700.00	675.00	FACE	SUCCESS	ST_DINING	RES20260819001	午餐自助	\N	\N	2026-08-19 16:57:25.836837+08
3	TRX20260819003	\N	2	MEAL_CARD	ACC20260819002	\N	CONSUME	-50.00	370.50	320.50	CARD	SUCCESS	ST_DINING	RES20260819002	两人晚餐	\N	\N	2026-08-19 16:57:25.836837+08
4	TRX20260819004	\N	3	VIRTUAL	ACC20260819003	\N	DEDUCT	-150.00	1650.00	1500.00	ALIPAY	SUCCESS	PAY_CENTER	\N	保证金扣除	\N	\N	2026-08-19 16:57:25.836837+08
5	TRX20260819005	\N	5	MEAL_CARD	ACC20260819005	\N	REFUND	30.00	220.00	250.00	WECHAT	SUCCESS	PAY_CENTER	\N	退款-重复扣费	\N	\N	2026-08-19 16:57:25.836837+08
\.


--
-- Data for Name: st_inventory_record; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.st_inventory_record (id, material_id, material_code, material_name, record_type, quantity, unit_price, total_amount, business_order_no, business_type, stock_before, stock_after, operator_id, operator_name, remark, create_time) FROM stdin;
1	1	MAT_2026_0001	东北珍珠米	IN	50.00	135.00	6750.00	\N	\N	50.00	100.00	\N	采购员张红	\N	2026-08-19 16:57:25.836837+08
2	4	MAT_2026_0004	双汇冷鲜猪肉	OUT	-30.00	18.50	-555.00	\N	\N	80.00	50.00	\N	厨师长王刚	\N	2026-08-19 16:57:25.836837+08
3	2	MAT_2026_0002	五得利面粉	IN	20.00	110.00	2200.00	\N	\N	40.00	60.00	\N	采购员张红	\N	2026-08-19 16:57:25.836837+08
4	5	MAT_2026_0005	正大鸡蛋	OUT	-10.00	25.00	-250.00	\N	\N	45.00	35.00	\N	面点师刘师傅	\N	2026-08-19 16:57:25.836837+08
5	3	MAT_2026_0003	鲁花花生油	ADJUST	5.00	169.00	845.00	\N	\N	25.00	30.00	\N	库存管理员李丽	\N	2026-08-19 16:57:25.836837+08
\.


--
-- Data for Name: st_kitchen_waste; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.st_kitchen_waste (id, record_date, meal_type, waste_weight, waste_type, disposal_method, disposal_person, remark, create_by, create_time, update_by, update_time, is_deleted) FROM stdin;
\.


--
-- Data for Name: st_material; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.st_material (id, material_code, material_name, category, spec, unit, shelf_life, safety_stock, max_stock, current_price, last_price, current_stock, occupied_stock, remark, create_by, create_time, update_by, update_time, is_deleted) FROM stdin;
5	MAT_2026_0005	正大鸡蛋	FRESH_INGREDIENTS	30枚/盘	盘	30	15.00	1000.00	25.00	\N	45.00	0.00	\N	\N	2026-08-19 16:57:25.836837+08	\N	2026-08-19 16:57:25.836837+08	f
6	TEST_MAT_0001	一次性餐盒	DAILY_GOODS	1000ml	个	365	50.00	2000.00	0.85	\N	0.00	0.00	测试数据	\N	2026-08-23 13:04:35.814319+08	\N	2026-08-23 13:04:46.803854+08	t
7	TEST_MAT_0002	洗洁精（大桶）	DAILY_GOODS	5kg/桶	桶	720	12.00	600.00	22.00	\N	0.00	0.00	编辑后	\N	2026-08-23 13:05:02.197546+08	\N	2026-08-23 13:05:20.709969+08	t
8	TEST_FRONT_001	测试西红柿(精品)	FRESH_INGREDIENTS	500g/份	份	\N	20.00	800.00	6.50	\N	0.00	0.00	前端测试	\N	2026-08-23 13:09:05.83371+08	\N	2026-08-23 13:11:26.819284+08	t
9	TEST_SHELF_001	测试保质期验证	CONDIMENT	1kg	袋	15	5.00	100.00	9.90	\N	0.00	0.00	\N	\N	2026-08-23 13:12:42.126662+08	\N	2026-08-23 13:12:54.040353+08	t
2	MAT_2026_0002	五得利面粉	FRESH_INGREDIENTS	25kg/袋	袋	120	8.00	1000.00	110.00	\N	60.00	0.00	\N	\N	2026-08-19 16:57:25.836837+08	\N	2026-08-23 13:37:02.367932+08	f
3	MAT_2026_0003	鲁花花生油	CONDIMENT	5L/桶	桶	540	5.00	1000.00	169.00	\N	30.00	0.00	\N	\N	2026-08-19 16:57:25.836837+08	\N	2026-08-23 13:37:02.368437+08	f
4	MAT_2026_0004	双汇冷鲜猪肉	FRESH_INGREDIENTS	精瘦	斤	7	20.00	1000.00	18.50	\N	50.00	0.00	\N	\N	2026-08-19 16:57:25.836837+08	\N	2026-08-23 13:37:02.368845+08	f
1	MAT_2026_0001	东北珍珠米	FRESH_INGREDIENTS	25kg/袋	袋	180	10.00	1000.00	135.00	\N	100.00	0.00	\N	\N	2026-08-19 16:57:25.836837+08	\N	2026-08-23 13:47:05.287335+08	f
\.


--
-- Data for Name: st_meal_reservation; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.st_meal_reservation (id, user_id, user_name, unit_id, unit_name, meal_date, meal_type, meal_count, reservation_time, cancel_time, is_cancelled, create_time, update_time, remark) FROM stdin;
1	1	张伟	1	机关事务管理局	2026-08-19	LUNCH	1	2026-08-19 16:57:25.836837+08	\N	f	2026-08-19 16:57:25.836837+08	2026-08-19 16:57:25.836837+08	\N
2	2	李娜	2	财政局	2026-08-19	LUNCH	2	2026-08-19 16:57:25.836837+08	\N	f	2026-08-19 16:57:25.836837+08	2026-08-19 16:57:25.836837+08	\N
3	3	王强	3	工信局	2026-08-20	DINNER	1	2026-08-19 16:57:25.836837+08	\N	f	2026-08-19 16:57:25.836837+08	2026-08-19 16:57:25.836837+08	\N
4	4	赵磊	4	人社局	2026-08-19	BREAKFAST	1	2026-08-19 16:57:25.836837+08	\N	f	2026-08-19 16:57:25.836837+08	2026-08-19 16:57:25.836837+08	\N
5	5	陈静	5	住建局	2026-08-20	LUNCH	3	2026-08-19 16:57:25.836837+08	\N	f	2026-08-19 16:57:25.836837+08	2026-08-19 16:57:25.836837+08	\N
\.


--
-- Data for Name: st_purchase_detail; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.st_purchase_detail (id, purchase_order_id, material_id, material_code, material_name, quantity, unit_price, subtotal, received_quantity, receive_time, create_time, update_time) FROM stdin;
1	1	1	MAT_2026_0001	东北珍珠米	50.00	135.00	6750.00	50.00	\N	2026-08-19 16:57:25.836837+08	2026-08-19 16:57:25.836837+08
2	2	4	MAT_2026_0004	双汇冷鲜猪肉	200.00	18.50	3700.00	0.00	\N	2026-08-19 16:57:25.836837+08	2026-08-19 16:57:25.836837+08
3	3	2	MAT_2026_0002	五得利面粉	20.00	110.00	2200.00	20.00	\N	2026-08-19 16:57:25.836837+08	2026-08-19 16:57:25.836837+08
4	3	3	MAT_2026_0003	鲁花花生油	50.00	169.00	8450.00	30.00	\N	2026-08-19 16:57:25.836837+08	2026-08-19 16:57:25.836837+08
5	5	5	MAT_2026_0005	正大鸡蛋	50.00	25.00	1250.00	0.00	\N	2026-08-19 16:57:25.836837+08	2026-08-19 16:57:25.836837+08
\.


--
-- Data for Name: st_purchase_order; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.st_purchase_order (id, order_no, purchase_reason, total_amount, material_count, effective_start, effective_end, is_expired, supplier_id, supplier_name, order_status, process_instance_id, accept_users, accept_time, accept_status, accept_remark, create_by, create_time, update_by, update_time, is_deleted, audit_user_id, audit_user_name, audit_time, audit_remark) FROM stdin;
1	CG20260819001	大米库存低于安全线，紧急采购	6750.00	1	2026-08-19 16:57:25.836837+08	2026-08-21 16:57:25.836837+08	f	\N	嘉峪关军粮供应站	RECEIVED	\N	\N	\N	\N	\N	\N	2026-08-19 16:57:25.836837+08	\N	2026-08-19 16:57:25.836837+08	f	\N	\N	\N	\N
2	CG20260819002	食堂每周肉类补充	3700.00	1	2026-08-19 16:57:25.836837+08	2026-08-21 16:57:25.836837+08	f	\N	双汇冷鲜肉嘉峪关店	PENDING	\N	\N	\N	\N	\N	\N	2026-08-19 16:57:25.836837+08	\N	2026-08-19 16:57:25.836837+08	f	\N	\N	\N	\N
4	CG20260819004	鸡蛋储备不足	1250.00	1	2026-08-19 16:57:25.836837+08	2026-08-21 16:57:25.836837+08	f	\N	正大蛋业	APPROVED	\N	\N	\N	\N	\N	\N	2026-08-19 16:57:25.836837+08	\N	2026-08-19 16:57:25.836837+08	f	\N	\N	\N	\N
5	CG20260819005	新菜品研发食材采购	3200.00	1	2026-08-16 16:57:25.836837+08	2026-08-18 16:57:25.836837+08	f	\N	本地蔬菜批发市场	EXPIRED	\N	\N	\N	\N	\N	\N	2026-08-19 16:57:25.836837+08	\N	2026-08-19 16:57:25.836837+08	f	\N	\N	\N	\N
3	CG20260819003	粮油调味品季度采购	8450.00	2	2026-08-19 16:57:25.836837+08	2026-08-21 16:57:25.836837+08	t	\N	嘉峪关市商贸公司	EXPIRED	\N	\N	\N	\N	\N	\N	2026-08-19 16:57:25.836837+08	\N	2026-08-23 13:20:37.14833+08	f	\N	\N	\N	\N
\.


--
-- Data for Name: sys_config; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.sys_config (id, config_key, config_value, config_type, config_group, config_desc, is_public, sort_order, create_by, create_time, update_by, update_time, is_deleted) FROM stdin;
1	SYS_NAME	嘉峪关后勤管理系统	STRING	BASIC	系统名称	t	0	\N	2026-08-19 16:56:45.886401+08	\N	2026-08-19 16:56:45.886401+08	f
2	BORROW_MAX_DAYS	30	STRING	GC	单次最大借用天数	t	0	\N	2026-08-19 16:56:45.886401+08	\N	2026-08-19 16:56:45.886401+08	f
3	MEAL_PRICE	25.00	STRING	ST	自助餐单价（元）	t	0	\N	2026-08-19 16:56:45.886401+08	\N	2026-08-19 16:56:45.886401+08	f
4	VEHICLE_FUEL_LIMIT	500	STRING	CL	单车月加油上限（升）	t	0	\N	2026-08-19 16:56:45.886401+08	\N	2026-08-19 16:56:45.886401+08	f
5	AUTO_APPROVE_AMOUNT	5000	STRING	ST	采购自动审批金额上限	t	0	\N	2026-08-19 16:56:45.886401+08	\N	2026-08-19 16:56:45.886401+08	f
\.


--
-- Data for Name: sys_message; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.sys_message (id, title, content, message_type, sender_id, receiver_id, is_read, read_time, biz_module, biz_order_no, create_by, create_time, update_by, update_time, is_deleted) FROM stdin;
10	您的用车申请已审核通过	单号：CL20260823001	APPROVAL	6	1	t	2026-08-23 18:17:47.046993+08	cl-apply	CL20260823001	6	2026-08-23 14:17:47.046993+08	\N	2026-08-23 19:17:47.046993+08	f
12	欢迎使用消息中心	本消息用于验证消息中心功能，包含未读/已读、各类型消息的展示。	SYSTEM	0	1	t	2026-08-21 19:17:47.046993+08	\N	\N	0	2026-08-20 19:17:47.046993+08	\N	2026-08-23 19:17:47.046993+08	f
11	您有新的公寓入住申请待审批	单号：10	BUSINESS	6	1	t	2026-08-23 20:05:39.586093+08	gy-occupant	10	6	2026-08-23 18:47:47.046993+08	\N	2026-08-23 19:17:47.046993+08	f
9	食堂物资库存预警	部分食堂物资库存已低于安全库存线，请及时补货。	WARNING	6	1	t	2026-08-23 20:39:11.325616+08	st-inventory	WARN-0001	6	2026-08-23 17:47:47.046993+08	\N	2026-08-23 19:17:47.046993+08	f
8	您有新的资产借用申请待审批	单号：GLZ-2026-0001	BUSINESS	6	1	t	2026-08-23 20:39:12.405449+08	gc-borrow	GLZ-2026-0001	6	2026-08-23 17:17:47.046993+08	\N	2026-08-23 19:17:47.046993+08	f
7	系统升级通知	系统将于本周末凌晨进行维护升级，期间服务可能短暂不可用，请提前保存数据。	SYSTEM	0	1	t	2026-08-23 20:39:12.894714+08	\N	\N	0	2026-08-23 16:17:47.046993+08	\N	2026-08-23 19:17:47.046993+08	f
\.


--
-- Data for Name: sys_operation_log; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.sys_operation_log (id, user_id, username, real_name, module, operation_type, operation_desc, request_url, request_method, request_params, response_code, response_msg, cost_time, client_ip, user_agent, exception_msg, create_time) FROM stdin;
1	1	zhangwei	张伟	SYS	LOGIN	用户登录系统	/api/login	\N	\N	\N	\N	120	192.168.1.100	\N	\N	2026-08-18 16:56:45.886401+08
2	2	lina	李娜	GC	ADD	新增资产卡片	/api/gc/asset/add	\N	\N	\N	\N	85	192.168.1.101	\N	\N	2026-08-17 16:56:45.886401+08
3	3	wangqiang	王强	CL	QUERY	查询车辆调度	/api/cl/dispatch/list	\N	\N	\N	\N	45	192.168.1.102	\N	\N	2026-08-16 16:56:45.886401+08
4	4	zhaolei	赵磊	CL	UPDATE	更新派单状态为完成	/api/cl/dispatch/finish	\N	\N	\N	\N	200	192.168.1.103	\N	\N	2026-08-15 16:56:45.886401+08
5	5	chenjing	陈静	ST	EXPORT	导出进销存报表	/api/st/inventory/export	\N	\N	\N	\N	350	192.168.1.104	\N	\N	2026-08-14 16:56:45.886401+08
6	2	lina	测试用户2	CL	QUERY	查询列表 #1	/api/cl/operation/1	POST	{"id":1}	200	成功	137	192.168.1.2	Mozilla/5.0 (Windows NT 10.0)	\N	2026-08-23 14:22:04.24274+08
7	3	wangqiang	测试用户3	GY	ADD	新增记录 #2	/api/gy/operation/2	POST	{"id":2}	200	成功	274	192.168.1.3	Mozilla/5.0 (Windows NT 10.0)	\N	2026-08-23 09:22:04.24274+08
8	4	zhaolei	测试用户4	ST	UPDATE	编辑更新 #3	/api/st/operation/3	POST	{"id":3}	200	成功	411	192.168.1.4	Mozilla/5.0 (Windows NT 10.0)	\N	2026-08-23 04:22:04.24274+08
9	5	chenjing	测试用户5	SYS	DELETE	删除记录 #4	/api/sys/operation/4	POST	{"id":4}	200	成功	548	192.168.1.5	Mozilla/5.0 (Windows NT 10.0)	\N	2026-08-22 23:22:04.24274+08
10	6	admin	测试用户6	PAY	APPROVE	审批通过 #5	/api/pay/operation/5	POST	{"id":5}	200	成功	685	192.168.1.6	Mozilla/5.0 (Windows NT 10.0)	\N	2026-08-22 18:22:04.24274+08
11	1	zhangwei	测试用户1	GC	EXPORT	用户登录 #6	/api/gc/operation/6	POST	{"id":6}	200	成功	822	192.168.1.7	Mozilla/5.0 (Windows NT 10.0)	\N	2026-08-22 13:22:04.24274+08
12	2	lina	测试用户2	CL	LOGIN	查询列表 #7	/api/cl/operation/7	POST	{"id":7}	200	成功	959	192.168.1.8	Mozilla/5.0 (Windows NT 10.0)	\N	2026-08-22 08:22:04.24274+08
13	3	wangqiang	测试用户3	GY	QUERY	新增记录 #8	/api/gy/operation/8	POST	{"id":8}	200	成功	1096	192.168.1.9	Mozilla/5.0 (Windows NT 10.0)	\N	2026-08-22 03:22:04.24274+08
14	4	zhaolei	测试用户4	ST	ADD	编辑更新 #9	/api/st/operation/9	POST	{"id":9}	200	成功	1233	192.168.1.10	Mozilla/5.0 (Windows NT 10.0)	\N	2026-08-21 22:22:04.24274+08
15	5	chenjing	测试用户5	SYS	UPDATE	删除记录 #10	/api/sys/operation/10	POST	{"id":10}	200	成功	1370	192.168.1.11	Mozilla/5.0 (Windows NT 10.0)	\N	2026-08-21 17:22:04.24274+08
16	6	admin	测试用户6	PAY	DELETE	审批通过 #11	/api/pay/operation/11	POST	{"id":11}	200	成功	1507	192.168.1.12	Mozilla/5.0 (Windows NT 10.0)	\N	2026-08-21 12:22:04.24274+08
17	1	zhangwei	测试用户1	GC	APPROVE	用户登录 #12	/api/gc/operation/12	POST	{"id":12}	200	成功	1644	192.168.1.13	Mozilla/5.0 (Windows NT 10.0)	\N	2026-08-21 07:22:04.24274+08
18	2	lina	测试用户2	CL	EXPORT	查询列表 #13	/api/cl/operation/13	POST	{"id":13}	200	成功	1781	192.168.1.14	Mozilla/5.0 (Windows NT 10.0)	\N	2026-08-21 02:22:04.24274+08
19	3	wangqiang	测试用户3	GY	LOGIN	新增记录 #14	/api/gy/operation/14	POST	{"id":14}	200	成功	1918	192.168.1.15	Mozilla/5.0 (Windows NT 10.0)	\N	2026-08-20 21:22:04.24274+08
20	4	zhaolei	测试用户4	ST	QUERY	编辑更新 #15	/api/st/operation/15	POST	{"id":15}	200	成功	55	192.168.1.16	Mozilla/5.0 (Windows NT 10.0)	\N	2026-08-20 16:22:04.24274+08
21	5	chenjing	测试用户5	SYS	ADD	删除记录 #16	/api/sys/operation/16	POST	{"id":16}	200	成功	192	192.168.1.17	Mozilla/5.0 (Windows NT 10.0)	\N	2026-08-20 11:22:04.24274+08
22	6	admin	测试用户6	PAY	UPDATE	审批通过 #17	/api/pay/operation/17	POST	{"id":17}	200	成功	329	192.168.1.18	Mozilla/5.0 (Windows NT 10.0)	\N	2026-08-20 06:22:04.24274+08
23	1	zhangwei	测试用户1	GC	DELETE	用户登录 #18	/api/gc/operation/18	POST	{"id":18}	200	成功	466	192.168.1.19	Mozilla/5.0 (Windows NT 10.0)	\N	2026-08-20 01:22:04.24274+08
24	2	lina	测试用户2	CL	APPROVE	查询列表 #19	/api/cl/operation/19	POST	{"id":19}	200	成功	603	192.168.1.20	Mozilla/5.0 (Windows NT 10.0)	\N	2026-08-19 20:22:04.24274+08
25	3	wangqiang	测试用户3	GY	EXPORT	新增记录 #20	/api/gy/operation/20	POST	{"id":20}	200	成功	740	192.168.1.21	Mozilla/5.0 (Windows NT 10.0)	\N	2026-08-19 15:22:04.24274+08
26	\N	anonymous	\N	SYS	LOGIN	用户登录	/api/auth/login	POST	[{"username":"zhangwei","password":"123456"}]	200	\N	241	0:0:0:0:0:0:0:1	\N	\N	2026-08-23 20:15:54.250505+08
27	\N	anonymous	\N	SYS	LOGIN	用户登录	/api/auth/login	POST	[{"username":"zhangwei","password":"123456"}]	200	\N	75	0:0:0:0:0:0:0:1	\N	\N	2026-08-23 20:16:15.257896+08
28	1	zhangwei	张伟	SYS	ADD	发送消息	/api/message/send	POST	[{"receiverIds":[1],"title":"AOP????","content":"????????????","messageType":"SYSTEM","bizModule":null,"bizOrderNo":null}]	200	\N	24	0:0:0:0:0:0:0:1	\N	\N	2026-08-23 20:16:15.370097+08
29	\N	anonymous	\N	SYS	LOGIN	用户登录	/api/auth/login	POST	[{"username":"zhangwei","password":"***"}]	200	\N	235	127.0.0.1	\N	\N	2026-08-23 20:38:07.080741+08
30	\N	anonymous	\N	SYS	LOGIN	用户登录	/api/auth/login	POST	[{"username":"zhangwei","password":"***"}]	200	\N	117	0:0:0:0:0:0:0:1	\N	\N	2026-08-24 08:50:29.190969+08
31	\N	anonymous	\N	SYS	LOGIN	用户登录	/api/auth/login	POST	[{"username":"lina","password":"***"}]	200	\N	86	0:0:0:0:0:0:0:1	\N	\N	2026-08-24 08:50:29.337726+08
32	\N	anonymous	\N	SYS	LOGIN	用户登录	/api/auth/login	POST	[{"username":"wangqiang","password":"***"}]	200	\N	82	0:0:0:0:0:0:0:1	\N	\N	2026-08-24 08:50:29.424868+08
33	\N	anonymous	\N	SYS	LOGIN	用户登录	/api/auth/login	POST	[{"username":"zhaolei","password":"***"}]	200	\N	83	0:0:0:0:0:0:0:1	\N	\N	2026-08-24 08:50:29.510559+08
34	\N	anonymous	\N	SYS	LOGIN	用户登录	/api/auth/login	POST	[{"username":"chenjing","password":"***"}]	200	\N	82	0:0:0:0:0:0:0:1	\N	\N	2026-08-24 08:50:29.595829+08
35	\N	anonymous	\N	SYS	LOGIN	用户登录	/api/auth/login	POST	[{"username":"admin","password":"***"}]	200	\N	79	0:0:0:0:0:0:0:1	\N	\N	2026-08-24 08:50:29.677523+08
36	\N	anonymous	\N	SYS	LOGIN	用户登录	/api/auth/login	POST	[{"username":"admin","password":"***"}]	200	\N	75	0:0:0:0:0:0:0:1	\N	\N	2026-08-24 08:54:26.629571+08
37	\N	anonymous	\N	SYS	LOGIN	用户登录	/api/auth/login	POST	[{"username":"admin","password":"***"}]	200	\N	83	0:0:0:0:0:0:0:1	\N	\N	2026-08-24 08:54:56.027382+08
38	\N	anonymous	\N	SYS	LOGIN	用户登录	/api/auth/login	POST	[{"username":"admin","password":"***"}]	200	\N	70	0:0:0:0:0:0:0:1	\N	\N	2026-08-24 09:17:02.756414+08
39	\N	anonymous	\N	SYS	LOGIN	用户登录	/api/auth/login	POST	[{"username":"admin","password":"***"}]	200	\N	73	0:0:0:0:0:0:0:1	\N	\N	2026-08-24 09:17:36.150121+08
40	\N	anonymous	\N	SYS	LOGIN	用户登录	/api/auth/login	POST	[{"username":"admin","password":"***"}]	200	\N	75	0:0:0:0:0:0:0:1	\N	\N	2026-08-24 09:22:34.994359+08
41	\N	anonymous	\N	SYS	LOGIN	用户登录	/api/auth/login	POST	[{"username":"zhangwei","password":"***"}]	200	\N	76	0:0:0:0:0:0:0:1	\N	\N	2026-08-24 09:26:04.542956+08
42	\N	anonymous	\N	SYS	LOGIN	用户登录	/api/auth/login	POST	[{"username":"admin","password":"***"}]	200	\N	75	0:0:0:0:0:0:0:1	\N	\N	2026-08-24 09:31:52.794829+08
43	\N	anonymous	\N	SYS	LOGIN	用户登录	/api/auth/login	POST	[{"username":"zhangwei","password":"***"}]	200	\N	68	0:0:0:0:0:0:0:1	\N	\N	2026-08-24 09:33:21.320129+08
44	\N	anonymous	\N	SYS	LOGIN	用户登录	/api/auth/login	POST	[{"username":"zhangwei","password":"***"}]	200	\N	75	0:0:0:0:0:0:0:1	\N	\N	2026-08-24 09:34:05.353653+08
45	\N	anonymous	\N	SYS	LOGIN	用户登录	/api/auth/login	POST	[{"username":"zhangwei","password":"***"}]	200	\N	188	127.0.0.1	\N	\N	2026-08-24 09:36:43.881997+08
46	\N	anonymous	\N	SYS	LOGIN	用户登录	/api/auth/login	POST	[{"username":"zhaolei","password":"***"}]	200	\N	80	0:0:0:0:0:0:0:1	\N	\N	2026-08-24 10:13:50.508207+08
47	\N	anonymous	\N	SYS	LOGIN	用户登录	/api/auth/login	POST	[{"username":"lina","password":"***"}]	200	\N	78	0:0:0:0:0:0:0:1	\N	\N	2026-08-24 10:14:21.581349+08
48	\N	anonymous	\N	SYS	LOGIN	用户登录	/api/auth/login	POST	[{"username":"chenjin","password":"***"}]	500	\N	0	0:0:0:0:0:0:0:1	\N	用户名或密码错误	2026-08-24 10:14:38.551572+08
49	\N	anonymous	\N	SYS	LOGIN	用户登录	/api/auth/login	POST	[{"username":"chenjing","password":"***"}]	200	\N	78	0:0:0:0:0:0:0:1	\N	\N	2026-08-24 10:14:39.983862+08
50	\N	anonymous	\N	SYS	LOGIN	用户登录	/api/auth/login	POST	[{"username":"wangqiang","password":"***"}]	200	\N	78	0:0:0:0:0:0:0:1	\N	\N	2026-08-24 10:15:25.104071+08
\.


--
-- Data for Name: sys_role; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.sys_role (id, role_code, role_name, role_desc, role_type, is_system, sort_order, create_by, create_time, update_by, update_time, is_deleted) FROM stdin;
1	ADMIN	系统管理员	拥有全部系统权限	\N	t	0	\N	2026-08-19 16:56:45.886401+08	\N	2026-08-19 16:56:45.886401+08	f
2	DEPT_MANAGER	部门管理员	管理本部门用户和业务	\N	f	0	\N	2026-08-19 16:56:45.886401+08	\N	2026-08-19 16:56:45.886401+08	f
3	USER	普通用户	基础业务操作	\N	f	0	\N	2026-08-19 16:56:45.886401+08	\N	2026-08-19 16:56:45.886401+08	f
4	DRIVER	司机	车辆驾驶和行程上报	\N	f	0	\N	2026-08-19 16:56:45.886401+08	\N	2026-08-19 16:56:45.886401+08	f
5	WAREHOUSE	仓库管理员	公物仓出入库管理	\N	f	0	\N	2026-08-19 16:56:45.886401+08	\N	2026-08-19 16:56:45.886401+08	f
6	CLEANER	保洁员	公寓保洁服务人员	\N	f	6	\N	2026-08-20 22:51:50.179669+08	\N	2026-08-20 22:51:50.179669+08	f
7	BIZ_ADMIN	业务管理员	业务审批与管理	\N	f	0	\N	2026-08-23 15:16:16.803406+08	\N	2026-08-23 15:16:16.803406+08	f
8	DIRECTOR	领导	单位领导审批	\N	f	0	\N	2026-08-23 15:16:16.820063+08	\N	2026-08-23 15:16:16.820063+08	f
\.


--
-- Data for Name: sys_unit; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.sys_unit (id, unit_code, unit_name, unit_type, parent_id, contact_person, contact_phone, sort_order, remark, create_by, create_time, update_by, update_time, is_deleted) FROM stdin;
1	DEPT_001	嘉峪关市机关事务管理局	DEPT	\N	张建国	0937-1111111	0	\N	\N	2026-08-19 16:56:45.886401+08	\N	2026-08-19 16:56:45.886401+08	f
2	DEPT_002	嘉峪关市财政局	DEPT	\N	李丽华	0937-2222222	0	\N	\N	2026-08-19 16:56:45.886401+08	\N	2026-08-19 16:56:45.886401+08	f
3	DEPT_003	嘉峪关市工业和信息化局	DEPT	\N	王强	0937-3333333	0	\N	\N	2026-08-19 16:56:45.886401+08	\N	2026-08-19 16:56:45.886401+08	f
4	DEPT_004	嘉峪关市人力资源和社会保障局	DEPT	\N	赵敏	0937-4444444	0	\N	\N	2026-08-19 16:56:45.886401+08	\N	2026-08-19 16:56:45.886401+08	f
5	DEPT_005	嘉峪关市住房和城乡建设局	DEPT	\N	陈磊	0937-5555555	0	\N	\N	2026-08-19 16:56:45.886401+08	\N	2026-08-19 16:56:45.886401+08	f
\.


--
-- Data for Name: sys_user; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.sys_user (id, username, password, real_name, phone, email, face_id, face_image_url, unit_id, unit_name, dept_id, "position", user_type, user_status, ext_json, create_by, create_time, update_by, update_time, is_deleted) FROM stdin;
1	zhangwei	$2a$10$FS4zjm1CYOEz4QZ29XzPL.6Uwc/wr2nw20rNMZPlRu2HkHz5U21ty	张伟	13800000001	zhangwei@jiayuguan.gov.cn	\N	\N	1	\N	\N	\N	ADMIN	ACTIVE	\N	\N	2026-08-19 16:56:45.886401+08	\N	2026-08-23 15:12:50.522996+08	f
2	lina	$2a$10$FS4zjm1CYOEz4QZ29XzPL.6Uwc/wr2nw20rNMZPlRu2HkHz5U21ty	李娜	13800000002	lina@jiayuguan.gov.cn	\N	\N	2	\N	\N	\N	DEPT_MANAGER	ACTIVE	\N	\N	2026-08-19 16:56:45.886401+08	\N	2026-08-23 15:12:50.522996+08	f
3	wangqiang	$2a$10$FS4zjm1CYOEz4QZ29XzPL.6Uwc/wr2nw20rNMZPlRu2HkHz5U21ty	王强	13800000003	wangqiang@jiayuguan.gov.cn	\N	\N	3	\N	\N	\N	USER	ACTIVE	\N	\N	2026-08-19 16:56:45.886401+08	\N	2026-08-23 15:12:50.522996+08	f
4	zhaolei	$2a$10$FS4zjm1CYOEz4QZ29XzPL.6Uwc/wr2nw20rNMZPlRu2HkHz5U21ty	赵磊	13800000004	zhaolei@jiayuguan.gov.cn	\N	\N	4	\N	\N	\N	DRIVER	ACTIVE	\N	\N	2026-08-19 16:56:45.886401+08	\N	2026-08-23 15:12:50.522996+08	f
5	chenjing	$2a$10$FS4zjm1CYOEz4QZ29XzPL.6Uwc/wr2nw20rNMZPlRu2HkHz5U21ty	陈静	13800000005	chenjing@jiayuguan.gov.cn	\N	\N	5	\N	\N	\N	WAREHOUSE	ACTIVE	\N	\N	2026-08-19 16:56:45.886401+08	\N	2026-08-23 15:12:50.522996+08	f
6	admin	$2a$10$OxGZrpRL1LpYsaqxOKH3JOv1aC1fUBA4Iw0fJdY9HQX56H9DZGgTO	系统管理员	\N	\N	\N	\N	1	嘉峪关市机关事务管理局	\N	\N	ADMIN	ACTIVE	\N	\N	2026-08-23 16:30:10.757608+08	\N	2026-08-23 16:30:10.757608+08	f
\.


--
-- Data for Name: sys_user_role; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.sys_user_role (id, user_id, role_id, create_by, create_time) FROM stdin;
1	1	1	\N	2026-08-19 16:56:45.886401+08
2	2	2	\N	2026-08-19 16:56:45.886401+08
3	3	3	\N	2026-08-19 16:56:45.886401+08
4	4	4	\N	2026-08-19 16:56:45.886401+08
5	5	5	\N	2026-08-19 16:56:45.886401+08
6	2	6	\N	2026-08-20 22:51:50.184747+08
7	3	6	\N	2026-08-20 22:51:50.184747+08
8	1	7	1	2026-08-23 15:16:16.820448+08
9	1	8	1	2026-08-23 15:16:16.820448+08
10	6	1	1	2026-08-23 16:30:10.803909+08
\.


--
-- Name: cl_apply_order_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.cl_apply_order_id_seq', 9, true);


--
-- Name: cl_cost_detail_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.cl_cost_detail_id_seq', 16, true);


--
-- Name: cl_dispatch_order_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.cl_dispatch_order_id_seq', 8, true);


--
-- Name: cl_repair_order_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.cl_repair_order_id_seq', 8, true);


--
-- Name: cl_track_point_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.cl_track_point_id_seq', 132, true);


--
-- Name: cl_vehicle_archive_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.cl_vehicle_archive_id_seq', 7, true);


--
-- Name: gc_asset_card_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.gc_asset_card_id_seq', 7, true);


--
-- Name: gc_borrow_detail_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.gc_borrow_detail_id_seq', 11, true);


--
-- Name: gc_borrow_order_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.gc_borrow_order_id_seq', 10, true);


--
-- Name: gc_logistics_order_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.gc_logistics_order_id_seq', 6, false);


--
-- Name: gc_return_order_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.gc_return_order_id_seq', 11, true);


--
-- Name: gc_transfer_detail_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.gc_transfer_detail_id_seq', 5, true);


--
-- Name: gc_transfer_order_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.gc_transfer_order_id_seq', 11, true);


--
-- Name: gy_cleaning_order_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.gy_cleaning_order_id_seq', 7, true);


--
-- Name: gy_occupant_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.gy_occupant_id_seq', 13, true);


--
-- Name: gy_repair_order_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.gy_repair_order_id_seq', 8, true);


--
-- Name: gy_room_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.gy_room_id_seq', 7, true);


--
-- Name: pay_account_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.pay_account_id_seq', 7, true);


--
-- Name: pay_transaction_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.pay_transaction_id_seq', 11, true);


--
-- Name: st_inventory_record_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.st_inventory_record_id_seq', 13, true);


--
-- Name: st_kitchen_waste_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.st_kitchen_waste_id_seq', 3, true);


--
-- Name: st_material_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.st_material_id_seq', 9, true);


--
-- Name: st_meal_reservation_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.st_meal_reservation_id_seq', 9, true);


--
-- Name: st_purchase_detail_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.st_purchase_detail_id_seq', 11, true);


--
-- Name: st_purchase_order_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.st_purchase_order_id_seq', 8, true);


--
-- Name: sys_config_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.sys_config_id_seq', 6, false);


--
-- Name: sys_message_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.sys_message_id_seq', 13, true);


--
-- Name: sys_operation_log_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.sys_operation_log_id_seq', 50, true);


--
-- Name: sys_role_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.sys_role_id_seq', 8, true);


--
-- Name: sys_unit_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.sys_unit_id_seq', 6, false);


--
-- Name: sys_user_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.sys_user_id_seq', 6, true);


--
-- Name: sys_user_role_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.sys_user_role_id_seq', 10, true);


--
-- Name: cl_apply_order cl_apply_order_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.cl_apply_order
    ADD CONSTRAINT cl_apply_order_pkey PRIMARY KEY (id);


--
-- Name: cl_cost_detail cl_cost_detail_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.cl_cost_detail
    ADD CONSTRAINT cl_cost_detail_pkey PRIMARY KEY (id);


--
-- Name: cl_dispatch_order cl_dispatch_order_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.cl_dispatch_order
    ADD CONSTRAINT cl_dispatch_order_pkey PRIMARY KEY (id);


--
-- Name: cl_repair_order cl_repair_order_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.cl_repair_order
    ADD CONSTRAINT cl_repair_order_pkey PRIMARY KEY (id);


--
-- Name: cl_track_point cl_track_point_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.cl_track_point
    ADD CONSTRAINT cl_track_point_pkey PRIMARY KEY (id);


--
-- Name: cl_vehicle_archive cl_vehicle_archive_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.cl_vehicle_archive
    ADD CONSTRAINT cl_vehicle_archive_pkey PRIMARY KEY (id);


--
-- Name: gc_asset_card gc_asset_card_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.gc_asset_card
    ADD CONSTRAINT gc_asset_card_pkey PRIMARY KEY (id);


--
-- Name: gc_borrow_detail gc_borrow_detail_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.gc_borrow_detail
    ADD CONSTRAINT gc_borrow_detail_pkey PRIMARY KEY (id);


--
-- Name: gc_borrow_order gc_borrow_order_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.gc_borrow_order
    ADD CONSTRAINT gc_borrow_order_pkey PRIMARY KEY (id);


--
-- Name: gc_logistics_order gc_logistics_order_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.gc_logistics_order
    ADD CONSTRAINT gc_logistics_order_pkey PRIMARY KEY (id);


--
-- Name: gc_return_order gc_return_order_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.gc_return_order
    ADD CONSTRAINT gc_return_order_pkey PRIMARY KEY (id);


--
-- Name: gc_transfer_detail gc_transfer_detail_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.gc_transfer_detail
    ADD CONSTRAINT gc_transfer_detail_pkey PRIMARY KEY (id);


--
-- Name: gc_transfer_order gc_transfer_order_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.gc_transfer_order
    ADD CONSTRAINT gc_transfer_order_pkey PRIMARY KEY (id);


--
-- Name: gy_cleaning_order gy_cleaning_order_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.gy_cleaning_order
    ADD CONSTRAINT gy_cleaning_order_pkey PRIMARY KEY (id);


--
-- Name: gy_occupant gy_occupant_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.gy_occupant
    ADD CONSTRAINT gy_occupant_pkey PRIMARY KEY (id);


--
-- Name: gy_repair_order gy_repair_order_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.gy_repair_order
    ADD CONSTRAINT gy_repair_order_pkey PRIMARY KEY (id);


--
-- Name: gy_room gy_room_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.gy_room
    ADD CONSTRAINT gy_room_pkey PRIMARY KEY (id);


--
-- Name: pay_account pay_account_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.pay_account
    ADD CONSTRAINT pay_account_pkey PRIMARY KEY (id);


--
-- Name: pay_transaction pay_transaction_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.pay_transaction
    ADD CONSTRAINT pay_transaction_pkey PRIMARY KEY (id);


--
-- Name: st_inventory_record st_inventory_record_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.st_inventory_record
    ADD CONSTRAINT st_inventory_record_pkey PRIMARY KEY (id);


--
-- Name: st_kitchen_waste st_kitchen_waste_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.st_kitchen_waste
    ADD CONSTRAINT st_kitchen_waste_pkey PRIMARY KEY (id);


--
-- Name: st_material st_material_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.st_material
    ADD CONSTRAINT st_material_pkey PRIMARY KEY (id);


--
-- Name: st_meal_reservation st_meal_reservation_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.st_meal_reservation
    ADD CONSTRAINT st_meal_reservation_pkey PRIMARY KEY (id);


--
-- Name: st_purchase_detail st_purchase_detail_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.st_purchase_detail
    ADD CONSTRAINT st_purchase_detail_pkey PRIMARY KEY (id);


--
-- Name: st_purchase_order st_purchase_order_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.st_purchase_order
    ADD CONSTRAINT st_purchase_order_pkey PRIMARY KEY (id);


--
-- Name: sys_config sys_config_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.sys_config
    ADD CONSTRAINT sys_config_pkey PRIMARY KEY (id);


--
-- Name: sys_message sys_message_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.sys_message
    ADD CONSTRAINT sys_message_pkey PRIMARY KEY (id);


--
-- Name: sys_operation_log sys_operation_log_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.sys_operation_log
    ADD CONSTRAINT sys_operation_log_pkey PRIMARY KEY (id);


--
-- Name: sys_role sys_role_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.sys_role
    ADD CONSTRAINT sys_role_pkey PRIMARY KEY (id);


--
-- Name: sys_unit sys_unit_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.sys_unit
    ADD CONSTRAINT sys_unit_pkey PRIMARY KEY (id);


--
-- Name: sys_user sys_user_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.sys_user
    ADD CONSTRAINT sys_user_pkey PRIMARY KEY (id);


--
-- Name: sys_user_role sys_user_role_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.sys_user_role
    ADD CONSTRAINT sys_user_role_pkey PRIMARY KEY (id);


--
-- Name: idx_cl_apply_applicant; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_cl_apply_applicant ON public.cl_apply_order USING btree (applicant_id);


--
-- Name: idx_cl_apply_start_time; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_cl_apply_start_time ON public.cl_apply_order USING btree (start_time);


--
-- Name: idx_cl_apply_status; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_cl_apply_status ON public.cl_apply_order USING btree (apply_status);


--
-- Name: idx_cl_apply_unit; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_cl_apply_unit ON public.cl_apply_order USING btree (applicant_unit_id);


--
-- Name: idx_cl_cost_status; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_cl_cost_status ON public.cl_cost_detail USING btree (approval_status);


--
-- Name: idx_cl_cost_time; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_cl_cost_time ON public.cl_cost_detail USING btree (cost_time);


--
-- Name: idx_cl_cost_type; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_cl_cost_type ON public.cl_cost_detail USING btree (cost_type);


--
-- Name: idx_cl_cost_vehicle; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_cl_cost_vehicle ON public.cl_cost_detail USING btree (vehicle_id);


--
-- Name: idx_cl_dispatch_actual_start; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_cl_dispatch_actual_start ON public.cl_dispatch_order USING btree (actual_start);


--
-- Name: idx_cl_dispatch_apply; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_cl_dispatch_apply ON public.cl_dispatch_order USING btree (apply_id);


--
-- Name: idx_cl_dispatch_driver; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_cl_dispatch_driver ON public.cl_dispatch_order USING btree (driver_id);


--
-- Name: idx_cl_dispatch_status; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_cl_dispatch_status ON public.cl_dispatch_order USING btree (dispatch_status);


--
-- Name: idx_cl_dispatch_vehicle; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_cl_dispatch_vehicle ON public.cl_dispatch_order USING btree (vehicle_id);


--
-- Name: idx_cl_repair_status; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_cl_repair_status ON public.cl_repair_order USING btree (order_status);


--
-- Name: idx_cl_repair_type; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_cl_repair_type ON public.cl_repair_order USING btree (repair_type);


--
-- Name: idx_cl_repair_vehicle; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_cl_repair_vehicle ON public.cl_repair_order USING btree (vehicle_id);


--
-- Name: idx_cl_track_dispatch; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_cl_track_dispatch ON public.cl_track_point USING btree (dispatch_id);


--
-- Name: idx_cl_track_vehicle_time; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_cl_track_vehicle_time ON public.cl_track_point USING btree (vehicle_id, track_time DESC);


--
-- Name: idx_cl_vehicle_insurance_end; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_cl_vehicle_insurance_end ON public.cl_vehicle_archive USING btree (insurance_end);


--
-- Name: idx_cl_vehicle_next_maintenance; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_cl_vehicle_next_maintenance ON public.cl_vehicle_archive USING btree (next_maintenance_mileage);


--
-- Name: idx_cl_vehicle_status; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_cl_vehicle_status ON public.cl_vehicle_archive USING btree (vehicle_status);


--
-- Name: idx_cl_vehicle_type; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_cl_vehicle_type ON public.cl_vehicle_archive USING btree (vehicle_type);


--
-- Name: idx_cl_vehicle_unit; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_cl_vehicle_unit ON public.cl_vehicle_archive USING btree (unit_id);


--
-- Name: idx_gc_asset_category; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_gc_asset_category ON public.gc_asset_card USING btree (category_code);


--
-- Name: idx_gc_asset_create_time; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_gc_asset_create_time ON public.gc_asset_card USING btree (create_time);


--
-- Name: idx_gc_asset_current_use_unit; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_gc_asset_current_use_unit ON public.gc_asset_card USING btree (current_use_unit_id);


--
-- Name: idx_gc_asset_owner_unit; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_gc_asset_owner_unit ON public.gc_asset_card USING btree (owner_unit_id);


--
-- Name: idx_gc_asset_purchase_date; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_gc_asset_purchase_date ON public.gc_asset_card USING btree (purchase_date);


--
-- Name: idx_gc_asset_status; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_gc_asset_status ON public.gc_asset_card USING btree (asset_status);


--
-- Name: idx_gc_asset_warehouse; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_gc_asset_warehouse ON public.gc_asset_card USING btree (warehouse_id);


--
-- Name: idx_gc_borrow_applicant; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_gc_borrow_applicant ON public.gc_borrow_order USING btree (applicant_id);


--
-- Name: idx_gc_borrow_applicant_unit; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_gc_borrow_applicant_unit ON public.gc_borrow_order USING btree (applicant_unit_id);


--
-- Name: idx_gc_borrow_create_time; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_gc_borrow_create_time ON public.gc_borrow_order USING btree (create_time);


--
-- Name: idx_gc_borrow_detail_asset; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_gc_borrow_detail_asset ON public.gc_borrow_detail USING btree (asset_id);


--
-- Name: idx_gc_borrow_detail_order; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_gc_borrow_detail_order ON public.gc_borrow_detail USING btree (borrow_order_id);


--
-- Name: idx_gc_borrow_detail_status; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_gc_borrow_detail_status ON public.gc_borrow_detail USING btree (detail_status);


--
-- Name: idx_gc_borrow_end; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_gc_borrow_end ON public.gc_borrow_order USING btree (borrow_end);


--
-- Name: idx_gc_borrow_status; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_gc_borrow_status ON public.gc_borrow_order USING btree (order_status);


--
-- Name: idx_gc_logistics_business; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_gc_logistics_business ON public.gc_logistics_order USING btree (business_order_no);


--
-- Name: idx_gc_logistics_sign_time; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_gc_logistics_sign_time ON public.gc_logistics_order USING btree (sign_time);


--
-- Name: idx_gc_logistics_status; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_gc_logistics_status ON public.gc_logistics_order USING btree (logistics_status);


--
-- Name: idx_gc_return_borrow; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_gc_return_borrow ON public.gc_return_order USING btree (borrow_order_id);


--
-- Name: idx_gc_return_status; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_gc_return_status ON public.gc_return_order USING btree (return_status);


--
-- Name: idx_gc_transfer_applicant; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_gc_transfer_applicant ON public.gc_transfer_order USING btree (applicant_unit_id);


--
-- Name: idx_gc_transfer_status; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_gc_transfer_status ON public.gc_transfer_order USING btree (order_status);


--
-- Name: idx_gc_transfer_type; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_gc_transfer_type ON public.gc_transfer_order USING btree (transfer_type);


--
-- Name: idx_gy_cleaning_room; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_gy_cleaning_room ON public.gy_cleaning_order USING btree (room_id);


--
-- Name: idx_gy_cleaning_status; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_gy_cleaning_status ON public.gy_cleaning_order USING btree (order_status);


--
-- Name: idx_gy_cleaning_time; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_gy_cleaning_time ON public.gy_cleaning_order USING btree (cleaning_time);


--
-- Name: idx_gy_occupant_checkin; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_gy_occupant_checkin ON public.gy_occupant USING btree (checkin_time);


--
-- Name: idx_gy_occupant_expected_leave; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_gy_occupant_expected_leave ON public.gy_occupant USING btree (expected_leave_time);


--
-- Name: idx_gy_occupant_name; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_gy_occupant_name ON public.gy_occupant USING btree (occupant_name);


--
-- Name: idx_gy_occupant_room; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_gy_occupant_room ON public.gy_occupant USING btree (room_id);


--
-- Name: idx_gy_occupant_status; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_gy_occupant_status ON public.gy_occupant USING btree (occupant_status);


--
-- Name: idx_gy_occupant_unit; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_gy_occupant_unit ON public.gy_occupant USING btree (unit_id);


--
-- Name: idx_gy_repair_applicant; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_gy_repair_applicant ON public.gy_repair_order USING btree (applicant_id);


--
-- Name: idx_gy_repair_room; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_gy_repair_room ON public.gy_repair_order USING btree (room_id);


--
-- Name: idx_gy_repair_status; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_gy_repair_status ON public.gy_repair_order USING btree (order_status);


--
-- Name: idx_gy_room_occupant; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_gy_room_occupant ON public.gy_room USING btree (current_occupant_id);


--
-- Name: idx_gy_room_status; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_gy_room_status ON public.gy_room USING btree (room_status);


--
-- Name: idx_gy_room_type; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_gy_room_type ON public.gy_room USING btree (room_type);


--
-- Name: idx_pay_account_card; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_pay_account_card ON public.pay_account USING btree (card_no);


--
-- Name: idx_pay_account_user; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_pay_account_user ON public.pay_account USING btree (user_id);


--
-- Name: idx_pay_trans_account; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_pay_trans_account ON public.pay_transaction USING btree (account_no);


--
-- Name: idx_pay_trans_biz; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_pay_trans_biz ON public.pay_transaction USING btree (biz_order_no);


--
-- Name: idx_pay_trans_card; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_pay_trans_card ON public.pay_transaction USING btree (card_no);


--
-- Name: idx_pay_trans_pay_time; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_pay_trans_pay_time ON public.pay_transaction USING btree (pay_time);


--
-- Name: idx_pay_trans_status; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_pay_trans_status ON public.pay_transaction USING btree (pay_status);


--
-- Name: idx_pay_trans_user; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_pay_trans_user ON public.pay_transaction USING btree (user_id);


--
-- Name: idx_st_inventory_business; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_st_inventory_business ON public.st_inventory_record USING btree (business_order_no);


--
-- Name: idx_st_inventory_create_time; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_st_inventory_create_time ON public.st_inventory_record USING btree (create_time);


--
-- Name: idx_st_inventory_material; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_st_inventory_material ON public.st_inventory_record USING btree (material_id);


--
-- Name: idx_st_inventory_type; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_st_inventory_type ON public.st_inventory_record USING btree (record_type);


--
-- Name: idx_st_material_category; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_st_material_category ON public.st_material USING btree (category);


--
-- Name: idx_st_material_name; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_st_material_name ON public.st_material USING btree (material_name);


--
-- Name: idx_st_meal_date; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_st_meal_date ON public.st_meal_reservation USING btree (meal_date);


--
-- Name: idx_st_meal_type; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_st_meal_type ON public.st_meal_reservation USING btree (meal_type);


--
-- Name: idx_st_meal_unit; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_st_meal_unit ON public.st_meal_reservation USING btree (unit_id);


--
-- Name: idx_st_meal_user; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_st_meal_user ON public.st_meal_reservation USING btree (user_id);


--
-- Name: idx_st_purchase_create_time; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_st_purchase_create_time ON public.st_purchase_order USING btree (create_time);


--
-- Name: idx_st_purchase_detail_material; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_st_purchase_detail_material ON public.st_purchase_detail USING btree (material_id);


--
-- Name: idx_st_purchase_detail_order; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_st_purchase_detail_order ON public.st_purchase_detail USING btree (purchase_order_id);


--
-- Name: idx_st_purchase_effective_end; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_st_purchase_effective_end ON public.st_purchase_order USING btree (effective_end);


--
-- Name: idx_st_purchase_status; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_st_purchase_status ON public.st_purchase_order USING btree (order_status);


--
-- Name: idx_st_purchase_supplier; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_st_purchase_supplier ON public.st_purchase_order USING btree (supplier_id);


--
-- Name: idx_st_waste_date; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_st_waste_date ON public.st_kitchen_waste USING btree (record_date);


--
-- Name: idx_sys_config_group; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_sys_config_group ON public.sys_config USING btree (config_group);


--
-- Name: idx_sys_log_module; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_sys_log_module ON public.sys_operation_log USING btree (module);


--
-- Name: idx_sys_log_time; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_sys_log_time ON public.sys_operation_log USING btree (create_time DESC);


--
-- Name: idx_sys_log_type; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_sys_log_type ON public.sys_operation_log USING btree (operation_type);


--
-- Name: idx_sys_log_user; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_sys_log_user ON public.sys_operation_log USING btree (user_id);


--
-- Name: idx_sys_message_create_time; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_sys_message_create_time ON public.sys_message USING btree (create_time DESC);


--
-- Name: idx_sys_message_read; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_sys_message_read ON public.sys_message USING btree (is_read);


--
-- Name: idx_sys_message_receiver; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_sys_message_receiver ON public.sys_message USING btree (receiver_id);


--
-- Name: idx_sys_unit_parent; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_sys_unit_parent ON public.sys_unit USING btree (parent_id);


--
-- Name: idx_sys_user_role_user; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_sys_user_role_user ON public.sys_user_role USING btree (user_id);


--
-- Name: idx_sys_user_status; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_sys_user_status ON public.sys_user USING btree (user_status);


--
-- Name: idx_sys_user_type; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_sys_user_type ON public.sys_user USING btree (user_type);


--
-- Name: idx_sys_user_unit; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_sys_user_unit ON public.sys_user USING btree (unit_id);


--
-- Name: uk_cl_apply_no; Type: INDEX; Schema: public; Owner: -
--

CREATE UNIQUE INDEX uk_cl_apply_no ON public.cl_apply_order USING btree (apply_no) WHERE (is_deleted = false);


--
-- Name: uk_cl_dispatch_no; Type: INDEX; Schema: public; Owner: -
--

CREATE UNIQUE INDEX uk_cl_dispatch_no ON public.cl_dispatch_order USING btree (dispatch_no) WHERE (is_deleted = false);


--
-- Name: uk_cl_frame_no; Type: INDEX; Schema: public; Owner: -
--

CREATE UNIQUE INDEX uk_cl_frame_no ON public.cl_vehicle_archive USING btree (frame_no) WHERE ((frame_no IS NOT NULL) AND (is_deleted = false));


--
-- Name: uk_cl_plate_number; Type: INDEX; Schema: public; Owner: -
--

CREATE UNIQUE INDEX uk_cl_plate_number ON public.cl_vehicle_archive USING btree (plate_number) WHERE (is_deleted = false);


--
-- Name: uk_cl_repair_no; Type: INDEX; Schema: public; Owner: -
--

CREATE UNIQUE INDEX uk_cl_repair_no ON public.cl_repair_order USING btree (repair_no) WHERE (is_deleted = false);


--
-- Name: uk_gc_asset_code; Type: INDEX; Schema: public; Owner: -
--

CREATE UNIQUE INDEX uk_gc_asset_code ON public.gc_asset_card USING btree (asset_code) WHERE (is_deleted = false);


--
-- Name: uk_gc_asset_rfid; Type: INDEX; Schema: public; Owner: -
--

CREATE UNIQUE INDEX uk_gc_asset_rfid ON public.gc_asset_card USING btree (rfid_tag) WHERE ((rfid_tag IS NOT NULL) AND (is_deleted = false));


--
-- Name: uk_gc_borrow_no; Type: INDEX; Schema: public; Owner: -
--

CREATE UNIQUE INDEX uk_gc_borrow_no ON public.gc_borrow_order USING btree (order_no) WHERE (is_deleted = false);


--
-- Name: uk_gc_logistics_no; Type: INDEX; Schema: public; Owner: -
--

CREATE UNIQUE INDEX uk_gc_logistics_no ON public.gc_logistics_order USING btree (logistics_no) WHERE (is_deleted = false);


--
-- Name: uk_gc_return_no; Type: INDEX; Schema: public; Owner: -
--

CREATE UNIQUE INDEX uk_gc_return_no ON public.gc_return_order USING btree (return_no) WHERE (is_deleted = false);


--
-- Name: uk_gc_transfer_no; Type: INDEX; Schema: public; Owner: -
--

CREATE UNIQUE INDEX uk_gc_transfer_no ON public.gc_transfer_order USING btree (order_no) WHERE (is_deleted = false);


--
-- Name: uk_gy_building_room; Type: INDEX; Schema: public; Owner: -
--

CREATE UNIQUE INDEX uk_gy_building_room ON public.gy_room USING btree (building, room_no) WHERE (is_deleted = false);


--
-- Name: uk_gy_cleaning_no; Type: INDEX; Schema: public; Owner: -
--

CREATE UNIQUE INDEX uk_gy_cleaning_no ON public.gy_cleaning_order USING btree (cleaning_no) WHERE (is_deleted = false);


--
-- Name: uk_gy_repair_no; Type: INDEX; Schema: public; Owner: -
--

CREATE UNIQUE INDEX uk_gy_repair_no ON public.gy_repair_order USING btree (repair_no) WHERE (is_deleted = false);


--
-- Name: uk_pay_account_no; Type: INDEX; Schema: public; Owner: -
--

CREATE UNIQUE INDEX uk_pay_account_no ON public.pay_account USING btree (account_no) WHERE (is_deleted = false);


--
-- Name: uk_pay_trans_no; Type: INDEX; Schema: public; Owner: -
--

CREATE UNIQUE INDEX uk_pay_trans_no ON public.pay_transaction USING btree (transaction_no);


--
-- Name: uk_pay_user_type; Type: INDEX; Schema: public; Owner: -
--

CREATE UNIQUE INDEX uk_pay_user_type ON public.pay_account USING btree (user_id, account_type) WHERE (is_deleted = false);


--
-- Name: uk_st_material_code; Type: INDEX; Schema: public; Owner: -
--

CREATE UNIQUE INDEX uk_st_material_code ON public.st_material USING btree (material_code) WHERE (is_deleted = false);


--
-- Name: uk_st_purchase_no; Type: INDEX; Schema: public; Owner: -
--

CREATE UNIQUE INDEX uk_st_purchase_no ON public.st_purchase_order USING btree (order_no) WHERE (is_deleted = false);


--
-- Name: uk_sys_config_key; Type: INDEX; Schema: public; Owner: -
--

CREATE UNIQUE INDEX uk_sys_config_key ON public.sys_config USING btree (config_key) WHERE (is_deleted = false);


--
-- Name: uk_sys_phone; Type: INDEX; Schema: public; Owner: -
--

CREATE UNIQUE INDEX uk_sys_phone ON public.sys_user USING btree (phone) WHERE ((phone IS NOT NULL) AND (is_deleted = false));


--
-- Name: uk_sys_role_code; Type: INDEX; Schema: public; Owner: -
--

CREATE UNIQUE INDEX uk_sys_role_code ON public.sys_role USING btree (role_code) WHERE (is_deleted = false);


--
-- Name: uk_sys_unit_code; Type: INDEX; Schema: public; Owner: -
--

CREATE UNIQUE INDEX uk_sys_unit_code ON public.sys_unit USING btree (unit_code) WHERE (is_deleted = false);


--
-- Name: uk_sys_user_role; Type: INDEX; Schema: public; Owner: -
--

CREATE UNIQUE INDEX uk_sys_user_role ON public.sys_user_role USING btree (user_id, role_id);


--
-- Name: uk_sys_username; Type: INDEX; Schema: public; Owner: -
--

CREATE UNIQUE INDEX uk_sys_username ON public.sys_user USING btree (username) WHERE (is_deleted = false);


--
-- Name: cl_apply_order trg_cl_apply_order_update_time; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER trg_cl_apply_order_update_time BEFORE UPDATE ON public.cl_apply_order FOR EACH ROW EXECUTE FUNCTION public.update_modified_column();


--
-- Name: cl_cost_detail trg_cl_cost_detail_update_time; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER trg_cl_cost_detail_update_time BEFORE UPDATE ON public.cl_cost_detail FOR EACH ROW EXECUTE FUNCTION public.update_modified_column();


--
-- Name: cl_dispatch_order trg_cl_dispatch_order_update_time; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER trg_cl_dispatch_order_update_time BEFORE UPDATE ON public.cl_dispatch_order FOR EACH ROW EXECUTE FUNCTION public.update_modified_column();


--
-- Name: cl_repair_order trg_cl_repair_order_update_time; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER trg_cl_repair_order_update_time BEFORE UPDATE ON public.cl_repair_order FOR EACH ROW EXECUTE FUNCTION public.update_modified_column();


--
-- Name: cl_vehicle_archive trg_cl_vehicle_archive_update_time; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER trg_cl_vehicle_archive_update_time BEFORE UPDATE ON public.cl_vehicle_archive FOR EACH ROW EXECUTE FUNCTION public.update_modified_column();


--
-- Name: gc_asset_card trg_gc_asset_card_update_time; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER trg_gc_asset_card_update_time BEFORE UPDATE ON public.gc_asset_card FOR EACH ROW EXECUTE FUNCTION public.update_modified_column();


--
-- Name: gc_borrow_detail trg_gc_borrow_detail_update_time; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER trg_gc_borrow_detail_update_time BEFORE UPDATE ON public.gc_borrow_detail FOR EACH ROW EXECUTE FUNCTION public.update_modified_column();


--
-- Name: gc_borrow_order trg_gc_borrow_order_update_time; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER trg_gc_borrow_order_update_time BEFORE UPDATE ON public.gc_borrow_order FOR EACH ROW EXECUTE FUNCTION public.update_modified_column();


--
-- Name: gc_logistics_order trg_gc_logistics_order_update_time; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER trg_gc_logistics_order_update_time BEFORE UPDATE ON public.gc_logistics_order FOR EACH ROW EXECUTE FUNCTION public.update_modified_column();


--
-- Name: gc_return_order trg_gc_return_order_update_time; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER trg_gc_return_order_update_time BEFORE UPDATE ON public.gc_return_order FOR EACH ROW EXECUTE FUNCTION public.update_modified_column();


--
-- Name: gc_transfer_order trg_gc_transfer_order_update_time; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER trg_gc_transfer_order_update_time BEFORE UPDATE ON public.gc_transfer_order FOR EACH ROW EXECUTE FUNCTION public.update_modified_column();


--
-- Name: gy_cleaning_order trg_gy_cleaning_order_update_time; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER trg_gy_cleaning_order_update_time BEFORE UPDATE ON public.gy_cleaning_order FOR EACH ROW EXECUTE FUNCTION public.update_modified_column();


--
-- Name: gy_occupant trg_gy_occupant_update_time; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER trg_gy_occupant_update_time BEFORE UPDATE ON public.gy_occupant FOR EACH ROW EXECUTE FUNCTION public.update_modified_column();


--
-- Name: gy_repair_order trg_gy_repair_order_update_time; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER trg_gy_repair_order_update_time BEFORE UPDATE ON public.gy_repair_order FOR EACH ROW EXECUTE FUNCTION public.update_modified_column();


--
-- Name: gy_room trg_gy_room_update_time; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER trg_gy_room_update_time BEFORE UPDATE ON public.gy_room FOR EACH ROW EXECUTE FUNCTION public.update_modified_column();


--
-- Name: pay_account trg_pay_account_update_time; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER trg_pay_account_update_time BEFORE UPDATE ON public.pay_account FOR EACH ROW EXECUTE FUNCTION public.update_modified_column();


--
-- Name: st_material trg_st_material_update_time; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER trg_st_material_update_time BEFORE UPDATE ON public.st_material FOR EACH ROW EXECUTE FUNCTION public.update_modified_column();


--
-- Name: st_meal_reservation trg_st_meal_reservation_update_time; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER trg_st_meal_reservation_update_time BEFORE UPDATE ON public.st_meal_reservation FOR EACH ROW EXECUTE FUNCTION public.update_modified_column();


--
-- Name: st_purchase_detail trg_st_purchase_detail_update_time; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER trg_st_purchase_detail_update_time BEFORE UPDATE ON public.st_purchase_detail FOR EACH ROW EXECUTE FUNCTION public.update_modified_column();


--
-- Name: st_purchase_order trg_st_purchase_order_update_time; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER trg_st_purchase_order_update_time BEFORE UPDATE ON public.st_purchase_order FOR EACH ROW EXECUTE FUNCTION public.update_modified_column();


--
-- Name: sys_config trg_sys_config_update_time; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER trg_sys_config_update_time BEFORE UPDATE ON public.sys_config FOR EACH ROW EXECUTE FUNCTION public.update_modified_column();


--
-- Name: sys_role trg_sys_role_update_time; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER trg_sys_role_update_time BEFORE UPDATE ON public.sys_role FOR EACH ROW EXECUTE FUNCTION public.update_modified_column();


--
-- Name: sys_unit trg_sys_unit_update_time; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER trg_sys_unit_update_time BEFORE UPDATE ON public.sys_unit FOR EACH ROW EXECUTE FUNCTION public.update_modified_column();


--
-- Name: sys_user trg_sys_user_update_time; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER trg_sys_user_update_time BEFORE UPDATE ON public.sys_user FOR EACH ROW EXECUTE FUNCTION public.update_modified_column();


--
-- Name: cl_track_point cl_track_point_dispatch_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.cl_track_point
    ADD CONSTRAINT cl_track_point_dispatch_id_fkey FOREIGN KEY (dispatch_id) REFERENCES public.cl_dispatch_order(id) ON DELETE CASCADE;


--
-- Name: cl_track_point cl_track_point_vehicle_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.cl_track_point
    ADD CONSTRAINT cl_track_point_vehicle_id_fkey FOREIGN KEY (vehicle_id) REFERENCES public.cl_vehicle_archive(id);


--
-- Name: gc_borrow_detail fk_borrow_detail_asset; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.gc_borrow_detail
    ADD CONSTRAINT fk_borrow_detail_asset FOREIGN KEY (asset_id) REFERENCES public.gc_asset_card(id);


--
-- Name: gc_borrow_detail fk_borrow_detail_order; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.gc_borrow_detail
    ADD CONSTRAINT fk_borrow_detail_order FOREIGN KEY (borrow_order_id) REFERENCES public.gc_borrow_order(id) ON DELETE CASCADE;


--
-- Name: gy_cleaning_order fk_cleaning_room; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.gy_cleaning_order
    ADD CONSTRAINT fk_cleaning_room FOREIGN KEY (room_id) REFERENCES public.gy_room(id);


--
-- Name: cl_cost_detail fk_cost_vehicle; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.cl_cost_detail
    ADD CONSTRAINT fk_cost_vehicle FOREIGN KEY (vehicle_id) REFERENCES public.cl_vehicle_archive(id);


--
-- Name: cl_dispatch_order fk_dispatch_apply; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.cl_dispatch_order
    ADD CONSTRAINT fk_dispatch_apply FOREIGN KEY (apply_id) REFERENCES public.cl_apply_order(id);


--
-- Name: cl_dispatch_order fk_dispatch_vehicle; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.cl_dispatch_order
    ADD CONSTRAINT fk_dispatch_vehicle FOREIGN KEY (vehicle_id) REFERENCES public.cl_vehicle_archive(id);


--
-- Name: gy_repair_order fk_gy_repair_room; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.gy_repair_order
    ADD CONSTRAINT fk_gy_repair_room FOREIGN KEY (room_id) REFERENCES public.gy_room(id);


--
-- Name: gy_occupant fk_occupant_room; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.gy_occupant
    ADD CONSTRAINT fk_occupant_room FOREIGN KEY (room_id) REFERENCES public.gy_room(id);


--
-- Name: cl_repair_order fk_repair_vehicle; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.cl_repair_order
    ADD CONSTRAINT fk_repair_vehicle FOREIGN KEY (vehicle_id) REFERENCES public.cl_vehicle_archive(id);


--
-- Name: gc_return_order fk_return_borrow; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.gc_return_order
    ADD CONSTRAINT fk_return_borrow FOREIGN KEY (borrow_order_id) REFERENCES public.gc_borrow_order(id);


--
-- Name: st_inventory_record fk_st_inventory_material; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.st_inventory_record
    ADD CONSTRAINT fk_st_inventory_material FOREIGN KEY (material_id) REFERENCES public.st_material(id);


--
-- Name: st_purchase_detail fk_st_purchase_detail_material; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.st_purchase_detail
    ADD CONSTRAINT fk_st_purchase_detail_material FOREIGN KEY (material_id) REFERENCES public.st_material(id);


--
-- Name: st_purchase_detail fk_st_purchase_detail_order; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.st_purchase_detail
    ADD CONSTRAINT fk_st_purchase_detail_order FOREIGN KEY (purchase_order_id) REFERENCES public.st_purchase_order(id) ON DELETE CASCADE;


--
-- Name: sys_user_role fk_user_role_role; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.sys_user_role
    ADD CONSTRAINT fk_user_role_role FOREIGN KEY (role_id) REFERENCES public.sys_role(id) ON DELETE CASCADE;


--
-- Name: sys_user_role fk_user_role_user; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.sys_user_role
    ADD CONSTRAINT fk_user_role_user FOREIGN KEY (user_id) REFERENCES public.sys_user(id) ON DELETE CASCADE;


--
-- Name: gc_transfer_detail gc_transfer_detail_asset_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.gc_transfer_detail
    ADD CONSTRAINT gc_transfer_detail_asset_id_fkey FOREIGN KEY (asset_id) REFERENCES public.gc_asset_card(id);


--
-- Name: gc_transfer_detail gc_transfer_detail_transfer_order_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.gc_transfer_detail
    ADD CONSTRAINT gc_transfer_detail_transfer_order_id_fkey FOREIGN KEY (transfer_order_id) REFERENCES public.gc_transfer_order(id) ON DELETE CASCADE;


--
-- PostgreSQL database dump complete
--

\unrestrict qcKPAO2fdZqqi31xACapWZe1fNrnXLnxKflZsm9o0xXnI0sVaDZ15DjrafJcTSX

