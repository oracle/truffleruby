require 'sqlite3'

db = SQLite3::Database.new(':memory:')

db.execute('CREATE TABLE test(id INTEGER, name VARCHAR(20) NOT NULL)')
db.execute('INSERT INTO test VALUES(1, "Alice")')
db.execute('INSERT INTO test VALUES(2, "Bob")')

raise unless db.execute('SELECT name FROM test ORDER BY name ASC') == [['Alice'], ['Bob']]
