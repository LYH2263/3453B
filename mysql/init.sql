SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

CREATE DATABASE IF NOT EXISTS club_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE club_db;

-- 用户表
CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码',
    real_name VARCHAR(50) NOT NULL COMMENT '真实姓名',
    student_id VARCHAR(20) DEFAULT NULL COMMENT '学号',
    role ENUM('ADMIN', 'UNION_ADMIN', 'CLUB_LEADER', 'MEMBER', 'GUEST') NOT NULL DEFAULT 'GUEST' COMMENT '角色',
    club_id INT DEFAULT NULL COMMENT '所属社团ID',
    avatar VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted TINYINT(1) DEFAULT 0 COMMENT '逻辑删除'
) COMMENT='用户表';

-- 社团表
CREATE TABLE IF NOT EXISTS clubs (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE COMMENT '社团名称',
    description TEXT COMMENT '社团简介',
    leader_id INT COMMENT '负责人ID',
    status ENUM('NORMAL', 'RETIRED') NOT NULL DEFAULT 'NORMAL' COMMENT '状态',
    logo VARCHAR(255) DEFAULT NULL COMMENT '社团Logo',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted TINYINT(1) DEFAULT 0 COMMENT '逻辑删除',
    FOREIGN KEY (leader_id) REFERENCES users(id)
) COMMENT='社团表';

-- 活动表
CREATE TABLE IF NOT EXISTS activities (
    id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL COMMENT '详情',
    description TEXT COMMENT '活动详情',
    club_id INT DEFAULT NULL COMMENT '所属社团ID',
    start_time DATETIME NOT NULL COMMENT '开始时间',
    end_time DATETIME NOT NULL COMMENT '结束时间',
    location VARCHAR(255) NOT NULL COMMENT '地点',
    max_count INT NOT NULL DEFAULT 50 COMMENT '人数限制',
    budget DECIMAL(10, 2) DEFAULT 0.00 COMMENT '预算',
    process TEXT COMMENT '活动流程',
    status ENUM('PENDING_UNION', 'PENDING_SCHOOL', 'APPROVED', 'REJECTED', 'FINISHED') NOT NULL DEFAULT 'PENDING_UNION' COMMENT '状态',
    reject_reason VARCHAR(255) DEFAULT NULL COMMENT '审核驳回原因',
    poster VARCHAR(255) DEFAULT NULL COMMENT '活动海报',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted TINYINT(1) DEFAULT 0 COMMENT '逻辑删除',
    FOREIGN KEY (club_id) REFERENCES clubs(id)
) COMMENT='活动表';

-- 活动报名表
CREATE TABLE IF NOT EXISTS activity_registrations (
    id INT AUTO_INCREMENT PRIMARY KEY,
    activity_id INT NOT NULL COMMENT '活动ID',
    user_id INT NOT NULL COMMENT '用户ID',
    status ENUM('REGISTERED', 'SIGNED_IN') NOT NULL DEFAULT 'REGISTERED' COMMENT '报名状态',
    rating INT DEFAULT NULL COMMENT '评分(1-5)',
    feedback TEXT DEFAULT NULL COMMENT '反馈内容',
    reply TEXT DEFAULT NULL COMMENT '负责人回复',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '报名时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (activity_id) REFERENCES activities(id),
    FOREIGN KEY (user_id) REFERENCES users(id)
) COMMENT='活动报名表';

-- 公告表
CREATE TABLE IF NOT EXISTS announcements (
    id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL COMMENT '标题',
    content TEXT NOT NULL COMMENT '内容',
    publisher_id INT NOT NULL COMMENT '发布者ID',
    club_id INT DEFAULT NULL COMMENT '定向社团ID(NULL为全校)',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
    is_deleted TINYINT(1) DEFAULT 0 COMMENT '逻辑删除',
    FOREIGN KEY (publisher_id) REFERENCES users(id)
) COMMENT='公告表';

