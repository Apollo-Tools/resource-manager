import env from '@beam-australia/react-env';
import {checkResponseOk, handleApiCall, setResult} from './ApiHandler';
const API_ROUTE = `${env('API_URL')}/resources`;

/**
 * List all metric values of a resource.
 *
 * @param {number} resourceId the id of the resource
 * @param {string} token the access token
 * @param {function} setMetricValues a function to set the retrieved metric values
 * @param {function} setLoading the function to set the loading state
 * @param {function} setError the function to set the error if one occurs
 */
export async function listResourceMetrics(resourceId, token, setMetricValues, setLoading, setError) {
  const apiCall = async () => {
    const response = await fetch(`${API_ROUTE}/${resourceId}/metrics`, {
      method: 'GET',
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });
    await setResult(response, setMetricValues);
  };
  await handleApiCall(apiCall, setLoading, setError);
}

/**
 * Delete a resource's metric value.
 *
 * @param {number} resourceId the id of the resource
 * @param {number} metricId the id of the metric
 * @param {string} token the access token
 * @param {function} setLoading the function to set the loading state
 * @param {function} setError the function to set the error if one occurs
 * @return {Promise<boolean>} true if the request was successful
 */
export async function deleteResourceMetric(resourceId, metricId, token, setLoading, setError) {
  const apiCall = async () => {
    const response = await fetch(
        `${API_ROUTE}/${resourceId}/metrics/${metricId}`, {
          method: 'DELETE',
          headers: {
            Authorization: `Bearer ${token}`,
          },
        });
    return checkResponseOk(response);
  };
  return await handleApiCall(apiCall, setLoading, setError);
}
