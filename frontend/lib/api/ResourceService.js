import env from '@beam-australia/react-env';
import {checkResponseOk, handleApiCall, setResult} from './ApiHandler';
const API_ROUTE = `${env('API_URL')}/resources`;

/**
 * Create a new resource.
 *
 * @param {string} name the name of the resource
 * @param {number} platformId the id of the platform
 * @param {number} regionId the id of the region
 * @param {boolean} isLockable whether a resource should be lockable for deployments or not
 * @param {string} token the access token
 * @param {function} setResource the function to set the created resource
 * @param {function} setLoading the function to set the loading state
 * @param {function} setError the function to set the error if one occurred
 */
export async function createResource(name, platformId, regionId, isLockable,
    token, setResource, setLoading, setError) {
  const apiCall = async () => {
    const response = await fetch(`${API_ROUTE}`, {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        name: name,
        platform: {
          platform_id: platformId,
        },
        region: {
          region_id: regionId,
        },
        is_lockable: isLockable,
      }),
    });
    await setResult(response, setResource);
  };
  await handleApiCall(apiCall, setLoading, setError);
}

/**
 * List all existing resources.
 *
 * @param {string} token the access token
 * @param {function} setResources the function to set the retrieved resources
 * @param {function} setLoading the function to set the loading state
 * @param {function} setError the function to set the error if one occurred
 */
export async function listResources(token, setResources, setLoading, setError) {
  const apiCall = async () => {
    const route = `${API_ROUTE}`;
    const response = await fetch(route, {
      method: 'GET',
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });
    await setResult(response, setResources);
  };
  await handleApiCall(apiCall, setLoading, setError);
}

/**
 * Get the details of a resource.
 *
 * @param {number} id the id of the resource
 * @param {string} token the access token
 * @param {function} setResource the function to set the retrieved resource
 * @param {function} setLoading the function to set the loading state
 * @param {function} setError the function to set the error if one occurred
 */
export async function getResource(id, token, setResource, setLoading, setError) {
  const apiCall = async () => {
    const response = await fetch(`${API_ROUTE}/${id}`, {
      method: 'GET',
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });
    await setResult(response, setResource);
  };
  await handleApiCall(apiCall, setLoading, setError);
}

/**
 * Get all subresources of a resource.
 *
 * @param {number} id the id of the resource
 * @param {string} token the access token
 * @param {function} setSubresources the function to set the retrieved resource
 * @param {function} setLoading the function to set the loading state
 * @param {function} setError the function to set the error if one occurred
 */
export async function listSubresources(id, token, setSubresources, setLoading, setError) {
  const apiCall = async () => {
    const response = await fetch(`${API_ROUTE}/${id}/subresources`, {
      method: 'GET',
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });
    await setResult(response, setSubresources);
  };
  await handleApiCall(apiCall, setLoading, setError);
}

/**
 * Get all resources that are locked by a deployment.
 *
 * @param {number} deploymentId the deploymentId of the deployment
 * @param {string} token the access token
 * @param {function} setResources the function to set the retrieved resource
 * @param {function} setLoading the function to set the loading state
 * @param {function} setError the function to set the error if one occurred
 */
export async function listLockedResources(deploymentId, token, setResources, setLoading, setError) {
  const apiCall = async () => {
    const response = await fetch(`${env('API_URL')}/deployments/${deploymentId}/resources/locked`, {
      method: 'GET',
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });
    await setResult(response, setResources);
  };
  await handleApiCall(apiCall, setLoading, setError);
}

/**
 * Delete an existing resource.
 *
 * @param {number} id the id of the resource
 * @param {string} token the access token
 * @param {function} setLoading the function to set the loading state
 * @param {function} setError the function to set the error if one occurred
 * @return {Promise<boolean>} true if the request was successful
 */
export async function deleteResource(id, token, setLoading, setError) {
  const apiCall = async () => {
    const response = await fetch(`${API_ROUTE}/${id}`, {
      method: 'DELETE',
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });
    return checkResponseOk(response);
  };
  return await handleApiCall(apiCall, setLoading, setError);
}

/**
 * Add metric values to an existing resource.
 *
 * @param {number} resourceId the id of the resource
 * @param {any[]} metricValues the metric values to add
 * @param {string} token the access token
 * @param {function} setLoading the function to set the loading state
 * @param {function} setError the function to set the error if one occurred
 * @return {Promise<boolean>} true if the request was successful
 */
export async function addResourceMetrics(resourceId, metricValues, token, setLoading, setError) {
  const apiCall = async () => {
    const response = await fetch(`${API_ROUTE}/${resourceId}/metrics`, {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(metricValues),
    });
    return checkResponseOk(response);
  };
  return await handleApiCall(apiCall, setLoading, setError);
}

/**
 * List all resources that conform the given service level objectives (slo).
 *
 * @param {any[]} slos the service level objectives
 * @param {string} token the access token
 * @param {function} setResources the function to set the retrieved resources
 * @param {function} setLoading the function to set the loading state
 * @param {function} setError the function to set the error if one occurred
 */
export async function listResourcesBySLOs(slos, token, setResources, setLoading, setError) {
  const apiCall = async () => {
    const route = `${API_ROUTE}/slo`;
    const response = await fetch(route, {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        slos: slos,
      }),
    });
    await setResult(response, setResources);
  };
  await handleApiCall(apiCall, setLoading, setError);
}


/**
 * Update an existing resource.
 *
 * @param {number} id the id of the resource
 * @param {boolean} isLockable whether the resource should be lockable for deployments
 * @param {string} token the access token
 * @param {function} setLoading the function to set the loading state
 * @param {function} setError the function to set the error if one occurred
 * @return {Promise<boolean>} true if the request was successful
 */
export async function updateResource(id, isLockable, token, setLoading, setError) {
  const apiCall = async () => {
    const response = await fetch(`${API_ROUTE}/${id}`, {
      method: 'PATCH',
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        is_lockable: isLockable,
      }),
    });
    return checkResponseOk(response);
  };
  return await handleApiCall(apiCall, setLoading, setError);
}
