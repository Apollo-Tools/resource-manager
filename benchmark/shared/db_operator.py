import psycopg2


class DbOperator:
    def __init__(self, dbname: str, user: str, password: str, host: str, port: int) -> None:
        self.dbname = dbname
        self.user = user
        self.password = password
        self.host = host
        self.port = port

    def update_ensemble_slo(self, ensemble_slo_id: int, new_value: float):
        with (psycopg2.connect(dbname=self.dbname, user=self.user, password=self.password, host=self.host,
                               port=self.port) as conn):
            with conn.cursor() as curs:
                curs.execute("UPDATE ensemble_slo SET value_numbers = %s "
                             "WHERE ensemble_slo_id = %s", ([new_value], ensemble_slo_id))
