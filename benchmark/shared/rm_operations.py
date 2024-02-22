import asyncio
import logging
import time

import httpx
from httpx import AsyncClient

from schemas.schemas import CreateDeployment

logger = logging.getLogger('uvicorn.error')


class RmOperator:

    def __init__(self, base_url: str, token: str) -> None:
        self.base_url = base_url
        self.authorization = {"Authorization": f"Bearer {token}"}

    async def create_deployment(self, request_body: CreateDeployment) -> any:
        endpoint = f"{self.base_url}/api/deployments"

        result = await httpx.AsyncClient().post(endpoint, json=request_body.dict(),
                                                headers=self.authorization)
        if result.status_code != 201:
            logger.warning(f'failed to create deployment, {result.text}')
            return None
        else:
            return result.json()

    async def cancel_deployment(self, deployment_id: int):
        endpoint = f"{self.base_url}/api/deployments/{deployment_id}/cancel"

        result = await httpx.AsyncClient().patch(endpoint, headers=self.authorization)
        if result.status_code != 204:
            logger.warning(f'failed to terminate deployment, {result.text}')
        else:
            logger.info(f'Successfully canceled deployment {deployment_id}')


    async def wait_for_deployment_created(self, deployment_id: int, max_retries: int = 60) -> dict | None:
        endpoint = f"{self.base_url}/api/deployments/{deployment_id}"
        for i in range(0, max_retries):
            result = await httpx.AsyncClient().get(endpoint, headers=self.authorization)
            if result.status_code != 200:
                logger.warning(f'wait_for_deployment_created returned with status code {result.status_code}')
                return None
            elif "finished_at" in result.json():
                logger.info(f'deployment {deployment_id} is ready')
                return result.json()
            else:
                logger.info(f'deployment {deployment_id} not ready yet, try {i}')
                await asyncio.sleep(5)
        return None

    async def trigger_function(self, client: AsyncClient, url: str, request: dict, is_direct: bool):
        headers = {"apollo-request-type": "rm"}
        if not is_direct:
            headers = self.authorization
        start = time.time()
        await client.post(url, json=request, headers=headers, timeout=120)
        end = time.time()
        return (end - start) * 1000

    async def trigger_function_deployment(self, trigger_url: str, concurrency: int,
                                          request: dict, is_direct: bool):
        async with httpx.AsyncClient() as client:
            url = trigger_url
            if not is_direct:
                url = f"{self.base_url}/api{trigger_url}"
            tasks = [self.trigger_function(client, url, request, is_direct) for _ in range(concurrency)]
            result = await asyncio.gather(*tasks)
            return result
