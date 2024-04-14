import env from '@beam-australia/react-env';
import {handleApiCall, setResult} from './ApiHandler';
const API_ROUTE = `${env('API_URL')}/runtimes`;

/**
 * List all exiting runtimes.
 *
 * @param {string} token the access token
 * @param {function} setRuntimes the function to set the retrieved runtimes
 * @param {function} setLoading the function to set the loading state
 * @param {function} setError the function to set the error if one occurred
 */
export async function listRuntimes(token, setRuntimes, setLoading, setError) {
  const apiCall = async () => {
    const response = await fetch(`${API_ROUTE}`, {
      method: 'GET',
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });
    await setResult(response, setRuntimes);
  };
  await handleApiCall(apiCall, setLoading, setError);
}

/**
 * Get the function template for a runtime.
 *
 * @param {number} id the id of the runtime
 * @param {string} token the access token
 * @param {function} setTemplate the function to set the retrieved template
 * @param {function} setLoading the function to set the loading state
 * @param {function} setError the function to set the error if one occurred
 */
export async function getRuntimeTemplate(id, token, setTemplate, setLoading, setError) {
  const apiCall = async () => {
    const response = await fetch(`${API_ROUTE}/${id}/template`, {
      method: 'GET',
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });
    await setResult(response, setTemplate);
  };
  await handleApiCall(apiCall, setLoading, setError);
}
