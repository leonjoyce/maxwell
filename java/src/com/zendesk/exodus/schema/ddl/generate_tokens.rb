tokens = %w(
ADD
AFTER
ALTER
BIT
CHANGE
COLLATE
COLUMN
ENGINE
FIRST
IGNORE
MEDIUMINT
MODIFY
ONLINE
OFFLINE
SMALLINT
TABLE
TINYINT

UNSIGNED
ZEROFILL
BIT
TINYINT
CHARACTER
SET
COLLATE
SMALLINT
MEDIUMINT
INT
INTEGER
BIGINT
REAL
DOUBLE
FLOAT
DECIMAL
NUMERIC
DATE
TIME
TIMESTAMP
DATETIME
YEAR
CHAR
VARCHAR
BINARY
VARBINARY
TINYBLOB
BLOB
MEDIUMBLOB
LONGBLOB
TINYTEXT
TEXT
MEDIUMTEXT
LONGTEXT
ENUM
SET

NOT
NULL
DEFAULT

AUTO_INCREMENT
UNIQUE
PRIMARY
KEY
COMMENT
COLUMN_FORMAT
FIXED
DYNAMIC
DEFAULT
STORAGE
DISK
MEMORY

CONSTRAINT
WITH
PARSER
KEY_BLOCK_SIZE
USING
BTREE
HASH
INDEX
KEY
FULLTEXT
SPATIAL
FOREIGN

DROP

DISABLE
ENABLE
KEYS

RENAME
TO
AS

ORDER
BY

CONVERT
CHARSET


)

File.open(File.dirname(__FILE__) + "/mysql_literal_tokens.g4", "w+") do |f|
  f.puts("lexer grammar mysql_literal_tokens;")
  f.puts
  
  tokens.select { |t| !t.empty? }.sort.uniq.each do |t|
    f.puts "%s: %s;" % [t, t.split(//).map { |c| c == "_" ? "'_'" : c }.join(' ')]
  end
  
  ('A'..'Z').map do |letter|
    f.puts("fragment %s: [%s%s];" % [letter, letter, letter.downcase]);
  end 
end