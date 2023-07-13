import env from '@beam-australia/react-env';
const API_ROUTE = `${env('API_URL')}/platforms`;

/**
 * List all metrics for an existing platform.
 *
 * @param {number} platformId the id of the platform
 * @param {string} token the access token
 * @param {function} setMetrics the function to set the retrieved metrics
 * @param {function} setError the function to set the error if one occurred
 */
export async function listPlatformMetrics(platformId, token, setMetrics, setError) {
  try {
    const response = await fetch(`${API_ROUTE}/${platformId}/metrics`, {
      method: 'GET',
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });
    const data = await response.json();
    setMetrics(() => data);
  } catch (error) {
    setError(true);
    console.log(error);
  }
}
