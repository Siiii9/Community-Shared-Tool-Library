-- 重新插入hammer工具信息

-- 插入hammer工具到published_tool表（发出人是3）
INSERT INTO published_tool (tool_name, tool_type, description, location, status, owner_id, borrower_id, borrow_days_limit) 
VALUES ('hammer', 'hand tools', 'A sturdy hammer for general use', 'Community Center', 'borrowing', 3, 2, 30);

-- 获取刚插入的工具ID
SET @hammer_id = LAST_INSERT_ID();

-- 插入借用信息到borrow_info表（借的人是2）
INSERT INTO borrow_info (tool_id, tool_name, borrower_id, owner_id, borrow_time, expected_return_time, status) 
VALUES (@hammer_id, 'hammer', 2, 3, NOW(), DATE_ADD(NOW(), INTERVAL 7 DAY), 'borrowing');

-- 输出结果
SELECT 'hammer工具重新插入成功！' AS message;