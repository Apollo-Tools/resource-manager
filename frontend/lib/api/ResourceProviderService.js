import env from '@beam-australia/react-env';
import {handleApiCall, setResult} from './ApiHandler';
const API_ROUTE = `${env('API_URL')}/resource-providers`;

/**
 * List all resource providers.
 *
 * @param {string} token the access token
 * @param {function} setResourceProviders the function to set the retrieved resource providers
 * @param {function} setLoading the function to set the loading state
 * @param {function} setError the function to set the error if one occurred
 */
export async function listResourceProviders(token, setResourceProviders, setLoading, setError) {
  const apiCall = async () => {
    const response = await fetch(`${API_ROUTE}`, {
      method: 'GET',
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });
    await setResult(response, setResourceProviders);
  };
  await handleApiCall(apiCall, setLoading, setError);
}
