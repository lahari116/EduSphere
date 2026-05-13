-- EduSphere Database Initialization Script
-- Run this as MySQL root before starting any service

CREATE DATABASE IF NOT EXISTS edusphere_iam       CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS edusphere_course     CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS edusphere_enrollment CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS edusphere_assignment CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS edusphere_exam       CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS edusphere_attendance CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS edusphere_notification CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS edusphere_analytics  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS edusphere_audit      CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Grant all privileges to root (adjust user/password as needed)
GRANT ALL PRIVILEGES ON edusphere_iam.*        TO 'root'@'localhost';
GRANT ALL PRIVILEGES ON edusphere_course.*     TO 'root'@'localhost';
GRANT ALL PRIVILEGES ON edusphere_enrollment.* TO 'root'@'localhost';
GRANT ALL PRIVILEGES ON edusphere_assignment.* TO 'root'@'localhost';
GRANT ALL PRIVILEGES ON edusphere_exam.*       TO 'root'@'localhost';
GRANT ALL PRIVILEGES ON edusphere_attendance.* TO 'root'@'localhost';
GRANT ALL PRIVILEGES ON edusphere_notification.* TO 'root'@'localhost';
GRANT ALL PRIVILEGES ON edusphere_analytics.*  TO 'root'@'localhost';
GRANT ALL PRIVILEGES ON edusphere_audit.*      TO 'root'@'localhost';

FLUSH PRIVILEGES;

SELECT 'EduSphere databases created successfully.' AS status;