-- 话题讨论表
CREATE TABLE IF NOT EXISTS topics (
    id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL COMMENT '标题',
    content TEXT NOT NULL COMMENT '内容',
    author_id INT NOT NULL COMMENT '作者ID',
    club_id INT DEFAULT NULL COMMENT '关联社团ID',
    type ENUM('IN_CLUB', 'CROSS_CLUB') NOT NULL DEFAULT 'IN_CLUB' COMMENT '类型',
    audit_status ENUM('PENDING', 'APPROVED', 'REJECTED') NOT NULL DEFAULT 'APPROVED' COMMENT '审核状态(跨社团需审)',
    status ENUM('NORMAL', 'TOP', 'HIDDEN') NOT NULL DEFAULT 'NORMAL' COMMENT '状态',
    likes_count INT DEFAULT 0 COMMENT '点赞数',
    favorites_count INT DEFAULT 0 COMMENT '收藏数',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted TINYINT(1) DEFAULT 0 COMMENT '逻辑删除',
    FOREIGN KEY (author_id) REFERENCES users(id)
) COMMENT='话题讨论表';

-- 评论表
CREATE TABLE IF NOT EXISTS comments (
    id INT AUTO_INCREMENT PRIMARY KEY,
    topic_id INT NOT NULL COMMENT '话题ID',
    author_id INT NOT NULL COMMENT '作者ID',
    content TEXT NOT NULL COMMENT '内容',
    reply_id INT DEFAULT NULL COMMENT '回复的评论ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '评论时间',
    is_deleted TINYINT(1) DEFAULT 0 COMMENT '逻辑删除',
    FOREIGN KEY (topic_id) REFERENCES topics(id),
    FOREIGN KEY (author_id) REFERENCES users(id)
) COMMENT='评论表';

-- 话题互动表(点赞/收藏)
CREATE TABLE IF NOT EXISTS topic_interactions (
    id INT AUTO_INCREMENT PRIMARY KEY,
    topic_id INT NOT NULL COMMENT '话题ID',
    user_id INT NOT NULL COMMENT '用户ID',
    type ENUM('LIKE', 'FAVORITE') NOT NULL COMMENT '互动类型',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '时间',
    UNIQUE KEY uk_topic_user_type (topic_id, user_id, type),
    FOREIGN KEY (topic_id) REFERENCES topics(id),
    FOREIGN KEY (user_id) REFERENCES users(id)
) COMMENT='话题互动表';

-- 招新信息表
CREATE TABLE IF NOT EXISTS recruitments (
    id INT AUTO_INCREMENT PRIMARY KEY,
    club_id INT NOT NULL COMMENT '社团ID',
    title VARCHAR(200) NOT NULL COMMENT '招新标题',
    description TEXT NOT NULL COMMENT '招新要求与介绍',
    status ENUM('OPEN', 'CLOSED') NOT NULL DEFAULT 'OPEN' COMMENT '状态',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (club_id) REFERENCES clubs(id)
) COMMENT='招新信息表';

-- 招新报名表
CREATE TABLE IF NOT EXISTS recruitment_applications (
    id INT AUTO_INCREMENT PRIMARY KEY,
    recruitment_id INT NOT NULL COMMENT '招新ID',
    user_id INT NOT NULL COMMENT '用户ID',
    resume_text TEXT NOT NULL COMMENT '个人陈述/简历',
    status ENUM('PENDING', 'APPROVED', 'REJECTED') NOT NULL DEFAULT 'PENDING' COMMENT '状态',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '投递时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (recruitment_id) REFERENCES recruitments(id),
    FOREIGN KEY (user_id) REFERENCES users(id)
) COMMENT='招新报名表';

-- 问答社区：问题表
CREATE TABLE IF NOT EXISTS questions (
    id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL COMMENT '问题标题',
    content TEXT NOT NULL COMMENT '问题详情',
    author_id INT NOT NULL COMMENT '提问者ID',
    target_club_id INT DEFAULT NULL COMMENT '指定社团ID(可选)',
    target_role ENUM('ADMIN', 'UNION_ADMIN', 'CLUB_LEADER') DEFAULT NULL COMMENT '指定解答角色(可选)',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '提问时间',
    FOREIGN KEY (author_id) REFERENCES users(id),
    FOREIGN KEY (target_club_id) REFERENCES clubs(id)
) COMMENT='问题表';

-- 问答社区：回答表
CREATE TABLE IF NOT EXISTS answers (
    id INT AUTO_INCREMENT PRIMARY KEY,
    question_id INT NOT NULL COMMENT '问题ID',
    author_id INT NOT NULL COMMENT '回答者ID',
    content TEXT NOT NULL COMMENT '回答内容',
    is_best TINYINT(1) DEFAULT 0 COMMENT '是否最佳答案',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '回答时间',
    FOREIGN KEY (question_id) REFERENCES questions(id),
    FOREIGN KEY (author_id) REFERENCES users(id)
) COMMENT='回答表';

