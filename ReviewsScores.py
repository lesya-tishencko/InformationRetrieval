import indicoio
import psycopg2

indicoio.config.api_key = '90425f7823ad29d8b85bdfeb8e5255ed'
connection = psycopg2.connect("dbname='postgres' user='postgres' host='localhost' " + \
                  "password='1234509876'")

cursor = connection.cursor()
cursor.execute("SELECT id, review FROM reviews;")
reviews = cursor.fetchall()
for review in reviews:
    score = indicoio.sentiment(review[1].strip())
    cursor.execute("UPDATE reviews SET score  = " + str(score) + "WHERE id = " + str(review[0]) + ";")

cursor.close()
connection.close()