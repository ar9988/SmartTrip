-- 1. User 데이터
INSERT INTO user (email, password_hash, nickname, profile, created_at, updated_at) VALUES
('kim@example.com', 'hash1234abcd', '부산킴', '부산 여행 매니아입니다', '2025-01-01 10:00:00', '2025-01-01 10:00:00'),
('lee@example.com', 'hash5678efgh', '여행러', '맛집 탐방이 취미에요', '2025-01-05 14:30:00', '2025-01-05 14:30:00'),
('park@example.com', 'hash9012ijkl', '맛집헌터', '전국 맛집 리뷰어', '2025-01-10 09:15:00', '2025-01-10 09:15:00'),
('choi@example.com', 'hash3456mnop', '포토그래퍼초이', '여행 사진 전문가', '2025-01-15 16:45:00', '2025-01-15 16:45:00'),
('hong@example.com', 'hash7890qrst', '부산러버', '부산 현지 가이드입니다', '2025-01-20 11:20:00', '2025-01-20 11:20:00');

-- 2. Trip 데이터
INSERT INTO trip (creator_id, title, start_date, end_date, created_at, updated_at) VALUES
(1, '부산 2박 3일 여행', '2025-02-01 10:00:00', '2025-02-03 18:00:00', '2025-01-22 14:30:00', '2025-01-22 14:30:00');

-- 3. Schedule 데이터
INSERT INTO schedule (place_name, trip_id, order_num, day, lat, lng, type) VALUES
('부산역', 1, 1, 1, 35.1150, 129.0420, 1),
('자갈치시장', 1, 2, 1, 35.0967, 129.0305, 2),
('용두산공원', 1, 3, 1, 35.1002, 129.0327, 2),
('광안리해수욕장', 1, 4, 1, 35.1533, 129.1187, 2),
('서면숙소', 1, 5, 1, 35.1581, 129.0584, 3),
('해운대해수욕장', 1, 1, 2, 35.1586, 129.1603, 2),
('동백섬', 1, 2, 2, 35.1530, 129.1530, 2),
('센텀시티', 1, 3, 2, 35.1689, 129.1312, 2),
('마린시티', 1, 4, 2, 35.1566, 129.1457, 2),
('서면숙소', 1, 5, 2, 35.1581, 129.0584, 3),
('감천문화마을', 1, 1, 3, 35.0947, 129.0102, 2),
('태종대', 1, 2, 3, 35.0513, 129.0873, 2),
('부산역', 1, 3, 3, 35.1150, 129.0420, 1);

-- 4. Route 데이터
INSERT INTO route (trip_id, day, order_num, drive_duration, trans_duration) VALUES
(1, 1, 1, 0, 0),
(1, 1, 2, 15, 25),
(1, 1, 3, 10, 15),
(1, 1, 4, 30, 45),
(1, 1, 5, 20, 30),
(1, 2, 1, 25, 40),
(1, 2, 2, 10, 15),
(1, 2, 3, 15, 20),
(1, 2, 4, 10, 15),
(1, 2, 5, 25, 40),
(1, 3, 1, 30, 45),
(1, 3, 2, 35, 50),
(1, 3, 3, 25, 40);

-- 5. TripMember 데이터
INSERT INTO trip_member (trip_id, user_id, is_owner) VALUES
(1, 1, true),
(1, 2, false),
(1, 3, false);

-- 6. PhotoAlbum 데이터
INSERT INTO photo_album (trip_id, image_url) VALUES
(1, 'https://storage.example.com/busan-trip-album-1');

-- 7. PhotoPlace 데이터
INSERT INTO photo_place (album_id, name) VALUES
(1, '지역1'),
(1, '지역2'),
(1, '지역3');

-- 8. Photo 데이터
INSERT INTO photo (album_id, photo_place_id, user_id, file_path, latitude, longitude, taken_at, uploaded_at) VALUES
(1, 1, 1, '/photos/2025/02/01/beach1.jpg', 35.1586, 129.1603, '2025-02-01 14:30:00', '2025-02-01 14:35:00'),
(1, 2, 2, '/photos/2025/02/01/bridge1.jpg', 35.1533, 129.1187, '2025-02-01 19:00:00', '2025-02-01 19:05:00'),
(1, 3, 3, '/photos/2025/02/03/cliff1.jpg', 35.0513, 129.0873, '2025-02-03 11:00:00', '2025-02-03 11:10:00');

-- 9. Comment 데이터
INSERT INTO comment (photo_id, author_id, content, created_at, updated_at) VALUES
(1, 2, '해운대 뷰가 진짜 끝내주네요! 다음에 꼭 가보고 싶어요', '2025-02-01 15:00:00', '2025-02-01 15:00:00'),
(1, 3, '날씨도 좋고 완벽한 사진이에요~', '2025-02-01 15:30:00', '2025-02-01 15:30:00'),
(2, 1, '야경이 정말 예쁘네요! 인생샷 건지셨어요', '2025-02-01 20:00:00', '2025-02-01 20:00:00');