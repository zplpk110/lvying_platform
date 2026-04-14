-- 旅盈 MVP V1.0 最小可用建表脚本
-- 目标：算清账、催到款、还清垫资

SET NAMES utf8mb4;

-- =====================================
-- 1) 团主表
-- =====================================
drop table if exists mvp_group;
create table mvp_group (
  id                 bigint(20) not null auto_increment comment '主键',
  group_name         varchar(64) not null comment '团名',
  depart_date        date not null comment '出团日',
  return_date        date default null comment '返程日',
  people_count       int(11) not null default 0 comment '人数',
  price_mode         tinyint(2) not null default 1 comment '计价方式 1人均 2总包',
  unit_price         decimal(10,2) default null comment '人均价',
  total_price        decimal(10,2) default null comment '总包价',
  budget_cost        decimal(10,2) not null default 0.00 comment '预算成本',
  status             tinyint(2) not null default 1 comment '状态 1进行中 2已结团',
  remark             varchar(255) default '' comment '备注',
  create_by          varchar(64) default '' comment '创建者',
  create_time        datetime comment '创建时间',
  update_by          varchar(64) default '' comment '更新者',
  update_time        datetime comment '更新时间',
  primary key (id),
  key idx_mvp_group_depart_date (depart_date),
  key idx_mvp_group_status (status)
) engine=innodb default charset=utf8mb4 comment='MVP 团主表';

-- =====================================
-- 2) 团-游客应收表
-- =====================================
drop table if exists mvp_group_customer;
create table mvp_group_customer (
  id                 bigint(20) not null auto_increment comment '主键',
  group_id           bigint(20) not null comment '团ID',
  customer_name      varchar(32) not null comment '游客姓名',
  phone              varchar(20) default '' comment '手机号',
  should_pay         decimal(10,2) not null default 0.00 comment '应收金额',
  paid_amount        decimal(10,2) not null default 0.00 comment '已收金额',
  payment_status     tinyint(2) not null default 0 comment '支付状态 0未付 1部分 2已付',
  final_due_date     date default null comment '尾款截止日',
  remind_level       tinyint(2) not null default 0 comment '催收级别 0未催 1T-3 2T-1 3当天',
  last_remind_time   datetime default null comment '最近催收时间',
  remark             varchar(255) default '' comment '备注',
  create_by          varchar(64) default '' comment '创建者',
  create_time        datetime comment '创建时间',
  update_by          varchar(64) default '' comment '更新者',
  update_time        datetime comment '更新时间',
  primary key (id),
  key idx_mvp_gc_group_id (group_id),
  key idx_mvp_gc_phone (phone),
  key idx_mvp_gc_due_status (final_due_date, payment_status)
) engine=innodb default charset=utf8mb4 comment='MVP 团-游客应收表';

-- =====================================
-- 3) 收支流水（核心账本）
-- =====================================
drop table if exists mvp_finance_record;
create table mvp_finance_record (
  id                 bigint(20) not null auto_increment comment '主键',
  group_id           bigint(20) not null comment '团ID',
  customer_id        bigint(20) default null comment '游客ID(收入场景可用)',
  record_type        tinyint(2) not null comment '流水类型 1收入 2成本',
  biz_type           varchar(20) not null comment '业务类型 定金/尾款/车费/餐费等',
  amount             decimal(10,2) not null comment '金额',
  occur_date         datetime not null comment '发生时间',
  payer_name         varchar(32) default '' comment '付款人(收入场景)',
  advance_user_id    bigint(20) default null comment '垫付员工ID(成本场景)',
  voucher_url        varchar(255) default '' comment '凭证图片地址',
  ocr_amount         decimal(10,2) default null comment 'OCR识别金额',
  status             tinyint(2) not null default 1 comment '状态 1有效 9作废',
  remark             varchar(255) default '' comment '备注',
  create_by          varchar(64) default '' comment '创建者',
  create_time        datetime comment '创建时间',
  update_by          varchar(64) default '' comment '更新者',
  update_time        datetime comment '更新时间',
  primary key (id),
  key idx_mvp_fin_group (group_id),
  key idx_mvp_fin_type_status (record_type, status),
  key idx_mvp_fin_adv_user (advance_user_id),
  key idx_mvp_fin_occur_date (occur_date)
) engine=innodb default charset=utf8mb4 comment='MVP 收支流水账本';

-- =====================================
-- 4) 垫资结算单
-- =====================================
drop table if exists mvp_advance_settlement;
create table mvp_advance_settlement (
  id                 bigint(20) not null auto_increment comment '主键',
  settle_month       varchar(7) not null comment '结算月份 YYYY-MM',
  user_id            bigint(20) not null comment '员工ID',
  total_advance      decimal(10,2) not null default 0.00 comment '本次应还总额',
  paid_amount        decimal(10,2) not null default 0.00 comment '本次实还金额',
  left_amount        decimal(10,2) not null default 0.00 comment '剩余未还金额',
  status             tinyint(2) not null default 1 comment '状态 1部分结清 2已结清',
  settle_time        datetime not null comment '结算时间',
  operator_id        bigint(20) not null comment '操作人(老板)ID',
  snapshot_json      text comment '结算明细快照',
  remark             varchar(255) default '' comment '备注',
  create_by          varchar(64) default '' comment '创建者',
  create_time        datetime comment '创建时间',
  update_by          varchar(64) default '' comment '更新者',
  update_time        datetime comment '更新时间',
  primary key (id),
  key idx_mvp_settle_month_user (settle_month, user_id),
  key idx_mvp_settle_status (status)
) engine=innodb default charset=utf8mb4 comment='MVP 垫资结算单';

