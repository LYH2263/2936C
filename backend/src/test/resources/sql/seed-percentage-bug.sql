INSERT INTO users (id, username, password, role, full_name, created_at) VALUES (1, 'teacher1', '$2a$10$dXJ3SW6G7P50lGmMQgel6uVktDQd7hF1YJgPbtrC8hO8U3Yq5S3Hi', 'TEACHER', '教师一', CURRENT_TIMESTAMP);
INSERT INTO users (id, username, password, role, full_name, created_at) VALUES (2, 'student_80pct', '$2a$10$dXJ3SW6G7P50lGmMQgel6uVktDQd7hF1YJgPbtrC8hO8U3Yq5S3Hi', 'STUDENT', '80%同学', CURRENT_TIMESTAMP);
INSERT INTO users (id, username, password, role, full_name, created_at) VALUES (3, 'student_60pct', '$2a$10$dXJ3SW6G7P50lGmMQgel6uVktDQd7hF1YJgPbtrC8hO8U3Yq5S3Hi', 'STUDENT', '60%同学', CURRENT_TIMESTAMP);

INSERT INTO exams (id, title, state, duration, target_audience, creator_id) VALUES (2, '满分50考试-百分比分布Bug回归', 'PUBLISHED', 90, 'ALL', 1);

INSERT INTO questions (id, content, type, answer, default_score, creator_id) VALUES (6, '50分考试-题1', 'SINGLE', 'A', 10, 1);
INSERT INTO questions (id, content, type, answer, default_score, creator_id) VALUES (7, '50分考试-题2', 'SINGLE', 'B', 10, 1);
INSERT INTO questions (id, content, type, answer, default_score, creator_id) VALUES (8, '50分考试-题3', 'JUDGE', 'TRUE', 10, 1);
INSERT INTO questions (id, content, type, answer, default_score, creator_id) VALUES (9, '50分考试-题4', 'SINGLE', 'C', 10, 1);
INSERT INTO questions (id, content, type, answer, default_score, creator_id) VALUES (10, '50分考试-题5', 'MULTI', 'AB', 10, 1);

INSERT INTO exam_questions (id, exam_id, question_id, score, sequence) VALUES (6, 2, 6, 10, 1);
INSERT INTO exam_questions (id, exam_id, question_id, score, sequence) VALUES (7, 2, 7, 10, 2);
INSERT INTO exam_questions (id, exam_id, question_id, score, sequence) VALUES (8, 2, 8, 10, 3);
INSERT INTO exam_questions (id, exam_id, question_id, score, sequence) VALUES (9, 2, 9, 10, 4);
INSERT INTO exam_questions (id, exam_id, question_id, score, sequence) VALUES (10, 2, 10, 10, 5);

INSERT INTO submissions (id, exam_id, student_id, score, state, start_time, end_time, tab_switch_count) VALUES (1, 2, 2, 40, 'SUBMITTED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);
INSERT INTO submissions (id, exam_id, student_id, score, state, start_time, end_time, tab_switch_count) VALUES (2, 2, 3, 30, 'SUBMITTED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);

INSERT INTO submission_answers (id, submission_id, question_id, score, student_answer) VALUES (1, 1, 6, 10, 'A');
INSERT INTO submission_answers (id, submission_id, question_id, score, student_answer) VALUES (2, 1, 7, 10, 'B');
INSERT INTO submission_answers (id, submission_id, question_id, score, student_answer) VALUES (3, 1, 8, 10, 'TRUE');
INSERT INTO submission_answers (id, submission_id, question_id, score, student_answer) VALUES (4, 1, 9, 10, 'C');
INSERT INTO submission_answers (id, submission_id, question_id, score, student_answer) VALUES (5, 1, 10, 0, 'C');

INSERT INTO submission_answers (id, submission_id, question_id, score, student_answer) VALUES (6, 2, 6, 10, 'A');
INSERT INTO submission_answers (id, submission_id, question_id, score, student_answer) VALUES (7, 2, 7, 10, 'B');
INSERT INTO submission_answers (id, submission_id, question_id, score, student_answer) VALUES (8, 2, 8, 10, 'TRUE');
INSERT INTO submission_answers (id, submission_id, question_id, score, student_answer) VALUES (9, 2, 9, 0, 'D');
INSERT INTO submission_answers (id, submission_id, question_id, score, student_answer) VALUES (10, 2, 10, 0, 'D');
