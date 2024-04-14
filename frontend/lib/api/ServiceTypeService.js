import env from '@beam-australia/react-env';
import {checkResponseOk, handleApiCall, setResult} from './ApiHandler';
const API_ROUTE = `${env('API_URL')}/service-types`;

/**
 * Create a new service type.
 *
 * @param {string} name the name of the service type
 * @param {string} token the access token
 * @param {function} setServiceType the function to set the created service type
 * @param {function} setLoading the function to set the loading state
 * @param {function} setError the function to set the error if one occurred
 */
export async function createServiceType(name, token, setServiceType, setLoading, setError) {
  const apiCall = async () => {
    const response = await fetch(`${API_ROUTE}`, {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        name: name,
      }),
    });
    await setResult(response, setServiceType);
  };
  await handleApiCall(apiCall, setLoading, setError);
}

/**
 * List all service types.
 *
 * @param {string} token the access token
 * @param {function} setServiceTypes the function to set the retrieved service types
 * @param {function} setLoading the function to set the loading state
 * @param {function} setError the function to set the error if one occurred
 */
export async function listServiceTypes(token, setServiceTypes, setLoading, setError) {
  const apiCall = async () => {
    const response = await fetch(`${API_ROUTE}`, {
      method: 'GET',
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });
    await setResult(response, setServiceTypes);
  };
  await handleApiCall(apiCall, setLoading, setError);
}

/**
 * Delete an existing service type.
 *
 * @param {number} id the id of the function type
 * @param {string} token the access token
 * @param {function} setLoading the function to set the loading state
 * @param {function} setError the function to set the error if one occurred
 * @return {Promise<boolean>} true if the request was successful
 */
export async function deleteServiceType(id, token, setLoading, setError) {
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
