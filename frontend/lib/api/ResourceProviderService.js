import env from '@beam-australia/react-env';
const API_ROUTE = `${env('API_URL')}/resource-providers`;

/**
 * List all resource providers.
 *
 * @param {string} token the access token
 * @param {function} setResourceProviders the function to set the retrieved resource providers
 * @param {function} setError the function to set the error if one occurred
 */
export async function listResourceProviders(token, setResourceProviders, setError) {
  try {
    const response = await fetch(`${API_ROUTE}`, {
      method: 'GET',
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });
    const data = await response.json();
    setResourceProviders(() => data);
  } catch (error) {
    setError(true);
    console.log(error);
  }
}