-- 志愿服务记录表
CREATE TABLE IF NOT EXISTS volunteer_records (
    id INT AUTO_INCREMENT PRIMARY KEY,
    student_id INT NOT NULL COMMENT '学生ID',
    activity_name VARCHAR(200) NOT NULL COMMENT '活动名称',
    service_date DATE NOT NULL COMMENT '服务日期',
    hours DECIMAL(5,2) NOT NULL COMMENT '时长小时数',
    proof_url VARCHAR(500) DEFAULT NULL COMMENT '证明URL',
    status ENUM('PENDING', 'APPROVED', 'REJECTED') NOT NULL DEFAULT 'PENDING' COMMENT '状态',
    auditor_id INT DEFAULT NULL COMMENT '审核人ID',
    club_id INT DEFAULT NULL COMMENT '所属社团ID',
    reject_reason VARCHAR(500) DEFAULT NULL COMMENT '驳回原因',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted TINYINT(1) DEFAULT 0 COMMENT '逻辑删除',
    FOREIGN KEY (student_id) REFERENCES users(id),
    FOREIGN KEY (auditor_id) REFERENCES users(id),
    FOREIGN KEY (club_id) REFERENCES clubs(id),
    INDEX idx_student_id (student_id),
    INDEX idx_club_id (club_id),
    INDEX idx_status (status),
    INDEX idx_service_date (service_date)
) COMMENT='志愿服务记录表';

-- 志愿服务统计表
CREATE TABLE IF NOT EXISTS volunteer_stats (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL COMMENT '用户ID',
    total_hours DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '累计服务时长',
    approved_count INT NOT NULL DEFAULT 0 COMMENT '已通过记录数',
    pending_count INT NOT NULL DEFAULT 0 COMMENT '待审核记录数',
    rejected_count INT NOT NULL DEFAULT 0 COMMENT '已驳回记录数',
    last_service_date DATE DEFAULT NULL COMMENT '最近服务日期',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_user_id (user_id),
    FOREIGN KEY (user_id) REFERENCES users(id)
) COMMENT='志愿服务统计表';

-- ----------------------------
-- Mock Data (测试数据)
-- ----------------------------

-- 用户数据 (密码均为 123456 的 BCrypt 加密或简单明文，取决于系统实现。这里假设后端处理)
-- 提示：Admin 默认密码通常由后端配置，此处仅供展示
INSERT INTO users (id, username, password, real_name, student_id, role, avatar) VALUES
(1, 'admin', '123456', '超级管理员', 'ADMIN001', 'ADMIN', 'https://api.dicebear.com/7.x/avataaars/svg?seed=admin'),
(2, 'union_admin', '123456', '社联负责人-张老师', 'UNION001', 'UNION_ADMIN', 'https://api.dicebear.com/7.x/avataaars/svg?seed=union'),
(3, 'tech_leader', '123456', '极客社长-赵极客', '20210001', 'CLUB_LEADER', 'https://api.dicebear.com/7.x/avataaars/svg?seed=tech'),
(4, 'art_leader', '123456', '艺术社长-刘艺术', '20210002', 'CLUB_LEADER', 'https://api.dicebear.com/7.x/avataaars/svg?seed=art'),
(5, 'member_lisi', '123456', '李四', '20220005', 'MEMBER', 'https://api.dicebear.com/7.x/avataaars/svg?seed=lisi'),
(6, 'member_wangwu', '123456', '王五', '20220006', 'MEMBER', 'https://api.dicebear.com/7.x/avataaars/svg?seed=wangwu'),
(7, 'club_leader1', '123456', '测试社团负责人', '20260001', 'CLUB_LEADER', 'https://api.dicebear.com/7.x/avataaars/svg?seed=cl1'),
(8, 'student1', '123456', '测试学生1', '20260002', 'MEMBER', 'https://api.dicebear.com/7.x/avataaars/svg?seed=st1');

