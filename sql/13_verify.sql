USE `stratos_system`;
SHOW TABLES LIKE 'cms_banner';
SELECT id, title, status FROM cms_banner;

USE `stratos_message`;
SELECT id, title, publish_status FROM message_notice LIMIT 5;
