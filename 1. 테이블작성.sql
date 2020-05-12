--------------------------------------
-- 기존 테이블 삭제
DROP TABLE guest PURGE;

DROP TABLE bbsReplyLike PURGE;
DROP TABLE bbsReply PURGE;
DROP TABLE bbsLike PURGE;
DROP TABLE bbs PURGE;

DROP TABLE board PURGE;
DROP TABLE notice PURGE;
DROP TABLE photo PURGE;

DROP TABLE schedule PURGE;

DROP TABLE member2 PURGE;
DROP TABLE member1 PURGE;

-- 시퀀스 삭제
DROP SEQUENCE board_seq;

--------------------------------------
-- 12C 이상만 가능
-- GENERATED AS IDENTITY : 자동으로 숫자가 증가. 내부적으로 시퀀스 이용. 값 지정 불가

-- 회원 테이블
CREATE TABLE member1 (--탈퇴 시 지워지지 않고 enabled의 항목이 0으로 바뀔 것임.
     userId        VARCHAR2(50) NOT NULL 
     ,userName  VARCHAR2(50) NOT NULL
     ,userPwd    VARCHAR2(100) NOT NULL
     ,enabled     NUMBER(1) DEFAULT 1
     ,created_date    DATE DEFAULT SYSDATE
     ,modify_date     DATE DEFAULT SYSDATE --이 항목을 이용하여 비밀번호 저장을 언제 했는지 검사 -> 특정 주기 단위로 비밀번호 변경을 요청할 수 있는 근거가 됨.
     ,CONSTRAINT pk_member1_userId PRIMARY KEY(userId)
);

CREATE TABLE member2 ( --탈퇴 시 거래내역까지 몽땅 삭제되는 것을 방지하기 위하여 분할하여 저장함. 회원탈퇴 시 이 테이블에서의 내용은 제거됨
     userId      VARCHAR2(50) NOT NULL
     ,birth        DATE
     ,email       VARCHAR2(100)
     ,tel           VARCHAR2(20)
     ,zip          VARCHAR2(7)
     ,addr1      VARCHAR2(100)
     ,addr2      VARCHAR2(100)
     ,CONSTRAINT pk_member2_userId PRIMARY KEY(userId)
     ,CONSTRAINT fk_member2_userId FOREIGN KEY(userId)
                  REFERENCES member1(userId) ON DELETE CASCADE
);

--------------------------------------
-- 테스트 사용자 입력
INSERT INTO member1(userId, userName, userPwd) VALUES 
    ('admin', '관리자', 'admin' );
INSERT INTO member1(userId, userName, userPwd) VALUES 
    ('han', '스프링', 'han' );

COMMIT;

--------------------------------------
-- 방명록
CREATE TABLE guest(
       num NUMBER GENERATED AS IDENTITY
       ,userId VARCHAR2(50) NOT NULL
       ,content VARCHAR2(4000) NOT NULL
       ,created DATE DEFAULT SYSDATE
       ,CONSTRAINT pk_guest_num PRIMARY KEY(num)
       ,CONSTRAINT fk_guest_userId FOREIGN KEY(userId)
              REFERENCES member1(userId)
);

SELECT * FROM tab;

--------------------------------------
-- 게시판 테이블
CREATE TABLE bbs (
    num NUMBER GENERATED AS IDENTITY PRIMARY KEY
    ,userId VARCHAR2(50) NOT NULL
    ,subject VARCHAR2(250) NOT NULL
    ,content VARCHAR2(4000) NOT NULL
    ,hitCount NUMBER DEFAULT 0
    ,created DATE DEFAULT SYSDATE
    ,CONSTRAINT fk_bbs_userId FOREIGN KEY(userId)
                REFERENCES member1(userId)
);

-- 게시판 - 좋아요
CREATE TABLE bbsLike (
    num       NUMBER NOT NULL
    ,userId   VARCHAR2(50) NOT NULL
    ,PRIMARY  KEY(num, userId)
    ,FOREIGN  KEY(num) REFERENCES bbs(num) ON DELETE CASCADE
    ,FOREIGN  KEY(userId) REFERENCES member1(userId) ON DELETE CASCADE
);

CREATE TABLE bbsReply (
    replyNum  NUMBER GENERATED AS IDENTITY
    ,num      NUMBER NOT NULL
    ,userId   VARCHAR2(50) NOT NULL
    ,content  VARCHAR2(4000) NOT NULL
    ,created  DATE DEFAULT SYSDATE
    ,answer   NUMBER NOT NULL
    ,CONSTRAINT pk_bbsReply_replyNum PRIMARY KEY(replyNum)
    ,CONSTRAINT fk_bbsReply_userId FOREIGN KEY(userId)
          REFERENCES member1(userId) ON DELETE CASCADE
    ,CONSTRAINT fk_bbsReply_num FOREIGN KEY(num)
          REFERENCES bbs(num) ON DELETE CASCADE
);