-- 社团数据
INSERT INTO clubs (id, name, description, leader_id, status, logo) VALUES
(1, '极客电子社', '致力于探索前沿电子技术、机器人开发与编程挑战的学术技术型社团。', 3, 'NORMAL', '/mock_images/club_logo_tech_1772179162369.png'),
(2, '悦动艺术社', '聚集全校音乐、绘画与表演爱好者，旨在打造多元化的校园文化艺术氛围。', 4, 'NORMAL', '/mock_images/club_logo_music_art_1772179189054.png');

-- 活动数据
-- 状态: PENDING_UNION, PENDING_SCHOOL, APPROVED, REJECTED, FINISHED
INSERT INTO activities (id, title, description, club_id, start_time, end_time, location, max_count, budget, process, status, poster) VALUES
(1, '2024春季校园马拉松', '一年一度的校园长跑盛会，全校师生均可参与，传播健康生活理念。', 2, '2024-03-20 09:00:00', '2024-03-20 12:00:00', '学校大操场', 500, 2000.00, '1. 集合签到\n2. 开幕式\n3. 正式开跑\n4. 颁奖闭幕', 'APPROVED', '/mock_images/activity_poster_sports_1772179174561.png'),
(2, '极客创意编程大赛', '针对全校学生的编程挑战赛，不限语言，旨在解决校园生活中的实际问题。', 1, '2024-04-15 14:00:00', '2024-04-15 18:00:00', '科技楼102', 100, 500.00, '1. 选题\n2. 现场编码\n3. 作品展示', 'PENDING_UNION', '/mock_images/activity_poster_coding_1772179331295.png'),
(3, '校园歌手大赛', '寻找校园最美声音，专业的评委阵容，丰厚的优胜奖品。', 2, '2024-02-15 19:30:00', '2024-02-15 22:00:00', '大礼堂', 300, 1500.00, '1. 海选\n2. 决赛', 'FINISHED', '/mock_images/activity_poster_music_1772179342665.png');

-- 报名数据
INSERT INTO activity_registrations (activity_id, user_id, status, rating, feedback) VALUES
(1, 5, 'SIGNED_IN', 5, '组织得很棒，奖牌很漂亮！'),
(1, 6, 'REGISTERED', NULL, NULL),
(3, 5, 'SIGNED_IN', 4, '音响效果很好，就是座位有点挤。');

-- 公告数据
INSERT INTO announcements (title, content, publisher_id, club_id) VALUES
('关于加强社团活动安全的通知', '各社团在举办大型户外活动时需严格遵守校园安全规定...', 1, NULL),
('极客社本周三例会通知', '请所有干事下午4点准时到达社团办公室，讨论招新后续。', 3, 1);

-- 话题讨论
-- 类型: IN_CLUB, CROSS_CLUB
INSERT INTO topics (id, title, content, author_id, club_id, type, audit_status) VALUES
(1, '关于极客社新项目招人的想法', '我想在社团内发起一个智能平衡车项目，欢迎有兴趣的同学加入讨论！', 3, 1, 'IN_CLUB', 'APPROVED'),
(2, '校园活动如何平衡学业与爱好？', '大家是怎么分配时间的？感觉最近活动太多有点忙不过来了。', 5, NULL, 'CROSS_CLUB', 'APPROVED');

-- 评论
INSERT INTO comments (topic_id, author_id, content) VALUES
(1, 6, '支持社长！我有Arduino开发的经验。'),
(2, 4, '我觉得还是要制定清晰的优先级，建议使用时间管理APP。');

-- 招新信息
INSERT INTO recruitments (id, club_id, title, description, status) VALUES
(1, 1, '2024极客社春季招新', '寻找对技术有热忱、敢于挑战自我的你，不限年级不限专业！', 'OPEN'),
(2, 2, '艺术社舞蹈部招社员', '如果你热爱舞蹈，有一定基础，欢迎加入我们的大家庭。', 'CLOSED');

-- 招新报名
INSERT INTO recruitment_applications (recruitment_id, user_id, resume_text, status) VALUES
(1, 5, '我对嵌入式开发非常感兴趣，自学过C语言。', 'PENDING'),
(1, 6, '我有较强的团队协作能力，想在技术社团提升技能。', 'APPROVED');

-- 问答
INSERT INTO questions (id, title, content, author_id, target_club_id, target_role) VALUES
(1, '社团管理系统如何申请账号？', '我是大一新生，想加入社团，请问系统账号是统一分配还是自己注册？', 6, NULL, 'ADMIN'),
(2, '极客社的项目经费如何报销？', '作为项目负责人，想了解具体的报销流程和所需票据。', 3, 1, 'CLUB_LEADER');

