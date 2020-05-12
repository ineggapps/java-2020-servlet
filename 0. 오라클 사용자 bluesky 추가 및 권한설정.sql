-- ----------------------------------------------------
-- sys 접속
cmd>sqlplus / as  sysdba
또는
cmd>sqlplus sys/"암호" as  sysdba

-- ----------------------------------------------------
-- 사용자 확인
SELECT * FROM all_users; 

-- bluesky 사용자가 존재하면 삭제
DROP USER bluesky CASCADE;

-- ----------------------------------------------------
-- 12C 이상의 버전에서 11g 방식으로 사용자 추가시 ORA-65096 오류가 발생하는 경우
 ALTER SESSION SET "_ORACLE_SCRIPT" = true;

-- sys 또는 system 계정에서
  -- 사용자 추가
  CREATE USER bluesky IDENTIFIED BY "java$!";

   -- 사용자 권한 부여 : CONN 및 테이블 작성등 기본 권한
   GRANT CONNECT, RESOURCE TO bluesky;

   -- bluesky 사용자 DEFAULT 테이블스페이스를 USERS로 변경
   ALTER USER bluesky DEFAULT TABLESPACE USERS;

   -- bluesky 사용자 TEMPORARY 테이블스페이스를 TEMP 변경
   ALTER USER bluesky TEMPORARY TABLESPACE TEMP;

   -- ORACLE 12C 이상의 버전 : USERS 테이블 스페이스 권한 부여(SYS 계정)
   ALTER USER bluesky DEFAULT TABLESPACE USERS QUOTA UNLIMITED ON USERS;


-- ----------------------------------------------------
  -- bluesky 사용자 CONN
  CONN bluesky/"java$!";
