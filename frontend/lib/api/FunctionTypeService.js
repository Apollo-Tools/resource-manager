import env from '@beam-australia/react-env';
import {checkResponseOk, handleApiCall, setResult} from './ApiHandler';
const API_ROUTE = `${env('API_URL')}/function-types`;

/**
 * Create a new function type.
 *
 * @param {string} name the name of the function type
 * @param {string} token the access token
 * @param {function} setFunctionType the function to set the created function type
 * @param {function} setLoading the function to set the loading state
 * @param {function} setError the function to set the error if one occurred
 */
export async function createFunctionType(name, token, setFunctionType, setLoading, setError) {
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
    await setResult(response, setFunctionType);
  };
  await handleApiCall(apiCall, setLoading, setError);
}

/**
 * List all function types.
 *
 * @param {string} token the access token
 * @param {function} setFunctionTypes the function to set the retrieved function types
 * @param {function} setLoading the function to set the loading state
 * @param {function} setError the function to set the error if one occurred
 */
export async function listFunctionTypes(token, setFunctionTypes, setLoading, setError) {
  const apiCall = async () => {
    const response = await fetch(`${API_ROUTE}`, {
      method: 'GET',
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });
    await setResult(response, setFunctionTypes);
  };
  await handleApiCall(apiCall, setLoading, setError);
}

/**
 * Delete an existing function type.
 *
 * @param {number} id the id of the function type
 * @param {string} token the access token
 * @param {function} setLoading the function to set the loading state
 * @param {function} setError the function to set the error if one occurred
 * @return {Promise<boolean>} true if the request was successful
 */
export async function deleteFunctionType(id, token, setLoading, setError) {
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