-- 回答
INSERT INTO answers (question_id, author_id, content, is_best) VALUES
(1, 1, '系统账号需通过学生证号注册，后台审核后即可登录。', 1),
(2, 2, '报销流程需先在社管系统提交申请，附上电子发票，张老师审核后通过。', 0);

-- 志愿服务记录测试数据
INSERT INTO volunteer_records (student_id, activity_name, service_date, hours, proof_url, status, auditor_id, club_id, reject_reason) VALUES
(5, '校园图书馆整理', '2024-03-15', 4.0, 'https://example.com/proof1.jpg', 'APPROVED', 3, 1, NULL),
(5, '社区敬老院慰问', '2024-03-22', 3.5, 'https://example.com/proof2.jpg', 'APPROVED', 3, 1, NULL),
(5, '校园清洁日活动', '2024-04-01', 2.0, 'https://example.com/proof3.jpg', 'PENDING', NULL, 2, NULL),
(6, '新生报到志愿服务', '2024-02-28', 6.0, 'https://example.com/proof4.jpg', 'APPROVED', 4, 2, NULL),
(6, '春季运动会志愿者', '2024-04-10', 8.0, 'https://example.com/proof5.jpg', 'PENDING', NULL, 2, NULL),
(8, '图书馆志愿服务', '2024-03-10', 3.0, 'https://example.com/proof6.jpg', 'REJECTED', 3, 1, '证明材料不清晰，请重新上传');

-- 志愿服务统计测试数据
INSERT INTO volunteer_stats (user_id, total_hours, approved_count, pending_count, rejected_count, last_service_date) VALUES
(5, 7.5, 2, 1, 0, '2024-04-01'),
(6, 6.0, 1, 1, 0, '2024-04-10'),
(8, 0.0, 0, 0, 1, '2024-03-10');

-- 投票表
CREATE TABLE IF NOT EXISTS votes (
    id INT AUTO_INCREMENT PRIMARY KEY,
    club_id INT NOT NULL COMMENT '社团ID',
    title VARCHAR(200) NOT NULL COMMENT '投票标题',
    type ENUM('SINGLE', 'MULTIPLE') NOT NULL DEFAULT 'SINGLE' COMMENT '投票类型: 单选/多选',
    max_choices INT NOT NULL DEFAULT 1 COMMENT '多选时最多可选数',
    deadline DATETIME NOT NULL COMMENT '截止时间',
    status ENUM('OPEN', 'CLOSED') NOT NULL DEFAULT 'OPEN' COMMENT '状态',
    creator_id INT NOT NULL COMMENT '创建人ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted TINYINT(1) DEFAULT 0 COMMENT '逻辑删除',
    FOREIGN KEY (club_id) REFERENCES clubs(id),
    FOREIGN KEY (creator_id) REFERENCES users(id),
    INDEX idx_club_id (club_id),
    INDEX idx_status (status)
) COMMENT='投票表';

-- 投票选项表
CREATE TABLE IF NOT EXISTS vote_options (
    id INT AUTO_INCREMENT PRIMARY KEY,
    vote_id INT NOT NULL COMMENT '投票ID',
    option_text VARCHAR(500) NOT NULL COMMENT '选项文本',
    vote_count INT NOT NULL DEFAULT 0 COMMENT '票数',
    sort_order INT NOT NULL DEFAULT 0 COMMENT '排序',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (vote_id) REFERENCES votes(id),
    INDEX idx_vote_id (vote_id)
) COMMENT='投票选项表';

-- 投票选票表
CREATE TABLE IF NOT EXISTS vote_ballots (
    id INT AUTO_INCREMENT PRIMARY KEY,
    vote_id INT NOT NULL COMMENT '投票ID',
    user_id INT NOT NULL COMMENT '投票用户ID',
    option_id INT NOT NULL COMMENT '选项ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '投票时间',
    UNIQUE KEY uk_vote_user_option (vote_id, user_id, option_id),
    FOREIGN KEY (vote_id) REFERENCES votes(id),
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (option_id) REFERENCES vote_options(id),
    INDEX idx_vote_id (vote_id),
    INDEX idx_user_id (user_id)
) COMMENT='投票选票表';

