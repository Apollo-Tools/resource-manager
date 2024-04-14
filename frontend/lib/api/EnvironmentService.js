import env from '@beam-australia/react-env';
import {handleApiCall, setResult} from './ApiHandler';
const API_ROUTE = `${env('API_URL')}/environments`;

/**
 * List all exiting environments.
 *
 * @param {string} token the access token
 * @param {function} setEnvironments the function to set the retrieved environments
 * @param {function} setLoading the function to set the loading state
 * @param {function} setError the function to set the error if one occurred
 */
export async function listEnvironments(token, setEnvironments, setLoading, setError) {
  const apiCall = async () => {
    const response = await fetch(`${API_ROUTE}`, {
      method: 'GET',
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });
    await setResult(response, setEnvironments);
  };
  await handleApiCall(apiCall, setLoading, setError);
}
