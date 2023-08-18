import env from '@beam-australia/react-env';
const API_ROUTE = `${env('API_URL')}/k8s-service-types`;

/**
 * List all supported k8s service types.
 *
 * @param {string} token the access token
 * @param {function} setServiceTypes the function to set the retrieved k8s service types
 * @param {function} setError the function to set the error if one occurred
 */
export async function listK8sServiceTypes(token, setServiceTypes, setError) {
  try {
    const response = await fetch(`${API_ROUTE}`, {
      method: 'GET',
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });
    const data = await response.json();
    setServiceTypes(() => data);
  } catch (error) {
    setError(true);
    console.log(error);
  }
}
