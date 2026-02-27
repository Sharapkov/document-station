### Пример запроса на получение документа по ID с автором и историей (и авторами истории)

explain SELECT d.*, u.*, h.*, hu.*
FROM doc d
LEFT JOIN users u ON d.user_id = u.id
LEFT JOIN history h ON h.doc_id = d.id
LEFT JOIN users hu ON h.user_id = hu.id
WHERE d.id = 113111251;

### добавленные индексы:

- первичный ключ doc(id)
- Внешний ключ history(doc_id)
- Внешний ключ history(user_id)
- Внешний ключ doc(user_id)


### explain analyze:
Nested Loop Left Join  (cost=0.57..33.03 rows=1 width=1461)

->  Nested Loop Left Join  (cost=0.43..24.74 rows=1 width=819)

->  Nested Loop Left Join  (cost=0.43..16.60 rows=1 width=726)

->  Index Scan using doc_pkey on doc d  (cost=0.29..8.31 rows=1 width=84)
Index Cond: (id = 113111251)

->  Index Scan using users_pkey on users u  (cost=0.14..8.16 rows=1 width=642)
Index Cond: (id = d.user_id)

->  Seq Scan on history h  (cost=0.00..8.12 rows=1 width=93)
Filter: (doc_id = 113111251)

->  Index Scan using users_pkey on users hu  (cost=0.14..8.16 rows=1 width=642)
Index Cond: (id = h.user_id)