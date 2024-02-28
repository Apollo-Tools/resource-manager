import asyncio
import logging
import time

import httpx
from httpx import AsyncClient

from schemas.schemas import CreateDeployment

logger = logging.getLogger('uvicorn.info')


class RmOperator:

    def __init__(self, base_url: str, token: str) -> None:
        self.base_url = base_url
        self.authorization = {"Authorization": f"Bearer {token}"}

    async def create_deployment(self, request_body: CreateDeployment) -> any:
        endpoint = f"{self.base_url}/api/deployments"

        async with httpx.AsyncClient(timeout=60.0) as client:
            result = await client.post(endpoint, json=request_body.dict(), headers=self.authorization)
            if result.status_code != 201:
                logger.warning(f'failed to create deployment, {result.text}')
                return None
            else:
                return result.json()

    async def cancel_deployment(self, deployment_id: int):
        endpoint = f"{self.base_url}/api/deployments/{deployment_id}/cancel"
        async with httpx.AsyncClient(timeout=60.0) as client:
            result = await client.patch(endpoint, headers=self.authorization)
            if result.status_code != 204:
                logger.warning(f'failed to terminate deployment, {result.text}')
            else:
                logger.info(f'Successfully triggered termination, {deployment_id}')

    async def wait_for_deployment_created(self, deployment_id: int, max_retries: int = 60) -> dict | None:
        endpoint = f"{self.base_url}/api/deployments/{deployment_id}"
        async with httpx.AsyncClient(timeout=60.0) as client:
            for i in range(0, max_retries):
                result = await client.get(endpoint, headers=self.authorization)
                if result.status_code != 200:
                    logger.warning(f'wait_for_deployment_created returned with status code {result.status_code}')
                    return None
                elif "finished_at" in result.json():
                    logger.info(f'deployment {deployment_id} is ready')
                    return result.json()
                else:
                    logger.info(f'deployment {deployment_id} not ready yet, try {i}')
                    await asyncio.sleep(5)
        logger.info(f'creation timed out')
        return None

    async def wait_for_deployment_terminated(self, deployment_id: int, max_retries: int = 60) -> int:
        endpoint = f"{self.base_url}/api/deployments/{deployment_id}"
        async with httpx.AsyncClient(timeout=60.0) as client:
            for i in range(0, max_retries):
                result = await client.get(endpoint, headers=self.authorization)
                resource_deployments = result.json()['function_resources']
                if not result.json()['function_resources']:
                    resource_deployments = result.json()['service_resources']

                if result.status_code != 200:
                    logger.warning(f'wait_for_deployment_terminated returned with status code {result.status_code}')
                    return -1
                elif resource_deployments[0]['status']['status_value'] == "TERMINATED":
                    logger.info(f'deployment {deployment_id} is terminated')
                    return resource_deployments[0]['updated_at']
                else:
                    logger.info(f'deployment {deployment_id} not yet terminated, try {i}')
                    await asyncio.sleep(5)
        logger.info(f'termination timed out')
        return -1

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
        async with httpx.AsyncClient(timeout=60.0) as client:
            url = trigger_url
            if not is_direct:
                url = f"{self.base_url}/api{trigger_url}"
            tasks = [self.trigger_function(client, url, request, is_direct) for _ in range(concurrency)]
            result = await asyncio.gather(*tasks)
            return result
