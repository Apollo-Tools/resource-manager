import env from '@beam-australia/react-env';
import {handleApiCall, setResult} from './ApiHandler';
const API_ROUTE = `${env('API_URL')}/k8s-service-types`;

/**
 * List all supported k8s service types.
 *
 * @param {string} token the access token
 * @param {function} setServiceTypes the function to set the retrieved k8s service types
 * @param {function} setLoading the function to set the loading state
 * @param {function} setError the function to set the error if one occurred
 */
export async function listK8sServiceTypes(token, setServiceTypes, setLoading, setError) {
  const apiCall = async () => {
    const response = await fetch(`${API_ROUTE}`, {
      method: 'GET',
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });
    await setResult(response, setServiceTypes);
  };
  await handleApiCall(apiCall, setLoading, setError);
}
