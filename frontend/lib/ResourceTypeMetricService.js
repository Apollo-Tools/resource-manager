import env from '@beam-australia/react-env';
const API_ROUTE = `${env('API_URL')}/resource-types`;

/**
 * List all metrics for an existing resource type.
 *
 * @param {string} token the access token
 * @param {number} resourceTypeId the id of the resource type
 * @param {function} setMetrics the function to set the retrieved metrics
 * @param {function} setError the function to set the error if one occurred
 */
export async function listResourceTypeMetrics(token, resourceTypeId, setMetrics, setError) {
  try {
    const response = await fetch(`${API_ROUTE}/${resourceTypeId}/metrics`, {
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
