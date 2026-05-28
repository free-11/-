INSERT IGNORE INTO users (email, password, nickname) VALUES ('test@example.com', '123456', '测试用户');

INSERT IGNORE INTO lunches (name, description, tags, user_id) VALUES
('麻辣烫', '麻辣鲜香，食材丰富', '辣,川菜,重口味', (SELECT id FROM users WHERE email = 'test@example.com')),
('炸鸡',   '外酥里嫩，香气扑鼻', '油炸,快餐,高热量', (SELECT id FROM users WHERE email = 'test@example.com')),
('寿司',   '新鲜美味，精致小巧', '日料,海鲜,清淡', (SELECT id FROM users WHERE email = 'test@example.com')),
('汉堡',   '经典美式，方便快捷', '快餐,西式,方便', (SELECT id FROM users WHERE email = 'test@example.com')),
('酸菜鱼', '酸辣可口，鱼肉嫩滑', '辣,川菜,鱼', (SELECT id FROM users WHERE email = 'test@example.com')),
('沙拉',   '健康轻食，营养均衡', '健康,蔬菜,轻食', (SELECT id FROM users WHERE email = 'test@example.com'));