-- 课程表
CREATE TABLE IF NOT EXISTS courses (
    id INT AUTO_INCREMENT PRIMARY KEY,
    club_id INT NOT NULL COMMENT '所属社团ID',
    title VARCHAR(200) NOT NULL COMMENT '课程标题',
    description TEXT COMMENT '课程简介',
    cover VARCHAR(500) DEFAULT NULL COMMENT '课程封面',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted TINYINT(1) DEFAULT 0 COMMENT '逻辑删除',
    FOREIGN KEY (club_id) REFERENCES clubs(id),
    INDEX idx_club_id (club_id)
) COMMENT='课程表';

-- 课程章节表
CREATE TABLE IF NOT EXISTS course_chapters (
    id INT AUTO_INCREMENT PRIMARY KEY,
    course_id INT NOT NULL COMMENT '课程ID',
    sort_order INT NOT NULL DEFAULT 0 COMMENT '章节序号',
    title VARCHAR(200) NOT NULL COMMENT '章节标题',
    content MEDIUMTEXT COMMENT '内容(Markdown/HTML)',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted TINYINT(1) DEFAULT 0 COMMENT '逻辑删除',
    FOREIGN KEY (course_id) REFERENCES courses(id),
    INDEX idx_course_id (course_id),
    INDEX idx_course_order (course_id, sort_order)
) COMMENT='课程章节表';

-- 章节进度表
CREATE TABLE IF NOT EXISTS chapter_progress (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL COMMENT '用户ID',
    chapter_id INT NOT NULL COMMENT '章节ID',
    completed TINYINT(1) DEFAULT 0 COMMENT '是否完成',
    complete_time DATETIME DEFAULT NULL COMMENT '完成时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_user_chapter (user_id, chapter_id),
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (chapter_id) REFERENCES course_chapters(id),
    INDEX idx_user_id (user_id),
    INDEX idx_chapter_id (chapter_id)
) COMMENT='章节进度表';

-- 课程测试数据
INSERT INTO courses (id, club_id, title, description, cover) VALUES
(1, 1, 'Arduino 入门到精通', '从零开始学习 Arduino 单片机开发，涵盖硬件基础、常用模块、项目实战。适合电子爱好者与编程初学者。', '/mock_images/media__1772178695205.png'),
(2, 1, 'Python 数据分析实战', '使用 Python 进行数据清洗、可视化与机器学习入门，包含 NumPy、Pandas、Matplotlib 等常用库。', '/mock_images/media__1772178708344.png'),
(3, 2, '声乐基础训练课', '从气息控制到发声技巧，系统化学习声乐基础知识，适合零基础音乐爱好者。', '/mock_images/media__1772178731457.png'),
(4, 2, '水彩画入门教程', '讲解水彩画的材料、技法与创作流程，包含大量实操案例。', '/mock_images/media__1772178768576.png');

