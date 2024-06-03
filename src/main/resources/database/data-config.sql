insert IGNORE INTO config (k, comment, v) VALUE ('CHAIN_TRC20_ADDR', '平台trc20收款地址', 'TAh1gLyLN8gphDTtUHcKUMp1ezgjfnSGmy');
insert IGNORE INTO config (k, comment, v) VALUE ('NEW_ACCOUNT_DONATE', '新用户赠送金额', '100');
insert IGNORE INTO config (k, comment, v) VALUE ('FOLLOW_COST_RATIO', '跟单手续费', '0.2');
insert IGNORE INTO config (k, comment, v) VALUE ('STATE_MODE', '结算模式', 'PER_ORDER');
insert IGNORE INTO config (k, comment, v) VALUE ('INVITE_REBATE', '邀请人返佣比例', '0.05');
insert IGNORE INTO config (k, comment, v) VALUE ('PARTNER_MAX_REBATE', '合伙人最高返佣比例', '0.45');
insert IGNORE INTO config (k, comment, v) VALUE ('STRATEGY_REBATE', '策略最高返佣比例', '0.45');
insert IGNORE INTO config (k, comment, v) VALUE ('TERMS_OF_SERVICE', '服务协议', '服务协议内容');
insert IGNORE INTO config (k, comment, v) VALUE ('EXCHANGE', '可用交易所', 'OKEX');
insert IGNORE INTO config (k, comment, v) VALUE ('SERVER_IP', '服务器IP', '16.162.79.61,193.134.211.206,18.166.52.72');
insert IGNORE INTO config (k, comment, v) VALUE ('VIP_FEE', 'VIP费用', '100');
insert IGNORE INTO config (k, comment, v) VALUE ('VIP_FEE_ORIGIN', 'vip开通原价费用', '120');
insert IGNORE INTO config (k, comment, v) VALUE ('VIP_ONE_LEVEL_REBATE', 'VIP关系一级返利', '0.15');
insert IGNORE INTO config (k, comment, v) VALUE ('VIP_TWO_LEVEL_REBATE', 'VIP关系二级返利', '0.05');
insert IGNORE INTO config (k, comment, v) VALUE ('BILL_NET_VALUE', '账单结算净值模式', 'true');
insert IGNORE INTO config (k, comment, v) VALUE ('REGISTER_GIVE_VIP_DAY', '注册赠送vip天数', '0');
insert IGNORE INTO config (k, comment, v) VALUE ('VIP_EXPIRE_WARN_DAY', '距离会员到期提醒的天数', '3');
insert IGNORE INTO config (k, comment, v) VALUE ('MIN_SETTLE_AMOUNT', '最小结算金额', '1');
insert IGNORE INTO config (k, comment, v) VALUE ('RECOMMEND_STRATEGY', '推荐策略', '28,15,17');
insert IGNORE INTO config (k, comment, v) VALUE ('PLATFORM_NAME', '平台名称', '[金狐量化]');
insert IGNORE INTO config (k, comment, v) VALUE ('EMAIL_FROM', '邮件发件人', 'noreply@glodfox.com');

insert IGNORE INTO vip_fee_config (discount, next_count) VALUE (0.6, 3);
insert IGNORE INTO vip_fee_config (discount, next_count) VALUE (0.5, 5);

insert IGNORE INTO `account`(`id`, `created_at`, `updated_at`, `email`, `head_img`, `invite_code`, `last_login_ip`, `last_login_time`, `password`, `role`, `status`, `token`, `trx_address`, `trx_private`, `vip_count`, `vip_expired`, `vip_level`) VALUES (1, '2023-10-23 10:56:29.931000', '2023-10-23 10:56:29.931000', 'admin@admin.com', NULL, 'FI6khqPm', NULL, NULL, '$2a$10$RiEeu0ZLYlVmWD7dztVKPeUWIMOFWX2NL7FOJuX/nzLVDFnuSlAy2', 'ROLE_ADMIN', 'NORMAL', NULL, 'TTrZnadHKd17SxwUMQp7oXnKDZVeosTAqS', '2a395912fc81e79deecc5026816f97b01045a77e23823774f7060765a1522755a20ace1e6e7e50479a5072576ae8d907bb64f44baa1528704d0bdb59284f14de9b0142507161b4145843a543547ec29d', 0, NULL, 0);
insert IGNORE into `wallet`(`id`,`created_at`,`updated_at`,`account_id`,`balance`,`cash_out_amount`,`follow_cost_amount`,`give_amount`,`rebate_amount`,`recharge_amount`) values (1,'2024-05-29 16:20:11.000000','2024-05-29 16:20:13.000000',1,'0.00000','0.00','0.00000','0.00','0.00000','0.00');