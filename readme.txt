Diamond��һ���־����ù������ģ����Ĺ�����ʹӦ���������и�֪�������ݵı仯��

���ݿ�ű���
create table config_info (	id bigint(64) PRIMARY KEY NOT NULL auto_increment,	data_id varchar(255) default NULL,	group_id varchar(128) default NULL,	content longtext NOT NULL,	md5 varchar(32) default NULL,	gmt_create datetime NOT NULL default '2013-05-01 00:00:00',	gmt_modified datetime NOT NULL default '2013-05-01 00:00:00',	UNIQUE KEY `uk_config_datagroup` (`data_id`,`group_id`));