-- 章节测试数据
INSERT INTO course_chapters (course_id, sort_order, title, content) VALUES
(1, 1, '第1章 Arduino 简介与硬件准备', '# Arduino 简介\n\nArduino 是一款开源的电子原型平台，易于学习和使用。\n\n## 所需材料\n- Arduino UNO 开发板\n- USB 数据线\n- 面包板与跳线\n- LED 若干\n\n## 安装 IDE\n1. 访问 [arduino.cc](https://www.arduino.cc/) 下载 IDE\n2. 安装驱动程序\n3. 连接开发板并选择端口'),
(1, 2, '第2章 第一个程序 Blink', '# Blink 示例\n\n```cpp\nvoid setup() {\n  pinMode(LED_BUILTIN, OUTPUT);\n}\n\nvoid loop() {\n  digitalWrite(LED_BUILTIN, HIGH);\n  delay(1000);\n  digitalWrite(LED_BUILTIN, LOW);\n  delay(1000);\n}\n```\n\n## 实验步骤\n1. 将代码复制到 IDE\n2. 选择正确的开发板和端口\n3. 点击上传按钮\n4. 观察 LED 闪烁'),
(1, 3, '第3章 数字输入输出', '# 数字 I/O\n\n## pinMode 模式\n- INPUT：输入模式（高阻抗）\n- INPUT_PULLUP：输入上拉模式\n- OUTPUT：输出模式\n\n## 按键实验\n```cpp\nconst int btnPin = 2;\nconst int ledPin = 13;\n\nvoid setup() {\n  pinMode(btnPin, INPUT_PULLUP);\n  pinMode(ledPin, OUTPUT);\n}\n\nvoid loop() {\n  if (digitalRead(btnPin) == LOW) {\n    digitalWrite(ledPin, HIGH);\n  } else {\n    digitalWrite(ledPin, LOW);\n  }\n}'),
(1, 4, '第4章 PWM 与模拟输出', '# PWM 模拟输出\n\nanalogWrite() 可产生 0-255 级的 PWM 信号，用于控制 LED 亮度、电机转速等。\n\n## 呼吸灯实验\n```cpp\nint brightness = 0;\nint fadeStep = 5;\n\nvoid setup() {\n  pinMode(9, OUTPUT);\n}\n\nvoid loop() {\n  analogWrite(9, brightness);\n  brightness += fadeStep;\n  if (brightness <= 0 || brightness >= 255) {\n    fadeStep = -fadeStep;\n  }\n  delay(30);\n}'),
(1, 5, '第5章 综合项目：智能小车', '# 综合项目\n\n## 功能需求\n- 蓝牙遥控\n- 红外避障\n- 速度可调\n\n## 模块清单\n- L298N 电机驱动 x1\n- 直流减速电机 x4\n- 蓝牙模块 HC-05 x1\n- 红外传感器 x2\n\n## 电路连接\n详见课程附件 PDF 文档。'),
(2, 1, '第1章 Python 环境准备', '# Python 安装\n\n推荐使用 Anaconda 发行版，内置数据科学所需的大部分库。\n\n## 创建虚拟环境\n```bash\nconda create -n data python=3.10\nconda activate data\n```\n\n## 安装依赖\n```bash\npip install numpy pandas matplotlib seaborn scikit-learn jupyter\n```'),
(2, 2, '第2章 NumPy 基础', '# NumPy 数组操作\n\n```python\nimport numpy as np\n\n# 创建数组\na = np.array([1, 2, 3, 4, 5])\nb = np.arange(0, 10, 2)\n\n# 数学运算\nprint(np.mean(a))\nprint(np.std(a))\n\n# 矩阵运算\nA = np.array([[1, 2], [3, 4]])\nB = np.array([[5, 6], [7, 8]])\nprint(np.dot(A, B))\n```'),
(2, 3, '第3章 Pandas 数据处理', '# Pandas 核心\n\n```python\nimport pandas as pd\n\n# 读取数据\ndf = pd.read_csv(\"data.csv\")\n\n# 数据探索\nprint(df.head())\nprint(df.describe())\nprint(df.info())\n\n# 数据清洗\ndf = df.dropna()  # 删除缺失值\ndf = df[df[\"age\"] > 0]  # 过滤异常值\n\n# 分组统计\nresult = df.groupby(\"category\")[\"sales\"].sum().reset_index()\n```'),
(3, 1, '第1章 气息控制基础', '# 气息训练\n\n## 腹式呼吸\n1. 站立放松，双手置于腹部\n2. 用鼻子吸气，感受腹部向外扩张\n3. 保持 3 秒后用嘴缓慢呼气\n4. 重复 10 次为一组\n\n## 常见问题\n- 肩膀不要上抬\n- 吸气要深而饱满\n- 呼气要均匀平稳'),
(3, 2, '第2章 发声位置与共鸣', '# 共鸣练习\n\n## 胸腔共鸣\n练习低音区发声，感受胸部震动。\n\n## 鼻腔共鸣\n哼唱 \"m\" 音，感受鼻腔震动。\n\n## 头腔共鸣\n发 \"i\" 母音，逐步升高音高。'),
(4, 1, '第1章 水彩画材料介绍', '# 画材清单\n\n## 颜料\n- 固体水彩（推荐初学者）\n- 管装水彩（色彩浓郁）\n\n## 画笔\n- 圆头笔：细节刻画\n- 平头笔：大面积铺色\n- 斜头笔：边缘处理\n\n## 纸张\n- 粗纹：适合风景\n- 中纹：通用型\n- 细纹：适合人物与细节'),
(4, 2, '第2章 基本技法练习', '# 三种基础技法\n\n## 1. 平涂法\n大面积均匀上色，注意水量控制。\n\n## 2. 渐变法\n湿画法过渡，一种颜色向另一种颜色自然融合。\n\n## 3. 叠色法\n干透后再上第二层色，用于增加层次感。');

