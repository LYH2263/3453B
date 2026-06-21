SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

USE club_db;

-- 社团线下据点表
CREATE TABLE IF NOT EXISTS club_locations (
    id INT AUTO_INCREMENT PRIMARY KEY,
    club_id INT NOT NULL COMMENT '所属社团ID',
    building VARCHAR(100) NOT NULL COMMENT '楼栋名称',
    floor VARCHAR(20) DEFAULT NULL COMMENT '楼层',
    room VARCHAR(50) DEFAULT NULL COMMENT '房间号',
    description TEXT COMMENT '据点描述',
    longitude DECIMAL(10, 6) DEFAULT NULL COMMENT '经度(可选)',
    latitude DECIMAL(10, 6) DEFAULT NULL COMMENT '纬度(可选)',
    open_hours VARCHAR(200) DEFAULT NULL COMMENT '开放时间描述',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted TINYINT(1) DEFAULT 0 COMMENT '逻辑删除',
    FOREIGN KEY (club_id) REFERENCES clubs(id) ON DELETE SET NULL,
    INDEX idx_club_id (club_id),
    INDEX idx_building (building)
) COMMENT='社团线下据点表';

-- 社团据点测试数据
INSERT INTO club_locations (id, club_id, building, floor, room, description, longitude, latitude, open_hours) VALUES
(1, 1, '科技楼', '1层', '102室', '极客电子社主活动场所，配备电脑、实验器材、服务器机柜，适合小型技术分享会和项目开发。', 116.407395, 39.904211, '周一至周五 14:00-22:00，周末 09:00-22:00'),
(2, 1, '科技楼', '3层', '305实验室', '机器人与嵌入式开发实验室，配备智能小车实验平台、Arduino套件、3D打印机等。', 116.407395, 39.904250, '周二、周四 18:00-22:00，周六全天'),
(3, 2, '艺术楼', '2层', '201琴房', '声乐训练专用琴房，配备专业钢琴及音响设备。', 116.407500, 39.904180, '周一至周日 09:00-21:00'),
(4, 2, '艺术楼', '3层', '302舞蹈厅', '舞蹈排练厅，全身镜、把杆、专业地胶，适合舞蹈队日常训练。', 116.407520, 39.904220, '周一、周三、周五 16:00-20:00'),
(5, 2, '艺术楼', '1层', '102排练厅', '综合排练厅，配备专业音响套装、灯光设备，可供乐队排练和小型演出使用。', 116.407480, 39.904160, '周三 19:00-22:00，周日 14:00-18:00');

SET FOREIGN_KEY_CHECKS = 1;
