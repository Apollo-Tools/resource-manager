import env from '@beam-australia/react-env';
import {handleApiCall, setResult} from './ApiHandler';
const API_ROUTE = `${env('API_URL')}/metrics`;

/**
 * List all existing metrics.
 *
 * @param {string} token the access token
 * @param {function} setMetrics the function to set the retrieved metrics
 * @param {function} setLoading the function to set the loading state
 * @param {function} setError the function to set the error if one occurs
 */
export async function listMetrics(token, setMetrics, setLoading, setError) {
  const apiCall = async () => {
    const response = await fetch(`${API_ROUTE}`, {
      method: 'GET',
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });
    await setResult(response, setMetrics);
  };
  await handleApiCall(apiCall, setLoading, setError);
}