-- 进度测试数据
INSERT INTO chapter_progress (user_id, chapter_id, completed, complete_time) VALUES
(5, 1, 1, '2024-03-10 10:30:00'),
(5, 2, 1, '2024-03-12 14:20:00'),
(5, 3, 1, '2024-03-15 09:15:00'),
(5, 4, 0, NULL),
(5, 5, 0, NULL),
(8, 1, 1, '2024-03-08 16:00:00'),
(8, 2, 0, NULL),
(6, 7, 1, '2024-03-18 11:00:00'),
(6, 8, 1, '2024-03-20 15:30:00'),
(6, 9, 0, NULL);

-- 场地表
CREATE TABLE IF NOT EXISTS venues (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL COMMENT '场地名称',
    capacity INT NOT NULL DEFAULT 50 COMMENT '容纳人数',
    location VARCHAR(255) NOT NULL COMMENT '位置描述',
    status ENUM('AVAILABLE', 'UNAVAILABLE') NOT NULL DEFAULT 'AVAILABLE' COMMENT '状态: 可用/不可用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted TINYINT(1) DEFAULT 0 COMMENT '逻辑删除',
    INDEX idx_status (status)
) COMMENT='场地表';

-- 场地预约表
CREATE TABLE IF NOT EXISTS venue_bookings (
    id INT AUTO_INCREMENT PRIMARY KEY,
    venue_id INT NOT NULL COMMENT '场地ID',
    club_id INT NOT NULL COMMENT '社团ID',
    start_time DATETIME NOT NULL COMMENT '开始时间',
    end_time DATETIME NOT NULL COMMENT '结束时间',
    purpose VARCHAR(500) NOT NULL COMMENT '使用目的',
    status ENUM('PENDING', 'APPROVED', 'REJECTED') NOT NULL DEFAULT 'PENDING' COMMENT '状态: 待审批/已通过/已驳回',
    applicant_id INT NOT NULL COMMENT '申请人ID',
    auditor_id INT DEFAULT NULL COMMENT '审批人ID',
    reject_reason VARCHAR(500) DEFAULT NULL COMMENT '驳回原因',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted TINYINT(1) DEFAULT 0 COMMENT '逻辑删除',
    FOREIGN KEY (venue_id) REFERENCES venues(id),
    FOREIGN KEY (club_id) REFERENCES clubs(id),
    FOREIGN KEY (applicant_id) REFERENCES users(id),
    FOREIGN KEY (auditor_id) REFERENCES users(id),
    INDEX idx_venue_id (venue_id),
    INDEX idx_club_id (club_id),
    INDEX idx_status (status),
    INDEX idx_time_range (start_time, end_time)
) COMMENT='场地预约表';

-- 场地测试数据
INSERT INTO venues (id, name, capacity, location, status) VALUES
(1, '大礼堂', 500, '学校东区综合楼1层', 'AVAILABLE'),
(2, '多功能报告厅', 200, '科技楼2层201', 'AVAILABLE'),
(3, '舞蹈排练厅', 50, '艺术楼3层302', 'AVAILABLE'),
(4, '会议室A', 30, '行政楼4层401', 'AVAILABLE'),
(5, '会议室B', 20, '行政楼4层402', 'AVAILABLE'),
(6, '室外篮球场', 100, '体育中心西侧', 'AVAILABLE');

-- 场地预约测试数据
INSERT INTO venue_bookings (venue_id, club_id, start_time, end_time, purpose, status, applicant_id, auditor_id, reject_reason) VALUES
(1, 1, '2024-05-20 14:00:00', '2024-05-20 18:00:00', '编程大赛决赛', 'APPROVED', 3, 1, NULL),
(2, 2, '2024-05-21 09:00:00', '2024-05-21 12:00:00', '声乐公开课', 'APPROVED', 4, 1, NULL),
(3, 2, '2024-05-22 15:00:00', '2024-05-22 18:00:00', '舞蹈队训练', 'PENDING', 4, NULL, NULL),
(4, 1, '2024-05-23 10:00:00', '2024-05-23 12:00:00', '社团例会', 'REJECTED', 3, 2, '该时段已有其他社团预约');
