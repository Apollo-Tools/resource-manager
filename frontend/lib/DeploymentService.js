import env from '@beam-australia/react-env';
const API_ROUTE = `${env('API_URL')}/deployments`;

/**
 * List all deployments of the currently logged-in user.
 *
 * @param {string} token the access token
 * @param {function} setDeployments the function to set the retrieved deployments
 * @param {function} setError the function to set the error if one occurred
 */
export async function listMyDeployments(token, setDeployments, setError) {
  try {
    const response = await fetch(`${API_ROUTE}`, {
      method: 'GET',
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });
    const data = await response.json();
    setDeployments(() => data);
  } catch (error) {
    setError(true);
    console.log(error);
  }
}

/**
 * List all logs from a deployment
 *
 * @param {number} deploymentId the id of the deployment
 * @param {string} token the access token
 * @param {function} setDeploymentLogs the function to set the retrieved logs
 * @param {function} setError the function to set the error if one occurred
 */
export async function listDeploymentLogs(deploymentId, token, setDeploymentLogs, setError) {
  try {
    const response = await fetch(`${API_ROUTE}/${deploymentId}/logs`, {
      method: 'GET',
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });
    const data = await response.json();
    setDeploymentLogs(() => data);
  } catch (error) {
    setError(true);
    console.log(error);
  }
}

/**
 * Deploy resources.
 *
 * @param {object} requestBody the request body
 * @param {string} token the access token
 * @param {function} setNewDeployment the function to set the created deployment
 * @param {function} setError the function to set the error if one occurred
 */
export async function deployResources(
    requestBody,
    token,
    setNewDeployment,
    setError,
) {
  try {
    const response = await fetch(`${API_ROUTE}`, {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(requestBody),
    });
    const data = await response.json();
    setNewDeployment(() => data);
  } catch (error) {
    setError(true);
    console.log(error);
  }
}

/**
 * Get the details of a deployment.
 *
 * @param {number} id the id of the deployment
 * @param {string} token the access token
 * @param {function} setDeployment the function to set the deployment
 * @param {function} setError the function to set the error if one occurred
 */
export async function getDeployment(id, token, setDeployment, setError) {
  try {
    const response = await fetch(`${API_ROUTE}/${id}`, {
      method: 'GET',
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });
    const data = await response.json();
    setDeployment(() => data);
  } catch (error) {
    setError(true);
    console.log(error);
  }
}

/**
 * Cancel an existing deployment
 *
 * @param {number} id the number of the deployment
 * @param {string} token the access token
 * @param {function} setError the function to set the error if one occurred
 * @return {Promise<boolean>} true if the request was successful
 */
export async function cancelDeployment(id, token, setError) {
  try {
    const response = await fetch(`${API_ROUTE}/${id}/cancel`, {
      method: 'PATCH',
      headers: {
        'Authorization': `Bearer ${token}`,
      },
    });
    return response.ok;
  } catch (error) {
    setError(true);
    console.log(error);
  }
}
