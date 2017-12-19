import indicoio
import psycopg2

indicoio.config.api_key = 'e18bfb1e008fd9be403749c3c0a7833d'
connection = psycopg2.connect("dbname='postgres' user='postgres' host='localhost' " + \
                  "password='RedDress2'")

cursor = connection.cursor()
cursor.execute("SELECT id, review FROM reviews WHERE id >= 6732 AND id < 9000 AND score < 0.001;")
reviews = cursor.fetchall()
for review in reviews:
    score = 0
    if review[1].strip():
        score = indicoio.sentiment(review[1].strip())
    cursor.execute("UPDATE reviews SET score = " + str(score) + "WHERE id = " + str(review[0]) + ";")
    connection.commit()
cursor.close()
connection.close()