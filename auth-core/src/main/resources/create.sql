CREATE TABLE IF NOT EXISTS `Account` (
	`id` int(11) NOT NULL AUTO_INCREMENT,
	`email` varchar(255) NOT NULL,
	`password` varchar(255) DEFAULT NULL,
	`created` datetime NOT NULL,
	`verified` tinyint(1) NOT NULL DEFAULT '0',
	`facebookId` varchar(255) DEFAULT NULL,
	PRIMARY KEY (`id`),
	UNIQUE KEY `email` (`email`)
);

CREATE TABLE IF NOT EXISTS `PlatformClient` (
	`id` int(11) NOT NULL AUTO_INCREMENT,
	`clientId` varchar(255) NOT NULL,
	`clientSecret` varchar(255) NOT NULL,
	`account_id` int(11) NOT NULL,
	`authorities` varchar(255) NOT NULL,
	`authorizedGrantTypes` varchar(255) NOT NULL,
	`scope` varchar(255) NOT NULL,
	`autoApprove` tinyint(1) NOT NULL DEFAULT '0',
	PRIMARY KEY (`id`),
	UNIQUE KEY `clientId` (`clientId`),
	FOREIGN KEY (`account_id`) REFERENCES `Account`(`id`)
);

CREATE TABLE `AllowedScope` (
	`scope` varchar(60) NOT NULL,
	PRIMARY KEY (`scope`)
);

INSERT INTO `Account` (`email`, `password`, `created`, `verified`)
	values ('kyle@bernstein.ca', '$2a$11$Y4MRghYtb/FoMlb2Pc4e5uuvi3vL3YaUTZNKadaSS/kYxRl7lCVLG', now(), 1);
INSERT INTO `Account` (`email`, `created`, `verified`, `facebookId`)
	values ('ky254@hotmail.com', now(), 1, '10208552944055205');
INSERT INTO `Account` (`email`, `password`, `created`, `verified`)
	values ('kbernst30@gmail.com', '$2a$11$Y4MRghYtb/FoMlb2Pc4e5uuvi3vL3YaUTZNKadaSS/kYxRl7lCVLG', now(), 1);

INSERT INTO `PlatformClient`values (0, 'my-client',
	'$2a$04$JMEZMLOkqLt51eTGsRqbbOrNv8eEGjRBMJgGoIXo5N.3cF9S3FEkq', 1, 'ROLE_ADMIN',
	'password,refresh_token,client_credentials,authorization_code,implicit', 'privileged', 1);