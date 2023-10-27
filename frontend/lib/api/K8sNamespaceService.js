import env from '@beam-australia/react-env';
const API_ROUTE = `${env('API_URL')}/k8snamespaces`;

/**
 * List all registered namespaces.
 *
 * @param {string} token the access token
 * @param {function} setNamespaces the function to set the retrieved k8s namespaces
 * @param {function} setError the function to set the error if one occurred
 */
export async function listK8sNamespaces(token, setNamespaces, setError) {
  try {
    const response = await fetch(`${API_ROUTE}`, {
      method: 'GET',
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });
    const data = await response.json();
    setNamespaces(() => data);
  } catch (error) {
    setError(true);
    console.log(error);
  }
}
