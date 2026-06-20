INSERT INTO users (id, username, password, role, full_name, created_at) VALUES (1, 'teacher1', '$2a$10$dXJ3SW6G7P50lGmMQgel6uVktDQd7hF1YJgPbtrC8hO8U3Yq5S3Hi', 'TEACHER', '教师一', CURRENT_TIMESTAMP);
INSERT INTO users (id, username, password, role, full_name, created_at) VALUES (2, 'student_a', '$2a$10$dXJ3SW6G7P50lGmMQgel6uVktDQd7hF1YJgPbtrC8hO8U3Yq5S3Hi', 'STUDENT', '学生甲', CURRENT_TIMESTAMP);
INSERT INTO users (id, username, password, role, full_name, created_at) VALUES (3, 'student_b', '$2a$10$dXJ3SW6G7P50lGmMQgel6uVktDQd7hF1YJgPbtrC8hO8U3Yq5S3Hi', 'STUDENT', '学生乙', CURRENT_TIMESTAMP);
INSERT INTO users (id, username, password, role, full_name, created_at) VALUES (4, 'student_c', '$2a$10$dXJ3SW6G7P50lGmMQgel6uVktDQd7hF1YJgPbtrC8hO8U3Yq5S3Hi', 'STUDENT', '学生丙', CURRENT_TIMESTAMP);

INSERT INTO exams (id, title, state, duration, target_audience, creator_id) VALUES (1, '零提交测试考试', 'PUBLISHED', 90, 'ALL', 1);

INSERT INTO questions (id, content, type, answer, default_score, creator_id) VALUES (1, '题目1', 'SINGLE', 'A', 20, 1);
INSERT INTO questions (id, content, type, answer, default_score, creator_id) VALUES (2, '题目2', 'SINGLE', 'B', 20, 1);
INSERT INTO questions (id, content, type, answer, default_score, creator_id) VALUES (3, '题目3', 'JUDGE', 'TRUE', 20, 1);
INSERT INTO questions (id, content, type, answer, default_score, creator_id) VALUES (4, '题目4', 'SINGLE', 'C', 20, 1);
INSERT INTO questions (id, content, type, answer, default_score, creator_id) VALUES (5, '题目5', 'MULTI', 'AB', 20, 1);

INSERT INTO exam_questions (id, exam_id, question_id, score, sequence) VALUES (1, 1, 1, 20, 1);
INSERT INTO exam_questions (id, exam_id, question_id, score, sequence) VALUES (2, 1, 2, 20, 2);
INSERT INTO exam_questions (id, exam_id, question_id, score, sequence) VALUES (3, 1, 3, 20, 3);
INSERT INTO exam_questions (id, exam_id, question_id, score, sequence) VALUES (4, 1, 4, 20, 4);
INSERT INTO exam_questions (id, exam_id, question_id, score, sequence) VALUES (5, 1, 5, 20, 5);
