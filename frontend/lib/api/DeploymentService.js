import env from '@beam-australia/react-env';
import {handleApiCall, setResult} from './ApiHandler';
const API_ROUTE = `${env('API_URL')}/deployments`;

/**
 * List all deployments of the currently logged-in user.
 *
 * @param {string} token the access token
 * @param {function} setDeployments the function to set the retrieved deployments
 * @param {function} setLoading the function to set the loading state
 * @param {function} setError the function to set the error if one occurred
 */
export async function listMyDeployments(token, setDeployments, setLoading, setError) {
  const apiCall = async () => {
    const response = await fetch(`${API_ROUTE}`, {
      method: 'GET',
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });
    await setResult(response, setDeployments);
  };
  await handleApiCall(apiCall, setLoading, setError);
}

/**
 * List all logs from a deployment
 *
 * @param {number} deploymentId the id of the deployment
 * @param {string} token the access token
 * @param {function} setDeploymentLogs the function to set the retrieved logs
 * @param {function} setLoading the function to set the loading state
 * @param {function} setError the function to set the error if one occurred
 */
export async function listDeploymentLogs(deploymentId, token, setDeploymentLogs, setLoading, setError) {
  const apiCall = async () => {
    const response = await fetch(`${API_ROUTE}/${deploymentId}/logs`, {
      method: 'GET',
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });
    await setResult(response, setDeploymentLogs);
  };
  await handleApiCall(apiCall, setLoading, setError);
}

/**
 * Deploy resources.
 *
 * @param {object} requestBody the request body
 * @param {string} token the access token
 * @param {function} setNewDeployment the function to set the created deployment
 * @param {function} setLoading the function to set the loading state
 * @param {function} setError the function to set the error if one occurred
 */
export async function deployResources(requestBody, token, setNewDeployment, setLoading, setError) {
  const apiCall = async () => {
    const response = await fetch(`${API_ROUTE}`, {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(requestBody),
    });
    await setResult(response, setNewDeployment);
  };
  await handleApiCall(apiCall, setLoading, setError);
}

/**
 * Get the details of a deployment.
 *
 * @param {number} id the id of the deployment
 * @param {string} token the access token
 * @param {function} setDeployment the function to set the deployment
 * @param {function} setLoading the function to set the loading state
 * @param {function} setError the function to set the error if one occurred
 */
export async function getDeployment(id, token, setDeployment, setLoading, setError) {
  const apiCall = async () => {
    const response = await fetch(`${API_ROUTE}/${id}`, {
      method: 'GET',
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });
    await setResult(response, setDeployment);
  };
  await handleApiCall(apiCall, setLoading, setError);
}

/**
 * Cancel an existing deployment
 *
 * @param {number} id the number of the deployment
 * @param {string} token the access token
 * @param {function} setLoading the function to set the loading state
 * @param {function} setError the function to set the error if one occurred
 * @return {Promise<boolean>} true if the request was successful
 */
export async function cancelDeployment(id, token, setLoading, setError) {
  const apiCall = async () => {
    const response = await fetch(`${API_ROUTE}/${id}/cancel`, {
      method: 'PATCH',
      headers: {
        'Authorization': `Bearer ${token}`,
      },
    });
    return response.ok;
  };
  await handleApiCall(apiCall, setLoading, setError);
}
