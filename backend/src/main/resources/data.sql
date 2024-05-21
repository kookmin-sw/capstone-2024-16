insert into room(can_chat, frequency, num_of_vote_success, end_day, image_id, max_user, now_user, category, certification_type, info, name, notice, password, periodicity, is_full, room_type) values (true, 1, 1, now(), 1, 1, 1, 'STUDY', 'ADMIN', 'hi', '테스트 공부방', 'hi','123', 'DAILY', true, 'AI');
insert into room(can_chat, frequency, num_of_vote_success, end_day, image_id, max_user, now_user, category, certification_type, info, name, notice, password, periodicity, is_full, room_type) values (true, 1, 1, now(), 1, 1, 1, 'STUDY', 'ADMIN', 'hi', '테스트 공부방(AI X) 비밀번호 X', 'hi','', 'DAILY', true, 'NORMAL');
insert into room_user(is_manager, created_time, room_id, user_id) values (true, NOW(), 1, 1);
insert into room_user(is_manager, created_time, room_id, user_id) values (true, NOW(), 2, 1);
insert into image(url) values ('https://my-dodo-bucket.s3.ap-northeast-2.amazonaws.com/image/f584c760-3%EB%8F%84%EB%8F%84.png');
insert into image(url) values ('https://my-dodo-bucket.s3.ap-northeast-2.amazonaws.com/image/b6238ffa-5%ED%95%B4%ED%8C%8C%EB%A6%AC.png');
insert into image(url) values ('https://my-dodo-bucket.s3.ap-northeast-2.amazonaws.com/image/ab11a086-d%EB%8B%88%EB%AA%A8.png');
insert into image(url) values ('https://my-dodo-bucket.s3.ap-northeast-2.amazonaws.com/image/9e953fc0-2%EA%B0%80%EC%98%A4%EB%A6%AC.png');
insert into image(url) values ('https://my-dodo-bucket.s3.ap-northeast-2.amazonaws.com/image/4344618d-3%EB%AD%90%EC%9E%84.png');
insert into image(url) values ('https://my-dodo-bucket.s3.ap-northeast-2.amazonaws.com/image/09873afd-0%ED%95%B4%EC%B4%88.png');
insert into image(url) values ('https://my-dodo-bucket.s3.ap-northeast-2.amazonaws.com/image/1af1d927-2%EC%84%B1%EA%B2%8C.png');
insert into creature(name, price, info, image_id) values ('도도', 0, '우리의 마스코트 도도이다.', 2);
insert into creature(name, price, info, image_id) values ('퍼프리', 30, '보라색의 몽환적인 분위기를 풍기는 것이 특징이다.', 3);
insert into creature(name, price, info, image_id) values ('흰동가리', 50, '니X를 닮았지만 이름은 X모이다.', 4);
insert into creature(name, price, info, image_id) values ('스마일', 70, '놀란 거 같지만 웃고 있는 모습이다.', 5);
insert into creature(name, price, info, image_id) values ('분홍이', 150, '전 주인이 이름붙였다. 우아한 친구이다.', 6);
insert into creature(name, price, info, image_id) values ('미역과 돌', 10, '미역과 돌은 떨어질 수 없는 사이이다.', 7);
insert into creature(name, price, info, image_id) values ('뭉게', 30, '학명은 성게이지만 이름은 뭉게이다.', 8);