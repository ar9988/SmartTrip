-- Users
INSERT INTO User (id, email, password, name, profile, created_at, updated_at) VALUES
(1, 'kim@email.com', 'hash1', '김여행러', '여행 좋아하는 직장인', '2024-01-01', '2024-01-01'),
(2, 'lee@email.com', 'hash2', '이배낭', '배낭여행 전문가', '2024-01-01', '2024-01-01'),
(3, 'park@email.com', 'hash3', '박포토', '사진작가', '2024-01-01', '2024-01-01'),
(4, 'choi@email.com', 'hash4', '최맛집', '맛집탐방러', '2024-01-01', '2024-01-01'),
(5, 'jung@email.com', 'hash5', '정로드', '국내여행전문', '2024-01-01', '2024-01-01'),
(6, 'hong@email.com', 'hash6', '홍스케줄', '여행플래너', '2024-01-01', '2024-01-01');

-- Trips
INSERT INTO Trip (id, creator_id, title, start_date, end_date, created_at, updated_at) VALUES
(1, 1, '부산 해운대 여행', '2024-02-01', '2024-02-03', '2024-01-15', '2024-01-15'),
(2, 1, '제주 여행', '2024-03-01', '2024-03-04', '2024-02-01', '2024-02-01');

-- Days
INSERT INTO Day (id, trip_id, start_time, order_num) VALUES
(1, 1, '2024-02-01 09:00:00', 1),
(2, 1, '2024-02-02 09:00:00', 2),
(3, 1, '2024-02-03 09:00:00', 3),
(4, 2, '2024-03-01 09:00:00', 1),
(5, 2, '2024-03-02 09:00:00', 2),
(6, 2, '2024-03-03 09:00:00', 3),
(7, 2, '2024-03-04 09:00:00', 4);

-- Schedules
INSERT INTO Schedule (id, day_id, place_name, trip_id, order_num, lat, lng, type, position_path) VALUES
-- 부산 여행 (trip_id = 1)
-- 1일차
(1, 1, '해운대해수욕장', 1, 1, 35.1586, 129.1604, 1, 1000),
(2, 1, '광안리해수욕장', 1, 2, 35.1531, 129.1182, 1, 1250),
(3, 1, '더베이 101', 1, 3, 35.1560, 129.1486, 1, 1500),
(4, 1, '부산타워', 1, 4, 35.1011, 129.0317, 1, 1750),

-- 2일차
(5, 2, '감천문화마을', 1, 1, 35.0970, 129.0107, 1, 2000),
(6, 2, '국제시장', 1, 2, 35.1298, 129.0297, 1, 2250),
(7, 2, '자갈치시장', 1, 3, 35.0967, 129.0304, 1, 2500),
(8, 2, '부산영화체험박물관', 1, 4, 35.0951, 129.0283, 1, 2750),

-- 제주 여행 (trip_id = 2)
-- 1일차
(9, 4, '제주공항', 2, 1, 33.5067, 126.4930, 1, '1000'),
(10, 4, '성산일출봉', 2, 2, 33.4587, 126.9426, 1, '1500),
-- 2일차
(11, 5, '한라산', 2, 1, 33.3616, 126.5292, 1, '2000'),
-- 3일차
(12, 6, '만장굴', 2, 1, 33.5284, 126.7714, 1, '3000'),
(13, 6, '세화해변', 2, 2, 33.5252, 126.8589, 1, '3333'),
(14, 6, '비자림', 2, 3, 33.4894, 126.8090, 1, '3666'),
-- 4일차
(15, 7, '협재해수욕장', 2, 1, 33.3940, 126.2392, 1, '4000'),
(16, 7, '제주공항', 2, 2, 33.5067, 126.4930, 1, '4500');

-- Routes
INSERT INTO Route (id, trip_id, day_id, order_num, drive_duration, trans_duration) VALUES
(1, 1, 1, 1, 30, 45),
(2, 1, 2, 1, 25, 40),
(3, 2, 4, 1, 60, 90),
(4, 2, 5, 1, 45, 70),
(5, 1, 3, 1, 20, 35), -- 자갈치→남포동
(6, 2, 5, 2, 40, 60), -- 한라산→우도
(7, 2, 6, 1, 35, 50), -- 만장굴→세화해변
(8, 2, 6, 2, 25, 40), -- 세화해변→비자림
(9, 2, 7, 1, 55, 80), -- 협재→공항
(10, 2, 4, 2, 50, 75); -- 공항→성산일출봉


-- TripMembers
INSERT INTO trip_member (id, trip_id, user_id, is_owner) VALUES
(1, 1, 1, true),
(2, 1, 2, false),
(3, 1, 4, false),
(4, 2, 3, true),
(5, 2, 1, false),
(6, 2, 5, false),
(7, 2, 6, false);

-- PhotoAlbums
INSERT INTO photo_album (id, trip_id, image_url) VALUES
(1, 1, 'busan_album.jpg'),
(2, 2, 'jeju_album.jpg');

-- PhotoPlaces
INSERT INTO photo_place (id, album_id, name) VALUES
(1, 1, '해운대'),
(2, 1, '광안리'),
(3, 2, '성산일출봉'),
(4, 2, '한라산');

-- Photos
INSERT INTO Photo (id, album_id, photo_place_id, user_id, file_path, latitude, longitude, taken_at, uploaded_at) VALUES
(1, 1, 1, 1, 'haeundae1.jpg', 35.1586, 129.1604, '2024-02-01 13:00:00', '2024-02-01 20:00:00'),
(2, 1, 2, 2, 'gwangalli1.jpg', 35.1531, 129.1182, '2024-02-01 18:00:00', '2024-02-01 21:00:00'),
(3, 2, 3, 3, 'seongsan1.jpg', 33.4587, 126.9426, '2024-03-01 06:00:00', '2024-03-01 10:00:00'),
(4, 2, 4, 5, 'hallasan1.jpg', 33.3616, 126.5292, '2024-03-02 12:00:00', '2024-03-02 18:00:00');

-- Comments
INSERT INTO Comment (id, photo_id, author_id, content, created_at, updated_at) VALUES
(1, 1, 2, '해운대 날씨가 정말 좋네요!', '2024-02-01 21:00:00', '2024-02-01 21:00:00'),
(2, 3, 5, '일출이 너무 아름다워요', '2024-03-01 11:00:00', '2024-03-01 11:00:00'),
(3, 4, 6, '등산하느라 고생많으셨어요', '2024-03-02 19:00:00', '2024-03-02 19:00:00');