-- 게시판 리플 - 좋아요/싫어요(Like/DisLike) : 컬럼명 like는 안됨
CREATE TABLE bbsReplyLike (
    replyNum  NUMBER NOT NULL
    ,userId   VARCHAR2(50) NOT NULL
    ,replyLike NUMBER(1) NOT NULL
    ,CONSTRAINT pk_bbsReplyLike_replyNum
            PRIMARY KEY(replyNum, userId)
    ,CONSTRAINT fk_bbsReplyLike_replyNum
            FOREIGN KEY(replyNum)
            REFERENCES bbsReply(replyNum) ON DELETE CASCADE
    ,CONSTRAINT fk_bbsReplyLike_userId
            FOREIGN KEY(userId)
            REFERENCES member1(userId) ON DELETE CASCADE
);

--------------------------------------
-- 답변형 게시판
CREATE TABLE board (
   boardNum NUMBER NOT NULL
   ,userId VARCHAR2(50) NOT NULL
   ,subject  VARCHAR2(255) NOT NULL
   ,content  VARCHAR2(4000) NOT NULL
   ,groupNum NUMBER NOT NULL
   ,depth    NUMBER(9) NOT NULL
   ,orderNo  NUMBER(9) NOT NULL
   ,parent   NUMBER NOT NULL
   ,hitCount NUMBER DEFAULT 0
   ,created  DATE DEFAULT SYSDATE
   ,CONSTRAINT pk_board_boardNum PRIMARY KEY(boardNum)
   ,CONSTRAINT fk_board_userId FOREIGN KEY(userId)
                REFERENCES member1(userId)
);

-- 시퀀스
CREATE SEQUENCE board_seq
    INCREMENT BY 1
    START WITH 1
    NOMAXVALUE
    NOCYCLE
    NOCACHE;
	
--------------------------------------
-- 공지사항
CREATE TABLE notice (
    num NUMBER GENERATED AS IDENTITY PRIMARY KEY
    ,notice NUMBER DEFAULT 0
    ,userId VARCHAR2(50) NOT NULL
    ,subject VARCHAR2(255) NOT NULL
    ,content VARCHAR2(4000) NOT NULL
    ,saveFilename VARCHAR2(255)
    ,originalFilename VARCHAR2(255)
    ,filesize NUMBER
    ,hitCount NUMBER DEFAULT 0
    ,created DATE DEFAULT SYSDATE
    ,CONSTRAINT fk_notice_userId FOREIGN KEY(userId)
                REFERENCES member1(userId)
);
-- notice 공지 여부

--------------------------------------
-- 포토갤러리
CREATE TABLE photo (
   num NUMBER GENERATED AS IDENTITY
   ,userId VARCHAR2(50) NOT NULL
   ,subject  VARCHAR2(255) NOT NULL
   ,content  VARCHAR2(4000) NOT NULL
   ,imageFilename  VARCHAR2(1000) NOT NULL
   ,created  DATE DEFAULT SYSDATE
   ,CONSTRAINT pk_photo_num PRIMARY KEY(num)
   ,CONSTRAINT fk_photo_userId FOREIGN KEY(userId)
                REFERENCES member1(userId)
);

--------------------------------------
-- 일정 테이블
CREATE TABLE schedule (
   num                 NUMBER GENERATED AS IDENTITY
   ,userId              VARCHAR2(50) NOT NULL
   ,subject             VARCHAR2(255) NOT NULL
   ,color                VARCHAR2(50) NOT NULL
   ,sday                VARCHAR2(10) NOT NULL
   ,eday                VARCHAR2(10)
   ,stime               VARCHAR2(5)
   ,etime               VARCHAR2(5)
   ,repeat              NUMBER(1) DEFAULT 0
   ,repeat_cycle     NUMBER(2) DEFAULT 0
   ,memo              VARCHAR2(4000)
   ,created            DATE DEFAULT SYSDATE
   ,CONSTRAINT   pk_schedule_num PRIMARY KEY(num)
   ,CONSTRAINT   fk_schedule_userId FOREIGN KEY(userId)
          REFERENCES member1(userId) ON DELETE CASCADE
);

-- color(category) : 개인일정(green), 가족일정(blue), 회사일정(tomato), 기타일정(purple)
-- start_time, end_time :시간일정이이닌경우 null
-- repeat :반복유무
     -- 0 : 반복일정이아님, 1:년반복, 2:월반복, 3:일반복
-- repeat_cycle : 반복주기