-- 旅盈 MVP - 核心财务模型与菜单

drop table if exists ly_tour;
create table ly_tour (
  tour_id             bigint(20) not null auto_increment comment '主键',
  tour_no             varchar(32) not null comment '团号',
  tour_name           varchar(128) not null comment '团名称',
  depart_date         date not null comment '出团日期',
  status              varchar(16) not null default 'READY' comment '状态 READY/ONGOING/CLOSED',
  group_size          int(11) not null default 0 comment '人数',
  unit_price          decimal(12,2) not null default 0 comment '人均单价',
  budget_limit        decimal(12,2) not null default 0 comment '预算红线',
  sales_user_name     varchar(64) default '' comment '销售人员',
  customer_phone      varchar(32) default '' comment '游客手机号',
  create_by           varchar(64) default '' comment '创建者',
  create_time         datetime comment '创建时间',
  update_by           varchar(64) default '' comment '更新者',
  update_time         datetime comment '更新时间',
  primary key (tour_id),
  unique key uk_ly_tour_no (tour_no)
) engine=innodb comment='团期主表';

drop table if exists ly_tour_income;
create table ly_tour_income (
  income_id           bigint(20) not null auto_increment comment '主键',
  tour_id             bigint(20) not null comment '团期ID',
  income_type         varchar(16) default 'other' comment '收入类型 deposit/final/other',
  amount              decimal(12,2) not null comment '金额',
  received_time       datetime comment '收款时间',
  remark              varchar(255) default '' comment '备注',
  create_time         datetime comment '创建时间',
  primary key (income_id),
  key idx_ly_income_tour (tour_id)
) engine=innodb comment='团期收入流水';

drop table if exists ly_tour_cost;
create table ly_tour_cost (
  cost_id             bigint(20) not null auto_increment comment '主键',
  tour_id             bigint(20) not null comment '团期ID',
  category            varchar(32) not null comment '成本类别',
  amount              decimal(12,2) not null comment '金额',
  payment_method      varchar(16) not null comment '支付方式 PUBLIC/ADVANCE',
  advance_user_name   varchar(64) default '' comment '垫付人',
  status              varchar(16) not null default 'PENDING' comment '状态 PENDING/PAID/REJECTED',
  receipt_url         varchar(255) default '' comment '票据URL',
  occur_date          date comment '发生日期',
  approval_remark     varchar(255) default '' comment '审批备注',
  remark              varchar(255) default '' comment '备注',
  create_time         datetime comment '创建时间',
  update_by           varchar(64) default '' comment '更新者',
  update_time         datetime comment '更新时间',
  primary key (cost_id),
  key idx_ly_cost_tour (tour_id),
  key idx_ly_cost_status (status),
  key idx_ly_cost_user (advance_user_name)
) engine=innodb comment='团期成本/报销流水';

-- 示例数据
insert into ly_tour(tour_id, tour_no, tour_name, depart_date, status, group_size, unit_price, budget_limit, sales_user_name, customer_phone, create_by, create_time)
values
(1, 'HZ0420', '杭州2日游', '2026-04-20', 'ONGOING', 20, 1000, 15000, '张三', '13900000000', 'admin', sysdate()),
(2, 'NJ0412', '南京3日游', '2026-04-18', 'READY', 16, 980, 12000, '李四', '13800000000', 'admin', sysdate());

insert into ly_tour_income(income_id, tour_id, income_type, amount, received_time, remark, create_time)
values
(1, 1, 'deposit', 6000, sysdate(), '已收定金', sysdate()),
(2, 1, 'final', 10000, sysdate(), '已收尾款', sysdate()),
(3, 2, 'deposit', 5000, sysdate(), '已收定金', sysdate());

insert into ly_tour_cost(cost_id, tour_id, category, amount, payment_method, advance_user_name, status, receipt_url, occur_date, remark, create_time)
values
(1, 1, '交通', 4000, 'PUBLIC', '', 'PAID', '', curdate(), '大巴费用', sysdate()),
(2, 1, '住宿', 5200, 'PUBLIC', '', 'PAID', '', curdate(), '酒店预付', sysdate()),
(3, 1, '门票', 500, 'ADVANCE', '张三', 'PENDING', '', curdate(), '导游垫付', sysdate()),
(4, 1, '交通', 65, 'ADVANCE', '张三', 'PENDING', '', curdate(), '过路费', sysdate()),
(5, 2, '餐饮', 45, 'ADVANCE', '张三', 'PAID', '', curdate(), '矿泉水', sysdate());

-- 菜单定义（可直接执行到若依菜单表）
insert into sys_menu values('2300', '旅盈经营', '0', '5', 'lvying', null, '', '', 1, 0, 'M', '0', '0', '', 'money', 'admin', sysdate(), '', null, '旅盈轻量管理系统');
insert into sys_menu values('2301', '老板仪表盘', '2300', '1', 'dashboard', 'lvying/dashboard/index', '', '', 1, 0, 'C', '0', '0', 'lvying:dashboard:view', 'dashboard', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2302', '团详情中心', '2300', '2', 'tour/detail/1', 'lvying/tour/detail', '', '', 1, 0, 'C', '0', '0', 'lvying:tour:view', 'example', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2303', '极速报销中心', '2300', '3', 'reimburse', 'lvying/reimburse/index', '', '', 1, 0, 'C', '0', '0', 'lvying:reimburse:view', 'form', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2304', '智能催收助手', '2300', '4', 'collection', 'lvying/collection/index', '', '', 1, 0, 'C', '0', '0', 'lvying:collection:view', 'message', 'admin', sysdate(), '', null, '');

insert into sys_menu values('2310', '查看仪表盘', '2301', '1', '#', '', '', '', 1, 0, 'F', '0', '0', 'lvying:dashboard:view', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2311', '查看团详情', '2302', '1', '#', '', '', '', 1, 0, 'F', '0', '0', 'lvying:tour:view', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2312', '编辑团收支', '2302', '2', '#', '', '', '', 1, 0, 'F', '0', '0', 'lvying:tour:edit', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2313', '查看报销', '2303', '1', '#', '', '', '', 1, 0, 'F', '0', '0', 'lvying:reimburse:view', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2314', '审批报销', '2303', '2', '#', '', '', '', 1, 0, 'F', '0', '0', 'lvying:reimburse:approve', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2315', '查看催收', '2304', '1', '#', '', '', '', 1, 0, 'F', '0', '0', 'lvying:collection:view', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2316', '发送催收', '2304', '2', '#', '', '', '', 1, 0, 'F', '0', '0', 'lvying:collection:send', '#', 'admin', sysdate(), '', null, '');

-- 授权给管理员角色
insert into sys_role_menu(role_id, menu_id)
select 1, m.menu_id from sys_menu m where m.menu_id between 2300 and 2316;

-- 删除若依官网菜单
delete from sys_role_menu where menu_id = 4;
delete from sys_menu where menu_id = 4;