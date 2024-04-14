import env from '@beam-australia/react-env';
import {handleApiCall, setResult} from './ApiHandler';
const API_ROUTE = `${env('API_URL')}/regions`;

/**
 * List all regions.
 *
 * @param {string} token the access token
 * @param {function} setRegions the function to set the retrieved regions
 * @param {function} setLoading the function to set the loading state
 * @param {function} setError the function to set the error if one occurred
 */
export async function listRegions(token, setRegions, setLoading, setError) {
  const apiCall = async () => {
    const response = await fetch(`${API_ROUTE}`, {
      method: 'GET',
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });
    await setResult(response, setRegions);
  };
  await handleApiCall(apiCall, setLoading, setError);
}
