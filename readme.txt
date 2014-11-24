Diamond是一个持久配置管理中心，核心功能是使应用在运行中感知配置数据的变化。

数据库脚本：
create table config_info (	id bigint(64) PRIMARY KEY NOT NULL auto_increment,	data_id varchar(255) default NULL,	group_id varchar(128) default NULL,	content longtext NOT NULL,	md5 varchar(32) default NULL,	gmt_create datetime NOT NULL default '2013-05-01 00:00:00',	gmt_modified datetime NOT NULL default '2013-05-01 00:00:00',	UNIQUE KEY `uk_config_datagroup` (`data_id`,`group_id`));