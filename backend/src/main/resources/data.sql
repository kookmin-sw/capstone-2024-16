insert into room(can_chat, frequency, num_of_vote_success, end_day, image_id, max_user, now_user, category, certification_type, info, name, notice, password, periodicity, is_full, room_type) values (true, 1, 1, now(), 1, 1, 1, 'STUDY', 'ADMIN', 'hi', '테스트 공부방', 'hi','123', 'DAILY', true, 'AI');
insert into room(can_chat, frequency, num_of_vote_success, end_day, image_id, max_user, now_user, category, certification_type, info, name, notice, password, periodicity, is_full, room_type) values (true, 1, 1, now(), 1, 1, 1, 'STUDY', 'ADMIN', 'hi', '테스트 공부방(AI X) 비밀번호 X', 'hi','', 'DAILY', true, 'NORMAL');
insert into room_user(is_manager, created_time, room_id, user_id) values (true, NOW(), 1, 1);
insert into room_user(is_manager, created_time, room_id, user_id) values (true, NOW(), 2, 1);