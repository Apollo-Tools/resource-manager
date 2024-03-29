import env from '@beam-australia/react-env';
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
 * @param {function} setError the function to set the error if one occurred
 */
export async function createResource(name, platformId, regionId, isLockable, token, setResource, setError) {
  try {
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
    const data = await response.json();
    setResource(() => data);
  } catch (error) {
    setError(true);
    console.log(error);
  }
}

/**
 * List all existing resources.
 *
 * @param {string} token the access token
 * @param {function} setResources the function to set the retrieved resources
 * @param {function} setError the function to set the error if one occurred
 */
export async function listResources(token, setResources, setError) {
  try {
    const route = `${API_ROUTE}`;
    const response = await fetch(route, {
      method: 'GET',
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });
    const data = await response.json();
    setResources(() => data);
  } catch (error) {
    setError(true);
    console.log(error);
  }
}

/**
 * Get the details of a resource.
 *
 * @param {number} id the id of the resource
 * @param {string} token the access token
 * @param {function} setResource the function to set the retrieved resource
 * @param {function} setError the function to set the error if one occurred
 */
export async function getResource(id, token, setResource, setError) {
  try {
    const response = await fetch(`${API_ROUTE}/${id}`, {
      method: 'GET',
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });
    const data = await response.json();
    setResource(() => data);
  } catch (error) {
    setError(true);
    console.log(error);
  }
}

/**
 * Get all subresources of a resource.
 *
 * @param {number} id the id of the resource
 * @param {string} token the access token
 * @param {function} setSubresources the function to set the retrieved resource
 * @param {function} setError the function to set the error if one occurred
 */
export async function listSubresources(id, token, setSubresources, setError) {
  try {
    const response = await fetch(`${API_ROUTE}/${id}/subresources`, {
      method: 'GET',
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });
    const data = await response.json();
    setSubresources(() => data);
  } catch (error) {
    setError(true);
    console.log(error);
  }
}

/**
 * Get all resources that are locked by a deployment.
 *
 * @param {number} deploymentId the deploymentId of the deployment
 * @param {string} token the access token
 * @param {function} setResources the function to set the retrieved resource
 * @param {function} setError the function to set the error if one occurred
 */
export async function listLockedResources(deploymentId, token, setResources, setError) {
  try {
    const response = await fetch(`${env('API_URL')}/deployments/${deploymentId}/resources/locked`, {
      method: 'GET',
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });
    const data = await response.json();
    setResources(() => data);
  } catch (error) {
    setError(true);
    console.log(error);
  }
}

/**
 * Delete an existing resource.
 *
 * @param {number} id the id of the resource
 * @param {string} token the access token
 * @param {function} setError the function to set the error if one occurred
 * @return {Promise<boolean>} true if the request was successful
 */
export async function deleteResource(id, token, setError) {
  try {
    const response = await fetch(`${API_ROUTE}/${id}`, {
      method: 'DELETE',
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });
    return response.ok;
  } catch (error) {
    setError(true);
    console.log(error);
  }
}

/**
 * Add metric values to an existing resource.
 *
 * @param {number} resourceId the id of the resource
 * @param {any[]} metricValues the metric values to add
 * @param {string} token the access token
 * @param {function} setError the function to set the error if one occurred
 */
export async function addResourceMetrics(resourceId, metricValues, token, setError) {
  try {
    const response = await fetch(`${API_ROUTE}/${resourceId}/metrics`, {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(metricValues),
    });
    await response;
  } catch (error) {
    setError(true);
    console.log(error);
  }
}

/**
 * List all resources that conform the given service level objectives (slo).
 *
 * @param {any[]} slos the service level objectives
 * @param {string} token the access token
 * @param {function} setResources the function to set the retrieved resources
 * @param {function} setError the function to set the error if one occurred
 */
export async function listResourcesBySLOs(slos, token, setResources, setError) {
  try {
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
    const data = await response.json();
    setResources(() => data);
  } catch (error) {
    setError(true);
    console.log(error);
  }
}


/**
 * Update an existing resource.
 *
 * @param {number} id the id of the resource
 * @param {boolean} isLockable whether the resource should be lockable for deployments
 * @param {string} token the access token
 * @param {function} setError the function to set the error if one occurred
 * @return {Promise<boolean>} true if the request was successful
 */
export async function updateResource(id, isLockable, token, setError) {
  try {
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
    return response.ok;
  } catch (error) {
    setError(true);
    console.log(error);
  }
}
