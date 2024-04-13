import env from '@beam-australia/react-env';
import {handleApiCall, setResult} from './ApiHandler';
const API_ROUTE = `${env('API_URL')}/k8snamespaces`;

/**
 * List all registered namespaces.
 *
 * @param {string} token the access token
 * @param {function} setNamespaces the function to set the retrieved k8s namespaces
 * @param {function} setLoading the function to set the loading state
 * @param {function} setError the function to set the error if one occurred
 */
export async function listK8sNamespaces(token, setNamespaces, setLoading, setError) {
  const apiCall = async () => {
    const response = await fetch(`${API_ROUTE}`, {
      method: 'GET',
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });
    await setResult(response, setNamespaces);
  };

  await handleApiCall(apiCall, setLoading, setError);
}
