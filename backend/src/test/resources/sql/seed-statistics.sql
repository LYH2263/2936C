INSERT INTO users (id, username, password, role, full_name, created_at) VALUES (1, 'teacher1', '$2a$10$dXJ3SW6G7P50lGmMQgel6uVktDQd7hF1YJgPbtrC8hO8U3Yq5S3Hi', 'TEACHER', '教师一', CURRENT_TIMESTAMP);
INSERT INTO users (id, username, password, role, full_name, created_at) VALUES (2, 'student_pass', '$2a$10$dXJ3SW6G7P50lGmMQgel6uVktDQd7hF1YJgPbtrC8hO8U3Yq5S3Hi', 'STUDENT', '及格同学', CURRENT_TIMESTAMP);
INSERT INTO users (id, username, password, role, full_name, created_at) VALUES (3, 'student_fail', '$2a$10$dXJ3SW6G7P50lGmMQgel6uVktDQd7hF1YJgPbtrC8hO8U3Yq5S3Hi', 'STUDENT', '不及格同学', CURRENT_TIMESTAMP);
INSERT INTO users (id, username, password, role, full_name, created_at) VALUES (4, 'student_absent', '$2a$10$dXJ3SW6G7P50lGmMQgel6uVktDQd7hF1YJgPbtrC8hO8U3Yq5S3Hi', 'STUDENT', '缺考同学', CURRENT_TIMESTAMP);

INSERT INTO exams (id, title, state, duration, target_audience, creator_id) VALUES (1, '统计测试考试', 'PUBLISHED', 90, 'ALL', 1);

INSERT INTO questions (id, content, type, answer, default_score, creator_id) VALUES (1, '题目1-单选', 'SINGLE', 'A', 20, 1);
INSERT INTO questions (id, content, type, answer, default_score, creator_id) VALUES (2, '题目2-单选', 'SINGLE', 'B', 20, 1);
INSERT INTO questions (id, content, type, answer, default_score, creator_id) VALUES (3, '题目3-判断', 'JUDGE', 'TRUE', 20, 1);
INSERT INTO questions (id, content, type, answer, default_score, creator_id) VALUES (4, '题目4-单选', 'SINGLE', 'C', 20, 1);
INSERT INTO questions (id, content, type, answer, default_score, creator_id) VALUES (5, '题目5-多选', 'MULTI', 'AB', 20, 1);

INSERT INTO exam_questions (id, exam_id, question_id, score, sequence) VALUES (1, 1, 1, 20, 1);
INSERT INTO exam_questions (id, exam_id, question_id, score, sequence) VALUES (2, 1, 2, 20, 2);
INSERT INTO exam_questions (id, exam_id, question_id, score, sequence) VALUES (3, 1, 3, 20, 3);
INSERT INTO exam_questions (id, exam_id, question_id, score, sequence) VALUES (4, 1, 4, 20, 4);
INSERT INTO exam_questions (id, exam_id, question_id, score, sequence) VALUES (5, 1, 5, 20, 5);

INSERT INTO submissions (id, exam_id, student_id, score, state, start_time, end_time, tab_switch_count) VALUES (1, 1, 2, 75, 'SUBMITTED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);
INSERT INTO submissions (id, exam_id, student_id, score, state, start_time, end_time, tab_switch_count) VALUES (2, 1, 3, 45, 'SUBMITTED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);

INSERT INTO submission_answers (id, submission_id, question_id, score, student_answer) VALUES (1, 1, 1, 20, 'A');
INSERT INTO submission_answers (id, submission_id, question_id, score, student_answer) VALUES (2, 1, 2, 20, 'B');
INSERT INTO submission_answers (id, submission_id, question_id, score, student_answer) VALUES (3, 1, 3, 15, 'TRUE');
INSERT INTO submission_answers (id, submission_id, question_id, score, student_answer) VALUES (4, 1, 4, 20, 'C');
INSERT INTO submission_answers (id, submission_id, question_id, score, student_answer) VALUES (5, 1, 5, 0, 'C');

INSERT INTO submission_answers (id, submission_id, question_id, score, student_answer) VALUES (6, 2, 1, 20, 'A');
INSERT INTO submission_answers (id, submission_id, question_id, score, student_answer) VALUES (7, 2, 2, 10, 'A');
INSERT INTO submission_answers (id, submission_id, question_id, score, student_answer) VALUES (8, 2, 3, 0, 'FALSE');
INSERT INTO submission_answers (id, submission_id, question_id, score, student_answer) VALUES (9, 2, 4, 15, 'C');
INSERT INTO submission_answers (id, submission_id, question_id, score, student_answer) VALUES (10, 2, 5, 0, 'D');
