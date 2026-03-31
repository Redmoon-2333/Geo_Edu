CREATE TABLE IF NOT EXISTS knowledge (
    id VARCHAR(50) PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    content TEXT NOT NULL,
    difficulty VARCHAR(20) DEFAULT 'medium',
    page_number INTEGER,
    grade VARCHAR(20),
    chapter VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS question (
    id VARCHAR(50) PRIMARY KEY,
    knowledge_id VARCHAR(50),
    question TEXT NOT NULL,
    answer TEXT NOT NULL,
    type VARCHAR(50) DEFAULT 'default',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_question_knowledge_id ON question(knowledge_id);

CREATE TABLE IF NOT EXISTS image (
    id VARCHAR(50) PRIMARY KEY,
    path VARCHAR(500) NOT NULL,
    original_name VARCHAR(255),
    file_size BIGINT,
    mime_type VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS knowledge_image (
    knowledge_id VARCHAR(50),
    image_id VARCHAR(50),
    display_order INTEGER DEFAULT 0,
    PRIMARY KEY (knowledge_id, image_id)
);

CREATE TABLE IF NOT EXISTS chat_log (
    id VARCHAR(50) PRIMARY KEY,
    session_id VARCHAR(100),
    question TEXT NOT NULL,
    answer TEXT,
    retrieved_knowledge TEXT,
    response_time INTEGER,
    user_id VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_chat_log_session_id ON chat_log(session_id);
CREATE INDEX IF NOT EXISTS idx_chat_log_user_id ON chat_log(user_id);
CREATE INDEX IF NOT EXISTS idx_chat_log_created_at ON chat_log(created_at);

CREATE TABLE IF NOT EXISTS users (
    id VARCHAR(50) PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(50) DEFAULT 'student',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS verify_code (
    id VARCHAR(50) PRIMARY KEY,
    phone VARCHAR(20),
    code VARCHAR(10) NOT NULL,
    type VARCHAR(50),
    expire_at TIMESTAMP NOT NULL,
    used BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_verify_code_phone ON verify_code(phone);
CREATE INDEX IF NOT EXISTS idx_verify_code_expire_at ON verify_code(expire_at);

CREATE INDEX IF NOT EXISTS idx_knowledge_grade_chapter ON knowledge(grade, chapter);
CREATE INDEX IF NOT EXISTS idx_knowledge_difficulty ON knowledge(difficulty);

CREATE TABLE IF NOT EXISTS user_error_book (
    id VARCHAR(50) PRIMARY KEY,
    user_id VARCHAR(50) NOT NULL,
    question_id VARCHAR(50) NOT NULL,
    error_count INTEGER DEFAULT 1,
    last_error_time TIMESTAMP,
    is_favorited BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_error_book_user_id ON user_error_book(user_id);
CREATE INDEX IF NOT EXISTS idx_error_book_question_id ON user_error_book(question_id);

CREATE TABLE IF NOT EXISTS user_practice_record (
    id VARCHAR(50) PRIMARY KEY,
    user_id VARCHAR(50) NOT NULL,
    question_id VARCHAR(50) NOT NULL,
    knowledge_id VARCHAR(50),
    user_answer TEXT,
    is_correct BOOLEAN NOT NULL,
    difficulty VARCHAR(20),
    practice_type VARCHAR(20) DEFAULT 'exercise',
    time_taken INTEGER,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_practice_record_user_id ON user_practice_record(user_id);
CREATE INDEX IF NOT EXISTS idx_practice_record_knowledge_id ON user_practice_record(knowledge_id);
CREATE INDEX IF NOT EXISTS idx_practice_record_difficulty ON user_practice_record(difficulty);

ALTER TABLE chat_log ADD COLUMN IF NOT EXISTS session_type VARCHAR(20) DEFAULT 'qa';
ALTER TABLE chat_log ADD COLUMN IF NOT EXISTS is_from_error_book BOOLEAN DEFAULT FALSE;