-- =====================================
-- 5) 催款任务
-- =====================================
drop table if exists mvp_remind_task;
create table mvp_remind_task (
  id                 bigint(20) not null auto_increment comment '主键',
  group_id           bigint(20) not null comment '团ID',
  customer_id        bigint(20) not null comment '游客ID',
  plan_time          datetime not null comment '计划提醒时间',
  remind_type        tinyint(2) not null comment '提醒类型 1T-3 2T-1 3当天',
  status             tinyint(2) not null default 0 comment '状态 0待提醒 1已提醒 2忽略',
  content_template   varchar(500) default '' comment '话术模板',
  sent_time          datetime default null comment '发送时间',
  create_by          varchar(64) default '' comment '创建者',
  create_time        datetime comment '创建时间',
  update_by          varchar(64) default '' comment '更新者',
  update_time        datetime comment '更新时间',
  primary key (id),
  key idx_mvp_remind_plan_status (plan_time, status),
  key idx_mvp_remind_group_customer (group_id, customer_id)
) engine=innodb default charset=utf8mb4 comment='MVP 催款任务';

-- =====================================
-- 6) 首页待办/红点
-- =====================================
drop table if exists mvp_todo_notice;
create table mvp_todo_notice (
  id                 bigint(20) not null auto_increment comment '主键',
  notice_type        tinyint(2) not null comment '类型 1尾款未收 2预算超支 3垫资待还',
  title              varchar(100) not null comment '标题',
  content            varchar(255) default '' comment '内容',
  rel_id             bigint(20) default null comment '关联ID',
  level              tinyint(2) not null default 1 comment '级别 1黄 2红',
  status             tinyint(2) not null default 0 comment '状态 0未读 1已读 2已处理',
  target_role        tinyint(2) not null comment '目标角色 1老板 2员工',
  create_time        datetime comment '创建时间',
  primary key (id),
  key idx_mvp_notice_role_status (target_role, status),
  key idx_mvp_notice_type_level (notice_type, level)
) engine=innodb default charset=utf8mb4 comment='MVP 首页待办与红点';

-- =====================================
-- 7) 关键操作留痕
-- =====================================
drop table if exists mvp_audit_log;
create table mvp_audit_log (
  id                 bigint(20) not null auto_increment comment '主键',
  biz_type           varchar(30) not null comment '业务类型 group/finance/settlement',
  biz_id             bigint(20) not null comment '业务ID',
  action             varchar(30) not null comment '动作 CREATE/UPDATE/SETTLE/VOID',
  before_json        text comment '变更前快照',
  after_json         text comment '变更后快照',
  operator_id        bigint(20) not null comment '操作人ID',
  operator_name      varchar(32) default '' comment '操作人姓名',
  operate_time       datetime not null comment '操作时间',
  primary key (id),
  key idx_mvp_audit_biz (biz_type, biz_id),
  key idx_mvp_audit_operator_time (operator_id, operate_time)
) engine=innodb default charset=utf8mb4 comment='MVP 关键操作留痕';

-- =====================================
-- 示例数据（可删）
-- =====================================
insert into mvp_group
(id, group_name, depart_date, return_date, people_count, price_mode, unit_price, total_price, budget_cost, status, remark, create_by, create_time)
values
(1, '杭州2日团', '2026-04-20', '2026-04-21', 20, 1, 699.00, null, 10000.00, 1, 'MVP样例团', 'admin', now());

insert into mvp_group_customer
(id, group_id, customer_name, phone, should_pay, paid_amount, payment_status, final_due_date, remind_level, remark, create_by, create_time)
values
(1, 1, '王一', '13900000001', 699.00, 300.00, 1, '2026-04-19', 1, '已收定金', 'admin', now()),
(2, 1, '王二', '13900000002', 699.00, 699.00, 2, '2026-04-19', 0, '已结清', 'admin', now()),
(3, 1, '王三', '13900000003', 699.00, 0.00, 0, '2026-04-19', 0, '未支付', 'admin', now());

insert into mvp_finance_record
(id, group_id, customer_id, record_type, biz_type, amount, occur_date, payer_name, advance_user_id, voucher_url, ocr_amount, status, remark, create_by, create_time)
values
(1, 1, 1, 1, '定金', 300.00, now(), '王一', null, '', null, 1, '线下收款', 'admin', now()),
(2, 1, null, 2, '车费', 2800.00, now(), '', null, '', null, 1, '公司对公支付', 'admin', now()),
(3, 1, null, 2, '门票', 500.00, now(), '', 102, '', 500.00, 1, '导游垫付', 'admin', now());

insert into mvp_todo_notice
(id, notice_type, title, content, rel_id, level, status, target_role, create_time)
values
(1, 1, '尾款待催收', '杭州2日团还有2人未付尾款，共1098元', 1, 2, 0, 1, now()),
(2, 3, '垫资待还', '员工ID 102 当前待还垫资500元', 102, 1, 0, 1, now());