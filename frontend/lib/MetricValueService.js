import env from '@beam-australia/react-env';
const API_ROUTE = `${env('API_URL')}/resources`;

/**
 * List all metric values of a resource.
 *
 * @param {number} resourceId the id of the resource
 * @param {string} token the access token
 * @param {function} setMetricValues a function to set the retrieved metric values
 * @param {function} setError the function to set the error if one occurs
 */
export async function listResourceMetrics(resourceId, token, setMetricValues, setError) {
  try {
    const response = await fetch(`${API_ROUTE}/${resourceId}/metrics`, {
      method: 'GET',
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });
    const data = await response.json();
    setMetricValues(() => data);
  } catch (error) {
    setError(true);
    console.log(error);
  }
}

/**
 * Delete a resource's metric value.
 *
 * @param {number} resourceId the id of the resource
 * @param {number} metricId the id of the metric
 * @param {string} token the access token
 * @param {function} setError the function to set the error if one occurs
 * @return {Promise<boolean>} true if the request was successful
 */
export async function deleteResourceMetric(
    resourceId,
    metricId,
    token,
    setError,
) {
  try {
    const response = await fetch(
        `${API_ROUTE}/${resourceId}/metrics/${metricId}`, {
          method: 'DELETE',
          headers: {
            Authorization: `Bearer ${token}`,
          },
        });
    return response.ok;
  } catch (error) {
    setError(true);
    console.log(error);
  }
}
