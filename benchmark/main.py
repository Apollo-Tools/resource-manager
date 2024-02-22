from fastapi import FastAPI

from routers import routers

app = FastAPI()

app.include_router(routers.router)


@app.get("/")
async def root():
    return {"message": "Benchmark is ready"}
