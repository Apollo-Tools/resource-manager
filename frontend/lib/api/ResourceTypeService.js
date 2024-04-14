import env from '@beam-australia/react-env';
import {handleApiCall, setResult} from './ApiHandler';
const API_ROUTE = `${env('API_URL')}/resource-types`;

/**
 * List all existing resource types.
 *
 * @param {string} token the access token
 * @param {function} setResourceTypes the function to set the retrieved resource types
 * @param {function} setLoading the function to set the loading state
 * @param {function} setError the function to set the error if one occurred
 */
export async function listResourceTypes(token, setResourceTypes, setLoading, setError) {
  const apiCall = async () => {
    const response = await fetch(`${API_ROUTE}`, {
      method: 'GET',
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });
    await setResult(response, setResourceTypes);
  };
  await handleApiCall(apiCall, setLoading, setError);
}
