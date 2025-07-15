-- 투표 생성
INSERT INTO vote (title, description, start_time, end_time)
VALUES (
    '당신이 선호하는 음식 스타일은 무엇인가요?',
    '좋아하는 음식 종류를 골라주세요.',
    NOW(),
    DATE_ADD(NOW(), INTERVAL 7 DAY)
);

-- 옵션 추가 (vote_id = 1로 가정)
INSERT INTO vote_option (vote_id, option_text)
VALUES
(1, '한식'),
(1, '중식'),
(1, '일식'),
(1, '양식'),
(1, '동남아식'),
(1, '기타');


CREATE PROCEDURE simulate_votes()
BEGIN
    DECLARE i INT DEFAULT 0;
    DECLARE random_option INT;

    WHILE i < 100 DO
        -- 랜덤 옵션 ID (1~6 중 하나 선택)
        SET random_option = FLOOR(1 + (RAND() * 6));

        -- 투표 기록 삽입
        INSERT INTO vote_record (vote_id, option_id)
        VALUES (1, random_option);

        SET i = i + 1;
    END WHILE;
END;

-- CALL simulate_votes();
---------------------------------------

INSERT INTO vote (title, description, start_time, end_time)
VALUES (
    '당신이 선호하는 색은 무엇인가요?',
    '좋아하는 색을 골라주세요.',
    NOW(),
    DATE_ADD(NOW(), INTERVAL 7 DAY)
);

-- 옵션 추가 (vote_id = 1로 가정)
INSERT INTO vote_option (vote_id, option_text)
VALUES
(2, '빨강'),
(2, '노랑'),
(2, '파랑'),
(2, '검정'),
(2, '흰색'),
(2, '기타');


---------------------------------------

INSERT INTO vote (title, description, start_time, end_time)
VALUES (
    '당신이 선호하는 애완동물은 무엇인가요?',
    '키우고 싶은 애완 동물의 종류를 골라주세요.',
    NOW(),
    DATE_ADD(NOW(), INTERVAL 7 DAY)
);

-- 옵션 추가 (vote_id = 1로 가정)
INSERT INTO vote_option (vote_id, option_text)
VALUES
(3, '개'),
(3, '고양이'),
(3, '토끼'),
(3, '햄스터'),
(3, '파충류'),
(3, '기타');